package com.example.databaser.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.Invoice
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.LineItemViewModel
import com.example.databaser.viewmodel.PredefinedLineItemViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddInvoiceScreen(
    customerId: Int,
    onInvoiceAdded: () -> Unit,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModel.Factory),
    customerViewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.Factory),
    predefinedLineItemViewModel: PredefinedLineItemViewModel = viewModel(factory = PredefinedLineItemViewModel.Factory),
    lineItemViewModel: LineItemViewModel = viewModel(factory = LineItemViewModel.Factory)
) {
    val customer by customerViewModel.getCustomerById(customerId).collectAsStateWithLifecycle(initialValue = null)
    val predefinedLineItems by predefinedLineItemViewModel.allPredefinedLineItems.collectAsStateWithLifecycle()
    val lineItems = remember { mutableStateListOf<LineItem>() }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var selectedDueDate by remember { mutableStateOf(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000) }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(stringResource(id = R.string.add_invoice)) })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showBottomSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_line_item))
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text(stringResource(id = R.string.invoice_to), style = MaterialTheme.typography.titleMedium)
                Text(customer!!.name)
                Text(customer!!.address)
                Text(customer!!.contactNumber)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    TextField(
                        value = dateFormat.format(Date(selectedDate)),
                        onValueChange = {},
                        label = { Text("Date") },
                        readOnly = true,
                        modifier = Modifier.weight(1f).clickable { showDatePicker = true }
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    TextField(
                        value = dateFormat.format(Date(selectedDueDate)),
                        onValueChange = {},
                        label = { Text("Due Date") },
                        readOnly = true,
                        modifier = Modifier.weight(1f).clickable { showDueDatePicker = true }
                    )
                }
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Text(stringResource(id = R.string.item), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                            Text(stringResource(id = R.string.total), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall)
                        }
                        HorizontalDivider()
                    }
                    items(lineItems) { lineItem ->
                        LineItemRow(lineItem, onLongClick = { /*TODO: Edit line item*/ })
                        HorizontalDivider()
                    }
                }

                Button(onClick = {
                    coroutineScope.launch {
                        val invoice = Invoice(
                            customerId = customerId, 
                            date = selectedDate,
                            invoiceNumber = invoiceViewModel.generateInvoiceNumber(),
                            dueDate = selectedDueDate
                        )
                        val invoiceId = invoiceViewModel.addInvoice(invoice)
                        lineItems.forEach { 
                            lineItemViewModel.addLineItem(it.copy(invoiceId = invoiceId.toInt()))
                        }
                        onInvoiceAdded()
                    }
                }) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    selectedDate = datePickerState.selectedDateMillis!!
                    showDatePicker = false 
                }) {
                    Text(stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showDueDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDueDate)
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    selectedDueDate = datePickerState.selectedDateMillis!!
                    showDueDatePicker = false 
                }) {
                    Text(stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(id = R.string.services), style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { showCustomItemDialog = true }) {
                        Text(stringResource(id = R.string.add_custom_item))
                    }
                }
                HorizontalDivider()
                LazyColumn {
                    items(predefinedLineItems) { service ->
                        ServiceItem(
                            service = service,
                            onClick = { 
                                lineItems.add(LineItem(invoiceId = 0, job = service.job, details = service.details, quantity = 1, unitPrice = service.unitPrice))
                                coroutineScope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion { 
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                             }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showCustomItemDialog) {
        AddCustomItemDialog(
            onDismiss = { showCustomItemDialog = false },
            onSave = {
                lineItems.add(it)
                showCustomItemDialog = false
                coroutineScope.launch {
                    sheetState.hide()
                }.invokeOnCompletion { 
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            }
        )
    }
}

@Composable
fun AddCustomItemDialog(onDismiss: () -> Unit, onSave: (LineItem) -> Unit) {
    var job by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(id = R.string.add_custom_item), style = MaterialTheme.typography.titleMedium)
                TextField(value = job, onValueChange = { job = it }, label = { Text(stringResource(id = R.string.job)) })
                TextField(value = details, onValueChange = { details = it }, label = { Text(stringResource(id = R.string.details)) })
                TextField(value = quantity, onValueChange = { quantity = it }, label = { Text(stringResource(id = R.string.quantity)) })
                TextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text(stringResource(id = R.string.unit_price)) })
                Spacer(modifier = Modifier.height(16.dp))
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
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    }
}
