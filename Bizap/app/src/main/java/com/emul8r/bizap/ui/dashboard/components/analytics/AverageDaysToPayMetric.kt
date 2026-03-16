package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.data.model.DaysToPayMetric
import kotlin.math.max

/**
 * Average Days to Payment Metric
 *
 * Shows how long invoices take to get paid on average (DSO).
 * Includes a sparkline showing trend over 12 months.
 *
 * Color coding:
 * - Green: < 15 days (excellent)
 * - Yellow: 15-25 days (normal)
 * - Red: > 25 days (problem)
 */
@Composable
fun AverageDaysToPayMetric(
    currentDaysToPayment: Double,
    trendHistory: List<DaysToPayMetric>,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        currentDaysToPayment < 15.0 -> Color(0xFF388E3C)  // Green
        currentDaysToPayment < 25.0 -> Color(0xFFF57C00)  // Yellow/Orange
        else -> Color(0xFFD32F2F)  // Red
    }

    val statusText = when {
        currentDaysToPayment < 15.0 -> "Excellent"
        currentDaysToPayment < 25.0 -> "Normal"
        else -> "Needs Attention"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Text(
                text = "Average Days to Payment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Main number with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("%.1f", currentDaysToPayment),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        fontSize = 48.sp
                    )
                    Text(
                        text = "days",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Sparkline
            if (trendHistory.isNotEmpty()) {
                DaysToPaySparkline(trendHistory)
            }

            // Help text
            Text(
                text = "⏱️ Days from invoice sent to payment received",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Simple sparkline showing DSO trend.
 * Uses miniature bars for easy visualization.
 */
@Composable
private fun DaysToPaySparkline(data: List<DaysToPayMetric>) {
    if (data.isEmpty()) return

    val maxDays = max(data.maxByOrNull { it.averageDaysToPayment }?.averageDaysToPayment ?: 1.0, 1.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.takeLast(12).forEach { metric ->  // Last 12 months
            val heightFraction = (metric.averageDaysToPayment / maxDays).toFloat()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .background(
                        color = Color(0xFF1976D2).copy(alpha = 0.6f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                    )
            )
        }
    }

    Text(
        text = "Last 12 months →",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

