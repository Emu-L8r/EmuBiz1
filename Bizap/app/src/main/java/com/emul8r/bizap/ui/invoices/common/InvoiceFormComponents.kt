package com.emul8r.bizap.ui.invoices.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.ui.components.CurrencySelector
import com.emul8r.bizap.ui.invoices.CustomerDropdown

/**
 * Reusable invoice form components for both GUI1 and GUI2.
 * These components maintain consistent structure and naming across interfaces.
 *
 * All components use standardized naming (Header/Subheader instead of Title/Subtitle)
 * and work identically in both GUI1 and GUI2 for feature parity.
 */

/**
 * Customer selection section - shared across both GUIs
 */
@Composable
fun CustomerSelectionSection(
    selectedCustomer: Customer?,
    customers: List<Customer>,
    onCustomerSelected: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    CustomerDropdown(
        selectedCustomer = selectedCustomer,
        customers = customers,
        onSelect = onCustomerSelected
    )
}

/**
 * Header and Subheader section - standardized naming
 */
@Composable
fun HeaderSubheaderSection(
    header: String,
    subheader: String,
    onHeaderChanged: (String) -> Unit,
    onSubheaderChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "📄 Invoice Header & Subheader",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = header,
                onValueChange = onHeaderChanged,
                label = { Text("Header") },
                placeholder = { Text("e.g., INVOICE") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true,
                supportingText = { Text("Main document title - appears at top of PDF") }
            )

            OutlinedTextField(
                value = subheader,
                onValueChange = onSubheaderChanged,
                label = { Text("Subheader (Optional)") },
                placeholder = { Text("e.g., Tax Invoice") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true,
                supportingText = { Text("Secondary title or document type") }
            )
        }
    }
}

/**
 * Currency selector section - shared across both GUIs
 */
@Composable
fun CurrencySelectorSection(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    CurrencySelector(
        selectedCurrency = selectedCurrency,
        onCurrencyChange = onCurrencyChange,
        isDarkMode = isDarkMode,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Notes and footer section
 */
@Composable
fun NotesAndFooterSection(
    notes: String,
    footer: String,
    onNotesChanged: (String) -> Unit,
    onFooterChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = footer,
            onValueChange = onFooterChanged,
            label = { Text("Footer") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Save/Cancel button section
 */
@Composable
fun SaveCancelButtons(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }

        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp),
                    strokeWidth = 2.dp
                )
            }
            Text(if (isSaving) "Saving..." else "Save")
        }
    }
}

/**
 * Tax rate editor
 */
@Composable
fun TaxRateSection(
    taxRate: Double,
    onTaxRateChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = (taxRate * 100).toInt().toString(),
        onValueChange = { value ->
            value.toIntOrNull()?.let {
                onTaxRateChanged(it / 100.0)
            }
        },
        label = { Text("Tax Rate (%)") },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = { Text("%") }
    )
}



