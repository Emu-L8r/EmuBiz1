package com.emul8r.bizap.data.repository.analytics

import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.domain.model.gui2.DailyTrendPointV2
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.model.gui2.RiskMetricsV2
import com.emul8r.bizap.domain.model.gui2.StatusBreakdownV2
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralises all analytics calculation logic that was previously duplicated
 * across [RevenueRepositoryV2], [PaymentAnalyticsRepositoryV2], and
 * [RiskAnalyticsRepositoryV2].
 *
 * Extracting these calculations here makes them:
 *   1. Testable in isolation without requiring a DAO or Flow.
 *   2. The single source of truth for each formula.
 *   3. Easy to update consistently across all repositories.
 */
@Singleton
class AnalyticsCalculator @Inject constructor() {

    /**
     * Combines individual revenue measurements into a unified [RevenueMetricsV2].
     *
     * @param businessId  Owning business profile ID.
     * @param mtd         Month-to-date paid revenue in cents.
     * @param ytd         Year-to-date paid revenue in cents.
     * @param weekly      Last-7-days paid revenue in cents.
     * @param totalPaid   All-time paid revenue in cents.
     * @param trend       Raw trend rows from DAO.
     * @return Combined [RevenueMetricsV2] (outstandingAmount/collectedAmount left at 0 to be
     *         populated by the payment repository layer).
     */
    fun combineRevenueMetrics(
        businessId: Long,
        mtd: Long,
        ytd: Long,
        weekly: Long,
        totalPaid: Long,
        trend: List<DailyRevenueTrendV2>
    ): RevenueMetricsV2 = RevenueMetricsV2(
        businessProfileId = businessId,
        mtdRevenue = mtd,
        ytdRevenue = ytd,
        weeklyRevenue = weekly,
        totalPaidRevenue = totalPaid,
        outstandingAmount = 0L,   // populated by PaymentAnalyticsRepositoryV2
        collectedAmount = 0L,     // populated by PaymentAnalyticsRepositoryV2
        dailyTrend = trend.map { point ->
            DailyTrendPointV2(
                date = point.dateString,
                revenueCents = point.revenue,
                invoiceCount = point.invoiceCount
            )
        }
    )

    /**
     * Combines individual payment measurements into a unified [PaymentMetricsV2].
     *
     * Collection rate is: (collected / (collected + outstanding)) × 100, clamped to [0, 100].
     *
     * @param businessId   Owning business profile ID.
     * @param outstanding  Total unpaid amount in cents.
     * @param collected    Total collected amount in cents.
     * @param statusCounts Per-status invoice counts from DAO.
     * @param overdueCount Number of overdue invoices.
     * @param avgDays      Average days from issue to payment.
     * @return Combined [PaymentMetricsV2].
     */
    fun combinePaymentMetrics(
        businessId: Long,
        outstanding: Long,
        collected: Long,
        statusCounts: List<InvoiceStatusCountV2>,
        overdueCount: Int,
        avgDays: Double
    ): PaymentMetricsV2 {
        val countMap = statusCounts.associate { it.status to it.count }
        val collectionRate = computeCollectionRate(outstanding, collected)
        return PaymentMetricsV2(
            businessProfileId = businessId,
            totalInvoices = statusCounts.sumOf { it.count },
            paidCount = countMap["PAID"] ?: 0,
            sentCount = countMap["SENT"] ?: 0,
            overdueCount = overdueCount,
            partiallyPaidCount = countMap["PARTIALLY_PAID"] ?: 0,
            draftCount = countMap["DRAFT"] ?: 0,
            outstandingAmount = outstanding,
            collectedAmount = collected,
            collectionRate = collectionRate,
            averageDaysToPayment = avgDays,
            statusBreakdown = statusCounts.map { StatusBreakdownV2(it.status, it.count) }
        )
    }

    /**
     * Combines individual risk measurements into a unified [RiskMetricsV2].
     *
     * @param businessId  Owning business profile ID.
     * @param highRisk    Count of invoices overdue by 60+ days.
     * @param atRisk      Count of invoices overdue by 30–59 days.
     * @param healthy     Count of paid or not-yet-due invoices.
     * @param overdue     Total overdue invoice count.
     * @param outstanding Total outstanding amount in cents.
     * @return Combined [RiskMetricsV2].
     */
    fun combineRiskMetrics(
        businessId: Long,
        highRisk: Int,
        atRisk: Int,
        healthy: Int,
        overdue: Int,
        outstanding: Long
    ): RiskMetricsV2 = RiskMetricsV2(
        businessProfileId = businessId,
        highRiskCount = highRisk,
        atRiskCount = atRisk,
        healthyCount = healthy,
        overdueCount = overdue,
        totalOutstandingCents = outstanding
    )

    /**
     * Computes collection rate as a percentage bounded to [0.0, 100.0].
     *
     * Formula: collected / (collected + outstanding) × 100
     */
    fun computeCollectionRate(outstanding: Long, collected: Long): Double =
        if (collected + outstanding > 0L) {
            (collected * 100.0 / (collected + outstanding)).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
}
