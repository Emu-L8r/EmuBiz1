package com.emul8r.bizap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.emul8r.bizap.domain.model.ThemeConfig
import timber.log.Timber

private val BrandPurple = Color(0xFF6750A4)

@Composable
fun BizapTheme(
    themeConfig: ThemeConfig,
    content: @Composable () -> Unit
) {
    val seedColor = parseSeedColor(themeConfig.seedColorHex)
    
    // Generate a harmonized color scheme where all slots relate to the seed
    val colorScheme = if (themeConfig.isDarkMode) {
        darkColorScheme(
            primary = seedColor,
            onPrimary = calculateOnColor(seedColor),
            primaryContainer = seedColor.copy(alpha = 0.3f),
            onPrimaryContainer = Color.White,
            secondary = seedColor, // Harmonized secondary
            surface = Color(0xFF1C1B1F),
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = seedColor,
            onPrimary = Color.White,
            primaryContainer = seedColor.copy(alpha = 0.12f), // Soft harmonized background
            onPrimaryContainer = seedColor,
            secondary = seedColor,
            onSecondary = Color.White,
            surface = Color(0xFFFBFDF8),
            onSurface = Color(0xFF191C19)
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

