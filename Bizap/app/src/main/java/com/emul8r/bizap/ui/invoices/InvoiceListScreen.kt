package com.emul8r.bizap.ui.invoices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.utils.CentsFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.common.StatusBadge
import com.emul8r.bizap.ui.components.ErrorStateView
import com.emul8r.bizap.ui.components.SkeletonLoadingItem
import com.emul8r.bizap.ui.theme.getStatusColor
import com.emul8r.bizap.ui.theme.getBackgroundColor
import com.emul8r.bizap.ui.utils.formatDate
import java.util.Locale

@Composable
fun InvoiceListScreen(
    onInvoiceClick: (Long) -> Unit,
    onViewAnalytics: (() -> Unit)? = null,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        if (onViewAnalytics != null) {
            OutlinedButton(
                onClick = onViewAnalytics,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Revenue Analytics")
            }
        }

        // MainActivity's Scaffold provides the TopAppBar
        when (val currentState = state) {
            is InvoiceListUiState.Loading -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(5) { SkeletonLoadingItem() }
            }
            is InvoiceListUiState.Empty -> EmptyState(Modifier.fillMaxSize())
            is InvoiceListUiState.Error -> ErrorStateView(
                title = "Error Loading Invoices",
                message = currentState.message,
                onAction = { viewModel.retry() },
                modifier = Modifier.fillMaxSize()
            )
            is InvoiceListUiState.Success -> InvoiceList(
                invoices = currentState.invoices,
                onInvoiceClick = onInvoiceClick,
                onStatusChange = viewModel::updateInvoiceStatus
            )
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
            val statusColor = invoice.status.getStatusColor()
            val backgroundColor = invoice.status.getBackgroundColor()
            
            Card(
                onClick = { onInvoiceClick(invoice.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor
                ),
                border = BorderStroke(2.dp, statusColor.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Top accent line
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .padding(bottom = 8.dp)
                    )
                    
                    // Header row with invoice number and status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = invoice.displayName.ifBlank { invoice.invoiceNumber },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatusBadge(status = invoice.status)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Customer name
                    Text(
                        text = invoice.customerName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Amount and date
                    Text(
                        text = "Total: ${CentsFormatter.formatCents(invoice.totalAmount, invoice.currencyCode)} | ${formatDate(invoice.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
