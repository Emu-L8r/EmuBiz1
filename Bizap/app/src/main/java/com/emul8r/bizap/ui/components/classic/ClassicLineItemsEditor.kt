package com.emul8r.bizap.ui.components.classic

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.LineItem

@Composable
fun ClassicLineItemsEditor(
    items: List<LineItem>,
    onItemsChange: (List<LineItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Line Items",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEachIndexed { index, lineItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Description field
                OutlinedTextField(
                    value = lineItem.description,
                    onValueChange = { newDesc ->
                        val updated = items.toMutableList()
                        updated[index] = updated[index].copy(description = newDesc)
                        onItemsChange(updated)
                    },
                    label = { Text("Description") },
                    modifier = Modifier
                        .weight(2f)
                        .height(56.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )

                // Quantity field
                OutlinedTextField(
                    value = lineItem.quantity.toString(),
                    onValueChange = { newQty ->
                        val updated = items.toMutableList()
                        updated[index] = updated[index].copy(quantity = newQty.toDoubleOrNull() ?: 1.0)
                        onItemsChange(updated)
                    },
                    label = { Text("Qty") },
                    modifier = Modifier
                        .weight(0.8f)
                        .height(56.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodySmall
                )

                // Unit Price field
                OutlinedTextField(
                    value = lineItem.unitPrice.toString(),
                    onValueChange = { newPrice ->
                        val updated = items.toMutableList()
                        updated[index] = updated[index].copy(unitPrice = newPrice.toLongOrNull() ?: 0L)
                        onItemsChange(updated)
                    },
                    label = { Text("Price") },
                    modifier = Modifier
                        .weight(0.8f)
                        .height(56.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodySmall
                )

                // Delete button
                IconButton(
                    onClick = {
                        onItemsChange(items.filterIndexed { i, _ -> i != index })
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                }
            }
        }

        // Add Item button
        Button(
            onClick = {
                onItemsChange(items + LineItem(
                    id = (items.maxOfOrNull { it.id } ?: 0) + 1,
                    description = "",
                    quantity = 1.0,
                    unitPrice = 0L
                ))
            },
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 12.dp)
        ) {
            Text("Add Item")
        }
    }
}
