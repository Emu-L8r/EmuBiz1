package com.emul8r.bizap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Unified Design System for Invoice Creation Pages
 *
 * **Purpose:**
 * Provides design tokens (spacing, typography, colors) for consistent styling
 * across invoice creation screens (both GUI1 and GUI2).
 *
 * **Benefits:**
 * - Single source of truth for design values
 * - Easy to maintain and update styles globally
 * - Consistent spacing and alignment
 * - Professional appearance on all screen sizes
 * - Follows Material Design 3 principles
 *
 * **Usage:**
 * ```kotlin
 * Column(
 *     modifier = Modifier.padding(InvoiceCreationTheme.Spacing.containerPadding),
 *     verticalArrangement = Arrangement.spacedBy(InvoiceCreationTheme.Spacing.sectionGap)
 * ) {
 *     Text("Customer", style = InvoiceCreationTheme.Typography.sectionLabel)
 *     // ... form content
 * }
 * ```
 *
 * **Hierarchy:**
 * - **Container Level:** Full-screen padding and gaps between major sections
 * - **Section Level:** Padding within form sections and between form groups
 * - **Component Level:** Spacing within individual form fields and buttons
 * - **Text Level:** Typography for labels, inputs, and descriptions
 */
object InvoiceCreationTheme {

    /**
     * Spacing System
     *
     * Controls padding and margins throughout the form.
     * Based on Material Design 3 scale (4dp base unit).
     */
    object Spacing {
        // Container-level spacing (outermost padding)
        val containerPadding = 16.dp      // Standard screen padding
        val containerPaddingLarge = 24.dp // Tablet/landscape padding

        // Section-level spacing (between major form sections)
        val sectionGap = 20.dp            // Gap between sections (customer, items, customization)
        val sectionPadding = 16.dp        // Padding within sections

        // Component-level spacing (within form fields)
        val componentGap = 12.dp          // Gap between form fields
        val componentPadding = 12.dp      // Padding within components

        // Small spacing (tight grouping)
        val smallGap = 8.dp               // Gap for tightly grouped items
        val smallPadding = 8.dp           // Tight padding

        // Icon and avatar spacing
        val iconSize = 24.dp              // Standard icon size
        val avatarSize = 40.dp            // Avatar/profile picture size

        // Dialog and overlay spacing
        val modalPadding = 24.dp          // Padding inside dialogs/modals
        val modalGap = 16.dp              // Gap between modal elements
    }

    /**
     * Divider and Border System
     *
     * Subtle visual separators between sections.
     */
    object Dividers {
        val thickness = 1.dp              // Standard divider thickness
        val thicknessBold = 2.dp          // Emphasized divider
    }

    /**
     * Radius System
     *
     * Corner rounding for consistent modern look.
     */
    object Radius {
        val small = 4.dp                  // Buttons, small components
        val medium = 8.dp                 // Cards, fields
        val large = 12.dp                 // Dialogs, bottom sheets
    }

    /**
     * Form Field Styling
     *
     * Standards for text inputs and other form controls.
     */
    object FormFields {
        val height = 56.dp                // Standard field height (Material Design)
        val minHeight = 48.dp             // Minimum touch target
        val horizontalPadding = 12.dp     // Inside field padding
        val cornerRadius = Radius.medium
    }

    /**
     * Card and Surface Styling
     *
     * Standards for cards, sections, and elevated surfaces.
     */
    object Surfaces {
        val cornerRadius = Radius.medium
        val padding = Spacing.sectionPadding
        val gap = Spacing.componentGap
        val elevation = 2.dp              // Subtle shadow
        val elevationHigh = 8.dp          // Prominent shadow (dialogs)
    }
}

/**
 * Extension to access Material Design 3 colors with InvoiceCreationTheme context.
 *
 * Example:
 * ```kotlin
 * Box(
 *     modifier = Modifier.background(
 *         MaterialTheme.colorScheme.surfaceVariant,
 *         RoundedCornerShape(InvoiceCreationTheme.Radius.medium)
 *     )
 * )
 * ```
 */
@Composable
fun rememberInvoiceCreationColors() = InvoiceCreationColors(
    surfaceBackground = MaterialTheme.colorScheme.surface,
    surfaceVariant = MaterialTheme.colorScheme.surfaceVariant,
    onSurface = MaterialTheme.colorScheme.onSurface,
    onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
    outline = MaterialTheme.colorScheme.outline,
    outlineVariant = MaterialTheme.colorScheme.outlineVariant,
    primary = MaterialTheme.colorScheme.primary,
    primaryContainer = MaterialTheme.colorScheme.primaryContainer,
    secondary = MaterialTheme.colorScheme.secondary,
    error = MaterialTheme.colorScheme.error
)

/**
 * Color scheme for invoice creation screens.
 * Derived from Material Design 3 theme.
 */
data class InvoiceCreationColors(
    val surfaceBackground: androidx.compose.ui.graphics.Color,
    val surfaceVariant: androidx.compose.ui.graphics.Color,
    val onSurface: androidx.compose.ui.graphics.Color,
    val onSurfaceVariant: androidx.compose.ui.graphics.Color,
    val outline: androidx.compose.ui.graphics.Color,
    val outlineVariant: androidx.compose.ui.graphics.Color,
    val primary: androidx.compose.ui.graphics.Color,
    val primaryContainer: androidx.compose.ui.graphics.Color,
    val secondary: androidx.compose.ui.graphics.Color,
    val error: androidx.compose.ui.graphics.Color
)

