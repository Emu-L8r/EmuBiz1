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
import com.emul8r.bizap.ui.components.OverwritePdfDialog
import com.emul8r.bizap.ui.invoices.components.InvoiceActionHub
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.text.SimpleDateFormat
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
    var showActionHub by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInvoice(invoiceId)
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
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
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Invoice Details") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showActionHub = true },
                icon = { Icon(Icons.Default.IosShare, contentDescription = null) },
                text = { Text("Export") }
            )
        }
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
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        InvoiceStatusBanner(invoice.status.name)
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("DRAFT") }, onClick = { 
                                viewModel.updateStatus(invoiceId, "DRAFT")
                                expanded = false 
                            })
                            DropdownMenuItem(text = { Text("SENT") }, onClick = { 
                                viewModel.updateStatus(invoiceId, "SENT") 
                                expanded = false
                            })
                            DropdownMenuItem(text = { Text("PAID") }, onClick = { 
                                viewModel.updateStatus(invoiceId, "PAID") 
                                expanded = false
                            })
                        }
                    }

                    if (!invoice.header.isNullOrBlank()) {
                        Section(title = "Header", content = invoice.header)
                    }

                    if (!invoice.subheader.isNullOrBlank()) {
                        Section(title = "Subheader", content = invoice.subheader)
                    }

                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Invoice #${invoice.invoiceId}", style = MaterialTheme.typography.headlineSmall)
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

                    if (!invoice.notes.isNullOrBlank()) {
                        Section(title = "Notes", content = invoice.notes)
                    }

                    if (!invoice.footer.isNullOrBlank()) {
                        Section(title = "Footer", content = invoice.footer)
                    }

                    invoice.pdfUri?.let { pdfUri ->
                        OutlinedButton(onClick = { 
                            val file = File(pdfUri)
                            val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Text("View Saved PDF")
                        }
                    }

                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Invoice")
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Invoice")
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Invoice") },
                            text = { Text("Are you sure you want to delete this invoice? This action cannot be undone.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.deleteInvoice(invoice.id)
                                        showDeleteDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }

        // Overwrite PDF Dialog
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

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
