package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceLineItem
import com.emul8r.bizap.domain.model.InvoiceStatus
import timber.log.Timber

/**
 * GUI2 Create Invoice Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreenV2(
    businessId: Long,
    onCreate: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateInvoiceViewModelV2 = hiltViewModel()
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var totalAmount by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000) }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var totalError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Customer selection
            OutlinedTextField(
                value = selectedCustomer?.name ?: "Select Customer",
                onValueChange = {},
                label = { Text("Customer") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            // Total Amount
            OutlinedTextField(
                value = totalAmount,
                onValueChange = {
                    totalAmount = it
                    totalError = null
                },
                label = { Text("Total Amount ($) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = totalError != null,
                supportingText = totalError?.let { { Text(it) } }
            )

            // Invoice Date
            OutlinedTextField(
                value = java.time.Instant.ofEpochMilli(invoiceDate)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().toString(),
                onValueChange = {},
                label = { Text("Invoice Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            // Due Date
            OutlinedTextField(
                value = java.time.Instant.ofEpochMilli(dueDate)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().toString(),
                onValueChange = {},
                label = { Text("Due Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (totalAmount.isBlank()) {
                        totalError = "Amount is required"
                        return@Button
                    }

                    val amountCents = (totalAmount.toDoubleOrNull() ?: 0.0) * 100
                    if (amountCents <= 0) {
                        totalError = "Amount must be greater than 0"
                        return@Button
                    }

                    isSaving = true
                    viewModel.createInvoice(
                        invoice = Invoice(
                            id = 0,
                            businessProfileId = businessId,
                            customerId = selectedCustomer?.id ?: 0,
                            customerName = selectedCustomer?.name ?: "Unknown",
                            customerAddress = selectedCustomer?.address ?: "",
                            customerEmail = selectedCustomer?.email ?: "",
                            items = emptyList(),
                            totalAmount = amountCents.toLong(),
                            amountPaid = 0L,
                            status = InvoiceStatus.DRAFT,
                            date = invoiceDate,
                            dueDate = dueDate,
                            isQuote = false,
                            currencyCode = "AUD",
                            taxRate = 0.0,
                            taxAmount = 0L,
                            invoiceYear = java.time.Instant.ofEpochMilli(invoiceDate)
                                .atZone(java.time.ZoneId.systemDefault())
                                .year,
                            invoiceSequence = 0,
                            notes = notes
                        ),
                        onSuccess = {
                            onCreate()
                        },
                        onError = {
                            isSaving = false
                            Timber.e("Failed to create invoice: $it")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Invoice")
                }
            }
        }
    }
}

