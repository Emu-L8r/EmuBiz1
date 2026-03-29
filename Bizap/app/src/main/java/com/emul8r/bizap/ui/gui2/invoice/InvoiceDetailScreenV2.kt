package com.emul8r.bizap.ui.gui2.invoice

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GetApp
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
    object ExportPdf : DialogState()
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
                        IconButton(onClick = { dialogState = DialogState.ExportPdf }) {
                            Icon(Icons.Default.GetApp, contentDescription = "Export PDF")
                        }
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
                // Use key to prevent flickering - keyed by invoice ID
                key(state.invoice.invoice.id) {
                    InvoiceDetailContentV2(
                        invoice = state.invoice,
                        businessId = businessId,
                        modifier = Modifier.padding(paddingValues)
                    )
                }

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

                // PDF Export Handler
                if (dialogState is DialogState.ExportPdf) {
                    val pdfExportState by viewModel.pdfExportState.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        viewModel.exportToPdf(state.invoice)
                    }

                    when (pdfExportState) {
                        is PdfExportState.Loading -> {
                            // Show loading dialog
                            AlertDialog(
                                onDismissRequest = { /* Don't allow dismiss during loading */ },
                                confirmButton = { /* No action during loading */ },
                                title = { Text("Exporting PDF") },
                                text = { Text("Please wait while your invoice is being exported to PDF...") }
                            )
                        }
                        is PdfExportState.Success -> {
                            // Show success with file details - KEEP DIALOG OPEN
                            val file = (pdfExportState as PdfExportState.Success).file
                            AlertDialog(
                                onDismissRequest = { dialogState = DialogState.None },
                                confirmButton = {
                                    Button(onClick = { dialogState = DialogState.None }) {
                                        Text("Done")
                                    }
                                },
                                title = { Text("✅ PDFs Generated Successfully") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Both Quote and Invoice PDFs have been generated and saved to the vault.")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Invoice PDF: ${file.name}", style = MaterialTheme.typography.labelSmall)
                                        Text("Size: ${(file.length() / 1024).toInt()} KB", style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Both files are now available in the Vault.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            )
                        }
                        is PdfExportState.Error -> {
                            // Show error with message
                            AlertDialog(
                                onDismissRequest = { dialogState = DialogState.None },
                                confirmButton = {
                                    Button(onClick = { dialogState = DialogState.None }) {
                                        Text("OK")
                                    }
                                },
                                title = { Text("Export Failed") },
                                text = { Text((pdfExportState as PdfExportState.Error).message) }
                            )
                        }
                        else -> {} // Idle state
                    }
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

        // Tab content: Use Crossfade animation for smooth transitions (eliminates flickering)
        // Crossfade provides a smooth fade animation between tabs instead of abrupt recomposition
        androidx.compose.animation.Crossfade(
            targetState = selectedTabIndex,
            modifier = Modifier.fillMaxSize(),
            label = "Tab transition"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> InvoiceDetailsTab(
                    invoice = invoice,
                    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
                )
                1 -> InvoiceItemsTab(
                    invoice = invoice,
                    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
                )
                2 -> PaymentHistoryTab(
                    invoice = invoice,
                    businessId = businessId,
                    modifier = Modifier.fillMaxSize()
                )
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

    Column(
        modifier = modifier,
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
    Column(
        modifier = modifier,
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
    PaymentHistoryScreen(
        invoiceId = invoice.invoice.id,
        businessId = businessId,
        modifier = modifier.fillMaxWidth()
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
