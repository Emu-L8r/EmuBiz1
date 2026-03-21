package com.emul8r.bizap.ui.components.classic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ClassicCurrencySelector(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencies = listOf("USD", "EUR", "GBP", "AUD", "CAD", "JPY", "CHF", "INR")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Currency",
            style = MaterialTheme.typography.labelMedium
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedCurrency)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            onCurrencyChange(currency)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

