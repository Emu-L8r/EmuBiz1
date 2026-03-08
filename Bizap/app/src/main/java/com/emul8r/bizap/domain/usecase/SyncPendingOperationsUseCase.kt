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
     */
    suspend operator fun invoke() {
        Timber.d("🔄 SyncPendingOperationsUseCase: Starting sync…")

        val pending = offlineQueueRepository.getPendingOperations().first()

        if (pending.isEmpty()) {
            Timber.d("✅ SyncPendingOperationsUseCase: No pending operations")
            return
        }

        Timber.d("📋 Processing ${pending.size} pending operation(s)…")

        for (operation in pending) {
            processOperation(operation)
        }

        offlineQueueRepository.clearCompleted()
        Timber.d("✅ SyncPendingOperationsUseCase: Sync complete")
    }

    /**
     * Applies a single pending operation.
     *
     * Dispatches to the appropriate handler based on operation type.
     * Handles retryable vs non-retryable errors appropriately.
     *
     * @throws SyncException.Retryable to signal the worker to retry
     * @throws SyncException.NonRetryable to mark operation as permanently failed
     */
    private suspend fun processOperation(operation: PendingOperation) {
        Timber.d("⚙️ Processing ${operation.operationType} on ${operation.entityType}#${operation.entityId}")
        try {
            // Dispatch to appropriate handler
            dispatcher.dispatch(operation)

            // Mark as successfully synced
            offlineQueueRepository.markCompleted(operation.id)
            Timber.d("✅ Operation #${operation.id} synced successfully")
        } catch (e: SyncOperationDispatcher.SyncException.Retryable) {
            // Retryable error - let SyncWorker handle retry logic
            Timber.w("⚠️ Retryable error for operation #${operation.id}: ${e.message}")
            throw e
        } catch (e: SyncOperationDispatcher.SyncException.NonRetryable) {
            // Non-retryable error - mark as permanently failed
            Timber.e("❌ Non-retryable error for operation #${operation.id}: ${e.message}")
            offlineQueueRepository.markFailed(operation.id, e.message ?: "Unknown error")
        } catch (e: Exception) {
            // Unexpected error - treat as retryable (network issue)
            Timber.e(e, "❌ Unexpected error for operation #${operation.id}")
            throw SyncOperationDispatcher.SyncException.Retryable("Unexpected error: ${e.message}")
        }
    }
}

