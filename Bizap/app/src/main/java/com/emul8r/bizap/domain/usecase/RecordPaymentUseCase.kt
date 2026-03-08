package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import timber.log.Timber
import javax.inject.Inject

/**
 * Validates and records a payment for an invoice (Phase 3 implementation).
 *
 * Validation rules:
 *  - amount must be > 0 and ≤ outstanding balance.
 *  - paymentDate (midnight of selected day) must be ≤ today's midnight.
 *  - paymentDate must be on or after the invoice date.
 *
 * Date comparisons use midnight values to guarantee consistency with the UI,
 * which always sets paymentDate to midnight of the selected calendar day.
 */
class RecordPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepositoryV2
) {
    /**
     * @param invoiceId   Target invoice.
     * @param businessId  Owning business.
     * @param amount      Payment amount in cents (> 0, ≤ [trueOutstanding]).
     * @param trueOutstanding Remaining balance in cents (totalAmount - amountPaid).
     * @param paymentDate Unix timestamp (ms) of midnight of the selected date.
     *                    Must be ≤ today's midnight and ≥ [invoiceDate].
     * @param invoiceDate Invoice creation date (ms) used as lower date boundary.
     * @param notes       Optional freeform notes (max 500 chars).
     */
    suspend operator fun invoke(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        trueOutstanding: Long,
        paymentDate: Long,
        invoiceDate: Long,
        notes: String? = null
    ): Result<Unit> {
        // Use midnight of today so comparison is consistent with the UI date picker
        val todayMidnight = todayMidnightMs()

        if (amount <= 0) {
            return Result.failure(
                IllegalArgumentException("Payment amount must be greater than zero.")
            )
        }
        
        // ALLOW payments on any invoice with a balance, including DRAFT.
        // We use trueOutstanding (totalAmount - amountPaid) passed from the caller.
        if (amount > trueOutstanding) {
            return Result.failure(
                IllegalArgumentException("Payment exceeds the outstanding balance.")
            )
        }

        if (paymentDate > todayMidnight) {
            return Result.failure(
                IllegalArgumentException("Payment date cannot be in the future.")
            )
        }
        if (paymentDate < invoiceDate) {
            return Result.failure(
                IllegalArgumentException("Payment date cannot be before the invoice date.")
            )
        }

        Timber.d("RecordPaymentUseCase: invoiceId=$invoiceId amount=$amount paymentDate=$paymentDate")

        return paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = amount,
            paymentDate = paymentDate,
            notes = notes
        )
    }

    private fun todayMidnightMs(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
