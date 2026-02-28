package com.example.databaser.ui

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.data.Customer
import com.example.databaser.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onCustomerClick: (Int) -> Unit,
    onAddCustomerClick: () -> Unit,
    navController: NavHostController,
    onBackClick: () -> Unit,
    customerViewModel: CustomerViewModel = hiltViewModel()
) {
    val customers by customerViewModel.allCustomers.collectAsStateWithLifecycle(initialValue = null)
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val cameFromAddInvoice by remember { mutableStateOf(navController.previousBackStackEntry?.destination?.route?.startsWith("addeditinvoice") ?: false) }


    Scaffold(
        topBar = { 
            AppTopAppBar(
                title = stringResource(id = R.string.customers), 
                navController = navController,
                canNavigateBack = true,
                onNavigateUp = onBackClick
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCustomerClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_customer))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { },
                active = false,
                onActiveChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(id = R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            modifier = Modifier.clickable { searchQuery = "" }
                        )
                    }
                }
            ) {}
            if (customers == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (customers!!.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.no_customers_added))
                }
            } else {
                LazyColumn {
                    items(customers!!.filter { it.name.contains(searchQuery, ignoreCase = true) }, key = { it.id }) { customer ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        val currentValue = dismissState.currentValue
                        LaunchedEffect(currentValue) {
                            if (currentValue != SwipeToDismissBoxValue.Settled) {
                                customerToDelete = customer
                                showDeleteConfirmationDialog = true
                                dismissState.reset()
                            }
                        }

                        Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                                            else -> MaterialTheme.colorScheme.errorContainer
                                        }, label = ""
                                    )
                                    val scale by animateFloatAsState(
                                        if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                                        label = ""
                                    )

                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(id = R.string.delete),
                                            modifier = Modifier.scale(scale),
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            ) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer, headlineColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                    headlineContent = { Text(customer.name, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier
                                        .clickable { 
                                            Log.d("CustomerListScreen", "Customer clicked: ${customer.id}, cameFromAddInvoice: $cameFromAddInvoice")
                                            if(cameFromAddInvoice) {
                                                navController.previousBackStackEntry?.savedStateHandle?.set("customerId", customer.id)
                                                navController.popBackStack()
                                            } else {
                                                onCustomerClick(customer.id)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        DeleteCustomerConfirmationDialog(
            onConfirm = {
                customerToDelete?.let { customerViewModel.deleteCustomer(it) }
                showDeleteConfirmationDialog = false
            },
            onDismiss = {
                showDeleteConfirmationDialog = false
            },
            customerName = customerToDelete?.name ?: ""
        )
    }
}

@Composable
private fun DeleteCustomerConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    customerName: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.confirm_delete_customer))
                Text(customerName)
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
