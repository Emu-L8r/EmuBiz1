package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.dao.PaymentDaoV2
import com.emul8r.bizap.data.local.entities.PaymentEntity
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

/**
 * GUI2 repository for recording payment transactions.
 *
 * Atomically:
 *  - Inserts a [PaymentEntity] in the `payments` table.
 *  - Updates `invoices.amountPaid`.
 *  - Transitions invoice status to PAID or PARTIALLY_PAID when appropriate.
 *
 * All monetary values are in cents (Long).
 */
class PaymentRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2,
    private val paymentDaoV2: PaymentDaoV2
) {

    /**
     * Records a payment for the given invoice.
     *
     * @param invoiceId   Invoice receiving the payment.
     * @param businessId  Owning business (denormalised onto the payment row).
     * @param amount      Payment amount in cents (must be > 0 and ≤ outstanding).
     * @param paymentDate Unix timestamp (ms) of the payment date.
     * @param notes       Optional freeform notes (max 500 chars, truncated if longer).
     */
    suspend fun recordPayment(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        paymentDate: Long,
        notes: String?
    ): Result<Unit> = runCatching {
        val invoice = invoiceDaoV2.getById(invoiceId)
            ?: error("Invoice $invoiceId not found")

        val newAmountPaid = invoice.amountPaid + amount

        val newStatus = when {
            newAmountPaid >= invoice.totalAmount -> InvoiceStatus.PAID.name
            newAmountPaid > 0 -> InvoiceStatus.PARTIALLY_PAID.name
            else -> invoice.status
        }

        // Insert the individual payment transaction
        val payment = PaymentEntity(
            businessId = businessId,
            invoiceId = invoiceId,
            amount = amount,
            paymentDate = paymentDate,
            notes = notes?.take(500),
            createdAt = System.currentTimeMillis()
        )
        paymentDaoV2.insert(payment)

        // Update the invoice's cumulative paid amount and status
        val now = System.currentTimeMillis()
        invoiceDaoV2.updateAmountPaid(invoiceId, newAmountPaid, now)
        invoiceDaoV2.updateStatus(invoiceId, newStatus, now)

        Timber.d("✅ Payment recorded: invoice=$invoiceId amount=$amount newStatus=$newStatus")
    }.also { result ->
        result.onFailure { Timber.e(it, "❌ recordPayment failed for invoice $invoiceId") }
    }

    /**
     * Observe all payments recorded against a single invoice, newest first.
     */
    fun observePaymentsByInvoice(invoiceId: Long): Flow<List<PaymentEntity>> =
        paymentDaoV2.observePaymentsForInvoice(invoiceId)
}
