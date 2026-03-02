package com.emul8r.bizap.ui.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveStatusChip(
    status: String,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val (backgroundColor, textColor) = when (status) {
        "PAID" -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        "SENT" -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        AssistChip(
            modifier = Modifier.menuAnchor(),
            onClick = { expanded = true },
            label = { Text(status) },
            colors = AssistChipDefaults.assistChipColors(containerColor = backgroundColor, labelColor = textColor),
            trailingIcon = { 
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Change status")
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Mark as DRAFT") },
                onClick = {
                    onStatusChange("DRAFT")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Mark as SENT") },
                onClick = {
                    onStatusChange("SENT")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Mark as PAID") },
                onClick = {
                    onStatusChange("PAID")
                    expanded = false
                }
            )
        }
    }
}

