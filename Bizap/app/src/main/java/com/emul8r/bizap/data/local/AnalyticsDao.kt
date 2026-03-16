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
    // DAILY REVENUE SNAPSHOTS
    // ═════════════════════════════════════════════════════════════════

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRevenue(revenue: DailyRevenue)

    @Query("""
        SELECT * FROM daily_revenue_snapshots
        WHERE businessId = :businessId
        AND date >= date('now', '-30 days')
        ORDER BY date ASC
    """)
    fun observeDailyRevenue(businessId: Long): Flow<List<DailyRevenue>>

    // ═════════════════════════════════════════════════════════════════
    // CUSTOMER REVENUE
    // ═════════════════════════════════════════════════════════════════

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerRevenue(revenue: CustomerRevenue)

    @Query("""
        SELECT * FROM customer_revenue
        WHERE businessId = :businessId
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
                    (julianday(i.paidDate) - julianday(i.sentDate)) AS REAL
                ))
            AS DOUBLE),
            0.0
        )
        FROM invoices i
        WHERE i.businessProfileId = :businessId
        AND i.status = 'PAID'
        AND i.paidDate IS NOT NULL
        AND i.sentDate IS NOT NULL
    """)
    fun observeAverageDaysToPayment(businessId: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amountInvoicedCents), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status != 'DRAFT'
    """)
    fun observeTotalOutstanding(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaidCents), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
    """)
    fun observeTotalCollected(businessId: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(SUM(amountPaidCents), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
        AND isActive = 1
    """)
    fun observeTotalRevenue(businessId: Long): Flow<Long>

    // ═════════════════════════════════════════════════════════════════
    // INVOICE VELOCITY
    // ═════════════════════════════════════════════════════════════════

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceVelocity(velocity: InvoiceVelocity)

    @Query("""
        SELECT * FROM invoice_velocity_metrics
        WHERE businessId = :businessId
        ORDER BY date DESC
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
    """)
    fun observeDraftInvoiceCount(businessId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM invoices 
        WHERE businessProfileId = :businessId 
        AND status = 'SENT'
        AND date(dueDate) < date('now')
    """)
    fun observeOverdueInvoiceCount(businessId: Long): Flow<Int>

    // ═════════════════════════════════════════════════════════════════
    // CLEANUP
    // ═════════════════════════════════════════════════════════════════

    @Query("DELETE FROM daily_revenue_snapshots WHERE date < date('now', '-90 days')")
    suspend fun cleanupOldDailyRevenue()

    @Query("DELETE FROM customer_revenue WHERE updatedAt < :timestamp")
    suspend fun cleanupStaleCustomerRevenue(timestamp: Long)

    @Query("DELETE FROM invoice_velocity_metrics WHERE date < date('now', '-90 days')")
    suspend fun cleanupOldVelocityMetrics()
}

