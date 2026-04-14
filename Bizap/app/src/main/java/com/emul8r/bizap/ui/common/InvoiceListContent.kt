package com.emul8r.bizap.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.designsystem.BizapStatusBadge
import com.emul8r.bizap.ui.gui2.common.PaymentProgressBar
import com.emul8r.bizap.ui.gui2.common.StatusBadge
import com.emul8r.bizap.utils.CentsFormatter

/**
 * Shared invoice list composable used by both GUI1 and GUI2.
 *
 * Renders a lazy scrollable list of [Invoice] items with a status badge
 * and a formatted amount.  Each row calls [onInvoiceClick] with the
 * invoice ID when tapped.
 *
 * This component is **display-only**: it does not own a ViewModel.
 * Pass pre-loaded data from whichever ViewModel is driving the screen.
 *
 * **Usage:**
 * ```kotlin
 * InvoiceListContent(
 *     invoices = invoiceState.invoices,
 *     onInvoiceClick = { id -> navController.navigate("invoice/$id") }
 * )
 * ```
 */
@Composable
fun InvoiceListContent(
    invoices: List<Invoice>,
    onInvoiceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(invoices, key = { it.id }) { invoice ->
            InvoiceListRow(invoice = invoice, onClick = { onInvoiceClick(invoice.id) })
        }
    }
}

/**
 * Single row in an invoice list.
 *
 * Extracted as a standalone composable so it can be previewed and
 * reused independently of [InvoiceListContent].
 */
@Suppress("DEPRECATION")  // Using new StatusBadge with colors intentionally
@Composable
fun InvoiceListRow(
    invoice: Invoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: Invoice Number + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = invoice.customerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                // WIN #14: Color-coded status badge with emoji
                StatusBadge(status = invoice.status)
            }

            // Amount row
            Text(
                text = CentsFormatter.formatCents(invoice.totalAmount, invoice.currency),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            // WIN #14: Payment progress bar (visual feedback)
            val paymentPercent = if (invoice.totalAmount > 0) {
                (invoice.amountPaid.toFloat() / invoice.totalAmount) * 100f
            } else {
                0f
            }
            PaymentProgressBar(percent = paymentPercent, animateToPercent = true)
        }
    }
}

/**
 * Empty-state placeholder shown when an invoice list has no items.
 */
@Composable
fun EmptyInvoiceList(
    message: String = "No invoices found",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
