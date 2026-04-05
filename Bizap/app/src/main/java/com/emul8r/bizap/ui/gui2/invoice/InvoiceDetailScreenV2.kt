package com.emul8r.bizap.ui.gui2.invoice

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.FolderOpen
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
 * GUI2 invoice detail screen.
 * Displays a read-only view of a single invoice's details and line items.
 *
 * STATE MANAGEMENT: All dialog state is managed by the ViewModel.
 * Composable is a pure presenter layer - only reads state and calls ViewModel methods.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreenV2(
    businessId: Long,
    invoiceId: Long,
    onBack: () -> Unit,
    onNavigateToVault: (() -> Unit)? = null,
    viewModel: InvoiceDetailViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                        IconButton(onClick = { viewModel.openPdfExport() }) {
                            Icon(Icons.Default.GetApp, contentDescription = "Export PDF")
                        }
                        IconButton(onClick = { viewModel.openPaymentDialog() }) {
                            Icon(Icons.Default.Payment, contentDescription = "Record Payment")
                        }
                        IconButton(onClick = { viewModel.openStatusMenu() }) {
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

                // ===== PAYMENT DIALOG =====
                if (state.dialogState is DialogState.PaymentDialog) {
                    RecordPaymentDialogV2(
                        invoiceId = state.invoice.invoice.id,
                        businessId = businessId,
                        invoiceTotal = state.invoice.invoice.totalAmount,
                        amountPaid = state.invoice.invoice.amountPaid,
                        invoiceDate = state.invoice.invoice.date,
                        invoiceStatus = currentStatus,
                        isLoading = state.paymentLoading,
                        error = state.paymentError,
                        onDismiss = { viewModel.closeDialog() },
                        onSuccess = { amount -> viewModel.recordPayment(amount) }
                    )
                }

                // ===== STATUS UPDATE DIALOG =====
                if (state.dialogState is DialogState.StatusMenu) {
                    StatusUpdateMenuV2(
                        currentStatus = currentStatus,
                        error = state.statusUpdateError,
                        onStatusSelected = { status: InvoiceStatus ->
                            viewModel.updateInvoiceStatus(status)
                        },
                        onDismiss = { viewModel.closeDialog() }
                    )
                }

                // ===== PDF EXPORT DIALOGS =====
                when (state.dialogState) {
                    is DialogState.PdfExport.Loading -> {
                        AlertDialog(
                            onDismissRequest = { /* Don't allow dismiss during loading */ },
                            confirmButton = { /* No action during loading */ },
                            title = { Text("📄 Generating PDF") },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator()
                                    Text(
                                        "Creating professional invoice PDF with selected styling...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "This may take a few seconds. Please wait.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                    is DialogState.PdfExport.Success -> {
                        val file = (state.dialogState as DialogState.PdfExport.Success).file
                        AlertDialog(
                            onDismissRequest = { viewModel.closeDialog() },
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.closeDialog()
                                    onNavigateToVault?.invoke()
                                }) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Go to Vault")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { viewModel.closeDialog() }) {
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
                                    Text("Tap 'Go to Vault' to view your PDFs immediately.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        )
                    }
                    is DialogState.PdfExport.Error -> {
                        AlertDialog(
                            onDismissRequest = { viewModel.closeDialog() },
                            confirmButton = {
                                Button(onClick = { viewModel.closeDialog() }) {
                                    Text("OK")
                                }
                            },
                            title = { Text("❌ Export Failed") },
                            text = { Text((state.dialogState as DialogState.PdfExport.Error).message) }
                        )
                    }
                    else -> {} // No PDF dialog
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

    // ✅ FLICKER FIX: Stabilize animation state to prevent unnecessary recompositions
    val animatedTabIndex = rememberUpdatedState(selectedTabIndex)

    Column(modifier = modifier.fillMaxSize()) {
        // ✅ FLICKER FIX: Tab bar with optimized state management
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        // ✅ FLICKER FIX: Debounce rapid tab clicks to prevent animation conflicts
                        if (selectedTabIndex != index) {
                            selectedTabIndex = index
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        // ✅ FLICKER FIX: Optimized Crossfade animation
        // Optimizations:
        // 1. Reduced duration to 150ms (faster = less flicker perception)
        // 2. Use rememberUpdatedState for stable animation state
        // 3. key() boundary for proper recomposition
        // 4. Box wrappers for composition stability
        key(animatedTabIndex.value) {
            Crossfade(
                targetState = animatedTabIndex.value,
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutLinearInEasing
                ),
                modifier = Modifier.fillMaxSize(),
                label = "Tab transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> Box(modifier = Modifier.fillMaxSize()) {
                        InvoiceDetailsTab(
                            invoice = invoice,
                            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
                        )
                    }
                    1 -> Box(modifier = Modifier.fillMaxSize()) {
                        InvoiceItemsTab(
                            invoice = invoice,
                            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
                        )
                    }
                    2 -> Box(modifier = Modifier.fillMaxSize()) {
                        PaymentHistoryTab(
                            invoice = invoice,
                            businessId = businessId,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
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
