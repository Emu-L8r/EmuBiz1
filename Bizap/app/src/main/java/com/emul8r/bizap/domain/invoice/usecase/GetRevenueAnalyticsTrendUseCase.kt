package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.analytics.ChartDataPoint
import com.emul8r.bizap.domain.analytics.TrendMetric
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
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
 * Coordinates data from [RevenueRepository] and aggregates into a single
 * [RevenueTrendData] object consumed by RevenueAnalyticsTabViewModel.
 */
class GetRevenueAnalyticsTrendUseCase @Inject constructor(
    private val revenueRepository: RevenueRepository
) {
    /**
     * Returns a live stream of revenue trend data for the given business.
     *
     * MTD and YTD values are sourced from [RevenueRepository]. Previous-period
     * comparison values are not yet available from the repository and default to 0.
     * The [revenueByStatus] and [topInvoices] fields are empty pending additional
     * repository methods.
     */
    operator fun invoke(businessId: Long): Flow<RevenueTrendData> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return revenueRepository.observeRevenueMetrics(businessId).map { result ->
            val metrics = result.getOrNull()
            RevenueTrendData(
                mtdRevenue = TrendMetric(
                    label = "MTD Revenue",
                    currentValue = (metrics?.mtdRevenue ?: 0L) / 100.0,
                    previousValue = 0.0,
                    unit = "$"
                ),
                ytdRevenue = TrendMetric(
                    label = "YTD Revenue",
                    currentValue = (metrics?.ytdRevenue ?: 0L) / 100.0,
                    previousValue = 0.0,
                    unit = "$"
                ),
                dailyTrend = metrics?.dailyTrend?.map { point ->
                    ChartDataPoint(
                        label = point.date,
                        value = point.revenueCents / 100f,
                        timestamp = runCatching {
                            dateFormat.parse(point.date)?.time ?: 0L
                        }.getOrDefault(0L)
                    )
                } ?: emptyList(),
                revenueByStatus = emptyMap(),
                topInvoices = emptyList()
            )
        }
    }
}

