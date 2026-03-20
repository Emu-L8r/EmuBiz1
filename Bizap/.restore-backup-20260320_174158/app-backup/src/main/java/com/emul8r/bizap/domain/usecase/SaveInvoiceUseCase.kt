package com.emul8r.bizap.domain.usecase

import android.content.Context
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.offline.OfflineQueueService
import com.emul8r.bizap.utils.ConnectivityHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase to save an invoice.
 * Supports Phase 2: Offline-First Reliability.
 */
class SaveInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val snapshotSyncHelper: SnapshotSyncHelper,
    private val offlineQueueService: OfflineQueueService,
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
                val queuedId = offlineQueueService.queueCreateInvoice(invoice)
                
                // Return the operation ID as a temporary success result
                Timber.d("✅ Invoice queued with operation ID: $queuedId")
                return Result.success(queuedId)
            }
            
            // 🌐 ONLINE: Save directly to database
            val result = repository.saveInvoice(invoice)

            if (result.isSuccess) {
                val invoiceId = result.getOrNull() ?: return result
                try {
                    val invoiceEntity = InvoiceEntity(
                        id = invoiceId,
                        businessProfileId = invoice.businessProfileId,
                        customerId = invoice.customerId,
                        customerName = invoice.customerName,
                        totalAmount = invoice.totalAmount,
                        amountPaid = invoice.amountPaid,
                        status = invoice.status.toString(),
                        dueDate = invoice.dueDate,
                        date = invoice.date,
                        updatedAt = invoice.updatedAt,
                        invoiceYear = invoice.invoiceYear,
                        invoiceSequence = invoice.invoiceSequence,
                        isQuote = invoice.isQuote
                    )
                    snapshotSyncHelper.syncAllSnapshots(invoiceEntity, invoice.businessProfileId)
                    Timber.d("✅ Snapshots synced for invoice $invoiceId")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Failed to sync snapshots, but invoice saved successfully")
                }
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to save invoice")
            Result.failure(e)
        }
    }
}
