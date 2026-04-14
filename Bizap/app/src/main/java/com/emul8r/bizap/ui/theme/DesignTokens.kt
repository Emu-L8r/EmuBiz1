package com.emul8r.bizap.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ============================================================================
 * COMPREHENSIVE DESIGN TOKENS FOR APP-WIDE CONSISTENCY
 * ============================================================================
 * This is the source of truth for all visual styling in the Bizap app.
 * Use these tokens consistently across all screens to ensure professional,
 * polished appearance and easy theme switching.
 */

// ============================================================================
// SPACING SYSTEM - Foundation for all layout
// ============================================================================
object Spacing {
    val xs = 4.dp      // Tight spacing, minimal gaps
    val sm = 8.dp      // Small spacing, adjacent elements
    val md = 12.dp     // Medium spacing, default separation
    val lg = 16.dp     // Large spacing, normal padding
    val xl = 24.dp     // Extra large, section separation
    val xxl = 32.dp    // XXL, major section breaks
    val xxxl = 48.dp   // XXXL, page-level spacing
}

// ============================================================================
// TYPOGRAPHY SCALE - Consistent text hierarchy
// ============================================================================
/**
 * Text sizes and styles for different contexts.
 * These work with Material 3 typography but provide semantic naming.
 */
object TextSizes {
    val h1 = 32.sp     // Page titles, main headings (ExtraBold)
    val h2 = 28.sp     // Section headers (Bold)
    val h3 = 24.sp     // Subsection headers (SemiBold)
    val h4 = 20.sp     // Card titles (SemiBold)
    val bodyLarge = 16.sp    // Primary body text
    val bodyMedium = 14.sp   // Default body text
    val bodySmall = 12.sp    // Secondary text
    val labelLarge = 14.sp   // Labels, emphasis
    val labelMedium = 12.sp  // Field labels, descriptions
    val labelSmall = 10.sp   // Helper text, captions
    val captionSmall = 10.sp // Smallest text, metadata
}

object LineHeights {
    val tight = 20.sp   // Compact text spacing
    val normal = 24.sp  // Default comfortable reading
    val relaxed = 28.sp // Generous spacing for body text
}

// ============================================================================
// DIMENSIONS - Sizes and radius
// ============================================================================
object Dimensions {
    // Color preview/selector sizes
    val colorPreviewHeight = 50.dp
    val colorPreviewRadius = 6.dp

    // Card and container radius
    val cardRadius = 12.dp
    val cardRadiusSmall = 8.dp
    val cardRadiusLarge = 16.dp

    // Spacing between preset items
    val presetSpacing = 12.dp
    val presetRowSpacing = 12.dp

    // Icon sizes - use consistently
    val iconSizeSmall = 18.dp    // Small inline icons
    val iconSizeMedium = 24.dp   // Standard icons
    val iconSizeLarge = 32.dp    // Large section icons
    val iconSizeXLarge = 48.dp   // Page-level icons

    // Border widths
    val borderWidthDefault = 1.dp
    val borderWidthFocus = 2.dp
    val borderWidthSelected = 3.dp

    // Button heights - for accessibility
    val buttonHeightSmall = 40.dp
    val buttonHeightMedium = 48.dp
    val buttonHeightLarge = 56.dp

    // Text field heights
    val textFieldHeight = 56.dp
    val textFieldHeightCompact = 48.dp

    // Progress indicator
    val progressIndicatorSize = 18.dp
    val progressIndicatorStroke = 2.dp

    // Elevation/Shadow heights
    val elevationNone = 0.dp
    val elevationLow = 2.dp
    val elevationMedium = 4.dp
    val elevationHigh = 8.dp
    val elevationXHigh = 16.dp
}

// ============================================================================
// FORM FIELD CONSTANTS - Consistent form styling
// ============================================================================
object FormDefaults {
    val fieldSpacing = Spacing.lg              // Space between fields
    val sectionSpacing = Spacing.xl            // Space between sections
    val labelToFieldSpacing = Spacing.sm       // Space from label to input
    val fieldToHelperSpacing = Spacing.xs      // Space from field to helper text

    val fieldMinHeight = Dimensions.textFieldHeight
    val fieldPadding = Spacing.md

    val borderRadius = Dimensions.cardRadiusSmall
}

// ============================================================================
// SECTION HEADER CONSTANTS - Consistent section styling
// ============================================================================
object SectionDefaults {
    val headerHeight = 56.dp
    val headerIconSize = Dimensions.iconSizeMedium
    val headerSpacing = Spacing.md
    val dividerThickness = 1.dp
}

// ============================================================================
// CARD CONSTANTS - Consistent card styling
// ============================================================================
object CardDefaults {
    val defaultElevation = Dimensions.elevationMedium
    val defaultRadius = Dimensions.cardRadius
    val defaultPadding = Spacing.lg
}

// ============================================================================
// TYPOGRAPHY TOKENS - Font sizes and styles for specific components
// ============================================================================
object TypographyTokens {
    // Page/Screen Titles
    val pageTitleSize = TextSizes.h1
    val pageTitleLineHeight = LineHeights.normal

    // Section Headers
    val sectionHeaderSize = TextSizes.h2
    val sectionHeaderLineHeight = LineHeights.normal

    // Form Labels
    val formLabelSize = TextSizes.labelMedium
    val formLabelLineHeight = LineHeights.tight

    // Helper Text
    val helperTextSize = TextSizes.labelSmall
    val helperTextLineHeight = LineHeights.tight

    // Body Text
    val bodyTextSize = TextSizes.bodyMedium
    val bodyTextLineHeight = LineHeights.normal
}

// ============================================================================
// ANIMATION/TRANSITION TOKENS - Consistent motion
// ============================================================================
object TransitionTokens {
    // Standard durations (in milliseconds)
    val durationShort = 150      // Quick feedback, micro-interactions
    val durationMedium = 300     // Standard transitions
    val durationLong = 500       // Deliberate, noticeable transitions

    // Easing curves are handled by Compose, these are durations only
}

// ============================================================================
// CONTRAST & ACCESSIBILITY - Ensure readability
// ============================================================================
/**
 * Opacity values for semantic meaning and accessibility.
 * Use these to ensure sufficient contrast ratios (WCAG AA or better).
 */
object OpacityTokens {
    val full = 1.0f              // Full opacity
    val highEmphasis = 0.95f     // Nearly full, primary content
    val mediumEmphasis = 0.75f   // Secondary content
    val lowEmphasis = 0.55f      // Tertiary/disabled content
    val disabled = 0.38f         // Disabled state
}

// ============================================================================
// STATUS COLORS - Semantic meaning
// ============================================================================
/**
 * These are used through the theme system, but grouped here for reference.
 * Success: Positive actions, valid states
 * Error: Invalid states, errors
 * Warning: Cautions, pending actions
 * Info: Informational messages
 */
object StatusColorDefaults {
    val successColor = 0xFF22c55e     // Green - Success/Valid
    val errorColor = 0xFFef4444       // Red - Error/Invalid
    val warningColor = 0xFFfbbf24     // Amber - Warning/Pending
    val infoColor = 0xFF0ea5e9        // Blue - Info/Informational
}

