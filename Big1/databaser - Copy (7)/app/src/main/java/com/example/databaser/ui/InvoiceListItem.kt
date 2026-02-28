package com.example.databaser.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.databaser.data.InvoiceWithCustomerAndLineItems

@Composable
fun InvoiceListItem(invoice: InvoiceWithCustomerAndLineItems, onInvoiceClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onInvoiceClick(invoice.invoice.id) },
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        ListItem(
            headlineContent = { Text(invoice.customer.name, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("Invoice #${invoice.invoice.invoiceNumber}") },
            trailingContent = { Text("$${invoice.lineItems.sumOf { it.quantity * it.unitPrice }}") },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        )
    }
}
