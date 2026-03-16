package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.model.CashFlowTrendPoint

/**
 * Cash Flow Trend Chart
 *
 * Shows 30-day invoiced vs. paid trends using a Canvas-based line chart.
 * Helps users identify seasonal patterns and predict cash needs.
 */
@Composable
fun CashFlowTrendChart(
    dailyTrends: List<CashFlowTrendPoint>,
    modifier: Modifier = Modifier
) {
    val invoicedColor = Color(0xFF1976D2)
    val paidColor = Color(0xFF388E3C)

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
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem("Invoiced", invoicedColor)
            LegendItem("Paid", paidColor)
        }

        // Chart
        if (dailyTrends.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                val chartPadding = 40.dp.toPx()
                val chartWidth = size.width - (2 * chartPadding)
                val chartHeight = size.height - (2 * chartPadding)

                val maxValue = maxOf(
                    dailyTrends.maxOfOrNull { it.invoicedCents } ?: 1L,
                    dailyTrends.maxOfOrNull { it.paidCents } ?: 1L
                ).coerceAtLeast(1L).toFloat()

                // Draw axes
                drawLine(
                    color = Color.Gray,
                    start = Offset(chartPadding, chartPadding),
                    end = Offset(chartPadding, chartHeight + chartPadding),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.Gray,
                    start = Offset(chartPadding, chartHeight + chartPadding),
                    end = Offset(chartPadding + chartWidth, chartHeight + chartPadding),
                    strokeWidth = 2f
                )

                val n = dailyTrends.size

                fun xFor(index: Int) =
                    chartPadding + (index.toFloat() / (n - 1).coerceAtLeast(1)) * chartWidth

                fun yFor(valueCents: Long) =
                    chartHeight + chartPadding - (valueCents.toFloat() / maxValue) * chartHeight

                // Draw invoiced line
                if (n > 1) {
                    val invoicedPath = Path()
                    dailyTrends.forEachIndexed { i, point ->
                        val x = xFor(i)
                        val y = yFor(point.invoicedCents)
                        if (i == 0) invoicedPath.moveTo(x, y) else invoicedPath.lineTo(x, y)
                    }
                    drawPath(
                        path = invoicedPath,
                        color = invoicedColor,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // Draw paid line
                    val paidPath = Path()
                    dailyTrends.forEachIndexed { i, point ->
                        val x = xFor(i)
                        val y = yFor(point.paidCents)
                        if (i == 0) paidPath.moveTo(x, y) else paidPath.lineTo(x, y)
                    }
                    drawPath(
                        path = paidPath,
                        color = paidColor,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }

                // Draw data points
                dailyTrends.forEachIndexed { i, point ->
                    val x = xFor(i)
                    drawCircle(color = invoicedColor, radius = 4f, center = Offset(x, yFor(point.invoicedCents)))
                    drawCircle(color = paidColor, radius = 4f, center = Offset(x, yFor(point.paidCents)))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
private fun LegendItem(label: String, color: Color) {
    Row(
        modifier = Modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

