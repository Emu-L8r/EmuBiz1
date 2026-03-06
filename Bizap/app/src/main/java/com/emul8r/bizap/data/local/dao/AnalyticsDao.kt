package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emul8r.bizap.data.local.entities.BusinessHealthMetrics
import com.emul8r.bizap.data.local.entities.CustomerAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

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

    @Query("DELETE FROM invoice_analytics_snapshots WHERE businessProfileId = :businessId")
    suspend fun deleteAllInvoiceSnapshots(businessId: Long)

    @Query("DELETE FROM daily_revenue_snapshots WHERE businessProfileId = :businessId")
    suspend fun deleteAllDailySnapshots(businessId: Long)

    @Query("DELETE FROM daily_revenue_snapshots WHERE id = :id")
    suspend fun deleteDailySnapshot(id: Long)

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

    /**
     * Updates [DailyRevenueSnapshot] for a status transition using optimistic locking with retry.
     *
     * Computes the revenue and paid-invoice-count deltas from [oldStatus] → [newStatus] and
     * applies them to the existing snapshot. Retries up to [CONCURRENCY_RETRY_MAX] times on
     * version conflicts. If no snapshot exists for [invoiceDate], the call is a no-op (daily
     * snapshots are created on invoice creation, not on status transitions).
     */
    suspend fun updateDailySnapshotWithOptimisticLock(
        businessId: Long,
        invoiceDate: String,
        invoiceEntity: InvoiceEntity,
        oldStatus: InvoiceStatus,
        newStatus: InvoiceStatus
    ) {
        val paidStatuses = listOf(InvoiceStatus.PAID, InvoiceStatus.PARTIALLY_PAID)
        var attempt = 0
        while (attempt < CONCURRENCY_RETRY_MAX) {
            val existing = getDailySnapshotByDate(businessId, invoiceDate) ?: return

            val oldRevenueContribution = if (oldStatus in paidStatuses) invoiceEntity.amountPaid else 0L
            val newRevenueContribution = if (newStatus in paidStatuses) invoiceEntity.amountPaid else 0L
            val delta = newRevenueContribution - oldRevenueContribution

            val newPaidCount = when {
                oldStatus != InvoiceStatus.PAID && newStatus == InvoiceStatus.PAID ->
                    existing.paidInvoiceCount + 1
                oldStatus == InvoiceStatus.PAID && newStatus != InvoiceStatus.PAID ->
                    (existing.paidInvoiceCount - 1).coerceAtLeast(0)
                else -> existing.paidInvoiceCount
            }

            val rowsUpdated = updateSnapshotWithVersion(
                id = existing.id,
                totalRevenue = (existing.totalRevenue + delta).coerceAtLeast(0L),
                paidInvoiceCount = newPaidCount,
                expectedVersion = existing.version,
                updatedAtMs = System.currentTimeMillis()
            )

            if (rowsUpdated > 0) return

            attempt++
            Timber.w("⚠️ DailyRevenueSnapshot version conflict – retrying ($attempt/$CONCURRENCY_RETRY_MAX)")
        }
        Timber.w("⚠️ Could not update DailyRevenueSnapshot after $CONCURRENCY_RETRY_MAX attempts (concurrent update conflict)")
    }

    companion object {
        const val CONCURRENCY_RETRY_MAX = 5
    }
}
