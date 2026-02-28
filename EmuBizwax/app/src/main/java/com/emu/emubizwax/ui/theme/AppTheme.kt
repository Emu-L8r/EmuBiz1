package com.emu.emubizwax.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.emu.emubizwax.domain.repository.ThemeConfig

@Composable
fun InvoicingTheme(
    themeConfig: ThemeConfig,
    content: @Composable () -> Unit
) {
    val seedColor = Color(android.graphics.Color.parseColor(themeConfig.primaryColorHex))
    
    // Generate M3 ColorScheme based on the user's picked seed color
    val colorScheme = if (themeConfig.isDarkMode) {
        dynamicDarkColorScheme(seedColor) // Or fallback to darkColorScheme(primary = seedColor)
    } else {
        dynamicLightColorScheme(seedColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Helper to generate schemes if not on Android 12+ (since we target Android 5.0)
private fun dynamicLightColorScheme(seed: Color) = lightColorScheme(
    primary = seed,
    onPrimary = Color.White,
    primaryContainer = seed.copy(alpha = 0.1f)
    // Add other color mappings as needed
)

private fun dynamicDarkColorScheme(seed: Color) = darkColorScheme(
    primary = seed,
    onPrimary = Color.Black,
    primaryContainer = seed.copy(alpha = 0.1f)
    // Add other color mappings as needed
)
