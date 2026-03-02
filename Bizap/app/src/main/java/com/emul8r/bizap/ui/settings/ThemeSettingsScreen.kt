package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.emul8r.bizap.ui.theme.ThemePresets

@Composable
fun ThemeSettingsScreen(viewModel: ThemeViewModel = hiltViewModel()) {
    val config by viewModel.themeConfig.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        item {
            Text("App Appearance", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
        }

        item {
            Text("Quick Presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Light mode presets
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePresets.allLightPresets.forEach { preset ->
                    PresetButton(
                        preset = preset,
                        isSelected = config.seedColorHex == preset.colorHex && !config.isDarkMode,
                        onSelect = { viewModel.applyPreset(preset) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        item {
            Text("Dark Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = config.isDarkMode,
                    onCheckedChange = { viewModel.updateDarkMode(it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Dark mode presets
            if (config.isDarkMode) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePresets.allDarkPresets.forEach { preset ->
                        PresetButton(
                            preset = preset,
                            isSelected = config.seedColorHex == preset.colorHex && config.isDarkMode,
                            onSelect = { viewModel.applyPreset(preset) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        item {
            Text("Custom Colour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Current color: ${config.seedColorHex}", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun PresetButton(
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
            // Color preview circle
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(android.graphics.Color.parseColor(preset.colorHex))
            ) {}

            // Preset info
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

            // Selection indicator
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

