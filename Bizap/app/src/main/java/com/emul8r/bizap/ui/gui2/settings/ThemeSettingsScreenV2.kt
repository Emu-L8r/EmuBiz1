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
import com.emul8r.bizap.ui.settings.ThemeViewModel
import com.emul8r.bizap.ui.theme.ThemePresets

/**
 * GUI2 Theme Settings Screen
 * Provides access to theme customization options, matching GUI1's ThemeSettingsScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val config by themeViewModel.themeConfig.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Settings") },
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
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Text("App Appearance", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(
                    "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePresets.allLightPresets.forEach { preset ->
                        ThemePresetButtonV2(
                            preset = preset,
                            isSelected = config.seedColorHex == preset.colorHex && !config.isDarkMode,
                            onSelect = { themeViewModel.applyPreset(preset) }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(
                    "Dark Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Dark Mode", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = config.isDarkMode,
                        onCheckedChange = { themeViewModel.updateDarkMode(it) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (config.isDarkMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemePresets.allDarkPresets.forEach { preset ->
                            ThemePresetButtonV2(
                                preset = preset,
                                isSelected = config.seedColorHex == preset.colorHex && config.isDarkMode,
                                onSelect = { themeViewModel.applyPreset(preset) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            item {
                Text(
                    "Custom Colour",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Current color: ${config.seedColorHex}",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
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
