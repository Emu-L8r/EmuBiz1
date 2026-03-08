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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.ui.invoices.CustomerDropdown
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
    // Observe ViewModel data
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()

    // ...existing code...
    var totalAmount by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000) }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var totalError by remember { mutableStateOf<String?>(null) }
    var customerError by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
            // Customer selection - Replace static TextField with interactive dropdown
            CustomerDropdown(
                selectedCustomer = selectedCustomer,
                customers = customers,
                onSelect = {
                    viewModel.selectCustomer(it)
                    customerError = null // Clear error when customer is selected
                }
            )

            // Customer error message
            if (customerError != null) {
                Text(
                    text = customerError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

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
                    // Reset errors
                    totalError = null
                    customerError = null

                    // Validate customer selection
                    if (selectedCustomer == null) {
                        customerError = "Please select a customer"
                        Timber.w("Invoice creation failed: No customer selected")
                        return@Button
                    }

                    // Validate amount
                    if (totalAmount.isBlank()) {
                        totalError = "Amount is required"
                        Timber.w("Invoice creation failed: Amount is blank")
                        return@Button
                    }

                    // Parse and validate amount
                    val amount = totalAmount.toDoubleOrNull()
                    if (amount == null) {
                        totalError = "Invalid amount format"
                        Timber.w("Invoice creation failed: Invalid amount format - '$totalAmount'")
                        return@Button
                    }

                    val amountCents = (amount * 100).toLong()
                    if (amountCents <= 0) {
                        totalError = "Amount must be greater than $0.00"
                        Timber.w("Invoice creation failed: Amount not positive - $amount")
                        return@Button
                    }

                    // Validate due date is after invoice date
                    if (dueDate < invoiceDate) {
                        errorMessage = "Due date cannot be before invoice date"
                        showErrorDialog = true
                        Timber.w("Invoice creation failed: Invalid dates")
                        return@Button
                    }

                    isSaving = true
                    try {
                        // Store selectedCustomer in local variable to avoid smart cast issues
                        val customer = selectedCustomer
                        if (customer != null) {
                            viewModel.createInvoice(
                                invoice = Invoice(
                                    id = 0,
                                    businessProfileId = businessId,
                                    customerId = customer.id,
                                    customerName = customer.name,
                                    customerAddress = customer.address ?: "",
                                    customerEmail = customer.email ?: "",
                                    items = emptyList(),
                                    totalAmount = amountCents,
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
                                    Timber.d("Invoice created successfully for customer: ${customer.name}")
                                    onCreate()
                                },
                                onError = { error ->
                                    isSaving = false
                                    errorMessage = error ?: "Failed to create invoice. Please try again."
                                    showErrorDialog = true
                                    Timber.e("Failed to create invoice: $error")
                                }
                            )
                        } else {
                            isSaving = false
                            customerError = "Please select a customer"
                        }
                    } catch (e: Exception) {
                        isSaving = false
                        errorMessage = "Unexpected error: ${e.message ?: "Unknown error"}"
                        showErrorDialog = true
                        Timber.e(e, "Exception during invoice creation")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && selectedCustomer != null
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

            // Error Dialog
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    title = { Text("Error") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        Button(onClick = { showErrorDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

