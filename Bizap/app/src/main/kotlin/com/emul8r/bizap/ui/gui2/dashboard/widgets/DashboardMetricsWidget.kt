package com.emul8r.bizap.ui.gui2.dashboard.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.repository.DashboardMetrics
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.utils.CentsFormatter

// ...existing code...

/**
 * Dashboard metrics widget showing key business stats at a glance.
 *
 * Displays three critical metrics:
 * 1. Unpaid invoices: Count of unpaid invoices + total outstanding amount
 * 2. Overdue amount: Critical metric - amount past due date
 * 3. Paid this month: Positive metric - amount collected this period
 *
 * **Purpose:**
 * Provides quick visual overview of business health and cash flow status.
 * Users can see financial health at a glance without opening reports.
 *
 * **Colors:**
 * - Unpaid: Orange (Warning - needs attention)
 * - Overdue: Red (Critical - immediate action needed)
 * - Paid: Green (Positive - money collected)
 *
 * @param metrics Dashboard metrics data
 * @param onUnpaidClick Optional callback when unpaid box is tapped
 * @param onOverdueClick Optional callback when overdue box is tapped
 * @param onPaidClick Optional callback when paid box is tapped
 * @param modifier Optional modifier
 */
@Composable
fun DashboardMetricsWidget(
    metrics: DashboardMetrics,
    onUnpaidClick: (() -> Unit)? = null,
    onOverdueClick: (() -> Unit)? = null,
    onPaidClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Unpaid Count & Overdue Amount (Critical)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Unpaid Invoices (with badge showing count)
            MetricBox(
                icon = Icons.Default.Receipt,
                label = "Unpaid",
                value = metrics.unpaidInvoiceCount.toString(),
                subValue = CentsFormatter.formatCents(metrics.unpaidAmount),
                color = BizapColors.AnalyticsWarning,
                modifier = Modifier.weight(1f),
                onClick = onUnpaidClick,
                badgeValue = if (metrics.unpaidInvoiceCount > 0) metrics.unpaidInvoiceCount.toString() else null
            )

            // Overdue (CRITICAL - with pulsing alert badge)
            MetricBox(
                icon = Icons.Default.Error,
                label = "Overdue",
                value = CentsFormatter.formatCents(metrics.overdueAmount),
                subValue = "Past due",
                color = BizapColors.AnalyticsAtRisk,
                modifier = Modifier.weight(1f),
                onClick = onOverdueClick,
                isCritical = metrics.overdueAmount > 0
            )
        }

        // Row 2: Paid This Month (Full Width - with check badge)
        MetricBox(
            icon = Icons.Default.CheckCircle,
            label = "Paid This Month",
            value = CentsFormatter.formatCents(metrics.paidThisMonth),
            subValue = "Collected",
            color = BizapColors.AnalyticsExcellent,
            modifier = Modifier.fillMaxWidth(),
            onClick = onPaidClick,
            badgeValue = "✓"
        )
    }
}

/**
 * Individual metric box component.
 *
 * Displays a metric with icon, label, main value, and sub-value.
 * Optionally shows badge for count or status.
 * Can be marked as critical to show alert state with pulsing animation.
 *
 * @param icon Icon to display
 * @param label Text label (e.g., "Unpaid")
 * @param value Main metric value (e.g., "$5,000")
 * @param subValue Sub-text (e.g., "3 invoices")
 * @param color Theme color (background + icon)
 * @param modifier Optional modifier
 * @param onClick Optional click handler
 * @param isCritical If true, shows pulsing alert styling
 * @param badgeValue Optional value to show in badge (e.g., count or "!")
 */
@Composable
private fun MetricBox(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isCritical: Boolean = false,
    badgeValue: String? = null
) {
    // Pulsing animation for critical items
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScan by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.height(100.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.08f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isCritical) 2.dp else 1.dp,
                color = if (isCritical) color else color.copy(alpha = 0.3f)
            ),
            onClick = { onClick?.invoke() },
            enabled = onClick != null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = color
                )

                // Text Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = subValue,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Badge overlay (top-right corner)
        if (badgeValue != null || isCritical) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .let {
                        if (isCritical) {
                            it.scale(pulseScan)  // Apply pulsing animation
                        } else {
                            it
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    color = if (isCritical) BizapColors.AnalyticsAtRisk else color,
                    shape = CircleShape,
                    shadowElevation = if (isCritical) 8.dp else 2.dp
                ) {
                    Text(
                        text = badgeValue ?: "!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    )
                }
            }
        }
    }
}






