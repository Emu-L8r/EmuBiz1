package com.emul8r.bizap.data.service.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlin.math.ceil

/**
 * FAIL-PROOF PDF TABLE ENGINE
 * Handles multi-line text wrapping and deterministic column alignment.
 *
 * **Improvements:**
 * - Zebra striping (alternating row background colors) for better readability
 * - Theme color support for headers
 * - Minimum row height enforcement
 * - Better line spacing and padding
 */
class PdfTableRenderer(
    private val canvas: Canvas,
    private val startX: Float,
    private var currentY: Float,
    private val pageWidth: Float,
    private val columnWeights: List<Float>, // e.g., listOf(0.5f, 0.15f, 0.15f, 0.2f)
    private val headerBackgroundColor: Int = Color.parseColor("#E8E8E8"),  // Light gray
    private val alternateRowColor: Int = Color.parseColor("#F9F9F9")      // Very light gray
) {
    private val margin = 40f
    private val padding = 10f
    private val tableWidth = pageWidth - (margin * 2)
    private val minRowHeight = 25f
    private var rowCount = 0

    fun drawRow(
        values: List<String>,
        basePaint: Paint,
        isHeader: Boolean = false,
        headerTextColor: Int? = null
    ): Float {
        val textPaint = TextPaint(basePaint)
        
        // ...existing code...
        val layouts = values.mapIndexed { index, text ->
            val colWidth = columnWeights[index] * tableWidth
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, (colWidth - padding * 2).toInt())
                .setAlignment(if (index == 0) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE)
                .setLineSpacing(0f, 1.15f)
                .build()
        }

        val maxHeight = layouts.maxOf { it.height }.toFloat()
        val rowHeight = maxOf(minRowHeight, maxHeight + (padding * 2))

        // Draw background with alternating colors (zebra striping)
        val backgroundColor = if (isHeader) {
            headerBackgroundColor
        } else if (rowCount % 2 == 0) {
            Color.WHITE
        } else {
            alternateRowColor
        }

        val bgPaint = Paint().apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(startX, currentY, startX + tableWidth, currentY + rowHeight, bgPaint)

        // Render cells with optional header text color
        val cellTextPaint = if (isHeader && headerTextColor != null) {
            TextPaint(textPaint).apply { color = headerTextColor }
        } else {
            textPaint
        }

        var xOffset = startX
        layouts.forEachIndexed { index, layout ->
            val colWidth = columnWeights[index] * tableWidth
            canvas.save()
            val xPos = if (index == 0) xOffset + padding else xOffset + colWidth - layout.width - padding
            canvas.translate(xPos, currentY + padding)
            layout.draw(canvas)
            canvas.restore()
            xOffset += colWidth
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.5f
        }
        canvas.drawLine(startX, currentY + rowHeight, startX + tableWidth, currentY + rowHeight, linePaint)

        val heightOccupied = rowHeight
        currentY += heightOccupied
        rowCount++
        return heightOccupied
    }

    /**
     * Draw vertical column separators for improved table readability
     */
    fun drawColumnSeparators(
        canvas: Canvas,
        rowHeight: Float,
        startingY: Float,
        columnSeparatorPaint: Paint
    ) {
        var xOffset = startX

        // Draw vertical lines between columns
        for (index in 0 until columnWeights.size - 1) {
            val colWidth = columnWeights[index] * tableWidth
            xOffset += colWidth
            canvas.drawLine(xOffset, startingY, xOffset, startingY + rowHeight, columnSeparatorPaint)
        }
    }

    fun getPosition(): Float = currentY

    /**
     * Resets row count (call this if you're starting a new table on a new page).
     */
    fun resetRowCount() {
        rowCount = 0
    }
}
