package com.emul8r.bizap.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    currencies: List<Currency>,
    selectedCurrencyCode: String,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Currency"
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCurrencyCode,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${currency.code} - ${currency.name} (${currency.symbol})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onCurrencySelected(currency.code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CurrencyDisplayWithAmount(
    currencySymbol: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$currencySymbol ${String.format(Locale.getDefault(), "%.2f", amount)}",
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier
    )
}

