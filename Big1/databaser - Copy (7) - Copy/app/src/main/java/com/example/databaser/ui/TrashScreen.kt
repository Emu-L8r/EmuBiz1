package com.example.databaser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.databaser.R
import com.example.databaser.data.Customer
import com.example.databaser.viewmodel.TrashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    trashViewModel: TrashViewModel = hiltViewModel()
) {
    val deletedCustomers by trashViewModel.deletedCustomers.collectAsState()
    var showRestoreDialog by remember { mutableStateOf<Customer?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Customer?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.trash)) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                placeholder = { Text("Search in trash") },
                content = {}
            )
            if (deletedCustomers.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(id = R.string.empty_trash))
                }
            } else {
                LazyColumn {
                    items(items = deletedCustomers.filter { it.name.contains(searchQuery, ignoreCase = true) }, key = { it.id }) { customer ->
                        TrashItem(
                            customer = customer,
                            onRestore = { showRestoreDialog = customer },
                            onDelete = { showDeleteDialog = customer }
                        )
                    }
                }
            }
        }

        showRestoreDialog?.let { customer ->
            AlertDialog(
                onDismissRequest = { showRestoreDialog = null },
                title = { Text(stringResource(id = R.string.restore_customer)) },
                text = { Text(stringResource(id = R.string.confirm_restore_customer)) },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            trashViewModel.restoreCustomer(customer)
                            showRestoreDialog = null
                        }
                    ) {
                        Text(stringResource(id = R.string.restore))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = null }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }

        showDeleteDialog?.let { customer ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text(stringResource(id = R.string.permanently_delete_customer_title)) },
                text = { Text(stringResource(id = R.string.confirm_permanently_delete_customer)) },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            trashViewModel.permanentlyDeleteCustomer(customer)
                            showDeleteDialog = null
                         }
                    ) {
                        Text(stringResource(id = R.string.permanently_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun TrashItem(customer: Customer, onRestore: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = customer.name, modifier = Modifier.weight(1f))
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.RestoreFromTrash, contentDescription = stringResource(id = R.string.restore))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteForever, contentDescription = stringResource(id = R.string.permanently_delete))
            }
        }
    }
}
