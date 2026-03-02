package com.emul8r.bizap.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Pre-defined theme presets for quick application
 */
data class ThemePreset(
    val name: String,
    val colorHex: String,
    val description: String,
    val icon: ImageVector = Icons.Default.Palette,
    val isDarkModePreset: Boolean = false
)

object ThemePresets {
    // Light mode presets
    val ProfessionalBlue = ThemePreset(
        name = "Professional Blue",
        colorHex = "#003087",
        description = "Trust and professionalism",
        isDarkModePreset = false
    )

    val ModernTeal = ThemePreset(
        name = "Modern Teal",
        colorHex = "#00897B",
        description = "Contemporary and fresh",
        isDarkModePreset = false
    )

    val WarmOrange = ThemePreset(
        name = "Warm Orange",
        colorHex = "#FF6F00",
        description = "Energetic and welcoming",
        isDarkModePreset = false
    )

    val DefaultPurple = ThemePreset(
        name = "Material Purple",
        colorHex = "#6750A4",
        description = "Modern Material Design",
        isDarkModePreset = false
    )

    // Dark mode presets
    val MidnightDark = ThemePreset(
        name = "Midnight Dark",
        colorHex = "#1A1A2E",
        description = "Deep and professional dark",
        isDarkModePreset = true
    )

    val DarkTeal = ThemePreset(
        name = "Dark Teal",
        colorHex = "#0D3B3B",
        description = "Cool dark variant",
        isDarkModePreset = true
    )

    // All presets for UI selection
    val allLightPresets = listOf(
        DefaultPurple,
        ProfessionalBlue,
        ModernTeal,
        WarmOrange
    )

    val allDarkPresets = listOf(
        MidnightDark,
        DarkTeal
    )

    val allPresets = allLightPresets + allDarkPresets
}



