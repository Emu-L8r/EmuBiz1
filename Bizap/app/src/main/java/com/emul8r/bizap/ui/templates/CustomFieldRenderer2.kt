package com.emul8r.bizap.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.local.entities.InvoiceCustomField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFieldRenderer(
    field: InvoiceCustomField,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = field.label, style = MaterialTheme.typography.labelMedium)
            if (field.isRequired) {
                Text(text = "*", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }

        when (field.fieldType) {
            "TEXT" -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = readOnly,
                    placeholder = { Text("Enter text") }
                )
            }
            "NUMBER" -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = readOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("0.00") }
                )
            }
            "DATE" -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = readOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("YYYY-MM-DD") },
                    trailingIcon = { Icon(Icons.Default.DateRange, "Date") }
                )
            }
            else -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = readOnly
                )
            }
        }
    }
}


