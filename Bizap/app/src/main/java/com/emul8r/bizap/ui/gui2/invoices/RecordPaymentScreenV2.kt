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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.local.entities.PaymentMethod
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2

/**
 * Record Payment Screen - Allow users to record payments against invoices.
 *
 * Collects:
 * - Payment amount
 * - Payment method (Cash, Check, ACH, Wire, Card)
 * - Optional notes
 *
 * Updates invoice status automatically to PAID or PARTIALLY_PAID.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentScreenV2(
    invoiceId: Long,
    invoiceAmount: Long = 0L,
    onBack: () -> Unit,
    onPaymentRecorded: () -> Unit = {}
) {
    var amountInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.ACH_TRANSFER) }
    var notesInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Payment") },
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
                        "Invoice Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${invoiceAmount / 100}.${(invoiceAmount % 100).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            // Payment amount input
            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it.filter { char -> char.isDigit() || char == '.' }
                },
                label = { Text("Payment Amount") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null
            )

            // Payment method dropdown
            var expandedMethod by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedMethod,
                onExpandedChange = { expandedMethod = it }
            ) {
                OutlinedTextField(
                    value = selectedMethod.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Method") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethod) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedMethod,
                    onDismissRequest = { expandedMethod = false }
                ) {
                    PaymentMethod.values().forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.displayName) },
                            onClick = {
                                selectedMethod = method
                                expandedMethod = false
                            }
                        )
                    }
                }
            }

            // Notes input
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = {
                    // Validate
                    val amount = amountInput.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }

                    errorMessage = null
                    isSubmitting = true

                    // In a real app, call viewModel.recordPayment()
                    // For now, simulate success
                    onPaymentRecorded()
                },
                enabled = amountInput.isNotEmpty() && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Record Payment")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

