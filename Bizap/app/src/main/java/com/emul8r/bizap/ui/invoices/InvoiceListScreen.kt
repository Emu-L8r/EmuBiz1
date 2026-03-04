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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.utils.CentsFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.shared.InvoiceStatusChip
import com.emul8r.bizap.ui.utils.formatDate
import java.util.Locale

@Composable
fun InvoiceListScreen(
    onInvoiceClick: (Long) -> Unit,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // MainActivity's Scaffold provides the TopAppBar
    when (val currentState = state) {
        is InvoiceListUiState.Loading -> Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
                is InvoiceListUiState.Empty -> EmptyState(Modifier.align(Alignment.Center))
                is InvoiceListUiState.Error -> Text(
                    currentState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                is InvoiceListUiState.Success -> InvoiceList(
                    invoices = currentState.invoices,
                    onInvoiceClick = onInvoiceClick,
                    onStatusChange = viewModel::updateInvoiceStatus
                )
            }
        is InvoiceListUiState.Empty -> Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text("No invoices yet", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun InvoiceList(
    invoices: List<Invoice>,
    onInvoiceClick: (Long) -> Unit,
    onStatusChange: (Long, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(invoices) { invoice ->
            ElevatedCard(
                onClick = { onInvoiceClick(invoice.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    overlineContent = { 
                        Text(
                            "INV-${invoice.id}", 
                            color = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    headlineContent = { Text(invoice.customerName) },
                    supportingContent = {
                        Text("Total: ${CentsFormatter.formatCents(invoice.totalAmount, invoice.currencyCode)} | ${formatDate(invoice.date)}")
                    },
                    trailingContent = {
                        InvoiceStatusChip(invoice.status.name)
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
            Icons.Filled.Receipt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text("No invoices yet", style = MaterialTheme.typography.bodyLarge)
    }
}
