package com.emu.emubizwax.domain.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.emu.emubizwax.domain.model.ExportData
import com.emu.emubizwax.domain.service.PdfConstants.COL_PRICE
import com.emu.emubizwax.domain.service.PdfConstants.COL_QTY
import com.emu.emubizwax.domain.service.PdfConstants.COL_TOTAL
import com.emu.emubizwax.domain.service.PdfConstants.LINE_HEIGHT
import com.emu.emubizwax.domain.service.PdfConstants.MARGIN
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val PAGE_WIDTH = 595
private const val PAGE_HEIGHT = 842

class PdfGeneratorService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateInvoice(data: ExportData, primaryColor: String, customerName: String): File {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var currentY = 0f

        fun startNewPage() {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            currentY = MARGIN
        }

        // Header
        paint.color = Color.parseColor(primaryColor)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 100f, paint)
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(if(data.invoice.isQuote) "QUOTE" else "INVOICE", MARGIN, 60f, paint)
        currentY = 120f

        // Invoice Info
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Invoice #${data.invoice.id}", MARGIN, currentY, paint)
        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(data.invoice.date))
        canvas.drawText("Date: $formattedDate", PAGE_WIDTH - MARGIN - 150f, currentY, paint)
        currentY += LINE_HEIGHT * 2

        // Customer Info
        canvas.drawText("Bill To:", MARGIN, currentY, paint)
        currentY += LINE_HEIGHT
        canvas.drawText(customerName, MARGIN, currentY, paint)
        currentY += LINE_HEIGHT * 2

        // Table Header
        paint.isFakeBoldText = true
        canvas.drawText("Description", MARGIN, currentY, paint)
        canvas.drawText("Qty", COL_QTY, currentY, paint)
        canvas.drawText("Price", COL_PRICE, currentY, paint)
        canvas.drawText("Total", COL_TOTAL, currentY, paint)
        currentY += LINE_HEIGHT

        // Table Rows
        paint.isFakeBoldText = false
        data.items.forEach { item ->
            if (currentY > PAGE_HEIGHT - MARGIN) startNewPage()
            canvas.drawText(item.description, MARGIN, currentY, paint)
            canvas.drawText(item.quantity.toString(), COL_QTY, currentY, paint)
            canvas.drawText("$${item.unitPrice}", COL_PRICE, currentY, paint)
            canvas.drawText("$${item.quantity.multiply(item.unitPrice)}", COL_TOTAL, currentY, paint)
            currentY += LINE_HEIGHT
        }

        // Evidence Appendix
        if (data.invoice.photoUris.isNotEmpty()) {
            if (currentY > PAGE_HEIGHT - MARGIN - 100f) startNewPage() // Check for space before appendix
            currentY += LINE_HEIGHT * 2
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("Evidence Appendix", MARGIN, currentY, paint)
            currentY += LINE_HEIGHT

            data.invoice.photoUris.forEach { uriString ->
                val bitmap = loadBitmapFromUri(Uri.parse(uriString))
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true)
                if (currentY + scaledBitmap.height > PAGE_HEIGHT - MARGIN) startNewPage()
                canvas.drawBitmap(scaledBitmap, MARGIN, currentY, paint)
                currentY += scaledBitmap.height + LINE_HEIGHT
            }
        }

        pdfDocument.finishPage(page)

        val safeCustomerName = customerName.replace(Regex("[^A-Za-z0-9]"), "")
        val fileName = "Invoice_${data.invoice.id}_${safeCustomerName}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()
        return file
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }
}
