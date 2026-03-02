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
        canvas.drawText("BILL TO:", 40f, 140f, labelPaint)
        canvas.drawText(snapshot.customerName, 40f, 145f, headerPaint)
        canvas.drawText(snapshot.customerAddress, 40f, 160f, bodyPaint)
        snapshot.customerEmail?.let { canvas.drawText(it, 40f, 175f, bodyPaint) }

        canvas.drawText(fileType.uppercase(), 400f, 140f, labelPaint)
        canvas.drawText("# ${snapshot.invoiceNumber}", 400f, 145f, headerPaint)
        canvas.drawText("Date: ${formatDate(snapshot.date)}", 400f, 170f, bodyPaint)
        canvas.drawText("Due: ${formatDate(snapshot.dueDate)}", 400f, 185f, bodyPaint)

        var currentY = 220f

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
                        String.format(Locale.getDefault(), "%s%.2f", symbol, item.unitPrice),
                        String.format(Locale.getDefault(), "%s%.2f", symbol, item.total)
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
        canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.subtotal), rightX, currentY, bodyPaint)

        currentY += 15f
        if (snapshot.taxAmount > 0) {
            canvas.drawText("Tax (${(snapshot.taxRate * 100).toInt()}%):", 450f, currentY, bodyPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.taxAmount), rightX, currentY, bodyPaint)
            currentY += 25f
        }

        val totalLabelPaint = Paint(headerPaint).apply {
            textSize = 14f
            color = colors.primary
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL AMOUNT DUE (${snapshot.currencyCode}):", 450f, currentY, totalLabelPaint)
        canvas.drawText(String.format(Locale.getDefault(), "%s%.2f", symbol, snapshot.totalAmount), rightX, currentY, totalLabelPaint)

        pdfDocument.finishPage(page)
        file.outputStream().use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return file
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
