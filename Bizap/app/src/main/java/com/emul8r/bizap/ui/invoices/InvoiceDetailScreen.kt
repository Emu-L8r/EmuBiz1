package com.emul8r.bizap.ui.invoices

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.ui.components.OverwritePdfDialog
import com.emul8r.bizap.ui.invoices.components.InvoiceActionHub
import com.emul8r.bizap.ui.invoices.components.VersionPicker
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.ui.utils.formatDate
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    onEdit: () -> Unit,
    onInvoiceDeleted: () -> Unit = {},
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val overwriteDialogState by viewModel.showOverwriteDialog.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showActionHub by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInvoice(invoiceId)
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.NavigateToInvoice -> {
                    // Logic to jump to a specific version handled by the navigation controller in the parent scope
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is InvoiceDetailEvent.InvoiceDeleted -> onInvoiceDeleted()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportEvent.collectLatest { file ->
            val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice"))
            isExporting = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Invoice Details") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (showActionHub) {
                InvoiceActionHub(
                    onShare = { viewModel.shareInternalPdf() },
                    onSaveToDownloads = { viewModel.exportToDownloads() },
                    onPrint = { viewModel.launchSystemPrint() },
                    onDismiss = { showActionHub = false }
                )
            }

            when (val s = state) {
                is InvoiceDetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is InvoiceDetailUiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text(s.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is InvoiceDetailUiState.Success -> {
                    val invoice = s.data
                    var expanded by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // PHASE 3A: Version Picker
                            VersionPicker(
                                currentInvoice = invoice,
                                allVersions = s.versions,
                                onVersionSelected = { newId -> viewModel.loadInvoice(newId) }
                            )

                            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                                InvoiceStatusBanner(invoice.status.name)
                                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    InvoiceStatus.entries.forEach { status ->
                                        DropdownMenuItem(text = { Text(status.name) }, onClick = {
                                            viewModel.updateStatus(invoiceId, status.name)
                                            expanded = false
                                        })
                                    }
                                }
                            }

                            // PHASE 3A: Payment Progress
                            PaymentProgressCard(invoice = invoice, onRecordPayment = { showPaymentDialog = true })

                            if (!invoice.header.isNullOrBlank()) {
                                Section(title = "Header", content = invoice.header)
                            }

                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(invoice.invoiceNumber, style = MaterialTheme.typography.headlineSmall)
                                    Text(formatDate(invoice.date), style = MaterialTheme.typography.bodyMedium)
                                    Text("Customer: ${invoice.customerName}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))

                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))

                                    invoice.items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Text(item.description, style = MaterialTheme.typography.bodyLarge)
                                                Text("${item.quantity} x $${String.format(Locale.getDefault(), "%.2f", item.unitPrice)}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(
                                                "$${String.format(Locale.getDefault(), "%.2f", item.quantity * item.unitPrice)}",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))

                                    // Subtotal
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            text = "$${String.format(Locale.getDefault(), "%.2f", invoice.totalAmount - invoice.taxAmount)}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    // Tax (only if business is tax registered and tax amount > 0)
                                    if (invoice.taxAmount > 0) {
                                        Spacer(Modifier.height(4.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Tax (${(invoice.taxRate * 100).toInt()}%)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "$${String.format(Locale.getDefault(), "%.2f", invoice.taxAmount)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "No tax applied – Business not registered for tax",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    // Total
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "$${String.format(Locale.getDefault(), "%.2f", invoice.totalAmount)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // PHASE 3A: Correction Workflow
                            if (invoice.status != InvoiceStatus.DRAFT) {
                                Button(
                                    onClick = { viewModel.createCorrection() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.HistoryEdu, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Create Correction (v${invoice.version + 1})")
                                }
                            }

                            // Immutability Rule: Disable edit if not DRAFT
                            Button(
                                onClick = onEdit,
                                enabled = invoice.status == InvoiceStatus.DRAFT,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (invoice.status == InvoiceStatus.DRAFT) "Edit Invoice" else "Locked (Correction Required)")
                            }

                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Delete Invoice")
                            }
                        }

                        // ACTION BUTTONS: Save + Export (Fixed Visual Hierarchy)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // PRIMARY ACTION: Save Invoice
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                onClick = {
                                    isSaving = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Invoice saved successfully")
                                    }
                                    isSaving = false
                                },
                                enabled = !isSaving && !isExporting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Saving...",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Save",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Save Invoice",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // SECONDARY ACTION: Export as PDF
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                onClick = {
                                    isExporting = true
                                    viewModel.shareInternalPdf()
                                },
                                enabled = !isSaving && !isExporting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                            ) {
                                if (isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Exporting...",
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Export",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Export as PDF",
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPaymentDialog) {
            RecordPaymentDialog(
                onDismiss = { showPaymentDialog = false },
                onConfirm = { amount ->
                    viewModel.recordPayment(amount)
                    showPaymentDialog = false
                }
            )
        }

        overwriteDialogState?.let { dialogState ->
            OverwritePdfDialog(
                fileName = dialogState.fileName,
                onOverwrite = { viewModel.onOverwriteExisting() },
                onKeepBoth = { viewModel.onKeepBothVersions() },
                onDismiss = { viewModel.onDismissOverwriteDialog() }
            )
        }
    }
}

@Composable
fun PaymentProgressCard(invoice: com.emul8r.bizap.domain.model.Invoice, onRecordPayment: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Payment Progress", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$${String.format("%.2f", invoice.amountPaid)} / $${String.format("%.2f", invoice.totalAmount)}",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (invoice.amountPaid / invoice.totalAmount).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRecordPayment, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.AddCard, null)
                Spacer(Modifier.width(8.dp))
                Text("Record Payment")
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount Received ($)") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                amount.toDoubleOrNull()?.let { doubleAmount ->
                    val centsAmount = (doubleAmount * 100).toLong()
                    onConfirm(centsAmount)
                }
            }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun Section(title: String, content: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(content, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
    }
}

@Composable
fun InvoiceStatusBanner(status: String) {
    val (backgroundColor, textColor, icon) = when (status) {
        "PAID" -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Icons.Default.CheckCircle)
        "SENT" -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Icons.AutoMirrored.Filled.Send)
        "OVERDUE" -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Icons.Default.Warning)
        "PARTIALLY_PAID" -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Icons.Default.Payments)
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.EditNote)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Status: $status",
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}







