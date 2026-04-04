package com.emul8r.bizap.domain.pdf

import timber.log.Timber

/**
 * ============================================================================
 * PHASE 1: GRID LAYOUT MANAGER
 * ============================================================================
 *
 * Provides systematic, grid-based coordinate calculation for PDF positioning.
 * Replaces scattered hardcoded drawText(x, y) coordinates with grid calculations.
 *
 * BENEFITS:
 * - ✅ All positions calculated as multiples of 8px grid unit
 * - ✅ Spacing becomes proportional and scalable
 * - ✅ Future changes are systematic (adjust GRID_UNIT, all positions scale)
 * - ✅ Coordinates are readable and understandable (getY(8) = 64px, not drawText(x, 64f))
 * - ✅ Shared between Canvas and HTML rendering paths
 *
 * USAGE EXAMPLE:
 *   val manager = GridLayoutManager()
 *   val headerY = manager.getHeaderY()           // Top of header (≈34px)
 *   val billToY = manager.getBillToY()           // Below header + gap
 *   val width = manager.getTwoColumnWidth()      // Width for side-by-side layout
 *
 * PREDEFINED SECTIONS:
 * - HEADER (60px): Company name + INVOICE label + business info
 * - BILL TO + INVOICE DETAILS (80px each, side-by-side): Customer and invoice info
 * - ITEMS TABLE: Dynamic height based on item count
 * - TOTALS: Integrated below items (no floating box)
 * - FOOTER: Bottom of page
 */
class GridLayoutManager(
    private val pageWidth: Float = InvoiceSpacingConfig.PAGE_WIDTH,
    private val pageHeight: Float = InvoiceSpacingConfig.PAGE_HEIGHT
) {
    companion object {
        private const val TAG = "GridLayoutManager"
    }

    // Cache spacing values for efficiency
    private val gridUnit = InvoiceSpacingConfig.GRID_UNIT
    private val leftMargin = InvoiceSpacingConfig.MARGIN_LEFT
    private val topMargin = InvoiceSpacingConfig.MARGIN_TOP
    private val rightMargin = InvoiceSpacingConfig.MARGIN_RIGHT
    private val bottomMargin = InvoiceSpacingConfig.MARGIN_BOTTOM

    // ==================== BASIC GRID CALCULATIONS ====================

    /**
     * Convert grid units to X coordinate (horizontal positioning from left margin)
     *
     * @param gridUnits Number of 8px grid units from left margin
     * @return X coordinate in pixels (includes left margin)
     *
     * Example:
     *   getX(0) = leftMargin (42.5px) - left edge of content
     *   getX(10) = leftMargin + 80px = column positioned 80px from left edge
     */
    fun getX(gridUnits: Int): Float {
        val x = leftMargin + (gridUnits * gridUnit)
        Timber.d("getX($gridUnits) = $x px (margin: $leftMargin + units: ${gridUnits * gridUnit})")
        return x
    }

    /**
     * Convert grid units to Y coordinate (vertical positioning from top margin)
     *
     * @param gridUnits Number of 8px grid units from top margin
     * @return Y coordinate in pixels (includes top margin)
     *
     * Example:
     *   getY(0) = topMargin (34px) - top edge of content
     *   getY(8) = topMargin + 64px = 98.3px (8 grid units down)
     */
    fun getY(gridUnits: Int): Float {
        val y = topMargin + (gridUnits * gridUnit)
        Timber.d("getY($gridUnits) = $y px (margin: $topMargin + units: ${gridUnits * gridUnit})")
        return y
    }

    /**
     * Get width based on grid units
     *
     * @param gridUnits Number of 8px grid units
     * @return Width in pixels
     */
    fun getWidth(gridUnits: Int): Float = gridUnits * gridUnit

    /**
     * Get height based on grid units
     *
     * @param gridUnits Number of 8px grid units
     * @return Height in pixels
     */
    fun getHeight(gridUnits: Int): Float = gridUnits * gridUnit

    // ==================== CONTENT DIMENSIONS ====================

    /**
     * Get usable content width (page width minus left/right margins)
     * @return Width available for content
     */
    fun getContentWidth(): Float = pageWidth - leftMargin - rightMargin

    /**
     * Get usable content height (page height minus top/bottom margins)
     * @return Height available for content
     */
    fun getContentHeight(): Float = pageHeight - topMargin - bottomMargin

    /**
     * Get left edge of content (accounting for left margin)
     * @return X coordinate of left edge
     */
    fun getContentLeft(): Float = leftMargin

    /**
     * Get right edge of content (accounting for right margin)
     * @return X coordinate of right edge
     */
    fun getContentRight(): Float = pageWidth - rightMargin

    /**
     * Get top edge of content (accounting for top margin)
     * @return Y coordinate of top edge
     */
    fun getContentTop(): Float = topMargin

    /**
     * Get bottom edge of content (accounting for bottom margin)
     * @return Y coordinate of bottom edge
     */
    fun getContentBottom(): Float = pageHeight - bottomMargin

    // ==================== SECTION POSITIONING ====================

    /**
     * HEADER SECTION (60px height)
     * Contains: Company name + INVOICE label + ABN/phone/email
     * Position: Top of page
     * Design: Integrated accent bar, professional styling
     */
    fun getHeaderY(): Float = getContentTop()

    fun getHeaderHeight(): Float = InvoiceSpacingConfig.HEADER_HEIGHT

    fun getHeaderLeft(): Float = getContentLeft()

    fun getHeaderRight(): Float = getContentRight()

    /**
     * BILL TO + INVOICE DETAILS SECTION
     * Layout: Side-by-side, two columns
     * Height: 80px each
     * Gap: 8px between columns
     * Design: HIGH DENSITY zone with subtle accent bars
     */
    fun getBillToY(): Float {
        val headerBottom = getHeaderY() + getHeaderHeight()
        return headerBottom + InvoiceSpacingConfig.SECTION_GAP
    }

    fun getBillToHeight(): Float = InvoiceSpacingConfig.BILL_TO_HEIGHT

    fun getBillToLeft(): Float = getContentLeft()

    fun getBillToRight(): Float {
        val columnWidth = getTwoColumnWidth()
        return getBillToLeft() + columnWidth
    }

    fun getInvoiceDetailsY(): Float = getBillToY()  // Same Y as Bill To (side-by-side)

    fun getInvoiceDetailsHeight(): Float = InvoiceSpacingConfig.INVOICE_DETAILS_HEIGHT

    fun getInvoiceDetailsLeft(): Float {
        return getBillToRight() + InvoiceSpacingConfig.SUBSECTION_GAP
    }

    fun getInvoiceDetailsRight(): Float = getContentRight()

    /**
     * Calculate width for two-column layout (used for Bill To and Invoice Details)
     * Total width: content width
     * Split into: [column1] [gap] [column2]
     * Each column gets equal width
     */
    fun getTwoColumnWidth(): Float {
        val gapWidth = InvoiceSpacingConfig.SUBSECTION_GAP
        return (getContentWidth() - gapWidth) / 2f
    }

    /**
     * Get Y position of Bill To / Invoice Details bottom edge
     */
    fun getInvoiceHeaderBlockBottom(): Float {
        return getBillToY() + maxOf(getBillToHeight(), getInvoiceDetailsHeight())
    }

    /**
     * ITEMS TABLE SECTION
     * Layout: Full width table with multiple rows
     * Row height: 28px each
     * Design: MEDIUM DENSITY, readable but compact
     */
    fun getItemsTableY(): Float {
        val invoiceBlockBottom = getInvoiceHeaderBlockBottom()
        return invoiceBlockBottom + InvoiceSpacingConfig.SECTION_GAP
    }

    fun getItemsTableLeft(): Float = getContentLeft()

    fun getItemsTableRight(): Float = getContentRight()

    fun getItemsTableWidth(): Float = getItemsTableRight() - getItemsTableLeft()

    fun getItemRowHeight(): Float = InvoiceSpacingConfig.TABLE_ROW_HEIGHT

    fun getTableHeaderHeight(): Float = InvoiceSpacingConfig.TABLE_HEADER_HEIGHT

    /**
     * Calculate Y position for a specific item row in the table
     * @param rowIndex 0-based row index (0 = first item)
     * @return Y coordinate of row top
     */
    fun getItemRowY(rowIndex: Int): Float {
        val headerY = getItemsTableY()
        val headerHeight = getTableHeaderHeight()
        return headerY + headerHeight + (rowIndex * getItemRowHeight())
    }

    /**
     * TOTALS SECTION
     * Layout: Integrated below items table (NO FLOATING BOX)
     * Design: Typography-driven hierarchy, HIGH VISUAL FOCUS
     * Height: 40px with subtotal, tax, and total due
     *
     * NOTE: This is calculated dynamically based on items table height
     * The exact Y position depends on how many items are in the table
     */
    fun getTotalsY(itemCount: Int): Float {
        val tableY = getItemsTableY()
        val headerHeight = getTableHeaderHeight()
        val itemsHeight = itemCount * getItemRowHeight()
        return tableY + headerHeight + itemsHeight + InvoiceSpacingConfig.SECTION_GAP
    }

    fun getTotalsHeight(): Float = InvoiceSpacingConfig.TOTALS_HEIGHT

    fun getTotalsLeft(): Float = getContentLeft()

    fun getTotalsRight(): Float = getContentRight()

    fun getTotalsWidth(): Float = getTotalsRight() - getTotalsLeft()

    /**
     * PAYMENT DETAILS SECTION (Optional)
     * Only shown if payment details are configured
     */
    fun getPaymentSectionY(itemCount: Int): Float {
        val totalsY = getTotalsY(itemCount)
        val totalsHeight = getTotalsHeight()
        return totalsY + totalsHeight + InvoiceSpacingConfig.SECTION_GAP
    }

    fun getPaymentSectionLeft(): Float = getContentLeft()

    fun getPaymentSectionRight(): Float = getContentRight()

    fun getPaymentRowHeight(): Float = InvoiceSpacingConfig.PAYMENT_SECTION_ROW_HEIGHT

    /**
     * FOOTER SECTION
     * Layout: Bottom of page, full width
     * Height: 40px
     * Design: Gradient or accent line for visual closure
     */
    fun getFooterY(): Float {
        return pageHeight - bottomMargin - getFooterHeight()
    }

    fun getFooterHeight(): Float = InvoiceSpacingConfig.FOOTER_HEIGHT

    fun getFooterLeft(): Float = getContentLeft()

    fun getFooterRight(): Float = getContentRight()

    // ==================== UTILITY METHODS ====================

    /**
     * Check if adding content of given height would exceed available page space
     * @param currentY Current Y position
     * @param contentHeight Height of content to be added
     * @return True if would exceed bottom margin
     */
    fun wouldExceedPage(currentY: Float, contentHeight: Float): Boolean {
        val exceeds = (currentY + contentHeight) > (pageHeight - bottomMargin)
        if (exceeds) {
            Timber.d("Content would exceed page: currentY=$currentY + contentHeight=$contentHeight > ${pageHeight - bottomMargin}")
        }
        return exceeds
    }

    /**
     * Round position to nearest grid unit for consistent alignment
     * @param value Position in pixels
     * @return Position rounded to nearest grid unit
     */
    fun roundToGrid(value: Float): Float {
        return (value / gridUnit).toInt() * gridUnit
    }

    /**
     * Calculate available space remaining on page from given Y position
     * @param currentY Current Y position
     * @return Available height (may be negative if past bottom margin)
     */
    fun getAvailableHeight(currentY: Float): Float {
        return pageHeight - bottomMargin - currentY
    }

    /**
     * Get formatted debug info about current layout state
     */
    fun getLayoutInfo(): String {
        return """
            Grid Layout Manager State:
            - Page: ${pageWidth}x${pageHeight}px
            - Content: ${getContentWidth()}x${getContentHeight()}px
            - Margins: L=${leftMargin}px, R=${rightMargin}px, T=${topMargin}px, B=${bottomMargin}px
            - Grid Unit: ${gridUnit}px
            - Header Y: ${getHeaderY()}-${getHeaderY() + getHeaderHeight()}px
            - Bill To Y: ${getBillToY()}-${getInvoiceHeaderBlockBottom()}px
            - Content Width: ${getContentWidth()}px
        """.trimIndent()
    }
}

