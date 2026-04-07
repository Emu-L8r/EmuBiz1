package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import com.emul8r.bizap.ui.gui2.common.formatCents
import com.emul8r.bizap.ui.gui2.invoices.InvoiceListViewModelV2
import com.emul8r.bizap.ui.gui2.invoices.InvoiceListUiStateV2
import com.emul8r.bizap.ui.gui2.invoices.components.CompactInvoiceList
import com.emul8r.bizap.ui.gui2.invoices.components.ModernInvoiceList
import com.emul8r.bizap.domain.model.UIMode

import androidx.navigation.NavController
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2

/**
 * GUI2 Invoice List Screen
 * Displays all invoices for the active business with type-safe navigation.
 *
 * **Phase 3 Improvement:**
 * - Replaced onInvoiceClick/onCreateInvoice/onBack callbacks with NavController
 * - Type-safe navigation via ScreenV2 routes
 * - Direct navigation control without callback chaining
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreenV2(
    businessId: Long,
    navController: NavController,
    uiMode: UIMode = UIMode.MODERN,
    viewModel: InvoiceListViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoices") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ScreenV2.CreateInvoice(businessId)) }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Invoice")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is InvoiceListUiStateV2.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is InvoiceListUiStateV2.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is InvoiceListUiStateV2.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.invoices) { invoice ->
                        InvoiceListItem(
                            invoice = invoice,
                            onClick = { navController.navigate(ScreenV2.InvoiceDetail(businessId, invoice.id)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceListItem(
    invoice: Invoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Invoice #${invoice.invoiceNumber}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = formatCents(invoice.totalAmount),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun InvoiceCardV2(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val statusColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row with customer name and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = invoice.status.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Invoice number
            Text(
                text = invoice.displayName.ifBlank { invoice.invoiceNumber },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Amount
            Text(
                text = formatCents(invoice.totalAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
    }
}
