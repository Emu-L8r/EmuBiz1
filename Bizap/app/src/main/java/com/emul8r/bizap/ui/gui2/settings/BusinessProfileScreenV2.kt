package com.emul8r.bizap.ui.gui2.settings

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
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import timber.log.Timber

/**
 * GUI2 Business Profile Screen
 * Edit business profile information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreenV2(
    onBack: () -> Unit,
    viewModel: BusinessProfileViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BusinessProfileUiStateV2.Loading -> {
                LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            }
            is BusinessProfileUiStateV2.Error -> {
                ErrorStateV2(
                    message = state.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is BusinessProfileUiStateV2.Success -> {
                BusinessProfileForm(
                    initialProfile = state.businessProfile,
                    onSave = { profile ->
                        viewModel.updateBusinessProfile(profile)
                        onBack()
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun BusinessProfileForm(
    initialProfile: BusinessProfile,
    onSave: (BusinessProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var businessName by remember(initialProfile) { mutableStateOf(initialProfile.businessName) }
    var businessAbn by remember(initialProfile) { mutableStateOf(initialProfile.businessAbn) }
    var businessAddress by remember(initialProfile) { mutableStateOf(initialProfile.businessAddress) }
    var businessPhone by remember(initialProfile) { mutableStateOf(initialProfile.businessPhone) }
    var businessEmail by remember(initialProfile) { mutableStateOf(initialProfile.businessEmail) }
    var isSaving by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Business Name
        OutlinedTextField(
            value = businessName,
            onValueChange = {
                businessName = it
                nameError = null
            },
            label = { Text("Business Name *") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } }
        )

        // ABN
        OutlinedTextField(
            value = businessAbn,
            onValueChange = { businessAbn = it },
            label = { Text("ABN/Tax ID") },
            modifier = Modifier.fillMaxWidth()
        )

        // Address
        OutlinedTextField(
            value = businessAddress,
            onValueChange = { businessAddress = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        // Phone
        OutlinedTextField(
            value = businessPhone,
            onValueChange = { businessPhone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        // Email
        OutlinedTextField(
            value = businessEmail,
            onValueChange = { businessEmail = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                if (businessName.isBlank()) {
                    nameError = "Business name is required"
                    return@Button
                }

                isSaving = true
                onSave(
                    initialProfile.copy(
                        businessName = businessName,
                        businessAbn = businessAbn,
                        businessAddress = businessAddress,
                        businessPhone = businessPhone,
                        businessEmail = businessEmail
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
                Text("Save Profile")
            }
        }
    }
}

sealed interface BusinessProfileUiStateV2 {
    object Loading : BusinessProfileUiStateV2
    data class Error(val message: String) : BusinessProfileUiStateV2
    data class Success(val businessProfile: BusinessProfile) : BusinessProfileUiStateV2
}

