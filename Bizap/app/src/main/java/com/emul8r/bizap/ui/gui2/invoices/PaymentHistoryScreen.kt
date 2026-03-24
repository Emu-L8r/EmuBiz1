package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.gui2.common.formatCents
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Payment history screen for GUI2.
 *
 * **Purpose:**
 * Displays complete payment history for a single invoice. Shows timeline of all recorded
 * payments with dates, amounts, and status indicators. Allows users to track payment progress
 * and see detailed payment records.
 *
 * **Features:**
 * - Invoice Summary Card
 *   - Total invoice amount
 *   - Amount paid so far
 *   - Outstanding balance
 *   - Payment completion percentage
 * - Payment Timeline
 *   - Chronological list of payments (latest first)
 *   - Color-coded status indicators
 *   - Payment dates and amounts
 *   - Optional payment notes
 * - Empty State
 *   - Shows message if no payments recorded
 *   - Encourages recording first payment
 * - Real-time Updates
 *   - Automatically reflects new payments
 *   - Updates when payment is recorded
 *
 * **Layout:**
 * ```
 * ┌─────────────────────────────┐
 * │ Invoice: INV-001            │
 * │                             │
 * │ Total:    $1,000.00         │
 * │ Paid:     $750.00 (75%)     │
 * │ Outstanding: $250.00        │
 * │                             │
 * │ [Payment Progress Bar]      │
 * │ ████████░░ 75%             │
 * └─────────────────────────────┘
 *
 * ┌─────────────────────────────┐
 * │ 💳 Payment #1              │
 * │ $500.00 on March 20, 2026   │
 * │ Notes: Received payment     │
 * └─────────────────────────────┘
 *
 * ┌─────────────────────────────┐
 * │ 💳 Payment #2              │
 * │ $250.00 on March 25, 2026   │
 * │ Notes: Final payment        │
 * └─────────────────────────────┘
 * ```
 *
 * **Data Flow:**
 * ```
 * Screen mounts with invoiceId
 *     ↓
 * ViewModel loads payment history
 *     ↓
 * Emits PaymentHistoryUiState with all payments
 *     ↓
 * UI displays summary + timeline
 *     ↓
 * Auto-updates when payments are recorded
 * ```
 *
 * **Color Coding:**
 * - 🟢 Green: Successfully recorded
 * - 🔵 Blue: Pending confirmation
 * - 🔴 Red: Failed/disputed
 * - 🟠 Orange: Partial payment
 *
 * @param invoiceId Invoice to show payment history for
 * @param viewModel PaymentHistoryViewModel managing state
 * @param modifier Composable modifier
 *
 * @see PaymentHistoryViewModel
 * @see InvoiceDetailScreen
 */
@Composable
fun PaymentHistoryScreen(
    invoiceId: Long,
    viewModel: PaymentHistoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.paymentHistory.collectAsStateWithLifecycle(
        initialValue = PaymentHistoryUiState(
            invoiceId = invoiceId,
            invoiceName = "",
            totalAmount = 0,
            paidAmount = 0,
            outstandingAmount = 0
        )
    )

    Timber.d("🎨 Rendering PaymentHistoryScreen for invoice $invoiceId")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with totals
        PaymentHistoryHeader(uiState)

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Timeline
        if (uiState.paymentHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No payment history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = uiState.paymentHistory,
                    key = { "${it.date}-${it.status}" }
                ) { payment ->
                    PaymentHistoryCard(payment)
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryHeader(state: PaymentHistoryUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Invoice: ${state.invoiceName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaymentStatCard(
                label = "Total",
                amount = state.totalAmount,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            PaymentStatCard(
                label = "Paid",
                amount = state.paidAmount,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            PaymentStatCard(
                label = "Outstanding",
                amount = state.outstandingAmount,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual stat card showing a single amount.
 *
 * @param label Card title (e.g., "Total")
 * @param amount Amount in cents
 * @param color Tint color for the card
 * @param modifier Compose modifier
 */
@Composable
private fun PaymentStatCard(
    label: String,
    amount: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(60.dp),
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                formatCents(amount),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Single payment record card in timeline.
 *
 * Displays:
 * - Status icon (circle with color)
 * - Payment amount
 * - Status text (PAID, UNPAID, etc.)
 * - Payment date
 * - Days overdue (if applicable)
 *
 * @param item Payment record
 */
@Composable
private fun PaymentHistoryCard(item: PaymentHistoryItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            val (icon, tint) = when (item.status) {
                "PAID" -> Icons.Filled.CheckCircle to Color(0xFF4CAF50) // Green
                "OVERDUE" -> Icons.Filled.Error to Color(0xFFF44336) // Red
                else -> Icons.Filled.Schedule to Color(0xFFFFC107) // Orange/Amber
            }

            Icon(
                imageVector = icon,
                contentDescription = item.status,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatCents(item.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = when (item.status) {
                            "PAID" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "OVERDUE" -> Color(0xFFF44336).copy(alpha = 0.2f)
                            else -> Color(0xFFFFC107).copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            item.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (item.status) {
                                "PAID" -> Color(0xFF4CAF50)
                                "OVERDUE" -> Color(0xFFF44336)
                                else -> Color(0xFFFFC107)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    formatPaymentDate(item.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (item.daysSinceDue > 0) {
                    Text(
                        "${item.daysSinceDue} days overdue",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Format milliseconds timestamp to user-friendly date string.
 *
 * @param millis Timestamp in milliseconds
 * @return Formatted date (e.g., "Mar 24, 2026")
 */
private fun formatPaymentDate(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        Timber.e(e, "Failed to format payment date: $millis")
        "Unknown date"
    }
}
