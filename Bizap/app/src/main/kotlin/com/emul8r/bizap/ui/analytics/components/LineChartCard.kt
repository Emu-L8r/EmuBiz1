package com.emul8r.bizap.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.analytics.ChartDataPoint
import timber.log.Timber

/**
 * Line chart card - Simple placeholder UI.
 *
 * TODO: Integrate Vico CartesianChartHost when dependency is verified.
 * Currently displays a gradient bar visualization as placeholder.
 *
 * @param data List of chart data points
 * @param title Display title for the chart
 * @param modifier Optional modifier
 * @param onDataPointClick Callback for future drill-down
 */
@Composable
fun LineChartCard(
    data: List<ChartDataPoint>,
    title: String,
    modifier: Modifier = Modifier,
    onDataPointClick: (ChartDataPoint) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp, max = 320.dp),
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )

            if (data.isNotEmpty()) {
                // Enhanced visualization with real data
                var hoveredIndex by remember { mutableStateOf(-1) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.Bottom
                ) {
                    val maxValue = data.maxOfOrNull { it.value } ?: 1f
                    data.forEachIndexed { index, point ->
                        val heightPercent = (point.value / maxValue).coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(heightPercent)
                                .background(
                                    color = if (index == hoveredIndex)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                                .clickable {
                                    hoveredIndex = index
                                    onDataPointClick(point)
                                }
                                .padding(top = 4.dp)
                        )
                    }
                }

                // Data point labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    data.take(5).forEach { point ->
                        Text(
                            text = point.label.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Summary
                Text(
                    "${data.size} data points | Max: ${data.maxOfOrNull { it.value }?.toInt() ?: 0}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        "No data available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}




