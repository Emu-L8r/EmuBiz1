package com.emul8r.bizap.data.pdf

import com.emul8r.bizap.domain.model.InvoiceSnapshot

/**
 * Unified PDF rendering model used by ALL interfaces (Modern & Classic)
 *
 * This is the single source of truth for PDF generation.
 * Both GUI2 (Modern) and GUI1 (Classic) invoice creation screens
 * build this object and pass it to the PDF generation service.
 *
 * Benefits:
 * - Eliminates code duplication between interfaces
 * - Guarantees consistent PDF styling across all interfaces
 * - Easy to customize without breaking layouts
 * - Clear separation of header/subheader/company data
 */
data class PdfRenderModel(
    // Document type - distinguishes Invoice vs Quote
    val documentType: DocumentType,

    // Document title - professional label at top of PDF
    // Examples: "INVOICE", "QUOTE", "BILL", "ESTIMATE"
    val documentTitle: String,

    // Header section - optional user-defined header
    // Example: Company name, project name, etc.
    // Can be null if user doesn't want a header
    val header: HeaderSection?,

    // Subheader section - supporting information
    // Example: Store location, department, shop number
    // Multiple lines, each properly positioned
    val subheader: SubheaderSection,

    // Core invoice data to render
    val invoiceData: InvoiceSnapshot,

    // Styling configuration
    val pdfTheme: PdfThemeConfig,
    val colors: PdfColorScheme,
    val typography: PdfTypography,

    // Layout configuration - prevents overlaps and ensures spacing
    val spacing: PdfSpacingConfig,

    // Layout mode - determines visual style
    val layoutMode: LayoutMode = LayoutMode.MODERN
)

/**
 * Document type - determines PDF label and style
 */
enum class DocumentType {
    INVOICE,    // Official invoice document
    QUOTE       // Quote/estimate document
}

/**
 * Layout mode - determines visual presentation
 */
enum class LayoutMode {
    MODERN,     // Clean, contemporary design
    COMPACT,    // Dense, space-efficient design
    FORMAL      // Traditional, formal appearance
}

/**
 * Header section - optional user-defined header
 * Separated from company name for flexibility
 */
data class HeaderSection(
    val text: String?,
    val style: HeaderStyle = HeaderStyle.DEFAULT
) {
    companion object {
        fun empty(): HeaderSection = HeaderSection(text = null)
    }
}

enum class HeaderStyle {
    DEFAULT,
    ACCENT,
    MUTED
}

/**
 * Subheader section - supporting information lines
 * Each line is properly positioned to avoid overlaps
 */
data class SubheaderSection(
    val lines: List<String> = emptyList(),
    val style: SubheaderStyle = SubheaderStyle.DEFAULT
) {
    companion object {
        fun empty(): SubheaderSection = SubheaderSection(lines = emptyList())

        fun from(vararg lines: String): SubheaderSection {
            return SubheaderSection(lines = lines.filter { it.isNotBlank() })
        }
    }
}

enum class SubheaderStyle {
    DEFAULT,
    ACCENT,
    MUTED
}

/**
 * PDF color scheme - synced from app theme
 * All colors are hex strings for easy CSS/Canvas conversion
 */
data class PdfColorScheme(
    val primary: String,        // Primary brand color (hex)
    val secondary: String,      // Secondary color (hex)
    val accent: String,         // Accent/highlight color (hex)
    val text: String,           // Primary text color (hex)
    val textLight: String,      // Secondary text color (hex)
    val background: String,     // Background color (hex)
    val border: String,         // Border/divider color (hex)
    val success: String = "#4CAF50",    // Success/paid color
    val warning: String = "#FF9800",    // Warning color
    val error: String = "#F44336"       // Error/overdue color
) {
    companion object {
        /**
         * Create from Material 3 colors (app theme)
         */
        fun fromMaterial3(
            primary: String,
            secondary: String,
            tertiary: String,
            onBackground: String,
            background: String,
            outlineVariant: String
        ): PdfColorScheme {
            return PdfColorScheme(
                primary = primary,
                secondary = secondary,
                accent = tertiary,
                text = onBackground,
                textLight = "#757575",
                background = background,
                border = outlineVariant
            )
        }
    }
}

/**
 * PDF typography - font sizes and families
 */
data class PdfTypography(
    val titleSize: Int = 24,        // "INVOICE" or "QUOTE" label
    val headerSize: Int = 16,       // Company/header section
    val subheaderSize: Int = 12,    // Supporting details
    val bodySize: Int = 11,         // Line items
    val footerSize: Int = 9,        // Footer text
    val fontFamily: String = "Roboto"
)

/**
 * PDF spacing configuration - prevents overlaps and ensures consistency
 * All measurements in pixels/points
 */
data class PdfSpacingConfig(
    val pageMargin: Int = 20,                       // Overall page margin
    val titleMarginBottom: Int = 15,                // Space below "INVOICE"
    val headerMarginBottom: Int = 10,               // Space below header
    val subheaderMarginBottom: Int = 15,            // Space below subheader
    val lineItemsStartY: Int = 250,                 // Where line items begin
    val sectionSpacing: Int = 8,                    // Space between subheader lines
    val tableRowHeight: Int = 18,                   // Height of line item rows
    val footerMarginTop: Int = 20                   // Space above footer
) {
    /**
     * Validate spacing won't cause page overflow
     */
    fun validate(): Boolean {
        val totalUsedSpace = pageMargin + 40 + titleMarginBottom + 50 + headerMarginBottom +
                             80 + subheaderMarginBottom + 200 + footerMarginTop
        return totalUsedSpace < 750  // Leave room for actual content
    }
}

/**
 * PDF theme configuration - selects template style
 */
data class PdfThemeConfig(
    val templateStyle: String = "PROFESSIONAL",    // PROFESSIONAL, MINIMAL, CORPORATE, CREATIVE
    val includeQrCode: Boolean = true,
    val includeWatermark: Boolean = false,
    val showLineNumbers: Boolean = false,
    val companyLogoPath: String? = null
)

