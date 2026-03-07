package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import timber.log.Timber

/**
 * GUI2 Edit Customer Screen
 * Form to edit an existing customer.
 *
 * @param businessId The business context
 * @param customerId The customer to edit
 * @param onUpdate Called when customer is updated
 * @param onBack Navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerScreenV2(
    businessId: Long,
    customerId: Long,
    onUpdate: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditCustomerViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Customer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is EditCustomerUiStateV2.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is EditCustomerUiStateV2.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is EditCustomerUiStateV2.Success -> {
                EditCustomerForm(
                    initialCustomer = state.customer,
                    onSave = { customer ->
                        viewModel.updateCustomer(customer, onUpdate)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EditCustomerForm(
    initialCustomer: Customer,
    onSave: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(initialCustomer) { mutableStateOf(initialCustomer.name) }
    var businessName by remember(initialCustomer) { mutableStateOf(initialCustomer.businessName) }
    var email by remember(initialCustomer) { mutableStateOf(initialCustomer.email) }
    var phone by remember(initialCustomer) { mutableStateOf(initialCustomer.phone) }
    var address by remember(initialCustomer) { mutableStateOf(initialCustomer.address) }
    var notes by remember(initialCustomer) { mutableStateOf(initialCustomer.notes) }
    var isSaving by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Name (required)
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = null
            },
            label = { Text("Name *") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = if (nameError != null) {{ Text(nameError!!) }} else null
        )

        // Business Name
        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        // Phone
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        // Address
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                if (name.isBlank()) {
                    nameError = "Name is required"
                    return@Button
                }

                isSaving = true
                onSave(
                    initialCustomer.copy(
                        name = name,
                        businessName = businessName,
                        email = email,
                        phone = phone,
                        address = address,
                        notes = notes
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save Changes")
            }
        }
    }
}

// UI State
sealed interface EditCustomerUiStateV2 {
    object Loading : EditCustomerUiStateV2
    data class Error(val message: String) : EditCustomerUiStateV2
    data class Success(val customer: Customer) : EditCustomerUiStateV2
}

