package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.InvoicePaymentEntity
import com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot
import com.emul8r.bizap.data.local.entities.DailyPaymentSnapshot
import com.emul8r.bizap.data.local.entities.CollectionMetrics
import kotlinx.coroutines.flow.Flow

/**
 * Data access for invoice payment analytics.
 */
@Dao
interface InvoicePaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: InvoicePaymentEntity)

    @Query("SELECT * FROM invoice_payments WHERE invoiceId = :invoiceId ORDER BY paymentDate DESC")
    suspend fun getPaymentsForInvoice(invoiceId: Long): List<InvoicePaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<InvoicePaymentSnapshot>)

    @Query("SELECT * FROM invoice_payment_snapshots WHERE invoiceId = :invoiceId LIMIT 1")
    suspend fun getSnapshotByInvoiceId(invoiceId: Long): InvoicePaymentSnapshot?

    @Update
    suspend fun updateSnapshot(snapshot: InvoicePaymentSnapshot)

    /**
     * Observe all payment snapshots for a business, EXCLUDING DRAFT invoices.
     * DRAFT invoices are works-in-progress and should not be included in financial metrics.
     * Filters by paymentStatus field which reflects the invoice status.
     */
    @Query("""
        SELECT * FROM invoice_payment_snapshots
        WHERE businessProfileId = :businessId
          AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
        ORDER BY dueDate ASC
    """)
    fun observeAllSnapshots(businessId: Long): Flow<List<InvoicePaymentSnapshot>>

    /**
     * Observe risky payment snapshots, EXCLUDING DRAFT invoices.
     * Risk analysis focuses on official invoices (PAID, UNPAID, OVERDUE).
     * Filters by paymentStatus which excludes DRAFT invoices.
     */
    @Query("""
        SELECT * FROM invoice_payment_snapshots
        WHERE businessProfileId = :businessId
          AND isAtRisk = 1
          AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
        ORDER BY riskScore DESC LIMIT :limit
    """)
    fun observeRiskInvoices(businessId: Long, limit: Int = 10): Flow<List<InvoicePaymentSnapshot>>

    /**
     * Get all payment snapshots for a business (suspend version), EXCLUDING DRAFT invoices.
     * DRAFT invoices are works-in-progress and should not be included in financial metrics.
     */
    @Query("""
        SELECT * FROM invoice_payment_snapshots
        WHERE businessProfileId = :businessId
          AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
        ORDER BY dueDate ASC
    """)
    suspend fun getAllSnapshots(businessId: Long): List<InvoicePaymentSnapshot>

    @Query("SELECT COUNT(*) FROM invoice_payment_snapshots WHERE businessProfileId = :businessId AND paymentStatus = :status")
    suspend fun countByStatus(businessId: Long, status: String): Int

    @Query("""
        SELECT 
            SUM(CASE WHEN ageingBucket = 'CURRENT' THEN outstandingAmount ELSE 0 END) / 100.0 as current,
            SUM(CASE WHEN ageingBucket = 'PAST_30' THEN outstandingAmount ELSE 0 END) / 100.0 as past30,
            SUM(CASE WHEN ageingBucket = 'PAST_60' THEN outstandingAmount ELSE 0 END) / 100.0 as past60,
            SUM(CASE WHEN ageingBucket = 'PAST_90' THEN outstandingAmount ELSE 0 END) / 100.0 as past90
        FROM invoice_payment_snapshots
        WHERE businessProfileId = :businessId
    """)
    suspend fun getOutstandingByAging(businessId: Long): OutstandingByAgingRow

    @Query("SELECT * FROM invoice_payment_snapshots WHERE businessProfileId = :businessId AND isAtRisk = 1 ORDER BY riskScore DESC LIMIT :limit")
    suspend fun getRiskInvoices(businessId: Long, limit: Int = 10): List<InvoicePaymentSnapshot>

    @Query("""
        SELECT 
            COUNT(*) as totalInvoices,
            SUM(totalAmount) / 100.0 as totalAmount,
            SUM(paidAmount) / 100.0 as paidAmount,
            SUM(outstandingAmount) / 100.0 as outstanding
        FROM invoice_payment_snapshots
        WHERE businessProfileId = :businessId
    """)
    suspend fun getPaymentMetrics(businessId: Long): PaymentMetricsRow

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySnapshot(snapshot: DailyPaymentSnapshot)

    @Query("SELECT * FROM daily_payment_snapshots WHERE businessProfileId = :businessId ORDER BY snapshotDate DESC LIMIT :days")
    suspend fun getDailySnapshots(businessId: Long, days: Int = 30): List<DailyPaymentSnapshot>

    /**
     * Observe payment history for a specific invoice.
     *
     * Returns snapshots ordered by date DESC (newest first).
     * Each snapshot represents the payment state at a point in time.
     *
     * Used by [PaymentHistoryViewModel] to display payment timeline.
     *
     * @param invoiceId Invoice to get history for
     * @return Flow of payment snapshots, newest first
     */
    @Query("""
        SELECT * FROM invoice_payment_snapshots
        WHERE invoiceId = :invoiceId
        ORDER BY lastUpdatedMs DESC
    """)
    fun observePaymentHistory(invoiceId: Long): Flow<List<InvoicePaymentSnapshot>>

    // ==================== HEALTH CHECK QUERIES ====================

    /**
     * Count total payment snapshots.
     * Used to verify snapshot coverage.
     */
    @Query("SELECT COUNT(*) FROM invoice_payment_snapshots")
    suspend fun countSnapshots(): Int

    @Query("DELETE FROM invoice_payment_snapshots WHERE invoiceId = :invoiceId")
    suspend fun deleteSnapshotByInvoiceId(invoiceId: Long)

    @Query("DELETE FROM invoice_payment_snapshots WHERE businessProfileId = :businessId")
    suspend fun deleteAllSnapshots(businessId: Long)

    /**
     * Find invoice IDs missing payment snapshots.
     * Used for health reporting and recovery.
     */
    @Query("""
        SELECT DISTINCT i.id FROM invoices i
        LEFT JOIN invoice_payment_snapshots ips ON i.id = ips.invoiceId
        WHERE ips.invoiceId IS NULL
    """)
    suspend fun getMissingSnapshots(): List<Long>

    /**
     * Find orphaned payment snapshots (snapshots without invoices).
     * Used for cleanup operations.
     */
    @Query("""
        SELECT DISTINCT ips.invoiceId FROM invoice_payment_snapshots ips
        LEFT JOIN invoices i ON ips.invoiceId = i.id
        WHERE i.id IS NULL
    """)
    suspend fun getOrphanedSnapshots(): List<Long>

    data class OutstandingByAgingRow(
        val current: Double,
        val past30: Double,
        val past60: Double,
        val past90: Double
    )

    data class PaymentMetricsRow(
        val totalInvoices: Int,
        val totalAmount: Double,
        val paidAmount: Double,
        val outstanding: Double
    )
}
