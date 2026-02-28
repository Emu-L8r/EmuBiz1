package com.emu.emubizwax.ui.invoices

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun InvoiceListScreen(onInvoiceClick: (Long) -> Unit, onAddInvoice: () -> Unit) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddInvoice,
                icon = { Icon(Icons.Default.Add, "Add Invoice") },
                text = { Text("New Invoice") }
            )
        }
    ) {
        Text(text = "Invoice List Screen", modifier = androidx.compose.ui.Modifier.padding(it))
    }
}
