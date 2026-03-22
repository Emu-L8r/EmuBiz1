package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.model.CashFlowTrendPoint
import com.emul8r.bizap.ui.designsystem.BizapColors

/**
 * Cash Flow Trend Chart
 *
 * Shows 30-day sent (outstanding) vs. paid trends.
 * Helps users identify seasonal patterns and predict cash flow gaps.
 *
 * Note: Implemented with basic Compose components for reliability.
 * Charts with complex data visualization can be added in Phase 2.
 */
@Composable
fun CashFlowTrendChart(
    dailyTrends: List<CashFlowTrendPoint>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Cash Flow Trend (30 Days)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Legend with SENT and PAID clearly distinguished
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem("Paid (Collected)", BizapColors.StatusPaid)
            LegendItem("Outstanding", BizapColors.StatusSent)
        }

        // Simple bar visualization
        if (dailyTrends.isNotEmpty()) {
            SimpleBarChart(dailyTrends)
        } else {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp)
            )
        }

        // Summary stats
        if (dailyTrends.isNotEmpty()) {
            val totalInvoiced = dailyTrends.sumOf { it.invoicedCents } / 100.0
            val totalPaid = dailyTrends.sumOf { it.paidCents } / 100.0
            val outstanding = totalInvoiced - totalPaid

            Column(modifier = Modifier.padding(top = 12.dp)) {
                SummaryRow("Total Sent (Outstanding)", totalInvoiced)
                SummaryRow("Total Paid (Collected)", totalPaid)
                SummaryRow("Outstanding Gap", outstanding, textColor = if (outstanding > 0) BizapColors.StatusOverdue else BizapColors.StatusPaid)
            }
        }

        // Info text
        Text(
            text = "💡 Tip: Green portions show collected revenue. Blue shows invoices awaiting payment.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun SimpleBarChart(trends: List<CashFlowTrendPoint>) {
    val maxValue = trends.maxOfOrNull { it.invoicedCents } ?: 0L

    if (maxValue == 0L) {
        Text(
            text = "No data to display",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "Last 7 days: Paid (Green) vs Outstanding (Blue)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Show last 7 days for clarity
        val displayTrends = trends.takeLast(7)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            displayTrends.forEach { trend ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // STACKED BAR: Total height represents invoiced amount
                    val totalHeight = ((trend.invoicedCents.toFloat() / maxValue) * 150).dp
                    val paidCents = trend.paidCents

                    // Calculate proportions for stacked bar
                    val paidProportion = if (trend.invoicedCents > 0)
                        paidCents.toFloat() / trend.invoicedCents
                    else
                        0f
                    val outstandingProportion = 1f - paidProportion

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(totalHeight)
                    ) {
                        // PAID (Green) - Bottom portion
                        if (paidProportion > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(paidProportion)
                                    .background(
                                        BizapColors.StatusPaid,  // Green - Collected
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            bottomStart = 2.dp,
                                            bottomEnd = 2.dp,
                                            topStart = if (outstandingProportion > 0f) 0.dp else 2.dp,
                                            topEnd = if (outstandingProportion > 0f) 0.dp else 2.dp
                                        )
                                    )
                                    .align(Alignment.BottomStart)
                            )
                        }

                        // OUTSTANDING (Blue) - Top portion
                        if (outstandingProportion > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(outstandingProportion)
                                    .background(
                                        BizapColors.StatusSent,  // Blue - Outstanding
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            topStart = 2.dp,
                                            topEnd = 2.dp,
                                            bottomStart = if (paidProportion > 0f) 0.dp else 2.dp,
                                            bottomEnd = if (paidProportion > 0f) 0.dp else 2.dp
                                        )
                                    )
                                    .align(Alignment.TopStart)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: Double, textColor: Color = Color.Unspecified) {
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
            text = String.format("$%.2f", amount),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (textColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else textColor
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        modifier = Modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

