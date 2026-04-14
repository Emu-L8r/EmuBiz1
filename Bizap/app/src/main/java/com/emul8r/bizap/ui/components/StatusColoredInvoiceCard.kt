package com.emul8r.bizap.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.gui2.common.formatCents
import com.emul8r.bizap.ui.theme.StatusColors
import com.emul8r.bizap.ui.theme.getBackgroundColor
import com.emul8r.bizap.ui.theme.getBorderColor
import com.emul8r.bizap.ui.theme.getStatusColor
import com.emul8r.bizap.ui.theme.getStatusColorDark
import com.emul8r.bizap.ui.utils.formatDate

/**
 * Status-Colored Invoice Card Component
 *
 * **Purpose:**
 * Reusable invoice list item that displays invoice information with status-based color coding.
 * Improves visual scanning by using colors to indicate invoice status at a glance.
 *
 * **Features:**
 * - Status-based background color
 * - Status-based text color (high contrast)
 * - Optional left accent bar (for modern/GUI2 design)
 * - Professional spacing and typography
 * - Tap feedback with clickable surface
 * - Works in light and dark themes
 *
 * **Color System:**
 * Uses StatusColors.kt color palette:
 * - PAID: Green
 * - SENT: Blue
 * - DRAFT: Gray
 * - OVERDUE: Red
 * - CANCELLED: Gray
 *
 * **Usage:**
 * ```kotlin
 * StatusColoredInvoiceCard(
 *     invoice = invoice,
 *     onClick = { onInvoiceClick(invoice.id) },
 *     showAccentBar = true  // For modern GUI2
 * )
 * ```
 *
 * @param invoice Invoice data to display
 * @param onClick Callback when card is tapped
 * @param modifier Compose modifier for card styling
 * @param showAccentBar Whether to show left accent bar (recommended for modern design)
 */
@Composable
fun StatusColoredInvoiceCard(
    invoice: Invoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAccentBar: Boolean = false
) {
    val backgroundColor = invoice.status.getBackgroundColor()
    val textColor = invoice.status.getStatusColorDark()
    val borderColor = invoice.status.getBorderColor()
    val accentColor = invoice.status.getStatusColor()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional: Left accent bar (modern design)
            if (showAccentBar) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(70.dp)
                        .background(accentColor, RoundedCornerShape(2.dp))
                )
            }

            // Receipt icon
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Invoice",
                tint = textColor,
                modifier = Modifier.size(28.dp)
            )

            // Invoice details (expandable)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Customer name
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Invoice number and date
                Text(
                    text = "Invoice #${invoice.invoiceNumber} • ${formatDate(invoice.dateCreated)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )

                // Notes (if available)
                if (!invoice.notes.isNullOrBlank()) {
                    Text(
                        text = invoice.notes!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp
                    )
                }
            }

            // Amount and status (right side)
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Total amount
                Text(
                    text = formatCents(invoice.totalAmount),
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Status badge
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = invoice.status.name
                                .replace("_", " ")
                                .split(" ")
                                .joinToString(" ") { word ->
                                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                },
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview/Skeleton variant for loading state.
 * Shows a placeholder card while invoice data is loading.
 */
@Composable
fun StatusColoredInvoiceCardSkeleton(
    modifier: Modifier = Modifier,
    showAccentBar: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showAccentBar) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(70.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                )
            }

            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(28.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(5.dp)
                        )
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(12.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(10.dp)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(5.dp)
                        )
                )
            }
        }
    }
}




