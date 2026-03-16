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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.data.model.InvoiceVelocity
import kotlin.math.max

/**
 * Invoicing Velocity Card
 *
 * Shows how quickly invoices are converted from creation to sent.
 * Tracks productivity trend over time.
 *
 * Helps identify workflow bottlenecks and productivity changes.
 */
@Composable
fun InvoicingVelocityCard(
    velocityData: List<InvoiceVelocity>,
    modifier: Modifier = Modifier
) {
    val currentVelocity = velocityData.lastOrNull()?.avgDaysFromCreationToSent ?: 0.0
    val isSlowing = if (velocityData.size >= 2) {
        velocityData[velocityData.size - 1].avgDaysFromCreationToSent >
        velocityData[velocityData.size - 2].avgDaysFromCreationToSent
    } else {
        false
    }

    val velocityColor = when {
        currentVelocity <= 2.0 -> Color(0xFF388E3C)   // Green - excellent
        currentVelocity <= 5.0 -> Color(0xFF1976D2)   // Blue - good
        else -> Color(0xFFF57C00)                     // Orange - slow
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
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
                text = "Invoicing Velocity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Main metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("%.1f", currentVelocity),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = velocityColor,
                        fontSize = 44.sp
                    )
                    Text(
                        text = "days",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Trend indicator
                if (velocityData.isNotEmpty()) {
                    val currentMetric = velocityData.last()
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "📊 ${currentMetric.invoicesSentCount} sent today",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "📝 ${currentMetric.invoicesInDraftCount} in draft",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (currentMetric.invoicesInDraftCount > 0) Color(0xFFF57C00) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sparkline
            if (velocityData.isNotEmpty()) {
                VelocitySparkline(velocityData)
            }

            // Warning if slowing down
            if (isSlowing && currentVelocity > 5.0) {
                Text(
                    text = "⚠️ Invoicing speed slowing. Review workflow.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF57C00),
                    fontWeight = FontWeight.Bold
                )
            } else if (currentVelocity <= 2.0) {
                Text(
                    text = "✅ Fast invoicing! Keep it up.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "⏱️ Time from invoice creation to sending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Sparkline showing velocity trend.
 */
@Composable
private fun VelocitySparkline(data: List<InvoiceVelocity>) {
    if (data.isEmpty()) return

    val maxVelocity = max(data.maxByOrNull { it.avgDaysFromCreationToSent }?.avgDaysFromCreationToSent ?: 1.0, 1.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.takeLast(14).forEach { metric ->  // Last 14 days
            val heightFraction = (metric.avgDaysFromCreationToSent / maxVelocity).toFloat()
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
}

