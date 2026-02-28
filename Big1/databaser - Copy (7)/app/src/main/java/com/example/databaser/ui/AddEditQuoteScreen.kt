package com.example.databaser.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBar
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
import coil.compose.AsyncImage
import com.example.databaser.R
import com.example.databaser.data.Invoice
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.LineItemViewModel
import com.example.databaser.viewmodel.PredefinedLineItemViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditQuoteScreen(
    quoteId: Int?,
    customerId: Int?,
    onQuoteSaved: () -> Unit,
    onBackClick: () -> Unit,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModel.Factory),
    customerViewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.Factory),
    predefinedLineItemViewModel: PredefinedLineItemViewModel = viewModel(factory = PredefinedLineItemViewModel.Factory),
    lineItemViewModel: LineItemViewModel = viewModel(factory = LineItemViewModel.Factory),
    businessInfoViewModel: BusinessInfoViewModel = viewModel(factory = BusinessInfoViewModel.Factory)
) {
    val isEditing = quoteId != null && quoteId != -1
    val customer by customerViewModel.getCustomerById(customerId!!).collectAsStateWithLifecycle(initialValue = null)
    val businessInfo by businessInfoViewModel.businessInfo.collectAsStateWithLifecycle(initialValue = null)
    val quote by if (isEditing) {
        invoiceViewModel.getInvoiceById(quoteId!!).collectAsStateWithLifecycle(initialValue = null)
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

    LaunchedEffect(quote, businessInfo) {
        if (isEditing) {
            quote?.let {
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
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) stringResource(id = R.string.edit_quote) else stringResource(id = R.string.add_quote)) },
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
                Text(stringResource(id = R.string.quote_to), style = MaterialTheme.typography.titleMedium)
                Text(customer!!.name)
                Text(customer!!.address)
                Text(customer!!.contactNumber)
                Spacer(modifier = Modifier.height(16.dp))

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
                            val quoteToSave = Invoice(
                                id = quoteId!!,
                                customerId = customerId,
                                date = selectedDate,
                                invoiceNumber = quote!!.invoice.invoiceNumber,
                                dueDate = selectedDueDate,
                                isQuote = true
                            )
                            invoiceViewModel.updateInvoice(quoteToSave)
                            lineItemViewModel.deleteLineItemsByInvoiceId(quoteId)
                            lineItems.forEach { 
                                lineItemViewModel.addLineItem(it.copy(invoiceId = quoteId))
                            }
                        } else {
                            val quoteToSave = Invoice(
                                customerId = customerId,
                                date = selectedDate,
                                invoiceNumber = invoiceViewModel.generateInvoiceNumber(),
                                dueDate = selectedDueDate,
                                isQuote = true
                            )
                            val newQuoteId = invoiceViewModel.addInvoice(quoteToSave)
                            lineItems.forEach { 
                                lineItemViewModel.addLineItem(it.copy(invoiceId = newQuoteId.toInt()))
                            }
                        }
                        onQuoteSaved()
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
}
