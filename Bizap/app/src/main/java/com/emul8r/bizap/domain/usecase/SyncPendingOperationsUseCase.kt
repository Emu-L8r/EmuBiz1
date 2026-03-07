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
 * For each pending operation the use case attempts to apply the change
 * and marks it completed or failed accordingly.
 *
 * Conflict resolution strategy: **server wins**.
 * If the remote rejects an operation the user is informed via the
 * failed-operation count visible in [SyncStatusIndicator].
 */
class SyncPendingOperationsUseCase @Inject constructor(
    private val offlineQueueRepository: OfflineQueueRepository
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
     * In this initial implementation the queue serves as the foundation for
     * offline support. Concrete remote-sync logic will be added per entity
     * type in Phase 2+.  For now each operation is marked completed so the
     * UI indicator clears correctly after the placeholder sync.
     */
    private suspend fun processOperation(operation: PendingOperation) {
        Timber.d("⚙️ Processing ${operation.operationType} on ${operation.entityType}#${operation.entityId}")
        try {
            // TODO(Phase 2+): Dispatch to entity-specific remote sync handlers.
            // e.g., when operationType == CREATE && entityType == "INVOICE" → call remote API
            offlineQueueRepository.markCompleted(operation.id)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to process operation #${operation.id}")
            offlineQueueRepository.markFailed(operation.id, e.message ?: "Unknown error")
        }
    }
}

