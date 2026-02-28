package com.example.databaser.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Import stable colors and typography
import com.example.databaser.ui.theme.Black
import com.example.databaser.ui.theme.DarkBlue
import com.example.databaser.ui.theme.DarkGreen
import com.example.databaser.ui.theme.DarkRed
import com.example.databaser.ui.theme.Grey12
import com.example.databaser.ui.theme.GreyF0
import com.example.databaser.ui.theme.LightBlue
import com.example.databaser.ui.theme.LightGreen
import com.example.databaser.ui.theme.LightRed
import com.example.databaser.ui.theme.Typography
import com.example.databaser.ui.theme.White

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF2196F3),
    tertiary = Color(0xFF9C27B0),
    background = Grey12,
    surface = Grey12,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = DarkGreen,
    secondaryContainer = DarkBlue,
    tertiaryContainer = DarkRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF2196F3),
    tertiary = Color(0xFF9C27B0),
    background = White,
    surface = GreyF0,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    primaryContainer = LightGreen,
    secondaryContainer = LightBlue,
    tertiaryContainer = LightRed
)

@Composable
fun DatabaserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
