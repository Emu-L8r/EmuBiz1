package com.emu.emubizwax.ui.invoices.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emu.emubizwax.ui.invoices.InvoiceFormState

@Composable
fun InvoiceSummaryBar(state: InvoiceFormState, onSave: () -> Unit) {
    Surface(tonalElevation = 8.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Amount:", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "$${String.format("%.2f", state.grandTotal)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = state.selectedCustomer != null && state.items.any { it.description.isNotBlank() }
            ) {
                Text("Generate Document")
            }
        }
    }
}
