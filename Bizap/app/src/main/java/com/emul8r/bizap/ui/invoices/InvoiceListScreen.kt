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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.shared.InvoiceStatusChip
import com.emul8r.bizap.ui.utils.formatDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onInvoiceClick: (Long) -> Unit,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Invoices & Quotes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) 
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val currentState = state) {
                is InvoiceListUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
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
                        Text("Total: $${String.format(Locale.getDefault(), "%.2f", invoice.totalAmount)} | ${formatDate(invoice.date)}")
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

