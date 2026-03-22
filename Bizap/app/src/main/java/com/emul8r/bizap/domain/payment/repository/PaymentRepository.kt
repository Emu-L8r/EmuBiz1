package com.emul8r.bizap.domain.payment.repository

/**
 * Domain-level repository interface for payment operations.
 *
 * SPRINT 3: Extracted from data layer implementation (PaymentRepositoryV2)
 * to ensure domain use cases import domain interfaces, not data layer implementations.
 */
interface PaymentRepository {
    /**
     * Records a payment for the given invoice.
     *
     * @param invoiceId Invoice receiving the payment.
     * @param businessId Owning business (denormalized on the payment row).
     * @param amount Payment amount in cents (must be > 0 and ≤ outstanding).
     * @param paymentDate Unix timestamp (ms) of the payment date.
     * @param notes Optional freeform notes (max 500 chars, truncated if longer).
     */
    suspend fun recordPayment(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        paymentDate: Long,
        notes: String?
    ): Result<Unit>
}

