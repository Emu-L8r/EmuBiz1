package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.R
import timber.log.Timber

/**
 * GUI2 Create Customer Screen - UI Layer Only
 *
 * **Responsibilities (UI Layer):**
 * - Render form fields
 * - Display validation errors from ViewModel
 * - Show loading state
 * - Navigate back on success
 *
 * **NO Business Logic:**
 * - No local form state (all in ViewModel)
 * - No validation logic (all in ViewModel)
 * - No callbacks (UI observes StateFlow)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomerScreenV2(
    businessId: Long,
    onCreate: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateCustomerViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate back on successful customer creation
    LaunchedEffect(uiState.customerCreated) {
        if (uiState.customerCreated) {
            onCreate()
        }
    }

    // Show general error in snackbar
    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.screen_title_create_customer), style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name (required) - delegates to ViewModel
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            // Business Name - delegates to ViewModel
            OutlinedTextField(
                value = uiState.businessName,
                onValueChange = { viewModel.updateBusinessName(it) },
                label = { Text("Business Name") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Email (optional) - delegates to ViewModel
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            // Phone - delegates to ViewModel
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = { viewModel.updatePhone(it) },
                label = { Text("Phone") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Address - delegates to ViewModel
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { viewModel.updateAddress(it) },
                label = { Text("Address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // Notes - delegates to ViewModel
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button - NO validation here, delegates to ViewModel
            Button(
                onClick = { viewModel.createCustomer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Customer")
                }
            }
        }
    }
}

