package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.model.TopCustomerMetric
import com.emul8r.bizap.ui.designsystem.BizapColors

/**
 * Revenue Concentration Chart
 *
 * Shows top 5 customers by revenue with percentage breakdown.
 * Highlights business risk if one customer represents >60% of revenue.
 *
 * Color coding:
 * - Green gradient: Well-distributed revenue
 * - Red: High concentration risk
 */
@Composable
fun RevenueConcentrationChart(
    topCustomers: List<TopCustomerMetric>,
    modifier: Modifier = Modifier,
    onCustomerClick: (Long) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Revenue Concentration (Top 5)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (topCustomers.isEmpty()) {
            Text(
                text = "No customer data available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(32.dp)
            )
        } else {
            // Risk warning if concentration is high
            val maxPercentage = topCustomers.maxOfOrNull { it.percentageOfTotal } ?: 0.0
            if (maxPercentage > 60.0) {
                RiskWarningBanner("High concentration risk: 1 customer = ${String.format("%.0f", maxPercentage)}% of revenue")
            }

            // Bars for each customer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                topCustomers.forEach { customer ->
                    CustomerRevenueBar(customer, onCustomerClick)
                }
            }

            // Legend/Info
            Text(
                text = "💡 Diversify customer base to reduce risk. Focus on top customers for retention.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun CustomerRevenueBar(
    customer: TopCustomerMetric,
    onCustomerClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCustomerClick(customer.customerId) }
    ) {
        // Customer name and percentage
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = customer.customerName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = customer.percentageFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = customer.revenueFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Percentage bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
        ) {
            // Colored fill
            val barColor = getRevenueConcentrationColor(customer.percentageOfTotal)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (customer.percentageOfTotal / 100.0).toFloat())
                    .background(
                        color = barColor,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun RiskWarningBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BizapColors.StatusOverdue.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "⚠️ $message",
            style = MaterialTheme.typography.labelSmall,
            color = BizapColors.StatusOverdue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

/**
 * Get color for revenue concentration bar.
 * Green for low concentration, red for high.
 */
private fun getRevenueConcentrationColor(percentage: Double): Color {
    return when {
        percentage > 50.0 -> BizapColors.StatusOverdue          // Red - high risk
        percentage > 30.0 -> BizapColors.AnalyticsWarning       // Orange - medium risk
        else -> BizapColors.StatusPaid                          // Green - well distributed
    }
}

