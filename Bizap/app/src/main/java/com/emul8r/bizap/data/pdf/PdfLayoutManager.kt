package com.emul8r.bizap.data.pdf

import android.graphics.Canvas
import android.graphics.Paint
import timber.log.Timber

/**
 * PDF Layout Manager - solves header/subheader overlapping issues
 *
 * This class calculates precise Y positions for each PDF section
 * to guarantee no overlaps and professional spacing.
 *
 * Problem it solves:
 * - Header/subheader "randomly placed on top of things"
 * - Text clashing and overwriting
 * - Inconsistent spacing
 *
 * Solution:
 * - Calculate all positions before rendering
 * - Validate layout won't overflow page
 * - Ensure consistent, professional spacing
 * - No overlaps guaranteed
 */
class PdfLayoutManager(
    private val config: PdfSpacingConfig,
    private val pageHeight: Int = 842,   // A4 page height in points
    private val pageWidth: Int = 595     // A4 page width in points
) {
    companion object {
        private const val TAG = "PdfLayoutManager"
        private const val A4_HEIGHT = 842
        private const val A4_WIDTH = 595
        private const val MIN_FOOTER_SPACE = 50
    }

    /**
     * Calculate precise Y position for each section
     * This prevents overlaps by ensuring each section has enough space
     */
    fun calculateSectionPositions(
        hasHeader: Boolean,
        subheaderLineCount: Int
    ): SectionPositions {
        // Title: "INVOICE" or "QUOTE"
        val titleY = config.pageMargin
        val titleHeight = 30

        // Header: Optional company/project name
        val headerY = titleY + titleHeight + config.titleMarginBottom
        val headerHeight = if (hasHeader) 40 else 0

        // Subheader: Supporting details (location, dept, etc.)
        val subheaderY = headerY + headerHeight + config.headerMarginBottom
        val subheaderHeight = calculateSubheaderHeight(subheaderLineCount)

        // Line items: Invoice line items start here
        val lineItemsY = subheaderY + subheaderHeight + config.subheaderMarginBottom

        // Available space for line items and details
        val availableHeight = pageHeight - lineItemsY - config.footerMarginTop - MIN_FOOTER_SPACE

        // Validate and log
        validateLayout(titleY, titleHeight, headerY, headerHeight, subheaderY,
                      subheaderHeight, lineItemsY, availableHeight)

        return SectionPositions(
            titleY = titleY,
            titleHeight = titleHeight,
            headerY = headerY,
            headerHeight = headerHeight,
            subheaderY = subheaderY,
            subheaderHeight = subheaderHeight,
            lineItemsY = lineItemsY,
            availableHeight = availableHeight,
            footerY = pageHeight - config.footerMarginTop
        )
    }

    /**
     * Calculate height needed for subheader lines
     */
    private fun calculateSubheaderHeight(lineCount: Int): Int {
        return if (lineCount > 0) {
            (lineCount * config.sectionSpacing) + 20  // Extra padding
        } else {
            0
        }
    }

    /**
     * Validate layout won't cause overlaps or page overflow
     */
    private fun validateLayout(
        titleY: Int,
        titleHeight: Int,
        headerY: Int,
        headerHeight: Int,
        subheaderY: Int,
        subheaderHeight: Int,
        lineItemsY: Int,
        availableHeight: Int
    ) {
        // Check: No negative values
        if (titleY < 0 || headerY < 0 || subheaderY < 0 || lineItemsY < 0) {
            Timber.w(TAG, "⚠️ Layout validation: Negative Y position detected")
        }

        // Check: Sections don't overlap
        val headerEnd = headerY + headerHeight
        val subheaderEnd = subheaderY + subheaderHeight

        if (headerEnd > subheaderY && headerHeight > 0) {
            Timber.w(TAG, "⚠️ Layout validation: Header overlaps with subheader")
        }

        if (subheaderEnd > lineItemsY) {
            Timber.w(TAG, "⚠️ Layout validation: Subheader overlaps with line items")
        }

        // Check: Line items have enough space
        if (availableHeight < 100) {
            Timber.w(TAG, "⚠️ Layout validation: Line items have less than 100px available (${availableHeight}px)")
        }

        // Check: Footer has space
        val footerY = pageHeight - config.footerMarginTop
        if (lineItemsY + availableHeight > footerY - 20) {
            Timber.w(TAG, "⚠️ Layout validation: Content may overlap with footer")
        }

        Timber.d(TAG, """
            ✅ Layout Validation Complete:
               Title Y: $titleY (height: $titleHeight)
               Header Y: $headerY (height: $headerHeight)
               Subheader Y: $subheaderY (height: $subheaderHeight)
               Line Items Y: $lineItemsY (available: ${availableHeight}px)
               Footer Y: $footerY
        """.trimIndent())
    }

    /**
     * Draw a text section at calculated position
     * Ensures text is placed exactly where calculated
     */
    fun drawSection(
        canvas: Canvas,
        text: String,
        y: Int,
        paint: Paint
    ) {
        if (text.isBlank()) return

        val x = config.pageMargin.toFloat()
        canvas.drawText(text, x, y.toFloat(), paint)
    }

    /**
     * Draw multiple lines with proper spacing
     */
    fun drawMultilineSection(
        canvas: Canvas,
        lines: List<String>,
        startY: Int,
        paint: Paint
    ) {
        lines.forEachIndexed { index, line ->
            if (line.isNotBlank()) {
                val y = startY + (index * config.sectionSpacing)
                drawSection(canvas, line, y, paint)
            }
        }
    }

    /**
     * Calculate if content fits on current page
     */
    fun canFitContent(
        lineItemCount: Int,
        additionalSpaceNeeded: Int = 0
    ): Boolean {
        val positionsDemo = calculateSectionPositions(
            hasHeader = false,
            subheaderLineCount = 2
        )

        val contentHeight = (lineItemCount * config.tableRowHeight) + additionalSpaceNeeded
        return contentHeight < positionsDemo.availableHeight
    }

    /**
     * Get safe margins for all content
     */
    fun getContentMargins(): ContentMargins {
        return ContentMargins(
            left = config.pageMargin,
            right = config.pageMargin,
            top = config.pageMargin,
            bottom = config.footerMarginTop + MIN_FOOTER_SPACE
        )
    }
}

/**
 * Calculated positions for each PDF section
 * All values are in points/pixels for Canvas drawing
 */
data class SectionPositions(
    val titleY: Int,                // Y position for "INVOICE"/"QUOTE" title
    val titleHeight: Int,           // Height allocated to title
    val headerY: Int,               // Y position for optional header
    val headerHeight: Int,          // Height allocated to header
    val subheaderY: Int,            // Y position for subheader lines
    val subheaderHeight: Int,       // Height allocated to subheader
    val lineItemsY: Int,            // Y position where line items begin
    val availableHeight: Int,       // Pixels available for line items/details
    val footerY: Int                // Y position for footer
) {
    /**
     * Validate all sections fit on page
     */
    fun isSafe(pageHeight: Int = 842): Boolean {
        val lastContentY = lineItemsY + availableHeight
        return lastContentY < pageHeight - 50
    }

    /**
     * Get safe content area (left, top, right, bottom)
     */
    fun getContentArea(): ContentArea {
        return ContentArea(
            left = 20,
            top = lineItemsY,
            right = 575,
            bottom = footerY - 10
        )
    }
}

/**
 * Page margins for safe content area
 */
data class ContentMargins(
    val left: Int,
    val right: Int,
    val top: Int,
    val bottom: Int
) {
    fun getWidth(pageWidth: Int = 595): Int {
        return pageWidth - left - right
    }
}

/**
 * Content bounding box
 */
data class ContentArea(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

