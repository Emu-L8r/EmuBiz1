@file:OptIn(ExperimentalMaterial3Api::class)
package com.emul8r.bizap.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import java.util.UUID

/**
 * Custom field builder for managing template custom fields.
 */
@Composable
fun CustomFieldBuilder(
    fields: List<InvoiceCustomField>,
    onFieldsChange: (List<InvoiceCustomField>) -> Unit,
    modifier: Modifier = Modifier
) {
    var localFields by remember { mutableStateOf(fields) }

    LaunchedEffect(fields) {
        localFields = fields
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Custom Fields", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "${localFields.size}/50 fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (localFields.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No custom fields yet. Add one to get started!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Replaced LazyColumn with Column for integration repair
            localFields.forEachIndexed { index, field ->
                CustomFieldItem(
                    field = field,
                    index = index,
                    onUpdate = { updatedField ->
                        val updated = localFields.toMutableList()
                        updated[index] = updatedField
                        localFields = updated
                        onFieldsChange(updated)
                    },
                    onDelete = {
                        val updated = localFields.toMutableList()
                        updated.removeAt(index)
                        localFields = updated.mapIndexed { i, f -> f.copy(displayOrder = i + 1) }
                        onFieldsChange(localFields)
                    },
                    onMoveUp = {
                        if (index > 0) {
                            val updated = localFields.toMutableList()
                            val temp = updated[index]
                            updated[index] = updated[index - 1]
                            updated[index - 1] = temp
                            localFields = updated.mapIndexed { i, f -> f.copy(displayOrder = i + 1) }
                            onFieldsChange(localFields)
                        }
                    },
                    onMoveDown = {
                        if (index < localFields.size - 1) {
                            val updated = localFields.toMutableList()
                            val temp = updated[index]
                            updated[index] = updated[index + 1]
                            updated[index + 1] = temp
                            localFields = updated.mapIndexed { i, f -> f.copy(displayOrder = i + 1) }
                            onFieldsChange(localFields)
                        }
                    }
                )
            }
        }

        if (localFields.size < 50) {
            Button(
                onClick = {
                    val newField = InvoiceCustomField(
                        id = UUID.randomUUID().toString(),
                        templateId = "",
                        label = "New Field",
                        fieldType = "TEXT",
                        displayOrder = localFields.size + 1
                    )
                    localFields = localFields + newField
                    onFieldsChange(localFields)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Field")
            }
        }
    }
}

@Composable
private fun CustomFieldItem(
    field: InvoiceCustomField,
    index: Int,
    onUpdate: (InvoiceCustomField) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DragHandle, null)
                OutlinedTextField(
                    value = field.label,
                    onValueChange = { onUpdate(field.copy(label = it)) },
                    label = { Text("Field Label") },
                    modifier = Modifier.weight(1f)
                )
            }

            var expandedType by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = it }
            ) {
                OutlinedTextField(
                    value = field.fieldType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    listOf("TEXT", "NUMBER", "DATE").forEach { type ->
                        DropdownMenuItem(text = { Text(type) }, onClick = { onUpdate(field.copy(fieldType = type)); expandedType = false })
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = field.isRequired, onCheckedChange = { onUpdate(field.copy(isRequired = it)) })
                Text("Required", style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onMoveUp, modifier = Modifier.weight(1f)) { Text("↑") }
                OutlinedButton(onClick = onMoveDown, modifier = Modifier.weight(1f)) { Text("↓") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
