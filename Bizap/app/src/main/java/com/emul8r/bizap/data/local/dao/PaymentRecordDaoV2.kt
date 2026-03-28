package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.PaymentRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for payment record operations.
 *
 * Handles:
 * - Inserting payment records
 * - Querying payment history
 * - Deleting payment records
 * - Observing payment changes
 */
@Dao
interface PaymentRecordDaoV2 {

    /**
     * Insert a new payment record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentRecord(paymentRecord: PaymentRecordEntity): Long

    /**
     * Get payment record by ID.
     */
    @Query("SELECT * FROM payment_records WHERE id = :id AND isActive = 1")
    suspend fun getPaymentRecordById(id: Long): PaymentRecordEntity?

    /**
     * Get all payment records for an invoice.
     */
    @Query("""
        SELECT * FROM payment_records 
        WHERE invoiceId = :invoiceId AND isActive = 1 
        ORDER BY recordedAt DESC
    """)
    suspend fun getPaymentsByInvoice(invoiceId: Long): List<PaymentRecordEntity>

    /**
     * Observe payment records for an invoice.
     */
    @Query("""
        SELECT * FROM payment_records 
        WHERE invoiceId = :invoiceId AND isActive = 1 
        ORDER BY recordedAt DESC
    """)
    fun observePaymentsByInvoice(invoiceId: Long): Flow<List<PaymentRecordEntity>>

    /**
     * Get total amount paid for an invoice.
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM payment_records 
        WHERE invoiceId = :invoiceId AND isActive = 1
    """)
    suspend fun getTotalAmountPaid(invoiceId: Long): Long

    /**
     * Get recent payments for a business.
     */
    @Query("""
        SELECT pr.* FROM payment_records pr
        INNER JOIN invoices i ON pr.invoiceId = i.id
        WHERE i.businessProfileId = :businessId AND pr.isActive = 1
        ORDER BY pr.recordedAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentPayments(businessId: Long, limit: Int = 20): List<PaymentRecordEntity>

    /**
     * Soft delete a payment record.
     */
    @Query("UPDATE payment_records SET isActive = 0 WHERE id = :id")
    suspend fun deletePaymentRecord(id: Long)

    /**
     * Hard delete a payment record (if needed).
     */
    @Query("DELETE FROM payment_records WHERE id = :id")
    suspend fun hardDeletePaymentRecord(id: Long)

    /**
     * Get payment statistics for a business in a date range.
     */
    @Query("""
        SELECT COUNT(*) as paymentCount, SUM(amount) as totalAmount
        FROM payment_records pr
        INNER JOIN invoices i ON pr.invoiceId = i.id
        WHERE i.businessProfileId = :businessId 
        AND pr.isActive = 1
        AND pr.recordedAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getPaymentStatistics(
        businessId: Long,
        startDate: Long,
        endDate: Long
    ): PaymentStatisticsResult?
}

/**
 * Result class for payment statistics query.
 */
data class PaymentStatisticsResult(
    val paymentCount: Int,
    val totalAmount: Long
)

