package com.example.databaser.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.PredefinedLineItem
import com.example.databaser.viewmodel.PredefinedLineItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePredefinedLineItemsScreen(
    onAddClick: () -> Unit,
    predefinedLineItemViewModel: PredefinedLineItemViewModel = viewModel(factory = PredefinedLineItemViewModel.Factory)
) {
    val items by predefinedLineItemViewModel.allPredefinedLineItems.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_pre_filled_item))
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.no_invoices_found))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(items, key = { it.id }) { item ->
                    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
                    var itemToDelete by remember { mutableStateOf<PredefinedLineItem?>(null) }
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            itemToDelete = item
                            showDeleteConfirmationDialog = true
                            dismissState.reset()
                        }
                    }

                    Card(modifier = Modifier.padding(8.dp)) {
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                                        SwipeToDismissBoxValue.EndToStart -> Color.Red
                                        SwipeToDismissBoxValue.StartToEnd -> Color.Red
                                    },
                                    label = "color"
                                )
                                val scale by animateFloatAsState(
                                    targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                                    label = "scale"
                                )

                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(id = R.string.delete),
                                        modifier = Modifier.scale(scale)
                                    )
                                }
                            }
                        ) {
                            ListItem(
                                headlineContent = { Text(item.job, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("$${item.unitPrice}") }
                            )
                        }
                    }

                    if (showDeleteConfirmationDialog) {
                        DeletePredefinedItemConfirmationDialog(
                            onConfirm = {
                                itemToDelete?.let { predefinedLineItemViewModel.deletePredefinedLineItem(it) }
                                showDeleteConfirmationDialog = false
                            },
                            onDismiss = { showDeleteConfirmationDialog = false },
                            itemName = itemToDelete?.job ?: ""
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeletePredefinedItemConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    itemName: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.confirm_delete_pre_filled_item))
                Text(itemName)
                Row {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}
