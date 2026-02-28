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

// Default Theme
private val DarkColorScheme = darkColorScheme(
    primary = Red,
    secondary = CornflowerBlue,
    tertiary = Violet,
    background = Grey12,
    surface = Indigo,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    primaryContainer = Green,
    secondaryContainer = Blue,
    tertiaryContainer = Orange,
    surfaceContainer = Yellow,
    surfaceContainerHigh = Red,
    surfaceContainerHighest = Orange,
    outline = Green,
    outlineVariant = Blue
)

private val LightColorScheme = lightColorScheme(
    primary = Red,
    secondary = CornflowerBlue,
    tertiary = Violet,
    background = Grey99,
    surface = Indigo,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    primaryContainer = Green,
    secondaryContainer = Blue,
    tertiaryContainer = Orange,
    surfaceContainer = Yellow,
    surfaceContainerHigh = Red,
    surfaceContainerHighest = Orange,
    outline = Green,
    outlineVariant = Blue
)

// Ocean Theme
private val DarkOceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = OceanTertiary,
    background = Grey12,
    surface = OceanPrimary,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    surfaceContainer = OceanPrimary,
    surfaceContainerHigh = OceanSecondary,
    surfaceContainerHighest = OceanPrimary,
    outline = OceanPrimary,
    outlineVariant = OceanSecondary
)

private val LightOceanColorScheme = lightColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = OceanTertiary,
    background = Grey99,
    surface = LightBlue,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    surfaceContainer = LightBlue,
    surfaceContainerHigh = LightGreen,
    surfaceContainerHighest = OceanPrimary,
    outline = OceanSecondary,
    outlineVariant = OceanPrimary
)

// Forest Theme
private val DarkForestColorScheme = darkColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = Grey12,
    surface = ForestPrimary,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    surfaceContainer = ForestPrimary,
    surfaceContainerHigh = ForestSecondary,
    surfaceContainerHighest = ForestPrimary,
    outline = ForestPrimary,
    outlineVariant = ForestSecondary
)

private val LightForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = Grey99,
    surface = LightGreen,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    surfaceContainer = LightGreen,
    surfaceContainerHigh = LightBlue,
    surfaceContainerHighest = ForestPrimary,
    outline = ForestSecondary,
    outlineVariant = ForestPrimary
)

// Night Theme
private val DarkNightColorScheme = darkColorScheme(
    primary = NightPrimary,
    secondary = NightSecondary,
    tertiary = NightTertiary,
    background = Grey12,
    surface = NightPrimary,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    surfaceContainer = NightPrimary,
    surfaceContainerHigh = NightSecondary,
    surfaceContainerHighest = NightPrimary,
    outline = NightPrimary,
    outlineVariant = NightSecondary
)

private val LightNightColorScheme = lightColorScheme(
    primary = NightPrimary,
    secondary = NightSecondary,
    tertiary = NightTertiary,
    background = Grey99,
    surface = LightNightPrimary,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    surfaceContainer = LightNightPrimary,
    surfaceContainerHigh = LightNightSecondary,
    surfaceContainerHighest = NightPrimary,
    outline = NightSecondary,
    outlineVariant = NightPrimary
)

// Cosmos Theme
private val DarkCosmosColorScheme = darkColorScheme(
    primary = CosmosPrimary,
    secondary = CosmosSecondary,
    tertiary = CosmosTertiary,
    background = Grey12,
    surface = CosmosPrimary,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    surfaceContainer = CosmosPrimary,
    surfaceContainerHigh = CosmosSecondary,
    surfaceContainerHighest = CosmosPrimary,
    outline = CosmosPrimary,
    outlineVariant = CosmosSecondary
)

private val LightCosmosColorScheme = lightColorScheme(
    primary = CosmosPrimary,
    secondary = CosmosSecondary,
    tertiary = CosmosTertiary,
    background = Grey99,
    surface = LightCosmosPrimary,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    surfaceContainer = LightCosmosPrimary,
    surfaceContainerHigh = LightCosmosSecondary,
    surfaceContainerHighest = CosmosPrimary,
    outline = CosmosSecondary,
    outlineVariant = CosmosPrimary
)

// Fire Theme
private val DarkFireColorScheme = darkColorScheme(
    primary = FirePrimary,
    secondary = FireSecondary,
    tertiary = FireTertiary,
    background = Grey12,
    surface = FirePrimary,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    surfaceContainer = FirePrimary,
    surfaceContainerHigh = FireSecondary,
    surfaceContainerHighest = FirePrimary,
    outline = FirePrimary,
    outlineVariant = FireSecondary
)

private val LightFireColorScheme = lightColorScheme(
    primary = FirePrimary,
    secondary = FireSecondary,
    tertiary = FireTertiary,
    background = Grey99,
    surface = LightFirePrimary,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
    surfaceContainer = LightFirePrimary,
    surfaceContainerHigh = LightFireSecondary,
    surfaceContainerHighest = FirePrimary,
    outline = FireSecondary,
    outlineVariant = FirePrimary
)

@Composable
fun DatabaserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    theme: String = "Default",
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> {
            when (theme) {
                "Ocean" -> DarkOceanColorScheme
                "Forest" -> DarkForestColorScheme
                "Night" -> DarkNightColorScheme
                "Cosmos" -> DarkCosmosColorScheme
                "Fire" -> DarkFireColorScheme
                "Rainbow" -> DarkRainbowColorScheme
                else -> DarkColorScheme
            }
        }

        else -> {
            when (theme) {
                "Ocean" -> LightOceanColorScheme
                "Forest" -> LightForestColorScheme
                "Night" -> LightNightColorScheme
                "Cosmos" -> LightCosmosColorScheme
                "Fire" -> LightFireColorScheme
                "Rainbow" -> LightRainbowColorScheme
                else -> LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
