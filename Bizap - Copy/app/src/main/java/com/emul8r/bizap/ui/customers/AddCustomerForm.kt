package com.emul8r.bizap.ui.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddCustomerForm(
    viewModel: CustomerViewModel,
    onCustomerSaved: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text("New Customer", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        // --- Primary Contact ---
        OutlinedTextField(
            value = viewModel.customerName,
            onValueChange = { viewModel.customerName = it },
            label = { Text("Contact Person *") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // --- Business Details ---
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.businessName,
            onValueChange = { viewModel.businessName = it },
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = viewModel.businessNumber,
            onValueChange = { viewModel.businessNumber = it },
            label = { Text("ABN / Tax ID") },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Communication ---
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.phone,
            onValueChange = { viewModel.phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // --- Physical Address ---
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.address,
            onValueChange = { viewModel.address = it },
            label = { Text("Physical Address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.saveNewCustomer(onSuccess = onCustomerSaved) },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.customerName.isNotBlank()
        ) {
            Text("Create Customer")
        }
    }
}
