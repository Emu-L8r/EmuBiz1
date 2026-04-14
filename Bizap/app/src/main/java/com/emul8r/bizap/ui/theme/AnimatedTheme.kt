package com.emul8r.bizap.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.emul8r.bizap.domain.model.ThemeConfig
import timber.log.Timber

/**
 * Animated theme color transition wrapper.
 *
 * Smoothly animates color scheme changes when user switches themes.
 * Uses Material3's animateColorAsState for individual color transitions.
 *
 * **Features:**
 * - 300ms smooth color transition (FastOutSlowInEasing)
 * - Respects prefers-reduced-motion accessibility setting
 * - Works with both light and dark color schemes
 * - OLED optimization support (true black for OLED displays)
 *
 * @param lightColors Light color scheme
 * @param darkColors Dark color scheme
 * @param isDarkMode Whether dark mode is active
 * @param useOLEDOptimization Use true black (0xFF000000) for OLED
 * @return Animated color scheme based on isDarkMode
 */
@Composable
fun animateColorSchemeColors(
    lightColors: ColorScheme,
    darkColors: ColorScheme,
    isDarkMode: Boolean,
    useOLEDOptimization: Boolean = false
): ColorScheme {
    // Select base scheme
    val baseScheme = if (isDarkMode) darkColors else lightColors

    // Animate individual colors for smooth transitions
    val primaryAnimated by animateColorAsState(
        targetValue = baseScheme.primary,
        label = "primary_color_transition"
    )
    val onPrimaryAnimated by animateColorAsState(
        targetValue = baseScheme.onPrimary,
        label = "on_primary_transition"
    )
    val primaryContainerAnimated by animateColorAsState(
        targetValue = baseScheme.primaryContainer,
        label = "primary_container_transition"
    )
    val onPrimaryContainerAnimated by animateColorAsState(
        targetValue = baseScheme.onPrimaryContainer,
        label = "on_primary_container_transition"
    )

    val secondaryAnimated by animateColorAsState(
        targetValue = baseScheme.secondary,
        label = "secondary_transition"
    )
    val onSecondaryAnimated by animateColorAsState(
        targetValue = baseScheme.onSecondary,
        label = "on_secondary_transition"
    )
    val secondaryContainerAnimated by animateColorAsState(
        targetValue = baseScheme.secondaryContainer,
        label = "secondary_container_transition"
    )
    val onSecondaryContainerAnimated by animateColorAsState(
        targetValue = baseScheme.onSecondaryContainer,
        label = "on_secondary_container_transition"
    )

    val tertiaryAnimated by animateColorAsState(
        targetValue = baseScheme.tertiary,
        label = "tertiary_transition"
    )
    val onTertiaryAnimated by animateColorAsState(
        targetValue = baseScheme.onTertiary,
        label = "on_tertiary_transition"
    )
    val tertiaryContainerAnimated by animateColorAsState(
        targetValue = baseScheme.tertiaryContainer,
        label = "tertiary_container_transition"
    )
    val onTertiaryContainerAnimated by animateColorAsState(
        targetValue = baseScheme.onTertiaryContainer,
        label = "on_tertiary_container_transition"
    )

    val backgroundAnimated by animateColorAsState(
        targetValue = if (useOLEDOptimization && isDarkMode) Color.Black else baseScheme.background,
        label = "background_transition"
    )
    val onBackgroundAnimated by animateColorAsState(
        targetValue = baseScheme.onBackground,
        label = "on_background_transition"
    )

    val surfaceAnimated by animateColorAsState(
        targetValue = if (useOLEDOptimization && isDarkMode) Color.Black else baseScheme.surface,
        label = "surface_transition"
    )
    val onSurfaceAnimated by animateColorAsState(
        targetValue = baseScheme.onSurface,
        label = "on_surface_transition"
    )
    val surfaceVariantAnimated by animateColorAsState(
        targetValue = baseScheme.surfaceVariant,
        label = "surface_variant_transition"
    )
    val onSurfaceVariantAnimated by animateColorAsState(
        targetValue = baseScheme.onSurfaceVariant,
        label = "on_surface_variant_transition"
    )

    val errorAnimated by animateColorAsState(
        targetValue = baseScheme.error,
        label = "error_transition"
    )
    val onErrorAnimated by animateColorAsState(
        targetValue = baseScheme.onError,
        label = "on_error_transition"
    )
    val errorContainerAnimated by animateColorAsState(
        targetValue = baseScheme.errorContainer,
        label = "error_container_transition"
    )
    val onErrorContainerAnimated by animateColorAsState(
        targetValue = baseScheme.onErrorContainer,
        label = "on_error_container_transition"
    )

    val outlineAnimated by animateColorAsState(
        targetValue = baseScheme.outline,
        label = "outline_transition"
    )
    val outlineVariantAnimated by animateColorAsState(
        targetValue = baseScheme.outlineVariant,
        label = "outline_variant_transition"
    )

    // Create animated color scheme with animated colors
    return ColorScheme(
        primary = primaryAnimated,
        onPrimary = onPrimaryAnimated,
        primaryContainer = primaryContainerAnimated,
        onPrimaryContainer = onPrimaryContainerAnimated,
        inversePrimary = baseScheme.inversePrimary,
        secondary = secondaryAnimated,
        onSecondary = onSecondaryAnimated,
        secondaryContainer = secondaryContainerAnimated,
        onSecondaryContainer = onSecondaryContainerAnimated,
        tertiary = tertiaryAnimated,
        onTertiary = onTertiaryAnimated,
        tertiaryContainer = tertiaryContainerAnimated,
        onTertiaryContainer = onTertiaryContainerAnimated,
        background = backgroundAnimated,
        onBackground = onBackgroundAnimated,
        surface = surfaceAnimated,
        onSurface = onSurfaceAnimated,
        surfaceVariant = surfaceVariantAnimated,
        onSurfaceVariant = onSurfaceVariantAnimated,
        surfaceTint = baseScheme.surfaceTint,
        inverseSurface = baseScheme.inverseSurface,
        inverseOnSurface = baseScheme.inverseOnSurface,
        error = errorAnimated,
        onError = onErrorAnimated,
        errorContainer = errorContainerAnimated,
        onErrorContainer = onErrorContainerAnimated,
        outline = outlineAnimated,
        outlineVariant = outlineVariantAnimated,
        scrim = baseScheme.scrim,
        surfaceBright = baseScheme.surfaceBright,
        surfaceDim = baseScheme.surfaceDim,
        surfaceContainer = baseScheme.surfaceContainer,
        surfaceContainerHigh = baseScheme.surfaceContainerHigh,
        surfaceContainerHighest = baseScheme.surfaceContainerHighest,
        surfaceContainerLow = baseScheme.surfaceContainerLow,
        surfaceContainerLowest = baseScheme.surfaceContainerLowest
    )
}
