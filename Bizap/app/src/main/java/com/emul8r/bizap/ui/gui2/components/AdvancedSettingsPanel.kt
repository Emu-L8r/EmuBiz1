package com.emul8r.bizap.ui.gui2.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Advanced Settings Panel with granular controls for spacing, typography, and visual effects
 * Phase 3.5: Task 1 - Advanced Settings Component
 *
 * Features:
 * - Spacing sliders (header, item, footer padding)
 * - Typography sliders (font size, line height)
 * - Visual effects toggles (gradients, shadows, rounded borders)
 */
@Composable
fun AdvancedSettingsPanel(
    headerPadding: Float,
    itemSpacing: Float,
    footerPadding: Float,
    fontSize: Float,
    lineHeight: Float,
    enableGradient: Boolean,
    enableShadow: Boolean,
    enableRounded: Boolean,
    onSpacingChange: (section: String, value: Float) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onToggleChange: (feature: String, enabled: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ──────────────────────────────────────────────────────
        // Section 1: Spacing Controls
        // ──────────────────────────────────────────────────────
        Text(
            "Spacing",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SliderControl(
            label = "Header Padding",
            value = headerPadding,
            range = 8f..32f,
            onValueChange = { onSpacingChange("header", it) }
        )

        SliderControl(
            label = "Item Spacing",
            value = itemSpacing,
            range = 4f..16f,
            onValueChange = { onSpacingChange("item", it) }
        )

        SliderControl(
            label = "Footer Padding",
            value = footerPadding,
            range = 8f..32f,
            onValueChange = { onSpacingChange("footer", it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ──────────────────────────────────────────────────────
        // Section 2: Typography Controls
        // ──────────────────────────────────────────────────────
        Text(
            "Typography",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SliderControl(
            label = "Font Size",
            value = fontSize,
            range = 8f..16f,
            onValueChange = { onFontSizeChange(it) }
        )

        SliderControl(
            label = "Line Height",
            value = lineHeight,
            range = 1.0f..1.8f,
            onValueChange = { onLineHeightChange(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // ──────────────────────────────────────────────────────
        // Section 3: Visual Effects
        // ──────────────────────────────────────────────────────
        Text(
            "Visual Effects",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ToggleControl(
            label = "Gradient Header",
            checked = enableGradient,
            onCheckedChange = { onToggleChange("gradient", it) }
        )

        ToggleControl(
            label = "Drop Shadow",
            checked = enableShadow,
            onCheckedChange = { onToggleChange("shadow", it) }
        )

        ToggleControl(
            label = "Rounded Borders",
            checked = enableRounded,
            onCheckedChange = { onToggleChange("rounded", it) }
        )
    }
}

/**
 * Reusable slider control for numeric settings
 */
@Composable
fun SliderControl(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.labelSmall
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

/**
 * Reusable toggle control for boolean settings
 */
@Composable
fun ToggleControl(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

