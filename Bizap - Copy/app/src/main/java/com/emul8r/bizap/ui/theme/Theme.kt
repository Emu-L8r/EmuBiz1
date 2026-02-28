package com.emul8r.bizap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.emul8r.bizap.domain.model.ThemeConfig

@Composable
fun EmuBizzzTheme(
    themeConfig: ThemeConfig,
    content: @Composable () -> Unit
) {
    val seedColor = Color(android.graphics.Color.parseColor(themeConfig.seedColorHex))
    
    // Modern M3 dynamic color generation from a single seed
    val colorScheme = if (themeConfig.isDarkMode) {
        androidx.compose.material3.darkColorScheme(primary = seedColor)
    } else {
        androidx.compose.material3.lightColorScheme(primary = seedColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure you have a Typography.kt file
        content = content
    )
}
