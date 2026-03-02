package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.data.local.entities.PrefilledItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrefilledItemsScreen(viewModel: PrefilledItemsViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add pre-filled item")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(items, key = { it.id }) { item ->
                ListItem(
                    headlineContent = { Text(item.description) },
                    supportingContent = { Text("$${item.unitPrice}") },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteItem(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete item")
                        }
                    }
                )
            }
        }
    }

    if (showDialog) {
        var description by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Pre-filled Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") }
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Unit Price") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    val priceInCents = (price.toDoubleOrNull() ?: 0.0) * 100
                    viewModel.addItem(description, priceInCents.toLong())
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

