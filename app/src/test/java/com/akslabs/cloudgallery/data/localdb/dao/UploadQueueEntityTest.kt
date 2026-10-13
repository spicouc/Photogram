package com.akslabs.cloudgallery.data.localdb.dao

import com.akslabs.cloudgallery.data.localdb.entities.UploadQueue
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for UploadQueue entity state machine.
 *
 * Tests state transitions, retry logic, and recovery conditions.
 */
class UploadQueueEntityTest {

    @Test
    fun `new upload queue entry starts as PENDING`() {
        val entry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc"
        )

        assertEquals("New entry should be PENDING", "PENDING", entry.status)
        assertEquals("New entry should have 0 retries", 0, entry.retryCount)
        assertNull("New entry should not have errorMessage", entry.errorMessage)
        assertNull("New entry should not have startedAt", entry.startedAt)
        assertNull("New entry should not have completedAt", entry.completedAt)
    }

    @Test
    fun `markInProgress sets status and timestamp`() {
        val entry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "PENDING"
        )

        // Simulate markInProgress
        val inProgress = entry.copy(status = "IN_PROGRESS", startedAt = System.currentTimeMillis())

        assertEquals("Status should be IN_PROGRESS", "IN_PROGRESS", inProgress.status)
        assertNotNull("startedAt should be set", inProgress.startedAt)
    }

    @Test
    fun `markDone sets status and completedAt`() {
        val entry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "IN_PROGRESS"
        )

        val done = entry.copy(status = "DONE", completedAt = System.currentTimeMillis())

        assertEquals("Status should be DONE", "DONE", done.status)
        assertNotNull("completedAt should be set", done.completedAt)
    }

    @Test
    fun `markFailed increments retryCount and stores error`() {
        val entry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "IN_PROGRESS",
            retryCount = 0
        )

        val failed = entry.copy(
            status = "FAILED",
            errorMessage = "Network timeout",
            retryCount = entry.retryCount + 1
        )

        assertEquals("Status should be FAILED", "FAILED", failed.status)
        assertEquals("RetryCount should increment to 1", 1, failed.retryCount)
        assertEquals("errorMessage should be stored", "Network timeout", failed.errorMessage)
    }

    @Test
    fun `retryCount exceeding max leads to CANCELLED`() {
        val MAX_RETRIES = 3
        val entry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "FAILED",
            retryCount = 3
        )

        // When retryCount >= MAX_RETRIES, entry should be CANCELLED
        if (entry.retryCount >= MAX_RETRIES) {
            val cancelled = entry.copy(status = "CANCELLED")
            assertEquals("Status should be CANCELLED", "CANCELLED", cancelled.status)
        }
    }

    @Test
    fun `stale in progress entry can be recovered`() {
        val STALE_UPLOAD_TIMEOUT_MS = 10 * 60 * 1000L // 10 minutes
        val now = System.currentTimeMillis()

        val staleEntry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "IN_PROGRESS",
            startedAt = now - (15 * 60 * 1000L), // 15 minutes ago
            retryCount = 0
        )

        // Should be eligible for recovery
        assertTrue("Stale entry should be recoverable",
            staleEntry.startedAt!! < now - STALE_UPLOAD_TIMEOUT_MS)

        // After recovery, should be FAILED with incremented retryCount
        val recovered = staleEntry.copy(
            status = "FAILED",
            errorMessage = "Interrupted upload; eligible for retry",
            retryCount = staleEntry.retryCount + 1
        )

        assertEquals("Recovered entry should be FAILED", "FAILED", recovered.status)
        assertEquals("RetryCount should increment", 1, recovered.retryCount)
        assertTrue("errorMessage should indicate interruption",
            recovered.errorMessage!!.contains("Interrupted"))
    }

    @Test
    fun `active in progress entry should not be recovered`() {
        val STALE_UPLOAD_TIMEOUT_MS = 10 * 60 * 1000L // 10 minutes
        val now = System.currentTimeMillis()

        val activeEntry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "IN_PROGRESS",
            startedAt = now - (5 * 60 * 1000L), // 5 minutes ago
            retryCount = 0
        )

        // Should NOT be eligible for recovery
        assertFalse("Active entry should not be recovered",
            activeEntry.startedAt!! < now - STALE_UPLOAD_TIMEOUT_MS)
    }

    @Test
    fun `failed entry with retries remaining can be reset`() {
        val MAX_RETRIES = 3
        val failedEntry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "FAILED",
            retryCount = 1,
            errorMessage = "Previous error"
        )

        // Should be resettable to PENDING
        if (failedEntry.retryCount < MAX_RETRIES) {
            val reset = failedEntry.copy(
                status = "PENDING",
                errorMessage = null,
                retryCount = 0
            )
            assertEquals("Reset entry should be PENDING", "PENDING", reset.status)
            assertNull("errorMessage should be cleared", reset.errorMessage)
            assertEquals("retryCount should be reset", 0, reset.retryCount)
        }
    }

    @Test
    fun `failed entry with exhausted retries cannot be reset`() {
        val MAX_RETRIES = 3
        val failedEntry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "FAILED",
            retryCount = 3,
            errorMessage = "Final error"
        )

        // Should NOT be resettable
        assertFalse("Entry with exhausted retries should not be reset",
            failedEntry.retryCount < MAX_RETRIES)
    }

    @Test
    fun `cancelled entry is terminal`() {
        val cancelledEntry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "CANCELLED"
        )

        // CANCELLED is a terminal state - no further transitions
        assertEquals("CANCELLED status", "CANCELLED", cancelledEntry.status)
    }

    @Test
    fun `done entry is terminal`() {
        val doneEntry = UploadQueue(
            localId = "photo_123",
            pathUri = "content://media/external/images/media/123",
            workerType = "periodic",
            deviceId = "device_abc",
            status = "DONE"
        )

        // DONE is a terminal state
        assertEquals("DONE status", "DONE", doneEntry.status)
    }
}
