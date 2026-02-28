package com.example.databaser.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.databaser.R
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPreviewScreen(
    invoiceId: Int,
    invoiceViewModel: InvoiceViewModel,
    businessInfoViewModel: BusinessInfoViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val invoiceWithLineItems by invoiceViewModel.getInvoiceById(invoiceId).collectAsStateWithLifecycle(initialValue = null)
    val businessInfo by businessInfoViewModel.businessInfo.collectAsStateWithLifecycle(initialValue = null)
    var showPdfSavedDialog by remember { mutableStateOf<Uri?>(null) }
    val photoUris by invoiceViewModel.photoUris.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        invoiceViewModel.pdfGenerated.collect { uri ->
            if (uri != null) {
                invoiceWithLineItems?.invoice?.let { invoiceViewModel.markInvoiceAsSent(it.id) }
            }
            showPdfSavedDialog = uri
        }
    }

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
            val invoice = invoiceWithLineItems!!.invoice
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                item { 
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                                businessInfo?.logoPath?.let {
                                    AsyncImage(
                                        model = it,
                                        contentDescription = "Company Logo",
                                        modifier = Modifier.size(100.dp).padding(end = 16.dp)
                                    )
                                }

                                if (businessInfo?.logoPath == null) {
                                    Image(
                                        painter = painterResource(id = R.drawable.thswalogo),
                                        contentDescription = "Company Logo",
                                        modifier = Modifier.size(100.dp).padding(end = 16.dp)
                                    )
                                }

                                Column {
                                    Text(businessInfo!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(businessInfo!!.address)
                                    Text(businessInfo!!.contactNumber)
                                    Text(businessInfo!!.email ?: "")
                                }


                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(id = R.string.invoice_to),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(customer.name, fontWeight = FontWeight.Bold)
                                    Text(customer.address)
                                    Text(customer.contactNumber)
                                    Text(customer.email)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Invoice #: ${invoice.invoiceNumber}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Date: ${dateFormat.format(Date(invoice.date))}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Due Date: ${dateFormat.format(Date(invoice.dueDate))}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (invoice.header.isNotEmpty()) {
                        Text(invoice.header, style = MaterialTheme.typography.titleLarge)
                    }
                    if (invoice.subHeader.isNotEmpty()) {
                        Text(invoice.subHeader, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Table Header
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(
                            stringResource(id = R.string.description),
                            modifier = Modifier.weight(1.5f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(id = R.string.qty),
                            modifier = Modifier.weight(0.5f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(id = R.string.unit_price),
                            modifier = Modifier.weight(0.7f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(id = R.string.total),
                            modifier = Modifier.weight(0.7f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()
                }
                items(invoiceWithLineItems!!.lineItems) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(it.job, fontWeight = FontWeight.Bold)
                            Text(it.details, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(it.quantity.toString(), modifier = Modifier.weight(0.5f))
                        Text("$${it.unitPrice}", modifier = Modifier.weight(0.7f))
                        Text("$${it.quantity * it.unitPrice}", modifier = Modifier.weight(0.7f))
                    }
                    HorizontalDivider()
                }
                item {
                    LazyRow {
                        val allPhotos = (invoice.photoUris.map { Uri.parse(it) } + photoUris).filterNotNull().distinct()
                        items(allPhotos) { uri ->
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
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    val totalAmount = invoiceWithLineItems!!.lineItems.sumOf { it.quantity * it.unitPrice }
                    Text(
                        "Total: $${totalAmount}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Footer
                    if (invoice.footer.isNotEmpty()) {
                        Text(text = invoice.footer, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(16.dp))

                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        ActionButton(icon = Icons.Default.Print, text = stringResource(id = R.string.print_to_pdf)) {
                            invoiceViewModel.generatePdf(context, invoiceWithLineItems!!, customer.name, businessInfo!!, false, invoice.header, invoice.subHeader, invoice.footer, photoUris)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        ActionButton(icon = Icons.Default.PictureAsPdf, text = stringResource(id = R.string.generate_quote)) {
                            invoiceViewModel.generatePdf(context, invoiceWithLineItems!!, customer.name, businessInfo!!, true, invoice.header, invoice.subHeader, invoice.footer, photoUris)
                        }
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

@Composable
fun ActionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text)
        }
    }
}
