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
 * UseCase to update an existing invoice.
 * Supports Phase 2: Offline-First Reliability.
 *
 * SPRINT 3: Simplified architecture - only depends on domain repository interfaces.
 * Snapshot sync is handled internally by InvoiceRepository.
 */
class UpdateInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val offlineQueueRepository: OfflineQueueRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(invoice: Invoice): Result<Long> {
        return try {
            // Validation
            if (invoice.items.isEmpty()) {
                return Result.failure(IllegalArgumentException("Invoice must have at least one line item"))
            }

            // 🔌 Check network connectivity
            val isOnline = ConnectivityHelper.isNetworkAvailable(context)
            
            if (!isOnline) {
                // 📝 OFFLINE: Queue the update
                Timber.i("📶 Offline detected. Queueing invoice update for sync.")
                offlineQueueRepository.enqueue(
                    PendingOperation(
                        operationType = OperationType.UPDATE,
                        entityType = "Invoice",
                        entityId = invoice.id,
                        payload = """{"businessId":${invoice.businessProfileId},"invoiceId":${invoice.id}}"""
                    )
                )
                Timber.d("✅ Invoice update queued for sync")
                return Result.success(-1L)
            }
            
            // 🌐 ONLINE: Update directly
            // InvoiceRepository handles snapshot sync internally
            repository.saveInvoice(invoice)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update invoice")
            Result.failure(e)
        }
    }
}
