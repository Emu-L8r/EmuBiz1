package com.emul8r.bizap.data.service.pdf_services

import com.emul8r.bizap.domain.model.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 3B: Dynamic CSS Generation Engine
 *
 * Generates CSS at runtime based on user settings (ColorScheme, SpacingProfile, VisualAccents).
 * Replaces static CSS files with dynamic generation, enabling settings to automatically cascade.
 *
 * Architecture:
 * - Phase 1 Improvements: Page breaks, footer positioning, spacing
 * - Quality Layer: Information hierarchy, table readability, footer emphasis
 * - Color Overrides: ColorScheme applied to base template
 * - Spacing Multipliers: SpacingProfile adjusts padding/margins
 * - Visual Accents: Toggles for borders, shadows, dividers, highlights, gradients
 */
@Singleton
class CssGenerator @Inject constructor() {

    /**
     * Generate complete CSS based on settings.
     * Combines base template, quality improvements, and user customizations.
     */
    fun generateCss(
        colorScheme: ColorScheme = ColorScheme.PROFESSIONAL,
        spacingProfile: SpacingProfile = SpacingProfile.NORMAL,
        visualAccents: VisualAccents = VisualAccents.default(),
        totalBoxStyle: TotalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
        enableAlternatingRows: Boolean = true,
        enableDividers: Boolean = true,
        enableGradientHeader: Boolean = true
    ): String {
        return buildString {
            // Phase 1: Core page layout improvements
            appendLine(generatePhase1Improvements())
            appendLine()

            // Phase 2: Design quality layer
            appendLine(generateQualityImprovements(totalBoxStyle))
            appendLine()

            // Color scheme overrides
            appendLine(generateColorOverrides(colorScheme))
            appendLine()

            // Spacing profile multipliers
            appendLine(generateSpacingOverrides(spacingProfile))
            appendLine()

            // Visual accents (toggles)
            appendLine(generateVisualAccentsCss(
                enableBorders = visualAccents.showBorders,
                enableShadows = visualAccents.showShadows,
                enableDividers = enableDividers && visualAccents.showDividers,
                enableHighlight = visualAccents.highlightTotals,
                enableGradients = visualAccents.useGradients && enableGradientHeader
            ))
            appendLine()

            // Table styling
            appendLine(generateTableStyling(
                enableAlternatingRows = enableAlternatingRows,
                colorScheme = colorScheme
            ))
        }
    }

    /**
     * Phase 1: Page breaks, footer positioning, spacing control.
     * Eliminates overlaying issues from default HTML-to-PDF.
     */
    private fun generatePhase1Improvements(): String = """
        /* ═══════════════════════════════════════════════════════════════ */
        /* PHASE 1: PAGE LAYOUT & BREAK CONTROL                            */
        /* ═══════════════════════════════════════════════════════════════ */

        * { box-sizing: border-box; }
        html, body { margin: 0; padding: 0; }

        .page {
            page-break-after: always;
            min-height: 11in;  /* A4 page height */
            display: flex;
            flex-direction: column;
            padding: 0.75in;
            background: white;
        }

        .invoice-container {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        /* Prevent sections from splitting across pages */
        .section,
        .invoice-header,
        .details-section,
        .items-table,
        .totals-section,
        .payment-section,
        .notes-section,
        table {
            page-break-inside: avoid;
        }

        /* Push footer to bottom of page */
        .footer {
            margin-top: auto;
            padding-top: 15px;
            border-top: 1px solid #ddd;
            text-align: center;
            font-size: 9pt;
            color: #666;
        }

        /* Table reset */
        table {
            border-collapse: collapse;
            width: 100%;
        }
        td, th {
            word-wrap: break-word;
            overflow-wrap: break-word;
        }
    """.trimIndent()

    /**
     * Phase 2: Design quality improvements for visual hierarchy, spacing, readability.
     */
    private fun generateQualityImprovements(totalBoxStyle: TotalBoxStyle): String = """
        /* ═══════════════════════════════════════════════════════════════ */
        /* PHASE 2: DESIGN QUALITY LAYER                                   */
        /* ═══════════════════════════════════════════════════════════════ */

        /* Issue #1: Information Hierarchy */
        .invoice-header {
            margin-bottom: 24px;
            padding-bottom: 10px;
        }

        .company-info h1 {
            margin: 0 0 8px 0;
            font-size: 20pt;
        }

        .company-info p {
            margin: 2px 0;
            font-size: 9pt;
        }

        .invoice-title {
            text-align: right;
            page-break-inside: avoid;
        }

        .invoice-title h2 {
            margin: 0;
            font-size: 28pt;
            font-weight: bold;
        }

        .invoice-title p {
            margin: 2px 0 0 0;
            font-size: 10pt;
            color: #666;
        }

        /* Bill To: Smaller, supporting info */
        .bill-to h3 {
            font-size: 10pt;
            color: #666;
            margin: 0 0 8px 0;
            font-weight: bold;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .bill-to p {
            margin: 3px 0;
            font-size: 9.5pt;
            line-height: 1.4;
        }

        /* Invoice Details: Larger, primary info */
        .invoice-details h3 {
            font-size: 10pt;
            color: #333;
            margin: 0 0 8px 0;
            font-weight: bold;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .invoice-details p {
            margin: 4px 0;
            font-size: 9.5pt;
            line-height: 1.5;
        }

        /* Issue #2: Vertical Spacing */
        .details-section {
            margin: 20px 0 25px 0;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            page-break-inside: avoid;
        }

        /* Issue #3: Table Readability */
        .items-table {
            width: 100%;
            margin: 25px 0;
            border-collapse: collapse;
        }

        .items-table thead {
            page-break-inside: avoid;
            background-color: #f9f9f9;
        }

        .items-table th {
            background-color: #f9f9f9;
            font-weight: bold;
            font-size: 9.5pt;
            padding: 10px 12px;
            text-align: left;
            border-bottom: 2px solid #ddd;
        }

        .items-table th.align-right {
            text-align: right;
        }

        .items-table th.align-center {
            text-align: center;
        }

        .items-table tbody tr {
            page-break-inside: avoid;
            height: 28px;  /* Increased from 20px for better readability */
        }

        .items-table td {
            padding: 10px 12px;
            font-size: 9.5pt;
            vertical-align: middle;
            border-bottom: 1px solid #e8e8e8;
        }

        .items-table td.align-right {
            text-align: right;
        }

        .items-table td.align-center {
            text-align: center;
        }

        /* Issue #4: Totals Section Emphasis */
        .totals-section {
            margin: 30px 0;
            page-break-inside: avoid;
        }

        .totals-box {
            width: 55%;
            margin-left: auto;
            padding: 16px 18px;
            background: #fafafa;
            border: 1px solid #ddd;
            border-left: 4px solid transparent;  /* Will be colored via ColorScheme */
        }

        .totals-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            font-size: 9.5pt;
            border-bottom: 1px solid #efefef;
        }

        .totals-row span:last-child {
            text-align: right;
            font-weight: 600;
        }

        .totals-row.total-amount {
            font-size: 11pt;
            font-weight: bold;
            border-bottom: none;
            padding-top: 12px;
            border-top: 2px solid #ddd;
        }

        /* Apply total box style */
        ${generateTotalBoxStyle(totalBoxStyle)}

        /* Payment Section Enhancement */
        .payment-section {
            margin-top: 30px;
            padding: 16px 18px;
            background: #fafafa;
            border-left: 4px solid transparent;  /* Will be colored via ColorScheme */
            page-break-inside: avoid;
        }

        .payment-section h3 {
            margin: 0 0 10px 0;
            font-size: 10pt;
            font-weight: bold;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .payment-section p {
            margin: 6px 0;
            font-size: 9.5pt;
            line-height: 1.5;
        }

        .payment-details {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            page-break-inside: avoid;
        }

        /* Notes Section */
        .notes-section {
            margin: 25px 0;
            padding: 12px 15px;
            background: #f5f5f5;
            border-left: 4px solid transparent;
            page-break-inside: avoid;
        }

        .notes-section h3 {
            margin: 0 0 8px 0;
            font-size: 10pt;
            font-weight: bold;
            text-transform: uppercase;
        }

        .notes-section p {
            margin: 0;
            font-size: 9pt;
            line-height: 1.6;
            color: #555;
        }
    """.trimIndent()

    /**
     * Apply color scheme overrides using CSS variables.
     */
    private fun generateColorOverrides(scheme: ColorScheme): String {
        return """
        /* ═══════════════════════════════════════════════════════════════ */
        /* COLOR SCHEME: ${scheme.displayName}                                     */
        /* ═══════════════════════════════════════════════════════════════ */

        :root {
            --primary-color: ${scheme.primaryHex};
            --accent-color: ${scheme.accentHex};
            --secondary-color: ${lightenColor(scheme.primaryHex, 15)};
            --muted-color: ${lightenColor(scheme.primaryHex, 85)};
        }

        .invoice-header {
            background-color: var(--primary-color);
            color: white;
        }

        .invoice-title h2 {
            color: var(--primary-color);
        }

        .bill-to h3,
        .invoice-details h3,
        .payment-section h3,
        .notes-section h3 {
            color: var(--primary-color);
        }

        .items-table th {
            background-color: var(--secondary-color);
            border-bottom-color: var(--primary-color);
        }

        .totals-box {
            border-left-color: var(--primary-color);
            background-color: var(--muted-color);
        }

        .totals-row.total-amount {
            color: var(--primary-color);
        }

        .payment-section {
            border-left-color: var(--accent-color);
        }

        .notes-section {
            border-left-color: var(--primary-color);
        }

        .text-primary { color: var(--primary-color); }
        .text-accent { color: var(--accent-color); }
        .bg-primary { background-color: var(--primary-color); }
        .bg-muted { background-color: var(--muted-color); }
        """.trimIndent()
    }

    /**
     * Spacing profile multipliers - scales padding/margins throughout invoice.
     */
    private fun generateSpacingOverrides(profile: SpacingProfile): String {
        val multiplier = when (profile) {
            SpacingProfile.TIGHT -> 0.75f
            SpacingProfile.NORMAL -> 1.0f
            SpacingProfile.GENEROUS -> 1.3f
            SpacingProfile.PREMIUM -> 1.6f
        }

        val headerMargin = (24 * multiplier).toInt()
        val detailsMargin = (20 * multiplier).toInt()
        val itemsMargin = (25 * multiplier).toInt()
        val rowHeight = (28 * multiplier).toInt()
        val totalsMargin = (30 * multiplier).toInt()
        val sectionPadding = (16 * multiplier).toInt()

        return """
        /* ═══════════════════════════════════════════════════════════════ */
        /* SPACING PROFILE: ${profile.displayName} (${multiplier}x multiplier)                          */
        /* ═══════════════════════════════════════════════════════════════ */

        .invoice-header {
            margin-bottom: ${headerMargin}px;
        }

        .details-section {
            margin-top: ${detailsMargin}px;
            margin-bottom: ${detailsMargin}px;
        }

        .items-table {
            margin-top: ${itemsMargin}px;
            margin-bottom: ${itemsMargin}px;
        }

        .items-table tbody tr {
            height: ${rowHeight}px;
        }

        .totals-section {
            margin-top: ${totalsMargin}px;
            margin-bottom: ${totalsMargin}px;
        }

        .payment-section {
            margin-top: ${totalsMargin}px;
            padding: ${sectionPadding}px;
        }

        .notes-section {
            margin-top: ${(20 * multiplier).toInt()}px;
            padding: ${(12 * multiplier).toInt()}px ${(15 * multiplier).toInt()}px;
        }
        """.trimIndent()
    }

    /**
     * Visual accents toggles - borders, shadows, dividers, highlights, gradients.
     */
    private fun generateVisualAccentsCss(
        enableBorders: Boolean,
        enableShadows: Boolean,
        enableDividers: Boolean,
        enableHighlight: Boolean,
        enableGradients: Boolean
    ): String = buildString {
        appendLine("/* ═══════════════════════════════════════════════════════════════ */")
        appendLine("/* VISUAL ACCENTS                                                   */")
        appendLine("/* ═══════════════════════════════════════════════════════════════ */")
        appendLine()

        if (enableBorders) {
            appendLine("""
            .section-card {
                border: 1px solid #ddd;
            }
            .items-table,
            .totals-box,
            .payment-section,
            .notes-section {
                border: 1px solid #ddd;
            }
            """.trimIndent())
        } else {
            appendLine("""
            .section-card,
            .items-table,
            .totals-box,
            .payment-section,
            .notes-section {
                border: none;
            }
            """.trimIndent())
        }

        if (enableShadows) {
            appendLine("""
            .totals-box,
            .payment-section,
            .notes-section {
                box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            }
            """.trimIndent())
        }

        if (enableDividers) {
            appendLine("""
            .invoice-header {
                border-bottom: 2px solid #ddd;
                padding-bottom: 12px;
            }
            """.trimIndent())
        }

        if (enableHighlight) {
            appendLine("""
            .totals-row.total-amount {
                background-color: rgba(107, 76, 154, 0.05);
                padding: 12px 0;
            }
            """.trimIndent())
        }

        if (enableGradients) {
            appendLine("""
            .invoice-header {
                background: linear-gradient(to right, var(--primary-color), var(--accent-color));
            }
            """.trimIndent())
        }
    }

    /**
     * Total box style variations.
     */
    private fun generateTotalBoxStyle(style: TotalBoxStyle): String = when (style) {
        TotalBoxStyle.SUBTLE_BACKGROUND -> """
            .totals-box {
                background-color: #fafafa;
                border: 1px solid #ddd;
            }
            .totals-row.total-amount {
                font-weight: bold;
                color: #333;
            }
        """.trimIndent()

        TotalBoxStyle.PROMINENT_BORDER -> """
            .totals-box {
                background-color: white;
                border: 3px solid var(--primary-color);
            }
            .totals-row.total-amount {
                color: var(--primary-color);
                font-weight: bold;
            }
        """.trimIndent()

        TotalBoxStyle.ACCENT_BORDER -> """
            .totals-box {
                background-color: white;
                border: 2px solid var(--primary-color);
                border-left: 4px solid var(--primary-color);
            }
            .totals-row.total-amount {
                color: var(--primary-color);
                font-weight: bold;
            }
        """.trimIndent()

        TotalBoxStyle.BOLD_HIGHLIGHT -> """
            .totals-box {
                background-color: var(--primary-color);
                color: white;
                border: none;
            }
            .totals-row {
                color: white;
                border-bottom-color: rgba(255, 255, 255, 0.2);
            }
            .totals-row span {
                color: white;
            }
        """.trimIndent()

        TotalBoxStyle.GRADIENT_BACKGROUND -> """
            .totals-box {
                background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
                color: white;
                border: none;
            }
            .totals-row {
                color: white;
                border-bottom-color: rgba(255, 255, 255, 0.2);
            }
            .totals-row span {
                color: white;
            }
        """.trimIndent()
    }

    /**
     * Table styling with alternating rows.
     */
    private fun generateTableStyling(
        enableAlternatingRows: Boolean,
        colorScheme: ColorScheme
    ): String = buildString {
        appendLine("/* ═══════════════════════════════════════════════════════════════ */")
        appendLine("/* TABLE STYLING                                                   */")
        appendLine("/* ═══════════════════════════════════════════════════════════════ */")
        appendLine()

        if (enableAlternatingRows) {
            val rowAltColor = lightenColor(colorScheme.primaryHex, 92)
            appendLine("""
            .items-table tbody tr:nth-child(even) {
                background-color: $rowAltColor;
            }
            """.trimIndent())
        }

        appendLine("""
        .items-table tbody tr:hover {
            background-color: #f0f0f0;
        }

        /* Right-aligned columns */
        .items-table td.amount,
        .items-table th.amount {
            text-align: right;
        }

        .items-table td.quantity,
        .items-table th.quantity {
            text-align: right;
            width: 15%;
        }

        .items-table td.price,
        .items-table th.price {
            text-align: right;
            width: 20%;
        }

        .items-table td.total,
        .items-table th.total {
            text-align: right;
            width: 20%;
            font-weight: 600;
        }

        .items-table td.description,
        .items-table th.description {
            text-align: left;
            width: 45%;
        }
        """.trimIndent())
    }

    /**
     * Helper: Lighten hex color by percentage.
     */
    private fun lightenColor(hexColor: String, percent: Int): String {
        val hex = hexColor.removePrefix("#").uppercase()
        val r = hex.substring(0, 2).toIntOrNull(16) ?: 0
        val g = hex.substring(2, 4).toIntOrNull(16) ?: 0
        val b = hex.substring(4, 6).toIntOrNull(16) ?: 0

        val factor = percent / 100.0
        val lr = (r + (255 - r) * factor).toInt().coerceIn(0, 255)
        val lg = (g + (255 - g) * factor).toInt().coerceIn(0, 255)
        val lb = (b + (255 - b) * factor).toInt().coerceIn(0, 255)

        return "#%02X%02X%02X".format(lr, lg, lb)
    }
}


