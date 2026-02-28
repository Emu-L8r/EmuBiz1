package com.example.databaser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.LineItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLineItemScreen(
    lineItemId: Int,
    onLineItemUpdated: () -> Unit,
    lineItemViewModel: LineItemViewModel = viewModel(factory = LineItemViewModel.Factory)
) {
    val lineItem by lineItemViewModel.getLineItemById(lineItemId).collectAsState(initial = null)
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    var job by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    val hasLoaded = remember { mutableStateOf(false) }

    LaunchedEffect(lineItem) {
        if (lineItem != null) {
            job = lineItem!!.job
            details = lineItem!!.details
            quantity = lineItem!!.quantity.toString()
            unitPrice = lineItem!!.unitPrice.toString()
            hasLoaded.value = true
        } else if (hasLoaded.value) {
            onLineItemUpdated()
        }
    }

    val currentLineItem = lineItem
    if (currentLineItem != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.edit_line_item)) }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                ThemedTextField(
                    value = job,
                    onValueChange = { job = it },
                    label = stringResource(id = R.string.job),
                    modifier = Modifier.fillMaxWidth()
                )
                ThemedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = stringResource(id = R.string.details),
                    modifier = Modifier.fillMaxWidth()
                )
                ThemedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = stringResource(id = R.string.quantity),
                    modifier = Modifier.fillMaxWidth()
                )
                ThemedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = stringResource(id = R.string.unit_price),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        try {
                            val updatedLineItem = LineItem(
                                id = lineItemId,
                                invoiceId = currentLineItem.invoiceId,
                                job = job,
                                details = details,
                                quantity = quantity.toIntOrNull() ?: 1,
                                unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                            )
                            lineItemViewModel.updateLineItem(updatedLineItem)
                            onLineItemUpdated()
                        } catch (e: NumberFormatException) {
                            // Handle the case where the input is not a valid number
                        }
                    }) {
                        Text(stringResource(id = R.string.update))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { showDeleteConfirmationDialog = true }) {
                        Text(stringResource(id = R.string.delete))
                    }
                }
            }
        }

        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false },
                title = { Text(stringResource(id = R.string.delete_line_item)) },
                text = { Text(stringResource(id = R.string.confirm_delete_line_item)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            lineItem?.let { 
                                lineItemViewModel.deleteLineItem(it)
                            }
                            showDeleteConfirmationDialog = false
                        }
                    ) {
                        Text(stringResource(id = R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmationDialog = false }
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
