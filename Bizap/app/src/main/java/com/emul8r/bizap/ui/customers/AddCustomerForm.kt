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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AddCustomerForm(
    viewModel: CustomerViewModel,
    onCustomerSaved: () -> Unit
) {
    val formState = viewModel.formState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text("New Customer", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Show validation error messages if any
        formState.value.validationError?.let { error ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Show database/save error messages if any
        formState.value.error?.let { error ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

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
            enabled = viewModel.customerName.isNotBlank() && !formState.value.isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (formState.value.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving...")
            } else {
                Text("Create Customer")
            }
        }
    }
}
