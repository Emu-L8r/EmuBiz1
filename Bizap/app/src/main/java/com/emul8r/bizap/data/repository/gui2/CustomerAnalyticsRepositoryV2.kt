package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.CustomerAnalyticsDao
import com.emul8r.bizap.data.local.dao.CustomerDaoV2
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.domain.model.gui2.CustomerMetricsV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUI2 customer analytics repository.
 * Provides customer segmentation metrics and KPIs based on real customer data.
 *
 * **Segmentation Logic:**
 * - **VIP:** High lifetime value customers (top 20% by revenue)
 * - **Regular:** Consistent invoice history (3+ invoices in last 90 days)
 * - **At-Risk:** Decreasing activity or unpaid invoices
 * - **Dormant:** No activity in last 90 days
 */
@Singleton
class CustomerAnalyticsRepositoryV2 @Inject constructor(
    private val customerDaoV2: CustomerDaoV2,
    private val customerAnalyticsDao: CustomerAnalyticsDao,
    private val invoiceDaoV2: InvoiceDaoV2,
    private val calculator: AnalyticsCalculator
) {
    /**
     * Observe customer metrics for the given business using real data from database.
     *
     * Queries customer snapshots and invoice data to calculate:
     * - Total customer count
     * - Segmentation breakdown (VIP, Regular, At-Risk, Dormant)
     * - Average Lifetime Value (LTV)
     * - Churn rate
     */
    fun observeCustomerMetrics(businessId: Long): Flow<Result<CustomerMetricsV2>> {
        return flowOf(businessId)
            .map { bid ->
                try {
                    Timber.d("CustomerAnalyticsRepositoryV2: Fetching real metrics for businessId=$bid")

                    // Get all customer snapshots (analytics data) for this business
                    val snapshots = customerAnalyticsDao.getAllCustomerSnapshots(bid)

                    if (snapshots.isEmpty()) {
                        Timber.d("CustomerAnalyticsRepositoryV2: No customers found for businessId=$bid")
                        return@map Result.success(CustomerMetricsV2(
                            totalCustomers = 0,
                            vipCount = 0,
                            regularCount = 0,
                            atRiskCount = 0,
                            dormantCount = 0,
                            averageLTV = 0.0,
                            churnRate = 0.0
                        ))
                    }

                    val totalCustomers = snapshots.size

                    // Calculate segment counts based on snapshot data
                    // VIP: Top 20% by total revenue
                    val sortedByRevenue = snapshots.sortedByDescending { it.totalRevenue }
                    val vipThreshold = (totalCustomers * 0.2).toInt().coerceAtLeast(1)
                    val vipCount = vipThreshold

                    // Count invoices to segment other customers
                    val regularCount = snapshots.count {
                        it.invoiceCount >= 3 && it !in sortedByRevenue.take(vipCount)
                    }

                    // At-Risk: Have unpaid invoices or low activity
                    // Note: overdueInvoiceCount > 0 indicates overdue invoices
                    val atRiskCount = snapshots.count {
                        (it.overdueInvoiceCount > 0 || it.invoiceCount < 3) &&
                        it !in sortedByRevenue.take(vipCount) &&
                        it.invoiceCount > 0
                    }

                    // Dormant: No invoices or very low activity
                    val dormantCount = totalCustomers - vipCount - regularCount - atRiskCount

                    // Calculate average LTV (revenue / customer, convert from cents)
                    val totalRevenue = snapshots.sumOf { it.totalRevenue }
                    val averageLTV = if (totalCustomers > 0) (totalRevenue / totalCustomers) / 100.0 else 0.0

                    // Calculate churn rate (customers with overdue invoices / total customers)
                    val atRiskOrUnpaid = snapshots.count { it.overdueInvoiceCount > 0 }
                    val churnRate = if (totalCustomers > 0) (atRiskOrUnpaid.toDouble() / totalCustomers * 100) else 0.0

                    val metrics = CustomerMetricsV2(
                        totalCustomers = totalCustomers,
                        vipCount = vipCount,
                        regularCount = regularCount,
                        atRiskCount = atRiskCount,
                        dormantCount = dormantCount.coerceAtLeast(0),
                        averageLTV = averageLTV,
                        churnRate = churnRate
                    )

                    Timber.d("CustomerAnalyticsRepositoryV2: Calculated metrics - Total=$totalCustomers, VIP=$vipCount, Regular=$regularCount, AtRisk=$atRiskCount, Dormant=$dormantCount")
                    Result.success(metrics)
                } catch (e: Exception) {
                    Timber.e(e, "CustomerAnalyticsRepositoryV2: Error calculating customer metrics for businessId=$businessId")
                    Result.failure(e)
                }
            }
            .catch { e ->
                Timber.e(e, "CustomerAnalyticsRepositoryV2: Flow error")
                emit(Result.failure(e))
            }
    }
}



