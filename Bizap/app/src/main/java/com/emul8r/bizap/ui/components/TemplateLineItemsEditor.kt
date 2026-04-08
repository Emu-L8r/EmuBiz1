package com.emul8r.bizap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.LineItem
import timber.log.Timber

/**
 * Component for managing line items in invoice templates.
 *
 * Allows users to add, edit, and remove preset line items that will
 * be used when creating invoices from a template.
 *
 * Features:
 * - Add new line items
 * - Edit description, quantity, and unit price
 * - Remove line items
 * - Validation feedback
 * - Real-time calculation of totals
 */
@Composable
fun TemplateLineItemsEditor(
    items: List<LineItem>,
    onItemsChange: (List<LineItem>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isAddingNew by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "📝 Template Line Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Add preset items to auto-populate when using this template",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { isAddingNew = true },
                modifier = Modifier.padding(start = 12.dp),
                enabled = enabled
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Item")
            }
        }

        // List of existing items
        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    "No template items yet. Click 'Add Item' to get started.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    TemplateLineItemRow(
                        item = item,
                        onUpdate = { updated ->
                            onItemsChange(items.map { if (it.id == item.id) updated else it })
                        },
                        onDelete = {
                            Timber.d("🗑️ Deleting template line item: ${item.description}")
                            onItemsChange(items.filter { it.id != item.id })
                        },
                        enabled = enabled
                    )
                }
            }
        }

        // Add New Item Form
        if (isAddingNew) {
            TemplateLineItemForm(
                onSave = { newItem ->
                    Timber.d("✅ Adding new template line item: ${newItem.description}")
                    onItemsChange(items + newItem)
                    isAddingNew = false
                },
                onCancel = { isAddingNew = false }
            )
        }
    }
}

/**
 * Individual line item row with edit/delete actions
 */
@Composable
private fun TemplateLineItemRow(
    item: LineItem,
    onUpdate: (LineItem) -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        TemplateLineItemEditForm(
            item = item,
            onSave = { updated ->
                onUpdate(updated)
                isEditing = false
            },
            onCancel = { isEditing = false }
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        item.description,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Qty: ${item.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Price: \$${String.format("%.2f", item.unitPrice / 100.0)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Total: \$${String.format("%.2f", (item.unitPrice * item.quantity.toLong()) / 100.0)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { isEditing = true },
                        enabled = enabled,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,  // Using Add as edit icon since we don't have Edit icon
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        enabled = enabled,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Form to add a new line item
 */
@Composable
private fun TemplateLineItemForm(
    onSave: (LineItem) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "➕ Add New Template Item",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Design Services, Consultation") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.take(6) }, // Limit input
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it.take(10) },
                    label = { Text("Unit Price (\$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (description.isNotBlank() && quantity.toDoubleOrNull() != null && unitPrice.toDoubleOrNull() != null) {
                            val newItem = LineItem(
                                id = (System.currentTimeMillis() % 100000).toLong(),
                                description = description,
                                quantity = quantity.toDouble(),
                                unitPrice = (unitPrice.toDouble() * 100).toLong() // Convert to cents
                            )
                            onSave(newItem)
                        }
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }
}

/**
 * Form to edit an existing line item
 */
@Composable
private fun TemplateLineItemEditForm(
    item: LineItem,
    onSave: (LineItem) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var description by remember { mutableStateOf(item.description) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var unitPrice by remember { mutableStateOf(String.format("%.2f", item.unitPrice / 100.0)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "✏️ Edit Template Item",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.take(6) },
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it.take(10) },
                    label = { Text("Unit Price (\$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (description.isNotBlank() && quantity.toDoubleOrNull() != null && unitPrice.toDoubleOrNull() != null) {
                            val updated = item.copy(
                                description = description,
                                quantity = quantity.toDouble(),
                                unitPrice = (unitPrice.toDouble() * 100).toLong()
                            )
                            onSave(updated)
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}

