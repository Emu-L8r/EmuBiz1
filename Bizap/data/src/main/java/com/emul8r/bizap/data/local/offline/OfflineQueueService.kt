package com.emul8r.bizap.data.local.offline

import com.emul8r.bizap.data.local.dao.OfflineOperationDao
import com.emul8r.bizap.data.local.entities.OfflineOperation
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main service managing the offline operations queue.
 * Part of Phase 2: Offline-First Reliability.
 */
@Singleton
class OfflineQueueService @Inject constructor(
    private val dao: OfflineOperationDao,
    private val invoiceRepository: InvoiceRepository
) {
    
    // In-memory cache of pending operations
    private val pendingCache = mutableListOf<OfflineOperation>()
    private val cacheUpdateMutex = Mutex()
    
    // State Flow for UI observation
    private val _queueState = MutableStateFlow(QueueState())
    val queueState: StateFlow<QueueState> = _queueState.asStateFlow()
    
    // Initialize cache on first use
    suspend fun initialize(businessId: Long) {
        cacheUpdateMutex.withLock {
            pendingCache.clear()
            pendingCache.addAll(dao.getPendingOperations(businessId))
            updateStateFlow(businessId)
            Timber.i("✅ Queue initialized with ${pendingCache.size} pending operations")
        }
    }
    
    /**
     * Queue a new invoice creation
     */
    suspend fun queueCreateInvoice(invoice: Invoice): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "CREATE_INVOICE",
                entityId = invoice.id,
                entityData = OperationSerializer.serializeInvoice(invoice),
                businessProfileId = invoice.businessProfileId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(invoice.businessProfileId)
            }
            
            Timber.d("📝 Queued CREATE_INVOICE: $id for invoice ${invoice.id}")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue invoice creation")
            throw e
        }
    }

    /**
     * Queue a new customer creation
     */
    suspend fun queueCreateCustomer(customer: Customer): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "CREATE_CUSTOMER",
                entityId = customer.id,
                entityData = OperationSerializer.serializeCustomer(customer),
                businessProfileId = 1L,
                status = "PENDING"
            )
            val id = dao.insert(operation)
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
            }
            Timber.d("📝 Queued CREATE_CUSTOMER: $id for customer ${customer.id}")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue customer creation")
            throw e
        }
    }

    /**
     * Queue a customer update
     */
    suspend fun queueUpdateCustomer(customer: Customer): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "UPDATE_CUSTOMER",
                entityId = customer.id,
                entityData = OperationSerializer.serializeCustomer(customer),
                businessProfileId = 1L,
                status = "PENDING"
            )
            val id = dao.insert(operation)
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
            }
            Timber.d("📝 Queued UPDATE_CUSTOMER: $id for customer ${customer.id}")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue customer update")
            throw e
        }
    }

    /**
     * Queue a customer deletion
     */
    suspend fun queueDeleteCustomer(customerId: Long, businessId: Long): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "DELETE_CUSTOMER",
                entityId = customerId,
                entityData = "",
                businessProfileId = businessId,
                status = "PENDING"
            )
            val id = dao.insert(operation)
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
            }
            Timber.d("🗑️ Queued DELETE_CUSTOMER: $id for customer $customerId")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue customer deletion")
            throw e
        }
    }

    /**
     * Queue an invoice update
     */
    suspend fun queueUpdateInvoice(invoice: Invoice): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "UPDATE_INVOICE",
                entityId = invoice.id,
                entityData = OperationSerializer.serializeInvoice(invoice),
                businessProfileId = invoice.businessProfileId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(invoice.businessProfileId)
            }
            
            Timber.d("📝 Queued UPDATE_INVOICE: $id for invoice ${invoice.id}")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue invoice update")
            throw e
        }
    }
    
    /**
     * Queue a payment record
     */
    suspend fun queueRecordPayment(invoiceId: Long, amountPaid: Long, businessId: Long): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "UPDATE_PAYMENT",
                entityId = invoiceId,
                entityData = OperationSerializer.serializePayment(invoiceId, amountPaid),
                businessProfileId = businessId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(businessId)
            }
            
            Timber.d("💰 Queued RECORD_PAYMENT: $id for invoice $invoiceId")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue payment")
            throw e
        }
    }

    /**
     * Queue a status update
     */
    suspend fun queueStatusUpdate(invoiceId: Long, status: InvoiceStatus, businessId: Long): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "UPDATE_STATUS",
                entityId = invoiceId,
                entityData = Json.encodeToString(mapOf("status" to status.toString())),
                businessProfileId = businessId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(businessId)
            }
            
            Timber.d("📋 Queued UPDATE_STATUS: $id for invoice $invoiceId")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue status update")
            throw e
        }
    }
    
    /**
     * Queue an invoice deletion
     */
    suspend fun queueDeleteInvoice(invoiceId: Long, businessId: Long): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "DELETE_INVOICE",
                entityId = invoiceId,
                entityData = "",
                businessProfileId = businessId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(businessId)
            }
            
            Timber.d("🗑️ Queued DELETE_INVOICE: $id for invoice $invoiceId")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue deletion")
            throw e
        }
    }
    
    /**
     * Get all pending operations for a business (FIFO order)
     */
    suspend fun getPendingOperations(businessId: Long): List<OfflineOperation> {
        return try {
            val ops = dao.getPendingOperations(businessId)
            Timber.d("📋 Found ${ops.size} pending operations for business $businessId")
            ops
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get pending operations")
            emptyList()
        }
    }
    
    /**
     * Mark operation as syncing
     */
    suspend fun markSyncing(operationId: Long) {
        try {
            dao.updateStatus(operationId, "SYNCING")
            Timber.d("⏳ Marked operation $operationId as SYNCING")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to mark as syncing")
        }
    }
    
    /**
     * Mark operation as successfully synced
     */
    suspend fun markSynced(operationId: Long) {
        try {
            dao.updateStatus(operationId, "SYNCED")
            Timber.d("✅ Marked operation $operationId as SYNCED")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to mark as synced")
        }
    }
    
    /**
     * Mark operation as failed with error message
     */
    suspend fun markFailed(operationId: Long, errorMessage: String) {
        try {
            val op = dao.getById(operationId)
            if (op != null) {
                val updated = op.copy(
                    status = "FAILED",
                    errorMessage = errorMessage,
                    retryCount = op.retryCount + 1
                )
                dao.update(updated)
                Timber.e("❌ Operation $operationId failed: $errorMessage (retry ${updated.retryCount})")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to mark as failed")
        }
    }
    
    /**
     * Get failed operations for retry
     */
    suspend fun getFailedOperations(): List<OfflineOperation> {
        return try {
            dao.getFailedOperations()
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get failed operations")
            emptyList()
        }
    }
    
    /**
     * Clean up successfully synced operations
     */
    suspend fun cleanupSyncedOperations(businessId: Long) {
        try {
            dao.deleteSuccessfullySyncedOperations(businessId)
            Timber.d("🧹 Cleaned up synced operations for business $businessId")
            updateStateFlow(businessId)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to cleanup operations")
        }
    }
    
    /**
     * Update the state flow with current queue status
     */
    private suspend fun updateStateFlow(businessId: Long) {
        try {
            val pending = dao.getPendingOperations(businessId)
            val failed = dao.getFailedOperations()
            
            _queueState.value = QueueState(
                totalPending = pending.size,
                failedCount = failed.size,
                lastSyncTime = System.currentTimeMillis(),
                isSyncing = false,
                errorMessage = null
            )
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update state flow")
        }
    }
}
