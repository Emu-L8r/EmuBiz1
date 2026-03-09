package com.emul8r.bizap.ui.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val statusColors = mapOf(
    "PAID" to Color(0xFF4CAF50),
    "SENT" to Color(0xFF2196F3),
    "DRAFT" to Color(0xFF9E9E9E),
    "OVERDUE" to Color(0xFFF44336),
    "PARTIALLY_PAID" to Color(0xFFFFC107),
    "CANCELLED" to Color(0xFF795548)
)

@Composable
fun InvoiceStatusPieChart(
    statusCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    if (statusCounts.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "No invoice data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val total = statusCounts.values.sum().toFloat()
    val entries = statusCounts.entries.toList()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var startAngle = -90f
            entries.forEach { (status, count) ->
                val sweep = (count / total) * 360f
                val color = statusColors[status] ?: Color(0xFF607D8B)
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                    size = Size(size.width * 0.9f, size.height * 0.9f)
                )
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                    size = Size(size.width * 0.9f, size.height * 0.9f),
                    style = Stroke(width = 2f)
                )
                startAngle += sweep
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            entries.forEach { (status, count) ->
                val color = statusColors[status] ?: Color(0xFF607D8B)
                val pct = if (total > 0f) (count / total * 100).toInt() else 0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = color)
                    }
                    Text(
                        text = status.replace('_', ' ').lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$count ($pct%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
