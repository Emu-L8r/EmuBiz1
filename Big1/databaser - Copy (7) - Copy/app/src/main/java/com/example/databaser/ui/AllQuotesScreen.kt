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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.viewmodel.CustomerViewModel
import com.example.databaser.viewmodel.InvoiceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllQuotesScreen(
    onQuoteClick: (Int, Int) -> Unit,
    onAddQuoteClick: (Int) -> Unit,
    navController: NavHostController,
    onBackClick: () -> Unit,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val uiState by invoiceViewModel.uiState.collectAsState()
    val customers by customerViewModel.allCustomers.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    val quoteDueString = stringResource(id = R.string.quote_due)
    var showCustomerSelection by remember { mutableStateOf(false) }

    if (showCustomerSelection) {
        CustomerSelectionDialog(
            customers = customers,
            onDismiss = { showCustomerSelection = false },
            onCustomerSelected = { customerId ->
                onAddQuoteClick(customerId)
                showCustomerSelection = false
            },
            onNewCustomer = { navController.navigate("addeditcustomer?cameFromAddInvoice=true") },
            title = stringResource(id = R.string.select_customer),
            newCustomerText = stringResource(id = R.string.add_customer),
            cancelText = stringResource(id = R.string.cancel)
        )
    }

    Scaffold(
        topBar = { 
            AppTopAppBar(
                title = stringResource(id = R.string.quotes), 
                navController = navController,
                canNavigateBack = true,
                onNavigateUp = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCustomerSelection = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_quote))
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.quotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.no_invoices_found))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(uiState.quotes, key = { it.invoice.id }) { quote ->
                    val dismissState = rememberSwipeToDismissBoxState()
                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            invoiceViewModel.updateInvoiceStatus(quote.invoice.id, isPaid = false, isSent = false, isHidden = true)
                            dismissState.reset()
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).clip(MaterialTheme.shapes.large),
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                                    else -> MaterialTheme.colorScheme.errorContainer
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
                                            modifier = Modifier.scale(scale),
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer, headlineColor = MaterialTheme.colorScheme.onPrimaryContainer, supportingColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                headlineContent = { Text(quote.customer.name, fontWeight = FontWeight.Bold) },
                                supportingContent = {
                                    Column {
                                        Text("${stringResource(id = R.string.invoice_number)}: ${quote.invoice.invoiceNumber}")
                                        Text("Date: ${dateFormat.format(Date(quote.invoice.date))}")
                                        StatusIndicator(invoice = quote.invoice)
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
                                modifier = Modifier.clickable { onQuoteClick(quote.invoice.id, quote.customer.id) }
                            )
                        }
                }
            }
        }
    }
}
