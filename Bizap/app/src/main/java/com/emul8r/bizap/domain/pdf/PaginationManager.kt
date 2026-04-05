package com.emul8r.bizap.domain.pdf

import com.emul8r.bizap.domain.model.InvoiceSnapshot
import timber.log.Timber

/**
 * Intelligent pagination manager for multi-page PDF invoices.
 *
 * Handles:
 * - Optimal page break points (between items, not mid-content)
 * - Balanced page fill (avoid orphaned content)
 * - Repeating headers/footers
 * - Page numbering (Page X of Y)
 * - Dynamic content sizing based on available space
 */
class PaginationManager {

    companion object {
        // Constants for layout calculations
        const val PAGE_HEIGHT_MM = 277  // A4 height minus margins (297 - 20)
        const val HEADER_HEIGHT_MM = 15  // 60px header
        const val FOOTER_HEIGHT_MM = 15  // 40px footer
        const val AVAILABLE_CONTENT_MM = PAGE_HEIGHT_MM - HEADER_HEIGHT_MM - FOOTER_HEIGHT_MM

        // Item row dimensions
        const val ITEM_ROW_HEIGHT_MM = 7   // 28px row height in mm
        const val TOTALS_HEIGHT_MM = 10    // ~40px for totals
        const val TABLE_HEADER_HEIGHT_MM = 8  // Table header row
        const val SECTION_GAP_MM = 3       // Gaps between sections
    }

    /**
     * Calculate optimal page breaks for invoice items.
     *
     * @param itemCount Number of items in invoice
     * @return Pages breakdown: list of item indices for each page
     */
    fun calculatePageBreaks(itemCount: Int): List<List<Int>> {
        val pages = mutableListOf<List<Int>>()
        val itemsPerPage = calculateItemsPerPage()

        Timber.d("📄 Calculating pagination: $itemCount items, ~$itemsPerPage per page")

        var currentPageItems = mutableListOf<Int>()
        for (i in 0 until itemCount) {
            if (currentPageItems.size >= itemsPerPage && currentPageItems.isNotEmpty()) {
                // Start new page
                pages.add(currentPageItems.toList())
                currentPageItems = mutableListOf()
                Timber.d("   → Page ${pages.size}: items ${currentPageItems.firstOrNull() ?: i}-${i-1}")
            }
            currentPageItems.add(i)
        }

        // Add remaining items
        if (currentPageItems.isNotEmpty()) {
            pages.add(currentPageItems.toList())
            Timber.d("   → Page ${pages.size}: items ${currentPageItems.first()}-${currentPageItems.last()}")
        }

        Timber.d("✅ Pagination complete: ${pages.size} page(s)")
        return pages
    }

    /**
     * Calculate how many items fit on a page.
     *
     * @return Maximum items per page
     */
    private fun calculateItemsPerPage(): Int {
        val availableForItems = AVAILABLE_CONTENT_MM -
                TABLE_HEADER_HEIGHT_MM -
                TOTALS_HEIGHT_MM -
                (SECTION_GAP_MM * 2)

        val itemsPerPage = (availableForItems / ITEM_ROW_HEIGHT_MM).toInt()
        return maxOf(1, itemsPerPage)  // At least 1 item per page
    }

    /**
     * Get page number display string.
     *
     * @param currentPage Current page number (1-indexed)
     * @param totalPages Total number of pages
     * @return Display string like "Page 1 of 3"
     */
    fun getPageNumberDisplay(currentPage: Int, totalPages: Int): String {
        return "Page $currentPage of $totalPages"
    }

    /**
     * Check if content should have multiple pages.
     *
     * @param itemCount Number of items
     * @return true if multiple pages needed
     */
    fun needsMultiplePages(itemCount: Int): Boolean {
        return itemCount > calculateItemsPerPage()
    }

    /**
     * Get coverage percentage for a page.
     *
     * @param itemsOnPage Number of items on this page
     * @param isTotalsPage Whether this page has totals section
     * @return Coverage percentage (0-100)
     */
    fun getPageCoverage(itemsOnPage: Int, isTotalsPage: Boolean): Int {
        val contentHeight = (itemsOnPage * ITEM_ROW_HEIGHT_MM) +
                TABLE_HEADER_HEIGHT_MM +
                (if (isTotalsPage) TOTALS_HEIGHT_MM else 0)

        val coverage = (contentHeight / AVAILABLE_CONTENT_MM * 100).toInt()
        return minOf(100, coverage)  // Cap at 100%
    }

    /**
     * Validate pagination balance (avoid orphaned content).
     *
     * @param pages List of item pages
     * @return true if balanced (no single orphaned items)
     */
    fun isBalanced(pages: List<List<Int>>): Boolean {
        if (pages.size <= 1) return true

        // Check if last page has at least 30% capacity
        val lastPageItems = pages.last().size
        val itemsPerPage = calculateItemsPerPage()
        val lastPageCoverage = (lastPageItems.toFloat() / itemsPerPage) * 100

        val balanced = lastPageCoverage >= 30
        Timber.d("${if (balanced) "✅" else "⚠️"} Page balance check: last page ${lastPageCoverage.toInt()}%")

        return balanced
    }

    /**
     * Build page header HTML for repeating header.
     *
     * @param pageNumber Current page number
     * @param totalPages Total pages
     * @param businessName Name for header
     * @return HTML for page header
     */
    fun buildPageHeader(pageNumber: Int, totalPages: Int, businessName: String): String {
        return if (pageNumber > 1) {
            // Repeat header on subsequent pages (minimal version)
            """
            <div class="page-header-repeat" style="border-bottom: 1px solid #E0E0E0; padding-bottom: 8px; margin-bottom: 12px;">
                <div style="font-size: 9pt; color: #666666;">
                    $businessName - ${getPageNumberDisplay(pageNumber, totalPages)}
                </div>
            </div>
            """
        } else {
            ""  // First page doesn't need repeat header
        }
    }

    /**
     * Build page footer with page numbering.
     *
     * @param pageNumber Current page number
     * @param totalPages Total pages
     * @return HTML for page footer
     */
    fun buildPageFooter(pageNumber: Int, totalPages: Int): String {
        return """
        <div class="page-footer" style="text-align: center; font-size: 8pt; color: #999999; margin-top: 12px; border-top: 1px solid #E0E0E0; padding-top: 8px;">
            ${getPageNumberDisplay(pageNumber, totalPages)}
        </div>
        """
    }
}

