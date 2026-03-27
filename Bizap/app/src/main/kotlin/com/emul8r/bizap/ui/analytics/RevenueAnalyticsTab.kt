package com.emul8r.bizap.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.ui.analytics.components.HeroMetricCard
import com.emul8r.bizap.ui.analytics.components.LineChartCard

/**
 * Revenue Analytics Tab content.
 *
 * Displays:
 * - MTD Revenue (month-to-date)
 * - YTD Revenue (year-to-date)
 * - Daily trend line chart
 * - Revenue breakdown by status
 * - Top invoices list
 *
 * @param viewModel RevenueAnalyticsTabViewModel providing data
 * @param dateRange Selected date range from parent
 * @param onDrillClick Callback to open drill-down bottom sheet
 * @param modifier Optional modifier
 */
@Composable
fun RevenueAnalyticsTab(
    viewModel: RevenueAnalyticsTabViewModel,
    dateRange: AnalyticsDateRange,
    onDrillClick: (String, List<Pair<String, Double>>) -> Unit,
    modifier: Modifier = Modifier
) {
    viewModel.setDateRange(dateRange)
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (state.error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Error: ${state.error}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // MTD Revenue hero card
            item {
                state.mtdRevenue?.let { metric ->
                    HeroMetricCard(
                        metric = metric,
                        onClick = {
                            onDrillClick(
                                "MTD Revenue Details",
                                state.revenueByStatus.toList()
                            )
                        }
                    )
                }
            }

            // YTD Revenue hero card
            item {
                state.ytdRevenue?.let { metric ->
                    HeroMetricCard(
                        metric = metric,
                        onClick = {
                            onDrillClick(
                                "YTD Revenue Details",
                                state.revenueByStatus.toList()
                            )
                        }
                    )
                }
            }

            // Daily trend line chart
            item {
                LineChartCard(
                    data = state.dailyTrendData,
                    title = "Revenue Trend (Daily)",
                    onDataPointClick = { point ->
                        // TODO: Implement point-specific drill
                    }
                )
            }

            // Revenue by Status breakdown card
            item {
                if (state.revenueByStatus.isNotEmpty()) {
                    HeroMetricCard(
                        metric = com.emul8r.bizap.domain.analytics.TrendMetric(
                            label = "Revenue by Status",
                            currentValue = state.revenueByStatus.values.sum(),
                            previousValue = state.revenueByStatus.values.sum() * 0.95,
                            unit = "$"
                        ),
                        onClick = {
                            onDrillClick(
                                "Revenue Breakdown by Status",
                                state.revenueByStatus.toList()
                            )
                        }
                    )
                }
            }

            // Top invoices list
            item {
                if (state.topInvoices.isNotEmpty()) {
                    HeroMetricCard(
                        metric = com.emul8r.bizap.domain.analytics.TrendMetric(
                            label = "Top Invoices",
                            currentValue = state.topInvoices.size.toDouble(),
                            previousValue = state.topInvoices.size.toDouble(),
                            unit = "invoices"
                        ),
                        onClick = {
                            onDrillClick("Top Invoices", state.topInvoices)
                        }
                    )
                }
            }
        }
    }
}

