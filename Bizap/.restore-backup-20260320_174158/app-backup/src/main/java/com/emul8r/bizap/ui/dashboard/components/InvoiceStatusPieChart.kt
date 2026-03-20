package com.emul8r.bizap.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas

/**
 * Simple pie chart component showing invoice status breakdown.
 * Displays counts of PAID, SENT, PARTIALLY_PAID, OVERDUE, and DRAFT invoices.
 */
@Composable
fun InvoiceStatusPieChart(
    statusCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    if (statusCounts.isEmpty() || statusCounts.values.sum() == 0) {
        // Show empty state
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("No invoices yet", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val total = statusCounts.values.sum()
    val colors = mapOf(
        "PAID" to Color(0xFF4CAF50),          // Green
        "PARTIALLY_PAID" to Color(0xFFFFC107), // Amber
        "SENT" to Color(0xFF2196F3),          // Blue
        "OVERDUE" to Color(0xFFF44336),       // Red
        "DRAFT" to Color(0xFF9E9E9E)          // Gray
    )

    Column(modifier = modifier) {
        // Pie chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(200.dp)
            ) {
                drawPieChart(statusCounts, total, colors)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statusCounts.forEach { (status, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = colors[status] ?: Color.Gray,
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "$count (${(count * 100 / total)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawPieChart(
    statusCounts: Map<String, Int>,
    total: Int,
    colors: Map<String, Color>
) {
    val radius = size.minDimension / 2
    val center = Offset(size.width / 2, size.height / 2)
    var startAngle = -90f

    statusCounts.forEach { (status, count) ->
        val sweepAngle = (count.toFloat() / total) * 360f
        val color = colors[status] ?: Color.Gray

        // Draw pie slice
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            topLeft = center - Offset(radius, radius)
        )

        // Draw border
        drawArc(
            color = Color.White,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            topLeft = center - Offset(radius, radius),
            style = Stroke(width = 2f)
        )

        startAngle += sweepAngle
    }
}


