package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 * GUI2 Customer Detail Screen
 * Shows full customer information with edit and delete options.
 *
 * @param businessId The business context
 * @param customerId The customer to display
 * @param onEdit Navigate to edit customer
 * @param onBack Navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreenV2(
    businessId: Long,
    customerId: Long,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    viewModel: CustomerDetailViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is CustomerDetailUiStateV2.Success) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CustomerDetailUiStateV2.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is CustomerDetailUiStateV2.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is CustomerDetailUiStateV2.Success -> {
                CustomerDetailContent(
                    customer = state.customer,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Customer") },
            text = { Text("Are you sure you want to delete this customer? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer()
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CustomerDetailContent(
    customer: Customer,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name section
        DetailSection(title = "Name") {
            Text(
                text = customer.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Business information
        if (!customer.businessName.isNullOrBlank()) {
            DetailSection(title = "Business Name") {
                Text(
                    text = customer.businessName!!,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Contact information
        if (!customer.email.isNullOrBlank()) {
            DetailSection(title = "Email") {
                Text(
                    text = customer.email!!,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (!customer.phone.isNullOrBlank()) {
            DetailSection(title = "Phone") {
                Text(
                    text = customer.phone!!,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Address
        if (!customer.address.isNullOrBlank()) {
            DetailSection(title = "Address") {
                Text(
                    text = customer.address!!,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Notes
        if (customer.notes.isNotBlank()) {
            DetailSection(title = "Notes") {
                Text(
                    text = customer.notes,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

// UI State
sealed interface CustomerDetailUiStateV2 {
    object Loading : CustomerDetailUiStateV2
    data class Error(val message: String) : CustomerDetailUiStateV2
    data class Success(val customer: Customer) : CustomerDetailUiStateV2
}

