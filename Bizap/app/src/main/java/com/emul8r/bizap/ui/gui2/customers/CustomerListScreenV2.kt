package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import timber.log.Timber

/**
 * GUI2 Customer List Screen
 * Displays all customers for the active business with CRUD operations.
 *
 * @param businessId The business context
 * @param onCustomerClick Navigate to customer detail
 * @param onCreateCustomer Navigate to create customer screen
 * @param onBack Navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreenV2(
    businessId: Long,
    onCustomerClick: (Long) -> Unit,
    onCreateCustomer: () -> Unit,
    onBack: () -> Unit,
    viewModel: CustomerListViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateCustomer) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CustomerListUiStateV2.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is CustomerListUiStateV2.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is CustomerListUiStateV2.Success -> {
                CustomerListContent(
                    customers = state.customers,
                    onCustomerClick = onCustomerClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun CustomerListContent(
    customers: List<Customer>,
    onCustomerClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (customers.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No customers yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tap + to add your first customer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(customers) { customer ->
                CustomerCardV2(
                    customer = customer,
                    onClick = { onCustomerClick(customer.id) }
                )
            }
        }
    }
}

@Composable
private fun CustomerCardV2(
    customer: Customer,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = customer.name,
                style = MaterialTheme.typography.titleMedium
            )

            if (customer.businessName.isNotBlank()) {
                Text(
                    text = customer.businessName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (customer.email.isNotBlank()) {
                Text(
                    text = customer.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (customer.phone.isNotBlank()) {
                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// UI State
sealed interface CustomerListUiStateV2 {
    object Loading : CustomerListUiStateV2
    data class Error(val message: String) : CustomerListUiStateV2
    data class Success(val customers: List<Customer>) : CustomerListUiStateV2
}


