package com.emul8r.bizap.data.local

import androidx.room.*
import com.emul8r.bizap.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for analytics queries.
 * Provides optimized queries for dashboard metrics.
 * Note: These are read-only queries that return data classes, not entities.
 */
@Dao
interface AnalyticsDao {

    // ═════════════════════════════════════════════════════════════════
    // DAILY REVENUE - Query existing invoice data
    // ═════════════════════════════════════════════════════════════════

    @Query("""
        SELECT 
            :businessId as businessId,
            DATE(date / 1000, 'unixepoch') as date,
            COALESCE(SUM(CASE WHEN status = 'PAID' THEN totalAmount ELSE 0 END), 0) as invoicedCents,
            COALESCE(SUM(amountPaid), 0) as paidCents,
            COUNT(*) as invoiceCount,
            COUNT(CASE WHEN status = 'PAID' THEN 1 END) as paidCount
        FROM invoices
        WHERE businessProfileId = :businessId
        AND date >= (datetime('now', '-30 days') * 1000)
        AND isActive = 1
        GROUP BY DATE(date / 1000, 'unixepoch')
        ORDER BY date ASC
    """)
    fun observeDailyRevenue(businessId: Long): Flow<List<DailyRevenue>>

    // ═════════════════════════════════════════════════════════════════
    // CUSTOMER REVENUE
    // ═════════════════════════════════════════════════════════════════

    @Query("""
        SELECT 
            customerId,
            customerName,
            COALESCE(SUM(amountPaid), 0) as totalRevenueCents,
            COUNT(*) as invoiceCount,
            MAX(dueDate) as lastPaymentDate
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
        AND isActive = 1
        GROUP BY customerId
        ORDER BY totalRevenueCents DESC
        LIMIT :limit
    """)
    fun observeTopCustomers(businessId: Long, limit: Int = 5): Flow<List<CustomerRevenue>>

    // ═════════════════════════════════════════════════════════════════
    // PAYMENT METRICS (Queries for DSO and collection data)
    // ═════════════════════════════════════════════════════════════════

    @Query("""
        SELECT COALESCE(
            AVG(CAST(
                (julianday(datetime(dueDate / 1000, 'unixepoch')) - 
                 julianday(datetime(date / 1000, 'unixepoch')))
                AS REAL
            )),
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
            DATE(createdAt / 1000, 'unixepoch') as date,
            COALESCE(AVG(CAST(
                (julianday(datetime(updatedAt / 1000, 'unixepoch')) - 
                 julianday(datetime(createdAt / 1000, 'unixepoch')))
                AS REAL)), 0.0) as avgDaysFromCreationToSent,
            COUNT(*) as invoicesCreatedCount,
            COUNT(CASE WHEN status IN ('SENT', 'PAID') THEN 1 END) as invoicesSentCount,
            COUNT(CASE WHEN status = 'DRAFT' THEN 1 END) as invoicesInDraftCount
        FROM invoices
        WHERE businessProfileId = :businessId
        AND createdAt >= (datetime('now', '-30 days') * 1000)
        AND isActive = 1
        GROUP BY DATE(createdAt / 1000, 'unixepoch')
        ORDER BY createdAt DESC
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


