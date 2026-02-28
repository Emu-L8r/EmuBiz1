package com.emul8r.bizap.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val profile by viewModel.profileState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Business Details", style = MaterialTheme.typography.headlineSmall)
        Text(
            "These details appear on your Tax Invoices",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = profile.businessName,
            onValueChange = { viewModel.updateProfile(profile.copy(businessName = it)) },
            label = { Text("Trading Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.abn,
            onValueChange = { viewModel.updateProfile(profile.copy(abn = it)) },
            label = { Text("ABN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.address,
            onValueChange = { viewModel.updateProfile(profile.copy(address = it)) },
            label = { Text("Business Address") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = profile.email,
            onValueChange = { viewModel.updateProfile(profile.copy(email = it)) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.phone,
            onValueChange = { viewModel.updateProfile(profile.copy(phone = it)) },
            label = { Text("Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        BrandingSection(viewModel = viewModel)
    }
}

@Composable
fun BrandingSection(viewModel: SettingsViewModel) {
    val logoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { viewModel.saveLogo(it) }
    }

    Column {
        Text("Branding", style = MaterialTheme.typography.titleMedium)

        OutlinedButton(onClick = { 
            logoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }) {
            Icon(Icons.Default.AddPhotoAlternate, null)
            Text(if (viewModel.hasLogo) "Change Logo" else "Upload Logo")
        }
    }
}
