package com.emu.emubizwax.ui.invoices.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.emu.emubizwax.domain.model.Customer
import com.emu.emubizwax.ui.invoices.InvoiceFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelector(viewModel: InvoiceFormViewModel) {
    val customers by viewModel.customers.collectAsState()
    val selectedCustomer by viewModel.formState.collectAsState()
    var isCustomerDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isCustomerDropdownExpanded,
        onExpandedChange = { isCustomerDropdownExpanded = !isCustomerDropdownExpanded }
    ) {
        OutlinedTextField(
            value = selectedCustomer.selectedCustomer?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Customer") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCustomerDropdownExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = isCustomerDropdownExpanded,
            onDismissRequest = { isCustomerDropdownExpanded = false }
        ) {
            customers.forEach { customer ->
                DropdownMenuItem(
                    text = { Text(customer.name) },
                    onClick = {
                        viewModel.updateCustomer(customer)
                        isCustomerDropdownExpanded = false
                    }
                )
            }
        }
    }
}
