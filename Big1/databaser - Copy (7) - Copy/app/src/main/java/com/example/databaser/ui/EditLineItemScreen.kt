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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.LineItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLineItemScreen(
    lineItemId: Int,
    onLineItemUpdated: () -> Unit,
    navController: NavHostController,
    lineItemViewModel: LineItemViewModel = hiltViewModel()
) {
    val lineItem by lineItemViewModel.getLineItemById(lineItemId).collectAsState(initial = null)
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    var job by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }

    var isQuantityError by remember { mutableStateOf(false) }
    var isUnitPriceError by remember { mutableStateOf(false) }

    LaunchedEffect(lineItem) {
        if (lineItem != null) {
            job = lineItem!!.job
            details = lineItem!!.details
            quantity = lineItem!!.quantity.toString()
            unitPrice = lineItem!!.unitPrice.toString()
        }
    }

    val currentLineItem = lineItem
    if (currentLineItem != null) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = stringResource(id = R.string.edit_line_item),
                    canNavigateBack = true,
                    onNavigateUp = onLineItemUpdated,
                    navController = navController
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
                    onValueChange = {
                        quantity = it
                        isQuantityError = it.toIntOrNull() == null
                    },
                    label = stringResource(id = R.string.quantity),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isQuantityError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ThemedTextField(
                    value = unitPrice,
                    onValueChange = {
                        unitPrice = it
                        isUnitPriceError = it.toDoubleOrNull() == null
                    },
                    label = stringResource(id = R.string.unit_price),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isUnitPriceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        onClick = {
                            val updatedLineItem = LineItem(
                                id = lineItemId,
                                invoiceId = currentLineItem.invoiceId,
                                job = job,
                                details = details,
                                quantity = quantity.toInt(),
                                unitPrice = unitPrice.toDouble()
                            )
                            lineItemViewModel.updateLineItem(updatedLineItem)
                            onLineItemUpdated()
                        },
                        enabled = quantity.isNotEmpty() && unitPrice.isNotEmpty() && !isQuantityError && !isUnitPriceError
                    ) {
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
                            lineItemViewModel.deleteLineItem(currentLineItem)
                            showDeleteConfirmationDialog = false
                            onLineItemUpdated() // Navigate away directly
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
