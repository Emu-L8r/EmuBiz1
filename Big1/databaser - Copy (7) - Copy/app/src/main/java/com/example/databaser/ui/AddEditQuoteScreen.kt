package com.example.databaser.ui

import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.Screen
import com.example.databaser.data.Invoice
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
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
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel(),
    predefinedLineItemViewModel: PredefinedLineItemViewModel = hiltViewModel(),
    businessInfoViewModel: BusinessInfoViewModel = hiltViewModel(),
    navController: NavHostController
) {
    var currentQuoteId by rememberSaveable { mutableIntStateOf(quoteId ?: -1) }
    val isEditing = currentQuoteId != -1
    val customer by customerViewModel.getCustomerById(customerId ?: -1).collectAsStateWithLifecycle(initialValue = null)
    val businessInfo by businessInfoViewModel.businessInfo.collectAsStateWithLifecycle(initialValue = null)
    val quote by if (isEditing) {
        invoiceViewModel.getInvoiceById(currentQuoteId).collectAsStateWithLifecycle(initialValue = null)
    } else {
        remember { mutableStateOf(null) }
    }
    val predefinedLineItems by predefinedLineItemViewModel.allPredefinedLineItems.collectAsStateWithLifecycle()
    val lineItems = remember { mutableStateListOf<LineItem>() }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf<LineItem?>(null) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedDueDate by remember { mutableLongStateOf(0L) }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showConversionDialog by remember { mutableStateOf(false) }
    val conversionResult by invoiceViewModel.conversionResult.collectAsState(initial = null)


    LaunchedEffect(key1 = quote, key2 = businessInfo) {
        if (isEditing) {
            quote?.let {
                selectedDate = it.invoice.date
                selectedDueDate = it.invoice.dueDate
                photoUris = it.invoice.photoUris.map { Uri.parse(it) }
                if (lineItems.isEmpty()) { // To avoid overwriting on recomposition
                    lineItems.addAll(it.lineItems)
                }
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
    
    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.get<LineItem>("newLineItem")?.let {
            lineItems.add(it)
            navController.currentBackStackEntry?.savedStateHandle?.remove<LineItem>("newLineItem")
        }
    }
    
    LaunchedEffect(conversionResult) {
        if (conversionResult == true) {
            showConversionDialog = true
        }
    }

    if (showConversionDialog) {
        AlertDialog(
            onDismissRequest = { showConversionDialog = false },
            title = { Text("Conversion Successful") },
            text = { Text("The quote has been successfully converted to an invoice.") },
            confirmButton = {
                TextButton(onClick = { showConversionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (businessInfo == null || (isEditing && quote == null)) {
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
                FloatingActionButton(onClick = {
                    coroutineScope.launch {
                        val id = if (isEditing) {
                            currentQuoteId
                        } else {
                            val newQuote = customer?.name?.let {
                                Invoice(
                                    customerId = customerId ?: -1,
                                    date = selectedDate,
                                    invoiceNumber = invoiceViewModel.generateQuoteNumber(it),
                                    dueDate = selectedDueDate,
                                    isQuote = true
                                )
                            }
                            val newId = newQuote?.let { invoiceViewModel.addInvoice(it).toInt() }
                            if (newId != null) {
                                currentQuoteId = newId
                            }
                            newId
                        }
                        id?.let { navController.navigate(Screen.AddLineItem.createRoute(it)) }
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_line_item))
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                customer?.let {
                    Text(stringResource(id = R.string.quote_to), style = MaterialTheme.typography.titleMedium)
                    Text(it.name)
                    Text(it.address)
                    Text(it.contactNumber)
                }
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
                        LineItemRow(lineItem, onLongClick = { showEditItemDialog = lineItem })
                        HorizontalDivider()
                    }
                }

                Button(onClick = {
                    coroutineScope.launch {
                        val quoteToSave = if (isEditing) {
                            quote!!.invoice.copy(
                                date = selectedDate,
                                dueDate = selectedDueDate
                            )
                        } else {
                            customer?.name?.let {
                                Invoice(
                                    customerId = customerId ?: -1,
                                    date = selectedDate,
                                    invoiceNumber = invoiceViewModel.generateQuoteNumber(it),
                                    dueDate = selectedDueDate,
                                    isQuote = true
                                )
                            }
                        }

                        if (isEditing) {
                            quoteToSave?.let { invoiceViewModel.updateInvoiceWithLineItems(it, lineItems) }
                        } else {
                            quoteToSave?.let { invoiceViewModel.addInvoiceWithLineItems(it, lineItems) }
                        }
                        onQuoteSaved()
                    }
                }) {
                    Text(stringResource(id = R.string.save))
                }

                Button(onClick = { invoiceViewModel.convertToInvoice(quote!!.invoice) }) {
                    Text("Convert to Invoice")
                }

                Button(onClick = {
                    val quoteIdForPreview = if (isEditing) quoteId else null
                    if (quoteIdForPreview != null) {
                        navController.navigate(Screen.PrintPreview.createRoute(quoteIdForPreview))
                    } else {
                        // Optionally, show a toast or message that the quote must be saved first
                    }
                }) {
                    Text(stringResource(id = R.string.print_preview))
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
            onSave = {newItem ->
                lineItems.add(newItem)
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

    showEditItemDialog?.let {
        EditLineItemDialog(
            lineItem = it,
            onDismiss = { showEditItemDialog = null },
            onSave = { updatedLineItem ->
                val index = lineItems.indexOf(it)
                if (index != -1) {
                    lineItems[index] = updatedLineItem
                }
                showEditItemDialog = null
            }
        )
    }
}
