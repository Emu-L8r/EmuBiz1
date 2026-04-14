package com.emul8r.bizap.ui.gui3.util
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun rememberResponsiveConfig(): ResponsiveConfig {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    return ResponsiveConfig(
        isTablet = isTablet, screenWidthDp = screenWidthDp,
        paddingSmall = if (isTablet) 24.dp else 12.dp,
        paddingMedium = if (isTablet) 32.dp else 16.dp,
        paddingLarge = if (isTablet) 40.dp else 24.dp,
        spacingSmall = if (isTablet) 16.dp else 8.dp,
        spacingMedium = if (isTablet) 20.dp else 12.dp,
        spacingLarge = if (isTablet) 32.dp else 20.dp,
        titleSize = if (isTablet) 28.sp else 24.sp,
        headlineSize = if (isTablet) 24.sp else 20.sp,
        bodySize = if (isTablet) 18.sp else 16.sp,
        labelSize = if (isTablet) 16.sp else 14.sp,
        buttonHeight = if (isTablet) 56.dp else 48.dp,
        cardHeight = if (isTablet) 140.dp else 120.dp,
        columnCount = if (isTablet) 2 else 1,
        iconSize = if (isTablet) 32.dp else 24.dp,
    )
}
data class ResponsiveConfig(val isTablet: Boolean, val screenWidthDp: Int, val paddingSmall: Dp, val paddingMedium: Dp, val paddingLarge: Dp, val spacingSmall: Dp, val spacingMedium: Dp, val spacingLarge: Dp, val titleSize: TextUnit, val headlineSize: TextUnit, val bodySize: TextUnit, val labelSize: TextUnit, val buttonHeight: Dp, val cardHeight: Dp, val columnCount: Int, val iconSize: Dp)
