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
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2

/**
 * GUI2 Edit Invoice Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceScreenV2(
    businessId: Long,
    invoiceId: Long,
    onUpdate: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditInvoiceViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is EditInvoiceUiStateV2.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is EditInvoiceUiStateV2.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is EditInvoiceUiStateV2.Success -> {
                EditInvoiceForm(
                    initialInvoice = state.invoice,
                    onSave = { invoice ->
                        viewModel.updateInvoice(invoice, onUpdate)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EditInvoiceForm(
    initialInvoice: Invoice,
    onSave: (Invoice) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalAmount by remember(initialInvoice) {
        mutableStateOf((initialInvoice.totalAmount / 100.0).toString())
    }
    var notes by remember(initialInvoice) { mutableStateOf(initialInvoice.notes ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Customer (read-only)
        OutlinedTextField(
            value = initialInvoice.customerName,
            onValueChange = {},
            label = { Text("Customer") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        // Total Amount
        OutlinedTextField(
            value = totalAmount,
            onValueChange = { totalAmount = it },
            label = { Text("Total Amount ($)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Invoice Date (read-only)
        OutlinedTextField(
            value = try {
                java.time.Instant.parse(initialInvoice.dateCreated)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().toString()
            } catch (e: Exception) {
                initialInvoice.dateCreated
            },
            onValueChange = {},
            label = { Text("Invoice Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        // Due Date (read-only)
        OutlinedTextField(
            value = try {
                java.time.Instant.parse(initialInvoice.dueDate)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().toString()
            } catch (e: Exception) {
                initialInvoice.dueDate
            },
            onValueChange = {},
            label = { Text("Due Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        // Status (read-only)
        OutlinedTextField(
            value = initialInvoice.status.toString(),
            onValueChange = {},
            label = { Text("Status") },
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
                val newAmount = (totalAmount.toDoubleOrNull() ?: 0.0) * 100
                isSaving = true
                onSave(
                    initialInvoice.copy(
                        totalAmount = newAmount.toLong(),
                        notes = notes
                    )
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
                Text("Save Changes")
            }
        }
    }
}

sealed interface EditInvoiceUiStateV2 {
    object Loading : EditInvoiceUiStateV2
    data class Error(val message: String) : EditInvoiceUiStateV2
    data class Success(val invoice: Invoice) : EditInvoiceUiStateV2
}

