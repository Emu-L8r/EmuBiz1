package com.emul8r.bizap.ui.invoices.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.Invoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionPicker(
    currentInvoice: Invoice,
    allVersions: List<Invoice>,
    onVersionSelected: (Long) -> Unit
) {
    if (allVersions.size <= 1) return

    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Version History",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        text = "v${currentInvoice.version} (Active)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    allVersions.sortedByDescending { it.version }.forEach { version ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Version v${version.version} (${version.status})",
                                    fontWeight = if (version.id == currentInvoice.id) FontWeight.Bold else FontWeight.Normal,
                                    color = if (version.id == currentInvoice.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                expanded = false
                                if (version.id != currentInvoice.id) {
                                    onVersionSelected(version.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
