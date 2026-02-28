package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.PendingOperationDao
import com.emul8r.bizap.data.local.entities.PendingOperation
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the queue of pending operations.
 * Operations are stored locally when offline, synced when online.
 */
@Singleton
class OfflineSyncQueue @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val json: Json
) {
    
    /**
     * Queues an operation for future synchronization.
     */
    suspend fun queueOperation(
        operationType: String, // CREATE, UPDATE, DELETE
        entityType: String, // INVOICE, CUSTOMER, etc.
        businessProfileId: Long,
        payload: String, // Already serialized JSON
        entityId: Long? = null
    ): String {
        val operation = PendingOperation(
            id = UUID.randomUUID().toString(),
            operationType = operationType,
            entityType = entityType,
            entityId = entityId,
            businessProfileId = businessProfileId,
            payload = payload,
            status = "PENDING"
        )
        
        pendingOperationDao.insertOperation(operation)
        Timber.d("📥 Operation Queued: $operationType $entityType (ID: ${operation.id})")
        return operation.id
    }

    suspend fun markSynced(id: String) {
        pendingOperationDao.updateStatus(id, "SYNCED")
        Timber.d("✅ Operation Synced: $id")
    }

    suspend fun markFailed(id: String, error: String) {
        pendingOperationDao.updateStatus(id, "FAILED")
        Timber.e("❌ Operation Failed: $id - $error")
    }

    suspend fun clearSyncedOperations() {
        // Logic to be implemented in DAO if needed, or iterate
    }
}
