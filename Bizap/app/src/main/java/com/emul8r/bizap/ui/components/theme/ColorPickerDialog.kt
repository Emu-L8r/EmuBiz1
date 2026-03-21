package com.emul8r.bizap.ui.components.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Advanced color picker for theme customization.
 * Allows users to select from HSL color space or enter hex values.
 */
@Composable
fun ColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Pick a Color"
) {
    var hexValue by remember { mutableStateOf(colorToHex(currentColor)) }
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var lightness by remember { mutableStateOf(0.5f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            color = hslToColor(hue, saturation, lightness),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                )

                // Hue slider
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hue", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = hue,
                        onValueChange = { hue = it },
                        valueRange = 0f..360f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${hue.roundToInt()}°", style = MaterialTheme.typography.bodySmall)
                }

                // Saturation slider
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Saturation", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = saturation,
                        onValueChange = { saturation = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${(saturation * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
                }

                // Lightness slider
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Lightness", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = lightness,
                        onValueChange = { lightness = it },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${(lightness * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
                }

                // Hex input
                OutlinedTextField(
                    value = hexValue,
                    onValueChange = { newValue ->
                        if (newValue.length <= 7 && (newValue.isEmpty() || newValue.startsWith("#"))) {
                            hexValue = newValue
                            try {
                                val color = Color(android.graphics.Color.parseColor(newValue.ifEmpty { "#000000" }))
                                val rgb = colorToHsl(color)
                                hue = rgb[0]
                                saturation = rgb[1]
                                lightness = rgb[2]
                            } catch (e: Exception) {
                                // Invalid hex, ignore
                            }
                        }
                    },
                    label = { Text("Hex Code") },
                    placeholder = { Text("#000000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(hslToColor(hue, saturation, lightness))
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Compact color selector button showing current color.
 */
@Composable
fun ColorSelectorButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(color, shape = RoundedCornerShape(8.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                colorToHex(color),
                style = MaterialTheme.typography.labelSmall,
                color = getContrastColor(color),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Preset color scheme selector.
 */
@Composable
fun PresetThemeSelector(
    presets: List<PresetTheme>,
    selectedPreset: PresetTheme?,
    onPresetSelected: (PresetTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Preset Themes", style = MaterialTheme.typography.labelLarge)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { preset ->
                PresetThemeCard(
                    preset = preset,
                    isSelected = preset == selectedPreset,
                    onClick = { onPresetSelected(preset) }
                )
            }
        }
    }
}

@Composable
private fun PresetThemeCard(
    preset: PresetTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(preset.name, style = MaterialTheme.typography.labelMedium)
                Text(preset.description, style = MaterialTheme.typography.bodySmall)
            }

            // Color preview squares
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(preset.primary, RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(preset.secondary, RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(preset.tertiary, RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Data class for preset themes.
 */
data class PresetTheme(
    val id: String,
    val name: String,
    val description: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)

// ============ Color Utility Functions ============

fun colorToHex(color: Color): String {
    val alpha = (color.alpha * 255).toInt()
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
}

fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
    val c = (1 - kotlin.math.abs(2 * lightness - 1)) * saturation
    val hPrime = hue / 60f
    val x = c * (1 - kotlin.math.abs(hPrime % 2 - 1))

    val (r, g, b) = when {
        hPrime < 1 -> Triple(c, x, 0f)
        hPrime < 2 -> Triple(x, c, 0f)
        hPrime < 3 -> Triple(0f, c, x)
        hPrime < 4 -> Triple(0f, x, c)
        hPrime < 5 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    val m = lightness - c / 2
    return Color(r + m, g + m, b + m)
}

fun colorToHsl(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue

    val max = kotlin.math.max(r, kotlin.math.max(g, b))
    val min = kotlin.math.min(r, kotlin.math.min(g, b))
    val l = (max + min) / 2

    val hue = when {
        max == min -> 0f
        max == r -> (60 * ((g - b) / (max - min)) + 360) % 360
        max == g -> (60 * ((b - r) / (max - min)) + 120) % 360
        else -> (60 * ((r - g) / (max - min)) + 240) % 360
    }

    val saturation = when {
        max == min -> 0f
        l < 0.5 -> (max - min) / (max + min)
        else -> (max - min) / (2 - max - min)
    }

    return floatArrayOf(hue, saturation, l)
}

fun getContrastColor(backgroundColor: Color): Color {
    val luminance = 0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue
    return if (luminance > 0.5) Color.Black else Color.White
}

