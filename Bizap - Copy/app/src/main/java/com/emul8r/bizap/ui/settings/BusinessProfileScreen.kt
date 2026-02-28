package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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

@Composable
fun BusinessProfileScreen(viewModel: BusinessProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profileState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Business Details", style = MaterialTheme.typography.headlineSmall)
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

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Billing Info", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = profile.bankName ?: "",
            onValueChange = { viewModel.updateProfile(profile.copy(bankName = it)) },
            label = { Text("Bank Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.accountName ?: "",
            onValueChange = { viewModel.updateProfile(profile.copy(accountName = it)) },
            label = { Text("Account Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.bsbNumber ?: "",
            onValueChange = { viewModel.updateProfile(profile.copy(bsbNumber = it)) },
            label = { Text("BSB Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = profile.accountNumber ?: "",
            onValueChange = { viewModel.updateProfile(profile.copy(accountNumber = it)) },
            label = { Text("Account Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
