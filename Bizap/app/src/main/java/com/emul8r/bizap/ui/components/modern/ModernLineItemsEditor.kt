package com.emul8r.bizap.ui.components.modern

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.emul8r.bizap.domain.model.LineItem

@Composable
fun ModernLineItemsEditor(
    items: List<LineItem>,
    onItemsChange: (List<LineItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            "Line Items",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(items.size) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Description field
                        OutlinedTextField(
                            value = items[index].description,
                            onValueChange = { newDesc ->
                                val updated = items.toMutableList()
                                updated[index] = updated[index].copy(description = newDesc)
                                onItemsChange(updated)
                            },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quantity and Price fields
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = items[index].quantity.toString(),
                                onValueChange = { newQty ->
                                    val updated = items.toMutableList()
                                    updated[index] = updated[index].copy(quantity = newQty.toIntOrNull() ?: 1)
                                    onItemsChange(updated)
                                },
                                label = { Text("Quantity") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )

                            OutlinedTextField(
                                value = items[index].unitPrice.toString(),
                                onValueChange = { newPrice ->
                                    val updated = items.toMutableList()
                                    updated[index] = updated[index].copy(unitPrice = newPrice.toDoubleOrNull() ?: 0.0)
                                    onItemsChange(updated)
                                },
                                label = { Text("Unit Price") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )

                            // Delete button
                            IconButton(
                                onClick = {
                                    onItemsChange(items.filterIndexed { i, _ -> i != index })
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Delete",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Total display
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "%.2f".format(items[index].total),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Add Item button
        Button(
            onClick = {
                onItemsChange(items + LineItem(
                    id = (items.maxOfOrNull { it.id } ?: 0) + 1,
                    description = "",
                    quantity = 1,
                    unitPrice = 0.0
                ))
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ Add Item")
        }
    }
}

