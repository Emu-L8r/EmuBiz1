package com.emul8r.bizap.ui.customers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerScreen(
    customerId: Long,
    onCustomerSaved: () -> Unit = {},
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showValidationError by remember { mutableStateOf("") }

    var name by rememberSaveable { mutableStateOf("") }
    var businessName by rememberSaveable { mutableStateOf("") }
    var businessNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    LaunchedEffect(uiState) {
        if (uiState is CustomerDetailUiState.Success) {
            val customer = (uiState as CustomerDetailUiState.Success).customer
            name = customer.name
            businessName = customer.businessName ?: ""
            businessNumber = customer.businessNumber ?: ""
            email = customer.email ?: ""
            phone = customer.phone ?: ""
            address = customer.address ?: ""
            notes = customer.notes
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is CustomerDetailEvent.CustomerUpdated -> {
                    snackbarHostState.showSnackbar("Customer saved successfully")
                    onCustomerSaved()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(showValidationError) {
        if (showValidationError.isNotEmpty()) {
            snackbarHostState.showSnackbar(showValidationError)
            showValidationError = ""
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Customer") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is CustomerDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CustomerDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is CustomerDetailUiState.Success -> {
                val customer = state.customer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = businessNumber,
                        onValueChange = { businessNumber = it },
                        label = { Text("Business Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                showValidationError = "Customer name is required"
                                return@Button
                            }

                            val updatedCustomer = customer.copy(
                                name = name,
                                businessName = businessName.ifBlank { null },
                                businessNumber = businessNumber.ifBlank { null },
                                email = email.ifBlank { null },
                                phone = phone.ifBlank { null },
                                address = address.ifBlank { null },
                                notes = notes,
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.updateCustomer(updatedCustomer)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}




