package com.example.databaser.ui

import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.Invoice
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.LineItemViewModel
import com.example.databaser.viewmodel.PredefinedLineItemViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditInvoiceScreen(
    invoiceId: Int?,
    customerId: Int?,
    onInvoiceSaved: () -> Unit,
    onBackClick: () -> Unit,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModel.Factory),
    customerViewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.Factory),
    predefinedLineItemViewModel: PredefinedLineItemViewModel = viewModel(factory = PredefinedLineItemViewModel.Factory),
    lineItemViewModel: LineItemViewModel = viewModel(factory = LineItemViewModel.Factory),
    businessInfoViewModel: BusinessInfoViewModel = viewModel(factory = BusinessInfoViewModel.Factory)
) {
    val isEditing = invoiceId != null && invoiceId != -1
    val customer by customerViewModel.getCustomerById(customerId!!).collectAsStateWithLifecycle(initialValue = null)
    val businessInfo by businessInfoViewModel.businessInfo.collectAsStateWithLifecycle(initialValue = null)
    val invoice by if (isEditing) {
        invoiceViewModel.getInvoiceById(invoiceId!!).collectAsStateWithLifecycle(initialValue = null)
    } else {
        remember { mutableStateOf(null) }
    }
    val predefinedLineItems by predefinedLineItemViewModel.allPredefinedLineItems.collectAsStateWithLifecycle()
    val lineItems = remember { mutableStateListOf<LineItem>() }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedDueDate by remember { mutableStateOf(0L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(invoice, businessInfo) {
        if (isEditing) {
            invoice?.let {
                selectedDate = it.invoice.date
                selectedDueDate = it.invoice.dueDate
                lineItems.clear()
                lineItems.addAll(it.lineItems)
            }
        } else {
            businessInfo?.let {
                selectedDate = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDate
                calendar.add(Calendar.DAY_OF_YEAR, it.defaultDueDateDays)
                selectedDueDate = calendar.timeInMillis
            }
        }
    }

    if (customer == null || businessInfo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val dateFormat = remember(businessInfo?.dateFormat) {
            try {
                SimpleDateFormat(businessInfo?.dateFormat ?: "dd/MM/yyyy", Locale.getDefault())
            } catch (e: IllegalArgumentException) {
                Log.e("AddEditInvoiceScreen", "Invalid date format provided: ${businessInfo?.dateFormat}. Using default.", e)
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) stringResource(id = R.string.edit_invoice) else stringResource(id = R.string.add_invoice)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                        }
                    }
                )
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
                        if (isEditing) {
                            val invoiceToSave = Invoice(
                                id = invoiceId!!,
                                customerId = customerId,
                                date = selectedDate,
                                invoiceNumber = invoice!!.invoice.invoiceNumber,
                                dueDate = selectedDueDate
                            )
                            invoiceViewModel.updateInvoice(invoiceToSave)
                            lineItemViewModel.deleteLineItemsByInvoiceId(invoiceId)
                            lineItems.forEach { 
                                lineItemViewModel.addLineItem(it.copy(invoiceId = invoiceId))
                            }
                        } else {
                            val invoiceToSave = Invoice(
                                customerId = customerId,
                                date = selectedDate,
                                invoiceNumber = invoiceViewModel.generateInvoiceNumber(),
                                dueDate = selectedDueDate
                            )
                            val newInvoiceId = invoiceViewModel.addInvoice(invoiceToSave)
                            lineItems.forEach { 
                                lineItemViewModel.addLineItem(it.copy(invoiceId = newInvoiceId.toInt()))
                            }
                        }
                        onInvoiceSaved()
                    }
                }) {
                    Text(stringResource(id = R.string.save))
                }
            }
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        val onDateSelected = {
            datePickerState.selectedDateMillis?.let {
                selectedDate = it
            }
            showDatePicker = false
        }
        DatePickerDialog(
            onDismissRequest = onDateSelected,
            confirmButton = {
                TextButton(onClick = onDateSelected) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDueDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDueDate)
        val onDueDateSelected = {
            datePickerState.selectedDateMillis?.let {
                selectedDueDate = it
            }
            showDueDatePicker = false
        }
        DatePickerDialog(
            onDismissRequest = onDueDateSelected,
            confirmButton = {
                TextButton(onClick = onDueDateSelected) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDueDatePicker = false }
                ) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
