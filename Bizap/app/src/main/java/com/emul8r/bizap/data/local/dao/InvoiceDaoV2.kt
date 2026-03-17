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

    /**
     * Month-to-date revenue using explicit epoch-millisecond boundaries calculated
     * in app code with the device's local timezone (via Calendar.getInstance()).
     * This avoids SQLite's UTC-only date() function causing timezone divergence.
     *
     * Pass [monthStartMs] = start of current month at midnight (local time).
     * Pass [nowMs]        = current time (System.currentTimeMillis()).
     */
    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'PAID' OR status = 'PARTIALLY_PAID')
          AND isActive = 1
          AND date >= :monthStartMs
          AND date <= :nowMs
    """)
    fun observeMTDRevenue(businessId: Long, monthStartMs: Long, nowMs: Long): Flow<Long>

    /**
     * Year-to-date revenue using explicit epoch-millisecond boundaries.
     * Pass [yearStartMs] = start of current year at midnight (local time).
     * Pass [nowMs]       = current time.
     */
    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'PAID' OR status = 'PARTIALLY_PAID')
          AND isActive = 1
          AND date >= :yearStartMs
          AND date <= :nowMs
    """)
    fun observeYTDRevenue(businessId: Long, yearStartMs: Long, nowMs: Long): Flow<Long>

    /**
     * Last-7-days revenue using explicit epoch-millisecond boundaries.
     * Pass [weekStartMs] = 7 days ago from the current time (rolling 7-day window).
     * Pass [nowMs]       = current time.
     */
    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'PAID' OR status = 'PARTIALLY_PAID')
          AND isActive = 1
          AND date >= :weekStartMs
          AND date <= :nowMs
    """)
    fun observeWeeklyRevenue(businessId: Long, weekStartMs: Long, nowMs: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'PAID' OR status = 'PARTIALLY_PAID')
          AND isActive = 1
    """)
    fun observeTotalPaidRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT
            DATE(date/1000, 'unixepoch')                                           AS dateString,
            COALESCE(SUM(CASE WHEN status IN ('PAID', 'PARTIALLY_PAID') THEN amountPaid ELSE 0 END), 0) AS revenue,
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
     * Total outstanding amount (balance remaining) across all invoices that are
     * actively outstanding: SENT, PARTIALLY_PAID, or OVERDUE.
     * DRAFT, PAID, CANCELLED, and DELETED invoices are excluded.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'SENT' OR status = 'PARTIALLY_PAID' OR status = 'OVERDUE')
          AND isActive = 1
    """)
    fun observeOutstandingAmount(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND (status = 'PAID' OR status = 'PARTIALLY_PAID')
          AND isActive = 1
    """)
    fun observeCollectedAmount(businessId: Long): Flow<Long>

    // ==================== STATUS BREAKDOWN ====================

    /**
     * Invoice count by status, EXCLUDING DRAFT invoices.
     * DRAFT invoices are works-in-progress and should not be included in financial metrics.
     * Returns counts for: PAID, PARTIALLY_PAID, SENT, OVERDUE, CANCELLED only.
     */
    @Query("""
        SELECT status, COUNT(*) AS count
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status IN ('PAID', 'PARTIALLY_PAID', 'SENT', 'OVERDUE', 'CANCELLED')
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

    /**
     * Total outstanding balance across all OVERDUE invoices (totalAmount - amountPaid).
     * Returns the actual overdue amount from the database rather than an estimate.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND status = 'OVERDUE'
          AND isActive = 1
    """)
    fun observeOverdueAmount(businessId: Long): Flow<Long>

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

    // ==================== ACCOUNTING SERVICE QUERIES ====================

    /**
     * Outstanding amount for specified statuses (parameterized for AccountingService).
     * Statuses must be passed as their string name (e.g. "SENT", "OVERDUE").
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status IN (:statuses)
    """)
    fun observeOutstandingAmountForStatuses(
        businessId: Long,
        statuses: List<String>
    ): Flow<Long>

    /**
     * Collected amount (sum of amountPaid) for specified statuses.
     * Statuses must be passed as their string name.
     */
    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status IN (:statuses)
    """)
    fun observeCollectedAmountForStatuses(
        businessId: Long,
        statuses: List<String>
    ): Flow<Long>

    /**
     * Billed amount (sum of totalAmount) excluding specified statuses.
     * Statuses must be passed as their string name.
     */
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status NOT IN (:excludeStatuses)
    """)
    fun observeBilledAmount(
        businessId: Long,
        excludeStatuses: List<String>
    ): Flow<Long>

    /**
     * Combined billed/collected metrics for collection rate calculation.
     * Excludes DRAFT invoices.
     */
    @Query("""
        SELECT
            COALESCE(SUM(totalAmount), 0)  AS billedAmount,
            COALESCE(SUM(amountPaid), 0)   AS collectedAmount
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status NOT IN ('DRAFT')
    """)
    fun observeCollectionMetrics(businessId: Long): Flow<CollectionSummary>

    /**
     * Revenue (sum of amountPaid) within a date range for a single status.
     * Uses invoice.date field (creation date, stored as epoch millis).
     * Status must be passed as its string name.
     */
    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND date >= :startDateMillis
          AND date <= :endDateMillis
          AND status = :status
    """)
    fun observeRevenueInDateRange(
        businessId: Long,
        startDateMillis: Long,
        endDateMillis: Long,
        status: String
    ): Flow<Long>

    /**
     * Invoice count for the specified set of statuses.
     * Statuses must be passed as their string name.
     */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status IN (:statuses)
    """)
    fun observeInvoiceCountForStatuses(
        businessId: Long,
        statuses: List<String>
    ): Flow<Int>

    /** Result type returned by [observeCollectionMetrics]. */
    data class CollectionSummary(
        @androidx.room.ColumnInfo(name = "billedAmount")   val billedAmount: Long,
        @androidx.room.ColumnInfo(name = "collectedAmount") val collectedAmount: Long
    )

    // ==================== DISPLAY NAME / DAILY COUNTER ====================

    /**
     * Count how many invoices (for any customer) were created on the same UTC date
     * as [dateMillis]. Used to compute the daily reset counter for new invoice display names.
     */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE DATE(createdAt/1000, 'unixepoch') = DATE(:dateMillis/1000, 'unixepoch')
          AND isActive = 1
    """)
    suspend fun countInvoicesOnDate(dateMillis: Long): Int

    // ==================== INVOICE ANALYTICS TIME SERIES ====================

    /**
     * Invoices grouped by week (ISO week string YYYY-WW) for the last [months] months.
     * Returns counts split by whether the invoice is completed (PAID) or not.
     */
    @Query("""
        SELECT
            strftime('%Y-W%W', date/1000, 'unixepoch') AS periodLabel,
            COUNT(*) AS totalCount,
            COUNT(CASE WHEN status = 'PAID' THEN 1 END) AS paidCount,
            COUNT(CASE WHEN status != 'PAID' AND status != 'DRAFT' THEN 1 END) AS sentCount
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status != 'DRAFT'
          AND date >= strftime('%s', 'now', '-' || :months || ' months') * 1000
        GROUP BY periodLabel
        ORDER BY periodLabel ASC
    """)
    suspend fun getWeeklyInvoiceTrend(businessId: Long, months: Int): List<InvoicePeriodStat>

    /**
     * Invoices grouped by month (YYYY-MM) for the last [months] months.
     */
    @Query("""
        SELECT
            strftime('%Y-%m', date/1000, 'unixepoch') AS periodLabel,
            COUNT(*) AS totalCount,
            COUNT(CASE WHEN status = 'PAID' THEN 1 END) AS paidCount,
            COUNT(CASE WHEN status != 'PAID' AND status != 'DRAFT' THEN 1 END) AS sentCount
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status != 'DRAFT'
          AND date >= strftime('%s', 'now', '-' || :months || ' months') * 1000
        GROUP BY periodLabel
        ORDER BY periodLabel ASC
    """)
    suspend fun getMonthlyInvoiceTrend(businessId: Long, months: Int): List<InvoicePeriodStat>

    // ==================== DASHBOARD INVOICE COUNTS ====================

    /** Total count of all active invoices (any status except DRAFT). */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status != 'DRAFT'
    """)
    fun observeTotalInvoiceCount(businessId: Long): Flow<Int>

    /** Count of invoices with PAID status. */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status = 'PAID'
    """)
    fun observePaidInvoiceCount(businessId: Long): Flow<Int>

    /** Count of invoices with SENT status (pending payment). */
    @Query("""
        SELECT COUNT(*)
        FROM invoices
        WHERE businessProfileId = :businessId
          AND isActive = 1
          AND status = 'SENT'
    """)
    fun observeSentInvoiceCount(businessId: Long): Flow<Int>
}

/** Aggregated invoice stats for one time period (week or month). */
data class InvoicePeriodStat(
    @androidx.room.ColumnInfo(name = "periodLabel") val periodLabel: String,
    @androidx.room.ColumnInfo(name = "totalCount")  val totalCount: Int,
    @androidx.room.ColumnInfo(name = "paidCount")   val paidCount: Int,
    @androidx.room.ColumnInfo(name = "sentCount")   val sentCount: Int
)
