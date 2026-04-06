package com.emul8r.bizap.ui.gui2.invoices.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.Invoice

/**
 * Spacious invoice list for Modern mode using [ModernInvoiceCard].
 */
@Composable
fun ModernInvoiceList(
    invoices: List<Invoice>,
    onInvoiceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(invoices, key = { it.id }) { invoice ->
            ModernInvoiceCard(
                invoice = invoice,
                onClick = { onInvoiceClick(invoice.id) }
            )
        }
    }
}
