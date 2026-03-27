package com.emul8r.bizap.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import com.emul8r.bizap.ui.analytics.components.BarChartCard
import com.emul8r.bizap.ui.analytics.components.HeroMetricCard
import com.emul8r.bizap.ui.designsystem.BizapColors

/**
 * Risk Analytics Tab content.
 *
 * Displays comprehensive business risk metrics:
 * - At-Risk Customer Count
 * - Overdue Amount
 * - Collection Rate Trend
 * - Aging Bucket Breakdown (visual)
 * - Risk Score calculation
 * - Collection Effectiveness
 *
 * @param viewModel PaymentAnalyticsTabViewModel providing payment/risk data
 * @param dateRange Selected date range from parent
 * @param onDrillClick Callback to open drill-down bottom sheet
 * @param modifier Optional modifier
 */
@Composable
fun RiskAnalyticsTab(
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

            // Risk Score (calculated from overdue %age)
            item {
                val riskScore = calculateRiskScore(
                    overdueAmount = analytics.outstandingByAging.past90,
                    totalOutstanding = analytics.totalOutstandingAmount
                )
                RiskScoreCard(
                    score = riskScore,
                    onClick = {
                        onDrillClick(
                            "Risk Assessment",
                            listOf(
                                "Risk Score" to riskScore,
                                "Overdue Amount" to analytics.outstandingByAging.past90,
                                "Total Outstanding" to analytics.totalOutstandingAmount
                            )
                        )
                    }
                )
            }

            // At-Risk Invoice Count
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "⚠️ At-Risk Invoices",
                        currentValue = analytics.riskInvoices.size.toDouble(),
                        previousValue = analytics.riskInvoices.size.toDouble() * 0.95,
                        unit = "invoices"
                    ),
                    onClick = {
                        onDrillClick(
                            "At-Risk Invoice Details",
                            analytics.riskInvoices.take(10).map { risk ->
                                "${risk.invoiceNumber} - ${risk.customerName}" to risk.outstandingAmount
                            }
                        )
                    }
                )
            }

            // Overdue Amount (90+ days)
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "🚨 Seriously Overdue (90+)",
                        currentValue = analytics.outstandingByAging.past90,
                        previousValue = analytics.outstandingByAging.past90 * 0.95,
                        unit = "$"
                    ),
                    onClick = {
                        onDrillClick(
                            "90+ Days Overdue",
                            listOf(
                                "Amount" to analytics.outstandingByAging.past90,
                                "Invoice Count" to analytics.overdueInvoices.toDouble()
                            )
                        )
                    }
                )
            }

            // Collection Rate (with risk indicator)
            item {
                val isCollectionRiskLevel = analytics.collectionRate < 75.0
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Collection Rate",
                        currentValue = analytics.collectionRate,
                        previousValue = analytics.collectionRate * 0.98,
                        unit = "%"
                    ),
                    onClick = {
                        onDrillClick(
                            "Collection Performance",
                            listOf(
                                "Collection Rate" to analytics.collectionRate,
                                "Paid" to analytics.totalPaidAmount,
                                "Outstanding" to analytics.totalOutstandingAmount
                            )
                        )
                    }
                )
            }

            // Aging Breakdown Bar Chart
            item {
                BarChartCard(
                    data = listOf(
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "0-30d",
                            analytics.outstandingByAging.current.toFloat(),
                            System.currentTimeMillis()
                        ),
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "31-60d",
                            analytics.outstandingByAging.past30.toFloat(),
                            System.currentTimeMillis()
                        ),
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "61-90d",
                            analytics.outstandingByAging.past60.toFloat(),
                            System.currentTimeMillis()
                        ),
                        com.emul8r.bizap.domain.analytics.ChartDataPoint(
                            "90+ days",
                            analytics.outstandingByAging.past90.toFloat(),
                            System.currentTimeMillis()
                        )
                    ),
                    title = "📊 Outstanding by Aging Bucket"
                )
            }

            // Collection Effectiveness Metric
            item {
                val collectionEffectiveness = calculateCollectionEffectiveness(
                    paidAmount = analytics.totalPaidAmount,
                    issuedAmount = analytics.totalInvoiceAmount
                )
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Collection Effectiveness",
                        currentValue = collectionEffectiveness,
                        previousValue = collectionEffectiveness * 0.98,
                        unit = "%"
                    ),
                    onClick = {
                        onDrillClick(
                            "Collection Effectiveness",
                            listOf(
                                "Issued Amount" to analytics.totalInvoiceAmount,
                                "Paid Amount" to analytics.totalPaidAmount,
                                "Effectiveness" to collectionEffectiveness
                            )
                        )
                    }
                )
            }

            // Days Sales Outstanding (DSO)
            item {
                HeroMetricCard(
                    metric = TrendMetric(
                        label = "Days Sales Outstanding",
                        currentValue = analytics.averagePaymentTime,
                        previousValue = analytics.averagePaymentTime * 1.05,
                        unit = "days"
                    ),
                    onClick = {
                        onDrillClick(
                            "Payment Timeline",
                            listOf(
                                "Average Days" to analytics.averagePaymentTime,
                                "Total Outstanding" to analytics.totalOutstandingAmount
                            )
                        )
                    }
                )
            }

            // Risk Summary Card
            item {
                RiskSummaryCard(analytics = analytics)
            }
        }
    }
}

/**
 * Calculates risk score from 0-100 based on overdue percentage.
 * - 0-10%: Low risk (Green)
 * - 10-20%: Medium risk (Yellow)
 * - 20%+: High risk (Red)
 */
private fun calculateRiskScore(overdueAmount: Double, totalOutstanding: Double): Double {
    if (totalOutstanding <= 0.0) return 0.0
    val overduePercentage = (overdueAmount / totalOutstanding) * 100.0
    return minOf(overduePercentage * 1.2, 100.0) // Cap at 100
}

/**
 * Calculates collection effectiveness as percentage of issued amount collected.
 */
private fun calculateCollectionEffectiveness(paidAmount: Double, issuedAmount: Double): Double {
    if (issuedAmount <= 0.0) return 0.0
    return (paidAmount / issuedAmount) * 100.0
}

/**
 * Risk Score visual indicator card with color coding.
 */
@Composable
private fun RiskScoreCard(
    score: Double,
    onClick: () -> Unit = {}
) {
    val (color, label) = when {
        score < 10.0 -> BizapColors.AnalyticsExcellent to "Low Risk"
        score < 20.0 -> BizapColors.AnalyticsWarning to "Medium Risk"
        else -> BizapColors.AnalyticsAtRisk to "High Risk"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Business Risk Score",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = String.format("%.1f", score),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                shape = RoundedCornerShape(3.dp),
                color = color.copy(alpha = 0.3f)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (score.toFloat() / 100f).coerceIn(0f, 1f))
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(3.dp),
                    color = color
                ) {}
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

/**
 * Summary card showing key risk indicators.
 */
@Composable
private fun RiskSummaryCard(
    analytics: com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = BizapColors.AnalyticsAtRisk
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Risk Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider(modifier = Modifier.fillMaxWidth())

            // Summary rows
            RiskSummaryRow(
                label = "Total Outstanding",
                value = "$${String.format("%.0f", analytics.totalOutstandingAmount)}"
            )

            RiskSummaryRow(
                label = "Overdue Invoices",
                value = "${analytics.overdueInvoices} invoices"
            )

            RiskSummaryRow(
                label = "At-Risk Invoices",
                value = "${analytics.riskInvoices.size} invoices"
            )

            RiskSummaryRow(
                label = "Average Days Outstanding",
                value = "${String.format("%.0f", analytics.averagePaymentTime)} days"
            )

            RiskSummaryRow(
                label = "Collection Rate",
                value = "${String.format("%.1f", analytics.collectionRate)}%"
            )
        }
    }
}

/**
 * Single row in risk summary showing label and value.
 */
@Composable
private fun RiskSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


