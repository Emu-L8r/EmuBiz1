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

    // TYPOGRAPHY SELECTION
    @ColumnInfo(name = "selected_typography")
    val selectedTypography: Typography = Typography.MODERN,

    // LOCALE & FORMATTING SELECTION
    @ColumnInfo(name = "selected_locale")
    val selectedLocale: InvoiceLocale = InvoiceLocale.AUSTRALIAN,

    // PDF HTML STYLE SELECTION (for HTML-to-PDF theme)
    @ColumnInfo(name = "selected_html_style")
    val selectedHtmlStyle: HtmlInvoiceStyle = HtmlInvoiceStyle.MODERN,

    // CANVAS TEMPLATE SELECTION (for Canvas theme)
    @ColumnInfo(name = "selected_canvas_template")
    val selectedCanvasTemplate: CanvasInvoiceTemplate = CanvasInvoiceTemplate.MODERN,

    // ─────────────────────────────────────────────────────────────────────────
    // NEW CUSTOMIZATION LAYERS (Phase 3 Enhancement)
    // ─────────────────────────────────────────────────────────────────────────

    // COLOR SCHEME SELECTION
    @ColumnInfo(name = "selected_color_scheme")
    val selectedColorScheme: ColorScheme = ColorScheme.PROFESSIONAL,

    // SPACING PROFILE SELECTION
    @ColumnInfo(name = "selected_spacing_profile")
    val selectedSpacingProfile: SpacingProfile = SpacingProfile.NORMAL,

    // VISUAL ACCENTS (JSON-serialized)
    @ColumnInfo(name = "visual_accents_json")
    val visualAccentsJson: String = VisualAccents.default().toJsonString(),

    // ─────────────────────────────────────────────────────────────────────────
    // PHASE 2: COMPREHENSIVE VISUAL CUSTOMIZATION
    // ─────────────────────────────────────────────────────────────────────────

    // GRADIENT & ACCENT OPTIONS
    @ColumnInfo(name = "enable_gradient_header")
    val enableGradientHeader: Boolean = true,
    @ColumnInfo(name = "header_gradient_end_color")
    val headerGradientEndColor: String = "#FF9F43",

    // SHAPE & SHADOW OPTIONS
    @ColumnInfo(name = "enable_rounded_corners")
    val enableRoundedCorners: Boolean = true,
    @ColumnInfo(name = "corner_radius_dp")
    val cornerRadiusDp: Float = 8f,
    @ColumnInfo(name = "enable_shadows")
    val enableShadows: Boolean = true,
    @ColumnInfo(name = "shadow_intensity")
    val shadowIntensity: Float = 0.15f,

    // ROW STYLING OPTIONS
    @ColumnInfo(name = "enable_alternating_row_colors")
    val enableAlternatingRowColors: Boolean = true,
    @ColumnInfo(name = "alternate_row_color")
    val alternateRowColor: String = "#F5F5F5",

    // DIVIDER OPTIONS
    @ColumnInfo(name = "enable_dividers")
    val enableDividers: Boolean = true,
    @ColumnInfo(name = "divider_style")
    val dividerStyle: DividerStyle = DividerStyle.SOLID,
    @ColumnInfo(name = "divider_color")
    val dividerColor: String = "#CCCCCC",
    @ColumnInfo(name = "divider_thickness_px")
    val dividerThicknessPx: Float = 1f,

    // HIGHLIGHT OPTIONS
    @ColumnInfo(name = "highlight_totals")
    val highlightTotals: Boolean = true,
    @ColumnInfo(name = "total_box_style")
    val totalBoxStyle: TotalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
    @ColumnInfo(name = "enable_status_badges")
    val enableStatusBadges: Boolean = true,
    @ColumnInfo(name = "badge_style")
    val badgeStyle: BadgeStyle = BadgeStyle.ROUNDED_FILLED,

    // BACKGROUND PATTERN OPTIONS
    @ColumnInfo(name = "enable_background_pattern")
    val enableBackgroundPattern: Boolean = false,
    @ColumnInfo(name = "background_pattern_type")
    val backgroundPatternType: BackgroundPattern = BackgroundPattern.WAVES,
    @ColumnInfo(name = "pattern_opacity")
    val patternOpacity: Float = 0.08f,

    // WATERMARK OPTIONS
    @ColumnInfo(name = "enable_watermark_text")
    val enableWatermarkText: Boolean = false,
    @ColumnInfo(name = "watermark_text")
    val watermarkText: String = "",
    @ColumnInfo(name = "watermark_opacity")
    val watermarkOpacity: Float = 0.1f,

    // ─────────────────────────────────────────────────────────────────────────
    // ✨ PHASE 3: BRANDING LAYER (Logo, Motto, Payment Icons, Signatures, QR)
    // ─────────────────────────────────────────────────────────────────────────

    // LOGO OPTIONS
    @ColumnInfo(name = "enable_logo")
    val enableLogo: Boolean = false,
    @ColumnInfo(name = "logo_uri")
    val logoUri: String = "",
    @ColumnInfo(name = "logo_width_mm")
    val logoWidthMm: Float = 30f,
    @ColumnInfo(name = "logo_height_mm")
    val logoHeightMm: Float = 30f,
    @ColumnInfo(name = "logo_position")
    val logoPosition: LogoPosition = LogoPosition.TOP_LEFT,

    // MOTTO / SLOGAN OPTIONS
    @ColumnInfo(name = "enable_motto")
    val enableMotto: Boolean = false,
    @ColumnInfo(name = "motto_text")
    val mottoText: String = "",
    @ColumnInfo(name = "motto_font_size")
    val mottoFontSize: Float = 10f,
    @ColumnInfo(name = "motto_color")
    val mottoColor: String = "#666666",

    // PAYMENT METHOD ICONS OPTIONS
    @ColumnInfo(name = "enable_payment_icons")
    val enablePaymentIcons: Boolean = false,
    @ColumnInfo(name = "accepted_payment_methods_json")
    val acceptedPaymentMethodsJson: String = "[]",
    @ColumnInfo(name = "payment_icons_size")
    val paymentIconsSize: Float = 16f,

    // SIGNATURE OPTIONS
    @ColumnInfo(name = "enable_signature_area")
    val enableSignatureArea: Boolean = false,
    @ColumnInfo(name = "signature_label")
    val signatureLabel: String = "Authorized By:",
    @ColumnInfo(name = "signature_line_length_mm")
    val signatureLineLengthMm: Float = 40f,

    // QR CODE OPTIONS
    @ColumnInfo(name = "enable_qr_code")
    val enableQrCode: Boolean = false,
    @ColumnInfo(name = "qr_code_content")
    val qrCodeContent: String = "",
    @ColumnInfo(name = "qr_code_size_mm")
    val qrCodeSizeMm: Float = 20f,
    @ColumnInfo(name = "qr_code_position")
    val qrCodePosition: QrCodePosition = QrCodePosition.BOTTOM_RIGHT,

    // COMPANY BRANDING
    @ColumnInfo(name = "company_motto")
    val companyMotto: String = "",
    @ColumnInfo(name = "company_website")
    val companyWebsite: String = "",
    @ColumnInfo(name = "company_social_media_json")
    val companySocialMediaJson: String = "{}",

    // ─────────────────────────────────────────────────────────────────────────

    // PREVIEW MODE
    @ColumnInfo(name = "preview_with_placeholder")
    val previewWithPlaceholder: Boolean = false,

    // VISIBILITY TOGGLES
    @ColumnInfo(name = "show_business_abn")
    val showBusinessAbn: Boolean = true,

    @ColumnInfo(name = "show_customer_phone")
    val showCustomerPhone: Boolean = true,

    @ColumnInfo(name = "show_status_watermark")
    val showStatusWatermark: Boolean = true,

    @ColumnInfo(name = "show_page_numbers")
    val showPageNumbers: Boolean = false,

    // Phase 2: signature / authorization field visibility
    @ColumnInfo(name = "show_signature_field")
    val showSignatureField: Boolean = true,

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
     *
     * Note: selectedTheme always has a default value (InvoiceTheme.CANVAS),
     * so this validation always passes. Kept for API stability.
     */
    fun isValid(): Boolean = true

    /**
     * Parse visual accents from JSON string.
     * Falls back to default if parsing fails.
     */
    fun getVisualAccents(): VisualAccents {
        return try {
            val json = visualAccentsJson
            val showBorders = json.contains("\"showBorders\":true", ignoreCase = true)
            val showShadows = json.contains("\"showShadows\":true", ignoreCase = true)
            val showDividers = json.contains("\"showDividers\":true", ignoreCase = true)
            val highlightTotals = json.contains("\"highlightTotals\":true", ignoreCase = true)
            val useGradients = json.contains("\"useGradients\":true", ignoreCase = true)
            VisualAccents(showBorders, showShadows, showDividers, highlightTotals, useGradients)
        } catch (e: Exception) {
            VisualAccents.default()
        }
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
 * SASS_PROFESSIONAL: Premium two-column layout with professional branding, icons, QR codes
 */
enum class PdfEngine {
    CANVAS,              // Canvas-based rendering with coordinate control
    HTML_CSS,            // HTML-to-PDF rendering with CSS styling
    SASS_PROFESSIONAL    // Professional SASS/HTML template - two-column, modern branding
}

/**
 * Enum for page layout organization - 8 different layout options.
 *
 * **Standard Layouts:**
 * - CLASSIC: Traditional layout - Header | Bill To + Invoice Details | Items | Totals | Footer
 * - MODERN: Compact side-by-side layout with grid organization
 * - SPACIOUS: Generous spacing and larger fonts for premium feel
 * - COMPACT: Executive compact layout - minimal margins, many items per page
 *
 * **Advanced Layouts:**
 * - SIDEBAR: Business info/branding in left sidebar, items and totals on right
 * - CARDS: Each line item displayed as a card instead of table rows
 * - MINIMAL_TABLES: Ultra-clean table layout with minimal borders and lines
 * - FOCUSED: Single-column layout with emphasis on totals and key metrics
 */
/**
 * Page Layout enum for PDF invoice generation.
 *
 * Defines 9 distinct page layout styles for professional invoices.
 * Each layout has a maximum item capacity before requiring pagination.
 *
 * - CLASSIC: Traditional invoice layout - header, details, items, totals, footer
 * - MODERN: Compact side-by-side grid layout - efficient and professional
 * - SPACIOUS: Premium layout with generous spacing - elegant and luxurious
 * - COMPACT: Executive tight layout - fits many items per page
 * - SIDEBAR: Business branding on left panel - modern and distinctive
 * - CARDS: Line items as cards - visually engaging and interactive-feel
 * - MINIMAL_TABLES: Ultra-clean tables with minimal borders - minimalist elegance
 * - FOCUSED: Single column with emphasis on totals - financial clarity
 * - ADVANCED_PAGINATED: Automatically paginates with smart headers for 12+ items
 */
enum class PageLayout(
    val emoji: String,
    val displayName: String,
    val description: String,
    val maxItemsPerPage: Int = 15 // Phase 3D: Max items before auto-pagination
) {
    CLASSIC("📋", "Classic", "Traditional invoice layout - header, details, items, totals, footer", 20),
    MODERN("🎯", "Modern", "Compact side-by-side grid layout - efficient and professional", 15),
    SPACIOUS("✨", "Spacious", "Premium layout with generous spacing - elegant and luxurious", 12),
    COMPACT("📊", "Compact", "Executive tight layout - fits many items per page", 25),
    SIDEBAR("📑", "Sidebar", "Business branding on left panel - modern and distinctive", 18),
    CARDS("🎨", "Cards", "Line items as cards - visually engaging and interactive-feel", 8),
    MINIMAL_TABLES("⚪", "Minimal", "Ultra-clean tables with minimal borders - minimalist elegance", 20),
    FOCUSED("🎯", "Focused", "Single column with emphasis on totals - financial clarity", 10),
    ADVANCED_PAGINATED("📄", "Multi-Page Auto", "Automatically paginates with smart headers for 12+ items", Int.MAX_VALUE)
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
 * - PREMIUM_PROFESSIONAL: Dark navy header with electric-blue accent bar
 * - WARM_APPROACHABLE: Warm amber tones with friendly typography
 * - SASS_PROFESSIONAL: SASS-engine compiled style — deep navy + electric blue,
 *   tight grid layout, large prominent totals; the highest-fidelity template
 * - REFINED: HTML template matching Canvas grid system with purple gradient
 * - PROFESSIONAL_PLUS: Sidebar branding, signature line, geometric accents —
 *   the highest quality Phase 1 template with modern professional design
 */
enum class HtmlInvoiceStyle(val displayName: String, val description: String, val styleFile: String) {
    MODERN("Modern (Premium)", "Professional modern design with purple gradient", "invoice-styles.css"),
    MINIMAL("Minimalist (Clean)", "Clean, elegant design with minimal styling", "invoice-styles-minimal.css"),
    CORPORATE("Corporate (Formal)", "Formal business design with serif typography", "invoice-styles-corporate.css"),
    CREATIVE("Creative (Startup)", "Vibrant, modern design perfect for startups", "invoice-styles-creative.css"),
    PREMIUM_PROFESSIONAL("Premium Professional", "Modern minimalist design with dark header and blue accents", "invoice-styles-premium.css"),
    WARM_APPROACHABLE("Warm Approachable", "Friendly design with warm colors and approachable typography", "invoice-styles-warm.css"),
    SASS_PROFESSIONAL("SASS Professional ✨", "SASS-engine compiled: deep navy header, electric-blue accents, tight grid layout", "invoice-styles-sass.css"),
    REFINED("REFINED (Canvas Match)", "HTML template matching Canvas grid system exactly with purple gradient", "invoice-styles-refined.css"),
    PROFESSIONAL_PLUS("Professional Plus ⭐", "Sidebar branding, signature line, geometric accents — highest quality template", "invoice-styles-professional-plus.css");

    companion object {
        fun getDefault() = MODERN
    }
}

/**
 * Enum for typography selection.
 *
 * MODERN: Sans-serif font (Segoe UI, Arial) - clean, contemporary look
 * CLASSIC: Serif font (Georgia, Times) - traditional, professional look
 * ROUNDED: Rounded sans-serif (Trebuchet MS) - friendly, approachable look
 */
enum class Typography {
    MODERN,      // Sans-serif - clean, contemporary
    CLASSIC,     // Serif - traditional, professional
    ROUNDED      // Rounded sans-serif - friendly, approachable
}

/**
 * Enum for locale-based formatting (currency & date).
 *
 * UNITED_STATES: $ before amount (e.g., $1,234.56), MM/DD/YYYY dates
 * EUROPEAN: € after amount (e.g., 1.234,56 €), DD/MM/YYYY dates
 * AUSTRALIAN: $ before amount (e.g., $1,234.56), DD/MM/YYYY dates
 * JAPANESE: ¥ before amount (e.g., ¥1,234), YYYY/MM/DD dates
 * BRITISH: £ before amount (e.g., £1,234.56), DD/MM/YYYY dates
 * CANADIAN: $ before amount (e.g., $1,234.56), YYYY/MM/DD dates
 */
enum class InvoiceLocale(
    val displayName: String,
    val currencySymbol: String,
    val currencyPosition: CurrencyPosition,
    val dateFormat: String,
    val thousandsSeparator: Char,
    val decimalSeparator: Char
) {
    UNITED_STATES(
        displayName = "United States",
        currencySymbol = "$",
        currencyPosition = CurrencyPosition.BEFORE,
        dateFormat = "MM/dd/yyyy",
        thousandsSeparator = ',',
        decimalSeparator = '.'
    ),
    EUROPEAN(
        displayName = "Europe (EUR)",
        currencySymbol = "€",
        currencyPosition = CurrencyPosition.AFTER,
        dateFormat = "dd/MM/yyyy",
        thousandsSeparator = '.',
        decimalSeparator = ','
    ),
    AUSTRALIAN(
        displayName = "Australia",
        currencySymbol = "$",
        currencyPosition = CurrencyPosition.BEFORE,
        dateFormat = "dd/MM/yyyy",
        thousandsSeparator = ',',
        decimalSeparator = '.'
    ),
    BRITISH(
        displayName = "United Kingdom",
        currencySymbol = "£",
        currencyPosition = CurrencyPosition.BEFORE,
        dateFormat = "dd/MM/yyyy",
        thousandsSeparator = ',',
        decimalSeparator = '.'
    ),
    CANADIAN(
        displayName = "Canada",
        currencySymbol = "$",
        currencyPosition = CurrencyPosition.BEFORE,
        dateFormat = "yyyy/MM/dd",
        thousandsSeparator = ',',
        decimalSeparator = '.'
    ),
    JAPANESE(
        displayName = "Japan",
        currencySymbol = "¥",
        currencyPosition = CurrencyPosition.BEFORE,
        dateFormat = "yyyy/MM/dd",
        thousandsSeparator = ',',
        decimalSeparator = '.'
    )
}

/**
 * Enum for color scheme selection - 6 professional palettes for invoice design.
 *
 * Each color scheme defines primary colors, accents, and styling guidelines
 * that are applied via CSS variable overrides to match the selected palette.
 */
enum class ColorScheme(val emoji: String, val displayName: String, val description: String, val primaryHex: String, val accentHex: String) {
    PROFESSIONAL("💼", "Professional", "Navy & gold formal business palette - corporate and trustworthy", "#003366", "#FFC107"),
    VIBRANT("🎨", "Vibrant", "Purple & orange dynamic palette - creative and energetic", "#6B4C9A", "#FF9F43"),
    MINIMAL("⚪", "Minimal", "Grayscale clean palette - modern and sophisticated", "#1a1a1a", "#666666"),
    WARM("🌅", "Warm", "Amber & brown friendly palette - approachable and welcoming", "#D97706", "#78350F"),
    TECH("💻", "Tech", "Deep blue & cyan modern palette - innovative and forward-thinking", "#0F172A", "#06B6D4"),
    NATURE("🌿", "Nature", "Green & earth-tone palette - sustainable and natural feeling", "#15803D", "#92400E")
}

/**
 * Enum for spacing profile - controls whitespace and padding ratios throughout invoice.
 *
 * Each profile defines standard padding, line-height, and gap ratios
 * applied consistently across all layout elements.
 */
enum class SpacingProfile(val emoji: String, val displayName: String, val description: String) {
    TIGHT("📍", "Tight", "Compact spacing - fits maximum items per page"),
    NORMAL("📄", "Normal", "Standard spacing - balanced and readable"),
    GENEROUS("📐", "Generous", "Extra spacing - spacious and premium feel"),
    PREMIUM("👑", "Premium", "Luxury spacing - large gaps between sections")
}

/**
 * Data class for visual accents - boolean toggles for PDF styling enhancements.
 *
 * These modifiers are applied as CSS classes/overrides to customize
 * borders, shadows, dividers, highlights, and gradients across the invoice.
 */
data class VisualAccents(
    val showBorders: Boolean = true,           // Show/hide borders around sections
    val showShadows: Boolean = true,           // Show/hide drop shadows on cards
    val showDividers: Boolean = true,          // Show/hide horizontal divider lines
    val highlightTotals: Boolean = true,       // Highlight totals section with accent color
    val useGradients: Boolean = false          // Use gradient backgrounds on headers
) {
    companion object {
        fun default() = VisualAccents()
    }

    fun toJsonString(): String {
        return """{"showBorders":$showBorders,"showShadows":$showShadows,"showDividers":$showDividers,"highlightTotals":$highlightTotals,"useGradients":$useGradients}"""
    }

    fun toMap(): Map<String, Boolean> {
        return mapOf(
            "showBorders" to showBorders,
            "showShadows" to showShadows,
            "showDividers" to showDividers,
            "highlightTotals" to highlightTotals,
            "useGradients" to useGradients
        )
    }
}

/**
 * Currency symbol position (before or after amount).
 */
enum class CurrencyPosition {
    BEFORE,  // $ 1,234.56
    AFTER    // 1.234,56 €
}

/**
 * Divider style options for section separators.
 * SOLID: Solid line
 * DASHED: Dashed pattern
 * DOTTED: Dotted pattern
 */
enum class DividerStyle {
    SOLID, DASHED, DOTTED
}

/**
 * Total box visual style options.
 * SUBTLE_BACKGROUND: Light background color
 * PROMINENT_BORDER: Colored border with no fill
 * ACCENT_BORDER: Colored border with no fill
 * BOLD_HIGHLIGHT: Strong colored background
 * GRADIENT_BACKGROUND: Gradient fill matching primary/accent
 */
enum class TotalBoxStyle {
    SUBTLE_BACKGROUND, PROMINENT_BORDER, ACCENT_BORDER, BOLD_HIGHLIGHT, GRADIENT_BACKGROUND
}

/**
 * Badge style options for invoice status (PAID, DUE, OVERDUE, etc).
 * ROUNDED_FILLED: Rounded rectangle with solid fill
 * ROUNDED_OUTLINE: Rounded rectangle with border only
 * CIRCULAR: Circular badge with centered text
 * BADGE_WITH_ICON: Badge with icon and text side-by-side
 */
enum class BadgeStyle {
    ROUNDED_FILLED, ROUNDED_OUTLINE, CIRCULAR, BADGE_WITH_ICON
}

/**
 * Background pattern options for subtle visual texture.
 * WAVES: Subtle wave pattern
 * DOTS: Dot grid pattern
 * GRID: Fine grid pattern
 * STRIPES: Diagonal stripe pattern
 * NONE: No pattern
 */
enum class BackgroundPattern {
    WAVES, DOTS, GRID, STRIPES, NONE
}
