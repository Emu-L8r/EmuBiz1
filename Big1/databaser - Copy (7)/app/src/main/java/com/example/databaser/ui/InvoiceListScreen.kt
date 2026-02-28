package com.example.databaser.ui

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.databaser.R
import com.example.databaser.data.Customer
import com.example.databaser.data.Invoice
import com.example.databaser.data.InvoiceWithCustomerAndLineItems
import com.example.databaser.data.LineItem
import com.example.databaser.viewmodel.InvoiceViewModel
import com.example.databaser.viewmodel.LineItemViewModel
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun InvoiceListScreen(
    invoiceViewModel: InvoiceViewModel,
    lineItemViewModel: LineItemViewModel,
    onInvoiceClick: (Int) -> Unit,
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var currentCustomer by remember { mutableStateOf<Customer?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_invoice))
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyColumn {
                items(uiState.invoices) { invoice ->
                    val context = LocalContext.current
                    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
                    var invoiceToDelete by remember { mutableStateOf<InvoiceWithCustomerAndLineItems?>(null) }
                    val density = LocalDensity.current
                    val positionalThreshold = with(density) { 150.dp.toPx() }
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                invoiceToDelete = invoice
                                showDeleteConfirmationDialog = true
                                false
                            } else {
                                true
                            }
                        },
                        positionalThreshold = { positionalThreshold }
                    )

                    LaunchedEffect(showDeleteConfirmationDialog) {
                        if (!showDeleteConfirmationDialog) {
                            dismissState.reset()
                        }
                    }

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { onInvoiceClick(invoice.invoice.id) },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
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
                                        modifier = Modifier
                                            .scale(scale)
                                            .alpha(alpha),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
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
                                invoice.invoice.isPaid -> MaterialTheme.colorScheme.primary
                                invoice.invoice.isSent -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.tertiary
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
                        customer = it
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
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.add_invoice), style = MaterialTheme.typography.headlineSmall)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("${customer.name}\n${customer.address}", style = MaterialTheme.typography.bodySmall)

                ThemedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = stringResource(R.string.invoice_number, ""),
                    modifier = Modifier.fillMaxWidth()
                )

                lineItems.forEach { item ->
                    ListItem(
                        headlineContent = { Text(item.job) },
                        supportingContent = { Text("${item.quantity} x $${item.unitPrice}") }
                    )
                }

                var showAddLineItemDialog by remember { mutableStateOf(false) }

                Button(onClick = { showAddLineItemDialog = true }, modifier = Modifier.fillMaxWidth()) {
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
                    Button(onClick = {
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
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.confirm_delete_invoice))
                Text(invoiceNumber)
                Row {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
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
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.add_line_item), style = MaterialTheme.typography.headlineSmall)

                ThemedTextField(
                    value = job,
                    onValueChange = { job = it },
                    label = stringResource(R.string.job),
                    modifier = Modifier.fillMaxWidth()
                )

                ThemedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = stringResource(R.string.details),
                    modifier = Modifier.fillMaxWidth()
                )

                ThemedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = stringResource(R.string.quantity),
                    modifier = Modifier.fillMaxWidth()
                )

                ThemedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it },
                    label = stringResource(R.string.unit_price),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(onClick = {
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
