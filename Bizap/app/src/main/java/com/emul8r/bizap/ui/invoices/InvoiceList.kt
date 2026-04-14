package com.emul8r.bizap.ui.invoices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.shared.InvoiceStatusChip
import com.emul8r.bizap.utils.CentsFormatter

@Composable
fun InvoiceList(
    modifier: Modifier = Modifier,
    viewModel: InvoiceListViewModel = hiltViewModel(),
    onInvoiceClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is InvoiceListUiState.Loading -> {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is InvoiceListUiState.Empty -> {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No invoices found", style = MaterialTheme.typography.bodyLarge)
            }
        }
        is InvoiceListUiState.Error -> {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is InvoiceListUiState.Success -> {
            Column(modifier = modifier.fillMaxWidth()) {
                state.invoices.forEach { invoice ->
                    ListItem(
                        modifier = Modifier.clickable { onInvoiceClick(invoice.id) },
                        headlineContent = { Text("Invoice #${invoice.id} - ${invoice.customerName}") },
                        supportingContent = { Text("Total: ${CentsFormatter.formatCents(invoice.totalAmount, invoice.currency)}") },
                        trailingContent = { InvoiceStatusChip(status = invoice.status.name) }
                    )
                }
            }
        }
    }
}
