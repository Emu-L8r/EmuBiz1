package com.example.databaser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.databaser.data.Invoice
import com.example.databaser.data.LineItem

@Composable
fun StatusIndicator(invoice: Invoice) {
    val (text, color) = when {
        invoice.isPaid -> "Paid" to Color(0xFF4CAF50) // Green
        invoice.isSent -> "Sent" to Color(0xFF2196F3) // Blue
        else -> "Draft" to Color(0xFF9E9E9E) // Gray
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AddCustomItemDialog(
    onDismiss: () -> Unit,
    onSave: (LineItem) -> Unit
) {
    var job by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Item") },
        text = {
            Column {
                TextField(value = job, onValueChange = { job = it }, label = { Text("Job") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = details, onValueChange = { details = it }, label = { Text("Details") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text("Unit Price") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val newItem = LineItem(
                    invoiceId = 0, // This will be updated when the invoice is saved
                    job = job,
                    details = details,
                    quantity = quantity.toIntOrNull() ?: 1,
                    unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                )
                onSave(newItem)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditLineItemDialog(
    lineItem: LineItem,
    onDismiss: () -> Unit,
    onSave: (LineItem) -> Unit
) {
    var job by remember { mutableStateOf(lineItem.job) }
    var details by remember { mutableStateOf(lineItem.details) }
    var quantity by remember { mutableStateOf(lineItem.quantity.toString()) }
    var unitPrice by remember { mutableStateOf(lineItem.unitPrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Line Item") },
        text = {
            Column {
                TextField(value = job, onValueChange = { job = it }, label = { Text("Job") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = details, onValueChange = { details = it }, label = { Text("Details") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text("Unit Price") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedItem = lineItem.copy(
                    job = job,
                    details = details,
                    quantity = quantity.toIntOrNull() ?: 1,
                    unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                )
                onSave(updatedItem)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
