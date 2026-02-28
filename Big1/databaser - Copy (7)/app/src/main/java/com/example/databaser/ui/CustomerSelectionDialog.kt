package com.example.databaser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.databaser.R
import com.example.databaser.data.Customer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectionDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onCustomerSelected: (Int) -> Unit,
    onNewCustomer: () -> Unit,
    title: String,
    newCustomerText: String,
    cancelText: String
) {
    var searchQuery by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { },
                    placeholder = { Text(stringResource(id = R.string.search)) },
                    active = false,
                    onActiveChange = {},
                    modifier = Modifier.fillMaxWidth()
                ) { }

                LazyColumn(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(customers.filter { it.name.contains(searchQuery, ignoreCase = true) }) { customer ->
                        ListItem(
                            headlineContent = { Text(customer.name) },
                            modifier = Modifier.clickable { onCustomerSelected(customer.id) }
                        )
                    }
                }
                Row {
                    TextButton(onClick = onNewCustomer) {
                        Text(newCustomerText)
                    }
                    TextButton(onClick = onDismiss) {
                        Text(cancelText)
                    }
                }
            }
        }
    }
}
