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

    /**
     * Inserts a new invoice and returns its generated row id.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(invoice: InvoiceEntity): Long

    /**
     * Inserts a batch of line items and returns their generated row ids.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<InvoiceItemEntity>): List<Long>

    @Update
    suspend fun update(invoice: InvoiceEntity)

    /**
     * Soft-deletes an invoice by setting [isActive] to false.
     * The record is retained for audit and analytics purposes.
     */
    @Query("UPDATE invoices SET isActive = 0, updatedAt = :updatedAt WHERE id = :invoiceId")
    suspend fun delete(invoiceId: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * Observe all active invoices for a business (with their line items), newest first.
     */
    @Transaction
    @Query("SELECT * FROM invoices WHERE businessProfileId = :businessId AND isActive = 1 ORDER BY date DESC")
    fun observeAllInvoices(businessId: Long): Flow<List<InvoiceWithInvoiceItems>>

    // ==================== REVENUE QUERIES ====================

    /**
     * Month-to-date paid revenue in cents.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'PAID'
          AND DATE(date/1000, 'unixepoch') >= DATE('now', 'start of month')
    """)
    fun observeMTDRevenue(businessId: Long): Flow<Long>

    /**
     * Year-to-date paid revenue in cents.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'PAID'
          AND strftime('%Y', date/1000, 'unixepoch') = strftime('%Y', 'now')
    """)
    fun observeYTDRevenue(businessId: Long): Flow<Long>

    /**
     * Last 7-day paid revenue in cents.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'PAID'
          AND DATE(date/1000, 'unixepoch') >= DATE('now', '-7 days')
    """)
    fun observeWeeklyRevenue(businessId: Long): Flow<Long>

    /**
     * All-time total paid revenue in cents.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'PAID'
    """)
    fun observeTotalPaidRevenue(businessId: Long): Flow<Long>

    /**
     * Daily revenue trend for the last 30 days.
     * Returns one row per day, newest first.
     */
    @Query("""
        SELECT
            DATE(date/1000, 'unixepoch')                                           AS dateString,
            COALESCE(SUM(CASE WHEN status = 'PAID' THEN totalAmount ELSE 0 END), 0) AS revenue,
            COUNT(*)                                                               AS invoiceCount,
            COUNT(CASE WHEN status = 'PAID' THEN 1 END)                           AS paidCount
        FROM invoices
        WHERE businessProfileId = :businessId
          AND DATE(date/1000, 'unixepoch') >= DATE('now', '-30 days')
        GROUP BY dateString
        ORDER BY dateString DESC
    """)
    fun observeLast30DaysRevenueTrend(businessId: Long): Flow<List<DailyRevenueTrendV2>>

    // ==================== OUTSTANDING / COLLECTED ====================

    /**
     * Total outstanding amount (unpaid invoices, excluding DRAFT) in cents.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')
    """)
    fun observeOutstandingAmount(businessId: Long): Flow<Long>

    /**
     * Total amount collected (sum of amountPaid across all invoices) in cents.
     */
    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
    """)
    fun observeCollectedAmount(businessId: Long): Flow<Long>

    // ==================== STATUS BREAKDOWN ====================

    /**
     * Invoice count grouped by status.
     */
    @Query("""
        SELECT status, COUNT(*) AS count
        FROM invoices
        WHERE businessProfileId = :businessId
        GROUP BY status
    """)
    fun observeInvoiceCountByStatus(businessId: Long): Flow<List<InvoiceStatusCountV2>>

    // ==================== RISK QUERIES ====================

    /**
     * Count of overdue invoices (status = OVERDUE).
     */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'OVERDUE'
    """)
    fun observeOverdueCount(businessId: Long): Flow<Int>

    /**
     * High-risk invoice count: overdue by 60+ days (dueDate is 60+ days in the past).
     */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'OVERDUE'
          AND dueDate > 0
          AND DATE(dueDate/1000, 'unixepoch') <= DATE('now', '-60 days')
    """)
    fun observeHighRiskInvoiceCount(businessId: Long): Flow<Int>

    /**
     * At-risk invoice count: overdue by 30–59 days (dueDate is 30–59 days in the past).
     */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'OVERDUE'
          AND dueDate > 0
          AND DATE(dueDate/1000, 'unixepoch') <= DATE('now', '-30 days')
          AND DATE(dueDate/1000, 'unixepoch') > DATE('now', '-60 days')
    """)
    fun observeAtRiskInvoiceCount(businessId: Long): Flow<Int>

    /**
     * Healthy invoice count: PAID or SENT and not yet due.
     */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (
              status = 'PAID'
              OR (status = 'SENT' AND (dueDate = 0 OR DATE(dueDate/1000, 'unixepoch') >= DATE('now')))
          )
    """)
    fun observeHealthyInvoiceCount(businessId: Long): Flow<Int>

    // ==================== PAYMENT METRICS ====================

    /**
     * Average days from invoice date to payment date (approximated as due date)
     * for PAID invoices. Returns 0.0 if no paid invoices exist.
     */
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
    """)
    fun observeAverageDaysToPayment(businessId: Long): Flow<Double>
}
