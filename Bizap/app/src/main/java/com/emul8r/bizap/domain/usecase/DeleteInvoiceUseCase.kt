package com.emul8r.bizap.domain.usecase

import android.content.Context
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import com.emul8r.bizap.domain.model.PendingOperation
import com.emul8r.bizap.domain.model.OperationType
import com.emul8r.bizap.utils.ConnectivityHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase to delete an invoice.
 * Supports Phase 2: Offline-First Reliability.
 *
 * SPRINT 3 FIX: Now imports domain OfflineQueueRepository interface instead of
 * data layer OfflineQueueService, ensuring layer independence.
 */
class DeleteInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val offlineQueueRepository: OfflineQueueRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(invoiceId: Long, businessId: Long): Result<Unit> {
        return try {
            // 🔌 Check network connectivity
            val isOnline = ConnectivityHelper.isNetworkAvailable(context)
            
            if (!isOnline) {
                // 📝 OFFLINE: Queue the deletion
                Timber.i("📶 Offline detected. Queueing deletion for sync.")
                offlineQueueRepository.enqueue(
                    PendingOperation(
                        operationType = OperationType.DELETE,
                        entityType = "Invoice",
                        entityId = invoiceId,
                        payload = """{"businessId":$businessId,"invoiceId":$invoiceId}"""
                    )
                )
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
