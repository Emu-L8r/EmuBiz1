package com.emul8r.bizap.data.service.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

/**
 * Renders watermarks on PDFs based on invoice status.
 *
 * **Features:**
 * - Diagonal "PAID" or "OVERDUE" watermark
 * - Semi-transparent rendering (alpha ~0.15)
 * - Centered and rotated for visual impact
 */
class PdfWatermarkRenderer(
    private val canvas: Canvas,
    private val pageWidth: Float = 595f,
    private val pageHeight: Float = 842f
) {

    companion object {
        private const val TAG = "PdfWatermarkRenderer"
        private const val WATERMARK_TEXT_SIZE = 120f
        private const val WATERMARK_ALPHA = 38  // ~0.15 opacity (38/255)
    }

    /**
     * Draws a status-based watermark on the PDF.
     *
     * @param status The invoice status (e.g., "PAID", "OVERDUE")
     * @param color Optional color; defaults to gray for PAID, red for OVERDUE
     */
    fun drawWatermark(status: String, color: Int? = null) {
        val watermarkText = when {
            status.contains("PAID", ignoreCase = true) -> "PAID"
            status.contains("OVERDUE", ignoreCase = true) -> "OVERDUE"
            else -> null
        }

        if (watermarkText == null) {
            return  // No watermark for other statuses
        }

        val watermarkColor = color ?: when (watermarkText) {
            "PAID" -> Color.GREEN
            "OVERDUE" -> Color.RED
            else -> Color.GRAY
        }

        drawDiagonalWatermark(watermarkText, watermarkColor)
    }

    /**
     * Draws diagonal text across the page with semi-transparent rendering.
     */
    private fun drawDiagonalWatermark(text: String, watermarkColor: Int) {
        canvas.save()

        val paint = Paint().apply {
            textSize = WATERMARK_TEXT_SIZE
            color = watermarkColor
            alpha = WATERMARK_ALPHA
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // Calculate rotation angle and position for diagonal placement
        val centerX = pageWidth / 2
        val centerY = pageHeight / 2
        val angle = -45f  // 45-degree diagonal

        // Translate to center and rotate
        canvas.translate(centerX, centerY)
        canvas.rotate(angle)

        // Draw text centered at origin
        canvas.drawText(text, -paint.measureText(text) / 2, 0f, paint)

        canvas.restore()
    }
}


