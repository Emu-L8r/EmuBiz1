package com.emul8r.bizap.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Modern theme using Material Design 3 style aesthetics.
 * 
 * Features:
 * - Material Design 3 color palette (purple-based primary)
 * - Larger corner radiuses (8-24dp) for a modern, rounded look
 * - Material You inspired colors with enhanced expressiveness
 * 
 * @param isDarkMode Whether to use dark or light color scheme
 * @param content The composable content to wrap with this theme
 */
@Composable
fun ModernTheme(
    isDarkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF6200EE),           // Material Purple (M3 default)
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE3DFFE),  // Light purple container
        onPrimaryContainer = Color(0xFF1F0054),
        
        secondary = Color(0xFF03DAC6),         // Material Teal (M3 secondary)
        onSecondary = Color(0xFF003735),
        secondaryContainer = Color(0xFFA2F4EE), // Light teal container
        onSecondaryContainer = Color(0xFF003735),
        
        tertiary = Color(0xFF018786),          // Teal variant
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF97F0EE),
        onTertiaryContainer = Color(0xFF003735),
        
        error = Color(0xFFB00020),             // M3 error color
        onError = Color.White,
        errorContainer = Color(0xFFFDE7E9),
        onErrorContainer = Color(0xFF8C0009),
        
        background = Color(0xFFFFFBFE),        // Slightly warm white (M3 style)
        onBackground = Color(0xFF1C1B1F),
        
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),    // Slight purple tint
        onSurfaceVariant = Color(0xFF49454F),
        
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0)
    )

    val darkColors = darkColorScheme(
        primary = Color(0xFFBB86FC),           // Lighter purple for dark mode
        onPrimary = Color(0xFF3700B3),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFE3DFFE),
        
        secondary = Color(0xFF03DAC6),
        onSecondary = Color(0xFF003735),
        secondaryContainer = Color(0xFF005047),
        onSecondaryContainer = Color(0xFFA2F4EE),
        
        tertiary = Color(0xFF03DAC6),
        onTertiary = Color(0xFF003735),
        tertiaryContainer = Color(0xFF005047),
        onTertiaryContainer = Color(0xFF97F0EE),
        
        error = Color(0xFFCF6679),
        onError = Color(0xFF5F000B),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFDE7E9),
        
        background = Color(0xFF1C1B1F),        // M3 dark background
        onBackground = Color(0xFFE6E1E5),
        
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
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
        colorScheme = if (isDarkMode) darkColors else lightColors,
        shapes = shapes,
        typography = MaterialTheme.typography,  // Use default Material typography
        content = content
    )
}
