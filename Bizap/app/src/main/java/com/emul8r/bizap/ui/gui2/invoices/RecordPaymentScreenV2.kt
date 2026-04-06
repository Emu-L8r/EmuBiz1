package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.R
import com.emul8r.bizap.data.local.entities.PaymentMethod
import com.emul8r.bizap.presentation.viewmodel.PaymentRecordingViewModel

/**
 * Record Payment Screen - UI layer for recording invoice payments.
 *
 * **Responsibilities (UI Layer Only):**
 * - Render form fields
 * - Display validation errors from ViewModel
 * - Show/hide loading indicator
 * - Navigate back on success
 *
 * **NO Business Logic Here:**
 * - Amount validation → ViewModel
 * - Payment persistence → ViewModel
 * - Error state → ViewModel
 *
 * All user actions are delegated to [PaymentRecordingViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentScreenV2(
    invoiceId: Long,
    invoiceAmount: Long = 0L,
    onBack: () -> Unit,
    viewModel: PaymentRecordingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate back on successful payment recording
    LaunchedEffect(uiState.paymentRecorded) {
        if (uiState.paymentRecorded) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.payment_record_title)) },
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
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Invoice amount info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(id = R.string.payment_invoice_total),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${invoiceAmount / 100}.${(invoiceAmount % 100).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            // Payment amount input - delegates to ViewModel
            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text(stringResource(id = R.string.payment_amount)) },
                placeholder = { Text(stringResource(id = R.string.payment_amount_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null
            )

            // Payment method dropdown
            var expandedMethod by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedMethod,
                onExpandedChange = { expandedMethod = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedMethod.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.payment_method)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethod) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedMethod,
                    onDismissRequest = { expandedMethod = false }
                ) {
                    // Use PaymentMethod.entries (Kotlin 1.9+) instead of .values()
                    PaymentMethod.entries.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.displayName) },
                            onClick = {
                                viewModel.updatePaymentMethod(method)
                                expandedMethod = false
                            }
                        )
                    }
                }
            }

            // Notes input - delegates to ViewModel
            OutlinedTextField(
                value = uiState.notesInput,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text(stringResource(id = R.string.payment_notes)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            // Error message display
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button - delegates to ViewModel, NO validation here
            Button(
                onClick = { viewModel.recordPayment(invoiceId) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(id = R.string.payment_record_button))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

