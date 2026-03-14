package com.emul8r.bizap.ui.gui2.invoices

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.gui2.common.formatCents
import java.text.SimpleDateFormat
import java.util.*

/**
 * GUI2 Record Payment Dialog (Phase 3)
 *
 * Features:
 *  - Prominent outstanding balance display
 *  - Amount input with real-time validation
 *  - Date picker (≤ today, ≥ invoice date)
 *  - Optional notes field (max 500 chars)
 *  - Record button disabled until form is valid
 *  - Field-level error messages in red
 *  - Loading indicator during submission
 */
@Composable
fun RecordPaymentDialogV2(
    invoiceId: Long,
    businessId: Long,
    invoiceTotal: Long,
    amountPaid: Long,
    invoiceDate: Long,
    invoiceStatus: com.emul8r.bizap.domain.model.InvoiceStatus,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: RecordPaymentViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Initialise the ViewModel with invoice context (idempotent)
    LaunchedEffect(invoiceId) {
        viewModel.initFor(
            invoiceId = invoiceId,
            businessId = businessId,
            invoiceTotal = invoiceTotal,
            amountPaid = amountPaid,
            invoiceDate = invoiceDate,
            invoiceStatus = invoiceStatus
        )
    }

    // Collect one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentEvent.Success -> {
                    onSuccess()
                    onDismiss()
                }
                is PaymentEvent.Error -> { /* error is already shown in formState */ }
            }
        }
    }

    val isFullyPaid = (invoiceTotal - amountPaid) <= 0
    val isDraft = invoiceStatus == com.emul8r.bizap.domain.model.InvoiceStatus.DRAFT

    AlertDialog(
        onDismissRequest = { if (!formState.isLoading) onDismiss() },
        title = { Text("Record Payment") },
        text = {
            if (isDraft) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "⚠️ Cannot Record Payment on Draft Invoice",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "You must send this invoice before recording payments. Change the status to SENT first.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            } else if (isFullyPaid) {
                Text(
                    "✅ This invoice is already fully paid.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Outstanding balance (prominent)
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Outstanding Balance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                formatCents(formState.outstanding),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Amount field
                    OutlinedTextField(
                        value = formState.amountRaw,
                        onValueChange = viewModel::onAmountChanged,
                        label = { Text("Payment Amount ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !formState.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = formState.amountError != null,
                        supportingText = formState.amountError?.let {
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    // Date picker button
                    val selectedDateLabel = dateFormatter.format(Date(formState.paymentDate))
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = formState.paymentDate
                            }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val picked = Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    viewModel.onDateChanged(picked)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).also { dialog ->
                                // Max date = end of today so the user can always select today
                                val endOfToday = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }.timeInMillis
                                dialog.datePicker.maxDate = endOfToday
                                if (invoiceDate > 0) dialog.datePicker.minDate = invoiceDate
                                dialog.show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !formState.isLoading
                    ) {
                        Text("Payment Date: $selectedDateLabel")
                    }
                    formState.dateError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // Notes field
                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = viewModel::onNotesChanged,
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !formState.isLoading,
                        maxLines = 3,
                        supportingText = {
                            Text("${formState.notes.length}/500")
                        }
                    )

                    // Submission error (shown below the form)
                    formState.submissionError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (formState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Button(
                    onClick = {
                        if (isDraft || isFullyPaid) {
                            onDismiss()
                        } else {
                            viewModel.submit()
                        }
                    },
                    enabled = isDraft || isFullyPaid || formState.isFormValid
                ) {
                    Text(if (isDraft || isFullyPaid) "OK" else "Record Payment")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !formState.isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

