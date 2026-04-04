package com.emul8r.bizap.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Centralized invoice settings and customization.
 * Stored per user, applied to all invoices unless overridden.
 *
 * This is the single source of truth for all invoice-related settings:
 * - Theme selection (Canvas or HTML-to-PDF)
 * - Company branding (logo, colors, contact info)
 * - Payment details (terms, bank info)
 * - Tax configuration (rate, name, handling mode)
 * - Invoice defaults (prefix, notes, footer message)
 */
@Entity(tableName = "invoice_settings")
data class InvoiceSettings(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,

    // PDF TEMPLATE SELECTION
    @ColumnInfo(name = "selected_theme")
    val selectedTheme: InvoiceTheme = InvoiceTheme.CANVAS,

    // PDF ENGINE SELECTION (new three-tier architecture)
    @ColumnInfo(name = "selected_pdf_engine")
    val selectedPdfEngine: PdfEngine = PdfEngine.HTML_CSS,

    // PAGE LAYOUT SELECTION
    @ColumnInfo(name = "selected_page_layout")
    val selectedPageLayout: PageLayout = PageLayout.MODERN,

    // PDF HTML STYLE SELECTION (for HTML-to-PDF theme)
    @ColumnInfo(name = "selected_html_style")
    val selectedHtmlStyle: HtmlInvoiceStyle = HtmlInvoiceStyle.MODERN,

    // CANVAS TEMPLATE SELECTION (for Canvas theme)
    @ColumnInfo(name = "selected_canvas_template")
    val selectedCanvasTemplate: CanvasInvoiceTemplate = CanvasInvoiceTemplate.MODERN,

    // PREVIEW MODE
    @ColumnInfo(name = "preview_with_placeholder")
    val previewWithPlaceholder: Boolean = false,

    // PDF STYLING (kept for invoice appearance)
    @ColumnInfo(name = "primary_color")
    val primaryColor: String = "#6B4C9A",      // Default purple
    @ColumnInfo(name = "secondary_color")
    val secondaryColor: String = "#f5f5f5",    // Light gray background
    @ColumnInfo(name = "accent_color")
    val accentColor: String = "#2c3e50",       // Dark blue-gray for text
    @ColumnInfo(name = "font_family")
    val fontFamily: String? = null,

    // PDF INVOICE CONFIGURATION
    @ColumnInfo(name = "payment_terms_days")
    val paymentTermsDays: Int = 30,
    @ColumnInfo(name = "default_payment_notes")
    val defaultPaymentNotes: String = "",
    @ColumnInfo(name = "footer_message")
    val footerMessage: String = "Thank you for your business",
    @ColumnInfo(name = "invoice_number_prefix")
    val invoiceNumberPrefix: String = "INV-",

    // TAX CONFIGURATION (for PDF display)
    @ColumnInfo(name = "tax_rate")
    val taxRate: Double = 0.10,
    @ColumnInfo(name = "tax_name")
    val taxName: String = "GST",
    @ColumnInfo(name = "tax_handling")
    val taxHandling: TaxHandling = TaxHandling.EXCLUSIVE,

    // METADATA
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    /**
     * Validate settings for PDF-specific fields only.
     * Business information validation happens in BusinessProfile, not here.
     */
    fun isValid(): Boolean {
        return selectedTheme != null
    }

    /**
     * Get default settings for user.
     */
    companion object {
        fun default(userId: String) = InvoiceSettings(userId = userId)
    }
}

/**
 * Enum for PDF rendering engine selection.
 *
 * CANVAS: Android's PdfDocument API - direct coordinate control, artistic designs
 * HTML_CSS: HTML-to-PDF conversion - professional layouts, CSS styling
 */
enum class PdfEngine {
    CANVAS,      // Canvas-based rendering with coordinate control
    HTML_CSS     // HTML-to-PDF rendering with CSS styling
}

/**
 * Enum for page layout organization.
 *
 * CLASSIC: Original layout - Header | Bill To + Invoice Details | Items | Totals | Footer
 * MODERN: Compact side-by-side layout with grid organization
 */
enum class PageLayout {
    CLASSIC,     // Traditional invoice layout
    MODERN       // Compact modern grid-based layout
}

/**
 * Enum for invoice theme selection.
 * DEPRECATED: Use PdfEngine instead.
 *
 * CANVAS: Existing Canvas-based PDF generation (Phase 9 implementation)
 * HTML_PDF: New HTML-to-PDF modern style (Phase 6 implementation)
 */
enum class InvoiceTheme {
    CANVAS,      // Existing Canvas-based PDF
    HTML_PDF     // New HTML-to-PDF modern style
}

/**
 * Enum for Canvas-based invoice template styles.
 *
 * Provides 4 distinct professional Canvas designs:
 * - MODERN: Purple + Orange vibrant artistic design
 * - PROFESSIONAL: Navy + Gold formal corporate design
 * - CREATIVE: Teal + Orange energetic modern design
 * - MINIMAL: Dark Gray + Teal clean and simple design
 */
enum class CanvasInvoiceTemplate(
    val displayName: String,
    val description: String,
    val colorScheme: String,
    val primaryHex: String,
    val accentHex: String
) {
    MODERN(
        displayName = "Modern (Artistic)",
        description = "Purple + Orange vibrant design with artistic styling",
        colorScheme = "Purple & Orange",
        primaryHex = "#6B4C9A",
        accentHex = "#FF9F43"
    ),
    PROFESSIONAL(
        displayName = "Professional (Formal)",
        description = "Navy + Gold classical design for business",
        colorScheme = "Navy & Gold",
        primaryHex = "#003366",
        accentHex = "#FFC107"
    ),
    CREATIVE(
        displayName = "Creative (Vibrant)",
        description = "Teal + Orange energetic modern design",
        colorScheme = "Teal & Orange",
        primaryHex = "#00A8A8",
        accentHex = "#FF6B35"
    ),
    MINIMAL(
        displayName = "Minimal (Clean)",
        description = "Dark Gray + Teal clean and simple",
        colorScheme = "Gray & Teal",
        primaryHex = "#2C3E50",
        accentHex = "#17A2B8"
    )
}

/**
 * Enum for tax handling mode.
 *
 * INCLUSIVE: Tax is included in the amount shown
 * EXCLUSIVE: Tax is added to the amount (default, Australian style)
 */
enum class TaxHandling {
    INCLUSIVE,   // Tax included in amount
    EXCLUSIVE    // Tax added to amount
}

/**
 * User-friendly preset colors for invoice branding.
 * Users select by name (e.g., "Professional Purple"), not hex codes.
 *
 * This eliminates the need for users to understand hex color codes,
 * making color selection intuitive and accessible.
 */
enum class PresetColor(val hexCode: String, val displayName: String) {
    PURPLE("#6B4C9A", "Professional Purple"),
    BLUE("#2E5090", "Corporate Blue"),
    GREEN("#27AE60", "Success Green"),
    ORANGE("#E67E22", "Warm Orange"),
    RED("#C0392B", "Professional Red"),
    DARK_GRAY("#2C3E50", "Dark Gray"),
    TEAL("#16A085", "Modern Teal"),
    INDIGO("#3F51B5", "Indigo"),
    NAVY("#1A5276", "Navy Blue"),
    FOREST("#1E5631", "Forest Green"),
    MAROON("#922B3E", "Maroon"),
    SLATE("#34495E", "Slate Blue");

    companion object {
        /**
         * Find preset color by hex code.
         * Useful for converting saved hex back to preset.
         *
         * @param hex Hex color code to match
         * @return Matching PresetColor or null if no match
         */
        fun fromHexCode(hex: String): PresetColor? {
            return values().find { it.hexCode.equals(hex, ignoreCase = true) }
        }
    }
}

/**
 * Enum for HTML-to-PDF invoice styles.
 *
 * Provides multiple professional design options for HTML-generated invoices:
 * - MODERN: Premium modern design with purple gradient (default)
 * - MINIMAL: Clean, minimalist design with black/white and simple lines
 * - CORPORATE: Formal business design with serif fonts and blue gradient
 * - CREATIVE: Vibrant, modern design with orange/teal colors (startup style)
 */
enum class HtmlInvoiceStyle(val displayName: String, val description: String, val styleFile: String) {
    MODERN("Modern (Premium)", "Professional modern design with purple gradient", "invoice-styles.css"),
    MINIMAL("Minimalist (Clean)", "Clean, elegant design with minimal styling", "invoice-styles-minimal.css"),
    CORPORATE("Corporate (Formal)", "Formal business design with serif typography", "invoice-styles-corporate.css"),
    CREATIVE("Creative (Startup)", "Vibrant, modern design perfect for startups", "invoice-styles-creative.css");

    companion object {
        fun getDefault() = MODERN
    }
}
