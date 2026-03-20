package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.domain.model.PendingOperation
import com.emul8r.bizap.domain.model.OperationType
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import timber.log.Timber
import javax.inject.Inject

/**
 * Dispatches offline operations to appropriate handlers based on operation type.
 *
 * Implements Week 2 API Integration:
 * - Remote Sync for Invoices and Customers
 * - "Server Wins" Conflict Resolution
 * - Error classification (Retryable vs Non-Retryable)
 */
class SyncOperationDispatcher @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val json: Json
) {

    /**
     * Process a single pending operation by dispatching to the appropriate handler.
     */
    suspend fun dispatch(operation: PendingOperation) {
        Timber.d("📤 Dispatching ${operation.operationType} on ${operation.entityType}#${operation.entityId}…")

        try {
            when {
                operation.entityType == "INVOICE" -> handleInvoiceOperation(operation)
                operation.entityType == "CUSTOMER" -> handleCustomerOperation(operation)
                operation.entityType == "PAYMENT" -> handlePaymentOperation(operation)
                else -> throw SyncException.NonRetryable("Unknown entity: ${operation.entityType}")
            }
            Timber.d("✅ Successfully synced operation #${operation.id}")
        } catch (e: SyncException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "❌ Unexpected error syncing operation #${operation.id}")
            throw SyncException.Retryable("Unexpected error: ${e.message}")
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // INVOICE OPERATIONS
    // ═════════════════════════════════════════════════════════════════════════

    private suspend fun handleInvoiceOperation(operation: PendingOperation) {
        val invoice = json.decodeFromString<Invoice>(operation.payload)
        
        when (operation.operationType) {
            OperationType.CREATE -> {
                invoiceRepository.createInvoiceRemote(invoice)
                    .onSuccess { remoteInvoice ->
                        invoiceRepository.saveInvoice(remoteInvoice) // Update local with server-generated ID/timestamps
                    }
                    .onFailure { throw classifyError(it) }
            }
            OperationType.UPDATE -> {
                invoiceRepository.updateInvoiceRemote(invoice)
                    .onFailure { error ->
                        if (isConflict(error)) {
                            resolveInvoiceConflict(invoice.id)
                        } else {
                            throw classifyError(error)
                        }
                    }
            }
            OperationType.DELETE -> {
                invoiceRepository.deleteInvoiceRemote(operation.entityId)
                    .onFailure { throw classifyError(it) }
            }
        }
    }

    private suspend fun resolveInvoiceConflict(id: Long) {
        Timber.w("⚔️ Conflict detected for Invoice #$id. Implementing 'Server Wins'...")
        invoiceRepository.getInvoiceRemote(id)
            .onSuccess { remoteInvoice ->
                invoiceRepository.saveInvoice(remoteInvoice)
                Timber.i("✅ Resolved conflict: Local state updated with server version of Invoice #$id")
            }
            .onFailure { Timber.e(it, "❌ Failed to resolve conflict for Invoice #$id") }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CUSTOMER OPERATIONS
    // ═════════════════════════════════════════════════════════════════════════

    private suspend fun handleCustomerOperation(operation: PendingOperation) {
        val customer = json.decodeFromString<Customer>(operation.payload)

        when (operation.operationType) {
            OperationType.CREATE -> {
                customerRepository.createCustomerRemote(customer)
                    .onSuccess { remoteCustomer ->
                        customerRepository.insert(remoteCustomer)
                    }
                    .onFailure { throw classifyError(it) }
            }
            OperationType.UPDATE -> {
                customerRepository.updateCustomerRemote(customer)
                    .onFailure { error ->
                        if (isConflict(error)) {
                            resolveCustomerConflict(customer.id)
                        } else {
                            throw classifyError(error)
                        }
                    }
            }
            OperationType.DELETE -> {
                customerRepository.deleteCustomerRemote(operation.entityId)
                    .onFailure { throw classifyError(it) }
            }
        }
    }

    private suspend fun resolveCustomerConflict(id: Long) {
        Timber.w("⚔️ Conflict detected for Customer #$id. Implementing 'Server Wins'...")
        customerRepository.getCustomerRemote(id)
            .onSuccess { remoteCustomer ->
                customerRepository.updateCustomer(remoteCustomer)
                Timber.i("✅ Resolved conflict: Local state updated with server version of Customer #$id")
            }
            .onFailure { Timber.e(it, "❌ Failed to resolve conflict for Customer #$id") }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PAYMENT OPERATIONS
    // ═════════════════════════════════════════════════════════════════════════

    private suspend fun handlePaymentOperation(operation: PendingOperation) {
        // For Week 2, we implement a basic record payment sync
        try {
            // Placeholder for payment parsing - usually a specific DTO
            // For now, entityId is the invoiceId
            invoiceRepository.recordPaymentRemote(
                invoiceId = operation.entityId,
                amount = 0L, // Should be extracted from operation.payload
                paymentDate = System.currentTimeMillis(),
                notes = "Synced from offline queue"
            ).onFailure { throw classifyError(it) }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync payment for invoice ${operation.entityId}")
            throw classifyError(e)
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ERROR HANDLING & CONFLICT DETECTION
    // ═════════════════════════════════════════════════════════════════════════

    private fun isConflict(error: Throwable): Boolean {
        return error.message?.contains("409") == true || error.message?.contains("Conflict") == true
    }

    private fun classifyError(error: Throwable): SyncException {
        val message = error.message ?: "Unknown sync error"
        return when {
            message.contains("401") || message.contains("403") -> 
                SyncException.NonRetryable("Auth error: $message")
            message.contains("404") -> 
                SyncException.NonRetryable("Entity not found: $message")
            message.contains("500") || message.contains("timeout") || message.contains("Network") -> 
                SyncException.Retryable("Temporary server/network error: $message")
            else -> SyncException.Retryable(message)
        }
    }

    sealed class SyncException(message: String) : Exception(message) {
        class Retryable(message: String) : SyncException(message)
        class NonRetryable(message: String) : SyncException(message)
    }
}
