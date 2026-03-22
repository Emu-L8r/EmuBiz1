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
 * Classic theme using Material Design 2 style aesthetics.
 * 
 * Features:
 * - Dynamically generated color palette from user's seed color
 * - Smaller corner radiuses (4-12dp) for a more traditional look
 * - Classic Material Design 2 aesthetic with good contrast
 * - Reactive to theme config changes - colors update instantly
 *
 * @param themeConfig User's custom theme settings (seed color, dark mode)
 * @param content The composable content to wrap with this theme
 */
@Composable
fun ClassicTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    content: @Composable () -> Unit
) {
    val seedColor = parseSeedColor(themeConfig.seedColorHex)
    Timber.d("🎨 Classic theme: seedColor=${themeConfig.seedColorHex}, isDarkMode=${themeConfig.isDarkMode}")

    // Generate harmonized color scheme from seed color
    val lightColors = lightColorScheme(
        primary = seedColor,
        onPrimary = Color.White,
        primaryContainer = seedColor.lighten(0.8f),
        onPrimaryContainer = seedColor.darken(0.35f),

        secondary = seedColor.darken(0.2f),
        onSecondary = Color.White,
        secondaryContainer = seedColor.lighten(0.75f),
        onSecondaryContainer = seedColor.darken(0.3f),

        tertiary = seedColor.darken(0.15f),
        onTertiary = Color.White,
        tertiaryContainer = seedColor.lighten(0.7f),
        onTertiaryContainer = seedColor.darken(0.25f),

        error = Color(0xFFD32F2F),
        onError = Color.White,
        errorContainer = Color(0xFFFFCDD2),
        onErrorContainer = Color(0xFFB71C1C),
        
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF212121),

        surface = Color.White,
        onSurface = Color(0xFF212121),
        surfaceVariant = seedColor.lighten(0.85f),
        onSurfaceVariant = Color(0xFF616161),

        outline = Color(0xFFBDBDBD),
        outlineVariant = Color(0xFFE0E0E0)
    )

    val darkColors = darkColorScheme(
        primary = seedColor.lighten(0.3f),
        onPrimary = seedColor.darken(0.5f),
        primaryContainer = seedColor.darken(0.1f),
        onPrimaryContainer = seedColor.lighten(0.65f),

        secondary = seedColor.darken(0.1f),
        onSecondary = Color.Black,
        secondaryContainer = seedColor.darken(0.35f),
        onSecondaryContainer = seedColor.lighten(0.6f),

        tertiary = seedColor.lighten(0.2f),
        onTertiary = seedColor.darken(0.4f),
        tertiaryContainer = seedColor.darken(0.25f),
        onTertiaryContainer = seedColor.lighten(0.65f),

        error = Color(0xFFEF9A9A),
        onError = Color(0xFFB71C1C),
        errorContainer = Color(0xFFC62828),
        onErrorContainer = Color(0xFFFFEBEE),
        
        background = Color(0xFF121212),
        onBackground = Color(0xFFE0E0E0),
        
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFE0E0E0),
        surfaceVariant = seedColor.darken(0.35f),
        onSurfaceVariant = Color(0xFFBDBDBD),
        
        outline = Color(0xFF616161),
        outlineVariant = Color(0xFF424242)
    )

    val shapes = Shapes(
        // Material Design 2 style - smaller radiuses
        extraSmall = RoundedCornerShape(2.dp),
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(12.dp),
        extraLarge = RoundedCornerShape(16.dp)
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
            Color(0xFF1976D2) // Default Material Blue
        } else {
            val colorInt = android.graphics.Color.parseColor(hexString)
            Color(colorInt.toLong() and 0xFFFFFFFFL)
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse hex: $hexString")
        Color(0xFF1976D2)
    }
}

