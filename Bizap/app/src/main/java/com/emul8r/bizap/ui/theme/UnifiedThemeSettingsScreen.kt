package com.emul8r.bizap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.components.theme.ColorPickerDialog
import com.emul8r.bizap.ui.components.theme.ColorSelectorButton
import com.emul8r.bizap.ui.components.theme.PresetTheme
import com.emul8r.bizap.ui.components.theme.PresetThemeSelector
import timber.log.Timber

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

    var showPrimaryColorPicker by remember { mutableStateOf(false) }
    var showSecondaryColorPicker by remember { mutableStateOf(false) }
    var showTertiaryColorPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when save is successful
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar(
                "✅ Theme saved successfully!",
                duration = SnackbarDuration.Short
            )
        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // INFO CARD: Direct users to App Appearance for theme mode
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                modifier = Modifier.padding(top = 8.dp)
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
                onClick = { showPrimaryColorPicker = true },
                modifier = Modifier.fillMaxWidth()
            )

            ColorSelectorButton(
                label = "Secondary Color",
                color = themeState.secondary,
                onClick = { showSecondaryColorPicker = true },
                modifier = Modifier.fillMaxWidth()
            )

            ColorSelectorButton(
                label = "Tertiary Color",
                color = themeState.tertiary,
                onClick = { showTertiaryColorPicker = true },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // ── ACTION BUTTONS ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset to Default")
                }

                Button(
                    onClick = { viewModel.saveTheme() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save Theme")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── COLOR PICKERS ──────────────────────────────────────────────────
    if (showPrimaryColorPicker) {
        ColorPickerDialog(
            currentColor = themeState.primary,
            onColorSelected = { viewModel.setPrimaryColor(it) },
            onDismiss = { showPrimaryColorPicker = false },
            title = "Select Primary Color"
        )
    }

    if (showSecondaryColorPicker) {
        ColorPickerDialog(
            currentColor = themeState.secondary,
            onColorSelected = { viewModel.setSecondaryColor(it) },
            onDismiss = { showSecondaryColorPicker = false },
            title = "Select Secondary Color"
        )
    }

    if (showTertiaryColorPicker) {
        ColorPickerDialog(
            currentColor = themeState.tertiary,
            onColorSelected = { viewModel.setTertiaryColor(it) },
            onDismiss = { showTertiaryColorPicker = false },
            title = "Select Tertiary Color"
        )
    }
}

@Composable
private fun PreviewPanel(colors: ThemeColors) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Live Preview",
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
                    Text("Primary")
                }

                // Secondary preview
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Secondary")
                }

                // Tertiary preview
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = colors.tertiary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tertiary")
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
    val presets = listOf(
        // Existing presets
        PresetTheme(
            id = "material_default",
            name = "Material Purple",
            description = "Official Material Design 3",
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            tertiary = Color(0xFF018786)
        ),
        PresetTheme(
            id = "ocean_blue",
            name = "Ocean Blue",
            description = "Professional & calm",
            primary = Color(0xFF0EA5E9),
            secondary = Color(0xFF06B6D4),
            tertiary = Color(0xFF0891B2)
        ),
        PresetTheme(
            id = "forest_green",
            name = "Forest Green",
            description = "Natural & peaceful",
            primary = Color(0xFF16A34A),
            secondary = Color(0xFF15803D),
            tertiary = Color(0xFF166534)
        ),
        PresetTheme(
            id = "sunset_orange",
            name = "Sunset Orange",
            description = "Warm & energetic",
            primary = Color(0xFFEA580C),
            secondary = Color(0xFFF97316),
            tertiary = Color(0xFFEF4444)
        ),
        PresetTheme(
            id = "royal_indigo",
            name = "Royal Indigo",
            description = "Elegant & bold",
            primary = Color(0xFF4F46E5),
            secondary = Color(0xFF6366F1),
            tertiary = Color(0xFF818CF8)
        ),
        PresetTheme(
            id = "rose_pink",
            name = "Rose Pink",
            description = "Modern & vibrant",
            primary = Color(0xFFE11D48),
            secondary = Color(0xFFBE185D),
            tertiary = Color(0xFF9D174D)
        ),
        // New presets
        PresetTheme(
            id = "sky_cyan",
            name = "Sky Cyan",
            description = "Fresh & airy",
            primary = Color(0xFF06B6D4),
            secondary = Color(0xFF0891B2),
            tertiary = Color(0xFF0E7490)
        ),
        PresetTheme(
            id = "emerald",
            name = "Emerald",
            description = "Luxurious & rich",
            primary = Color(0xFF059669),
            secondary = Color(0xFF047857),
            tertiary = Color(0xFF065F46)
        ),
        PresetTheme(
            id = "coral",
            name = "Coral",
            description = "Playful & warm",
            primary = Color(0xFFFF6B6B),
            secondary = Color(0xFFFF8787),
            tertiary = Color(0xFFFFA5A5)
        ),
        PresetTheme(
            id = "deep_purple",
            name = "Deep Purple",
            description = "Sophisticated & dark",
            primary = Color(0xFF7C3AED),
            secondary = Color(0xFF8B5CF6),
            tertiary = Color(0xFFA78BFA)
        ),
        PresetTheme(
            id = "mint",
            name = "Mint",
            description = "Clean & refreshing",
            primary = Color(0xFF14B8A6),
            secondary = Color(0xFF2DD4BF),
            tertiary = Color(0xFF5EEAD4)
        ),
        PresetTheme(
            id = "gold",
            name = "Gold",
            description = "Premium & warm",
            primary = Color(0xFFD97706),
            secondary = Color(0xFFF59E0B),
            tertiary = Color(0xFFFBBF24)
        )
    )

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
                        onSelected = onPresetSelected,
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

/**
 * Enhanced preset card with visual preview of all 3 colors
 */
@Composable
private fun PresetCard(
    preset: PresetTheme,
    onSelected: (PresetTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected(preset) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Color preview row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(preset.primary, RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(preset.secondary, RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(preset.tertiary, RoundedCornerShape(6.dp))
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
