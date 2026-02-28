package com.example.databaser.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.databaser.ui.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    navController: NavController,
    viewModel: CustomerViewModel = hiltViewModel(),
    aFor: String = "view"
) {
    val customers by viewModel.customers.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (aFor == "view") "Customers" else "Select Customer") })
        },
        floatingActionButton = {
            if (aFor == "view") {
                FloatingActionButton(onClick = { navController.navigate("${Routes.CUSTOMER_DETAILS}/0") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Customer")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(customers) { customer ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            when (aFor) {
                                "view" -> navController.navigate("${Routes.CUSTOMER_DETAILS}/${customer.id}")
                                "invoice" -> navController.navigate("${Routes.INVOICE_DETAILS}/${customer.id}/0")
                                "quote" -> navController.navigate("${Routes.QUOTE_DETAILS}/${customer.id}/0")
                            }
                        }
                ) {
                    Text(
                        text = customer.name,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
