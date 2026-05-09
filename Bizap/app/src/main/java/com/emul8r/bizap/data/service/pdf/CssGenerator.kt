package com.emul8r.bizap.data.service.pdf

import com.emul8r.bizap.domain.model.*
import android.content.Context
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dynamic CSS generation service for PDF invoices.
 *
 * Generates CSS at runtime by composing:
 * 1. Base template CSS (from HtmlInvoiceStyle)
 * 2. Phase 1 improvements (page breaks, spacing, footer)
 * 3. Design quality improvements (hierarchy, table readability, totals emphasis)
 * 4. Color scheme overrides
 * 5. Spacing profile multipliers
 * 6. Visual accents toggles
 *
 * **Benefits:**
 * - All improvements cascade automatically to all templates
 * - User settings control which features are applied
 * - No need to maintain multiple CSS files
 * - Easy to add new customization options
 * - Settings changes don't require code deployment
 *
 * **Usage:**
 * ```kotlin
 * val css = cssGenerator.generateCss(
 *     baseTemplate = HtmlInvoiceStyle.MODERN,
 *     colorScheme = ColorScheme.PROFESSIONAL,
 *     spacingProfile = SpacingProfile.GENEROUS,
 *     visualAccents = VisualAccents(showBorders = true, showShadows = true),
 *     totalBoxStyle = TotalBoxStyle.BOLD_HIGHLIGHT
 * )
 *
 * val html = layout.generateHtml(snapshot, css)
 * ```
 */
@Singleton
class CssGenerator @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val TAG = "CssGenerator"
        private const val CSS_ASSET_PATH = "invoices/html-theme/"
    }

    /**
     * Cache loaded CSS templates to avoid repeated asset reads
     */
    private val cssCache = mutableMapOf<String, String>()

    /**
     * Generate complete CSS for invoice PDF.
     *
     * @param baseTemplate Base template style (MODERN, CORPORATE, etc.)
     * @param colorScheme Color palette to use
     * @param spacingProfile Whitespace/padding profile
     * @param visualAccents Visual styling toggles (borders, shadows, etc.)
     * @param totalBoxStyle How to emphasize totals section
     *
     * @return Complete CSS string ready for embedding in HTML
     */
    fun generateCss(
        baseTemplate: HtmlInvoiceStyle,
        colorScheme: ColorScheme,
        spacingProfile: SpacingProfile,
        visualAccents: VisualAccents,
        totalBoxStyle: TotalBoxStyle
    ): String {
        Timber.tag(TAG).d(
            "Generating CSS: template=${baseTemplate.name}, " +
            "colors=${colorScheme.displayName}, " +
            "spacing=${spacingProfile.displayName}"
        )

        return buildString {
            // 1. Load base template CSS
            append(getBaseTemplate(baseTemplate))
            append("\n\n")

            // 2. Apply Phase 1 improvements (page breaks, spacing, footer)
            append(generatePhase1Improvements())
            append("\n\n")

            // 3. Apply design quality improvements (hierarchy, emphasis)
            append(generateQualityImprovements(totalBoxStyle))
            append("\n\n")

            // 4. Apply color scheme overrides
            append(generateColorOverrides(colorScheme))
            append("\n\n")

            // 5. Apply spacing profile multipliers
            append(generateSpacingOverrides(spacingProfile))
            append("\n\n")

            // 6. Apply visual accents toggles
            append(generateVisualAccentsCss(visualAccents))

            Timber.tag(TAG).d("✅ CSS generation complete (${this.length} bytes)")
        }
    }

    /**
     * Get base template CSS for selected style.
     *
     * Returns the foundation CSS for each template (colors, fonts, layout).
     * Typically loaded from assets, but returned here for generation purposes.
     */
    private fun getBaseTemplate(style: HtmlInvoiceStyle): String = when (style) {
        HtmlInvoiceStyle.MODERN -> getModernTemplate()
        HtmlInvoiceStyle.MINIMAL -> getMinimalTemplate()
        HtmlInvoiceStyle.CORPORATE -> getCorporateTemplate()
        HtmlInvoiceStyle.CREATIVE -> getCreativeTemplate()
        HtmlInvoiceStyle.PREMIUM_PROFESSIONAL -> getPremiumProfessionalTemplate()
        HtmlInvoiceStyle.WARM_APPROACHABLE -> getWarmApproachableTemplate()
        HtmlInvoiceStyle.SASS_PROFESSIONAL -> getSassProfessionalTemplate()
        HtmlInvoiceStyle.REFINED -> getRefinedTemplate()
        HtmlInvoiceStyle.PROFESSIONAL_PLUS -> getProfessionalPlusTemplate()
    }

    /**
     * PHASE 1 IMPROVEMENTS: Page breaks, spacing, footer positioning
     *
     * These CSS improvements were added in Phase 1 and ensure:
     * - Proper page breaking (page-break-inside: avoid on sections)
     * - Flex layout for automatic footer positioning
     * - A4 page sizing (11 inches)
     * - Professional spacing throughout
     */
    private fun generatePhase1Improvements(): String = """
        /* ═══════════════════════════════════════════════════════════════
           PHASE 1: PAGE LAYOUT & BREAK CONTROL
           ═══════════════════════════════════════════════════════════════ */

        * {
            box-sizing: border-box;
        }

        html, body {
            margin: 0;
            padding: 0;
            height: 100%;
        }

        .page {
            page-break-after: always;
            min-height: 11in;              /* A4 page height */
            display: flex;
            flex-direction: column;
            padding: 0.75in;
            background: white;
        }

        /* Protect sections from page breaks */
        .invoice-header,
        .details-section,
        .items-table,
        .totals-section,
        .payment-section,
        .notes-section {
            page-break-inside: avoid;
        }

        /* Footer always positioned at bottom of page */
        .footer {
            margin-top: auto;
            padding-top: 15px;
            border-top: 1px solid #eee;
        }
    """.trimIndent()

    /**
     * DESIGN QUALITY IMPROVEMENTS:
     * - Issue #1: Information Hierarchy (Bill To smaller, Invoice Details larger)
     * - Issue #2: Vertical Spacing (breathing room between sections)
     * - Issue #3: Table Readability (larger rows, alternating colors)
     * - Issue #4: Total Due Emphasis (prominently styled)
     * - Issue #5: Payment Section Enhancement (distinct styling)
     */
    private fun generateQualityImprovements(totalBoxStyle: TotalBoxStyle): String = """
        /* ═══════════════════════════════════════════════════════════════
           DESIGN QUALITY: HIERARCHY, SPACING, READABILITY
           ═══════════════════════════════════════════════════════════════ */

        /* Issue #1: Information Hierarchy */
        .bill-to h3 {
            font-size: 10pt;        /* Smaller - supporting context */
            font-weight: 600;
            color: #666;
            margin: 0 0 8px 0;
        }

        .bill-to p {
            font-size: 9pt;
            color: #666;
            margin: 3px 0;
        }

        .invoice-details h3 {
            font-size: 12pt;        /* Larger - primary information */
            font-weight: bold;
            color: #6B4C9A;         /* Primary accent color */
            margin: 0 0 8px 0;
        }

        .invoice-details p {
            font-size: 10pt;
            color: #333;
            margin: 4px 0;
        }

        /* Issue #2: Vertical Spacing - Breathing Room */
        .invoice-header {
            margin-bottom: 24px;    /* Was 12px - increase breathing room */
        }

        .details-section {
            margin-bottom: 20px;
        }

        .items-table {
            margin: 20px 0 24px 0;  /* Extra bottom margin before totals */
        }

        .totals-section {
            margin-top: 20px;
            margin-bottom: 20px;
        }

        .payment-section {
            margin-top: 24px;
        }

        /* Issue #3: Table Readability */
        .items-table {
            width: 100%;
            border-collapse: collapse;
        }

        .items-table th {
            background: #f0f0f0;
            font-weight: bold;
            border-bottom: 2px solid #6B4C9A;
            padding: 10px;
            font-size: 10pt;
            text-align: left;
        }

        .items-table tr {
            height: 28px;           /* Increased from 20px for readability */
        }

        .items-table tbody tr:nth-child(even) {
            background: #f9f9f9;    /* Zebra striping for visual separation */
        }

        .items-table td {
            padding: 8px 12px;      /* Better vertical/horizontal spacing */
            vertical-align: middle;
            border-bottom: 1px solid #eee;
            font-size: 10pt;
        }

        .items-table td:nth-child(n+2) {
            text-align: right;      /* Right-align numeric columns */
        }

        /* Column width proportions */
        .items-table td:first-child {
            width: 50%;             /* Description gets more space */
        }

        /* Issue #4: Total Due Emphasis - Using style parameter */
        ${generateTotalBoxStyleCss(totalBoxStyle)}

        /* Issue #5: Payment Section Enhancement */
        .payment-section {
            background: #fafafa;
            border: 1px solid #e0e0e0;
            border-left: 4px solid #FF9F43;
            padding: 16px 12px;
            border-radius: 2px;
        }

        .payment-section h3 {
            font-size: 12pt;
            font-weight: bold;
            color: #6B4C9A;
            margin-top: 0;
            margin-bottom: 12px;
        }

        .payment-detail {
            font-size: 10pt;
            margin: 6px 0;
            display: flex;
            justify-content: space-between;
        }

        .payment-label {
            font-weight: 600;
            color: #333;
        }

        .payment-value {
            color: #666;
        }

        /* Bank details styling */
        .bank-detail-label {
            font-weight: 600;
            color: #333;
            font-size: 9pt;
        }

        .bank-detail-value {
            color: #666;
            font-size: 9pt;
            font-family: monospace;  /* Monospace for account numbers */
        }
    """.trimIndent()

    /**
     * Total box styling - varies by TotalBoxStyle selection.
     */
    private fun generateTotalBoxStyleCss(style: TotalBoxStyle): String = when (style) {
        TotalBoxStyle.SUBTLE_BACKGROUND -> """
            .total-amount {
                background: #f5f5f5;
                padding: 12px;
                border-radius: 2px;
                margin: 10px 0;
            }

            .total-amount strong {
                font-size: 14pt;
                font-weight: bold;
            }
        """.trimIndent()

        TotalBoxStyle.PROMINENT_BORDER -> """
            .total-amount {
                border: 3px solid #6B4C9A;
                padding: 12px;
                border-radius: 2px;
                margin: 10px 0;
            }

            .total-amount strong {
                font-size: 14pt;
                font-weight: bold;
            }
        """.trimIndent()

        TotalBoxStyle.ACCENT_BORDER -> """
            .total-amount {
                border: 2px solid #6B4C9A;
                padding: 12px;
                border-radius: 2px;
                margin: 10px 0;
            }

            .total-amount strong {
                font-size: 14pt;
                font-weight: bold;
            }
        """.trimIndent()

        TotalBoxStyle.BOLD_HIGHLIGHT -> """
            .total-amount {
                background: #6B4C9A;
                color: white;
                padding: 12px;
                border-radius: 2px;
                margin: 10px 0;
                font-weight: bold;
            }

            .total-amount strong {
                font-size: 16pt;
                font-weight: bold;
            }
        """.trimIndent()

        TotalBoxStyle.GRADIENT_BACKGROUND -> """
            .total-amount {
                background: linear-gradient(to right, #6B4C9A, #FF9F43);
                color: white;
                padding: 12px;
                border-radius: 2px;
                margin: 10px 0;
                font-weight: bold;
            }

            .total-amount strong {
                font-size: 16pt;
                font-weight: bold;
            }
        """.trimIndent()
    }

    /**
     * Color scheme overrides - applies palette colors throughout template.
     */
    private fun generateColorOverrides(scheme: ColorScheme): String {
        val secondary = lightenHex(scheme.primaryHex, 20)

        return """
            /* ═══════════════════════════════════════════════════════════════
               COLOR SCHEME: ${scheme.displayName}
               ═══════════════════════════════════════════════════════════════ */

            :root {
                --primary-color: ${scheme.primaryHex};
                --accent-color: ${scheme.accentHex};
                --secondary-color: $secondary;
            }

            .invoice-header {
                background-color: ${scheme.primaryHex};
                color: white;
            }

            .invoice-title h2 {
                color: rgba(255, 255, 255, 0.9);
            }

            .card {
                border-left-color: ${scheme.accentHex};
            }

            .card-header {
                color: ${scheme.primaryHex};
            }

            .invoice-details h3 {
                color: ${scheme.primaryHex};
            }

            .items-table th {
                border-bottom-color: ${scheme.primaryHex};
            }

            .payment-section {
                border-left-color: ${scheme.accentHex};
            }

            .payment-section h3 {
                color: ${scheme.primaryHex};
            }

            .total-amount {
                border-color: ${scheme.primaryHex};
            }
        """.trimIndent()
    }

    /**
     * Spacing profile multipliers - scale all spacing values based on profile.
     */
    private fun generateSpacingOverrides(profile: SpacingProfile): String {
        val multiplier = when (profile) {
            SpacingProfile.TIGHT -> 0.8f
            SpacingProfile.NORMAL -> 1.0f
            SpacingProfile.GENEROUS -> 1.3f
            SpacingProfile.PREMIUM -> 1.6f
        }

        return """
            /* ═══════════════════════════════════════════════════════════════
               SPACING PROFILE: ${profile.displayName} (multiplier: $multiplier)
               ═══════════════════════════════════════════════════════════════ */

            .invoice-header {
                margin-bottom: ${"%.0f".format(24 * multiplier)}px;
            }

            .details-section {
                margin-bottom: ${"%.0f".format(20 * multiplier)}px;
            }

            .items-table {
                margin: ${"%.0f".format(20 * multiplier)}px 0 ${"%.0f".format(24 * multiplier)}px 0;
            }

            .items-table tr {
                height: ${"%.0f".format(28 * multiplier)}px;
            }

            .items-table td {
                padding: ${"%.0f".format(8 * multiplier)}px ${"%.0f".format(12 * multiplier)}px;
            }

            .totals-section {
                margin-top: ${"%.0f".format(20 * multiplier)}px;
                margin-bottom: ${"%.0f".format(20 * multiplier)}px;
            }

            .payment-section {
                margin-top: ${"%.0f".format(24 * multiplier)}px;
                padding: ${"%.0f".format(16 * multiplier)}px ${"%.0f".format(12 * multiplier)}px;
            }
        """.trimIndent()
    }

    /**
     * Visual accents toggles - show/hide decorative elements.
     */
    private fun generateVisualAccentsCss(accents: VisualAccents): String = buildString {
        append("/* ═══════════════════════════════════════════════════════════════\n")
        append("   VISUAL ACCENTS\n")
        append("   ═══════════════════════════════════════════════════════════════ */\n\n")

        if (accents.showBorders) {
            append(".card { border: 1px solid #ddd; }\n")
        }

        if (accents.showShadows) {
            append(".card { box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }\n")
        }

        if (accents.showDividers) {
            append("hr { border: 1px solid #ccc; margin: 16px 0; }\n")
        }

        if (accents.highlightTotals) {
            append(".total-amount { font-size: 20pt; font-weight: bold; }\n")
        }

        if (accents.useGradients) {
            append("""
                .invoice-header {
                    background: linear-gradient(to right, #6B4C9A, #FF9F43);
                }
            """.trimIndent())
            append("\n")
        }
    }

    /**
     * Helper: Lighten a hex color by percentage.
     */
    private fun lightenHex(hex: String, percent: Int): String {
        val cleanHex = hex.removePrefix("#")
        val r = cleanHex.substring(0, 2).toInt(16).let { (it * (100 + percent)) / 100 }.coerceAtMost(255)
        val g = cleanHex.substring(2, 4).toInt(16).let { (it * (100 + percent)) / 100 }.coerceAtMost(255)
        val b = cleanHex.substring(4, 6).toInt(16).let { (it * (100 + percent)) / 100 }.coerceAtMost(255)
        return "#%02x%02x%02x".format(r, g, b)
    }

    /**
     * Load CSS template from assets with caching.
     */
    private fun loadCssAsset(fileName: String): String {
        // Check cache first
        cssCache[fileName]?.let { return it }

        return try {
            val css = context.assets.open("$CSS_ASSET_PATH$fileName").bufferedReader().use { it.readText() }
            cssCache[fileName] = css  // Cache for future use
            Timber.tag(TAG).d("✅ Loaded CSS asset: $fileName")
            css
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Failed to load CSS asset: $fileName")
            "/* Failed to load CSS: $fileName */"
        }
    }

    // ───────────────────────────────────────────────────────────────
    // BASE TEMPLATE CSS GETTERS (Loaded from assets)
    // ───────────────────────────────────────────────────────────────

    private fun getModernTemplate(): String = loadCssAsset("invoice-styles.css")
    private fun getMinimalTemplate(): String = loadCssAsset("invoice-styles-minimal.css")
    private fun getCorporateTemplate(): String = loadCssAsset("invoice-styles-corporate.css")
    private fun getCreativeTemplate(): String = loadCssAsset("invoice-styles-creative.css")

    private fun getPremiumProfessionalTemplate(): String {
        // Premium uses SASS professional template if available
        return loadCssAsset("invoice-styles.css")  // Fallback to modern
    }

    private fun getWarmApproachableTemplate(): String {
        // Warm uses creative template as base if available
        return loadCssAsset("invoice-styles-creative.css")  // Fallback
    }

    private fun getSassProfessionalTemplate(): String {
        // SASS professional uses corporate template if available
        return loadCssAsset("invoice-styles-corporate.css")  // Fallback
    }

    private fun getRefinedTemplate(): String = loadCssAsset("invoice-styles-refined.css")

    private fun getProfessionalPlusTemplate(): String {
        // Professional Plus uses corporate as base
        return loadCssAsset("invoice-styles-corporate.css")  // Fallback
    }
}




