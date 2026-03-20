package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the offline operation queue.
 */
@Dao
interface PendingOperationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingOperationEntity): Long

    @Query("SELECT * FROM pending_operations WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun observePending(): Flow<List<PendingOperationEntity>>

    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM pending_operations WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPending(): List<PendingOperationEntity>

    @Query("""
        UPDATE pending_operations
        SET status = 'COMPLETED'
        WHERE id = :id
    """)
    suspend fun markCompleted(id: Long)

    @Query("""
        UPDATE pending_operations
        SET status       = 'FAILED',
            attemptCount = attemptCount + 1,
            lastAttemptAt = :nowMs,
            errorMessage = :errorMessage
        WHERE id = :id
    """)
    suspend fun markFailed(id: Long, nowMs: Long, errorMessage: String)

    @Query("DELETE FROM pending_operations WHERE status = 'COMPLETED'")
    suspend fun deleteCompleted()
}
