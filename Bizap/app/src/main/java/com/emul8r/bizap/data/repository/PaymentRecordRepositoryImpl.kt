package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.dao.PaymentRecordDaoV2
import com.emul8r.bizap.data.local.entities.PaymentRecordEntity
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.data.local.entities.PaymentMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository for payment record operations.
 *
 * Handles:
 * - Recording payments
 * - Updating invoice status based on payments
 * - Retrieving payment history
 * - Calculating payment statistics
 */
class PaymentRecordRepositoryImpl @Inject constructor(
    private val paymentRecordDao: PaymentRecordDaoV2,
    private val invoiceDao: InvoiceDaoV2
) : PaymentRecordRepository {

    override suspend fun recordPayment(
        invoiceId: Long,
        amount: Long,
        paymentMethod: String,
        notes: String?
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Get the invoice
            val invoice = invoiceDao.getById(invoiceId)
                ?: return@withContext Result.failure(Exception("Invoice not found"))

            // Create payment record
            val paymentRecord = PaymentRecordEntity(
                invoiceId = invoiceId,
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes,
                recordedAt = System.currentTimeMillis()
            )

            // Insert payment record
            val recordId = paymentRecordDao.insertPaymentRecord(paymentRecord)
            Timber.d("Payment recorded: $recordId for invoice $invoiceId, amount: $amount cents")

            // Calculate new total paid
            val newAmountPaid = paymentRecordDao.getTotalAmountPaid(invoiceId)
            Timber.d("New amount paid: $newAmountPaid / ${invoice.totalAmount}")

            // Determine new status
            val newStatus = when {
                newAmountPaid >= invoice.totalAmount -> InvoiceStatus.PAID.name
                newAmountPaid > 0 -> InvoiceStatus.PARTIALLY_PAID.name
                else -> invoice.status
            }

            // Update invoice
            val updatedInvoice = invoice.copy(
                amountPaid = newAmountPaid,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )

            invoiceDao.update(updatedInvoice)
            Timber.d("Invoice updated: status=$newStatus, amountPaid=$newAmountPaid")

            Result.success(recordId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to record payment for invoice: $invoiceId")
            Result.failure(e)
        }
    }

    override suspend fun getPaymentHistory(invoiceId: Long): Result<List<PaymentRecordEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val payments = paymentRecordDao.getPaymentsByInvoice(invoiceId)
                Result.success(payments)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get payment history for invoice: $invoiceId")
                Result.failure(e)
            }
        }

    override suspend fun deletePayment(paymentId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                paymentRecordDao.deletePaymentRecord(paymentId)
                Timber.d("Payment deleted: $paymentId")
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete payment: $paymentId")
                Result.failure(e)
            }
        }

    override suspend fun getRecentPayments(
        businessId: Long,
        limit: Int
    ): Result<List<PaymentRecordEntity>> = withContext(Dispatchers.IO) {
        try {
            val payments = paymentRecordDao.getRecentPayments(businessId, limit)
            Result.success(payments)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get recent payments for business: $businessId")
            Result.failure(e)
        }
    }
}

/**
 * Repository interface for payment records.
 */
interface PaymentRecordRepository {

    /**
     * Record a payment against an invoice.
     *
     * Automatically updates invoice status:
     * - PAID if fully paid
     * - PARTIALLY_PAID if partially paid
     * - Keeps existing status otherwise
     *
     * @param invoiceId Invoice to record payment for
     * @param amount Amount in cents
     * @param paymentMethod Payment method (CASH, CHECK, ACH_TRANSFER, etc.)
     * @param notes Optional notes
     * @return Result with payment record ID on success
     */
    suspend fun recordPayment(
        invoiceId: Long,
        amount: Long,
        paymentMethod: String,
        notes: String? = null
    ): Result<Long>

    /**
     * Get payment history for an invoice.
     *
     * @param invoiceId Invoice to get payments for
     * @return Result with list of payment records
     */
    suspend fun getPaymentHistory(invoiceId: Long): Result<List<PaymentRecordEntity>>

    /**
     * Delete a payment record.
     *
     * @param paymentId Payment record to delete
     * @return Result of deletion
     */
    suspend fun deletePayment(paymentId: Long): Result<Unit>

    /**
     * Get recent payments for a business.
     *
     * @param businessId Business to get payments for
     * @param limit Maximum number of payments to return (default 20)
     * @return Result with list of recent payments
     */
    suspend fun getRecentPayments(businessId: Long, limit: Int = 20): Result<List<PaymentRecordEntity>>
}



