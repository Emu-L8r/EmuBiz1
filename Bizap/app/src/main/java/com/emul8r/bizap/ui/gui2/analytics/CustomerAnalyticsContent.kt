package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.gui2.CustomerMetricsV2
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.ui.gui2.common.MetricCardV2
import com.emul8r.bizap.ui.gui2.common.SectionHeaderV2
import java.text.NumberFormat
import java.util.*

@Composable
fun CustomerAnalyticsContent(
    metrics: CustomerMetricsV2,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total Customers Hero Card
        SectionHeaderV2("Customer Overview")
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Total Customers",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    metrics.totalCustomers.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Avg LTV: ${formatCurrency(metrics.averageLTV)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Churn: ${"%.1f".format(metrics.churnRate)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Customer Segments
        SectionHeaderV2("Customer Segments")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SegmentCard(
                emoji = "⭐",
                label = "VIP",
                count = metrics.vipCount,
                percentage = metrics.vipPercentage,
                color = BizapColors.AnalyticsExcellent,
                modifier = Modifier.weight(1f)
            )
            SegmentCard(
                emoji = "✓",
                label = "Regular",
                count = metrics.regularCount,
                percentage = metrics.regularPercentage,
                color = BizapColors.AnalyticsGood,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SegmentCard(
                emoji = "⚠️",
                label = "At-Risk",
                count = metrics.atRiskCount,
                percentage = metrics.atRiskPercentage,
                color = BizapColors.AnalyticsWarning,
                modifier = Modifier.weight(1f)
            )
            SegmentCard(
                emoji = "💤",
                label = "Dormant",
                count = metrics.dormantCount,
                percentage = metrics.dormantPercentage,
                color = BizapColors.AnalyticsAtRisk,
                modifier = Modifier.weight(1f)
            )
        }

        // Segment Distribution
        SectionHeaderV2("Distribution")
        if (metrics.totalCustomers > 0) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SegmentBar(
                    label = "VIP",
                    count = metrics.vipCount,
                    percentage = metrics.vipPercentage,
                    color = BizapColors.AnalyticsExcellent
                )
                SegmentBar(
                    label = "Regular",
                    count = metrics.regularCount,
                    percentage = metrics.regularPercentage,
                    color = BizapColors.AnalyticsGood
                )
                SegmentBar(
                    label = "At-Risk",
                    count = metrics.atRiskCount,
                    percentage = metrics.atRiskPercentage,
                    color = BizapColors.AnalyticsWarning
                )
                SegmentBar(
                    label = "Dormant",
                    count = metrics.dormantCount,
                    percentage = metrics.dormantPercentage,
                    color = BizapColors.AnalyticsAtRisk
                )
            }
        } else {
            Text(
                "No customer data available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SegmentCard(
    emoji: String,
    label: String,
    count: Int,
    percentage: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "${"%.0f".format(percentage)}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SegmentBar(
    label: String,
    count: Int,
    percentage: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(
                "$count (${String.format("%.0f", percentage)}%)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            shape = MaterialTheme.shapes.small,
            color = Color.LightGray.copy(alpha = 0.3f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (percentage / 100).coerceIn(0.0, 1.0).toFloat()),
                color = color,
                shape = MaterialTheme.shapes.small
            ) {}
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}

