package com.emul8r.bizap.ui.components.modern

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.InvoiceCustomization

@Composable
fun ModernInvoiceCustomizationEditor(
    customization: InvoiceCustomization,
    onCustomizationChange: (InvoiceCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Invoice Customization",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = customization.companyName,
            onValueChange = { onCustomizationChange(customization.copy(companyName = it)) },
            label = { Text("Company Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = customization.headerText,
            onValueChange = { onCustomizationChange(customization.copy(headerText = it)) },
            label = { Text("Header Text") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = customization.footerText,
            onValueChange = { onCustomizationChange(customization.copy(footerText = it)) },
            label = { Text("Footer Text") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            "Template Type",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("standard", "minimal", "detailed").forEach { template ->
                FilterChip(
                    selected = customization.templateType == template,
                    onClick = { onCustomizationChange(customization.copy(templateType = template)) },
                    label = { Text(template.replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

