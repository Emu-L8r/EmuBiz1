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
        AND isActive = 1
    """)
    fun observeDraftInvoiceCount(businessId: Long): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM invoices 
        WHERE businessProfileId = :businessId 
        AND status IN ('SENT', 'OVERDUE')
        AND dueDate < ?2
        AND isActive = 1
    """)
    fun observeOverdueInvoiceCount(businessId: Long, currentTimeMs: Long = System.currentTimeMillis()): Flow<Int>

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

