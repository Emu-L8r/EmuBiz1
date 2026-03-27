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
import com.emul8r.bizap.domain.analytics.TrendMetric
import com.emul8r.bizap.ui.analytics.components.HeroMetricCard
import com.emul8r.bizap.ui.analytics.components.PieChartCard

/**
 * Customer Analytics Tab content.
 *
 * Displays:
 * - Total Customer Count
 * - VIP Customer Segment
 * - Regular Customer Segment
 * - At-Risk Customer Segment
 * - Dormant Customer Segment
 * - Average Lifetime Value
 * - Churn Rate
 *
 * @param viewModel CustomerAnalyticsTabViewModel providing data
 * @param dateRange Selected date range from parent
 * @param onDrillClick Callback to open drill-down bottom sheet
 * @param modifier Optional modifier
 */
@Composable
fun CustomerAnalyticsTab(
    viewModel: CustomerAnalyticsTabViewModel,
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
            // Total Customers
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Total Customers",
                        currentValue = state.totalCustomers.toDouble(),
                        previousValue = (state.totalCustomers * 0.95).toDouble(),
                        unit = "customers"
                    ),
                    onClick = {
                        onDrillClick(
                            "Customer Breakdown",
                            listOf(
                                "VIP" to state.vipCount.toDouble(),
                                "Regular" to state.regularCount.toDouble(),
                                "At-Risk" to state.atRiskCount.toDouble(),
                                "Dormant" to state.dormantCount.toDouble()
                            )
                        )
                    }
                )
            }

            // VIP Customers
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "⭐ VIP Customers",
                        currentValue = state.vipCount.toDouble(),
                        previousValue = (state.vipCount * 0.98).toDouble(),
                        unit = "customers"
                    )
                )
            }

            // Regular Customers
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Regular Customers",
                        currentValue = state.regularCount.toDouble(),
                        previousValue = (state.regularCount * 0.95).toDouble(),
                        unit = "customers"
                    )
                )
            }

            // Customer Segmentation Pie Chart
            item {
                PieChartCard(
                    data = listOf(
                        "VIP" to state.vipCount.toDouble(),
                        "Regular" to state.regularCount.toDouble(),
                        "At-Risk" to state.atRiskCount.toDouble(),
                        "Dormant" to state.dormantCount.toDouble()
                    ),
                    title = "Customer Segmentation"
                )
            }

            // At-Risk Customers
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "⚠️ At-Risk Customers",
                        currentValue = state.atRiskCount.toDouble(),
                        previousValue = (state.atRiskCount * 1.1).toDouble(),
                        unit = "customers"
                    ),
                    onClick = {
                        onDrillClick(
                            "At-Risk Customers",
                            listOf(
                                "Need Attention" to state.atRiskCount.toDouble()
                            )
                        )
                    }
                )
            }

            // Average Lifetime Value (LTV)
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Average Customer LTV",
                        currentValue = state.averageLTV,
                        previousValue = state.averageLTV * 0.97,
                        unit = "$"
                    )
                )
            }

            // Churn Rate
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Churn Rate",
                        currentValue = state.churnRate,
                        previousValue = state.churnRate * 1.05,
                        unit = "%"
                    )
                )
            }
        }
    }
}



