package com.emul8r.bizap.domain.pdf

/**
 * ============================================================================
 * PHASE 1: INVOICE SPACING CONFIGURATION
 * ============================================================================
 *
 * Centralized spacing and layout constants for professional invoice PDF design.
 * Used by BOTH Canvas and HTML-to-PDF rendering paths to ensure consistency.
 *
 * GRID SYSTEM: 8px base unit
 * MARGINS: 15mm (standard PDF margins, ≈ 42px)
 * SECTION GAPS: 12px maximum (no arbitrary spacing)
 *
 * This object eliminates scattered pixel values throughout the codebase.
 * All spacing is now systematic, measurable, and proportional.
 *
 * SUCCESS CRITERIA:
 * - ✅ No hardcoded pixel values in rendering code
 * - ✅ Consistent spacing across Canvas and HTML themes
 * - ✅ 85%+ page coverage (not 40-50%)
 * - ✅ 12px max gaps between sections (no >20px whitespace)
 */
object InvoiceSpacingConfig {

    // ==================== GRID SYSTEM ====================
    /** Base grid unit for all positioning */
    const val GRID_UNIT = 8f

    /** Conversion factor: millimeters to pixels (A4 standard) */
    const val MM_TO_PX = 2.834f

    // ==================== PAGE DIMENSIONS ====================
    /** A4 page width in pixels (210mm) */
    const val PAGE_WIDTH = 595f

    /** A4 page height in pixels (297mm) */
    const val PAGE_HEIGHT = 842f

    // ==================== MARGINS (in mm, converted to px) ====================
    /** Left margin: 15mm ≈ 42.5px */
    const val MARGIN_LEFT_MM = 15f
    const val MARGIN_LEFT = MARGIN_LEFT_MM * MM_TO_PX

    /** Right margin: 15mm ≈ 42.5px */
    const val MARGIN_RIGHT_MM = 15f
    const val MARGIN_RIGHT = MARGIN_RIGHT_MM * MM_TO_PX

    /** Top margin: 12mm ≈ 34px */
    const val MARGIN_TOP_MM = 12f
    const val MARGIN_TOP = MARGIN_TOP_MM * MM_TO_PX

    /** Bottom margin: 10mm ≈ 28.3px */
    const val MARGIN_BOTTOM_MM = 10f
    const val MARGIN_BOTTOM = MARGIN_BOTTOM_MM * MM_TO_PX

    // ==================== VERTICAL SPACING ====================
    /** Gap between major sections (max 12px) */
    const val SECTION_GAP = 12f

    /** Gap between minor sections (8px) */
    const val SUBSECTION_GAP = 8f

    /** Gap between individual lines (4px) */
    const val LINE_SPACING = 4f

    // ==================== COMPONENT HEIGHTS ====================
    /**
     * HEADER SECTION: 60px (compressed from 100px)
     * Contains: Business name + INVOICE label + ABN/phone/email
     * Integrated visual design with accent bar
     */
    const val HEADER_HEIGHT = 60f

    /**
     * SUBHEADER SECTION: 20px
     * Contains: Business details (ABN, phone, email)
     * Integrated with header for visual continuity
     */
    const val SUBHEADER_HEIGHT = 20f

    /**
     * BILL TO SECTION: 80px (compact, was scattered before)
     * Contains: Customer name, address, email, phone
     * Side-by-side with Invoice Details in HIGH DENSITY zone
     */
    const val BILL_TO_HEIGHT = 80f

    /**
     * INVOICE DETAILS SECTION: 80px (was separate, now integrated)
     * Contains: Invoice number, date, due date, status
     * Side-by-side with Bill To for dense layout
     */
    const val INVOICE_DETAILS_HEIGHT = 80f

    /**
     * ITEMS TABLE ROW HEIGHT: 28px
     * Ensures readable but compact table layout
     * With 1px borders = 29px effective row height
     */
    const val TABLE_ROW_HEIGHT = 28f

    /**
     * TABLE HEADER HEIGHT: 32px
     * Bold typography, colored background
     * WCAG AA contrast compliance
     */
    const val TABLE_HEADER_HEIGHT = 32f

    /**
     * TOTALS SECTION: 40px (integrated, no floating box)
     * HIGH VISUAL FOCUS - prominent typography
     * Layout: Subtotal → Tax → TOTAL DUE (bold, large)
     * No separate box = cleaner, more professional
     */
    const val TOTALS_HEIGHT = 40f

    /**
     * PAYMENT DETAILS SECTION: variable height
     * Contains: Payment terms, bank info, payment reference
     * Only shown if configured
     */
    const val PAYMENT_SECTION_ROW_HEIGHT = 16f

    /**
     * FOOTER SECTION: 40px
     * Contains: Thank you message, QR code (optional)
     * Gradient or accent line for visual closure
     */
    const val FOOTER_HEIGHT = 40f

    // ==================== HORIZONTAL SPACING ====================
    /** Spacing between table columns */
    const val COL_SPACING = 8f

    /** Horizontal padding inside components */
    const val PADDING_H = 12f

    /** Vertical padding inside components */
    const val PADDING_V = 8f

    /** Gap between label and value (e.g., "TOTAL DUE" and "$1,100.00") */
    const val LABEL_VALUE_GAP = 6f

    // ==================== TEXT SPACING ====================
    /** Space above text in containers */
    const val TEXT_TOP_PADDING = 4f

    /** Space below text in containers */
    const val TEXT_BOTTOM_PADDING = 4f

    // ==================== TYPOGRAPHY SIZES (pixels) ====================
    /** Header text: Company name, "INVOICE" label */
    const val TEXT_SIZE_HEADER = 18f

    /** Section headers: "BILL TO", "INVOICE DETAILS" */
    const val TEXT_SIZE_SECTION_HEADER = 11f

    /** Body text: Customer details, item descriptions */
    const val TEXT_SIZE_BODY = 10f

    /** Table header and items */
    const val TEXT_SIZE_TABLE = 10f

    /** Small labels and details */
    const val TEXT_SIZE_SMALL = 9f

    /** Totals amount (LARGE and BOLD for prominence) */
    const val TEXT_SIZE_TOTAL_AMOUNT = 16f

    /** Total label */
    const val TEXT_SIZE_TOTAL_LABEL = 11f

    // ==================== CALCULATED DIMENSIONS ====================
    /**
     * Get usable content width (page width minus left and right margins)
     * Used for all content layout
     */
    fun getContentWidth(): Float = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

    /**
     * Get usable content height (page height minus top and bottom margins)
     * Used to determine available space for content
     */
    fun getContentHeight(): Float = PAGE_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM

    /**
     * Calculate two-column layout width (for Bill To and Invoice Details side-by-side)
     * Returns width for each column in a 2-column layout
     */
    fun getTwoColumnWidth(): Float = (getContentWidth() / 2f) - (SUBSECTION_GAP / 2f)

    // ==================== DENSITY ZONES ====================
    /**
     * HIGH DENSITY: Tight spacing for essential info (header, Bill To)
     * Maximizes information density while maintaining readability
     */
    const val DENSITY_HIGH_GAP = 6f

    /**
     * MEDIUM DENSITY: Readable but compact (items table, totals)
     * Balanced between density and visual breathing room
     */
    const val DENSITY_MEDIUM_GAP = 12f

    /**
     * LOW DENSITY: Breathing room for focal points (totals box, footer)
     * Creates visual emphasis through intentional whitespace
     */
    const val DENSITY_LOW_GAP = 20f

    // ==================== ACCENT & STYLING ====================
    /** Accent bar width (left side of cards) */
    const val ACCENT_BAR_WIDTH = 4f

    /** Border width for cards and tables */
    const val BORDER_WIDTH = 0.8f

    /** Shadow offset for depth effect */
    const val SHADOW_OFFSET = 2f

    /** Rounded corner radius for cards */
    const val CORNER_RADIUS = 8f

    // ==================== HELPER FUNCTIONS ====================

    /**
     * ✅ PHASE 2 FEATURE #4: SPACING PROFILES
     * Apply spacing multiplier based on selected profile
     * Returns a multiplier (0.75x for TIGHT, 1.0x for NORMAL, 1.25x for GENEROUS, 1.5x for PREMIUM)
     */
    fun getSpacingMultiplier(spacingProfile: com.emul8r.bizap.domain.model.SpacingProfile): Float = when (spacingProfile) {
        com.emul8r.bizap.domain.model.SpacingProfile.TIGHT -> 0.75f       // Compact: 75% of standard spacing
        com.emul8r.bizap.domain.model.SpacingProfile.NORMAL -> 1.0f       // Standard: 100% (default)
        com.emul8r.bizap.domain.model.SpacingProfile.GENEROUS -> 1.25f    // Spacious: 125% of standard
        com.emul8r.bizap.domain.model.SpacingProfile.PREMIUM -> 1.5f      // Luxury: 150% of standard
    }


    /**
     * Get total height needed for header sections (header + bill to/invoice details)
     * @return Combined height
     */
    fun getHeaderBlockHeight(): Float =
        HEADER_HEIGHT + SECTION_GAP + maxOf(BILL_TO_HEIGHT, INVOICE_DETAILS_HEIGHT)

    /**
     * Calculate space remaining on page for content
     * @param currentY Current Y position on page
     * @return Available height remaining
     */
    fun getAvailableHeight(currentY: Float): Float =
        PAGE_HEIGHT - MARGIN_BOTTOM - currentY
}


