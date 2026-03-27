package com.emul8r.bizap.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.designsystem.BizapColors
import timber.log.Timber

/**
 * Pie/Donut chart card using simple Compose drawing.
 *
 * Renders categorical distribution data (e.g., customer segments, payment status breakdown)
 * as a visual pie chart with legend. Uses Material 3 styling.
 *
 * Note: This is a simplified implementation using Canvas and Compose primitives.
 * For more complex charts, consider integrating Vico's pie/donut module.
 *
 * @param data List of (label, value) pairs
 * @param title Display title for the chart
 * @param modifier Optional modifier for styling
 */
@Composable
fun PieChartCard(
    data: List<Pair<String, Double>>,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 240.dp, max = 340.dp),
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
                PieChartContent(data = data)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
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

/**
 * Internal composable that renders the pie chart content and legend.
 */
@Composable
private fun PieChartContent(data: List<Pair<String, Double>>) {
    val total = data.sumOf { it.second }

    if (total <= 0.0) {
        Text("No data to display", style = MaterialTheme.typography.labelSmall)
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Legend with percentages
        data.forEachIndexed { index, (label, value) ->
            val percentage = (value / total) * 100.0
            val color = getPieChartColor(index)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = color,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    "${String.format("%.1f", percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = color
                )
            }
        }
    }

    Timber.d("PieChartCard: Rendered pie chart with ${data.size} segments, total value: $total")
}

/**
 * Get distinct colors for pie chart segments.
 * Cycles through a predefined palette for different indices.
 */
@Composable
private fun getPieChartColor(index: Int): androidx.compose.ui.graphics.Color {
    return when (index % 8) {
        0 -> BizapColors.StatusPaid        // Green
        1 -> BizapColors.StatusSent        // Blue
        2 -> BizapColors.StatusDraft       // Gray
        3 -> BizapColors.Presets.Orange
        4 -> BizapColors.Presets.Purple
        5 -> BizapColors.Presets.Pink
        6 -> BizapColors.Presets.Teal
        else -> BizapColors.Presets.Indigo
    }
}



