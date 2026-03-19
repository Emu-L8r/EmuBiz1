package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.presentation.viewmodel.SettingsViewModel
import com.emul8r.bizap.ui.settings.ThemeViewModel
import com.emul8r.bizap.ui.theme.ThemePresets

/**
 * Unified App Appearance Screen (GUI2).
 *
 * Consolidates theme, display mode, and appearance settings from both
 * ThemeSettingsScreenV2 and AppSettingsScreenV2 into a single cohesive screen.
 *
 * Sections:
 * 1. Theme Mode (Light/Dark/Auto)
 * 2. Display Mode (List/Grid/Card)
 * 3. Quick Presets
 * 4. Dark Mode Toggle + Dark Presets
 * 5. Custom Colour
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAppearanceScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val config by themeViewModel.themeConfig.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val themePreference by settingsViewModel.themePreference.collectAsStateWithLifecycle()

    val isDarkMode = themePreference == ThemePreference.DARK

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Appearance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── SECTION 1: Theme Mode ──
            item {
                SectionHeader("Theme Mode")
                Spacer(Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        ThemePreference.LIGHT to "Light",
                        ThemePreference.DARK to "Dark",
                        ThemePreference.AUTO to "Follow system"
                    ).forEach { (preference, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setThemePreference(preference)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                            RadioButton(
                                selected = themePreference == preference,
                                onClick = { settingsViewModel.setThemePreference(preference) }
                            )
                        }
                    }
                }
            }

            // ── SECTION 2: Display Mode ──
            item {
                SectionHeader("Display Mode")
                Spacer(Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        DisplayMode.LIST to "List view",
                        DisplayMode.GRID to "Grid view",
                        DisplayMode.CARD to "Card view"
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setDisplayMode(mode)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                            RadioButton(
                                selected = settings.displayMode == mode,
                                onClick = { settingsViewModel.setDisplayMode(mode) }
                            )
                        }
                    }
                }
            }

            // ── SECTION 3: Quick Presets ──
            item {
                SectionHeader("Quick Presets")
                Spacer(Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePresets.allLightPresets.forEach { preset ->
                        ThemePresetButtonV2(
                            preset = preset,
                            isSelected = config.seedColorHex == preset.colorHex && !isDarkMode,
                            onSelect = {
                                themeViewModel.applyPreset(preset)
                                settingsViewModel.setThemePreference(ThemePreference.LIGHT)
                            }
                        )
                    }
                }
            }

            // ── SECTION 4: Dark Mode & Dark Presets ──
            item {
                SectionHeader("Dark Mode")
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Dark Mode", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { enabled ->
                            themeViewModel.updateDarkMode(enabled)
                            settingsViewModel.setThemePreference(
                                if (enabled) ThemePreference.DARK else ThemePreference.LIGHT
                            )
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (isDarkMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemePresets.allDarkPresets.forEach { preset ->
                            ThemePresetButtonV2(
                                preset = preset,
                                isSelected = config.seedColorHex == preset.colorHex && isDarkMode,
                                onSelect = {
                                    themeViewModel.applyPreset(preset)
                                    settingsViewModel.setThemePreference(ThemePreference.DARK)
                                }
                            )
                        }
                    }
                }
            }

            // ── SECTION 5: Custom Colour ──
            item {
                SectionHeader("Custom Colour")
                Spacer(Modifier.height(12.dp))
                Text(
                    "Current color: ${config.seedColorHex}",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ThemePresetButtonV2(
    preset: com.emul8r.bizap.ui.theme.ThemePreset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(android.graphics.Color.parseColor(preset.colorHex))
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    preset.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    preset.description,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
