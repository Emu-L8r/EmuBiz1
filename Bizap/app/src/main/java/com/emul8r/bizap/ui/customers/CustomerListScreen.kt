package com.emul8r.bizap.ui.customers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onCustomerClick: (Long) -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val customers by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") },
                actions = {
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = { viewModel.seedTestCustomers() }) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Seed Test Data",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            CustomerList(customers = customers, onCustomerClick = onCustomerClick)
        }
    }
}

