package com.emul8r.bizap.data.service

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.RequiresApi
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.pdf.PdfTableRenderer
import com.emul8r.bizap.ui.templates.TemplateSnapshotManager
import com.emul8r.bizap.utils.DocumentNamingUtils
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
        val customFieldValues = snapshotManager.restoreCustomFieldValues(customFieldValuesJson)

        val colors = pdfStyler.extractColors(templateSnapshot)
        val hideLineItems = pdfStyler.shouldHideLineItems(templateSnapshot)
        val hidePaymentTerms = pdfStyler.shouldHidePaymentTerms(templateSnapshot)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val boldTypeface = pdfStyler.getTypeface(templateSnapshot?.fontFamily, context, isBold = true)
        val regularTypeface = pdfStyler.getTypeface(templateSnapshot?.fontFamily, context, isBold = false)
        val italicTypeface = Typeface.create(regularTypeface, Typeface.ITALIC)

        val symbol = getCurrencySymbol(snapshot.currencyCode)

        val headerPaint = Paint().apply { typeface = boldTypeface; textSize = 10f; color = Color.BLACK; isAntiAlias = true }
        val brandPaint = Paint().apply { typeface = boldTypeface; textSize = 18f; color = colors.primary; isAntiAlias = true }
        val bodyPaint = Paint().apply { typeface = regularTypeface; textSize = 10f; color = colors.textLight; isAntiAlias = true }
        val labelPaint = Paint().apply { typeface = boldTypeface; textSize = 9f; color = Color.GRAY; isAntiAlias = true }

        val centerX = 595f / 2f
        brandPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(snapshot.businessName.uppercase(), centerX, 50f, brandPaint)

        bodyPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("ABN: ${snapshot.businessAbn} | Phone: ${snapshot.businessPhone}", centerX, 65f, bodyPaint)
        canvas.drawText("Email: ${snapshot.businessEmail}", centerX, 80f, bodyPaint)
        canvas.drawText(snapshot.businessAddress, centerX, 95f, bodyPaint)

        canvas.drawLine(40f, 110f, 555f, 110f, Paint().apply { color = colors.secondary; strokeWidth = 1f })

        bodyPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("BILL TO:", 40f, 130f, labelPaint)
        canvas.drawText(snapshot.customerName, 40f, 145f, headerPaint)
        canvas.drawText(snapshot.customerAddress, 40f, 162f, bodyPaint)
        snapshot.customerEmail?.let { canvas.drawText(it, 40f, 178f, bodyPaint) }

        canvas.drawText(fileType.uppercase(), 400f, 130f, labelPaint)
        canvas.drawText(snapshot.displayName.ifBlank { snapshot.invoiceNumber }, 400f, 145f, headerPaint)
        canvas.drawText("Date: ${formatDate(snapshot.date)}", 400f, 162f, bodyPaint)
        canvas.drawText("Due: ${formatDate(snapshot.dueDate)}", 400f, 178f, bodyPaint)

        var currentY = 200f

        val separatorPaint = Paint().apply { color = colors.secondary; strokeWidth = 0.5f; style = Paint.Style.STROKE }
        val sectionHeaderPaint = Paint().apply { typeface = boldTypeface; textSize = 11f; color = colors.primary; isAntiAlias = true }
        val subheaderBodyPaint = Paint().apply { typeface = italicTypeface; textSize = 10f; color = colors.textLight; isAntiAlias = true }
        val footerBodyPaint = Paint().apply { typeface = italicTypeface; textSize = 9f; color = Color.GRAY; isAntiAlias = true }

        // Render header and subheader if present
        if (snapshot.headerText.isNotBlank()) {
            currentY += 15f
            canvas.drawLine(40f, currentY, 555f, currentY, separatorPaint)
            currentY += 12f
            currentY = drawWrappedText(canvas, snapshot.headerText, 40f, currentY, 515f, sectionHeaderPaint)
        }
        if (snapshot.subheaderText.isNotBlank()) {
            currentY += 4f
            currentY = drawWrappedText(canvas, snapshot.subheaderText, 40f, currentY, 515f, subheaderBodyPaint)
        }

        currentY += 15f

        if (!hideLineItems) {
            val tableRenderer = PdfTableRenderer(
                canvas = canvas,
                startX = 40f,
                currentY = currentY,
                pageWidth = 595f,
                columnWeights = listOf(0.5f, 0.1f, 0.15f, 0.25f)
            )

            tableRenderer.drawRow(listOf("Description", "Qty", "Price", "Total"), headerPaint, isHeader = true)

            snapshot.items.forEach { item ->
                tableRenderer.drawRow(
                    listOf(
                        item.description,
                        item.quantity.toInt().toString(),
                        String.format(Locale.getDefault(), "%s%.2f", symbol, item.unitPrice / 100.0),
                        String.format(Locale.getDefault(), "%s%.2f", symbol, item.total / 100.0)
                    ),
                    bodyPaint
                )
            }
            currentY = tableRenderer.getPosition() + 30f
        }

        val rightX = 555f
        bodyPaint.textAlign = Paint.Align.RIGHT
        headerPaint.textAlign = Paint.Align.RIGHT

        canvas.drawText("Subtotal:", 450f, currentY, bodyPaint)
        canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.subtotal / 100.0), rightX, currentY, bodyPaint)

        currentY += 15f
        if (snapshot.taxAmount > 0) {
            canvas.drawText("Tax (${(snapshot.taxRate * 100).toInt()}%):", 450f, currentY, bodyPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.taxAmount / 100.0), rightX, currentY, bodyPaint)
            currentY += 25f
        }

        val totalLabelPaint = Paint(headerPaint).apply {
            textSize = 14f
            color = colors.primary
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL AMOUNT DUE (${snapshot.currencyCode}):", 450f, currentY, totalLabelPaint)
        val formattedAmount = String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.totalAmount / 100.0)
        canvas.drawText(formattedAmount, rightX, currentY, totalLabelPaint)

        // ===== PAYMENT DETAILS SECTION =====
        currentY += 30f
        canvas.drawLine(40f, currentY, 555f, currentY, separatorPaint)
        currentY += 12f
        canvas.drawText("PAYMENT DETAILS", 40f, currentY, labelPaint)
        currentY += 14f

        bodyPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Payment Terms: Due within 30 days of invoice date", 40f, currentY, bodyPaint)
        currentY += 12f
        canvas.drawText("Reference: ${snapshot.invoiceNumber}", 40f, currentY, bodyPaint)
        currentY += 12f

        if (snapshot.businessPhone.isNotBlank()) {
            canvas.drawText("Contact: ${snapshot.businessPhone}", 40f, currentY, bodyPaint)
            currentY += 12f
        }

        if (snapshot.businessEmail.isNotBlank()) {
            canvas.drawText(snapshot.businessEmail, 40f, currentY, bodyPaint)
            currentY += 12f
        }

        // Render notes and footer below totals
        bodyPaint.textAlign = Paint.Align.LEFT
        if (snapshot.notes.isNotBlank()) {
            currentY += 30f
            canvas.drawLine(40f, currentY, 555f, currentY, separatorPaint)
            currentY += 12f
            canvas.drawText("NOTES", 40f, currentY, labelPaint)
            currentY += 14f
            currentY = drawWrappedText(canvas, snapshot.notes, 40f, currentY, 515f, bodyPaint)
        }
        if (snapshot.footerText.isNotBlank()) {
            currentY += 20f
            canvas.drawLine(40f, currentY, 555f, currentY, separatorPaint)
            currentY += 12f
            currentY = drawWrappedText(canvas, snapshot.footerText, 40f, currentY, 515f, footerBodyPaint)
        }

        pdfDocument.finishPage(page)
        file.outputStream().use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return file
    }

    /**
     * Draws wrapped text using StaticLayout and returns the Y position after the drawn text.
     */
    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint
    ): Float {
        val textPaint = TextPaint(paint)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }

    private fun getCurrencySymbol(code: String): String {
        return when (code) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "AUD" -> "$"
            else -> "$"
        }
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
