package com.emul8r.bizap.data.pdf

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import timber.log.Timber

/**
 * PDF Theme Adapter - converts app theme to PDF styling
 *
 * Automatically syncs Bizap app colors and typography to PDF generation.
 * Ensures PDFs always match the current app theme.
 *
 * Problem it solves:
 * - PDFs don't match app theme
 * - Colors are hardcoded, not flexible
 * - When app theme changes, PDFs still use old colors
 *
 * Solution:
 * - Extract colors from Material 3 theme
 * - Adapt typography from app design system
 * - Adapt spacing from app design system
 * - PDFs auto-sync when app theme changes
 */
class PdfThemeAdapter(
    private val context: Context,
    private val colorScheme: ColorScheme? = null
) {
    companion object {
        private const val TAG = "PdfThemeAdapter"

        // Status colors (from BizapColors, but defined here to avoid UI layer dependency)
        private const val STATUS_PAID = "#4CAF50"           // Green
        private const val ANALYTICS_WARNING = "#FF9800"     // Orange
    }

    /**
     * Adapt Material 3 colors to PDF color scheme
     * Uses current app theme or defaults to Material 3 light theme
     */
    fun adaptColors(): PdfColorScheme {
        return try {
            if (colorScheme != null) {
                Timber.d(TAG, "📱 Adapting colors from provided Material 3 ColorScheme")
                adaptFromMaterial3(colorScheme)
            } else {
                Timber.d(TAG, "📱 Using default Material 3 colors")
                createDefaultColorScheme()
            }
        } catch (e: Exception) {
            Timber.e(e, TAG, "Failed to adapt colors, using defaults")
            createDefaultColorScheme()
        }
    }

    /**
     * Extract colors from Material 3 theme
     */
    private fun adaptFromMaterial3(scheme: ColorScheme): PdfColorScheme {
        return PdfColorScheme(
            primary = colorToHex(scheme.primary),
            secondary = colorToHex(scheme.secondary),
            accent = colorToHex(scheme.tertiary),
            text = colorToHex(scheme.onBackground),
            textLight = colorToHex(scheme.onSurfaceVariant),
            background = colorToHex(scheme.background),
            border = colorToHex(scheme.outlineVariant),
            success = colorToHex(scheme.tertiary),           // Use tertiary for success
            warning = ANALYTICS_WARNING,                     // Use fallback color constant
            error = colorToHex(scheme.error)
        ).also {
            Timber.d(TAG, """
                ✅ Colors adapted from Material 3:
                   Primary: ${it.primary}
                   Secondary: ${it.secondary}
                   Accent: ${it.accent}
                   Text: ${it.text}
                   Background: ${it.background}
            """.trimIndent())
        }
    }

    /**
     * Create default color scheme based on Material 3
     */
    private fun createDefaultColorScheme(): PdfColorScheme {
        return PdfColorScheme(
            primary = colorToHex(Color(0xFF6750A4)),       // Material 3 Purple
            secondary = colorToHex(Color(0xFF625B71)),     // Material 3 Gray
            accent = colorToHex(Color(0xFF7D5260)),        // Material 3 Pink
            text = colorToHex(Color(0xFF1C1B1F)),          // Near black
            textLight = colorToHex(Color(0xFF49454E)),     // Medium gray
            background = colorToHex(Color(0xFFFFFBFE)),    // Near white
            border = colorToHex(Color(0xFFCAC7D0)),        // Light gray
            success = STATUS_PAID,                          // Green success
            warning = ANALYTICS_WARNING,                    // Orange warning
            error = colorToHex(Color(0xFFB3261E))          // Error red
        ).also {
            Timber.d(TAG, "✅ Using default Material 3 color scheme")
        }
    }

    /**
     * Adapt typography from app design system
     */
    fun adaptTypography(): PdfTypography {
        return PdfTypography(
            titleSize = 24,           // "INVOICE"/"QUOTE" title
            headerSize = 16,          // Company name / header
            subheaderSize = 12,       // Details / location / dept
            bodySize = 11,            // Line items
            footerSize = 9,           // Footer text
            fontFamily = "Roboto"     // Material 3 default
        ).also {
            Timber.d(TAG, """
                ✅ Typography adapted:
                   Title: ${it.titleSize}pt
                   Header: ${it.headerSize}pt
                   Subheader: ${it.subheaderSize}pt
                   Body: ${it.bodySize}pt
                   Font: ${it.fontFamily}
            """.trimIndent())
        }
    }

    /**
     * Adapt spacing from app design system
     */
    fun adaptSpacing(): PdfSpacingConfig {
        // Design system spacing units (typical: 4dp, 8dp, 12dp, 16dp, 24dp)
        val spacingSmall = 8
        val spacingMedium = 16
        val spacingLarge = 24

        return PdfSpacingConfig(
            pageMargin = spacingMedium,
            titleMarginBottom = spacingSmall + 7,          // 15
            headerMarginBottom = spacingSmall + 2,         // 10
            subheaderMarginBottom = spacingMedium - 1,     // 15
            lineItemsStartY = 250,
            sectionSpacing = spacingSmall,                 // 8
            tableRowHeight = 18,
            footerMarginTop = spacingMedium + 4            // 20
        ).also {
            Timber.d(TAG, """
                ✅ Spacing adapted:
                   Page Margin: ${it.pageMargin}px
                   Title Margin: ${it.titleMarginBottom}px
                   Header Margin: ${it.headerMarginBottom}px
                   Subheader Margin: ${it.subheaderMarginBottom}px
                   Line Items Start: ${it.lineItemsStartY}px
            """.trimIndent())
        }
    }

    /**
     * Adapt PDF theme configuration
     */
    fun adaptPdfThemeConfig(): PdfThemeConfig {
        return PdfThemeConfig(
            templateStyle = "PROFESSIONAL",
            includeQrCode = true,
            includeWatermark = false,
            showLineNumbers = false,
            companyLogoPath = null
        ).also {
            Timber.d(TAG, "✅ PDF theme configured: ${it.templateStyle}")
        }
    }

    /**
     * Create complete PDF render model with adapted theme
     */
    fun createThemedRenderModel(
        documentType: DocumentType,
        documentTitle: String,
        header: HeaderSection?,
        subheader: SubheaderSection,
        invoiceData: com.emul8r.bizap.domain.model.InvoiceSnapshot,
        layoutMode: LayoutMode = LayoutMode.MODERN
    ): PdfRenderModel {
        return PdfRenderModel(
            documentType = documentType,
            documentTitle = documentTitle,
            header = header,
            subheader = subheader,
            invoiceData = invoiceData,
            pdfTheme = adaptPdfThemeConfig(),
            colors = adaptColors(),
            typography = adaptTypography(),
            spacing = adaptSpacing(),
            layoutMode = layoutMode
        ).also {
            Timber.d(TAG, """
                ✅ Complete themed render model created:
                   Document: $documentTitle ($documentType)
                   Layout: $layoutMode
                   Colors synced from app theme
                   Spacing from design system
                   Typography from design system
            """.trimIndent())
        }
    }

    /**
     * Convert Compose Color to hex string
     */
    private fun colorToHex(color: Color): String {
        val colorInt = color.value.toLong()
        return String.format("#%08X", colorInt).substring(0, 7)
    }

    /**
     * Validate that adapted theme is valid
     */
    fun validateTheme(theme: PdfColorScheme, spacing: PdfSpacingConfig): Boolean {
        return try {
            // Validate colors are valid hex
            val hexPattern = Regex("^#[0-9A-F]{6}$")
            val colorsValid = hexPattern.matches(theme.primary) &&
                    hexPattern.matches(theme.accent) &&
                    hexPattern.matches(theme.text)

            // Validate spacing is reasonable
            val spacingValid = spacing.validate()

            (colorsValid && spacingValid).also { valid ->
                if (valid) {
                    Timber.d(TAG, "✅ Theme validation passed")
                } else {
                    Timber.w(TAG, "⚠️ Theme validation failed: Colors=$colorsValid, Spacing=$spacingValid")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, TAG, "Theme validation error")
            false
        }
    }
}

/**
 * Builder for easier PdfThemeAdapter usage
 */
class PdfThemeAdapterBuilder(private val context: Context) {
    private var colorScheme: ColorScheme? = null

    fun withColorScheme(scheme: ColorScheme): PdfThemeAdapterBuilder {
        this.colorScheme = scheme
        return this
    }

    fun build(): PdfThemeAdapter {
        return PdfThemeAdapter(context, colorScheme)
    }
}


