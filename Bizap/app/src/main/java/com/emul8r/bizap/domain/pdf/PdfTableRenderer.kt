package com.emul8r.bizap.domain.pdf

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlin.math.ceil

/**
 * FAIL-PROOF PDF TABLE ENGINE
 * Handles multi-line text wrapping and deterministic column alignment.
 */
class PdfTableRenderer(
    private val canvas: Canvas,
    private val startX: Float,
    private var currentY: Float,
    private val pageWidth: Float,
    private val columnWeights: List<Float> // e.g., listOf(0.5f, 0.15f, 0.15f, 0.2f)
) {
    private val margin = 40f
    private val padding = 10f
    private val tableWidth = pageWidth - (margin * 2)

    fun drawRow(
        values: List<String>,
        basePaint: Paint,
        isHeader: Boolean = false
    ): Float {
        val textPaint = TextPaint(basePaint)
        
        // 1. Create layouts for each cell to handle wrapping
        val layouts = values.mapIndexed { index, text ->
            val colWidth = columnWeights[index] * tableWidth
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, (colWidth - padding * 2).toInt())
                .setAlignment(if (index == 0) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE)
                .setLineSpacing(0f, 1.1f)
                .build()
        }

        // 2. Calculate row height based on the tallest cell
        val maxHeight = layouts.maxOf { it.height }.toFloat()
        val rowHeight = maxHeight + (padding * 2)

        // 3. Draw background for headers
        if (isHeader) {
            val headerPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#F5F5F5")
                style = Paint.Style.FILL
            }
            canvas.drawRect(startX, currentY, startX + tableWidth, currentY + rowHeight, headerPaint)
        }

        // 4. Render cells
        var xOffset = startX
        layouts.forEachIndexed { index, layout ->
            val colWidth = columnWeights[index] * tableWidth
            canvas.save()
            // Right-align numbers, left-align description
            val xPos = if (index == 0) xOffset + padding else xOffset + colWidth - layout.width - padding
            canvas.translate(xPos, currentY + padding)
            layout.draw(canvas)
            canvas.restore()
            xOffset += colWidth
        }

        // 5. Draw divider line
        val linePaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 0.5f
        }
        canvas.drawLine(startX, currentY + rowHeight, startX + tableWidth, currentY + rowHeight, linePaint)

        val heightOccupied = rowHeight
        currentY += heightOccupied
        return heightOccupied
    }

    fun getPosition(): Float = currentY
}
