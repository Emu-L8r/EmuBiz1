package com.example.databaser.ui

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
fun AllQuotesScreen(
    onQuoteClick: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = InvoiceViewModel.Factory),
    customerViewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.Factory)
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    val customers by customerViewModel.allCustomers.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    val quoteDueString = stringResource(id = R.string.quote_due)
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopAppBar(title = stringResource(id = R.string.quotes), onNavigate = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_quote))
            }
        }
    ) {
        if (showDialog) {
            CustomerSelectionDialog(
                customers = customers,
                onDismiss = { showDialog = false },
                onCustomerSelected = { customerId ->
                    showDialog = false
                    onNavigate("addeditquote?quoteId=-1&customerId=$customerId")
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
        } else if (uiState.quotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.no_invoices_found))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(uiState.quotes, key = { it.invoice.id }) { quote ->
                    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
                    var quoteToDelete by remember { mutableStateOf<InvoiceWithCustomerAndLineItems?>(null) }
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            quoteToDelete = quote
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
                                headlineContent = { Text(quote.customer.name, fontWeight = FontWeight.Bold) },
                                supportingContent = {
                                    Column {
                                        Text("${stringResource(id = R.string.invoice_number)}: ${quote.invoice.invoiceNumber}")
                                        Text("Date: ${dateFormat.format(Date(quote.invoice.date))}")
                                        val statusText = when {
                                            quote.invoice.isHidden -> "Hidden"
                                            quote.invoice.isPaid -> stringResource(id = R.string.converted)
                                            quote.invoice.isSent -> stringResource(id = R.string.sent)
                                            else -> stringResource(id = R.string.draft)
                                        }
                                        val statusColor = when {
                                            quote.invoice.isHidden -> Color.Gray
                                            quote.invoice.isPaid -> DarkGreen
                                            quote.invoice.isSent -> Color.Blue
                                            else -> Color.Red
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
                                                    putExtra(CalendarContract.Events.TITLE, "$quoteDueString: ${quote.invoice.invoiceNumber}")
                                                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, quote.invoice.dueDate)
                                                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, quote.invoice.dueDate)
                                                }
                                                context.startActivity(intent)
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.sent)) },
                                            onClick = { 
                                                invoiceViewModel.updateInvoiceStatus(quote.invoice.id, isPaid = false, isSent = true, isHidden = false)
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.draft)) },
                                            onClick = { 
                                                invoiceViewModel.updateInvoiceStatus(quote.invoice.id, isPaid = false, isSent = false, isHidden = false)
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Hide") },
                                            onClick = { 
                                                invoiceViewModel.updateInvoiceStatus(quote.invoice.id, isPaid = false, isSent = false, isHidden = true)
                                                expanded = false
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.clickable { onQuoteClick(quote.invoice.id) }
                            )
                        }
                    }

                    if (showDeleteConfirmationDialog) {
                        DeleteQuoteConfirmationDialog(
                            onConfirm = {
                                quoteToDelete?.let { invoiceViewModel.deleteInvoice(it.invoice) }
                                showDeleteConfirmationDialog = false
                            },
                            onDismiss = { showDeleteConfirmationDialog = false },
                            quoteNumber = quoteToDelete?.invoice?.invoiceNumber ?: ""
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteQuoteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    quoteNumber: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.confirm_delete_invoice))
                Text(quoteNumber)
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
