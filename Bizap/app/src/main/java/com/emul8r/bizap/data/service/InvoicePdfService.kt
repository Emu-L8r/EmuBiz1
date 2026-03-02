package com.emul8r.bizap.data.service

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.annotation.RequiresApi
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.pdf.PdfTableRenderer
import com.emul8r.bizap.ui.templates.TemplateSnapshotManager
import com.emul8r.bizap.ui.templates.TemplateSnapshot
import com.emul8r.bizap.utils.DocumentNamingUtils
import com.emul8r.bizap.utils.CurrencyFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoicePdfService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository
) {
    companion object {
        private const val TAG = "InvoicePdfService"
        
        // Layout Constants
        private const val PAGE_WIDTH = 595f
        private const val PAGE_HEIGHT = 842f
        private const val MARGIN_X = 40f
        private const val TOP_MARGIN_NORMAL = 30f
        private const val TOP_MARGIN_COMPACT = 15f
        
        // Font Sizes
        private const val FONT_SIZE_TITLE = 22f
        private const val FONT_SIZE_SUBTITLE = 10f
        private const val FONT_SIZE_LABEL = 9f
        private const val FONT_SIZE_HEADER = 10f
        private const val FONT_SIZE_BODY = 10f
        private const val FONT_SIZE_TOTAL = 16f
        
        // Large Presets
        private const val FONT_SIZE_BODY_LARGE = 12f
        private const val FONT_SIZE_TOTAL_LARGE = 20f
    }

    private val pdfStyler = PdfStyler()
    private val snapshotManager = TemplateSnapshotManager()

    suspend fun checkIfPdfExists(invoiceId: Long, fileType: String): Pair<Boolean, String?> {
        val existingDoc = documentRepository.getDocumentByInvoiceAndType(invoiceId, fileType)
        return if (existingDoc != null) Pair(true, existingDoc.fileName) else Pair(false, null)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    suspend fun generateInvoice(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean = true,
        templateSnapshotJson: String? = null,
        customFieldValuesJson: String? = null
    ): File {
        val fileType = if (isQuote) "Quote" else "Invoice"
        val baseFileName = DocumentNamingUtils.generateFileName(
            snapshot.customerName, snapshot.date, snapshot.invoiceId.toInt(), fileType
        )

        val existingDoc = documentRepository.getDocumentByInvoiceAndType(snapshot.invoiceId, fileType)
        val fileName = if (!overwriteExisting && existingDoc != null) {
            generateVersionedFileName(baseFileName)
        } else {
            if (existingDoc != null) File(existingDoc.absolutePath).let { if (it.exists()) it.delete() }
            baseFileName
        }

        val file = File(context.filesDir, "documents/$fileName")
        file.parentFile?.mkdirs()

        val templateSnapshot = snapshotManager.restoreSnapshot(templateSnapshotJson)
        val colors = pdfStyler.extractColors(templateSnapshot)
        
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val boldTypeface = pdfStyler.getTypeface(templateSnapshot?.fontFamily, context, isBold = true)
        val regularTypeface = pdfStyler.getTypeface(templateSnapshot?.fontFamily, context, isBold = false)

        var currentY = if (templateSnapshot?.marginPreset == "COMPACT") TOP_MARGIN_COMPACT else TOP_MARGIN_NORMAL

        // 1. Draw Brand/Company Header
        currentY = drawHeader(canvas, snapshot, templateSnapshot, colors, boldTypeface, regularTypeface, currentY)

        // 2. Draw Bill To & Invoice Metadata
        currentY = drawBillToAndMeta(canvas, snapshot, fileType, colors, boldTypeface, regularTypeface, currentY)

        // 3. Draw Table
        if (!pdfStyler.shouldHideLineItems(templateSnapshot)) {
            currentY = drawTable(canvas, snapshot, templateSnapshot, colors, boldTypeface, regularTypeface, currentY)
        }

        // 4. Draw Totals
        currentY = drawTotals(canvas, snapshot, templateSnapshot, colors, boldTypeface, regularTypeface, currentY)

        // 5. Draw Footer
        drawFooter(canvas, templateSnapshot, colors, regularTypeface)

        pdfDocument.finishPage(page)
        file.outputStream().use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return file
    }

    private fun drawHeader(
        canvas: Canvas,
        snapshot: InvoiceSnapshot,
        template: TemplateSnapshot?,
        colors: PdfColors,
        boldTypeface: Typeface,
        regularTypeface: Typeface,
        startY: Float
    ): Float {
        val brandPaint = Paint().apply {
            typeface = boldTypeface
            textSize = FONT_SIZE_TITLE
            color = colors.primary
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val infoPaint = Paint().apply {
            typeface = regularTypeface
            textSize = FONT_SIZE_SUBTITLE
            color = colors.textLight
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        var y = startY + 20f
        canvas.drawText(snapshot.businessName.uppercase(), PAGE_WIDTH / 2f, y, brandPaint)
        
        y += 20f
        val parts = mutableListOf<String>()
        if (template?.showTaxId != false) parts.add("ABN: ${snapshot.businessAbn}")
        if (template?.showPhone != false) parts.add("Phone: ${snapshot.businessPhone}")
        
        if (parts.isNotEmpty()) {
            canvas.drawText(parts.joinToString(" | "), PAGE_WIDTH / 2f, y, infoPaint)
            y += 15f
        }
        
        val contactParts = mutableListOf<String>()
        if (template?.showEmail != false) contactParts.add(snapshot.businessEmail)
        if (template?.showAddress != false) contactParts.add(snapshot.businessAddress)
        
        if (contactParts.isNotEmpty()) {
            canvas.drawText(contactParts.joinToString(" | "), PAGE_WIDTH / 2f, y, infoPaint)
            y += 15f
        }

        y += 5f
        canvas.drawLine(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y, Paint().apply { color = colors.secondary; strokeWidth = 1.5f })
        
        return y + 30f
    }

    private fun drawBillToAndMeta(
        canvas: Canvas,
        snapshot: InvoiceSnapshot,
        fileType: String,
        colors: PdfColors,
        boldTypeface: Typeface,
        regularTypeface: Typeface,
        startY: Float
    ): Float {
        val labelPaint = Paint().apply { typeface = boldTypeface; textSize = FONT_SIZE_LABEL; color = Color.GRAY; isAntiAlias = true }
        val headerPaint = Paint().apply { typeface = boldTypeface; textSize = FONT_SIZE_HEADER; color = Color.BLACK; isAntiAlias = true }
        val bodyPaint = Paint().apply { typeface = regularTypeface; textSize = FONT_SIZE_BODY; color = Color.BLACK; isAntiAlias = true }

        // Left Column: Bill To
        var leftY = startY
        canvas.drawText("BILL TO:", MARGIN_X, leftY, labelPaint)
        leftY += 15f
        canvas.drawText(snapshot.customerName, MARGIN_X, leftY, headerPaint)
        leftY += 15f
        canvas.drawText(snapshot.customerAddress, MARGIN_X, leftY, bodyPaint)
        snapshot.customerEmail?.let {
            leftY += 15f
            canvas.drawText(it, MARGIN_X, leftY, bodyPaint)
        }

        // Right Column: Metadata
        var rightY = startY
        val rightAlignX = PAGE_WIDTH - MARGIN_X
        labelPaint.textAlign = Paint.Align.RIGHT
        headerPaint.textAlign = Paint.Align.RIGHT
        bodyPaint.textAlign = Paint.Align.RIGHT

        canvas.drawText("${fileType.uppercase()} #:", rightAlignX - 80f, rightY, labelPaint)
        canvas.drawText(snapshot.invoiceNumber, rightAlignX, rightY, headerPaint)
        
        rightY += 20f
        canvas.drawText("DATE:", rightAlignX - 80f, rightY, labelPaint)
        canvas.drawText(formatDate(snapshot.date), rightAlignX, rightY, bodyPaint)
        
        rightY += 15f
        canvas.drawText("DUE DATE:", rightAlignX - 80f, rightY, labelPaint)
        canvas.drawText(formatDate(snapshot.dueDate), rightAlignX, rightY, bodyPaint)

        return maxOf(leftY, rightY) + 40f
    }

    private fun drawTable(
        canvas: Canvas,
        snapshot: InvoiceSnapshot,
        template: TemplateSnapshot?,
        colors: PdfColors,
        boldTypeface: Typeface,
        regularTypeface: Typeface,
        startY: Float
    ): Float {
        val fontSize = if (template?.fontSizePreset == "LARGE") FONT_SIZE_BODY_LARGE else FONT_SIZE_BODY
        val headerPaint = Paint().apply { typeface = boldTypeface; textSize = fontSize; color = Color.BLACK; isAntiAlias = true }
        val bodyPaint = Paint().apply { typeface = regularTypeface; textSize = fontSize; color = Color.BLACK; isAntiAlias = true }

        val tableRenderer = PdfTableRenderer(
            canvas = canvas,
            startX = MARGIN_X,
            currentY = startY,
            pageWidth = PAGE_WIDTH,
            columnWeights = listOf(0.5f, 0.1f, 0.18f, 0.22f),
            accentColor = colors.primary
        )

        tableRenderer.drawRow(
            listOf("Description", "Qty", "Unit Price", "Amount"), 
            headerPaint, 
            isHeader = true,
            isZebraEnabled = template?.showZebraStripes != false
        )

        snapshot.items.forEach { item ->
            tableRenderer.drawRow(
                listOf(
                    item.description,
                    item.quantity.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() },
                    CurrencyFormatter.formatCents(item.unitPrice, snapshot.currencyCode),
                    CurrencyFormatter.formatCents(item.total, snapshot.currencyCode)
                ),
                bodyPaint,
                isZebraEnabled = template?.showZebraStripes != false
            )
        }

        return tableRenderer.getPosition() + 20f
    }

    private fun drawTotals(
        canvas: Canvas,
        snapshot: InvoiceSnapshot,
        template: TemplateSnapshot?,
        colors: PdfColors,
        boldTypeface: Typeface,
        regularTypeface: Typeface,
        startY: Float
    ): Float {
        var y = startY
        val rightX = PAGE_WIDTH - MARGIN_X
        val labelX = rightX - 120f
        
        val fontSize = if (template?.fontSizePreset == "LARGE") FONT_SIZE_BODY_LARGE else FONT_SIZE_BODY
        val bodyPaint = Paint().apply { typeface = regularTypeface; textSize = fontSize; color = Color.BLACK; isAntiAlias = true; textAlign = Paint.Align.RIGHT }
        
        // Subtotal
        canvas.drawText("Subtotal:", labelX, y, bodyPaint)
        canvas.drawText(CurrencyFormatter.formatCents(snapshot.subtotal, snapshot.currencyCode), rightX, y, bodyPaint)

        // Tax
        if (snapshot.taxAmount > 0) {
            y += 18f
            canvas.drawText("Tax (${(snapshot.taxRate * 100).toInt()}%):", labelX, y, bodyPaint)
            canvas.drawText(CurrencyFormatter.formatCents(snapshot.taxAmount, snapshot.currencyCode), rightX, y, bodyPaint)
        }

        // Total Divider
        y += 12f
        canvas.drawLine(labelX - 20f, y, rightX, y, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        
        // Total Amount Due
        y += 25f
        val totalFontSize = if (template?.fontSizePreset == "LARGE") FONT_SIZE_TOTAL_LARGE else FONT_SIZE_TOTAL
        val totalLabelPaint = Paint().apply {
            typeface = boldTypeface
            textSize = totalFontSize
            color = colors.primary
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        
        canvas.drawText("TOTAL (${snapshot.currencyCode}):", labelX, y, totalLabelPaint)
        canvas.drawText(CurrencyFormatter.formatCents(snapshot.totalAmount, snapshot.currencyCode), rightX, y, totalLabelPaint)

        return y + 40f
    }

    private fun drawFooter(canvas: Canvas, template: TemplateSnapshot?, colors: PdfColors, regularTypeface: Typeface) {
        val footerPaint = Paint().apply {
            typeface = regularTypeface
            textSize = 9f
            color = Color.GRAY
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val message = template?.footerMessage ?: "Thank you for your business!"
        canvas.drawText(message, PAGE_WIDTH / 2f, PAGE_HEIGHT - 40f, footerPaint)
    }

    private fun formatDate(timestamp: Long): String = 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

    private fun generateVersionedFileName(baseFileName: String): String {
        val nameWithoutExt = baseFileName.substringBeforeLast(".pdf")
        var version = 2
        while (true) {
            val name = "${nameWithoutExt}_v${version}.pdf"
            if (!File(context.filesDir, "documents/$name").exists()) return name
            version++
        }
    }
}
