package com.emul8r.bizap.data.service.sass

/**
 * SASS-inspired style engine for invoice PDF generation.
 *
 * Implements a compile-time Kotlin analogue of SCSS variable substitution and mixin expansion.
 * Since Android does not ship a SASS runtime, this engine stores design tokens as typed Kotlin
 * data classes ("variables") and resolves them into plain CSS strings at runtime — exactly the
 * same responsibility that the SASS compiler has on the web.
 *
 * ## Design-token hierarchy (mirrors SCSS partial imports)
 *
 * ```
 * SassTokens           ← top-level design tokens ($primary, $spacing-unit, …)
 *   └── SassMixins     ← parameterised CSS fragments (equivalent to @mixin / @include)
 *         └── SassStyleEngine ← compiles tokens + mixins into a complete CSS string
 * ```
 *
 * ## Usage
 * ```kotlin
 * val engine  = SassStyleEngine(SassTokens.premiumProfessional())
 * val css     = engine.compile()
 * val html    = "<style>$css</style> ... invoice html ..."
 * ```
 */

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 1 — Design tokens  (equivalent to SCSS _variables.scss)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Complete set of design tokens for a single invoice theme.
 *
 * Naming convention mirrors the SCSS variable convention (`$kebab-case` → `camelCase`).
 */
data class SassTokens(
    // --- Palette ---
    val colorPrimary: String,          // $color-primary
    val colorAccent: String,           // $color-accent
    val colorBackground: String,       // $color-bg
    val colorSurface: String,          // $color-surface
    val colorText: String,             // $color-text
    val colorTextMuted: String,        // $color-text-muted
    val colorBorder: String,           // $color-border
    val colorTotalBg: String,          // $color-total-bg
    val colorTotalText: String,        // $color-total-text
    val colorRowAlt: String,           // $color-row-alt

    // --- Typography ---
    val fontFamily: String,            // $font-family
    val fontSizeBase: String,          // $font-size-base
    val fontSizeSmall: String,         // $font-size-small
    val fontSizeLarge: String,         // $font-size-large
    val fontSizeTitle: String,         // $font-size-title
    val fontWeightNormal: String,      // $font-weight-normal
    val fontWeightBold: String,        // $font-weight-bold
    val lineHeight: String,            // $line-height

    // --- Spacing ---
    val spacingUnit: String,           // $spacing-unit  (base 8 px grid)
    val spacingHalf: String,           // $spacing-unit / 2
    val spacingDouble: String,         // $spacing-unit * 2
    val spacingPageMargin: String,     // @page margin
    val cellPaddingV: String,          // table cell vertical padding
    val cellPaddingH: String,          // table cell horizontal padding

    // --- Structural ---
    val headerAccentBorderWidth: String,   // accent bar height under header
    val tableBorderWidth: String,          // row border
    val sectionBorderLeftWidth: String,    // left-accent on info boxes
    val borderRadius: String              // subtle rounding (preview only)
) {
    companion object {

        /**
         * "SASS Professional" theme — the dark-header, blue-accent design inspired by
         * high-quality B2B invoicing tools.
         */
        fun sassprofessional(): SassTokens = SassTokens(
            colorPrimary             = "#0A2540",   // deep navy
            colorAccent              = "#0066FF",   // electric blue
            colorBackground          = "#FFFFFF",
            colorSurface             = "#F7F9FC",
            colorText                = "#1A1A2E",
            colorTextMuted           = "#6B7280",
            colorBorder              = "#E2E8F0",
            colorTotalBg             = "#0A2540",
            colorTotalText           = "#FFFFFF",
            colorRowAlt              = "#F7F9FC",
            fontFamily               = "Arial, Helvetica, 'Segoe UI', sans-serif",
            fontSizeBase             = "10pt",
            fontSizeSmall            = "8.5pt",
            fontSizeLarge            = "12pt",
            fontSizeTitle            = "26pt",
            fontWeightNormal         = "400",
            fontWeightBold           = "700",
            lineHeight               = "1.8",
            spacingUnit              = "8px",
            spacingHalf              = "4px",
            spacingDouble            = "16px",
            spacingPageMargin        = "14mm",
            cellPaddingV             = "10px",
            cellPaddingH             = "14px",
            headerAccentBorderWidth  = "4px",
            tableBorderWidth         = "1px",
            sectionBorderLeftWidth   = "4px",
            borderRadius             = "4px"
        )

        /**
         * Slate + teal variant of the SASS engine — lighter feel.
         */
        fun sassLight(): SassTokens = SassTokens(
            colorPrimary             = "#2D3748",
            colorAccent              = "#00BFA5",
            colorBackground          = "#FFFFFF",
            colorSurface             = "#F7FAFC",
            colorText                = "#1A202C",
            colorTextMuted           = "#718096",
            colorBorder              = "#E2E8F0",
            colorTotalBg             = "#2D3748",
            colorTotalText           = "#FFFFFF",
            colorRowAlt              = "#F7FAFC",
            fontFamily               = "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
            fontSizeBase             = "10pt",
            fontSizeSmall            = "8.5pt",
            fontSizeLarge            = "12pt",
            fontSizeTitle            = "24pt",
            fontWeightNormal         = "400",
            fontWeightBold           = "700",
            lineHeight               = "1.8",
            spacingUnit              = "8px",
            spacingHalf              = "4px",
            spacingDouble            = "16px",
            spacingPageMargin        = "14mm",
            cellPaddingV             = "10px",
            cellPaddingH             = "14px",
            headerAccentBorderWidth  = "3px",
            tableBorderWidth         = "1px",
            sectionBorderLeftWidth   = "3px",
            borderRadius             = "4px"
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 2 — Mixins  (equivalent to SCSS _mixins.scss)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Parameterised CSS fragments.  Each function corresponds to a `@mixin` definition;
 * the caller supplies arguments just like `@include mixin($arg1, $arg2)`.
 */
object SassMixins {

    /**
     * @mixin page-setup($margin)
     * iText7 @page rule for PDF margin control.
     */
    fun pageSetup(margin: String): String =
        "@page { margin: $margin; }"

    /**
     * @mixin body-base($family, $size, $color, $lineHeight)
     */
    fun bodyBase(family: String, size: String, color: String, lineHeight: String): String = """
        body {
            font-family: $family;
            font-size: $size;
            color: $color;
            margin: 0;
            padding: 0;
            line-height: $lineHeight;
            background-color: #FFFFFF;
        }
    """.trimIndent()

    /**
     * @mixin table-reset
     * Forces iText7-safe table rendering.
     */
    fun tableReset(): String = """
        table { border-collapse: collapse; width: 100%; }
        td, th { word-wrap: break-word; }
    """.trimIndent()

    /**
     * @mixin header-band($bg, $accentColor, $accentHeight)
     * Full-width dark header band with bottom accent line.
     */
    fun headerBand(bg: String, accentColor: String, accentHeight: String): String = """
        .header-band {
            background-color: $bg;
            color: #FFFFFF;
        }
        .header-accent {
            background-color: $accentColor;
            height: $accentHeight;
        }
    """.trimIndent()

    /**
     * @mixin section-box($bg, $borderColor, $borderWidth)
     * Info box with a left-coloured border (Bill To, Invoice Details, etc.).
     */
    fun sectionBox(bg: String, borderColor: String, borderWidth: String): String = """
        .section-box {
            background-color: $bg;
            border-left: $borderWidth solid $borderColor;
        }
    """.trimIndent()

    /**
     * @mixin table-header($bg, $textColor, $fontSize)
     */
    fun tableHeader(bg: String, textColor: String, fontSize: String): String = """
        .table-header th {
            background-color: $bg;
            color: $textColor;
            font-size: $fontSize;
            padding: 12px 14px;
            text-align: left;
        }
        .table-header th.align-right { text-align: right; }
        .table-header th.align-center { text-align: center; }
    """.trimIndent()

    /**
     * @mixin table-row($altBg, $borderColor, $paddingV, $paddingH, $lineHeight)
     */
    fun tableRow(
        altBg: String,
        borderColor: String,
        paddingV: String,
        paddingH: String,
        lineHeight: String
    ): String = """
        .table-row td {
            padding: $paddingV $paddingH;
            border-bottom: 1px solid $borderColor;
            line-height: $lineHeight;
        }
        .table-row-alt { background-color: $altBg; }
    """.trimIndent()

    /**
     * @mixin totals-row($bg, $textColor, $fontSize, $fontWeight)
     */
    fun totalsRow(bg: String, textColor: String, fontSize: String, fontWeight: String): String = """
        .total-row td {
            background-color: $bg;
            color: $textColor;
            font-size: $fontSize;
            font-weight: $fontWeight;
            padding: 12px 14px;
        }
    """.trimIndent()
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 3 — Compiler  (the engine itself)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Compiles [SassTokens] + [SassMixins] into a self-contained CSS stylesheet string.
 *
 * The output is valid, iText7-compatible inline CSS (no flexbox, no CSS variables,
 * no gradients — only properties that iText7's HTML converter can parse).
 */
class SassStyleEngine(private val tokens: SassTokens) {

    /**
     * Compile all design tokens and mixins into a single CSS string.
     *
     * The resulting string can be embedded directly in a `<style>` tag inside
     * the invoice HTML document.
     */
    fun compile(): String = buildString {
        // @page
        appendLine(SassMixins.pageSetup(tokens.spacingPageMargin))
        appendLine()

        // body
        appendLine(SassMixins.bodyBase(
            family     = tokens.fontFamily,
            size       = tokens.fontSizeBase,
            color      = tokens.colorText,
            lineHeight = tokens.lineHeight
        ))
        appendLine()

        // table reset
        appendLine(SassMixins.tableReset())
        appendLine()

        // header band
        appendLine(SassMixins.headerBand(
            bg           = tokens.colorPrimary,
            accentColor  = tokens.colorAccent,
            accentHeight = tokens.headerAccentBorderWidth
        ))
        appendLine()

        // section boxes (info panels)
        appendLine(SassMixins.sectionBox(
            bg          = tokens.colorSurface,
            borderColor = tokens.colorAccent,
            borderWidth = tokens.sectionBorderLeftWidth
        ))
        appendLine()

        // items table header
        appendLine(SassMixins.tableHeader(
            bg        = tokens.colorAccent,
            textColor = "#FFFFFF",
            fontSize  = tokens.fontSizeBase
        ))
        appendLine()

        // items table rows
        appendLine(SassMixins.tableRow(
            altBg       = tokens.colorRowAlt,
            borderColor = tokens.colorBorder,
            paddingV    = tokens.cellPaddingV,
            paddingH    = tokens.cellPaddingH,
            lineHeight  = tokens.lineHeight
        ))
        appendLine()

        // totals row
        appendLine(SassMixins.totalsRow(
            bg         = tokens.colorTotalBg,
            textColor  = tokens.colorTotalText,
            fontSize   = tokens.fontSizeLarge,
            fontWeight = tokens.fontWeightBold
        ))
        appendLine()

        // Utility classes derived from tokens
        appendLine(buildUtilityClasses())
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun buildUtilityClasses(): String = """
        /* Utility classes compiled from design tokens */
        .text-muted   { color: ${tokens.colorTextMuted}; }
        .text-primary { color: ${tokens.colorPrimary}; }
        .text-accent  { color: ${tokens.colorAccent}; }
        .text-bold    { font-weight: ${tokens.fontWeightBold}; }
        .text-small   { font-size: ${tokens.fontSizeSmall}; }
        .text-large   { font-size: ${tokens.fontSizeLarge}; }
        .text-title   { font-size: ${tokens.fontSizeTitle}; }
        .text-right   { text-align: right; }
        .text-center  { text-align: center; }
        .text-left    { text-align: left; }
        .surface-bg   { background-color: ${tokens.colorSurface}; }
        .accent-border-left { border-left: ${tokens.sectionBorderLeftWidth} solid ${tokens.colorAccent}; }
        .primary-border-left { border-left: ${tokens.sectionBorderLeftWidth} solid ${tokens.colorPrimary}; }
        .border-bottom { border-bottom: ${tokens.tableBorderWidth} solid ${tokens.colorBorder}; }
        .p-cell   { padding: ${tokens.cellPaddingV} ${tokens.cellPaddingH}; }
        .p-half   { padding: ${tokens.spacingHalf}; }
        .p-unit   { padding: ${tokens.spacingUnit}; }
        .p-double { padding: ${tokens.spacingDouble}; }
        .mb-unit  { margin-bottom: ${tokens.spacingUnit}; }
        .mb-double{ margin-bottom: ${tokens.spacingDouble}; }
    """.trimIndent()
}
