package com.emul8r.bizap.domain.usecase

import android.content.Context
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.data.local.offline.OfflineQueueService
import com.emul8r.bizap.utils.ConnectivityHelper
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase to record a payment for an invoice.
 * Supports Phase 2: Offline-First Reliability.
 */
class RecordPaymentUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val offlineQueueService: OfflineQueueService,
    private val context: Context
) {
    suspend operator fun invoke(
        invoiceId: Long,
        amountPaid: Long,
        businessId: Long
    ): Result<Unit> {
        return try {
            // Validation
            if (amountPaid <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
            }

            // 🔌 Check network connectivity
            val isOnline = ConnectivityHelper.isNetworkAvailable(context)
            
            if (!isOnline) {
                // 📝 OFFLINE: Queue the payment
                Timber.i("📶 Offline detected. Queueing payment for sync.")
                offlineQueueService.queueRecordPayment(invoiceId, amountPaid, businessId)
                Timber.d("✅ Payment queued for invoice $invoiceId")
                return Result.success(Unit)
            }
            
            // 🌐 ONLINE: Process payment directly
            repository.updateAmountPaid(invoiceId, amountPaid)
            Timber.d("✅ Payment recorded for invoice $invoiceId: $amountPaid cents")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to record payment")
            Result.failure(e)
        }
    }
}
