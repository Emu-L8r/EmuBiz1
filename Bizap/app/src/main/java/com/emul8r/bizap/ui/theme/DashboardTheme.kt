package com.emul8r.bizap.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralised styling tokens for all dashboard components.
 *
 * Single source of truth for dashboard spacing, borders, corner radii and
 * elevation — eliminates scattered inline magic numbers throughout
 * DashboardScreen and its child composables.
 */
object DashboardTheme {

    // -------------------------------------------------------------------------
    // Spacing
    // -------------------------------------------------------------------------

    /** Horizontal/vertical gap between sibling cards. */
    val cardSpacing: Dp = 12.dp

    /** Internal padding inside every card. */
    val cardPadding: Dp = 16.dp

    /** Standard screen-edge padding. */
    val screenPadding: Dp = 16.dp

    /** Vertical gap between major sections. */
    val sectionSpacing: Dp = 16.dp

    // -------------------------------------------------------------------------
    // Shape
    // -------------------------------------------------------------------------

    /** Unified corner radius for all dashboard cards. */
    val cornerRadius: Dp = 12.dp

    val cardShape = RoundedCornerShape(cornerRadius)

    // -------------------------------------------------------------------------
    // Border
    // -------------------------------------------------------------------------

    /** Stroke width for card borders. */
    val borderWidth: Dp = 2.dp

    /** Default alpha applied to the accent color for the card background tint. */
    const val backgroundAlpha: Float = 0.08f

    /** Default alpha applied to the accent color for the border stroke. */
    const val borderAlpha: Float = 0.3f

    // -------------------------------------------------------------------------
    // Elevation
    // -------------------------------------------------------------------------

    /** Default card elevation — shared across all dashboard cards. */
    val cardElevation: Dp = 2.dp

    // -------------------------------------------------------------------------
    // Icon
    // -------------------------------------------------------------------------

    /** Icon size used in metric cards. */
    val metricIconSize: Dp = 32.dp

    // -------------------------------------------------------------------------
    // Factory helpers
    // -------------------------------------------------------------------------

    /**
     * Returns a [BorderStroke] consistent with dashboard styling for the given
     * [accentColor].
     */
    fun borderStroke(accentColor: Color): BorderStroke =
        BorderStroke(borderWidth, accentColor.copy(alpha = borderAlpha))

    /**
     * Returns the card container color for the given [accentColor].
     */
    fun containerColor(accentColor: Color): Color =
        accentColor.copy(alpha = backgroundAlpha)
}

// -----------------------------------------------------------------------------
// Modifier extensions
// -----------------------------------------------------------------------------

/**
 * Applies the standard dashboard card elevation via [CardDefaults].
 *
 * Usage:
 * ```kotlin
 * Card(elevation = dashboardCardElevation()) { … }
 * ```
 */
@Composable
fun dashboardCardElevation() = CardDefaults.cardElevation(
    defaultElevation = DashboardTheme.cardElevation
)
