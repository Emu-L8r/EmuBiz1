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

    data class SegmentationSummaryRow(
        val segment: String,
        val count: Int,
        val avgRevenue: Double,
        val totalRevenue: Double
    )
}

