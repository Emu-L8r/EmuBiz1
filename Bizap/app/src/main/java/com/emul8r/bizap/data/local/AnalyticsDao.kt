package com.emul8r.bizap.data.local

import androidx.room.*
import com.emul8r.bizap.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for analytics queries.
 * Provides optimized queries for dashboard metrics.
 */
@Dao
interface AnalyticsDao {

    // ═════════════════════════════════════════════════════════════════
    // DAILY REVENUE - Query existing data
    // ═════════════════════════════════════════════════════════════════

    @Query("""
        SELECT 
            :businessId as businessId,
            date(invoices.date / 1000, 'unixepoch') as date,
            COALESCE(SUM(CASE WHEN invoices.status = 'PAID' THEN invoices.totalAmount ELSE 0 END), 0) as invoicedCents,
            COALESCE(SUM(invoices.amountPaid), 0) as paidCents,
            COUNT(*) as invoiceCount,
            COUNT(CASE WHEN invoices.status = 'PAID' THEN 1 END) as paidCount
        FROM invoices
        WHERE invoices.businessProfileId = :businessId
        AND invoices.date >= (SELECT datetime('now', '-30 days', 'unixepoch') * 1000)
        AND invoices.isActive = 1
        GROUP BY date(invoices.date / 1000, 'unixepoch')
        ORDER BY invoices.date ASC
    """)
    fun observeDailyRevenue(businessId: Long): Flow<List<DailyRevenue>>

    // ═════════════════════════════════════════════════════════════════
    // CUSTOMER REVENUE
    // ═════════════════════════════════════════════════════════════════

    @Query("""
        SELECT 
            invoices.customerId,
            invoices.customerName,
            COALESCE(SUM(invoices.totalAmount), 0) as totalRevenueCents,
            COUNT(*) as invoiceCount,
            MAX(invoices.date) / 1000 as lastPaymentDate
        FROM invoices
        WHERE invoices.businessProfileId = :businessId
        AND invoices.status = 'PAID'
        AND invoices.isActive = 1
        GROUP BY invoices.customerId
        ORDER BY totalRevenueCents DESC
        LIMIT :limit
    """)
    fun observeTopCustomers(businessId: Long, limit: Int = 5): Flow<List<CustomerRevenue>>

    // ═════════════════════════════════════════════════════════════════
    // PAYMENT METRICS (Queries for DSO and collection data)
    // ═════════════════════════════════════════════════════════════════

    @Query("""
        SELECT COALESCE(
            CAST(
                AVG(CAST(
                    ((julianday(datetime(dueDate / 1000, 'unixepoch')) - julianday(datetime(date / 1000, 'unixepoch')))) AS REAL
                ))
            AS DOUBLE),
            0.0
        )
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
        AND dueDate > 0
        AND date > 0
    """)
    fun observeAverageDaysToPayment(businessId: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status IN ('SENT', 'DRAFT', 'OVERDUE')
        AND isActive = 1
    """)
    fun observeTotalOutstanding(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaid), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
        AND isActive = 1
    """)
    fun observeTotalCollected(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
        AND isActive = 1
    """)
    fun observeTotalRevenue(businessId: Long): Flow<Long>

    // ═════════════════════════════════════════════════════════════════
    // INVOICE VELOCITY - Computed from invoices
    // ═════════════════════════════════════════════════════════════════

    @Query("""
        SELECT 
            :businessId as businessId,
            date(invoices.createdAt / 1000, 'unixepoch') as date,
            COALESCE(AVG(CAST((julianday(datetime(invoices.updatedAt / 1000, 'unixepoch')) - 
                               julianday(datetime(invoices.createdAt / 1000, 'unixepoch'))) AS REAL)), 0.0) as avgDaysFromCreationToSent,
            COUNT(*) as invoicesCreatedCount,
            COUNT(CASE WHEN invoices.status IN ('SENT', 'PAID') THEN 1 END) as invoicesSentCount,
            COUNT(CASE WHEN invoices.status = 'DRAFT' THEN 1 END) as invoicesInDraftCount
        FROM invoices
        WHERE invoices.businessProfileId = :businessId
        AND invoices.createdAt >= (SELECT datetime('now', '-30 days', 'unixepoch') * 1000)
        AND invoices.isActive = 1
        GROUP BY date(invoices.createdAt / 1000, 'unixepoch')
        ORDER BY invoices.createdAt DESC
        LIMIT 30
    """)
    fun observeInvoicingVelocity(businessId: Long): Flow<List<InvoiceVelocity>>

    // ═════════════════════════════════════════════════════════════════
    // HELPER QUERIES
    // ═════════════════════════════════════════════════════════════════

    @Query("""
        SELECT COUNT(*) 
        FROM invoices 
        WHERE businessProfileId = :businessId 
        AND status = 'DRAFT'
        AND isActive = 1
    """)
    fun observeDraftInvoiceCount(businessId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM invoices 
        WHERE businessProfileId = :businessId 
        AND status IN ('SENT', 'OVERDUE')
        AND dueDate < :currentTimeMs
        AND isActive = 1
    """)
    fun observeOverdueInvoiceCount(businessId: Long, currentTimeMs: Long = System.currentTimeMillis()): Flow<Int>
}


