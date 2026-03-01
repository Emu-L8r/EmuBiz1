package com.emul8r.bizap.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.local.entities.InvoiceCustomField

/**
 * Renders a custom field input based on field type
 * Supports TEXT, NUMBER, and DATE types
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFieldRenderer(
    field: InvoiceCustomField,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Field Label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = field.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (field.isRequired) {
                Text(
                    text = "*",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Field Input
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
                    onValueChange = { newValue ->
                        // Only allow numbers and decimal point
                        if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                            onValueChange(newValue)
                        }
                    },
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
                    onValueChange = { newValue ->
                        // Validate date format (YYYY-MM-DD)
                        if (newValue.isEmpty() || newValue.matches(Regex("\\d{0,4}-?\\d{0,2}-?\\d{0,2}"))) {
                            onValueChange(newValue)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = readOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("YYYY-MM-DD") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            else -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = readOnly,
                    placeholder = { Text("Enter value") }
                )
            }
        }

        // Delete Button (only in edit/create mode)
        if (onDelete != null && !readOnly) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Custom field list renderer for invoice display
 * Shows multiple custom fields in order
 */
@Composable
fun CustomFieldsListRenderer(
    fields: List<InvoiceCustomField>,
    values: Map<String, String>,
    modifier: Modifier = Modifier
) {
    if (fields.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        fields.sortedBy { it.displayOrder }.forEach { field ->
            CustomFieldRenderer(
                field = field,
                value = values[field.id] ?: "",
                readOnly = true
            )
        }
    }
}

/**
 * Validation helper for custom field values
 */
object CustomFieldValidator {
    fun validateField(field: InvoiceCustomField, value: String): String? {
        if (field.isRequired && value.isBlank()) {
            return "${field.label} is required"
        }

        if (value.isBlank()) return null

        return when (field.fieldType) {
            "NUMBER" -> {
                if (value.toDoubleOrNull() == null) {
                    "${field.label} must be a valid number"
                } else null
            }
            "DATE" -> {
                if (!value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    "${field.label} must be in YYYY-MM-DD format"
                } else null
            }
            else -> null
        }
    }

    fun validateFields(fields: List<InvoiceCustomField>, values: Map<String, String>): Map<String, String> {
        return fields.associate { field ->
            field.id to (validateField(field, values[field.id] ?: "") ?: "")
        }.filterValues { it.isNotEmpty() }
    }
}
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            else -> {
                Text("Unsupported field type: ${field.fieldType}")
            }
        }
    }
}
