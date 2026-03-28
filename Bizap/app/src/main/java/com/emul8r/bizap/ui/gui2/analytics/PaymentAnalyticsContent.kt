package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.gui2.PaymentMetricsV2
import com.emul8r.bizap.ui.gui2.common.MetricCardV2
import com.emul8r.bizap.ui.gui2.common.SectionHeaderV2
import java.text.NumberFormat
import java.util.*

@Composable
fun PaymentAnalyticsContent(
    metrics: PaymentMetricsV2,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Outstanding Amount Hero Card
        SectionHeaderV2("Outstanding Balance")
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Total Outstanding",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    formatCurrency(metrics.outstandingAmount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "${metrics.overdueCount} overdue invoices",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Collection Rate
        SectionHeaderV2("Collection Health")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Collection Rate",
                value = "${"%.1f".format(metrics.collectionRate)}%",
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Paid Invoices",
                value = metrics.paidCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Invoice Status Breakdown
        SectionHeaderV2("Status Breakdown")
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            metrics.statusBreakdown.forEach { status ->
                StatusBucket(
                    label = status.status,
                    count = status.count,
                    percentage = calculatePercentage(status.count.toDouble(), metrics.totalInvoices.toDouble())
                )
            }
        }

        // Payment Summary
        SectionHeaderV2("Summary")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Sent",
                value = metrics.sentCount.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Partially Paid",
                value = metrics.partiallyPaidCount.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "Overdue",
                value = metrics.overdueCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatusBucket(
    label: String,
    count: Int,
    percentage: Double,
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

        val barColor = when {
            label.contains("PAID", ignoreCase = true) -> Color(0xFF388E3C)  // Green
            label.contains("OVERDUE", ignoreCase = true) -> Color(0xFFD32F2F)  // Red
            label.contains("DRAFT", ignoreCase = true) -> Color(0xFF9E9E9E)  // Gray
            else -> Color(0xFF1976D2)  // Blue
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            shape = MaterialTheme.shapes.small,
            color = Color.LightGray.copy(alpha = 0.3f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (percentage / 100).coerceIn(0.0, 1.0).toFloat()),
                color = barColor,
                shape = MaterialTheme.shapes.small
            ) {}
        }
    }
}

private fun calculatePercentage(value: Double, total: Double): Double {
    return if (total > 0) (value / total * 100) else 0.0
}

private fun formatCurrency(cents: Long): String {
    return formatCurrency(cents / 100.0)
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}



