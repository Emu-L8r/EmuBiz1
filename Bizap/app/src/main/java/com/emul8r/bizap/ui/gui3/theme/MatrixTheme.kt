package com.emul8r.bizap.ui.gui3.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.emul8r.bizap.ui.gui3.components.MatrixBackground

/**
 * Matrix Theme Composable
 *
 * Applies the Matrix design system (green on dark, monospace elements, cyberpunk aesthetic)
 * to the entire UI subtree.
 *
 * Features:
 * - Matrix green color palette (#00DD00)
 * - Dark-mode optimized design
 * - Monospace typography for code/numbers
 * - Professional business styling
 * - Smooth animations
 */
@Composable
fun MatrixTheme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) {
        MatrixColorScheme.darkColorScheme()
    } else {
        MatrixColorScheme.lightColorScheme()
    }

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(20.dp)
    )

    val typography = MatrixTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography,
        content = { MatrixBackground { content() } }
    )
}

/**
 * Matrix Typography System
 *
 * Combines monospace fonts for code/numbers with clean sans-serif for body text.
 * Creates visual hierarchy while maintaining the Matrix aesthetic.
 */
@Composable
fun MatrixTypography(): Typography {
    return Typography(
        // Titles and Headlines
        displayLarge = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
            letterSpacing = 1.5.sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            letterSpacing = 1.2.sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp,
            letterSpacing = 1.sp
        ),

        // Headlines
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            letterSpacing = 0.8.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 22.sp,
            letterSpacing = 0.6.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp,
            letterSpacing = 0.4.sp
        ),

        // Body text - clean sans-serif
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // Labels - monospace for numbers/codes
        labelLarge = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

