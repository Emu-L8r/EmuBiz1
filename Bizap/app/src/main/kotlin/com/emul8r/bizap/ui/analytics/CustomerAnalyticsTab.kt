package com.emul8r.bizap.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.domain.analytics.TrendMetric
import com.emul8r.bizap.ui.analytics.components.HeroMetricCard
import com.emul8r.bizap.ui.analytics.components.PieChartCard
import com.emul8r.bizap.ui.designsystem.BizapColors

/**
 * Customer Analytics Tab content.
 *
 * Displays:
 * - Total Customer Count with segmentation breakdown
 * - Customer segmentation pie chart (VIP, Regular, At-Risk, Dormant)
 * - Individual segment cards with visual percentages
 * - Average Lifetime Value (LTV)
 * - Churn Rate with trend indicator
 * - Interactive drill-down on all metrics
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
            // Total Customers Header
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

            // Customer Segmentation Pie Chart
            item {
                PieChartCard(
                    data = listOf(
                        "VIP (⭐)" to state.vipCount.toDouble(),
                        "Regular" to state.regularCount.toDouble(),
                        "At-Risk (⚠️)" to state.atRiskCount.toDouble(),
                        "Dormant" to state.dormantCount.toDouble()
                    ),
                    title = "📊 Customer Segmentation"
                )
            }

            // Segment cards in a grid layout
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: VIP & Regular
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SegmentCard(
                            label = "⭐ VIP",
                            count = state.vipCount,
                            percentage = if (state.totalCustomers > 0) {
                                (state.vipCount.toDouble() / state.totalCustomers * 100).toInt()
                            } else 0,
                            color = BizapColors.AnalyticsExcellent,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDrillClick(
                                    "VIP Customers",
                                    listOf("VIP Count" to state.vipCount.toDouble())
                                )
                            }
                        )

                        SegmentCard(
                            label = "Regular",
                            count = state.regularCount,
                            percentage = if (state.totalCustomers > 0) {
                                (state.regularCount.toDouble() / state.totalCustomers * 100).toInt()
                            } else 0,
                            color = BizapColors.AnalyticsGood,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDrillClick(
                                    "Regular Customers",
                                    listOf("Regular Count" to state.regularCount.toDouble())
                                )
                            }
                        )
                    }

                    // Row 2: At-Risk & Dormant
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SegmentCard(
                            label = "⚠️ At-Risk",
                            count = state.atRiskCount,
                            percentage = if (state.totalCustomers > 0) {
                                (state.atRiskCount.toDouble() / state.totalCustomers * 100).toInt()
                            } else 0,
                            color = BizapColors.AnalyticsWarning,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDrillClick(
                                    "At-Risk Customers",
                                    listOf("At-Risk Count" to state.atRiskCount.toDouble())
                                )
                            }
                        )

                        SegmentCard(
                            label = "Dormant",
                            count = state.dormantCount,
                            percentage = if (state.totalCustomers > 0) {
                                (state.dormantCount.toDouble() / state.totalCustomers * 100).toInt()
                            } else 0,
                            color = BizapColors.AnalyticsAtRisk,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDrillClick(
                                    "Dormant Customers",
                                    listOf("Dormant Count" to state.dormantCount.toDouble())
                                )
                            }
                        )
                    }
                }
            }

            // Average Lifetime Value (LTV)
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Average Customer LTV",
                        currentValue = state.averageLTV,
                        previousValue = state.averageLTV * 0.97,
                        unit = "$"
                    ),
                    onClick = {
                        onDrillClick(
                            "Customer LTV",
                            listOf(
                                "Average LTV" to state.averageLTV,
                                "Total Customers" to state.totalCustomers.toDouble()
                            )
                        )
                    }
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
                    ),
                    onClick = {
                        onDrillClick(
                            "Churn Metrics",
                            listOf(
                                "Churn Rate" to state.churnRate,
                                "At-Risk Customers" to state.atRiskCount.toDouble()
                            )
                        )
                    }
                )
            }
        }
    }
}

/**
 * Reusable segment card component showing count, percentage, and visual progress bar.
 */
@Composable
private fun SegmentCard(
    label: String,
    count: Int,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .heightIn(min = 100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = color.copy(alpha = 0.3f)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(fraction = percentage / 100f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(2.dp),
                    color = color
                ) {}
            }

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
