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

    @Query("SELECT * FROM invoice_analytics_snapshots WHERE invoiceId = :invoiceId LIMIT 1")
    suspend fun getInvoiceSnapshot(invoiceId: Long): InvoiceAnalyticsSnapshot?

    @androidx.room.Update
    suspend fun updateInvoiceSnapshot(snapshot: InvoiceAnalyticsSnapshot)

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySnapshot(snapshot: DailyRevenueSnapshot)

    @Query("SELECT * FROM daily_revenue_snapshots WHERE businessProfileId = :businessId AND dateString = :dateString LIMIT 1")
    suspend fun getDailySnapshotByDate(businessId: Long, dateString: String): DailyRevenueSnapshot?

    @androidx.room.Update
    suspend fun updateDailySnapshot(snapshot: DailyRevenueSnapshot)

    /**
     * Updates a [DailyRevenueSnapshot] only when the stored version matches [expectedVersion].
     *
     * Returns the number of rows updated (1 on success, 0 if the version has changed due to a
     * concurrent write). Callers should retry with a fresh read when 0 is returned.
     */
    @Query("""
        UPDATE daily_revenue_snapshots
        SET totalRevenue      = :totalRevenue,
            paidInvoiceCount  = :paidInvoiceCount,
            version           = version + 1,
            updatedAtMs       = :updatedAtMs
        WHERE id = :id AND version = :expectedVersion
    """)
    suspend fun updateSnapshotWithVersion(
        id: Long,
        totalRevenue: Long,
        paidInvoiceCount: Int,
        expectedVersion: Int,
        updatedAtMs: Long
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoiceSnapshot(snapshot: InvoiceAnalyticsSnapshot)

    @Query("SELECT * FROM daily_revenue_snapshots WHERE businessProfileId = :businessId AND dateString >= :startDate ORDER BY dateString DESC")
    suspend fun getDailyRevenueTrend(businessId: Long, startDate: String): List<DailyRevenueSnapshot>

    @Query("SELECT * FROM daily_revenue_snapshots WHERE businessProfileId = :businessId ORDER BY dateString DESC LIMIT 30")
    suspend fun getLast30DaysRevenue(businessId: Long): List<DailyRevenueSnapshot>

    @Query("SELECT * FROM daily_revenue_snapshots WHERE businessProfileId = :businessId ORDER BY dateString DESC LIMIT 30")
    fun observeLast30DaysRevenue(businessId: Long): Flow<List<DailyRevenueSnapshot>>

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

    // ==================== HEALTH CHECK QUERIES ====================

    /**
     * Count total invoice analytics snapshots.
     * Used to verify snapshot coverage.
     */
    @Query("SELECT COUNT(*) FROM invoice_analytics_snapshots")
    suspend fun countInvoiceSnapshots(): Int

    @Query("DELETE FROM invoice_analytics_snapshots WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceSnapshot(invoiceId: Long)

    /**
     * Find invoice IDs that are missing analytics snapshots.
     * Used for health reporting and recovery.
     */
    @Query("""
        SELECT DISTINCT i.id FROM invoices i
        LEFT JOIN invoice_analytics_snapshots ias ON i.id = ias.invoiceId
        WHERE ias.invoiceId IS NULL
    """)
    suspend fun getMissingInvoiceSnapshots(): List<Long>

    /**
     * Find orphaned invoice analytics snapshots (snapshots without invoices).
     * Used for cleanup operations.
     */
    @Query("""
        SELECT DISTINCT ias.invoiceId FROM invoice_analytics_snapshots ias
        LEFT JOIN invoices i ON ias.invoiceId = i.id
        WHERE i.id IS NULL
    """)
    suspend fun getOrphanedInvoiceSnapshots(): List<Long>
}
