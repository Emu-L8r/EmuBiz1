package com.emul8r.bizap.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.ThemeConfig
import timber.log.Timber

/**
 * Modern theme using Material Design 3 style aesthetics.
 * 
 * Features:
 * - Dynamically generated color palette from user's seed color
 * - Larger corner radiuses (8-24dp) for a modern, rounded look
 * - Material You inspired colors with enhanced expressiveness
 * - Reactive to theme config changes - colors update instantly
 *
 * @param themeConfig User's custom theme settings (seed color, dark mode)
 * @param content The composable content to wrap with this theme
 */
@Composable
fun ModernTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    content: @Composable () -> Unit
) {
    val seedColor = parseSeedColor(themeConfig.seedColorHex)
    Timber.d("🎨 Modern theme: seedColor=${themeConfig.seedColorHex}, isDarkMode=${themeConfig.isDarkMode}")

    // Generate harmonized color scheme from seed color
    val lightColors = lightColorScheme(
        primary = seedColor,
        onPrimary = Color.White,
        primaryContainer = seedColor.lighten(0.85f),
        onPrimaryContainer = seedColor.darken(0.4f),

        secondary = seedColor.darken(0.1f),
        onSecondary = Color.White,
        secondaryContainer = seedColor.lighten(0.8f),
        onSecondaryContainer = seedColor.darken(0.35f),

        tertiary = seedColor.darken(0.25f),
        onTertiary = Color.White,
        tertiaryContainer = seedColor.lighten(0.75f),
        onTertiaryContainer = seedColor.darken(0.3f),

        error = Color(0xFFB00020),
        onError = Color.White,
        errorContainer = Color(0xFFFDE7E9),
        onErrorContainer = Color(0xFF8C0009),
        
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = seedColor.lighten(0.9f),
        onSurfaceVariant = Color(0xFF49454F),
        
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0)
    )

    val darkColors = darkColorScheme(
        primary = seedColor.lighten(0.4f),
        onPrimary = seedColor.darken(0.6f),
        primaryContainer = seedColor.darken(0.2f),
        onPrimaryContainer = seedColor.lighten(0.7f),

        secondary = seedColor.darken(0.05f),
        onSecondary = Color.Black,
        secondaryContainer = seedColor.darken(0.35f),
        onSecondaryContainer = seedColor.lighten(0.65f),

        tertiary = seedColor.lighten(0.15f),
        onTertiary = seedColor.darken(0.5f),
        tertiaryContainer = seedColor.darken(0.3f),
        onTertiaryContainer = seedColor.lighten(0.7f),

        error = Color(0xFFCF6679),
        onError = Color(0xFF5F000B),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFDE7E9),
        
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = seedColor.darken(0.3f),
        onSurfaceVariant = Color(0xFFCAC4D0),
        
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F)
    )

    val shapes = Shapes(
        // Material Design 3 style - larger, more rounded shapes
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp)
    )

    MaterialTheme(
        colorScheme = if (themeConfig.isDarkMode) darkColors else lightColors,
        shapes = shapes,
        typography = MaterialTheme.typography,
        content = content
    )
}

// Color manipulation helpers
private fun Color.darken(factor: Float): Color = copy(
    red = (red * (1 - factor)).coerceIn(0f, 1f),
    green = (green * (1 - factor)).coerceIn(0f, 1f),
    blue = (blue * (1 - factor)).coerceIn(0f, 1f)
)

private fun Color.lighten(factor: Float): Color = copy(
    red = (red + (1 - red) * factor).coerceIn(0f, 1f),
    green = (green + (1 - green) * factor).coerceIn(0f, 1f),
    blue = (blue + (1 - blue) * factor).coerceIn(0f, 1f)
)

private fun parseSeedColor(hexString: String?): Color {
    return try {
        if (hexString.isNullOrBlank()) {
            Color(0xFF6750A4) // Default Material Purple
        } else {
            val colorInt = android.graphics.Color.parseColor(hexString)
            Color(colorInt.toLong() and 0xFFFFFFFFL)
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse hex: $hexString")
        Color(0xFF6750A4)
    }
}

