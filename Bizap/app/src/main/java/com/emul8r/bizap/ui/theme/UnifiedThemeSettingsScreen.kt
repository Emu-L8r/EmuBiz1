package com.emul8r.bizap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.BackHandler
import com.emul8r.bizap.ui.components.theme.ColorPickerDialog
import com.emul8r.bizap.ui.components.theme.ColorSelectorButton
import com.emul8r.bizap.ui.components.theme.PresetTheme
import com.emul8r.bizap.ui.components.theme.PresetThemeSelector
import com.emul8r.bizap.ui.designsystem.BizapColors
import com.emul8r.bizap.R
import timber.log.Timber

// ── DIALOG STATE MANAGEMENT ─────────────────────────────────────────
private sealed class ColorPickerState {
    object Hidden : ColorPickerState()
    data class Showing(val colorType: ColorType) : ColorPickerState()
}

private enum class ColorType { PRIMARY, SECONDARY, TERTIARY }

/**
 * Advanced Color Themes Screen for Bizap
 * Available in both GUI1 and GUI2 - single centralized location for color customization only.
 *
 * NOTE: Dark/Light mode is handled exclusively in Settings → App Appearance → Theme Mode
 * This screen focuses ONLY on custom color selection to avoid conflicts.
 *
 * Features:
 * - 4-color customization (Primary, Secondary, Tertiary, Background)
 * - Live preview
 * - Preset themes for quick selection
 * - Save/Reset functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedThemeSettingsScreen(
    onBack: () -> Unit,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val themeState by viewModel.themeState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    var colorPickerState by remember { mutableStateOf<ColorPickerState>(ColorPickerState.Hidden) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var savedTheme by remember { mutableStateOf(themeState) }

    // Detect unsaved changes using derivedStateOf for efficiency
    val hasUnsavedChanges by remember {
        derivedStateOf {
            themeState.primary != savedTheme.primary ||
            themeState.secondary != savedTheme.secondary ||
            themeState.tertiary != savedTheme.tertiary
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when save is successful and update saved theme
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            savedTheme = themeState
            snackbarHostState.showSnackbar(
                message = "✅ Theme saved successfully!",
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
        }
    }

    // Handle back press - show dialog if unsaved changes
    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedChangesDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Color Themes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxl)
        ) {
            // INFO CARD: Direct users to App Appearance for theme mode
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        "Theme Mode (Light/Dark/Auto)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "To change between Light, Dark, or Auto mode, go to Settings → App Appearance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // ── LIVE PREVIEW ──────────────────────────────────────────────
            PreviewPanel(colors = themeState)


            // INFO CARD - Direct users to App Appearance for theme mode
            Text(
                "Preset Themes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = Spacing.sm)
            )

            // ── PRESET THEMES ─────────────────────────────────────────────
            PresetThemesSection(
                onPresetSelected = { preset ->
                    viewModel.applyPreset(preset)
                }
            )

            HorizontalDivider()

            // ── COLOR CUSTOMIZATION ───────────────────────────────────────
            Text(
                "Custom Colors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ColorSelectorButton(
                label = "Primary Color",
                color = themeState.primary,
                onClick = { colorPickerState = ColorPickerState.Showing(ColorType.PRIMARY) },
                modifier = Modifier.fillMaxWidth()
            )

            ColorSelectorButton(
                label = "Secondary Color",
                color = themeState.secondary,
                onClick = { colorPickerState = ColorPickerState.Showing(ColorType.SECONDARY) },
                modifier = Modifier.fillMaxWidth()
            )

            ColorSelectorButton(
                label = "Tertiary Color",
                color = themeState.tertiary,
                onClick = { colorPickerState = ColorPickerState.Showing(ColorType.TERTIARY) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // ── ACTION BUTTONS ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset to Default")
                }

                Button(
                    onClick = { viewModel.saveTheme() },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimensions.progressIndicatorSize),
                            strokeWidth = Dimensions.progressIndicatorStroke
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Saving...")
                    } else {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(Dimensions.iconSizeSmall))
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Save Theme")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }

    // ── COLOR PICKERS ──────────────────────────────────────────────────
    when (colorPickerState) {
        is ColorPickerState.Showing -> {
            val currentColor = when ((colorPickerState as ColorPickerState.Showing).colorType) {
                ColorType.PRIMARY -> themeState.primary
                ColorType.SECONDARY -> themeState.secondary
                ColorType.TERTIARY -> themeState.tertiary
            }

            ColorPickerDialog(
                currentColor = currentColor,
                onColorSelected = { color ->
                    when ((colorPickerState as ColorPickerState.Showing).colorType) {
                        ColorType.PRIMARY -> viewModel.setPrimaryColor(color)
                        ColorType.SECONDARY -> viewModel.setSecondaryColor(color)
                        ColorType.TERTIARY -> viewModel.setTertiaryColor(color)
                    }
                },
                onDismiss = { colorPickerState = ColorPickerState.Hidden },
                title = when ((colorPickerState as ColorPickerState.Showing).colorType) {
                    ColorType.PRIMARY -> "Select Primary Color"
                    ColorType.SECONDARY -> "Select Secondary Color"
                    ColorType.TERTIARY -> "Select Tertiary Color"
                }
            )
        }
        ColorPickerState.Hidden -> {}
    }

    // ── UNSAVED CHANGES DIALOG ───────────────────────────────────────
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Do you really want to go back?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Discard changes and navigate back
                        onBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnsavedChangesDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun PreviewPanel(colors: ThemeColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.label_live_preview),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            // Preview of the theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary preview
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.label_primary))
                }

                // Secondary preview
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.label_secondary))
                }

                // Tertiary preview
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = colors.tertiary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.label_tertiary))
                }
            }

            Text(
                "Your theme changes appear in real-time across the entire app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun PresetThemesSection(
    onPresetSelected: (PresetTheme) -> Unit
) {
    val presets = remember { PRESET_THEMES }
    var selectedPresetId by remember { mutableStateOf<String?>(null) }

    // ── DISPLAY PRESET OPTIONS ────────────────────────────────
    Text(
        "Quick Presets",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Text(
        "Choose from professionally curated color themes",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    // Display presets in 2-column grid
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        presets.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { preset ->
                    PresetCard(
                        preset = preset,
                        onSelected = { selected ->
                            selectedPresetId = selected.id
                            onPresetSelected(selected)
                        },
                        isSelected = selectedPresetId == preset.id,
                        modifier = Modifier.weight(1f)
                    )
                }
                // If odd number of presets, add spacer
                if (row.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private val PRESET_THEMES = listOf(
    PresetTheme(
        id = "blue",
        name = "Material Blue",
        description = "Professional & calm",
        primary = BizapColors.Presets.Blue,
        secondary = BizapColors.Presets.Cyan,
        tertiary = BizapColors.Presets.Indigo
    ),
    PresetTheme(
        id = "purple",
        name = "Material Purple",
        description = "Official Material Design 3",
        primary = BizapColors.Presets.Purple,
        secondary = BizapColors.Presets.Pink,
        tertiary = BizapColors.Presets.Red
    ),
    PresetTheme(
        id = "green",
        name = "Forest Green",
        description = "Natural & peaceful",
        primary = BizapColors.Presets.Green,
        secondary = BizapColors.Presets.Teal,
        tertiary = BizapColors.Presets.Lime
    ),
    PresetTheme(
        id = "orange",
        name = "Sunset Orange",
        description = "Warm & energetic",
        primary = BizapColors.Presets.Orange,
        secondary = BizapColors.Presets.DeepOrange,
        tertiary = BizapColors.Presets.Red
    ),
    PresetTheme(
        id = "indigo",
        name = "Royal Indigo",
        description = "Elegant & bold",
        primary = BizapColors.Presets.Indigo,
        secondary = BizapColors.Presets.Purple,
        tertiary = BizapColors.Presets.Pink
    ),
    PresetTheme(
        id = "pink",
        name = "Rose Pink",
        description = "Modern & vibrant",
        primary = BizapColors.Presets.Pink,
        secondary = BizapColors.Presets.Red,
        tertiary = BizapColors.Presets.DeepOrange
    ),
    PresetTheme(
        id = "cyan",
        name = "Sky Cyan",
        description = "Fresh & airy",
        primary = BizapColors.Presets.Cyan,
        secondary = BizapColors.Presets.Blue,
        tertiary = BizapColors.Presets.Teal
    ),
    PresetTheme(
        id = "teal",
        name = "Emerald Teal",
        description = "Luxurious & rich",
        primary = BizapColors.Presets.Teal,
        secondary = BizapColors.Presets.Green,
        tertiary = BizapColors.Presets.Lime
    ),
    PresetTheme(
        id = "red",
        name = "Vibrant Red",
        description = "Bold & striking",
        primary = BizapColors.Presets.Red,
        secondary = BizapColors.Presets.Orange,
        tertiary = BizapColors.Presets.Pink
    ),
    PresetTheme(
        id = "deeporange",
        name = "Deep Orange",
        description = "Rich & warm",
        primary = BizapColors.Presets.DeepOrange,
        secondary = BizapColors.Presets.Orange,
        tertiary = BizapColors.Presets.Red
    ),
    PresetTheme(
        id = "lime",
        name = "Lime Green",
        description = "Fresh & vibrant",
        primary = BizapColors.Presets.Lime,
        secondary = BizapColors.Presets.Green,
        tertiary = BizapColors.Presets.Teal
    ),
    PresetTheme(
        id = "bluegrey",
        name = "Blue Grey",
        description = "Calm & professional",
        primary = BizapColors.Presets.BlueGrey,
        secondary = BizapColors.Presets.Blue,
        tertiary = BizapColors.Presets.Cyan
    )
)

/**
 * Enhanced preset card with visual preview of all 3 colors
 */
@Composable
private fun PresetCard(
    preset: PresetTheme,
    onSelected: (PresetTheme) -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelected(preset) },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Color preview row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.colorPreviewHeight),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(preset.primary, RoundedCornerShape(Dimensions.colorPreviewRadius))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(preset.secondary, RoundedCornerShape(Dimensions.colorPreviewRadius))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(preset.tertiary, RoundedCornerShape(Dimensions.colorPreviewRadius))
                )
            }

            // Title and description
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = preset.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            // Select button
            Button(
                onClick = { onSelected(preset) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = preset.primary
                )
            ) {
                Text("Select", fontSize = 12.sp)
            }
        }
    }
}
