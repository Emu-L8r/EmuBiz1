package com.emul8r.bizap.data.service

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.utils.DocumentNamingUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoicePdfService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customerRepository: CustomerRepository
) {
    suspend fun generateInvoice(
        invoiceData: InvoiceWithItems,
        businessProfile: BusinessProfile,
        isQuote: Boolean
    ): File {
        val customer = customerRepository.getCustomerById(invoiceData.invoice.customerId ?: 0) ?: throw Exception("Customer not found")
        val pdfDocument = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            drawBranding(canvas, businessProfile)
        }

        // Draw watermark if PAID
        if (invoiceData.invoice.status == "PAID") {
            canvas.save()
            canvas.rotate(-45f, pageInfo.pageWidth / 2f, pageInfo.pageHeight / 2f)
            paint.color = Color.argb(40, 0, 0, 0) // Light grey
            paint.textSize = 140f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("PAID", pageInfo.pageWidth / 2f, pageInfo.pageHeight / 2f + 50, paint)
            canvas.restore()
            paint.textAlign = Paint.Align.LEFT // Reset alignment
        }

        // 1. Header - Branding
        val title = if (isQuote) "QUOTE" else "TAX INVOICE"
        paint.color = Color.BLACK
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText(title, 40f, 60f, paint)

        // 2. Business Details (Top Right)
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val rightAlign = 400f
        var currentY = 50f
        canvas.drawText(businessProfile.businessName, rightAlign, currentY, paint)
        canvas.drawText("ABN: ${businessProfile.abn}", rightAlign, currentY + 15, paint)
        
        // 3. Invoice Metadata
        currentY = 120f
        paint.isFakeBoldText = true
        canvas.drawText("Invoice To:", 40f, currentY, paint)
        paint.isFakeBoldText = false
        canvas.drawText(customer.name, 40f, currentY + 15, paint)
        
        canvas.drawText("Invoice #: ${invoiceData.invoice.invoiceId}", rightAlign, currentY, paint)
        canvas.drawText("Date: ${formatDate(invoiceData.invoice.date)}", rightAlign, currentY + 15, paint)

        // 4. Table Header
        currentY = 200f
        paint.color = Color.LTGRAY
        canvas.drawRect(40f, currentY, 555f, currentY + 25f, paint)
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("Description", 50f, currentY + 17, paint)
        canvas.drawText("Total (Inc. GST)", 450f, currentY + 17, paint)

        // 5. Line Items Loop
        currentY += 45f
        paint.isFakeBoldText = false
        invoiceData.items.forEach { item ->
            canvas.drawText(item.description, 50f, currentY, paint)
            canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", item.total)}", 450f, currentY, paint)
            currentY += 20f
        }

        // 6. Totals Section (Bottom Right)
        currentY += 40f
        val gstAmount = invoiceData.subtotal / 11 // Standard AU GST calculation
        canvas.drawText("Includes GST of: $${String.format(Locale.getDefault(), "%.2f", gstAmount)}", 380f, currentY, paint)
        
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL DUE: $${String.format(Locale.getDefault(), "%.2f", invoiceData.subtotal)}", 380f, currentY + 25, paint)

        // 7. Photos
        val photoUris = invoiceData.invoice.photoUris.split(",").mapNotNull { uriString ->
            try { Uri.parse(uriString) } catch(e: Exception) { null } 
        }

        if (photoUris.isNotEmpty()) {
            currentY += 60f
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Attached Photos", 40f, currentY, paint)
            currentY += 20f

            photoUris.forEach { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                    
                    if (currentY + scaledBitmap.height > pageInfo.pageHeight - 40) {
                        pdfDocument.finishPage(page)
                        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 40f
                    }

                    canvas.drawBitmap(scaledBitmap, 40f, currentY, paint)
                    currentY += scaledBitmap.height + 10f

                } catch (e: Exception) {
                    // Ignore photo if it can't be loaded
                }
            }
        }

        pdfDocument.finishPage(page)

        val fileName = DocumentNamingUtils.generateFileName(customer.name, invoiceData.invoice.date, invoiceData.invoice.invoiceId.toInt(), if (isQuote) "Quote" else "Invoice")
        val file = File(context.filesDir, "documents/$fileName")
        file.parentFile?.mkdirs()

        file.outputStream().use { 
            pdfDocument.writeTo(it)
        }
        pdfDocument.close()
        return file
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawBranding(canvas: Canvas, profile: BusinessProfile) {
        val paint = Paint()

        profile.logoUri?.let { uriString ->
            val uri = Uri.parse(uriString)
            val bitmap = loadScaledBitmap(uri, maxWidth = 100) // Scale for header
            bitmap?.let {
                // Draw logo in the top-right corner
                canvas.drawBitmap(it, 450f, 40f, paint)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun loadScaledBitmap(uri: Uri, maxWidth: Int): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // Required for PDF Canvas
            }.let { 
                val aspectRatio = it.height.toFloat() / it.width.toFloat()
                Bitmap.createScaledBitmap(it, maxWidth, (maxWidth * aspectRatio).toInt(), true)
            }
        } catch (e: Exception) {
            null
        }
    }
}
