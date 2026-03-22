package com.emul8r.bizap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.components.theme.ColorPickerDialog
import com.emul8r.bizap.ui.components.theme.ColorSelectorButton
import com.emul8r.bizap.ui.components.theme.PresetTheme
import com.emul8r.bizap.ui.components.theme.PresetThemeSelector
import timber.log.Timber

/**
 * Unified Theme Customization Screen
 * Available in both GUI1 and GUI2 - single centralized location for all theme settings.
 *
 * Features:
 * - 4-color customization (Primary, Secondary, Tertiary, Background)
 * - Live preview
 * - Preset themes for quick selection
 * - Dark/Light mode toggle
 * - Save/Reset functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedThemeSettingsScreen(
    onBack: () -> Unit,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val themeState by viewModel.themeState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
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
                title = { Text("Theme Customization") },
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
            // ── LIVE PREVIEW ──────────────────────────────────────────────
            PreviewPanel(colors = themeState)

            // ── THEME MODE ────────────────────────────────────────────────
            ThemeModeSection(
                isDarkMode = isDarkMode,
                onDarkModeToggle = { viewModel.setDarkMode(it) }
            )

            HorizontalDivider()

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
private fun ThemeModeSection(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Dark Mode",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (isDarkMode) "Currently enabled" else "Currently disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isDarkMode,
                onCheckedChange = onDarkModeToggle
            )
        }
    }
}

@Composable
private fun PresetThemesSection(
    onPresetSelected: (PresetTheme) -> Unit
) {
    val presets = listOf(
        PresetTheme(
            id = "material_default",
            name = "Material Default",
            description = "Official Material Design 3 colors",
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            tertiary = Color(0xFF018786)
        ),
        PresetTheme(
            id = "ocean_blue",
            name = "Ocean Blue",
            description = "Professional ocean-inspired palette",
            primary = Color(0xFF1976D2),
            secondary = Color(0xFF0097A7),
            tertiary = Color(0xFF388E3C)
        ),
        PresetTheme(
            id = "sunset",
            name = "Sunset",
            description = "Warm and vibrant evening tones",
            primary = Color(0xFFFF6B35),
            secondary = Color(0xFFF7931E),
            tertiary = Color(0xFFFFB627)
        ),
        PresetTheme(
            id = "forest",
            name = "Forest",
            description = "Natural green and earthy tones",
            primary = Color(0xFF2D6A4F),
            secondary = Color(0xFF40916C),
            tertiary = Color(0xFF52B788)
        ),
        PresetTheme(
            id = "royal_purple",
            name = "Royal Purple",
            description = "Elegant and sophisticated purples",
            primary = Color(0xFF7209B7),
            secondary = Color(0xFFB5179E),
            tertiary = Color(0xFFF72585)
        ),
        PresetTheme(
            id = "tech_dark",
            name = "Tech Dark",
            description = "Modern dark tech aesthetic",
            primary = Color(0xFF00D9FF),
            secondary = Color(0xFF0099FF),
            tertiary = Color(0xFF9D4EDD)
        )
    )

    PresetThemeSelector(
        presets = presets,
        selectedPreset = null,
        onPresetSelected = onPresetSelected
    )
}


