package com.emul8r.bizap.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Design tokens for consistent spacing, sizing, and layout throughout the app.
 * These tokens ensure visual consistency and make it easy to adjust spacing globally.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/**
 * Dimension tokens for common component sizes.
 */
object Dimensions {
    // Color preview/selector sizes
    val colorPreviewHeight = 50.dp
    val colorPreviewRadius = 6.dp

    // Card dimensions
    val cardRadius = 12.dp
    val cardRadiusSmall = 8.dp

    // Spacing between preset items
    val presetSpacing = 12.dp
    val presetRowSpacing = 12.dp

    // Icon sizes
    val iconSizeSmall = 18.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 32.dp
    val iconSizeXLarge = 48.dp

    // Border widths
    val borderWidthDefault = 1.dp
    val borderWidthSelected = 3.dp

    // Progress indicator
    val progressIndicatorSize = 18.dp
    val progressIndicatorStroke = 2.dp
}

/**
 * Typography scale tokens for consistency.
 * Note: Primary typography is managed through MaterialTheme.typography
 * These are supplementary tokens for specific use cases.
 */
object TypographyTokens {
    // Font sizes are managed through Material 3 typography
    // Use MaterialTheme.typography.labelSmall, labelMedium, etc.
}

/**
 * Animation/Transition tokens for consistent motion.
 */
object TransitionTokens {
    // Standard durations
    val durationShort = 150
    val durationMedium = 300
    val durationLong = 500
}

