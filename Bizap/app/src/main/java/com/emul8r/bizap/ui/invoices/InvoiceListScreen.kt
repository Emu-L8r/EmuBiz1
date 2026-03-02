package com.emul8r.bizap.ui.invoices

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.shared.InvoiceStatusChip
import com.emul8r.bizap.ui.utils.formatDate
import com.emul8r.bizap.utils.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onInvoiceClick: (Long) -> Unit,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Invoices & Quotes") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                SyncStatusIndicator(syncState)
            }
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
fun SyncStatusIndicator(syncState: SyncUiState) {
    val (text, color, icon) = when {
        !syncState.isOnline -> Triple("Offline", MaterialTheme.colorScheme.error, Icons.Default.CloudOff)
        syncState.pendingCount > 0 -> Triple("${syncState.pendingCount} operations pending", MaterialTheme.colorScheme.secondary, Icons.Default.Sync)
        else -> Triple("All synced", MaterialTheme.colorScheme.primary, Icons.Default.CloudDone)
    }

    val visible = !syncState.isOnline || syncState.pendingCount > 0

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = Modifier.testTag("SyncStatusIndicator")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.1f))
                .padding(vertical = 4.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
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
            ElevatedCard(
                onClick = { onInvoiceClick(invoice.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    overlineContent = { 
                        Text(
                            "INV-${invoice.id}", 
                            color = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    headlineContent = { Text(invoice.customerName) },
                    supportingContent = {
                        Text("Total: ${CurrencyFormatter.formatCents(invoice.totalAmount, invoice.currencyCode)} | ${formatDate(invoice.date)}")
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
