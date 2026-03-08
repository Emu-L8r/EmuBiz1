package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.InvoiceItemEntity
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.data.local.entities.InvoiceWithInvoiceItems
import kotlinx.coroutines.flow.Flow

/**
 * GUI2 data access layer: queries directly against the invoices table.
 * Option C — no snapshot dependencies, data is always fresh and consistent.
 *
 * All monetary values are in cents (Long).
 * businessId parameter is mandatory on every query (context-aware design).
 */
@Dao
interface InvoiceDaoV2 {

    // ==================== CRUD ====================

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(invoice: InvoiceEntity): Long

    @Query("SELECT * FROM invoices WHERE id = :invoiceId LIMIT 1")
    suspend fun getById(invoiceId: Long): InvoiceEntity?

    @Query("UPDATE invoices SET amountPaid = :amountPaid, updatedAt = :updatedAt WHERE id = :invoiceId")
    suspend fun updateAmountPaid(
        invoiceId: Long,
        amountPaid: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE invoices SET status = :status, updatedAt = :updatedAt WHERE id = :invoiceId")
    suspend fun updateStatus(
        invoiceId: Long,
        status: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<InvoiceItemEntity>): List<Long>

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET isActive = 0, updatedAt = :updatedAt WHERE id = :invoiceId")
    suspend fun delete(invoiceId: Long, updatedAt: Long = System.currentTimeMillis())

    @Transaction
    @Query("SELECT * FROM invoices WHERE businessProfileId = :businessId AND isActive = 1 ORDER BY date DESC")
    fun observeAllInvoices(businessId: Long): Flow<List<InvoiceWithInvoiceItems>>

    // ==================== REVENUE QUERIES ====================

    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'PAID' OR status = 'PARTIALLY_PAID')
          AND isActive = 1
          AND DATE(date/1000, 'unixepoch') >= DATE('now', 'start of month')
    """)
    fun observeMTDRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'PAID' OR status = 'PARTIALLY_PAID')
          AND isActive = 1
          AND strftime('%Y', date/1000, 'unixepoch') = strftime('%Y', 'now')
    """)
    fun observeYTDRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'PAID' OR status = 'PARTIALLY_PAID')
          AND isActive = 1
          AND DATE(date/1000, 'unixepoch') >= DATE('now', '-7 days')
    """)
    fun observeWeeklyRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
    """)
    fun observeTotalPaidRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT
            DATE(date/1000, 'unixepoch')                                           AS dateString,
            COALESCE(SUM(CASE WHEN status != 'DRAFT' THEN totalAmount ELSE 0 END), 0) AS revenue,
            COUNT(*)                                                               AS invoiceCount,
            COUNT(CASE WHEN status = 'PAID' THEN 1 END)                           AS paidCount
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND DATE(date/1000, 'unixepoch') >= DATE('now', '-30 days')
        GROUP BY dateString
        ORDER BY dateString DESC
    """)
    fun observeLast30DaysRevenueTrend(businessId: Long): Flow<List<DailyRevenueTrendV2>>

    // ==================== OUTSTANDING / COLLECTED ====================

    /**
     * Total outstanding amount (Billed - Paid) across all issued invoices.
     * Accrual Basis logic: Any non-draft invoice with a balance remaining is outstanding.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status != 'DRAFT'
          AND isActive = 1
    """)
    fun observeOutstandingAmount(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
    """)
    fun observeCollectedAmount(businessId: Long): Flow<Long>

    // ==================== STATUS BREAKDOWN ====================

    @Query("""
        SELECT status, COUNT(*) AS count
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
        GROUP BY status
    """)
    fun observeInvoiceCountByStatus(businessId: Long): Flow<List<InvoiceStatusCountV2>>

    // ==================== RISK QUERIES ====================

    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'OVERDUE'
          AND isActive = 1
    """)
    fun observeOverdueCount(businessId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'OVERDUE'
          AND dueDate > 0
          AND isActive = 1
          AND DATE(dueDate/1000, 'unixepoch') <= DATE('now', '-60 days')
    """)
    fun observeHighRiskInvoiceCount(businessId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'OVERDUE'
          AND dueDate > 0
          AND isActive = 1
          AND DATE(dueDate/1000, 'unixepoch') <= DATE('now', '-30 days')
          AND DATE(dueDate/1000, 'unixepoch') > DATE('now', '-60 days')
    """)
    fun observeAtRiskInvoiceCount(businessId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND (
              status = 'PAID'
              OR (status != 'DRAFT' AND (dueDate = 0 OR DATE(dueDate/1000, 'unixepoch') >= DATE('now')))
          )
    """)
    fun observeHealthyInvoiceCount(businessId: Long): Flow<Int>

    // ==================== PAYMENT METRICS ====================

    @Query("""
        SELECT COALESCE(
            AVG(
                CASE
                    WHEN dueDate > 0
                    THEN CAST((dueDate - date) AS REAL) / 86400000.0
                    ELSE 30.0
                END
            ), 0.0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'PAID'
          AND isActive = 1
    """)
    fun observeAverageDaysToPayment(businessId: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'SENT' OR status = 'PARTIALLY_PAID' OR status = 'OVERDUE')
          AND isActive = 1
    """)
    fun observeActualOutstanding(businessId: Long): Flow<Long>
}
