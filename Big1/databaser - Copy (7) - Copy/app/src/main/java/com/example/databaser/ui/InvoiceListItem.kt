package com.example.databaser.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.databaser.data.InvoiceWithCustomerAndLineItems
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.SettingsViewModel

@Composable
fun InvoiceListItem(
    invoice: InvoiceWithCustomerAndLineItems,
    onInvoiceClick: (Int, Int) -> Unit,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    val useCompactMode by settingsViewModel.useCompactMode.collectAsState()

    val cardPadding = if (useCompactMode) 8.dp else 16.dp
    val verticalPadding = if (useCompactMode) 2.dp else 4.dp

    val totalAmount = invoice.lineItems.sumOf { it.quantity * it.unitPrice }
    val amountColor = when {
        totalAmount > 1000 -> Color.Red
        totalAmount > 500 -> Color(0xFFFFA500) // Orange
        else -> Color.Green
    }

    Card(
        modifier = Modifier.padding(horizontal = cardPadding, vertical = verticalPadding)
            .clickable { onInvoiceClick(invoice.invoice.id, invoice.customer.id) },
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        ListItem(
            headlineContent = { Text(invoice.customer.name, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("Invoice #${invoice.invoice.invoiceNumber}") },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(color = amountColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$${totalAmount}")
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusIndicator(invoice = invoice.invoice)
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Create Quote") },
                                onClick = { 
                                    invoiceViewModel.createQuoteFromInvoice(invoice.invoice.id)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Paid") },
                                onClick = { 
                                    invoiceViewModel.updateInvoiceStatus(invoice.invoice.id, isPaid = true, isSent = true, isHidden = false)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sent") },
                                onClick = { 
                                    invoiceViewModel.updateInvoiceStatus(invoice.invoice.id, isPaid = false, isSent = true, isHidden = false)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Draft") },
                                onClick = { 
                                    invoiceViewModel.updateInvoiceStatus(invoice.invoice.id, isPaid = false, isSent = false, isHidden = false)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        )
    }
}
