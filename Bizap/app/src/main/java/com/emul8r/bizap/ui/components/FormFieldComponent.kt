package com.emul8r.bizap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.theme.Dimensions
import com.emul8r.bizap.ui.theme.FormDefaults
import com.emul8r.bizap.ui.theme.Spacing
import com.emul8r.bizap.ui.theme.TextSizes

/**
 * ============================================================================
 * FORM FIELD COMPONENT - Reusable form input with consistent styling
 * ============================================================================
 *
 * A standardized form field that includes:
 * - Clear label (required indicator if needed)
 * - Input field with good border and focus state
 * - Status indicator (valid ✓, invalid ✗, pending ⟳)
 * - Helper text (descriptive or error message)
 * - Consistent spacing and sizing
 *
 * Use this across all screens for consistent form appearance.
 */

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    helperText: String = "",
    errorText: String = "",
    isRequired: Boolean = false,
    isValid: Boolean? = null,  // null = no validation shown, true = valid, false = invalid
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    minLines: Int = 1,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // ---- LABEL ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = TextSizes.labelMedium,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Required indicator
            if (isRequired) {
                Text(
                    text = "*",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = Spacing.xs)
                )
            }

            // Status indicator
            when (isValid) {
                true -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Valid",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimensions.iconSizeSmall)
                )
                false -> Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Invalid",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimensions.iconSizeSmall)
                )
                null -> {} // No status indicator
            }
        }

        // ---- INPUT FIELD ----
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(FormDefaults.fieldMinHeight),
            enabled = enabled,
            maxLines = maxLines,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = errorText.isNotEmpty(),
            leadingIcon = leadingIcon,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = TextSizes.bodyMedium
            ),
            shape = MaterialTheme.shapes.small,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                errorLabelColor = MaterialTheme.colorScheme.error,
            )
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        // ---- HELPER / ERROR TEXT ----
        if (errorText.isNotEmpty()) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = TextSizes.labelSmall,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = Spacing.sm)
            )
        } else if (helperText.isNotEmpty()) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = TextSizes.labelSmall,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.sm)
            )
        }
    }
}

