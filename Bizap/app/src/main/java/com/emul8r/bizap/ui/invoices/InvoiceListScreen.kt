package com.emul8r.bizap.ui.invoices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
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
import com.emul8r.bizap.ui.designsystem.BizapStatusBadge
import com.emul8r.bizap.ui.components.ErrorStateView
import com.emul8r.bizap.ui.components.SkeletonLoadingItem
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.common.formatCents
import com.emul8r.bizap.ui.gui2.invoices.InvoiceListUiStateV2
import com.emul8r.bizap.ui.gui2.invoices.InvoiceListViewModelV2
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.theme.getStatusColor
import com.emul8r.bizap.ui.theme.getBackgroundColor
import com.emul8r.bizap.ui.utils.formatDate
import com.emul8r.bizap.ui.invoices.InvoiceListUiState
import com.emul8r.bizap.ui.invoices.InvoiceListViewModel

/**
 * Invoice list screen Composable for GUI1.
 *
 * **Purpose:**
 * Displays all invoices for the active business in a scrollable list.
 * Allows users to view invoice details, filter by status, and manage invoices.
 *
 * **Features:**
 * - Scrollable invoice list with pagination
 * - Invoice cards showing:
 *   - Customer name
 *   - Invoice date and total amount
 *   - Status badge (DRAFT, SENT, PAID, OVERDUE, CANCELLED)
 *   - Invoice number
 * - Status filter tabs
 * - Pull-to-refresh retry
 * - Loading skeleton while fetching
 * - Error state with retry button
 * - Click to view invoice details
 * - FAB (Floating Action Button) to create new invoice
 *
 * **States:**
 * - Loading: Shows skeleton loading cards
 * - Empty: Shows empty state message when no invoices
 * - Success: Shows invoice list
 * - Error: Shows error message with retry button
 *
 * **Data Flow:**
 * ```
 * Screen mounts
 *     ↓
 * ViewModel loads invoices via repository
 *     ↓
 * InvoiceListUiState emits:
 *   - Loading (initial)
 *   - Success(invoices) or Empty or Error
 *     ↓
 * UI recomposes with updated state
 *     ↓
 * User taps invoice
 *     ↓
 * Navigate to InvoiceDetailScreen(invoiceId)
 * ```
 *
 * **Interactions:**
 * - Tap invoice card → Navigate to detail screen
 * - Pull down → Retry load
 * - FAB → Create new invoice
 * - Status tab → Filter list (if implemented)
 *
 * @param viewModel InvoiceListViewModel managing list state
 * @param onInvoiceSelected Callback with invoiceId when user taps an invoice
 * @param onCreateNew Callback when user taps FAB to create invoice
 *
 * @see InvoiceListViewModel
 * @see InvoiceDetailScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    guiMode: GuiMode = GuiMode.GUI1,
    businessId: Long? = null,
    onInvoiceClick: (Long) -> Unit,
    onCreateInvoice: () -> Unit = {},
    onViewAnalytics: (() -> Unit)? = null,
    onBack: () -> Unit = {},
) {
    when (guiMode) {
        GuiMode.GUI1 -> InvoiceListScreenV1Content(
            onInvoiceClick = onInvoiceClick,
            onViewAnalytics = onViewAnalytics,
        )
        GuiMode.GUI2 -> InvoiceListScreenV2Content(
            onInvoiceClick = onInvoiceClick,
            onCreateInvoice = onCreateInvoice,
            onBack = onBack,
        )
    }
}

@Composable
private fun InvoiceListScreenV1Content(
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
                onInvoiceClick = onInvoiceClick
            )
        }
    }
}

@Composable
fun InvoiceList(
    invoices: List<Invoice>,
    onInvoiceClick: (Long) -> Unit
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
                        BizapStatusBadge(status = invoice.status)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceListScreenV2Content(
    onInvoiceClick: (Long) -> Unit,
    onCreateInvoice: () -> Unit,
    onBack: () -> Unit,
    viewModel: InvoiceListViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateInvoice) {
                Icon(Icons.Default.Receipt, contentDescription = "Create Invoice")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is InvoiceListUiStateV2.Loading -> LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            is InvoiceListUiStateV2.Error -> ErrorStateV2(message = state.message, modifier = Modifier.padding(paddingValues))
            is InvoiceListUiStateV2.Success -> InvoiceListV2Content(
                invoices = state.invoices,
                onInvoiceClick = onInvoiceClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun InvoiceListV2Content(invoices: List<Invoice>, onInvoiceClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    if (invoices.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No invoices yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tap + to create your first invoice", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(invoices) { invoice ->
                InvoiceCardV2(invoice = invoice, onClick = { onInvoiceClick(invoice.id) })
            }
        }
    }
}

@Composable
private fun InvoiceCardV2(invoice: Invoice, onClick: () -> Unit) {
    val statusColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, statusColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = invoice.customerName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(text = invoice.status.toString(), style = MaterialTheme.typography.labelSmall)
            }
            Text(text = invoice.displayName.ifBlank { invoice.invoiceNumber }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = formatCents(invoice.totalAmount), style = MaterialTheme.typography.titleMedium, color = statusColor)
        }
    }
}
