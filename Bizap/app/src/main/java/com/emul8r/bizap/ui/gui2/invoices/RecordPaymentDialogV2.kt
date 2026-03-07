package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.gui2.common.formatCents

/**
 * GUI2 Record Payment Dialog
 * Dialog to record a payment on an invoice.
 */
@Composable
fun RecordPaymentDialogV2(
    invoiceTotal: Long,
    amountPaid: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val remainingBalance = invoiceTotal - amountPaid
    val isFullyPaid = remainingBalance <= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isFullyPaid) {
                    Text(
                        "✅ This invoice is already fully paid",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        "Remaining balance: ${formatCents(remainingBalance)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            errorMessage = null
                        },
                        label = { Text("Payment Amount ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isFullyPaid,
                        supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        isError = errorMessage != null
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFullyPaid) {
                        onDismiss()
                        return@Button
                    }

                    amount.toDoubleOrNull()?.let { doubleAmount ->
                        val centsAmount = (doubleAmount * 100).toLong()

                        when {
                            centsAmount <= 0 -> {
                                errorMessage = "Amount must be greater than \$0"
                            }
                            centsAmount > remainingBalance -> {
                                errorMessage = "Payment exceeds remaining balance"
                            }
                            else -> {
                                onConfirm(centsAmount)
                                onDismiss()
                            }
                        }
                    } ?: run {
                        errorMessage = "Invalid amount"
                    }
                },
                enabled = !isFullyPaid
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

