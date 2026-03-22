package com.emul8r.bizap.domain.usecase

import android.content.Context
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import com.emul8r.bizap.domain.model.PendingOperation
import com.emul8r.bizap.domain.model.OperationType
import com.emul8r.bizap.utils.ConnectivityHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase to save an invoice.
 * Supports Phase 2: Offline-First Reliability.
 *
 * SPRINT 3: Simplified architecture - only depends on domain repository interfaces.
 * Snapshot sync is handled internally by InvoiceRepository (proper separation of concerns).
 */
class SaveInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val offlineQueueRepository: OfflineQueueRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(invoice: Invoice): Result<Long> {
        // Validation
        if (invoice.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invoice must have at least one line item"))
        }

        if (invoice.customerName.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name cannot be empty"))
        }

        return try {
            // 🔌 Check network connectivity
            val isOnline = ConnectivityHelper.isNetworkAvailable(context)
            
            if (!isOnline) {
                // 📝 OFFLINE: Queue the operation
                Timber.i("📶 Offline detected. Queueing invoice for sync.")
                offlineQueueRepository.enqueue(
                    PendingOperation(
                        operationType = OperationType.CREATE,
                        entityType = "Invoice",
                        entityId = invoice.id,
                        payload = """{"businessId":${invoice.businessProfileId},"invoiceId":${invoice.id}}"""
                    )
                )

                Timber.d("✅ Invoice queued for sync")
                return Result.success(-1L)
            }
            
            // 🌐 ONLINE: Save directly to database
            // InvoiceRepository handles snapshot sync internally (data layer concern)
            repository.saveInvoice(invoice)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to save invoice")
            Result.failure(e)
        }
    }
}
