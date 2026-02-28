package com.emul8r.bizap.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.ui.shared.InvoiceStatusChip
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onInvoiceClick: (Long) -> Unit,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Invoices & Quotes") }) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val currentState = state) {
                is InvoiceListUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is InvoiceListUiState.Empty -> EmptyState(Modifier.align(Alignment.Center))
                is InvoiceListUiState.Error -> Text(
                    currentState.message,
                    color = MaterialTheme.colorScheme.error
                )

                is InvoiceListUiState.Success -> InvoiceList(
                    invoices = currentState.invoices,
                    onInvoiceClick = onInvoiceClick,
                    onStatusChange = viewModel::updateInvoiceStatus
                )
            }
        }
    }
}

@Composable
fun InvoiceList(
    invoices: List<InvoiceWithItems>,
    onInvoiceClick: (Long) -> Unit,
    onStatusChange: (Long, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(invoices) { item ->
            ElevatedCard(
                onClick = { onInvoiceClick(item.invoice.invoiceId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    overlineContent = { Text("INV-${item.invoice.invoiceId}") },
                    headlineContent = { Text(item.invoice.customerName) }, // Assuming you joined Customer Name
                    supportingContent = {
                        Text("Total: $${String.format("%.2f", item.subtotal)} | ${formatDate(item.invoice.date)}")
                    },
                    trailingContent = {
                        InvoiceStatusChip(item.invoice.status)
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text("No invoices yet", style = MaterialTheme.typography.bodyLarge)
    }
}
