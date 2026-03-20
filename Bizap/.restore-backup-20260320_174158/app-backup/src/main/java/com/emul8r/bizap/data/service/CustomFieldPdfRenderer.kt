package com.emul8r.bizap.data.service

import android.graphics.Canvas
import android.graphics.Paint
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat

/**
 * Renders custom fields section in PDF invoice
 */
class CustomFieldPdfRenderer(
    private val canvas: Canvas,
    private val startY: Float,
    private val pageWidth: Float = 595f,
    private val colors: PdfColors,
    private val headerPaint: Paint,
    private val bodyPaint: Paint
) {

    companion object {
        private const val TAG = "CustomFieldPdfRenderer"
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 16f  // ✅ Increased from 15f for better spacing
        private const val SECTION_SPACING = 15f  // ✅ Increased from 10f
        private const val MAX_WIDTH = 515f  // pageWidth - (margin * 2) = 595 - 80
    }

    private var currentY = startY

    /**
     * Render custom fields section with type-aware formatting
     * ✅ FIX: Now handles text wrapping for long values
     */
    fun renderCustomFields(
        fieldLabels: Map<String, String>,
        fieldValues: Map<String, String>,
        fieldTypes: Map<String, String> = emptyMap()
    ): Float {
        if (fieldLabels.isEmpty() || fieldValues.isEmpty()) {
            return currentY
        }

        Timber.d("✅ Rendering ${fieldLabels.size} custom fields")

        // Draw section header with proper spacing
        currentY += SECTION_SPACING
        headerPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("ADDITIONAL INFORMATION", MARGIN, currentY, headerPaint)
        currentY += LINE_HEIGHT + 5f

        // Draw divider
        val dividerPaint = Paint(bodyPaint).apply {
            strokeWidth = 1f
            color = colors.secondary
        }
        canvas.drawLine(MARGIN, currentY, pageWidth - MARGIN, currentY, dividerPaint)
        currentY += SECTION_SPACING

        // Render each field with text wrapping support
        bodyPaint.textAlign = Paint.Align.LEFT
        fieldLabels.forEach { (fieldId, label) ->
            val value = fieldValues[fieldId] ?: ""
            val type = fieldTypes[fieldId] ?: "TEXT"

            if (value.isNotEmpty()) {
                val formattedValue = formatFieldValue(value, type)
                val displayText = "$label: $formattedValue"

                // ✅ FIX: Wrap long text instead of letting it overflow
                val wrappedLines = wrapText(displayText)
                wrappedLines.forEach { line ->
                    canvas.drawText(line, MARGIN, currentY, bodyPaint)
                    currentY += LINE_HEIGHT
                }

                // Add spacing between fields
                currentY += 5f
            }
        }

        currentY += SECTION_SPACING
        return currentY
    }

    /**
     * ✅ NEW METHOD: Wrap text to fit within page width
     */
    private fun wrapText(text: String): List<String> {
        if (text.isEmpty()) return emptyList()

        val result = mutableListOf<String>()
        var currentLine = ""
        val words = text.split(" ")

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = bodyPaint.measureText(testLine)

            if (width > MAX_WIDTH) {
                if (currentLine.isNotEmpty()) {
                    result.add(currentLine)
                    currentLine = word
                } else {
                    // Word is longer than max width, add it anyway
                    result.add(word)
                    currentLine = ""
                }
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) {
            result.add(currentLine)
        }

        return result
    }

    /**
     * Format field value based on type
     */
    private fun formatFieldValue(value: String, type: String): String {
        return try {
            when (type) {
                "TEXT" -> value
                "NUMBER" -> formatNumber(value)
                "DATE" -> formatDate(value)
                else -> value
            }
        } catch (e: Exception) {
            Timber.w(e, "Error formatting field value: $value (type: $type)")
            value
        }
    }

    /**
     * Format number with thousand separators
     */
    private fun formatNumber(numberString: String): String {
        return try {
            val number = numberString.toDouble()
            val formatter = DecimalFormat("#,##0.##")
            formatter.format(number)
        } catch (e: Exception) {
            numberString
        }
    }

    /**
     * Format date to readable format (MMM DD, YYYY)
     */
    private fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            Timber.w(e, "Could not format date: $dateString")
            dateString
        }
    }

    /**
     * Get current Y position
     */
    fun getPosition(): Float = currentY

    /**
     * Render single custom field (utility method)
     */
    fun renderSingleField(label: String, value: String, type: String = "TEXT") {
        if (value.isEmpty()) return

        bodyPaint.textAlign = Paint.Align.LEFT
        val formattedValue = formatFieldValue(value, type)
        canvas.drawText("$label: $formattedValue", MARGIN, currentY, bodyPaint)
        currentY += LINE_HEIGHT
    }
}

