package com.emul8r.bizap.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.local.entities.InvoiceTemplate

/**
 * Single template list item composable
 */
@Composable
fun TemplateListItem(
    template: InvoiceTemplate,
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onSetDefault: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name and Default Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = template.designType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Default Badge
                if (template.isDefault) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Default") },
                        modifier = Modifier.padding(start = 8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Company Info Preview
            if (template.companyName.isNotEmpty()) {
                Text(
                    text = template.companyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Colors:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Primary color box
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color(
                                android.graphics.Color.parseColor(template.primaryColor)
                            )
                        )
                    ) {}
                }
                // Secondary color box
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color(
                                android.graphics.Color.parseColor(template.secondaryColor)
                            )
                        )
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Set Default Button (shown only if not already default)
                if (!template.isDefault) {
                    OutlinedButton(
                        onClick = { onSetDefault(template.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set Default")
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Edit Button
                IconButton(
                    onClick = { onEdit(template.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Template",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete Button
                IconButton(
                    onClick = { onDelete(template.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Template",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


