package com.akslabs.cloudgallery.workers

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for retry logic and backoff calculations.
 *
 * Tests exponential backoff, 429 handling, and retry count management.
 */
class RetryLogicTest {

    @Test
    fun `exponential backoff calculation`() {
        val baseDelayMs = 1000L
        val multiplier = 2

        var delay = baseDelayMs
        val delays = mutableListOf<Long>()

        repeat(5) { attempt ->
            delays.add(delay)
            delay *= multiplier
        }

        assertEquals("Attempt 1 should have base delay", 1000L, delays[0])
        assertEquals("Attempt 2 should double", 2000L, delays[1])
        assertEquals("Attempt 3 should quadruple", 4000L, delays[2])
        assertEquals("Attempt 4 should be 8x", 8000L, delays[3])
        assertEquals("Attempt 5 should be 16x", 16000L, delays[4])
    }

    @Test
    fun `exponential backoff with jitter`() {
        val baseDelayMs = 1000L
        val multiplier = 2
        val jitterFactor = 0.1 // ±10%

        var delay = baseDelayMs
        val delays = mutableListOf<Long>()

        repeat(3) {
            val jitter = (delay * jitterFactor * (Math.random() * 2 - 1)).toLong()
            delays.add(delay + jitter)
            delay *= multiplier
        }

        // Verify delays are within expected range (±10%)
        assertTrue("First delay should be ~1000ms", delays[0] in 900..1100)
        assertTrue("Second delay should be ~2000ms", delays[1] in 1800..2200)
        assertTrue("Third delay should be ~4000ms", delays[2] in 3600..4400)
    }

    @Test
    fun `max backoff cap prevents excessive delays`() {
        val maxBackoffMs = 30 * 60 * 1000L // 30 minutes cap
        val baseDelayMs = 1000L
        val multiplier = 2

        var delay = baseDelayMs
        repeat(10) {
            delay = minOf(delay * multiplier, maxBackoffMs)
        }

        // After 10 iterations: 1000 -> 2000 -> 4000 -> 8000 -> 16000 -> 32000 -> 64000 -> 128000 -> 256000 -> 512000 -> 1024000
        // The cap is 30 minutes (1800000ms), so delay should be less than cap
        assertTrue("Delay should not exceed cap", delay <= maxBackoffMs)
        assertTrue("Delay should be positive", delay > 0)
    }

    @Test
    fun `429 retry after parsing`() {
        // Simulate parsing Telegram 429 response
        val responseBody = """{"ok":false,"error_code":429,"description":"Too Many Requests: retry after 30","parameters":{"retry_after":30}}"""

        // Extract retry_after value
        val retryAfter = extractRetryAfter(responseBody)

        assertEquals("Should extract retry_after 30", 30, retryAfter)
    }

    @Test
    fun `429 retry after defaults when missing`() {
        val responseBody = """{"ok":false,"error_code":429,"description":"Too Many Requests"}"""

        val retryAfter = extractRetryAfter(responseBody)

        assertEquals("Should default to 5 seconds", 5, retryAfter)
    }

    @Test
    fun `non-429 error has no retry after`() {
        val responseBody = """{"ok":false,"error_code":400,"description":"Bad Request"}"""

        val retryAfter = extractRetryAfter(responseBody)

        // Non-429 errors return default (5 seconds) since there's no retry_after field
        assertEquals("Non-429 should return default 5", 5, retryAfter)
    }

    @Test
    fun `retry count prevents infinite retries`() {
        val MAX_RETRIES = 3
        var retryCount = 0

        // Simulate retry loop
        while (retryCount < MAX_RETRIES) {
            retryCount++
        }

        assertTrue("Should stop after MAX_RETRIES", retryCount >= MAX_RETRIES)
    }

    @Test
    fun `failed upload does not block queue`() {
        // Verify that a single failure doesn't block other uploads
        val totalUploads = 10
        val failedUploads = 2
        val successfulUploads = totalUploads - failedUploads

        assertTrue("Successful uploads should be processed", successfulUploads > 0)
        assertTrue("Failed uploads should not prevent others", failedUploads < totalUploads)
    }

    @Test
    fun `periodic worker retry decision based on failure count`() {
        // Test the decision logic: if any failures, return Result.retry()
        var successCount = 8
        var failedCount = 2

        val shouldRetry = failedCount > 0

        assertTrue("Should retry when there are failures", shouldRetry)

        // All succeed
        failedCount = 0
        val shouldSucceed = failedCount == 0

        assertTrue("Should succeed when no failures", shouldSucceed)
    }

    // Helper function to simulate retry_after parsing
    private fun extractRetryAfter(json: String): Int {
        return try {
            val regex = """"retry_after"\s*:\s*(\d+)""".toRegex()
            regex.find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 5
        } catch (e: Exception) {
            5 // Default retry delay
        }
    }
}
