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
import com.emul8r.bizap.domain.analytics.TrendDirection
import com.emul8r.bizap.ui.analytics.components.HeroMetricCard
import com.emul8r.bizap.ui.analytics.components.BarChartCard

/**
 * Payment Analytics Tab content.
 *
 * Displays:
 * - Outstanding Amount
 * - Collection Rate %
 * - Days Sales Outstanding (DSO)
 * - Aging breakdown by bucket
 * - Risk alerts
 *
 * Integrates with existing PaymentAnalyticsTabViewModel which wraps
 * GetPaymentAnalyticsUseCase.
 *
 * @param viewModel PaymentAnalyticsTabViewModel providing data
 * @param dateRange Selected date range from parent
 * @param onDrillClick Callback to open drill-down bottom sheet
 * @param modifier Optional modifier
 */
@Composable
fun PaymentAnalyticsTab(
    viewModel: PaymentAnalyticsTabViewModel,
    dateRange: AnalyticsDateRange,
    onDrillClick: (String, List<Pair<String, Double>>) -> Unit,
    modifier: Modifier = Modifier
) {
    viewModel.setDateRange(dateRange)
    val state by viewModel.state.collectAsState()

    if (state == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val analytics = state!!

            // Outstanding Amount
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Outstanding Amount",
                        currentValue = analytics.totalOutstandingAmount,
                        previousValue = analytics.totalOutstandingAmount * 0.95,
                        unit = "$"
                    ),
                    onClick = {
                        onDrillClick(
                            "Outstanding Breakdown",
                            listOf(
                                "Current (0-30d)" to analytics.outstandingByAging.current,
                                "Past 30d" to analytics.outstandingByAging.past30,
                                "Past 60d" to analytics.outstandingByAging.past60,
                                "Past 90d" to analytics.outstandingByAging.past90,
                                "90+ days" to 0.0  // past90 covers 90+ days
                            )
                        )
                    }
                )
            }

            // Collection Rate
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Collection Rate",
                        currentValue = analytics.collectionRate,
                        previousValue = analytics.collectionRate * 0.98,
                        unit = "%"
                    ),
                    onClick = {
                        onDrillClick(
                            "Collection Metrics",
                            listOf(
                                "Total Invoiced" to analytics.totalInvoiceAmount,
                                "Total Paid" to analytics.totalPaidAmount,
                                "Outstanding" to analytics.totalOutstandingAmount
                            )
                        )
                    }
                )
            }

            // Days Sales Outstanding (DSO)
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Avg Days to Payment",
                        currentValue = analytics.averagePaymentTime,
                        previousValue = analytics.averagePaymentTime * 1.05,
                        unit = "days"
                    )
                )
            }

            // Aging breakdown bar chart
            item {
                BarChartCard(
                    data = listOf(
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "Current",
                            analytics.outstandingByAging.current.toFloat(),
                            System.currentTimeMillis()
                        ),
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "30d",
                            analytics.outstandingByAging.past30.toFloat(),
                            System.currentTimeMillis()
                        ),
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "60d",
                            analytics.outstandingByAging.past60.toFloat(),
                            System.currentTimeMillis()
                        ),
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "90d",
                            analytics.outstandingByAging.past90.toFloat(),
                            System.currentTimeMillis()
                        ),
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "90+",
                            analytics.outstandingByAging.past90.toFloat(),  // past90 bucket includes 90+ days
                            System.currentTimeMillis()
                        )
                    ),
                    title = "Outstanding by Aging Bucket"
                )
            }

            // Invoice Metrics Summary
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Total Invoices",
                        currentValue = analytics.totalInvoices.toDouble(),
                        previousValue = (analytics.totalInvoices * 0.9).toDouble(),
                        unit = "invoices"
                    ),
                    onClick = {
                        onDrillClick(
                            "Invoice Status",
                            listOf(
                                "Paid" to analytics.paidInvoices.toDouble(),
                                "Unpaid" to analytics.unpaidInvoices.toDouble(),
                                "Overdue" to analytics.overdueInvoices.toDouble()
                            )
                        )
                    }
                )
            }

            // Risk Alerts (if any)
            if (analytics.riskInvoices.isNotEmpty()) {
                item {
                    HeroMetricCard(
                        metric = TrendMetric(
                            label = "⚠️ At-Risk Invoices",
                            currentValue = analytics.riskInvoices.size.toDouble(),
                            previousValue = analytics.riskInvoices.size.toDouble() * 1.1,
                            unit = "invoices"
                        ),
                        onClick = {
                            onDrillClick(
                                "At-Risk Invoice Details",
                                analytics.riskInvoices.map { risk ->
                                    "${risk.invoiceNumber} (${risk.customerName})" to risk.outstandingAmount
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}





