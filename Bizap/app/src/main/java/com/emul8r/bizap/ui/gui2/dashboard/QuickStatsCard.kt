package com.emul8r.bizap.ui.gui2.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.gui2.common.formatCents
import timber.log.Timber

/**
 * WIN #12: Dashboard Quick Stats Card
 *
 * Displays 4 key metrics at a glance:
 * - Total Revenue (YTD)
 * - Amount Overdue (red alert if > 0)
 * - Due This Month (count)
 * - Pending Payments (count)
 *
 * Users see business health in 2 seconds!
 */
@Composable
fun QuickStatsCard(
    totalRevenue: Long,          // cents
    amountOverdue: Long,         // cents
    dueThisMonth: Int,           // count
    pendingPayments: Int,        // count
    modifier: Modifier = Modifier
) {
    Timber.d("QuickStatsCard: revenue=$totalRevenue, overdue=$amountOverdue, dueMonth=$dueThisMonth, pending=$pendingPayments")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Text(
                "📊 Business Overview",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Top Row: Revenue | Overdue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Revenue
                QuickStatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Total Revenue",
                    value = formatCents(totalRevenue),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Amount Overdue (Red if > 0)
                QuickStatItem(
                    icon = Icons.Default.WarningAmber,
                    label = "Overdue Amount",
                    value = formatCents(amountOverdue),
                    valueColor = if (amountOverdue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom Row: Due This Month | Pending
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Due This Month
                QuickStatItem(
                    icon = Icons.Default.DateRange,
                    label = "Due This Month",
                    value = "$dueThisMonth invoices",
                    valueColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )

                // Pending Payments
                QuickStatItem(
                    icon = Icons.Default.HourglassEmpty,
                    label = "Pending Payments",
                    value = "$pendingPayments awaiting",
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual stat item within QuickStatsCard
 */
@Composable
private fun QuickStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Icon + Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = MaterialTheme.typography.labelSmall.fontSize
            )
        }

        // Value
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
    }
}


