package com.emul8r.bizap.data.repository

import android.content.Context
import com.emul8r.bizap.data.local.dao.PendingOperationDao
import com.emul8r.bizap.data.local.entities.PendingOperationEntity
import com.emul8r.bizap.data.worker.SyncWorker
import com.emul8r.bizap.domain.model.OperationType
import com.emul8r.bizap.domain.model.PendingOperation
import com.emul8r.bizap.domain.model.PendingOperationStatus
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class OfflineQueueRepositoryImpl @Inject constructor(
    private val dao: PendingOperationDao,
    @ApplicationContext private val context: Context
) : OfflineQueueRepository {

    override suspend fun enqueue(operation: PendingOperation) {
        val entity = operation.toEntity()
        dao.insert(entity)
        Timber.d("📥 Enqueued ${operation.operationType} on ${operation.entityType}#${operation.entityId}")
    }

    override fun getPendingOperations(): Flow<List<PendingOperation>> =
        dao.observePending().map { list -> list.map { it.toDomain() } }

    override fun getPendingCount(): Flow<Int> = dao.observePendingCount()

    override suspend fun markCompleted(operationId: Long) {
        dao.markCompleted(operationId)
        Timber.d("✅ Operation #$operationId marked as completed")
    }

    override suspend fun markFailed(operationId: Long, errorMessage: String) {
        dao.markFailed(
            id = operationId,
            nowMs = System.currentTimeMillis(),
            errorMessage = errorMessage
        )
        Timber.w("⚠️ Operation #$operationId marked as failed: $errorMessage")
    }

    override suspend fun clearCompleted() {
        dao.deleteCompleted()
        Timber.d("🧹 Cleared completed operations from queue")
    }

    override suspend fun triggerSync() {
        Timber.d("🚀 Triggering immediate sync via WorkManager")
        SyncWorker.enqueueOneShot(context)
    }

    // ── Mapping helpers ──────────────────────────────────────────────────────

    private fun PendingOperation.toEntity() = PendingOperationEntity(
        id = id,
        operationType = operationType.name,
        entityType = entityType,
        entityId = entityId,
        payload = payload,
        createdAt = createdAt,
        attemptCount = attemptCount,
        lastAttemptAt = lastAttemptAt,
        status = status.name,
        errorMessage = errorMessage
    )

    private fun PendingOperationEntity.toDomain() = PendingOperation(
        id = id,
        operationType = OperationType.valueOf(operationType),
        entityType = entityType,
        entityId = entityId,
        payload = payload,
        createdAt = createdAt,
        attemptCount = attemptCount,
        lastAttemptAt = lastAttemptAt,
        status = PendingOperationStatus.valueOf(status),
        errorMessage = errorMessage
    )
}
