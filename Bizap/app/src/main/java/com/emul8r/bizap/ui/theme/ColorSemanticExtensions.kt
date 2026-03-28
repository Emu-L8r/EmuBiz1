package com.emul8r.bizap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color extensions for status-based coloring.
 *
 * Maps business logic statuses to Material 3 color roles.
 * This ensures colors work in both light & dark modes automatically.
 *
 * USAGE:
 * Text(color = ColorSemantics.statusPaid())  // ✅ Theme-aware
 * Box(background = ColorSemantics.statusOverdue())  // ✅ Dark mode safe
 *
 * Benefits:
 * - Automatic light/dark mode support
 * - Single source of truth (changed in theme, updates everywhere)
 * - Consistent with Material 3 design system
 */
object ColorSemantics {

    /**
     * Paid status color (green/success)
     * Mapped to tertiary color role for visibility
     */
    @Composable
    fun statusPaid(): Color = MaterialTheme.colorScheme.tertiary

    /**
     * Paid status background color (light green)
     */
    @Composable
    fun statusPaidContainer(): Color = MaterialTheme.colorScheme.tertiaryContainer

    /**
     * Overdue status color (red/error)
     * Mapped to error color role for warning
     */
    @Composable
    fun statusOverdue(): Color = MaterialTheme.colorScheme.error

    /**
     * Overdue status background color (light red)
     */
    @Composable
    fun statusOverdueContainer(): Color = MaterialTheme.colorScheme.errorContainer

    /**
     * Sent/Active status color (primary)
     */
    @Composable
    fun statusSent(): Color = MaterialTheme.colorScheme.primary

    /**
     * Sent/Active status background color
     */
    @Composable
    fun statusSentContainer(): Color = MaterialTheme.colorScheme.primaryContainer

    /**
     * Partially paid status (secondary)
     */
    @Composable
    fun statusPartial(): Color = MaterialTheme.colorScheme.secondary

    /**
     * Partially paid background
     */
    @Composable
    fun statusPartialContainer(): Color = MaterialTheme.colorScheme.secondaryContainer
}

