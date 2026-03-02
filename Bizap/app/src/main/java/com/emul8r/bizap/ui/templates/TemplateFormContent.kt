package com.emul8r.bizap.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.graphics.Color as AndroidColor

/**
 * Reusable template form content used by Create and Edit screens.
 */
@Composable
fun TemplateFormContent(
    formState: TemplateFormState,
    onFieldChange: (field: String, value: Any?) -> Unit,
    onLogoSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Template Details", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = formState.name,
            onValueChange = { onFieldChange("name", it) },
            label = { Text("Template Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.errors.containsKey("name"),
            supportingText = { if (formState.errors.containsKey("name")) Text(formState.errors["name"] ?: "") },
            singleLine = true
        )

        Text(text = "Colors", style = MaterialTheme.typography.headlineSmall)

        ColorPickerField(
            label = "Primary Color",
            color = formState.primaryColor,
            onColorChange = { onFieldChange("primaryColor", it) },
            error = formState.errors["primaryColor"]
        )

        ColorPickerField(
            label = "Secondary Color",
            color = formState.secondaryColor,
            onColorChange = { onFieldChange("secondaryColor", it) },
            error = formState.errors["secondaryColor"]
        )

        Text(text = "Company Information", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = formState.companyName,
            onValueChange = { onFieldChange("companyName", it) },
            label = { Text("Company Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.errors.containsKey("companyName")
        )

        OutlinedTextField(
            value = formState.companyEmail,
            onValueChange = { onFieldChange("companyEmail", it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Button(onClick = onLogoSelect, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (formState.logoFileName != null) "Change Logo" else "Upload Logo")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Hide Line Items", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = formState.hideLineItems,
                onCheckedChange = { onFieldChange("hideLineItems", it) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Hide Payment Terms", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = formState.hidePaymentTerms,
                onCheckedChange = { onFieldChange("hidePaymentTerms", it) }
            )
        }
    }
}

@Composable
private fun ColorPickerField(
    label: String,
    color: String,
    onColorChange: (String) -> Unit,
    error: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(try { Color(AndroidColor.parseColor(color)) } catch (e: Exception) { Color.Gray })
        )
        OutlinedTextField(
            value = color,
            onValueChange = onColorChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            isError = error != null
        )
    }
}

