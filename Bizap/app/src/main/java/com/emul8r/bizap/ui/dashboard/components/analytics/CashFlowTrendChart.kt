package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
 * Shows 30-day invoiced vs. paid trends.
 * Helps users identify seasonal patterns and predict cash needs.
 *
 * Implemented with Compose Canvas to avoid Vico library version constraints.
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
            val invoicedValues = dailyTrends.map { it.invoicedCents.toFloat() }
            val paidValues = dailyTrends.map { it.paidCents.toFloat() }
            val maxValue = (invoicedValues + paidValues).maxOrNull()?.takeIf { it > 0f } ?: 1f

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val w = size.width
                val h = size.height
                val count = dailyTrends.size
                val step = if (count > 1) w / (count - 1) else w

                // Draw grid lines
                for (i in 0..4) {
                    val y = h * i / 4f
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // Draw invoiced line
                if (count > 1) {
                    val invoicedPath = Path().apply {
                        invoicedValues.forEachIndexed { i, v ->
                            val x = i * step
                            val y = h - (v / maxValue) * h
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = invoicedPath,
                        color = invoicedColor,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }

                // Draw paid line
                if (count > 1) {
                    val paidPath = Path().apply {
                        paidValues.forEachIndexed { i, v ->
                            val x = i * step
                            val y = h - (v / maxValue) * h
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = paidPath,
                        color = paidColor,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }
            }
        } else {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp)
            )
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
                .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

