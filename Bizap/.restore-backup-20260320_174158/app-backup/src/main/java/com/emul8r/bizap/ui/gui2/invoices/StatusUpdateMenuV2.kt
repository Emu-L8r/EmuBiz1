package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import com.emul8r.bizap.domain.model.InvoiceStatus

/**
 * Status Update Menu for Invoice
 */
@Composable
fun StatusUpdateMenuV2(
    currentStatus: InvoiceStatus,
    onStatusSelected: (InvoiceStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val statusOptions = listOf(
        InvoiceStatus.DRAFT,
        InvoiceStatus.SENT,
        InvoiceStatus.PAID,
        InvoiceStatus.OVERDUE
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Invoice Status") },
        text = {
            Column {
                statusOptions.forEach { status ->
                    if (status != currentStatus) {
                        TextButton(
                            onClick = {
                                onStatusSelected(status)
                                onDismiss()
                            }
                        ) {
                            Text(status.toString())
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

