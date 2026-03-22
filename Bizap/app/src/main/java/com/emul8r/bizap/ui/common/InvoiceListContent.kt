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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                Text(
                    text = CentsFormatter.formatCents(invoice.totalAmount, invoice.currencyCode),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            BizapStatusBadge(status = invoice.status)
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
