package com.emul8r.bizap.ui.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.settings.InvoiceSettingsViewModel
import com.emul8r.bizap.ui.settings.InvoiceSettingsUiState
import com.emul8r.bizap.domain.model.ColorScheme
import com.emul8r.bizap.domain.model.SpacingProfile
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.PdfEngine

/**
 * Left-side scrollable settings panel for split-screen layout
 *
 * **IMPORTANT:** This component is designed to pair with PreviewPanel on the right.
 * All settings changes should trigger debouncedGeneratePreview() in ViewModel
 * to update the preview in real-time.
 */
@Composable
fun SettingsPanel(
    viewModel: InvoiceSettingsViewModel,
    uiState: InvoiceSettingsUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Customize Invoice",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Adjust settings • See live preview",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Info banner
        item {
            InfoCard(
                icon = "💡",
                title = "Live Preview",
                message = "Changes update in real-time on the right"
            )
        }

        // PDF Engine selection
        item {
            if (uiState.settings != null) {
                PdfEngineSelectionCard(
                    selectedEngine = uiState.settings.selectedPdfEngine,
                    onEngineSelected = { viewModel.updateSelectedPdfEngine(it) }
                )
            }
        }

        // Color scheme section
        item {
            if (uiState.settings != null) {
                ColorSchemeSection(
                    selectedColorScheme = uiState.settings.selectedColorScheme,
                    onColorSchemeSelected = { viewModel.updateSelectedColorScheme(it) }
                )
            }
        }

        // Spacing section
        item {
            if (uiState.settings != null) {
                SpacingSection(
                    selectedSpacing = uiState.settings.selectedSpacingProfile,
                    onSpacingSelected = { viewModel.updateSelectedSpacingProfile(it) }
                )
            }
        }

        // Visual effects section
        item {
            if (uiState.settings != null) {
                VisualEffectsSection(
                    settings = uiState.settings,
                    viewModel = viewModel
                )
            }
        }

        // Action buttons
        item {
            ActionButtons(
                onSave = { viewModel.saveSettings() },
                onReset = { viewModel.resetToDefaults() },
                isSaving = uiState.isSaving
            )
        }

        // Spacer
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Informational card with icon, title, and message
 */
@Composable
fun InfoCard(
    icon: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(icon, style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Color scheme selection with visual preview
 */
@Composable
private fun ColorSchemeSection(
    selectedColorScheme: ColorScheme,
    onColorSchemeSelected: (ColorScheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Color Scheme", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

        ColorScheme.values().forEach { scheme ->
            ColorSchemeOption(
                scheme = scheme,
                isSelected = selectedColorScheme == scheme,
                onClick = { onColorSchemeSelected(scheme) }
            )
        }
    }
}

/**
 * Individual color scheme option card
 */
@Composable
private fun ColorSchemeOption(
    scheme: ColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color preview boxes
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
                    .padding(2.dp)
            ) {
                // Primary color
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(14.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = Color(android.graphics.Color.parseColor(scheme.primaryHex))
                    ) {}
                }
                // Accent color
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(14.dp),
                        shape = RoundedCornerShape(2.dp),
                        color = Color(android.graphics.Color.parseColor(scheme.accentHex))
                    ) {}
                }
            }

            // Text
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${scheme.emoji} ${scheme.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    scheme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Spacing profile selection
 */
@Composable
private fun SpacingSection(
    selectedSpacing: SpacingProfile,
    onSpacingSelected: (SpacingProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Spacing", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

        SpacingProfile.values().forEach { spacing ->
            SettingsOptionButton(
                label = "${spacing.emoji} ${spacing.displayName}",
                description = spacing.description,
                isSelected = selectedSpacing == spacing,
                onClick = { onSpacingSelected(spacing) }
            )
        }
    }
}

/**
 * Generic settings option button for radio-button style selection
 */
@Composable
private fun SettingsOptionButton(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Visual effects toggle section
 */
@Composable
private fun VisualEffectsSection(
    settings: InvoiceSettings,
    viewModel: InvoiceSettingsViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Visual Effects", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

        EffectToggle(
            label = "Gradient Header",
            description = "Add gradient to header section",
            enabled = settings.enableGradientHeader,
            onToggle = { viewModel.updateGradientHeader(it) }
        )

        EffectToggle(
            label = "Rounded Corners",
            description = "Soften edges of containers",
            enabled = settings.enableRoundedCorners,
            onToggle = { viewModel.updateRoundedCorners(it) }
        )

        EffectToggle(
            label = "Drop Shadows",
            description = "Add depth with shadows",
            enabled = settings.enableShadows,
            onToggle = { viewModel.updateShadows(it) }
        )

        EffectToggle(
            label = "Divider Lines",
            description = "Show section dividers",
            enabled = settings.enableDividers,
            onToggle = { viewModel.updateDividers(it) }
        )
    }
}

/**
 * PDF Engine selection card
 */
@Composable
private fun PdfEngineSelectionCard(
    selectedEngine: PdfEngine,
    onEngineSelected: (PdfEngine) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("PDF Engine", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)

        SettingsOptionButton(
            label = "🎨 Canvas",
            description = "Fast, native PDF rendering",
            isSelected = selectedEngine == PdfEngine.CANVAS,
            onClick = { onEngineSelected(PdfEngine.CANVAS) }
        )

        SettingsOptionButton(
            label = "📄 HTML to PDF",
            description = "Flexible, CSS-based styling",
            isSelected = selectedEngine == PdfEngine.HTML_CSS,
            onClick = { onEngineSelected(PdfEngine.HTML_CSS) }
        )

        SettingsOptionButton(
            label = "⭐ SASS Professional",
            description = "Premium two-column layout",
            isSelected = selectedEngine == PdfEngine.SASS_PROFESSIONAL,
            onClick = { onEngineSelected(PdfEngine.SASS_PROFESSIONAL) }
        )
    }
}

/**
 * Visual effect toggle with label and description
 */
@Composable
private fun EffectToggle(
    label: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Action buttons (Save, Reset) at bottom of settings
 */
@Composable
private fun ActionButtons(
    onSave: () -> Unit,
    onReset: () -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save")
            }
        }

        OutlinedButton(
            onClick = onReset,
            enabled = !isSaving,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
        ) {
            Text("Reset")
        }
    }
}










