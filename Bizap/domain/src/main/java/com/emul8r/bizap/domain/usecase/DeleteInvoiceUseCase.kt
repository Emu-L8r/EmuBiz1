package com.emul8r.bizap.domain.usecase

import android.content.Context
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.data.local.offline.OfflineQueueService
import com.emul8r.bizap.utils.ConnectivityHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase to delete an invoice.
 * Supports Phase 2: Offline-First Reliability.
 */
class DeleteInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val offlineQueueService: OfflineQueueService,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(invoiceId: Long, businessId: Long): Result<Unit> {
        return try {
            // 🔌 Check network connectivity
            val isOnline = ConnectivityHelper.isNetworkAvailable(context)
            
            if (!isOnline) {
                // 📝 OFFLINE: Queue the deletion
                Timber.i("📶 Offline detected. Queueing deletion for sync.")
                offlineQueueService.queueDeleteInvoice(invoiceId, businessId)
                Timber.d("✅ Deletion queued for invoice $invoiceId")
                return Result.success(Unit)
            }
            
            // 🌐 ONLINE: Delete directly
            repository.deleteInvoice(invoiceId)
            Timber.d("✅ Invoice deleted: $invoiceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to delete invoice")
            Result.failure(e)
        }
    }
}
