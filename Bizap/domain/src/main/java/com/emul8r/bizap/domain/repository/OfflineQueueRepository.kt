package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.PendingOperation
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing the offline operation queue.
 * Operations are stored locally and synced to the remote backend
 * when connectivity is available.
 */
interface OfflineQueueRepository {

    /**
     * Enqueues an operation to be synced when online.
     */
    suspend fun enqueue(operation: PendingOperation)

    /**
     * Returns a live stream of all pending (unprocessed) operations.
     */
    fun getPendingOperations(): Flow<List<PendingOperation>>

    /**
     * Returns a live count of pending operations, useful for the UI indicator.
     */
    fun getPendingCount(): Flow<Int>

    /**
     * Marks an operation as completed and removes it from the active queue.
     */
    suspend fun markCompleted(operationId: Long)

    /**
     * Marks an operation as failed and stores the error message for diagnostics.
     * Increments the attempt counter so callers can implement retry limits.
     */
    suspend fun markFailed(operationId: Long, errorMessage: String)

    /**
     * Removes all completed operations from persistent storage.
     */
    suspend fun clearCompleted()

    /**
     * Requests an immediate sync attempt via WorkManager.
     */
    suspend fun triggerSync()
}
