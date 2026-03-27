package com.emul8r.bizap.ui.gui2.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.ui.gui2.common.*
import com.emul8r.bizap.ui.gui2.invoices.RecordPaymentDialogV2
import com.emul8r.bizap.ui.gui2.invoices.StatusUpdateMenuV2
import com.emul8r.bizap.ui.gui2.invoices.PaymentHistoryScreen
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog state to manage multiple dialogs without recomposition issues.
 */
private sealed class DialogState {
    object None : DialogState()
    object PaymentDialog : DialogState()
    object StatusMenu : DialogState()
}

/**
 * GUI2 invoice detail screen.
 * Displays a read-only view of a single invoice's details and line items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreenV2(
    businessId: Long,
    invoiceId: Long,
    onBack: () -> Unit,
    viewModel: InvoiceDetailViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is InvoiceDetailUiStateV2.Success) {
                        IconButton(onClick = { dialogState = DialogState.PaymentDialog }) {
                            Icon(Icons.Default.Payment, contentDescription = "Record Payment")
                        }
                        IconButton(onClick = { dialogState = DialogState.StatusMenu }) {
                            Icon(Icons.Default.Edit, contentDescription = "Update Status")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is InvoiceDetailUiStateV2.Loading -> LoadingIndicatorV2(
                modifier = Modifier.padding(paddingValues)
            )
            is InvoiceDetailUiStateV2.NotFound -> ErrorStateV2(
                message = "Invoice #$invoiceId not found.",
                modifier = Modifier.padding(paddingValues)
            )
            is InvoiceDetailUiStateV2.Error -> ErrorStateV2(
                message = state.message,
                modifier = Modifier.padding(paddingValues)
            )
            is InvoiceDetailUiStateV2.Success -> {
                InvoiceDetailContentV2(
                    invoice = state.invoice,
                    businessId = businessId,
                    modifier = Modifier.padding(paddingValues)
                )

                // Memoize status parsing to avoid duplicate conversions
                val currentStatus = remember(state.invoice.invoice.status) {
                    runCatching {
                        InvoiceStatus.valueOf(state.invoice.invoice.status)
                    }.getOrElse { InvoiceStatus.DRAFT }
                }

                // Payment Dialog
                if (dialogState is DialogState.PaymentDialog) {
                    RecordPaymentDialogV2(
                        invoiceId = state.invoice.invoice.id,
                        businessId = businessId,
                        invoiceTotal = state.invoice.invoice.totalAmount,
                        amountPaid = state.invoice.invoice.amountPaid,
                        invoiceDate = state.invoice.invoice.date,
                        invoiceStatus = currentStatus,
                        onDismiss = { dialogState = DialogState.None },
                        onSuccess = { dialogState = DialogState.None }
                    )
                }

                // Status Update Menu
                if (dialogState is DialogState.StatusMenu) {
                    StatusUpdateMenuV2(
                        currentStatus = currentStatus,
                        onStatusSelected = { status: InvoiceStatus ->
                            viewModel.updateInvoiceStatus(status)
                            dialogState = DialogState.None
                        },
                        onDismiss = { dialogState = DialogState.None }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetailContentV2(
    invoice: InvoiceWithItems,
    businessId: Long,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Details", "Items", "Payment History")

    Column(modifier = modifier.fillMaxSize()) {
        // Tab bar
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // Tab content - NO parent scroll, let each tab manage its own scrolling
        // This prevents nested scrolling constraints crash
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> InvoiceDetailsTab(invoice)
                1 -> InvoiceItemsTab(invoice)
                2 -> PaymentHistoryTab(invoice, businessId)
            }
        }
    }
}

@Composable
private fun InvoiceDetailsTab(
    invoice: InvoiceWithItems,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val entity = invoice.invoice

    // THIS TAB manages its own scrolling
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderV2("Invoice Info")

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRowV2("Customer", entity.customerName)
                DetailRowV2("Status", entity.status)
                DetailRowV2("Date", dateFormatter.format(Date(entity.date)))
                if (entity.dueDate > 0) {
                    DetailRowV2("Due Date", dateFormatter.format(Date(entity.dueDate)))
                }
                DetailRowV2("Total", formatCents(entity.totalAmount))
                DetailRowV2("Amount Paid", formatCents(entity.amountPaid))
                DetailRowV2("Outstanding", formatCents(entity.totalAmount - entity.amountPaid))
                DetailRowV2("Currency", entity.currencyCode)
            }
        }

        entity.notes?.let { notes ->
            if (notes.isNotBlank()) {
                HorizontalDivider()
                SectionHeaderV2("Notes")
                Text(notes, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InvoiceItemsTab(
    invoice: InvoiceWithItems,
    modifier: Modifier = Modifier
) {
    // THIS TAB manages its own scrolling
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderV2("Line Items")

        if (invoice.items.isEmpty()) {
            Text(
                "No line items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            invoice.items.forEach { item ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Qty: ${item.quantity}  ×  ${formatCents(item.unitPrice.toLong())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatCents((item.unitPrice * item.quantity).toLong()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PaymentHistoryTab(
    invoice: InvoiceWithItems,
    businessId: Long,
    modifier: Modifier = Modifier
) {
    // PaymentHistoryScreen has its own LazyColumn, so we pass fillMaxSize for proper constraints
    PaymentHistoryScreen(
        invoiceId = invoice.invoice.id,
        businessId = businessId,  // ✅ FIXED: Pass businessId for multi-tenant safety
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun DetailRowV2(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
