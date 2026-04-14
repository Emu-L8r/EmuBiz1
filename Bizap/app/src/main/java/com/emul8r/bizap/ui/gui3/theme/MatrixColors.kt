package com.emul8r.bizap.ui.gui3.theme

import androidx.compose.ui.graphics.Color

/**
 * Matrix Theme Color Palette
 *
 * Cyberpunk green on dark aesthetic with professional business elements.
 */

// Primary Matrix Colors
val MatrixGreen = Color(0xFF00DD00)          // Bright matrix green
val MatrixGreenBright = Color(0xFF00FF00)    // Accent bright green
val CyanAccent = Color(0xFF00FFFF)           // Electric cyan
val MatrixDarkGreen = Color(0xFF00AA00)      // Darker shade for depth

// Backgrounds
val MatrixBlack = Color(0xFF000000)          // True black
val MatrixSurface = Color(0xFF111111)        // Deep gray
val MatrixSurfaceVariant = Color(0xFF1A1A1A) // Slightly lighter surface

// Text Colors
val MatrixTextPrimary = Color(0xFF00DD00)    // Green text
val MatrixTextSecondary = Color(0xFF00CCCC)  // Cyan text
val MatrixTextTertiary = Color(0xFF888888)   // Gray text

// Semantic Colors
val MatrixSuccess = Color(0xFF00FF00)        // Bright green
val MatrixWarning = Color(0xFFFFDD00)        // Bright yellow/amber
val MatrixError = Color(0xFFFF4444)          // Red
val MatrixInfo = Color(0xFF00FFFF)           // Cyan

// Borders & Highlights
val MatrixBorderGreen = Color(0xFF00DD00)    // Border color
val MatrixHighlight = Color(0xFF00FF00)      // Highlight color
val MatrixGlow = Color(0xFF00DD00)           // Glow effect color

// Opacity Variants
val MatrixGreenFaded = Color(0x6600DD00)     // 40% opacity
val MatrixGreenVeryFaded = Color(0x3300DD00) // 20% opacity

/**
 * Material3-compatible color scheme for Matrix theme
 */
object MatrixColorScheme {
    fun lightColorScheme() = androidx.compose.material3.lightColorScheme(
        primary = MatrixGreen,
        onPrimary = MatrixBlack,
        primaryContainer = MatrixGreen.copy(alpha = 0.15f),
        onPrimaryContainer = MatrixGreen,

        secondary = CyanAccent,
        onSecondary = MatrixBlack,
        secondaryContainer = CyanAccent.copy(alpha = 0.15f),
        onSecondaryContainer = CyanAccent,

        tertiary = MatrixGreenBright,
        onTertiary = MatrixBlack,
        tertiaryContainer = MatrixGreenBright.copy(alpha = 0.15f),
        onTertiaryContainer = MatrixGreenBright,

        background = MatrixBlack,
        onBackground = MatrixGreen,

        surface = MatrixSurface,
        onSurface = MatrixGreen,
        surfaceVariant = MatrixSurfaceVariant,
        onSurfaceVariant = MatrixGreen.copy(alpha = 0.8f),

        error = MatrixError,
        onError = MatrixBlack,
        errorContainer = MatrixError.copy(alpha = 0.15f),
        onErrorContainer = MatrixError,

        outline = MatrixGreen.copy(alpha = 0.5f),
        outlineVariant = MatrixGreen.copy(alpha = 0.3f),
        scrim = Color.Black
    )

    fun darkColorScheme() = androidx.compose.material3.darkColorScheme(
        primary = MatrixGreen,
        onPrimary = MatrixBlack,
        primaryContainer = MatrixGreenDarkVar,
        onPrimaryContainer = MatrixGreen,

        secondary = CyanAccent,
        onSecondary = MatrixBlack,
        secondaryContainer = CyanAccent.copy(alpha = 0.2f),
        onSecondaryContainer = CyanAccent,

        tertiary = MatrixGreenBright,
        onTertiary = MatrixBlack,
        tertiaryContainer = MatrixGreenBright.copy(alpha = 0.2f),
        onTertiaryContainer = MatrixGreenBright,

        background = MatrixBlack,
        onBackground = MatrixGreen,

        surface = MatrixSurface,
        onSurface = MatrixGreen,
        surfaceVariant = MatrixSurfaceVariant,
        onSurfaceVariant = MatrixGreen.copy(alpha = 0.8f),

        error = MatrixError,
        onError = MatrixBlack,
        errorContainer = MatrixError.copy(alpha = 0.2f),
        onErrorContainer = MatrixError,

        outline = MatrixGreen.copy(alpha = 0.5f),
        outlineVariant = MatrixGreen.copy(alpha = 0.3f),
        scrim = Color.Black
    )
}

// Dark variant for brand color
private val MatrixGreenDarkVar = Color(0xFF00AA00)

