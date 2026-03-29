package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.domain.payment.repository.PaymentRepository
import com.emul8r.bizap.domain.model.InvoiceStatus
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * Validates and records a payment for an invoice (Phase 3 implementation).
 *
 * Validation rules:
 *  - amount must be > 0 and ≤ outstanding balance.
 *  - paymentDate must be ≤ today's midnight.
 *  - paymentDate must be on or after the invoice date (same-day payments allowed).
 *
 * BUGFIX: Now properly normalizes invoiceDate to midnight before comparison.
 * Previously, comparing midnight paymentDate with full-timestamp invoiceDate caused false rejections.
 *
 * SPRINT 3 FIX: Now imports domain PaymentRepository interface instead of
 * data layer PaymentRepositoryV2, ensuring use case layer independence.
 */
class RecordPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    /**
     * @param invoiceId   Target invoice.
     * @param businessId  Owning business.
     * @param amount      Payment amount in cents (> 0, ≤ [trueOutstanding]).
     * @param trueOutstanding Remaining balance in cents (totalAmount - amountPaid).
     * @param paymentDate Unix timestamp (ms) of midnight of the selected date.
     * @param invoiceDate   Invoice creation date (ms) - normalized to midnight internally.
     * @param invoiceStatus Current status of the invoice; DRAFT invoices are blocked.
     * @param notes         Optional freeform notes (max 500 chars).
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
        if (invoiceStatus == InvoiceStatus.DRAFT) {
            return Result.failure(
                IllegalArgumentException(
                    "Cannot record payment on a draft invoice. Send the invoice first."
                )
            )
        }

        // Use midnight of today so comparison is consistent with the UI date picker
        val todayMidnight = todayMidnightMs()

        if (amount <= 0) {
            return Result.failure(
                IllegalArgumentException("Payment amount must be greater than zero.")
            )
        }
        
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

        // BUGFIX: Normalize invoiceDate to midnight before comparing
        // The problem: Invoices are created with System.currentTimeMillis() which includes time component (e.g., 2:30 PM)
        // But payment dates are ALWAYS normalized to midnight (00:00:00)
        // Without normalization: midnight < 2:30 PM = true, causing false rejection of same-day payments
        // With normalization: midnight == midnight = equal, allowing same-day payments
        val invoiceDateMidnight = Calendar.getInstance().apply {
            timeInMillis = invoiceDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (paymentDate < invoiceDateMidnight) {
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
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
