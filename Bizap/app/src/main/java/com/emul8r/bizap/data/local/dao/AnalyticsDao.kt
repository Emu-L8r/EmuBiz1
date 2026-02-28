package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.data.local.entities.CustomerAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.BusinessHealthMetrics
import kotlinx.coroutines.flow.Flow

/**
 * Data access for analytics queries
 */
@Dao
interface AnalyticsDao {

    // ==================== INVOICE ANALYTICS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceSnapshot(snapshot: InvoiceAnalyticsSnapshot)

    @Query("SELECT * FROM invoice_analytics_snapshots WHERE businessProfileId = :businessId ORDER BY invoiceDateMs DESC LIMIT 100")
    suspend fun getRecentInvoices(businessId: Long): List<InvoiceAnalyticsSnapshot>

    @Query("SELECT * FROM invoice_analytics_snapshots WHERE businessProfileId = :businessId AND status = :status")
    suspend fun getInvoicesByStatus(businessId: Long, status: String): List<InvoiceAnalyticsSnapshot>

    @Query("SELECT SUM(totalAmount) as total FROM invoice_analytics_snapshots WHERE businessProfileId = :businessId AND isPaid = 1")
    suspend fun getTotalPaidRevenue(businessId: Long): Double?

    @Query("SELECT COUNT(*) FROM invoice_analytics_snapshots WHERE businessProfileId = :businessId AND status = 'PAID'")
    suspend fun getPaidInvoiceCount(businessId: Long): Int

    // ==================== DAILY REVENUE ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRevenue(snapshot: DailyRevenueSnapshot)

    @Query("SELECT * FROM daily_revenue_snapshots WHERE businessProfileId = :businessId AND dateString >= :startDate ORDER BY dateString DESC")
    suspend fun getDailyRevenueTrend(businessId: Long, startDate: String): List<DailyRevenueSnapshot>

    @Query("SELECT * FROM daily_revenue_snapshots WHERE businessProfileId = :businessId ORDER BY dateString DESC LIMIT 30")
    suspend fun getLast30DaysRevenue(businessId: Long): List<DailyRevenueSnapshot>

    // ==================== CUSTOMER ANALYTICS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerSnapshot(snapshot: CustomerAnalyticsSnapshot)

    @Query("SELECT * FROM customer_analytics_snapshots WHERE businessProfileId = :businessId ORDER BY customerLifetimeValue DESC LIMIT 10")
    suspend fun getTopCustomers(businessId: Long): List<CustomerAnalyticsSnapshot>

    @Query("SELECT * FROM customer_analytics_snapshots WHERE businessProfileId = :businessId AND isActive = 1")
    suspend fun getActiveCustomers(businessId: Long): List<CustomerAnalyticsSnapshot>

    @Query("SELECT AVG(customerLifetimeValue) FROM customer_analytics_snapshots WHERE businessProfileId = :businessId")
    suspend fun getAverageCustomerValue(businessId: Long): Double?

    // ==================== BUSINESS HEALTH ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessHealth(metrics: BusinessHealthMetrics)

    @Query("SELECT * FROM business_health_metrics WHERE businessProfileId = :businessId")
    suspend fun getBusinessHealth(businessId: Long): BusinessHealthMetrics?

    @Query("SELECT * FROM business_health_metrics WHERE businessProfileId = :businessId")
    fun observeBusinessHealth(businessId: Long): Flow<BusinessHealthMetrics?>
}

