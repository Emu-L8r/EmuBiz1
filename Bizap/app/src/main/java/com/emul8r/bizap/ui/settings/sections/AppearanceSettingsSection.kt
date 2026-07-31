package com.emul8r.bizap.ui.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.ColorScheme
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.ui.settings.InvoiceSettingsViewModel
import com.emul8r.bizap.ui.settings.components.*

/**
 * Appearance Settings Section
 *
 * Tab containing all visual customization options:
 * - Color schemes and custom colors
 * - Gradients and shadows
 * - Borders and dividers
 * - Background patterns
 * - Watermarks and accents
 *
 * Organization:
 * - Color Scheme Selection (accordion)
 * - Color Customization (accordion)
 * - Visual Effects (accordion)
 * - Advanced Visual Options (accordion)
 */
@Composable
fun AppearanceSettingsSection(
    viewModel: InvoiceSettingsViewModel,
    settings: InvoiceSettings?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Section header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Visual Appearance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Customize colors, gradients, and visual effects",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Color Scheme Selection
        item {
            SettingsAccordion(
                title = "Color Scheme",
                icon = Icons.Default.Palette,
                initiallyExpanded = true,
                description = "Choose a preset palette or customize colors"
            ) {
                ColorSchemeSelector(
                    selectedScheme = settings?.selectedColorScheme ?: ColorScheme.PROFESSIONAL,
                    onSchemeSelected = { viewModel.updateColorScheme(it) }
                )
            }
        }

        // Custom Color Pickers
        item {
            SettingsAccordion(
                title = "Custom Colors",
                icon = Icons.Default.ColorLens,
                description = "Fine-tune primary and accent colors"
            ) {
                CustomColorPickers(
                    settings = settings,
                    onPrimaryColorChanged = { viewModel.updatePrimaryColor(it) },
                    onAccentColorChanged = { viewModel.updateAccentColor(it) }
                )
            }
        }

        // Visual Effects
        item {
            SettingsAccordion(
                title = "Visual Effects",
                icon = Icons.Default.AutoAwesome,
                description = "Gradients, shadows, and decorative elements"
            ) {
                VisualEffectsControls(
                    settings = settings,
                    viewModel = viewModel
                )
            }
        }

        // Background & Patterns
        item {
            SettingsAccordion(
                title = "Background & Patterns",
                icon = Icons.Default.Wallpaper,
                description = "Background accents and subtle patterns"
            ) {
                BackgroundPatternControls(
                    settings = settings,
                    viewModel = viewModel
                )
            }
        }

        // Dividers & Borders
        item {
            SettingsAccordion(
                title = "Dividers & Borders",
                icon = Icons.Default.BorderAll,
                description = "Divider styles and border customization"
            ) {
                DividerBorderControls(
                    settings = settings,
                    viewModel = viewModel
                )
            }
        }

        // Spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Color Scheme Selector
 *
 * Displays preset color schemes as button grid.
 * Each preset applies a set of colors instantly.
 */
@Composable
private fun ColorSchemeSelector(
    selectedScheme: ColorScheme,
    onSchemeSelected: (ColorScheme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Preset Palettes",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        // Grid of color scheme buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // First row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorScheme.PROFESSIONAL.let {
                    PaletteButton(
                        name = "Professional",
                        isSelected = selectedScheme == it,
                        onClick = { onSchemeSelected(it) },
                        colors = Pair(Color(0xFF003366), Color(0xFFFFC107)), // actual: navy + gold
                        modifier = Modifier.weight(1f)
                    )
                }
                ColorScheme.VIBRANT.let {
                    PaletteButton(
                        name = "Vibrant",
                        isSelected = selectedScheme == it,
                        onClick = { onSchemeSelected(it) },
                        colors = Pair(Color(0xFF6B4C9A), Color(0xFFFF9F43)), // actual: purple + orange
                        modifier = Modifier.weight(1f)
                    )
                }
                ColorScheme.MINIMAL.let {
                    PaletteButton(
                        name = "Minimal",
                        isSelected = selectedScheme == it,
                        onClick = { onSchemeSelected(it) },
                        colors = Pair(Color(0xFF1A1A1A), Color(0xFF666666)), // actual: dark + mid grey
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Second row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorScheme.WARM.let {
                    PaletteButton(
                        name = "Warm",
                        isSelected = selectedScheme == it,
                        onClick = { onSchemeSelected(it) },
                        colors = Pair(Color(0xFFD97706), Color(0xFF78350F)), // actual: amber + dark brown
                        modifier = Modifier.weight(1f)
                    )
                }
                ColorScheme.TECH.let {
                    PaletteButton(
                        name = "Tech",
                        isSelected = selectedScheme == it,
                        onClick = { onSchemeSelected(it) },
                        colors = Pair(Color(0xFF0F172A), Color(0xFF06B6D4)), // actual: deep navy + cyan
                        modifier = Modifier.weight(1f)
                    )
                }
                ColorScheme.NATURE.let {
                    PaletteButton(
                        name = "Nature",
                        isSelected = selectedScheme == it,
                        onClick = { onSchemeSelected(it) },
                        colors = Pair(Color(0xFF15803D), Color(0xFF92400E)), // actual: green + earth brown
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Palette Button
 *
 * Shows color scheme preview with two color swatches.
 */
@Composable
private fun PaletteButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: Pair<Color, Color>,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface
        ),
        border = if (isSelected)
            null
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors.first)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colors.second)
                )
            }
            Text(name, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Custom Color Pickers
 *
 * Hex input fields for primary and accent colors.
 */
@Composable
private fun CustomColorPickers(
    settings: InvoiceSettings?,
    onPrimaryColorChanged: (String) -> Unit,
    onAccentColorChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingRow(label = "Primary Color") {
            OutlinedTextField(
                value = settings?.primaryColor ?: "#0066CC",
                onValueChange = {
                    // Validate hex format
                    if (it.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                        onPrimaryColorChanged(it)
                    }
                },
                modifier = Modifier.width(120.dp),
                textStyle = MaterialTheme.typography.labelSmall,
                singleLine = true,
                placeholder = { Text("#000000", style = MaterialTheme.typography.labelSmall) }
            )
        }

        SettingRow(label = "Accent Color") {
            OutlinedTextField(
                value = settings?.accentColor ?: "#FF9F43",
                onValueChange = {
                    if (it.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                        onAccentColorChanged(it)
                    }
                },
                modifier = Modifier.width(120.dp),
                textStyle = MaterialTheme.typography.labelSmall,
                singleLine = true,
                placeholder = { Text("#000000", style = MaterialTheme.typography.labelSmall) }
            )
        }

        Text(
            "Use 6-digit hex format: #RRGGBB",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Visual Effects Controls
 *
 * Toggles for gradients, shadows, rounded corners.
 */
@Composable
private fun VisualEffectsControls(
    settings: InvoiceSettings?,
    viewModel: InvoiceSettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Gradient header toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Gradient Header",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enableGradientHeader ?: true,
                onCheckedChange = { viewModel.updateGradientHeader(it) }
            )
        }

        // Rounded corners toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Rounded Corners",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enableRoundedCorners ?: true,
                onCheckedChange = { viewModel.updateRoundedCorners(it) }
            )
        }

        // Shadows toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Drop Shadows",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enableShadows ?: true,
                onCheckedChange = { viewModel.updateShadows(it) }
            )
        }
    }
}

/**
 * Background Pattern Controls
 *
 * Toggles for background accents and patterns.
 */
@Composable
private fun BackgroundPatternControls(
    settings: InvoiceSettings?,
    viewModel: InvoiceSettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
         Row(
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(vertical = 8.dp),
             verticalAlignment = Alignment.CenterVertically,
             horizontalArrangement = Arrangement.SpaceBetween
         ) {
             Text(
                 "Background Pattern",
                 style = MaterialTheme.typography.labelMedium
             )
             Switch(
                 checked = settings?.enableBackgroundPattern ?: false,
                 onCheckedChange = { viewModel.toggleBackgroundPattern(it) }
             )
         }
    }
}

/**
 * Divider & Border Controls
 *
 * Divider style and border customization.
 */
@Composable
private fun DividerBorderControls(
    settings: InvoiceSettings?,
    viewModel: InvoiceSettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Show Dividers",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enableDividers ?: true,
                onCheckedChange = { viewModel.updateDividers(it) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Alternating Row Colors",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enableAlternatingRowColors ?: true,
                onCheckedChange = { viewModel.updateAlternatingRowColors(it) }
            )
        }
    }
}

