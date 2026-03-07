package com.emul8r.bizap.domain.usecase

import android.content.Context
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.offline.OfflineQueueService
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.utils.ConnectivityHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase to update an existing invoice.
 * Supports Phase 2: Offline-First Reliability.
 */
class UpdateInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val snapshotSyncHelper: SnapshotSyncHelper,
    private val offlineQueueService: OfflineQueueService,
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
                val queuedId = offlineQueueService.queueUpdateInvoice(invoice)
                Timber.d("✅ Invoice update queued with operation ID: $queuedId")
                return Result.success(queuedId)
            }
            
            // 🌐 ONLINE: Update directly
            val result = repository.saveInvoice(invoice) // saveInvoice handles updates in repository
            
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
                    Timber.d("✅ Snapshots synced for updated invoice $invoiceId")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Failed to sync snapshots, but invoice updated successfully")
                }
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update invoice")
            Result.failure(e)
        }
    }
}
