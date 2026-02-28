package com.example.databaser.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.databaser.R
import com.example.databaser.utils.PdfGenerator
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPreviewScreen(
    invoiceId: Int,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModel.Factory),
    businessInfoViewModel: BusinessInfoViewModel = viewModel(factory = BusinessInfoViewModel.Factory),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val invoiceWithLineItems by invoiceViewModel.getInvoiceById(invoiceId).collectAsStateWithLifecycle(initialValue = null)
    val businessInfo by businessInfoViewModel.businessInfo.collectAsStateWithLifecycle(initialValue = null)
    var showPdfSavedDialog by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.print_preview)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        val isLoading = invoiceWithLineItems == null || businessInfo == null
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val dateFormat = remember(businessInfo?.dateFormat) {
                try {
                    SimpleDateFormat(businessInfo?.dateFormat ?: "dd/MM/yyyy", Locale.getDefault())
                } catch (e: IllegalArgumentException) {
                    Log.e("PrintPreviewScreen", "Invalid date format provided: ${businessInfo?.dateFormat}. Using default.", e)
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                }
            }

            val customer = invoiceWithLineItems!!.customer
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(id = R.string.invoice_from),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(businessInfo!!.name, style = MaterialTheme.typography.titleLarge)
                                Text(businessInfo!!.address)
                                Text(businessInfo!!.contactNumber)
                                Text(businessInfo!!.email ?: "")
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    stringResource(id = R.string.invoice_to),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(customer.name)
                                Text(customer.address)
                                Text(customer.contactNumber)
                                Text(customer.email)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(id = R.string.invoice_details),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Invoice #: ${invoiceWithLineItems!!.invoice.invoiceNumber}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Date: ${dateFormat.format(Date(invoiceWithLineItems!!.invoice.date))}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Due Date: ${dateFormat.format(Date(invoiceWithLineItems!!.invoice.dueDate))}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                invoiceWithLineItems!!.invoice.header?.let {
                    Text(it, style = MaterialTheme.typography.titleLarge)
                }
                invoiceWithLineItems!!.invoice.subHeader?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text(
                        stringResource(id = R.string.description),
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(id = R.string.qty),
                        modifier = Modifier.weight(0.2f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(id = R.string.unit_price),
                        modifier = Modifier.weight(0.4f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(id = R.string.total),
                        modifier = Modifier.weight(0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(invoiceWithLineItems!!.lineItems) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(it.job, fontWeight = FontWeight.Bold)
                                Text(it.details)
                            }
                            Text(it.quantity.toString(), modifier = Modifier.weight(0.2f))
                            Text("$${it.unitPrice}", modifier = Modifier.weight(0.4f))
                            Text("$${it.quantity * it.unitPrice}", modifier = Modifier.weight(0.4f))
                        }
                        HorizontalDivider()
                    }
                    item {
                        LazyRow {
                            items(invoiceWithLineItems!!.invoice.photoUris) { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected photo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val totalAmount = invoiceWithLineItems!!.lineItems.sumOf { it.quantity * it.unitPrice }
                Text(
                    "Total: $${totalAmount}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val uri = PdfGenerator.createInvoicePdf(
                                    context,
                                    invoiceWithLineItems!!,
                                    customer.name,
                                    businessInfo!!,
                                    false,
                                    invoiceViewModel
                                )
                                if (uri != null) {
                                    invoiceViewModel.markInvoiceAsSent(invoiceWithLineItems!!.invoice.id)
                                }
                                showPdfSavedDialog = uri
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.print_to_pdf))
                    }
                    Spacer(modifier = Modifier.weight(0.1f))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val newQuoteId = invoiceViewModel.createQuoteFromInvoice(
                                    invoiceWithLineItems!!.invoice,
                                    invoiceWithLineItems!!.lineItems
                                )
                                if (newQuoteId != null) {
                                    val newQuote = invoiceViewModel.getInvoiceById(newQuoteId.toInt()).first()
                                    if (newQuote != null) {
                                        val uri = PdfGenerator.createInvoicePdf(
                                            context,
                                            newQuote,
                                            newQuote.customer.name,
                                            businessInfo!!,
                                            true,
                                            invoiceViewModel
                                        )
                                        showPdfSavedDialog = uri
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.generate_quote))
                    }
                }
            }
        }
    }

    if (showPdfSavedDialog != null) {
        AlertDialog(
            onDismissRequest = { showPdfSavedDialog = null },
            title = { Text(stringResource(id = R.string.pdf_saved)) },
            text = { Text(stringResource(id = R.string.pdf_saved_to_downloads_view)) },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(showPdfSavedDialog, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                    showPdfSavedDialog = null
                }) {
                    Text(stringResource(id = R.string.view))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfSavedDialog = null }) {
                    Text(stringResource(id = R.string.dismiss))
                }
            }
        )
    }
}
