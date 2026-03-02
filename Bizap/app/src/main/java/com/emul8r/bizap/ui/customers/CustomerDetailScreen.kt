package com.emul8r.bizap.ui.customers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    onCustomerDeleted: () -> Unit = {},
    onNavigateToEdit: (Long) -> Unit = {},
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is CustomerDetailEvent.CustomerDeleted -> onCustomerDeleted()
                is CustomerDetailEvent.CustomerUpdated -> {
                    // Refresh the customer data
                    viewModel.loadCustomer(customerId)
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Customer Details") }) }
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
                val address = customer.address
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Text(customer.name, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(16.dp))
                    InfoRow(icon = Icons.Default.Business, text = customer.businessName ?: "N/A")
                    InfoRow(icon = Icons.Default.Email, text = customer.email ?: "N/A")
                    InfoRow(icon = Icons.Default.Phone, text = customer.phone ?: "N/A")
                    if (!address.isNullOrBlank()) {
                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            text = address,
                            modifier = Modifier.clickable {
                                val gmmIntentUri = Uri.parse("geo:0,0?q=$address")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            }
                        )
                    } else {
                        InfoRow(icon = Icons.Default.LocationOn, text = "N/A")
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToEdit(customer.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Edit")
                        }

                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Customer") },
                            text = { Text("Are you sure you want to delete ${customer.name}? This action cannot be undone.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.deleteCustomer(customer.id)
                                        showDeleteDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
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
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

