package com.emul8r.bizap.ui.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Centralized color definitions for Bizap app.
 *
 * This object defines all colors used throughout the app, ensuring consistency
 * and making theme changes easy. Colors are organized by their semantic purpose.
 */
object BizapColors {

    // ═══════════════════════════════════════════════════════════════════════════
    // STATUS COLORS
    // ═══════════════════════════════════════════════════════════════════════════

    val StatusPaid = Color(0xFF4CAF50)           // Green - Payment received
    val StatusSent = Color(0xFF2196F3)           // Blue - Invoice sent
    val StatusDraft = Color(0xFF999999)          // Gray - Not yet sent
    val StatusOverdue = Color(0xFFB3261E)        // Red - Past due date
    val StatusPartiallyPaid = Color(0xFFFFA500)  // Orange - Partial payment
    val StatusOutstanding = Color(0xFFFFA500)    // Orange - Expected income

    // Darker variants for text on light backgrounds
    val StatusPaidDark = Color(0xFF2E7D32)
    val StatusSentDark = Color(0xFF1565C0)
    val StatusDraftDark = Color(0xFF666666)
    val StatusOverdueDark = Color(0xFF8B0000)
    val StatusPartiallyPaidDark = Color(0xFFCC7700)

    // Light variants for backgrounds
    val StatusPaidLight = Color(0xFFE8F5E9)
    val StatusSentLight = Color(0xFFE3F2FD)
    val StatusDraftLight = Color(0xFFF5F5F5)
    val StatusOverdueLight = Color(0xFFFFEBEE)
    val StatusPartiallyPaidLight = Color(0xFFFFF3E0)

    // ═══════════════════════════════════════════════════════════════════════════
    // ANALYTICS COLORS
    // ═══════════════════════════════════════════════════════════════════════════

    val AnalyticsExcellent = Color(0xFF4CAF50)   // ≥ 90% - Green
    val AnalyticsGood = Color(0xFF8BC34A)        // 70-89% - Light Green
    val AnalyticsWarning = Color(0xFFFFC107)     // 50-69% - Amber
    val AnalyticsAtRisk = Color(0xFFFF5722)      // < 50% - Red-Orange

    // ═══════════════════════════════════════════════════════════════════════════
    // ACTION COLORS
    // ═══════════════════════════════════════════════════════════════════════════

    val ActionCreate = Color(0xFF4CAF50)          // Green - Create/Add action
    val ActionEdit = Color(0xFF2196F3)            // Blue - Edit action
    val ActionDelete = Color(0xFFF44336)          // Red - Delete action
    val ActionArchive = Color(0xFF9C27B0)         // Purple - Archive action
    val ActionExport = Color(0xFF00BCD4)          // Cyan - Export action

    // ═══════════════════════════════════════════════════════════════════════════
    // SEMANTIC COLORS (Theme-aware, should use Material3 instead)
    // ═══════════════════════════════════════════════════════════════════════════

    val Success = Color(0xFF4CAF50)               // Success feedback
    val Error = Color(0xFFF44336)                 // Error feedback
    val Warning = Color(0xFFFFC107)               // Warning feedback
    val Info = Color(0xFF2196F3)                  // Informational feedback

    // ═══════════════════════════════════════════════════════════════════════════
    // NEUTRAL COLORS
    // ═══════════════════════════════════════════════════════════════════════════

    val DarkGray = Color(0xFF424242)
    val MediumGray = Color(0xFF757575)
    val LightGray = Color(0xFFBDBDBD)
    val VeryLightGray = Color(0xFFEEEEEE)

    // ═══════════════════════════════════════════════════════════════════════════
    // PRESET THEME COLORS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Pre-defined theme seed colors for quick theme selection.
     * Each can be used with Material3 dynamic theming.
     */
    object Presets {
        val Blue = Color(0xFF2196F3)              // Material Blue
        val Purple = Color(0xFF9C27B0)            // Material Purple
        val Pink = Color(0xFFE91E63)              // Material Pink
        val Red = Color(0xFFF44336)               // Material Red
        val Orange = Color(0xFFFF9800)            // Material Orange
        val Green = Color(0xFF4CAF50)             // Material Green
        val Teal = Color(0xFF009688)              // Material Teal
        val Indigo = Color(0xFF3F51B5)            // Material Indigo
        val DeepOrange = Color(0xFFFF5722)        // Material Deep Orange
        val Lime = Color(0xFFCDDC39)              // Material Lime
        val Cyan = Color(0xFF00BCD4)              // Material Cyan
        val BlueGrey = Color(0xFF607D8B)          // Material Blue Grey
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY FUNCTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get all preset colors as a list for iteration.
     */
    fun getPresetsList(): List<Pair<String, Color>> = listOf(
        "Blue" to Presets.Blue,
        "Purple" to Presets.Purple,
        "Pink" to Presets.Pink,
        "Red" to Presets.Red,
        "Orange" to Presets.Orange,
        "Green" to Presets.Green,
        "Teal" to Presets.Teal,
        "Indigo" to Presets.Indigo,
        "Deep Orange" to Presets.DeepOrange,
        "Lime" to Presets.Lime,
        "Cyan" to Presets.Cyan,
        "Blue Grey" to Presets.BlueGrey
    )
}

