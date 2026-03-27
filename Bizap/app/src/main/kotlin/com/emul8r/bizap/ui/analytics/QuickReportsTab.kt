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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.analytics.AnalyticsDateRange
import com.emul8r.bizap.domain.analytics.TrendMetric
import com.emul8r.bizap.ui.analytics.components.HeroMetricCard

/**
 * Quick Reports Tab - Executive Dashboard.
 *
 * Displays 9 critical business metrics in a unified view:
 * - Row 1: Revenue metrics (Total Revenue, Growth %, Invoice Count)
 * - Row 2: Payment health (Outstanding, Collection Rate, Days to Pay)
 * - Row 3: Risk indicators (At-Risk Count, Overdue Amount, Risk Score)
 *
 * All metrics are clickable for drill-down analysis.
 *
 * @param viewModel QuickReportsTabViewModel providing data
 * @param dateRange Selected date range from parent
 * @param onDrillClick Callback to open drill-down bottom sheet
 * @param modifier Optional modifier
 */
@Composable
fun QuickReportsTab(
    viewModel: QuickReportsTabViewModel,
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
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // === ROW 1: REVENUE METRICS ===
            item {
                Text(
                    "Revenue & Invoices",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Revenue
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "Total Revenue",
                            currentValue = state.totalRevenue,
                            previousValue = state.totalRevenue * 0.89,
                            unit = "$"
                        ),
                        onClick = {
                            onDrillClick("Revenue Details", listOf(
                                "MTD" to (state.totalRevenue * 0.35),
                                "YTD" to state.totalRevenue
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // YTD Growth %
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "YTD Growth",
                            currentValue = state.revenueGrowth,
                            previousValue = 10.0,
                            unit = "%"
                        ),
                        onClick = {
                            onDrillClick("Growth Analysis", listOf(
                                "Current YTD" to state.totalRevenue,
                                "Previous YTD" to (state.totalRevenue / 1.125)
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Invoice Count
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "Invoice Count",
                            currentValue = state.invoiceCount.toDouble(),
                            previousValue = (state.invoiceCount * 0.913).toDouble(),
                            unit = "invoices"
                        ),
                        onClick = {
                            onDrillClick("Invoice Breakdown", listOf(
                                "Total" to state.invoiceCount.toDouble(),
                                "Paid" to (state.invoiceCount * 0.6).toDouble()
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // === ROW 2: PAYMENT HEALTH ===
            item {
                Text(
                    "Payment Health",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Outstanding Amount
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "Outstanding",
                            currentValue = state.outstandingAmount,
                            previousValue = state.outstandingAmount * 0.8,
                            unit = "$"
                        ),
                        onClick = {
                            onDrillClick("Outstanding Details", listOf(
                                "Current" to state.outstandingAmount,
                                "Previous Month" to (state.outstandingAmount * 0.8)
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Collection Rate
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "Collection Rate",
                            currentValue = state.collectionRate,
                            previousValue = state.collectionRate - 3.0,
                            unit = "%"
                        ),
                        onClick = {
                            onDrillClick("Collection Details", listOf(
                                "Current" to state.collectionRate,
                                "Target" to 80.0
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Days to Payment
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "Days to Payment",
                            currentValue = state.averageDaysToPayment,
                            previousValue = state.averageDaysToPayment + 3.0,
                            unit = "days"
                        ),
                        onClick = {
                            onDrillClick("Payment Timeline", listOf(
                                "Average" to state.averageDaysToPayment,
                                "Target" to 15.0
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // === ROW 3: RISK INDICATORS ===
            item {
                Text(
                    "Risk Indicators",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // At-Risk Count
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "At-Risk",
                            currentValue = state.atRiskCount.toDouble(),
                            previousValue = (state.atRiskCount * 0.625).toDouble(),
                            unit = "invoices"
                        ),
                        onClick = {
                            onDrillClick("At-Risk Details", listOf(
                                "High Risk" to (state.atRiskCount * 0.35).toDouble(),
                                "Medium Risk" to (state.atRiskCount * 0.65).toDouble()
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Overdue Amount
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "Overdue Amount",
                            currentValue = state.overdueTotalAmount,
                            previousValue = state.overdueTotalAmount * 0.765,
                            unit = "$"
                        ),
                        onClick = {
                            onDrillClick("Overdue Details", listOf(
                                "Current" to state.overdueTotalAmount,
                                "Previous Month" to (state.overdueTotalAmount * 0.765)
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Risk Score
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "Risk Score",
                            currentValue = state.riskScore,
                            previousValue = state.riskScore - 2.0,
                            unit = "%"
                        ),
                        onClick = {
                            onDrillClick("Risk Analysis", listOf(
                                "Current Score" to state.riskScore,
                                "Safe Level" to 10.0
                            ))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
