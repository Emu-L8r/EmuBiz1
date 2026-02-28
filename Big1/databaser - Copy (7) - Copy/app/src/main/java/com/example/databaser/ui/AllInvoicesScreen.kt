package com.example.databaser.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllInvoicesScreen(
    onInvoiceClick: (Int, Int) -> Unit,
    onAddInvoiceClick: () -> Unit,
    navController: NavHostController,
    onBackClick: () -> Unit,
    invoiceViewModel: InvoiceViewModel = hiltViewModel()
) {
    val uiState by invoiceViewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by invoiceViewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopAppBar(title = stringResource(id = R.string.all_invoices), navController = navController, canNavigateBack = true, onNavigateUp = onBackClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d("AllInvoicesScreen", "Add invoice clicked")
                onAddInvoiceClick()
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_invoice))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { invoiceViewModel.onSearchQueryChange(it) },
                onSearch = { },
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by customer or invoice #") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            modifier = Modifier.clickable { invoiceViewModel.onSearchQueryChange("") }
                        )
                    }
                }
            ) {}

            if (uiState.invoices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Image(painter = painterResource(id = R.drawable.no_invoices), contentDescription = "No invoices", modifier = Modifier.size(128.dp))
                        Text(text = "No invoices yet.", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        Text(text = "Click the '+' button to create one.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    val groupedInvoices = uiState.invoices.groupBy { it.invoice.status }
                    groupedInvoices.forEach { (status, invoices) ->
                        stickyHeader {
                            Text(
                                text = status,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp)
                            )
                        }
                        items(invoices) { invoice ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { 
                                    if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                        invoiceViewModel.updateInvoiceStatus(invoice.invoice.id, isPaid = false, isSent = false, isHidden = true)
                                    }
                                    true 
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                                        else -> Color.Transparent
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete"
                                        )
                                    }
                                },
                            ){
                                InvoiceListItem(
                                    invoice = invoice,
                                    onInvoiceClick = { _, _ -> onInvoiceClick(invoice.invoice.id, invoice.customer.id) },
                                    invoiceViewModel = invoiceViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
