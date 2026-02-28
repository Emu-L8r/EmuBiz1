package com.example.databaser.ui

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.BusinessInfo
import com.example.databaser.data.Customer
import com.example.databaser.data.Invoice
import com.example.databaser.data.InvoiceWithCustomerAndLineItems
import com.example.databaser.data.LineItem
import com.example.databaser.ui.theme.CornflowerBlue
import com.example.databaser.ui.theme.DarkGreen
import com.example.databaser.ui.theme.Orange
import com.example.databaser.viewmodel.BusinessInfoViewModel
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.LineItemViewModel
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InvoiceListScreen(
    customerId: Int,
    onBackClick: () -> Unit,
    onInvoiceClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModel.Factory),
    customerViewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.Factory),
    lineItemViewModel: LineItemViewModel = viewModel(factory = LineItemViewModel.Factory),
    businessInfoViewModel: BusinessInfoViewModel = viewModel(factory = BusinessInfoViewModel.Factory)
) {
    val invoices by invoiceViewModel.getInvoicesForCustomer(customerId).collectAsState(initial = emptyList())
    val customer by customerViewModel.getCustomerById(customerId).collectAsState(initial = null)
    val businessInfo by businessInfoViewModel.businessInfo.collectAsState(initial = null)
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var invoiceToDelete by remember { mutableStateOf<InvoiceWithCustomerAndLineItems?>(null) }
    var selectedInvoices by remember { mutableStateOf(emptySet<InvoiceWithCustomerAndLineItems>()) }
    val coroutineScope = rememberCoroutineScope()
    val currentCustomer = customer
    val context = LocalContext.current

    if (currentCustomer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                if (selectedInvoices.isNotEmpty()) {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.selected, selectedInvoices.size)) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        actions = {
                            IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete))
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.invoices_for, currentCustomer.name)) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                            }
                        },
                        actions = {
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.settings))
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_invoice))
                }
            }
        ) { padding ->
            if (invoices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.no_invoices_found))
                }
            } else {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(invoices, key = { it.invoice.id }) { invoice ->
                        val dismissState = rememberSwipeToDismissBoxState()

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                invoiceToDelete = invoice
                                showDeleteConfirmationDialog = true
                                dismissState.reset()
                            }
                        }

                        Card(
                            modifier = Modifier.padding(8.dp).clickable { onInvoiceClick(invoice.invoice.id) },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
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
                                    val alpha by animateFloatAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0f else 1f,
                                        label = "alpha"
                                    )

                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            stringResource(id = R.string.delete),
                                            modifier = Modifier.scale(scale).alpha(alpha),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                var expanded by remember { mutableStateOf(false) }
                                val statusText = when {
                                    invoice.invoice.isPaid -> stringResource(id = R.string.paid)
                                    invoice.invoice.isSent -> stringResource(id = R.string.sent)
                                    else -> stringResource(id = R.string.unsent)
                                }
                                val statusColor = when {
                                    invoice.invoice.isPaid -> DarkGreen
                                    invoice.invoice.isSent -> CornflowerBlue
                                    else -> Orange
                                }

                                ListItem(
                                    headlineContent = { Text(invoice.customer.name, fontWeight = FontWeight.Bold) },
                                    supportingContent = {
                                        Column {
                                            Text(stringResource(id = R.string.invoice_number, invoice.invoice.invoiceNumber))
                                            Text(
                                                text = stringResource(id = R.string.status, statusText),
                                                color = statusColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    trailingContent = {
                                        Box {
                                            IconButton(onClick = { expanded = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more))
                                            }
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Add to Calendar") },
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_INSERT).apply {
                                                            data = CalendarContract.Events.CONTENT_URI
                                                            putExtra(CalendarContract.Events.TITLE, "Invoice Due: ${invoice.invoice.invoiceNumber}")
                                                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, invoice.invoice.dueDate)
                                                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, invoice.invoice.dueDate)
                                                        }
                                                        context.startActivity(intent)
                                                        expanded = false
                                                    }
                                                )
                                                DropdownMenuItem(text = { Text(stringResource(id = R.string.unsent)) }, onClick = {
                                                    coroutineScope.launch {
                                                        invoiceViewModel.updateInvoice(invoice.invoice.copy(isSent = false, isPaid = false))
                                                    }
                                                    expanded = false
                                                })
                                                DropdownMenuItem(text = { Text(stringResource(id = R.string.sent)) }, onClick = {
                                                    coroutineScope.launch {
                                                        invoiceViewModel.updateInvoice(invoice.invoice.copy(isSent = true, isPaid = false))
                                                    }
                                                    expanded = false
                                                })
                                                DropdownMenuItem(text = { Text(stringResource(id = R.string.paid)) }, onClick = {
                                                    coroutineScope.launch {
                                                        invoiceViewModel.updateInvoice(invoice.invoice.copy(isSent = true, isPaid = true))
                                                    }
                                                    expanded = false
                                                })
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        if (showDeleteConfirmationDialog) {
                            DeleteInvoiceConfirmationDialog(
                                onConfirm = {
                                    invoiceToDelete?.let { invoiceViewModel.deleteInvoice(it.invoice) }
                                    showDeleteConfirmationDialog = false
                                },
                                onDismiss = { showDeleteConfirmationDialog = false },
                                invoiceNumber = invoiceToDelete?.invoice?.invoiceNumber ?: ""
                            )
                        }
                    }
                }
            }
            if (showDialog) {
                currentCustomer?.let {
                    AddInvoiceDialog(
                        onDismiss = { showDialog = false },
                        onConfirm = { newInvoice, lineItems ->
                            coroutineScope.launch {
                                val newInvoiceId = invoiceViewModel.addInvoice(newInvoice)
                                lineItems.forEach { lineItem ->
                                    lineItemViewModel.addLineItem(lineItem.copy(invoiceId = newInvoiceId.toInt()))
                                }
                                showDialog = false
                            }
                        },
                        invoiceViewModel = invoiceViewModel,
                        customer = it,
                        businessInfo = businessInfo
                    )
                }
            }
        }
    }
}

@Composable
fun AddInvoiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (Invoice, List<LineItem>) -> Unit,
    invoiceViewModel: InvoiceViewModel,
    customer: Customer,
    businessInfo: BusinessInfo?
) {
    val coroutineScope = rememberCoroutineScope()
    var invoiceNumber by remember { mutableStateOf("") }
    var lineItems by remember { mutableStateOf(listOf<LineItem>()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            invoiceNumber = invoiceViewModel.generateInvoiceNumber()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.add_invoice), style = MaterialTheme.typography.headlineSmall)

                businessInfo?.let {
                    Text("${it.name}\n${it.address}\n${it.email}", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("${customer.name}\n${customer.address}", style = MaterialTheme.typography.bodySmall)

                TextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text(stringResource(R.string.invoice_number, "")) },
                    modifier = Modifier.fillMaxWidth()
                )

                lineItems.forEach { item ->
                    ListItem(
                        headlineContent = { Text(item.job) },
                        supportingContent = { Text("${item.quantity} x $${item.unitPrice}") }
                    )
                }

                var showAddLineItemDialog by remember { mutableStateOf(false) }

                FilledTonalButton(onClick = { showAddLineItemDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.add_line_item))
                }

                if (showAddLineItemDialog) {
                    AddLineItemDialog(
                        onDismiss = { showAddLineItemDialog = false },
                        onAdd = { newItem ->
                            lineItems = lineItems + newItem
                            showAddLineItemDialog = false
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        val newInvoice = Invoice(
                            id = 0,
                            customerId = customer.id,
                            invoiceNumber = invoiceNumber,
                            date = Date().time,
                            dueDate = Date().time + 1209600000, // 2 weeks in milliseconds
                        )
                        onConfirm(newInvoice, lineItems)
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteInvoiceConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    invoiceNumber: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.confirm_delete_invoice))
                Text(invoiceNumber)
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

@Composable
fun AddLineItemDialog(
    onDismiss: () -> Unit,
    onAdd: (LineItem) -> Unit
) {
    var job by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.add_line_item), style = MaterialTheme.typography.headlineSmall)

                TextField(
                    value = job,
                    onValueChange = { job = it },
                    label = { Text(stringResource(R.string.job)) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(stringResource(R.string.details)) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.quantity)) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = { Text(stringResource(R.string.unit_price)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        val newItem = LineItem(
                            id = 0, // Room will auto-generate
                            invoiceId = 0, // This will be set later
                            job = job,
                            details = details,
                            quantity = quantity.toIntOrNull() ?: 1,
                            unitPrice = unitPrice.toDoubleOrNull() ?: 0.0
                        )
                        onAdd(newItem)
                    }) {
                        Text(stringResource(R.string.add_line_item))
                    }
                }
            }
        }
    }
}