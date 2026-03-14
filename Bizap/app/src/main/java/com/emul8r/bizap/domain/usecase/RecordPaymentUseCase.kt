package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import com.emul8r.bizap.domain.model.InvoiceStatus
import timber.log.Timber
import javax.inject.Inject

/**
 * Validates and records a payment for an invoice (Phase 3 implementation).
 *
 * Validation rules:
 *  - invoice must be in SENT, PARTIALLY_PAID, PAID, or OVERDUE status (not DRAFT).
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
     * @param invoiceStatus Current status of the invoice (must not be DRAFT).
     * @param notes       Optional freeform notes (max 500 chars).
     */
    suspend operator fun invoke(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        trueOutstanding: Long,
        paymentDate: Long,
        invoiceDate: Long,
        invoiceStatus: InvoiceStatus,
        notes: String? = null
    ): Result<Unit> {
        // Use midnight of today so comparison is consistent with the UI date picker
        val todayMidnight = todayMidnightMs()

        // Validate invoice status - payments can only be recorded on sent invoices
        if (invoiceStatus == InvoiceStatus.DRAFT) {
            return Result.failure(
                IllegalArgumentException("Cannot record payment on a draft invoice. Send the invoice first.")
            )
        }

        if (amount <= 0) {
            return Result.failure(
                IllegalArgumentException("Payment amount must be greater than zero.")
            )
        }
        
        // Validate payment does not exceed outstanding balance
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

        Timber.d("RecordPaymentUseCase: invoiceId=$invoiceId status=$invoiceStatus amount=$amount paymentDate=$paymentDate")

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
