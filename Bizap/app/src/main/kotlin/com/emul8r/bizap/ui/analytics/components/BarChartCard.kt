package com.emul8r.bizap.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.analytics.ChartDataPoint
import timber.log.Timber

/**
 * Bar chart card using simple bar visualization.
 *
 * Renders a simple bar chart with visual bars.
 * Falls back to a placeholder when [data] is empty.
 *
 * @param data List of chart data points
 * @param title Display title
 * @param modifier Optional modifier
 */
@Composable
fun BarChartCard(
    data: List<ChartDataPoint>,
    title: String,
    modifier: Modifier = Modifier
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
                fontWeight = FontWeight.SemiBold
            )

            if (data.isNotEmpty()) {
                SimpleBarChart(data = data, modifier = Modifier.fillMaxWidth().height(160.dp))
                Text(
                    "${data.size} categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
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
 * Simple bar chart implementation using Compose primitives.
 */
@Composable
private fun SimpleBarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    // Find max value for scaling
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val normalizedMax = if (maxValue > 0) maxValue else 1f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Display bars
        data.take(5).forEachIndexed { index, point ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Label
                Text(
                    text = point.label.take(3),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(30.dp)
                )

                // Bar
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth((point.value / normalizedMax).coerceIn(0f, 1f))
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                // Value
                Text(
                    text = String.format("%.0f", point.value),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}
