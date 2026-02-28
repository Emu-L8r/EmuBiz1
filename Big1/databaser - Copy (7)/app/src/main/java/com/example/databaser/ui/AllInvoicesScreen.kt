package com.example.databaser.ui

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.databaser.R
import com.example.databaser.data.InvoiceWithCustomerAndLineItems
import com.example.databaser.ui.theme.DarkGreen
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllInvoicesScreen(
    onInvoiceClick: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModel.Factory),
    customerViewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.Factory)
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    val customers by customerViewModel.allCustomers.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopAppBar(title = stringResource(id = R.string.invoices), onNavigate = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_invoice))
            }
        }
    ) {
        if (showDialog) {
            CustomerSelectionDialog(
                customers = customers,
                onDismiss = { showDialog = false },
                onCustomerSelected = { customerId ->
                    showDialog = false
                    onNavigate("addeditinvoice?invoiceId=-1&customerId=$customerId")
                },
                onNewCustomer = {
                    showDialog = false
                    onNavigate("addeditcustomer?customerId=-1")
                },
                title = stringResource(id = R.string.select_customer),
                newCustomerText = stringResource(id = R.string.new_customer),
                cancelText = stringResource(id = R.string.cancel)
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.no_invoices_found))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(uiState.invoices, key = { it.invoice.id }) { invoice ->
                    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
                    var invoiceToDelete by remember { mutableStateOf<InvoiceWithCustomerAndLineItems?>(null) }
                    val dismissState = rememberSwipeToDismissBoxState()
                    var statusColor by remember { mutableStateOf(Color.Transparent) }

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            invoiceToDelete = invoice
                            showDeleteConfirmationDialog = true
                            dismissState.reset()
                        }
                    }

                    Card(
                        modifier = Modifier.padding(8.dp),
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

                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(id = R.string.delete),
                                            modifier = Modifier.scale(scale)
                                        )
                                    }
                                }
                            }
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            ListItem(
                                headlineContent = { Text(invoice.customer.name, fontWeight = FontWeight.Bold) },
                                supportingContent = {
                                    Column {
                                        Text("${stringResource(id = R.string.invoice_number)}: ${invoice.invoice.invoiceNumber}")
                                        Text("Date: ${dateFormat.format(Date(invoice.invoice.date))}")
                                        val statusText = when {
                                            invoice.invoice.isHidden -> "Hidden"
                                            invoice.invoice.isPaid -> stringResource(id = R.string.paid)
                                            invoice.invoice.isSent -> stringResource(id = R.string.sent)
                                            else -> stringResource(id = R.string.unsent)
                                        }
                                        LaunchedEffect(invoice.invoice.isPaid, invoice.invoice.isSent, invoice.invoice.isHidden) {
                                            statusColor = when {
                                                invoice.invoice.isHidden -> Color.Gray
                                                invoice.invoice.isPaid -> DarkGreen
                                                invoice.invoice.isSent -> Color.Blue
                                                else -> Color.Red
                                            }
                                        }
                                        Text(
                                            text = "${stringResource(id = R.string.status)}: $statusText",
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                trailingContent = {
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
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.paid)) },
                                            onClick = { 
                                                invoiceViewModel.updateInvoiceStatus(invoice.invoice.id, isPaid = true, isSent = true, isHidden = false)
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.sent)) },
                                            onClick = { 
                                                invoiceViewModel.updateInvoiceStatus(invoice.invoice.id, isPaid = false, isSent = true, isHidden = false)
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.unsent)) },
                                            onClick = { 
                                                invoiceViewModel.updateInvoiceStatus(invoice.invoice.id, isPaid = false, isSent = false, isHidden = false)
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Hide") },
                                            onClick = { 
                                                invoiceViewModel.updateInvoiceStatus(invoice.invoice.id, isPaid = invoice.invoice.isPaid, isSent = invoice.invoice.isSent, isHidden = true)
                                                expanded = false
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.clickable { onInvoiceClick(invoice.invoice.id) }
                            )
                        }
                    }

                    if (showDeleteConfirmationDialog) {
                        ConfirmationDialog(
                            title = stringResource(id = R.string.confirm_delete_invoice),
                            text = invoiceToDelete?.invoice?.invoiceNumber ?: "",
                            onConfirm = {
                                invoiceToDelete?.let { invoiceViewModel.deleteInvoice(it.invoice) }
                                showDeleteConfirmationDialog = false
                            },
                            onDismiss = { showDeleteConfirmationDialog = false }
                        )
                    }
                }
            }
        }
    }
}
