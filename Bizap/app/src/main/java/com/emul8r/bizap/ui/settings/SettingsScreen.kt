package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel

@Composable
fun SettingsScreen(viewModel: BusinessProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profileState.collectAsStateWithLifecycle()

    // Use safe defaults if profile is null
    val businessName = profile?.businessName ?: ""
    val abn = profile?.abn ?: ""
    val address = profile?.address ?: ""
    val email = profile?.email ?: ""
    val phone = profile?.phone ?: ""

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
            value = businessName,
            onValueChange = { newValue -> profile?.let { viewModel.updateProfile(it.copy(businessName = newValue)) } },
            label = { Text("Trading Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = abn,
            onValueChange = { newValue -> profile?.let { viewModel.updateProfile(it.copy(abn = newValue)) } },
            label = { Text("ABN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = { newValue -> profile?.let { viewModel.updateProfile(it.copy(address = newValue)) } },
            label = { Text("Business Address") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { newValue -> profile?.let { viewModel.updateProfile(it.copy(email = newValue)) } },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { newValue -> profile?.let { viewModel.updateProfile(it.copy(phone = newValue)) } },
            label = { Text("Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
