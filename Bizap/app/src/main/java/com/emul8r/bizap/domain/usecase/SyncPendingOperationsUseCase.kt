package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.domain.model.PendingOperation
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

/**
 * Orchestrates processing of the offline operation queue.
 *
 * Called by [SyncWorker] once network connectivity is available.
 * For each pending operation:
 * 1. Dispatches to [SyncOperationDispatcher] to handle the sync
 * 2. Marks operation as SYNCED on success
 * 3. Marks operation as FAILED with error on error
 *
 * Conflict resolution strategy: **server wins**.
 * If the remote rejects an operation the user is informed via the
 * failed-operation count visible in the UI.
 *
 * Error handling strategy:
 * - Retryable errors (network, timeouts) → Re-throw for SyncWorker to retry
 * - Non-retryable errors (validation, conflicts) → Mark operation as FAILED
 * - Unexpected errors → Treat as retryable (likely network issue)
 */
class SyncPendingOperationsUseCase @Inject constructor(
    private val offlineQueueRepository: OfflineQueueRepository,
    private val dispatcher: SyncOperationDispatcher
) {
    /**
     * Processes all pending operations in FIFO order.
     *
     * Throws if a fatal (non-retryable) error occurs so [SyncWorker]
     * can decide whether to retry.
     *
     * **Error handling:**
     * - Retryable exceptions are re-thrown → SyncWorker will retry
     * - Non-retryable exceptions mark operation as failed → no retry
     * - Unexpected exceptions are treated as retryable → SyncWorker will retry
     */
    suspend operator fun invoke() {
        Timber.d("🔄 SyncPendingOperationsUseCase: Starting sync…")

        val pending = offlineQueueRepository.getPendingOperations().first()

        if (pending.isEmpty()) {
            Timber.d("✅ SyncPendingOperationsUseCase: No pending operations, sync complete")
            return
        }

        Timber.d("📋 Processing ${pending.size} pending operation(s) in FIFO order…")

        var successCount = 0
        var failureCount = 0

        for ((index, operation) in pending.withIndex()) {
            Timber.d("⚙️ [${index + 1}/${pending.size}] Processing ${operation.operationType} on ${operation.entityType}#${operation.entityId}")
            try {
                processOperation(operation)
                successCount++
                Timber.d("   ✅ Operation #${operation.id} synced successfully")
            } catch (e: SyncOperationDispatcher.SyncException.Retryable) {
                Timber.w("   ⚠️ Retryable error for operation #${operation.id}: ${e.message}")
                failureCount++
                // Stop processing on retryable error - will retry entire queue
                throw e
            } catch (e: SyncOperationDispatcher.SyncException.NonRetryable) {
                Timber.e("   ❌ Non-retryable error for operation #${operation.id}: ${e.message}")
                failureCount++
                // Continue processing other operations, but mark this one as failed
                offlineQueueRepository.markFailed(operation.id, e.message ?: "Unknown error")
            } catch (e: Exception) {
                Timber.e(e, "   ❌ Unexpected error for operation #${operation.id}")
                failureCount++
                // Unexpected error - mark as failed to prevent getting stuck
                offlineQueueRepository.markFailed(operation.id, "Unexpected error: ${e.message ?: "Unknown"}")
            }
        }

        try {
            offlineQueueRepository.clearCompleted()
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Failed to clear completed operations")
        }

        Timber.i("✅ SyncPendingOperationsUseCase: Sync complete. Success: $successCount, Failed: $failureCount")
    }

    /**
     * Applies a single pending operation.
     *
     * Dispatches to the appropriate handler based on operation type.
     * Handles retryable vs non-retryable errors appropriately.
     *
     * **Throws:**
     * - [SyncOperationDispatcher.SyncException.Retryable] to signal the worker to retry
     * - [SyncOperationDispatcher.SyncException.NonRetryable] to mark operation as permanently failed
     *
     * @param operation The operation to process
     * @throws SyncException if processing fails
     */
    private suspend fun processOperation(operation: PendingOperation) {
        try {
            Timber.d("   📤 Dispatching ${operation.operationType} on ${operation.entityType}#${operation.entityId}…")
            // Dispatch to appropriate handler
            dispatcher.dispatch(operation)

            // Mark as successfully synced
            offlineQueueRepository.markCompleted(operation.id)
        } catch (e: SyncOperationDispatcher.SyncException.Retryable) {
            // Retryable error - let SyncWorker handle retry logic
            Timber.w(e, "   ⚠️ Retryable sync error for operation #${operation.id}")
            throw e
        } catch (e: SyncOperationDispatcher.SyncException.NonRetryable) {
            // Non-retryable error - mark as permanently failed, don't retry
            Timber.e(e, "   ❌ Non-retryable sync error for operation #${operation.id}")
            offlineQueueRepository.markFailed(operation.id, e.message ?: "Unknown error")
        } catch (e: Exception) {
            // Unexpected error - treat as non-retryable to avoid infinite loops
            Timber.e(e, "   ❌ Unexpected error during operation #${operation.id} sync")
            offlineQueueRepository.markFailed(operation.id, "Unexpected error: ${e.javaClass.simpleName}: ${e.message ?: "Unknown"}")
        }
    }
}

