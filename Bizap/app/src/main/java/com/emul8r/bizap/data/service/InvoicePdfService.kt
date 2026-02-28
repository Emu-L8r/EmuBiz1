package com.emul8r.bizap.data.service

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.pdf.PdfTableRenderer
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
    suspend fun checkIfPdfExists(invoiceId: Long, fileType: String): Pair<Boolean, String?> {
        val existingDoc = documentRepository.getDocumentByInvoiceAndType(invoiceId, fileType)
        return if (existingDoc != null) Pair(true, existingDoc.fileName) else Pair(false, null)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    suspend fun generateInvoice(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean = true
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

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val boldTypeface = try { Typeface.createFromAsset(context.assets, "fonts/Roboto-Bold.ttf") } catch (e: Exception) { Typeface.DEFAULT_BOLD }
        val regularTypeface = try { Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf") } catch (e: Exception) { Typeface.DEFAULT }
        val italicTypeface = Typeface.create(regularTypeface, Typeface.ITALIC)

        val titlePaint = Paint().apply { typeface = boldTypeface; textSize = 26f; color = Color.BLACK; isAntiAlias = true }
        val brandPaint = Paint().apply { typeface = boldTypeface; textSize = 18f; color = Color.parseColor("#6750A4"); isAntiAlias = true }
        val headerPaint = Paint().apply { typeface = boldTypeface; textSize = 10f; color = Color.BLACK; isAntiAlias = true }
        val bodyPaint = Paint().apply { typeface = regularTypeface; textSize = 10f; color = Color.DKGRAY; isAntiAlias = true }
        val labelPaint = Paint().apply { typeface = boldTypeface; textSize = 9f; color = Color.GRAY; isAntiAlias = true }

        // 1. BUSINESS HEADER (Center Aligned Branding)
        val centerX = 595f / 2f
        brandPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(snapshot.businessName.uppercase(), centerX, 50f, brandPaint)
        
        bodyPaint.textAlign = Paint.Align.CENTER
        // FIX 2: Added Phone and Address to header
        canvas.drawText("ABN: ${snapshot.businessAbn} | Phone: ${snapshot.businessPhone}", centerX, 65f, bodyPaint)
        canvas.drawText("Email: ${snapshot.businessEmail}", centerX, 80f, bodyPaint)
        canvas.drawText(snapshot.businessAddress, centerX, 95f, bodyPaint)
        
        // Horizontal Line
        canvas.drawLine(40f, 110f, 555f, 110f, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })

        // 2. CLIENT & INVOICE DETAILS
        bodyPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("BILL TO:", 40f, 140f, labelPaint)
        canvas.drawText(snapshot.customerName, 40f, 155f, headerPaint)
        canvas.drawText(snapshot.customerAddress, 40f, 170f, bodyPaint)
        snapshot.customerEmail?.let { canvas.drawText(it, 40f, 185f, bodyPaint) }

        canvas.drawText(fileType.uppercase(), 400f, 140f, labelPaint)
        canvas.drawText("# ${snapshot.invoiceNumber}", 400f, 155f, headerPaint)
        canvas.drawText("Date: ${formatDate(snapshot.date)}", 400f, 170f, bodyPaint)
        canvas.drawText("Due: ${formatDate(snapshot.dueDate)}", 400f, 185f, bodyPaint)

        // 3. ITEMS TABLE
        val tableRenderer = PdfTableRenderer(
            canvas = canvas,
            startX = 40f,
            currentY = 220f,
            pageWidth = 595f,
            columnWeights = listOf(0.5f, 0.1f, 0.15f, 0.25f)
        )

        tableRenderer.drawRow(listOf("Description", "Qty", "Unit Price", "Total"), headerPaint, isHeader = true)

        snapshot.items.forEach { item ->
            tableRenderer.drawRow(
                listOf(
                    item.description,
                    item.quantity.toInt().toString(),
                    String.format(Locale.getDefault(), "$%.2f", item.unitPrice),
                    String.format(Locale.getDefault(), "$%.2f", item.total)
                ),
                bodyPaint
            )
        }

        // 4. TOTALS
        var currentY = tableRenderer.getPosition() + 30f
        val rightX = 555f
        bodyPaint.textAlign = Paint.Align.RIGHT
        headerPaint.textAlign = Paint.Align.RIGHT
        
        canvas.drawText("Subtotal:", 450f, currentY, bodyPaint)
        canvas.drawText(String.format(Locale.getDefault(), "$%.2f", snapshot.subtotal), rightX, currentY, bodyPaint)
        
        currentY += 15f
        canvas.drawText("Tax (${(snapshot.taxRate * 100).toInt()}%):", 450f, currentY, bodyPaint)
        canvas.drawText(String.format(Locale.getDefault(), "$%.2f", snapshot.taxAmount), rightX, currentY, bodyPaint)

        currentY += 25f
        val totalLabelPaint = Paint(headerPaint).apply { 
            textSize = 14f
            color = Color.parseColor("#6750A4")
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL AMOUNT DUE:", 450f, currentY, totalLabelPaint)
        canvas.drawText(String.format(Locale.getDefault(), "$%.2f", snapshot.totalAmount), rightX, currentY, totalLabelPaint)

        // 5. PAYMENT DETAILS SECTION
        currentY += 60f
        canvas.drawRect(40f, currentY, 555f, currentY + 100f, Paint().apply { color = Color.parseColor("#FAFAFA") })
        canvas.drawText("PAYMENT DETAILS", 50f, currentY + 20f, headerPaint.apply { textAlign = Paint.Align.LEFT })
        canvas.drawText("Payment Terms: Due within 30 days", 50f, currentY + 40f, bodyPaint.apply { textAlign = Paint.Align.LEFT })
        canvas.drawText("Reference: ${snapshot.invoiceNumber}", 50f, currentY + 55f, bodyPaint)
        canvas.drawText("Contact: ${snapshot.businessEmail}", 50f, currentY + 70f, bodyPaint)

        // 6. FOOTER
        val footerPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.CENTER; typeface = italicTypeface }
        canvas.drawText("Thank you for your business!", centerX, 800f, footerPaint)
        canvas.drawText("${snapshot.businessName} | ABN: ${snapshot.businessAbn}", centerX, 815f, labelPaint.apply { textAlign = Paint.Align.CENTER })

        pdfDocument.finishPage(page)
        file.outputStream().use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return file
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
