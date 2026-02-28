package com.example.databaser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    customerId: Int,
    onInvoiceClick: (Int) -> Unit,
    onAddInvoiceClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    navController: NavHostController,
    invoiceViewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by invoiceViewModel.getInvoicesForCustomer(customerId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.invoices),
                canNavigateBack = true,
                onNavigateUp = onBackClick,
                navController = navController
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddInvoiceClick(customerId) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_invoice))
            }
        }
    ) { padding ->
        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.no_invoices_found))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(invoices) { invoice ->
                    ListItem(
                        headlineContent = { Text(invoice.invoice.invoiceNumber) },
                        modifier = Modifier.clickable { onInvoiceClick(invoice.invoice.id) }
                    )
                }
            }
        }
    }
}
