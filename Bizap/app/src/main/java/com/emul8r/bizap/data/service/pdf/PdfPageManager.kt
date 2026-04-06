package com.emul8r.bizap.data.service.pdf

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import timber.log.Timber

/**
 * Manages pagination logic for PDF documents.
 *
 * Tracks current Y position and handles page breaks when content exceeds available space.
 * **Critical for preventing content overflow on invoices with many line items.**
 */
class PdfPageManager(
    private val pdfDocument: PdfDocument,
    private val pageWidth: Int = 595,
    private val pageHeight: Int = 842
) {
    companion object {
        private const val TAG = "PdfPageManager"
    }

    private val topMargin = 40f
    private val bottomMargin = 40f
    private val availableHeight = pageHeight - topMargin - bottomMargin

    var currentY = topMargin
        private set

    var currentPageIndex = 0
        private set

    private var currentPage: PdfDocument.Page? = null
    private var currentCanvas: Canvas? = null

    /**
     * Starts a new page and returns the canvas for drawing.
     */
    fun startNewPage(): Canvas {
        finishCurrentPage()
        currentPageIndex++

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageIndex).create()
        currentPage = pdfDocument.startPage(pageInfo)
        currentCanvas = currentPage!!.canvas
        currentY = topMargin

        return currentCanvas!!
    }

    /**
     * Returns the current canvas for drawing. If no page is active, starts a new one.
     */
    fun getCanvas(): Canvas {
        if (currentCanvas == null) {
            return startNewPage()
        }
        return currentCanvas!!
    }

    /**
     * Checks if adding content of the given height would exceed the current page.
     * If it would, triggers a page break automatically.
     *
     * @param contentHeight The height of the content to be added
     * @return The canvas to draw on (might be a new page)
     */
    fun ensureSpace(contentHeight: Float): Canvas {
        if (currentY + contentHeight > (pageHeight - bottomMargin)) {
            return startNewPage()
        }
        return getCanvas()
    }

    /**
     * Advances the Y position by the given amount.
     */
    fun advanceY(height: Float) {
        currentY += height
    }

    /**
     * Sets the Y position directly (useful for manual positioning).
     */
    fun setY(y: Float) {
        currentY = y
    }

    /**
     * Finishes the current page (if any).
     */
    private fun finishCurrentPage() {
        if (currentPage != null) {
            try {
                pdfDocument.finishPage(currentPage!!)
            } catch (e: IllegalStateException) {
                // Document was already closed - this can happen in finalizers
                Timber.w("$TAG: Page already finished or document closed: ${e.message}")
            }
        }
    }

    /**
     * Finalizes all pages and closes the document.
     * Call this when PDF generation is complete.
     */
    fun finalize() {
        finishCurrentPage()
    }

    /**
     * Gets the total number of pages in the document.
     */
    fun getTotalPages(): Int = currentPageIndex
}

