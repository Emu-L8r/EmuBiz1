package com.emul8r.bizap.domain.pdf

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

/**
 * Universal Invoice Grid System (UIGS) - April 5, 2026
 *
 * Guarantees visual consistency across ALL invoice templates by defining
 * a single, unified grid system that all HTML and Canvas templates must follow.
 *
 * Philosophy: One grid system, infinite theme possibilities.
 * All templates use the SAME spacing, margins, and layout structure.
 * Only colors, fonts, and decorative elements vary per theme.
 *
 * This solves the "some look good, some look crappy" problem by enforcing
 * professional spacing standards across all invoice designs.
 */
object InvoiceGridSystem {

    // =========================================================================
    // PAGE & DOCUMENT DIMENSIONS (A4 Standard)
    // =========================================================================
    const val PAGE_WIDTH_MM = 210
    const val PAGE_HEIGHT_MM = 297
    const val MARGIN_TOP_MM = 15
    const val MARGIN_BOTTOM_MM = 15
    const val MARGIN_LEFT_MM = 15
    const val MARGIN_RIGHT_MM = 15
    const val CONTENT_WIDTH_MM = PAGE_WIDTH_MM - (MARGIN_LEFT_MM + MARGIN_RIGHT_MM)  // 180mm
    const val CONTENT_WIDTH_PX = 510  // 180mm at 72dpi

    // =========================================================================
    // GRID BASE UNIT (8px = 1 unit, following Material Design 3)
    // =========================================================================
    const val GRID_BASE_UNIT_DP = 8
    val UNIT_1 = 8.dp
    val UNIT_2 = 16.dp
    val UNIT_3 = 24.dp
    val UNIT_4 = 32.dp
    val UNIT_5 = 40.dp
    val UNIT_6 = 48.dp

    // =========================================================================
    // SECTION HEIGHTS (All invoice sections use these)
    // =========================================================================
    val HEADER_HEIGHT = 60.dp          // Company header section
    val SECTION_GAP = 16.dp            // Gap between major sections
    val ITEM_ROW_HEIGHT = 28.dp        // Height of each line item row
    val CARD_HEIGHT = 80.dp            // Bill To/Invoice Details cards
    val FOOTER_HEIGHT = 40.dp          // Footer section

    // =========================================================================
    // PADDING & SPACING (Universal spacing for all templates)
    // =========================================================================
    val PADDING_LARGE = 16.dp          // Main content padding
    val PADDING_MEDIUM = 12.dp         // Section padding
    val PADDING_SMALL = 8.dp           // Item padding
    val GAP_SECTION = 16.dp            // Between major sections
    val GAP_SUBSECTION = 12.dp         // Between subsections
    val GAP_ITEM = 8.dp                // Between items

    // =========================================================================
    // TYPOGRAPHY SCALE (Professional hierarchy)
    // =========================================================================
    val FONT_SIZE_TITLE = 24.sp         // "INVOICE" title
    val FONT_SIZE_HEADING = 14.sp       // Section headers
    val FONT_SIZE_BODY = 11.sp          // Body text
    val FONT_SIZE_SMALL = 9.sp          // Labels, secondary text
    val FONT_SIZE_TINY = 8.sp           // Footer, very small text

    // =========================================================================
    // COLORS (Default theme - can be overridden per template)
    // =========================================================================
    // Primary colors
    val COLOR_PRIMARY = Color(0xFF6B4C9A)              // Purple
    val COLOR_SECONDARY = Color(0xFFFF9F43)            // Orange

    // Text colors
    val COLOR_TEXT_PRIMARY = Color(0xFF000000)         // Black
    val COLOR_TEXT_SECONDARY = Color(0xFF666666)       // Gray
    val COLOR_TEXT_MUTED = Color(0xFF999999)           // Light gray

    // Background colors
    val COLOR_BACKGROUND = Color(0xFFFFFFFF)           // White
    val COLOR_STRIPE_ODD = Color(0xFFFFFFFF)           // Odd rows: white
    val COLOR_STRIPE_EVEN = Color(0xFFF9F9F9)          // Even rows: light gray

    // Border & dividers
    val COLOR_BORDER = Color(0xFFE0E0E0)               // Light gray
    val COLOR_DIVIDER = Color(0xFFE0E0E0)              // Same as border

    // =========================================================================
    // TABLE COLUMN WIDTHS (Items table: 100% distribution)
    // =========================================================================
    const val COL_DESCRIPTION_PCT = 50    // Description: 50%
    const val COL_QUANTITY_PCT = 13       // Quantity: 13%
    const val COL_UNIT_PRICE_PCT = 18     // Unit Price: 18%
    const val COL_AMOUNT_PCT = 19         // Amount: 19%

    // =========================================================================
    // STRIPE PATTERN (Alternating row backgrounds for readability)
    // =========================================================================
    fun getItemRowBackground(index: Int): Color {
        return if (index % 2 == 0) COLOR_STRIPE_ODD else COLOR_STRIPE_EVEN
    }

    // =========================================================================
    // CARD LAYOUT (Bill To & Invoice Details - side by side)
    // =========================================================================
    const val CARD_WIDTH_PERCENT = 48  // Each card: 48% width (with 4% gap between)
    const val CARD_GAP_PERCENT = 4     // Gap between cards

    // =========================================================================
    // BORDERS & DIVIDERS
    // =========================================================================
    const val BORDER_WIDTH_DP = 1
    const val BORDER_RADIUS_DP = 4

    // =========================================================================
    // PROFESSIONAL SPACING RULES (All templates must follow)
    // =========================================================================
    /**
     * Standard spacing between sections.
     * Use this between: Header -> Cards, Cards -> Items Table, Items -> Totals, Totals -> Payment, etc.
     */
    fun getStandardSectionGap(): Int = 16  // pixels

    /**
     * Standard padding inside elements.
     * Use this for: Cards, sections, table cells, etc.
     */
    fun getStandardPadding(): Int = 16  // pixels

    /**
     * Standard item row height for consistency.
     * All items table rows must be exactly this height.
     */
    fun getItemRowHeightPx(): Int = 28  // pixels

    // =========================================================================
    // PAGE LAYOUT CONSTANTS
    // =========================================================================
    /**
     * Total available vertical space for content (excluding margins)
     */
    const val AVAILABLE_HEIGHT_MM = PAGE_HEIGHT_MM - (MARGIN_TOP_MM + MARGIN_BOTTOM_MM)  // 267mm

    /**
     * Safe content area width (excluding margins)
     */
    const val SAFE_CONTENT_WIDTH_MM = CONTENT_WIDTH_MM

    // =========================================================================
    // SUMMARY (For developers implementing new templates)
    // =========================================================================
    /**
     * REQUIRED STRUCTURE for all invoice templates:
     *
     * 1. Header (60px)
     *    - Company name, logo, "INVOICE" label
     *    - Margin: 15mm all sides
     *
     * 2. Bill To & Invoice Details Cards (2 columns, 48% each)
     *    - Gap: 16px between sections
     *    - Padding: 16px inside each card
     *    - Height: ~80px
     *
     * 3. Items Table
     *    - Each row: 28px height
     *    - Column widths: 50% / 13% / 18% / 19%
     *    - Stripe pattern: white / light gray
     *    - Border: 1px #E0E0E0
     *
     * 4. Totals Section
     *    - Subtotal
     *    - Tax
     *    - Total Due (larger, bold)
     *    - Right-aligned
     *    - Gap above: 16px
     *
     * 5. Payment Section (optional)
     *    - Bank name, account, etc.
     *    - Border: left accent border
     *    - Gap above: 16px
     *    - Gap below: 16px
     *
     * 6. Footer (40px)
     *    - Company info, thank you message
     *    - Background: subtle color or gradient
     *    - Margin: 15mm all sides
     *
     * All gaps between sections: 16px
     * All padding inside sections: 16px
     * All margins: 15mm
     * All text follows typography scale
     * All colors are overridable per template
     */
}

