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
import kotlin.math.max

/**
 * Cash Flow Trend Chart
 *
 * Shows 30-day invoiced vs. paid trends.
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

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem("Invoiced", Color(0xFF1976D2))
            LegendItem("Paid", Color(0xFF388E3C))
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
            val gap = totalInvoiced - totalPaid

            Column(modifier = Modifier.padding(top = 12.dp)) {
                SummaryRow("Total Invoiced", totalInvoiced)
                SummaryRow("Total Paid", totalPaid)
                SummaryRow("Gap", gap, textColor = if (gap > 0) Color(0xFFD32F2F) else Color(0xFF388E3C))
            }
        }

        // Info text
        Text(
            text = "💡 Tip: Compare invoiced vs. paid to identify cash flow gaps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun SimpleBarChart(trends: List<CashFlowTrendPoint>) {
    val maxValue = max(
        trends.maxOfOrNull { it.invoicedCents } ?: 0L,
        trends.maxOfOrNull { it.paidCents } ?: 0L
    )

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
            text = "Last 30 days: Invoiced vs Paid",
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
                    // Invoiced bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(((trend.invoicedCents.toFloat() / maxValue) * 150).dp)
                            .background(Color(0xFF1976D2), shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Paid bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(((trend.paidCents.toFloat() / maxValue) * 150).dp)
                            .background(Color(0xFF388E3C), shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    )
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

