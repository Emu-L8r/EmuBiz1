package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.analytics.ChartDataPoint
import com.emul8r.bizap.domain.analytics.TrendMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Data class representing complete revenue trend analytics.
 *
 * Bundles MTD/YTD metrics, daily trends, status breakdown, and top invoices
 * into a single object for easy consumption by ViewModels.
 */
data class RevenueTrendData(
    val mtdRevenue: TrendMetric,
    val ytdRevenue: TrendMetric,
    val dailyTrend: List<ChartDataPoint>,
    val revenueByStatus: Map<String, Double>,
    val topInvoices: List<Pair<String, Double>>
)

/**
 * Use case for fetching complete revenue analytics trends.
 *
 * Coordinates data from multiple repository methods and aggregates into
 * a single RevenueTrendData object that's consumed by RevenueAnalyticsTabViewModel.
 *
 * TODO: Wire with RevenueRepository once repository is implemented.
 */
class GetRevenueAnalyticsTrendUseCase @Inject constructor() {
    // TODO: Uncomment when repository exists and inject it
    // @Inject constructor(private val repository: RevenueRepository)

    /**
     * Mock implementation for testing UI without repository.
     * Returns sample revenue data for display.
     */
    operator fun invoke(businessId: Long): Flow<RevenueTrendData> {
        return flowOf(
            RevenueTrendData(
                mtdRevenue = TrendMetric(
                    label = "MTD Revenue",
                    currentValue = 5000.0,
                    previousValue = 4620.0,
                    unit = "$"
                ),
                ytdRevenue = TrendMetric(
                    label = "YTD Revenue",
                    currentValue = 45000.0,
                    previousValue = 40000.0,
                    unit = "$"
                ),
                dailyTrend = listOf(
                    ChartDataPoint("Mar 1", 150f, System.currentTimeMillis() - 86400000 * 6),
                    ChartDataPoint("Mar 2", 200f, System.currentTimeMillis() - 86400000 * 5),
                    ChartDataPoint("Mar 3", 175f, System.currentTimeMillis() - 86400000 * 4),
                    ChartDataPoint("Mar 4", 280f, System.currentTimeMillis() - 86400000 * 3),
                    ChartDataPoint("Mar 5", 320f, System.currentTimeMillis() - 86400000 * 2),
                    ChartDataPoint("Mar 6", 290f, System.currentTimeMillis() - 86400000),
                    ChartDataPoint("Mar 7", 350f, System.currentTimeMillis())
                ),
                revenueByStatus = mapOf(
                    "PAID" to 3500.0,
                    "PARTIALLY_PAID" to 800.0,
                    "SENT" to 700.0
                ),
                topInvoices = listOf(
                    "Invoice #2026-001" to 2500.0,
                    "Invoice #2026-002" to 1200.0,
                    "Invoice #2026-003" to 800.0,
                    "Invoice #2026-004" to 500.0
                )
            )
        )
    }
}

