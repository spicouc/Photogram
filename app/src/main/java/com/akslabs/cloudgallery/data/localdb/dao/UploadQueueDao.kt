package com.akslabs.cloudgallery.data.localdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.akslabs.cloudgallery.data.localdb.entities.UploadQueue
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadQueueDao {
    @Insert
    suspend fun insert(item: UploadQueue): Long

    @Query("SELECT * FROM upload_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPending(): List<UploadQueue>

    @Query("SELECT * FROM upload_queue WHERE localId = :localId AND status IN ('PENDING','IN_PROGRESS')")
    suspend fun getActiveForPhoto(localId: String): UploadQueue?

    /**
     * Latest queue row for a photo, regardless of status.
     *
     * Workers reuse this row instead of inserting a new one on every attempt, which
     * is what makes [retryCount] meaningful and stops permanently failing photos
     * from being retried forever with a fresh counter.
     */
    @Query("SELECT * FROM upload_queue WHERE localId = :localId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestForPhoto(localId: String): UploadQueue?

    @Query("SELECT * FROM upload_queue WHERE contentHash = :hash AND status IN ('PENDING','IN_PROGRESS','DONE')")
    suspend fun getByContentHash(hash: String): UploadQueue?

    @Query("UPDATE upload_queue SET status = 'IN_PROGRESS', startedAt = :startedAt WHERE id = :id")
    suspend fun markInProgress(id: Long, startedAt: Long = System.currentTimeMillis())

    @Query("UPDATE upload_queue SET status = 'DONE', completedAt = :completedAt WHERE id = :id")
    suspend fun markDone(id: Long, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE upload_queue SET status = 'FAILED', errorMessage = :error, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun markFailed(id: Long, error: String?)

    @Query("UPDATE upload_queue SET status = 'CANCELLED' WHERE id = :id")
    suspend fun markCancelled(id: Long)

    /** Explicit user retry: give a single photo a fresh set of automatic attempts. */
    @Query("UPDATE upload_queue SET status = 'PENDING', errorMessage = null, retryCount = 0 WHERE id = :id")
    suspend fun resetForRetry(id: Long)

    /** Explicit user retry of every cancelled upload. */
    @Query(
        """
        UPDATE upload_queue SET status = 'PENDING', errorMessage = null, retryCount = 0
        WHERE status = 'CANCELLED'
          AND localId IN (SELECT localId FROM photos WHERE remoteId IS NULL)
        """
    )
    suspend fun resetAllCancelledToPending(): Int

    // An interrupted worker can leave a row IN_PROGRESS forever.
    // Only reclaim entries older than the timeout to avoid stealing live uploads.
    @Query("UPDATE upload_queue SET status = 'FAILED', errorMessage = 'Interrupted upload; eligible for retry', retryCount = retryCount + 1 WHERE status = 'IN_PROGRESS' AND startedAt < :cutoff")
    suspend fun failStaleInProgress(cutoff: Long): Int

    @Query("SELECT * FROM upload_queue WHERE id = :id")
    suspend fun getById(id: Long): UploadQueue?

    @Query("DELETE FROM upload_queue WHERE status = 'DONE' AND completedAt < :before")
    suspend fun cleanupOld(before: Long)

    @Query("SELECT * FROM upload_queue ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<UploadQueue>>
}
