package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.CustomerAnalyticsSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Data access for customer analytics snapshots.
 * Optimized for fast aggregations and segmentation queries.
 */
@Dao
interface CustomerAnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<CustomerAnalyticsSnapshot>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: CustomerAnalyticsSnapshot): Long

    @Query("SELECT * FROM customer_analytics_snapshots WHERE customerId = :customerId LIMIT 1")
    suspend fun getCustomerSnapshot(customerId: Long): CustomerAnalyticsSnapshot?

    @Query("SELECT * FROM customer_analytics_snapshots WHERE businessProfileId = :businessId ORDER BY totalRevenue DESC")
    suspend fun getAllCustomerSnapshots(businessId: Long): List<CustomerAnalyticsSnapshot>

    @Query("SELECT * FROM customer_analytics_snapshots WHERE businessProfileId = :businessId AND segment = :segment ORDER BY totalRevenue DESC")
    suspend fun getCustomersBySegment(businessId: Long, segment: String): List<CustomerAnalyticsSnapshot>

    @Query("SELECT * FROM customer_analytics_snapshots WHERE businessProfileId = :businessId AND isPredictedToChurn = 1 ORDER BY churnRiskScore DESC")
    suspend fun getAtRiskCustomers(businessId: Long): List<CustomerAnalyticsSnapshot>

    @Query("SELECT * FROM customer_analytics_snapshots WHERE businessProfileId = :businessId ORDER BY estimatedLTV DESC LIMIT :limit")
    suspend fun getTopValueCustomers(businessId: Long, limit: Int = 10): List<CustomerAnalyticsSnapshot>

    @Query("DELETE FROM customer_analytics_snapshots WHERE customerId = :customerId")
    suspend fun deleteCustomerSnapshot(customerId: Long)

    @Query("""
        SELECT 
            segment,
            COUNT(*) as count,
            AVG(totalRevenue) as avgRevenue,
            SUM(totalRevenue) as totalRevenue
        FROM customer_analytics_snapshots
        WHERE businessProfileId = :businessId
        GROUP BY segment
    """)
    suspend fun getSegmentationSummary(businessId: Long): List<SegmentationSummaryRow>

    // ==================== HEALTH CHECK QUERIES ====================

    /**
     * Count total customer analytics snapshots.
     * Used to verify snapshot coverage.
     */
    @Query("SELECT COUNT(*) FROM customer_analytics_snapshots")
    suspend fun countSnapshots(): Int

    /**
     * Find customer IDs with missing analytics snapshots.
     * Used for health reporting and recovery.
     */
    @Query("""
        SELECT DISTINCT c.id FROM (
            SELECT DISTINCT customerId as id FROM invoices
        ) c
        LEFT JOIN customer_analytics_snapshots cas ON c.id = cas.customerId
        WHERE cas.customerId IS NULL
    """)
    suspend fun getMissingSnapshots(): List<Long>

    /**
     * Find orphaned customer analytics snapshots (snapshots without customers).
     * Used for cleanup operations.
     */
    @Query("""
        SELECT DISTINCT cas.customerId FROM customer_analytics_snapshots cas
        LEFT JOIN (
            SELECT DISTINCT customerId FROM invoices
        ) c ON cas.customerId = c.customerId
        WHERE c.customerId IS NULL
    """)
    suspend fun getOrphanedSnapshots(): List<Long>

    data class SegmentationSummaryRow(
        val segment: String,
        val count: Int,
        val avgRevenue: Double,
        val totalRevenue: Double
    )
}
