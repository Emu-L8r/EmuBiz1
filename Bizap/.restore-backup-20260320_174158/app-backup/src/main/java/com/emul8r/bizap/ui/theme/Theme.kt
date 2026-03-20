package com.emul8r.bizap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.emul8r.bizap.domain.model.ThemeConfig
import timber.log.Timber

private val BrandPurple = Color(0xFF6750A4)

// Helper functions to create tonal variants from a seed color
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

@Composable
fun BizapTheme(
    themeConfig: ThemeConfig,
    content: @Composable () -> Unit
) {
    val seedColor = parseSeedColor(themeConfig.seedColorHex)
    
    // Generate a harmonized color scheme where all slots relate to the seed color
    val colorScheme = if (themeConfig.isDarkMode) {
        darkColorScheme(
            primary = seedColor,
            onPrimary = calculateOnColor(seedColor),
            primaryContainer = seedColor.copy(alpha = 0.3f),
            onPrimaryContainer = seedColor.lighten(0.7f),
            secondary = seedColor.darken(0.15f),        // Different from primary
            onSecondary = Color.White,
            secondaryContainer = seedColor.copy(alpha = 0.2f),
            onSecondaryContainer = seedColor.lighten(0.6f),
            tertiary = seedColor.lighten(0.2f),
            onTertiary = Color.Black,
            tertiaryContainer = seedColor.copy(alpha = 0.15f),
            onTertiaryContainer = seedColor.lighten(0.5f),
            background = Color(0xFF1C1B1F),
            onBackground = Color(0xFFE6E1E5),
            surface = Color(0xFF1C1B1F),
            onSurface = Color(0xFFE6E1E5),
            surfaceVariant = Color(0xFF49454F),
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = Color(0xFF938F99),
            outlineVariant = Color(0xFF49454F),
            error = Color(0xFFF2B8B5),
            onError = Color(0xFF601410),
            errorContainer = Color(0xFF8C1D18),
            onErrorContainer = Color(0xFFF2B8B5)
        )
    } else {
        lightColorScheme(
            primary = seedColor,
            onPrimary = Color.White,
            primaryContainer = seedColor.lighten(0.85f),
            onPrimaryContainer = seedColor.darken(0.4f),
            secondary = seedColor.darken(0.1f),         // Different from primary
            onSecondary = Color.White,
            secondaryContainer = seedColor.lighten(0.8f),
            onSecondaryContainer = seedColor.darken(0.35f),
            tertiary = seedColor.darken(0.25f),
            onTertiary = Color.White,
            tertiaryContainer = seedColor.lighten(0.75f),
            onTertiaryContainer = seedColor.darken(0.3f),
            background = Color(0xFFFBFDF8),
            onBackground = Color(0xFF191C19),
            surface = Color(0xFFFBFDF8),
            onSurface = Color(0xFF191C19),
            surfaceVariant = seedColor.lighten(0.9f),
            onSurfaceVariant = Color(0xFF49454F),
            outline = Color(0xFF79747E),
            outlineVariant = Color(0xFFCAC4D0),
            error = Color(0xFFB3261E),
            onError = Color.White,
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun parseSeedColor(hexString: String?): Color {
    return try {
        if (hexString.isNullOrBlank()) {
            BrandPurple
        } else {
            val colorInt = android.graphics.Color.parseColor(hexString)
            Color(colorInt.toLong() and 0xFFFFFFFFL)
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse hex: $hexString")
        BrandPurple
    }
}

// Helper to determine if text should be black or white on a background
private fun calculateOnColor(color: Color): Color {
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return if (luminance > 0.5) Color.Black else Color.White
}
