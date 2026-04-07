package com.emul8r.bizap.ui.customers

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
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    businessId: Long? = null,
    onEdit: () -> Unit = {},
    onBack: () -> Unit = {},
    onCustomerDeleted: () -> Unit = {},
    onNavigateToEdit: (Long) -> Unit = {},
) {
    // GUI2 only - GUI1 legacy code removed
    CustomerDetailScreenV2Content(
        businessId = businessId ?: 1L,
        customerId = customerId,
        onEdit = onEdit,
        onBack = onBack,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailScreenV2Content(
    businessId: Long,
    customerId: Long,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

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
                    if (uiState is CustomerDetailUiState.Success) {
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
            is CustomerDetailUiState.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is CustomerDetailUiState.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is CustomerDetailUiState.Success -> {
                CustomerDetailV2Content(
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
                        viewModel.deleteCustomer(customerId)
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
private fun CustomerDetailV2Content(
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
        CustomerDetailSection(title = "Name") {
            Text(
                text = customer.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        customer.businessName?.takeIf { it.isNotBlank() }?.let { name ->
            CustomerDetailSection(title = "Business Name") {
                Text(text = name, style = MaterialTheme.typography.bodyLarge)
            }
        }

        customer.email?.takeIf { it.isNotBlank() }?.let { email ->
            CustomerDetailSection(title = "Email") {
                Text(text = email, style = MaterialTheme.typography.bodyLarge)
            }
        }

        customer.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            CustomerDetailSection(title = "Phone") {
                Text(text = phone, style = MaterialTheme.typography.bodyLarge)
            }
        }

        customer.address?.takeIf { it.isNotBlank() }?.let { address ->
            CustomerDetailSection(title = "Address") {
                Text(text = address, style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (customer.notes.isNotBlank()) {
            CustomerDetailSection(title = "Notes") {
                Text(text = customer.notes, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun CustomerDetailSection(
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
