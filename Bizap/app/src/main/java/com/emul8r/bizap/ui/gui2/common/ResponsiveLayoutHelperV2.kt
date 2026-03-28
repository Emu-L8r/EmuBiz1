package com.emul8r.bizap.ui.gui2.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*

/**
 * Screen size categories for responsive design.
 */
enum class ScreenSize {
    PHONE,      // < 600dp
    TABLET,     // 600-840dp
    DESKTOP     // > 840dp
}

/**
 * Helper object for responsive layouts.
 *
 * Provides:
 * - Screen size detection
 * - Responsive padding/spacing
 * - Adaptive layouts
 */
object ResponsiveLayoutHelperV2 {

    /**
     * Get current screen size.
     */
    @Composable
    fun getScreenSize(): ScreenSize {
        val widthDp = LocalConfiguration.current.screenWidthDp
        return when {
            widthDp < 600 -> ScreenSize.PHONE
            widthDp < 840 -> ScreenSize.TABLET
            else -> ScreenSize.DESKTOP
        }
    }

    /**
     * Get responsive horizontal padding.
     */
    @Composable
    fun getHorizontalPadding(): Dp {
        return when (getScreenSize()) {
            ScreenSize.PHONE -> 12.dp
            ScreenSize.TABLET -> 16.dp
            ScreenSize.DESKTOP -> 24.dp
        }
    }

    /**
     * Get responsive vertical padding.
     */
    @Composable
    fun getVerticalPadding(): Dp {
        return when (getScreenSize()) {
            ScreenSize.PHONE -> 8.dp
            ScreenSize.TABLET -> 12.dp
            ScreenSize.DESKTOP -> 16.dp
        }
    }

    /**
     * Get responsive card spacing.
     */
    @Composable
    fun getCardSpacing(): Dp {
        return when (getScreenSize()) {
            ScreenSize.PHONE -> 8.dp
            ScreenSize.TABLET -> 12.dp
            ScreenSize.DESKTOP -> 16.dp
        }
    }

    /**
     * Responsive column with adaptive padding.
     */
    @Composable
    fun ResponsiveColumn(
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = getHorizontalPadding(),
                    vertical = getVerticalPadding()
                )
        ) {
            content()
        }
    }

    /**
     * Responsive row with adaptive spacing.
     */
    @Composable
    fun ResponsiveRow(
        modifier: Modifier = Modifier,
        content: @Composable RowScope.() -> Unit
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = getHorizontalPadding(),
                    vertical = getVerticalPadding()
                ),
            horizontalArrangement = Arrangement.spacedBy(getCardSpacing())
        ) {
            content()
        }
    }

    /**
     * Check if device is in portrait mode.
     */
    @Composable
    fun isPortraitMode(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenHeightDp > configuration.screenWidthDp
    }

    /**
     * Get max width for content (useful for tablets).
     */
    @Composable
    fun getMaxContentWidth(): Dp {
        return when (getScreenSize()) {
            ScreenSize.PHONE -> 600.dp
            ScreenSize.TABLET -> 840.dp
            ScreenSize.DESKTOP -> 1200.dp
        }
    }
}

/**
 * Connection status enum for offline support.
 */
enum class ConnectionStatus {
    ONLINE,
    OFFLINE,
    RECONNECTING
}

