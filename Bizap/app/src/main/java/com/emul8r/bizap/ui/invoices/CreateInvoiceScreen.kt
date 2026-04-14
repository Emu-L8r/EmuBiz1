package com.emul8r.bizap.ui.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.domain.model.Customer

/**
 * GUI1 Invoice Creation Screen - Wrapper using UnifiedCreateInvoicePage
 *
 * **Purpose:**
 * Provides GUI1-specific wrapper around the unified invoice creation page.
 * Maintains backward compatibility with existing navigation and callbacks.
 *
 * **Features:**
 * - Reuses all functionality from UnifiedCreateInvoicePage
 * - GUI1 styling and layout conventions
 * - Responsive to phone and tablet form factors
 * - Full invoice creation workflow
 *
 * **Delegates To:**
 * @see UnifiedCreateInvoicePage - Core invoice creation logic
 *
 * @param viewModel CreateInvoiceViewModel managing form state
 * @param onInvoiceSaved Callback when invoice successfully saved (navigate back)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    viewModel: CreateInvoiceViewModel = hiltViewModel(),
    onInvoiceSaved: () -> Unit
) {
    // Delegate to unified page - reuses all functionality
    UnifiedCreateInvoicePage(
        businessId = 0L, // GUI1 uses context from ViewModel
        onInvoiceSaved = onInvoiceSaved,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDropdown(
    selectedCustomer: Customer?,
    customers: List<Customer>,
    onSelect: (Customer) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCustomer?.name ?: "Select Customer",
            onValueChange = {},
            readOnly = true,
            label = { Text("Customer") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            customers.forEach { customer ->
                DropdownMenuItem(
                    text = { Text(customer.name) },
                    onClick = {
                        onSelect(customer)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LineItemEditor(
    description: String,
    quantity: Double,
    unitPrice: Long,
    onUpdate: (String, Double, Long) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = { onUpdate(it, quantity, unitPrice) },
            label = { Text("Service/Item") },
            modifier = Modifier.weight(1.5f)
        )
        OutlinedTextField(
            value = if (quantity == 0.0) "" else quantity.toString(),
            onValueChange = { it.toDoubleOrNull()?.let { valQty -> onUpdate(description, valQty, unitPrice) } },
            label = { Text("Qty") },
            modifier = Modifier.weight(0.5f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = if (unitPrice == 0L) "" else (unitPrice.toDouble() / 100.0).toString(),
            onValueChange = { it.toDoubleOrNull()?.let { valPrice -> onUpdate(description, quantity, (valPrice * 100).toLong()) } },
            label = { Text("$") },
            modifier = Modifier.weight(0.7f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
        }
    }
}
