package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import timber.log.Timber
import javax.inject.Inject

/**
 * Validates and records a payment for an invoice (Phase 3 implementation).
 *
 * Validation rules:
 *  - amount must be > 0 and ≤ outstanding balance.
 *  - paymentDate must not be in the future.
 *  - paymentDate must be on or after the invoice date.
 */
class RecordPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepositoryV2
) {
    /**
     * @param invoiceId   Target invoice.
     * @param businessId  Owning business.
     * @param amount      Payment amount in cents (> 0, ≤ [outstanding]).
     * @param outstanding Remaining balance in cents used for amount validation.
     * @param paymentDate Unix timestamp (ms) — must not be future and must be ≥ [invoiceDate].
     * @param invoiceDate Invoice creation date (ms) used as lower date boundary.
     * @param notes       Optional freeform notes (max 500 chars).
     */
    suspend operator fun invoke(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        outstanding: Long,
        paymentDate: Long,
        invoiceDate: Long,
        notes: String? = null
    ): Result<Unit> {
        val now = System.currentTimeMillis()

        if (amount <= 0) {
            return Result.failure(
                IllegalArgumentException("Payment amount must be greater than zero.")
            )
        }
        if (amount > outstanding) {
            return Result.failure(
                IllegalArgumentException("Payment exceeds the outstanding balance.")
            )
        }
        if (paymentDate > now) {
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
}
