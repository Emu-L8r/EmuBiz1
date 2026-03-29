package com.emul8r.bizap.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.InvoiceStatus

/**
 * Centralized Design System for Bizap
 *
 * This module provides all reusable UI components and styling constants.
 * Ensures consistency across the app and makes theming changes apply everywhere.
 *
 * Usage:
 * ```
 * BizapStatusBadge(status = InvoiceStatus.PAID)
 * BizapMetricCard(title = "Revenue", value = "$5,000", icon = Icons.Default.TrendingUp)
 * BizapPaymentCard(customerName = "Acme Corp", amount = "$2,500")
 * ```
 */
object BizapDesignSystem {

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT: Status Badge
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Status badge with icon and colored background.
     * Maps invoice status to appropriate visual indicator.
     *
     * @param status The invoice status to display
     * @param modifier Optional modifier for layout customization
     */
    @Composable
    fun StatusBadge(
        status: InvoiceStatus,
        modifier: Modifier = Modifier
    ) {
        val (backgroundColor, textColor, icon) = when (status) {
            InvoiceStatus.PAID -> Triple(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.primary,
                Icons.Default.CheckCircle
            )
            InvoiceStatus.SENT -> Triple(
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.secondary,
                Icons.AutoMirrored.Filled.Send
            )
            InvoiceStatus.DRAFT -> Triple(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.outline,
                Icons.Default.Edit
            )
            InvoiceStatus.OVERDUE -> Triple(
                MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.error,
                Icons.Default.Error
            )
            InvoiceStatus.PARTIALLY_PAID -> Triple(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.tertiary,
                Icons.Default.Schedule
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    tint = textColor,
                    modifier = Modifier.size(16.dp),
                    contentDescription = null
                )
                Text(
                    text = status.name.replace("_", " "),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT: Metric Card
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Color-coded metric card with border, background tint, icon and value display.
     * Perfect for dashboard metric displays with visual hierarchy.
     *
     * @param title The metric title (e.g., "Total Revenue")
     * @param value The metric value (e.g., "$5,000.00")
     * @param icon The icon to display
     * @param modifier Optional modifier
     */
    @Composable
    fun MetricCard(
        title: String,
        value: String,
        icon: ImageVector,
        modifier: Modifier = Modifier
    ) {
        val backgroundColor = MaterialTheme.colorScheme.primaryContainer
        val borderColor = MaterialTheme.colorScheme.primary
        val accentColor = MaterialTheme.colorScheme.primary

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.5f)
            ),
            border = BorderStroke(2.dp, borderColor.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT: Payment Card
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Payment card showing customer name, amount, and status.
     * Used in payment lists and dashboards.
     *
     * @param customerName The customer name
     * @param amount The payment amount
     * @param status The payment status
     * @param date Optional date string
     * @param modifier Optional modifier
     * @param onClick Optional click handler
     */
    @Composable
    fun PaymentCard(
        customerName: String,
        amount: String,
        status: InvoiceStatus,
        date: String? = null,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (date != null) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatusBadge(status = status)
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT: Analytics Card
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Analytics metric card with percentage display.
     * Used for collection rate, aging analysis, and KPI displays.
     *
     * @param title The metric title
     * @param percentage The percentage value (0-100)
     * @param modifier Optional modifier
     */
    @Composable
    fun AnalyticsCard(
        title: String,
        percentage: Double,
        modifier: Modifier = Modifier
    ) {
        val percentageColor = when {
            percentage >= 90 -> MaterialTheme.colorScheme.primary
            percentage >= 70 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.error
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = percentageColor.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, percentageColor.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f%%", percentage),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = percentageColor
                    )
                    LinearProgressIndicator(
                        progress = { (percentage / 100.0).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp),
                        color = percentageColor,
                        trackColor = percentageColor.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT: Invoice Card
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Invoice summary card with all key details.
     * Used in invoice lists and dashboards.
     *
     * @param invoiceNumber The invoice number
     * @param customerName The customer name
     * @param amount The invoice amount
     * @param status The invoice status
     * @param dueDate Optional due date
     * @param modifier Optional modifier
     */
    @Composable
    fun InvoiceCard(
        invoiceNumber: String,
        customerName: String,
        amount: String,
        status: InvoiceStatus,
        dueDate: String? = null,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = invoiceNumber,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = status)
                }

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (dueDate != null) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Due",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dueDate,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT: Line Item Card
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Line item card for invoice details.
     * Shows description, quantity, unit price, and total.
     *
     * @param description Item description
     * @param quantity Item quantity
     * @param unitPrice Price per unit
     * @param totalPrice Total price for this item
     * @param modifier Optional modifier
     */
    @Composable
    fun LineItemCard(
        description: String,
        quantity: String,
        unitPrice: String,
        totalPrice: String,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Qty: $quantity", style = MaterialTheme.typography.labelSmall)
                        Text("Unit: $unitPrice", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(
                        text = totalPrice,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT: Colored Card (Generic Wrapper)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Generic colored card wrapper with accent color border and optional title.
     * Provides flexible styling for custom content.
     *
     * @param accentColor The accent color for border and background
     * @param modifier Optional modifier
     * @param title Optional title text
     * @param content The card content composable
     */
    @Composable
    fun ColoredCard(
        accentColor: Color,
        modifier: Modifier = Modifier,
        title: String? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = accentColor.copy(alpha = 0.08f)
            ),
            border = BorderStroke(2.dp, accentColor.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                content()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CONVENIENCE FUNCTIONS (Shorter names for common use)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Shorter alias for BizapDesignSystem.StatusBadge
 */
@Composable
fun BizapStatusBadge(
    status: InvoiceStatus,
    modifier: Modifier = Modifier
) = BizapDesignSystem.StatusBadge(status, modifier)

/**
 * Shorter alias for BizapDesignSystem.MetricCard
 */
@Composable
fun BizapMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) = BizapDesignSystem.MetricCard(title, value, icon, modifier)

/**
 * Shorter alias for BizapDesignSystem.PaymentCard
 */
@Composable
fun BizapPaymentCard(
    customerName: String,
    amount: String,
    status: InvoiceStatus,
    date: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) = BizapDesignSystem.PaymentCard(customerName, amount, status, date, modifier, onClick)

/**
 * Shorter alias for BizapDesignSystem.AnalyticsCard
 */
@Composable
fun BizapAnalyticsCard(
    title: String,
    percentage: Double,
    modifier: Modifier = Modifier
) = BizapDesignSystem.AnalyticsCard(title, percentage, modifier)

/**
 * Shorter alias for BizapDesignSystem.InvoiceCard
 */
@Composable
fun BizapInvoiceCard(
    invoiceNumber: String,
    customerName: String,
    amount: String,
    status: InvoiceStatus,
    dueDate: String? = null,
    modifier: Modifier = Modifier
) = BizapDesignSystem.InvoiceCard(invoiceNumber, customerName, amount, status, dueDate, modifier)

/**
 * Shorter alias for BizapDesignSystem.LineItemCard
 */
@Composable
fun BizapLineItemCard(
    description: String,
    quantity: String,
    unitPrice: String,
    totalPrice: String,
    modifier: Modifier = Modifier
) = BizapDesignSystem.LineItemCard(description, quantity, unitPrice, totalPrice, modifier)


