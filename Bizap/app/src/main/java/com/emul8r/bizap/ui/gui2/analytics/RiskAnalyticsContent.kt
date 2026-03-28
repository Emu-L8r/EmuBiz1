package com.emul8r.bizap.ui.gui2.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.gui2.RiskMetricsV2
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.ui.gui2.common.MetricCardV2
import com.emul8r.bizap.ui.gui2.common.SectionHeaderV2
import java.text.NumberFormat
import java.util.*

@Composable
fun RiskAnalyticsContent(
    metrics: RiskMetricsV2,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Risk Hero Card
        SectionHeaderV2("Risk Assessment")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "At-Risk Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            (metrics.highRiskCount + metrics.atRiskCount).toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Text(
                    "${metrics.highRiskCount} high-risk + ${metrics.atRiskCount} at-risk invoices",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Risk Categories
        SectionHeaderV2("Risk Breakdown")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskCard(
                title = "High Risk",
                count = metrics.highRiskCount,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            )
            RiskCard(
                title = "At-Risk",
                count = metrics.atRiskCount,
                color = Color(0xFFFBC02D),
                modifier = Modifier.weight(1f)
            )
            RiskCard(
                title = "Healthy",
                count = metrics.healthyCount,
                color = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            )
        }

        // Overdue Details
        SectionHeaderV2("Overdue Status")
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverdueItem(
                label = "High Risk (60+ days)",
                count = metrics.highRiskCount,
                isHighRisk = true
            )
            OverdueItem(
                label = "At-Risk (30-59 days)",
                count = metrics.atRiskCount,
                isHighRisk = false
            )
            OverdueItem(
                label = "Healthy / On-time",
                count = metrics.healthyCount,
                isHighRisk = false
            )
        }

        // Health Metrics
        SectionHeaderV2("Collection Efficiency")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCardV2(
                label = "Total Outstanding",
                value = formatCurrency(metrics.totalOutstandingCents),
                modifier = Modifier.weight(1f)
            )
            MetricCardV2(
                label = "At-Risk Count",
                value = (metrics.highRiskCount + metrics.atRiskCount).toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun RiskCard(
    title: String,
    count: Int,
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
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Surface(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp),
                color = color,
                shape = MaterialTheme.shapes.small
            ) {}
        }
    }
}

@Composable
private fun OverdueItem(
    label: String,
    count: Int,
    isHighRisk: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
        Surface(
            color = if (isHighRisk) Color(0xFFD32F2F).copy(alpha = 0.1f) else Color(0xFFFBC02D).copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isHighRisk) Color(0xFFD32F2F) else Color(0xFFFBC02D),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun formatCurrency(cents: Long): String {
    val amount = cents / 100.0
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}



