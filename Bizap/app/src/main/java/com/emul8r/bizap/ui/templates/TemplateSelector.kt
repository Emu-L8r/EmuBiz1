package com.emul8r.bizap.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import android.graphics.Color as AndroidColor

/**
 * Template selector composable for invoice creation and editing
 * Displays available templates with previews and selection indicators
 */
@Composable
fun TemplateSelector(
    templates: List<InvoiceTemplate>,
    selectedTemplateId: String? = null,
    isLoading: Boolean = false,
    onTemplateSelected: (InvoiceTemplate?) -> Unit,
    onCreateNewTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Template",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = onCreateNewTemplate,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", style = MaterialTheme.typography.labelSmall)
            }
        }

        // No Template Option
        TemplateSelectorItem(
            name = "No Template",
            designType = "CUSTOM",
            primaryColor = "#CCCCCC",
            isSelected = selectedTemplateId == null,
            isDefault = false,
            onClick = { onTemplateSelected(null) }
        )

        // Loading State
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Empty State
        if (templates.isEmpty() && !isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "No templates created yet",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onCreateNewTemplate) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Template")
                    }
                }
            }
        }

        // Templates List
        if (templates.isNotEmpty() && !isLoading) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(templates, key = { it.id }) { template ->
                    TemplateSelectorItem(
                        name = template.name,
                        designType = template.designType,
                        primaryColor = template.primaryColor,
                        isSelected = selectedTemplateId == template.id.toString(),
                        isDefault = template.isDefault,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
        }
    }
}

/**
 * Individual template selection card
 */
@Composable
private fun TemplateSelectorItem(
    name: String,
    designType: String,
    primaryColor: String,
    isSelected: Boolean,
    isDefault: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name and badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (isDefault) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(4.dp, 2.dp)
                            )
                        }
                    }
                }

                // Design type and color
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = designType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(primaryColor))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Composable
private fun localLazyList(templates: List<InvoiceTemplate>, selectedTemplateId: String?, onTemplateSelected: (InvoiceTemplate?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        templates.forEach { template ->
            TemplateSelectorCard(
                isSelected = selectedTemplateId == template.id,
                name = template.name,
                designType = template.designType,
                primaryColor = template.primaryColor,
                isDefault = template.isDefault,
                onClick = { onTemplateSelected(template) }
            )
        }
    }
}

@Composable
private fun TemplateSelectorCard(
    isSelected: Boolean,
    name: String,
    designType: String,
    primaryColor: String?,
    isDefault: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(text = designType, style = MaterialTheme.typography.bodySmall)
            }
            if (primaryColor != null) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)).background(try { Color(AndroidColor.parseColor(primaryColor)) } catch (e: Exception) { Color.Gray }))
            }
        }
    }
}

