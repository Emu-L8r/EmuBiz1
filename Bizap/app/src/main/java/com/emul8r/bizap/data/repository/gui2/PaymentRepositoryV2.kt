package com.emul8r.bizap.data.repository.gui2

import androidx.room.withTransaction
import com.emul8r.bizap.data.local.AppDatabase
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
 * All three database writes (insert payment, update amountPaid, update status)
 * execute inside a single Room transaction to guarantee atomicity.
 *
 * All monetary values are in cents (Long).
 */
class PaymentRepositoryV2 @Inject constructor(
    private val database: AppDatabase,
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
        database.withTransaction {
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
        }
    }.also { result ->
        result.onFailure { Timber.e(it, "❌ recordPayment failed for invoice $invoiceId") }
    }

    /**
     * Observe all payments recorded against a single invoice, newest first.
     */
    fun observePaymentsByInvoice(invoiceId: Long): Flow<List<PaymentEntity>> =
        paymentDaoV2.observePaymentsForInvoice(invoiceId)

    /**
     * Atomically marks an invoice as PAID, setting amountPaid to totalAmount and
     * recording a payment entry for any outstanding balance.
     *
     * If the invoice is already fully paid, only the status is updated.
     *
     * @param invoiceId  Invoice to mark as paid.
     * @param businessId Owning business (denormalised onto the payment row).
     */
    suspend fun markInvoiceAsPaid(invoiceId: Long, businessId: Long): Result<Unit> = runCatching {
        database.withTransaction {
            val invoice = invoiceDaoV2.getById(invoiceId)
                ?: error("Invoice $invoiceId not found")

            val outstanding = invoice.totalAmount - invoice.amountPaid
            val now = System.currentTimeMillis()

            if (outstanding > 0) {
                val payment = PaymentEntity(
                    businessId = businessId,
                    invoiceId = invoiceId,
                    amount = outstanding,
                    paymentDate = now,
                    notes = "Auto-recorded when invoice marked as PAID"
                )
                paymentDaoV2.insert(payment)
                invoiceDaoV2.updateAmountPaid(invoiceId, invoice.totalAmount, now)
            }

            invoiceDaoV2.updateStatus(invoiceId, InvoiceStatus.PAID.name, now)

            Timber.d("✅ Invoice $invoiceId marked as PAID, auto-recorded outstanding=$outstanding cents")
        }
    }.also { result ->
        result.onFailure { Timber.e(it, "❌ markInvoiceAsPaid failed for invoice $invoiceId") }
    }
}

