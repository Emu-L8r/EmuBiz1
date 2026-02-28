package com.emu.emubizwax.ui.invoices

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.math.BigDecimal

@Composable
fun InvoiceDetailsScreen(
    viewModel: InvoiceDetailsViewModel = hiltViewModel()
) {
    val invoiceDetails by viewModel.invoiceState.collectAsState()
    val pdfState by viewModel.pdfGenerationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pdfState) {
        if (pdfState is PdfGenerationUiState.Error) {
            snackbarHostState.showSnackbar((pdfState as PdfGenerationUiState.Error).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.generateAndSharePdf() },
                icon = {
                    if (pdfState is PdfGenerationUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Share, "Share Invoice")
                    }
                },
                text = { Text(if (pdfState is PdfGenerationUiState.Loading) "Generating..." else "Generate & Share") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
             if (invoiceDetails == null) {
                CircularProgressIndicator()
            } else {
                val invoice = invoiceDetails!!.invoice.invoice
                val subtotal = invoice.subtotal
                val taxAmount = subtotal.multiply(BigDecimal(invoice.taxRate))
                val grandTotal = invoice.totalAmount

                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    item {
                        Text("Invoice Details", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Customer", style = MaterialTheme.typography.titleMedium)
                                Text(invoiceDetails!!.customer.name, fontWeight = FontWeight.Bold)
                                Text(invoiceDetails!!.customer.email)
                                Text(invoiceDetails!!.customer.phone)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Text("Items", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(invoiceDetails!!.invoice.items) { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.description)
                            Text("$${item.unitPrice} x ${item.quantity}")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            Text("Subtotal: $${String.format("%.2f", subtotal)}")
                            Text("Tax (${(invoice.taxRate * 100).toInt()}%): $${String.format("%.2f", taxAmount)}")
                            Text("Grand Total: $${String.format("%.2f", grandTotal)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
