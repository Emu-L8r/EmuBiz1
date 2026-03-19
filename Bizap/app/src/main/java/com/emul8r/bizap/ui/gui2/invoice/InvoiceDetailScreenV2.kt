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
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.ui.gui2.common.*
import com.emul8r.bizap.ui.gui2.invoices.RecordPaymentDialogV2
import com.emul8r.bizap.ui.gui2.invoices.StatusUpdateMenuV2
import com.emul8r.bizap.ui.invoices.InvoiceDetailUiState
import com.emul8r.bizap.ui.invoices.InvoiceDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

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
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

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
                    if (uiState is InvoiceDetailUiState.Success) {
                        IconButton(onClick = { showPaymentDialog = true }) {
                            Icon(Icons.Default.Payment, contentDescription = "Record Payment")
                        }
                        IconButton(onClick = { showStatusMenu = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Update Status")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is InvoiceDetailUiState.Loading -> LoadingIndicatorV2(
                modifier = Modifier.padding(paddingValues)
            )
            is InvoiceDetailUiState.Error -> ErrorStateV2(
                message = state.message,
                modifier = Modifier.padding(paddingValues)
            )
            is InvoiceDetailUiState.Success -> {
                InvoiceDetailContentV2(
                    invoice = state.data,
                    modifier = Modifier.padding(paddingValues)
                )

                // Payment Dialog
                if (showPaymentDialog) {
                    RecordPaymentDialogV2(
                        invoiceId = state.data.id,
                        businessId = businessId,
                        invoiceTotal = state.data.totalAmount,
                        amountPaid = state.data.amountPaid,
                        invoiceDate = state.data.date,
                        invoiceStatus = state.data.status,
                        onDismiss = { showPaymentDialog = false },
                        onSuccess = { showPaymentDialog = false }
                    )
                }

                // Status Update Menu
                if (showStatusMenu) {
                    StatusUpdateMenuV2(
                        currentStatus = state.data.status,
                        onStatusSelected = { status: InvoiceStatus ->
                            viewModel.updateInvoiceStatus(status)
                            showStatusMenu = false
                        },
                        onDismiss = { showStatusMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetailContentV2(
    invoice: Invoice,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeaderV2("Invoice Info")

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRowV2("Customer", invoice.customerName)
                DetailRowV2("Status", invoice.status.name)
                DetailRowV2("Date", dateFormatter.format(Date(invoice.date)))
                if (invoice.dueDate > 0) {
                    DetailRowV2("Due Date", dateFormatter.format(Date(invoice.dueDate)))
                }
                DetailRowV2("Total", formatCents(invoice.totalAmount))
                DetailRowV2("Amount Paid", formatCents(invoice.amountPaid))
                DetailRowV2("Outstanding", formatCents(invoice.totalAmount - invoice.amountPaid))
                DetailRowV2("Currency", invoice.currencyCode)
            }
        }

        if (invoice.items.isNotEmpty()) {
            HorizontalDivider()
            SectionHeaderV2("Line Items")
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
                                text = "Qty: ${item.quantity}  ×  ${formatCents(item.unitPrice)}",
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

        invoice.notes?.let { notes ->
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
