package com.emul8r.bizap.ui.gui2.invoices.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emul8r.bizap.domain.model.Invoice

/**
 * Dense invoice list for Compact mode using [CompactInvoiceItem].
 */
@Composable
fun CompactInvoiceList(
    invoices: List<Invoice>,
    onInvoiceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(invoices, key = { it.id }) { invoice ->
            CompactInvoiceItem(
                invoice = invoice,
                onClick = { onInvoiceClick(invoice.id) }
            )
            HorizontalDivider()
        }
    }
}
