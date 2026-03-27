package com.emul8r.bizap.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.analytics.TrendDirection
import com.emul8r.bizap.domain.analytics.TrendMetric

/**
 * Large metric card with trend indicator badge.
 *
 * Shows current value + % change vs previous period with visual up/down indicator.
 * Used as hero metrics at the top of each analytics tab.
 *
 * @param metric The TrendMetric containing value and delta data
 * @param onClick Callback when card is tapped
 * @param modifier Optional modifier for styling
 */
@Composable
fun HeroMetricCard(
    metric: TrendMetric,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val trendColor = when (metric.trendDirection) {
        TrendDirection.UP -> Color(0xFF4CAF50)     // Green
        TrendDirection.DOWN -> Color(0xFFF44336)   // Red
        TrendDirection.NEUTRAL -> Color(0xFF9E9E9E) // Gray
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Label + Trend badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Trend badge with icon and percentage
                Row(
                    modifier = Modifier
                        .background(
                            color = trendColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (metric.trendDirection == TrendDirection.UP) {
                            Icons.AutoMirrored.Filled.TrendingUp
                        } else {
                            Icons.AutoMirrored.Filled.TrendingDown
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = trendColor
                    )
                    Text(
                        text = "${String.format("%.1f", kotlin.math.abs(metric.deltaPercent))}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Large metric value (formatted with unit if present)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.2f", metric.currentValue),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (metric.unit.isNotEmpty()) {
                    Text(
                        text = metric.unit,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Subtext: comparison info
            Text(
                text = "vs previous period: ${String.format("%.2f", metric.deltaAbsolute)} ${metric.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


