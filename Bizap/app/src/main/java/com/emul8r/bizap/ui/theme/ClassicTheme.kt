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
 * Classic theme using Material Design 2 style aesthetics.
 * 
 * Features:
 * - Material Design 2 color palette (blue-based primary)
 * - Smaller corner radiuses (4-12dp) for a more traditional look
 * - Classic Material colors with good contrast
 * 
 * @param isDarkMode Whether to use dark or light color scheme
 * @param content The composable content to wrap with this theme
 */
@Composable
fun ClassicTheme(
    isDarkMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF1976D2),           // Material Blue 700
        onPrimary = Color.White,
        primaryContainer = Color(0xFF90CAF9),  // Material Blue 200
        onPrimaryContainer = Color(0xFF0D47A1),
        
        secondary = Color(0xFF0097A7),         // Material Cyan 700
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF80DEEA), // Material Cyan 200
        onSecondaryContainer = Color(0xFF006064),
        
        tertiary = Color(0xFF388E3C),          // Material Green 700
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFA5D6A7), // Material Green 200
        onTertiaryContainer = Color(0xFF1B5E20),
        
        error = Color(0xFFD32F2F),             // Material Red 700
        onError = Color.White,
        errorContainer = Color(0xFFFFCDD2),    // Material Red 100
        onErrorContainer = Color(0xFFB71C1C),
        
        background = Color(0xFFFAFAFA),        // Very light gray
        onBackground = Color(0xFF212121),      // Almost black
        
        surface = Color.White,
        onSurface = Color(0xFF212121),
        surfaceVariant = Color(0xFFEEEEEE),    // Light gray
        onSurfaceVariant = Color(0xFF616161),  // Medium gray
        
        outline = Color(0xFFBDBDBD),           // Gray border
        outlineVariant = Color(0xFFE0E0E0)
    )

    val darkColors = darkColorScheme(
        primary = Color(0xFF90CAF9),           // Lighter blue for dark mode
        onPrimary = Color(0xFF0D47A1),
        primaryContainer = Color(0xFF1565C0),
        onPrimaryContainer = Color(0xFFBBDEFB),
        
        secondary = Color(0xFF80DEEA),
        onSecondary = Color(0xFF006064),
        secondaryContainer = Color(0xFF00838F),
        onSecondaryContainer = Color(0xFFB2EBF2),
        
        tertiary = Color(0xFFA5D6A7),
        onTertiary = Color(0xFF1B5E20),
        tertiaryContainer = Color(0xFF2E7D32),
        onTertiaryContainer = Color(0xFFC8E6C9),
        
        error = Color(0xFFEF9A9A),
        onError = Color(0xFFB71C1C),
        errorContainer = Color(0xFFC62828),
        onErrorContainer = Color(0xFFFFEBEE),
        
        background = Color(0xFF121212),        // Material dark background
        onBackground = Color(0xFFE0E0E0),
        
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFE0E0E0),
        surfaceVariant = Color(0xFF2C2C2C),
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
        colorScheme = if (isDarkMode) darkColors else lightColors,
        shapes = shapes,
        typography = MaterialTheme.typography,  // Use default Material typography
        content = content
    )
}
