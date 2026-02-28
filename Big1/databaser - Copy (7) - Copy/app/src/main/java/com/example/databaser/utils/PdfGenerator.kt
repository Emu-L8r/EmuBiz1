package com.example.databaser.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import coil.imageLoader
import coil.request.ImageRequest
import com.example.databaser.R
import com.example.databaser.data.BusinessInfo
import com.example.databaser.data.InvoiceWithCustomerAndLineItems
import com.example.databaser.viewmodel.InvoiceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    suspend fun createInvoicePdf(context: Context, invoice: InvoiceWithCustomerAndLineItems, customerName: String, businessInfo: BusinessInfo, isQuote: Boolean, invoiceViewModel: InvoiceViewModel, header: String, subHeader: String, footer: String, photoUris: List<Uri>): Uri? {
        val pageHeight = 1120
        val pagewidth = 792
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 32f
            color = Color.BLACK
        }

        val headingPaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            color = Color.DKGRAY
        }

        val textPaint = TextPaint().apply {
            textSize = 12f
            color = Color.BLACK
        }

        var yPosition = 40f

        // Draw Header, Business and Customer Details
        yPosition = drawTopSection(context, canvas, businessInfo, invoice, titlePaint, headingPaint, textPaint, pagewidth, yPosition, isQuote)

        // Draw Invoice Details
        yPosition = drawInvoiceDetails(canvas, invoice, headingPaint, textPaint, pagewidth, yPosition, isQuote)

        // Draw Header/Sub-header
        yPosition = drawHeaderSubheader(canvas, header, subHeader, titlePaint, textPaint, pagewidth, yPosition)

        // Draw Line Items Table
        yPosition = drawLineItemsTable(canvas, invoice, textPaint, pagewidth, yPosition, businessInfo.currencySymbol)

        // Draw Total
        yPosition = drawTotal(canvas, invoice, businessInfo, headingPaint, pagewidth, yPosition)

        // Draw Photos
        yPosition = drawPhotosSection(context, canvas, photoUris, yPosition, pagewidth)

        // Draw Notes
        drawFooter(canvas, businessInfo, footer, headingPaint, textPaint, pagewidth, yPosition)

        document.finishPage(page)

        return savePdfToFile(context, document, isQuote, customerName, invoice, invoiceViewModel)
    }

    private suspend fun drawTopSection(context: Context, canvas: Canvas, businessInfo: BusinessInfo, invoice: InvoiceWithCustomerAndLineItems, titlePaint: TextPaint, headingPaint: TextPaint, textPaint: TextPaint, pagewidth: Int, yPosition: Float, isQuote: Boolean): Float {
        var currentY = yPosition
        val logoPaint = Paint()
        var logoBitmap: Bitmap? = null

        // Load logo
        val logoPath = businessInfo.logoPath
        logoBitmap = if (logoPath != null) {
            try {
                val logoUri = logoPath.toUri()
                val request = ImageRequest.Builder(context).data(logoUri).allowHardware(false).build()
                withContext(Dispatchers.IO) { context.imageLoader.execute(request).drawable?.toBitmap() }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            ResourcesCompat.getDrawable(context.resources, R.drawable.thswalogo, null)?.toBitmap()
        }

        // Draw Logo on the left
        logoBitmap?.let {
            val scale = 120f / it.height
            val scaledWidth = it.width * scale
            canvas.drawBitmap(it, null, Rect(40, currentY.toInt(), (40 + scaledWidth).toInt(), (currentY.toInt() + 120)), logoPaint)
        }

        // Draw Title on the right
        val title = if (isQuote) "QUOTE" else "INVOICE"
        val titleX = pagewidth - titlePaint.measureText(title) - 40f
        canvas.drawText(title, titleX, currentY + 30f, titlePaint)

        // Business Details (From)
        var fromY = currentY + 80f
        canvas.drawText("FROM", titleX, fromY, headingPaint)
        fromY += 20
        canvas.drawText(businessInfo.name, titleX, fromY, textPaint)
        fromY += 20
        canvas.drawText(businessInfo.address, titleX, fromY, textPaint)
        fromY += 20
        canvas.drawText(businessInfo.contactNumber, titleX, fromY, textPaint)
        fromY += 20
        businessInfo.email?.let { canvas.drawText(it, titleX, fromY, textPaint) }

        // Customer Details (To)
        var toY = currentY + 160f // Start below the logo space
        canvas.drawText("TO", 40f, toY, headingPaint)
        toY += 20
        canvas.drawText(invoice.customer.name, 40f, toY, textPaint)
        toY += 20
        canvas.drawText(invoice.customer.address, 40f, toY, textPaint)
        toY += 20
        canvas.drawText(invoice.customer.contactNumber, 40f, toY, textPaint)
        toY += 20
        canvas.drawText(invoice.customer.email, 40f, toY, textPaint)
        
        return maxOf(fromY, toY) + 40f
    }

    private fun drawInvoiceDetails(canvas: Canvas, invoice: InvoiceWithCustomerAndLineItems, headingPaint: TextPaint, textPaint: TextPaint, pagewidth: Int, yPosition: Float, isQuote: Boolean): Float {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val detailsData = listOf(
            (if (isQuote) "QUOTE#" else "INVOICE#") to invoice.invoice.invoiceNumber,
            "DATE" to dateFormat.format(Date(invoice.invoice.date)),
            "DUE DATE" to if (isQuote) "N/A" else dateFormat.format(Date(invoice.invoice.dueDate))
        )
        val detailsBoxWidth = (pagewidth - 80f) / 3
        
        detailsData.forEachIndexed { index, pair ->
             canvas.drawText(pair.first, 40f + (index * detailsBoxWidth), yPosition, textPaint)
             canvas.drawText(pair.second, 40f + (index * detailsBoxWidth), yPosition + 25, headingPaint)
        }
        
        var currentY = yPosition + 60f
        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.LTGRAY
        }
        canvas.drawLine(40f, currentY, pagewidth-40f, currentY, linePaint)
        return currentY + 20f
    }

    private fun drawHeaderSubheader(canvas: Canvas, header: String, subHeader: String, titlePaint: TextPaint, textPaint: TextPaint, pagewidth: Int, yPosition: Float): Float {
        var currentY = yPosition
        if (header.isNotBlank()) {
            val headerLayout = StaticLayout.Builder.obtain(header, 0, header.length, titlePaint.apply{textSize=18f}, pagewidth - 80).build()
            canvas.save()
            canvas.translate(40f, currentY)
            headerLayout.draw(canvas)
            canvas.restore()
            currentY += headerLayout.height + 10f
        }
        if (subHeader.isNotBlank()) {
            val subHeaderLayout = StaticLayout.Builder.obtain(subHeader, 0, subHeader.length, textPaint, pagewidth - 80).build()
            canvas.save()
            canvas.translate(40f, currentY)
            subHeaderLayout.draw(canvas)
            canvas.restore()
            currentY += subHeaderLayout.height + 30f
        }
        return currentY
    }

    private fun drawLineItemsTable(canvas: Canvas, invoice: InvoiceWithCustomerAndLineItems, textPaint: TextPaint, pagewidth: Int, yPosition: Float, currencySymbol: String): Float {
        val tableHeaderPaint = TextPaint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.WHITE
        }
        val tableHeaderBackground = Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#4A4A4A") // A darker grey
        }
        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.LTGRAY
        }

        var currentY = yPosition
        canvas.drawRect(40f, currentY, pagewidth - 40f, currentY + 25, tableHeaderBackground)
        currentY += 18
        canvas.drawText("ITEM", 60f, currentY, tableHeaderPaint)
        canvas.drawText("QTY", 500f, currentY, tableHeaderPaint)
        canvas.drawText("PRICE", 580f, currentY, tableHeaderPaint)
        canvas.drawText("TOTAL", 680f, currentY, tableHeaderPaint)
        currentY += 12

        invoice.lineItems.forEach { item ->
            canvas.drawLine(40f, currentY, pagewidth - 40f, currentY, linePaint)
            currentY += 20
            val itemDescription = "${item.job}\n${item.details}"
            val descriptionLayout = StaticLayout.Builder.obtain(itemDescription, 0, itemDescription.length, textPaint, 420).build()
            canvas.save()
            canvas.translate(60f, currentY - 15)
            descriptionLayout.draw(canvas)
            canvas.restore()

            canvas.drawText(item.quantity.toString(), 510f, currentY, textPaint)
            canvas.drawText("$currencySymbol${String.format("%.2f", item.unitPrice)}", 590f, currentY, textPaint)
            canvas.drawText("$currencySymbol${String.format("%.2f", item.quantity * item.unitPrice)}", 690f, currentY, textPaint)
            currentY += descriptionLayout.height + 10f
        }
        canvas.drawLine(40f, currentY, pagewidth - 40f, currentY, linePaint)
        return currentY
    }

    private suspend fun drawPhotosSection(context: Context, canvas: Canvas, photoUris: List<Uri>, yPosition: Float, pagewidth: Int): Float {
        var currentY = yPosition
        if (photoUris.isNotEmpty()) {
            currentY += 30f // Add some space before the photos
            val photoPaint = Paint()
            val photoSize = 150f
            val padding = 10f
            var currentX = 40f

            val headingPaint = TextPaint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 14f
                color = Color.DKGRAY
            }
            canvas.drawText("Photos", 40f, currentY, headingPaint)
            currentY += 30f


            for (photoUri in photoUris) {
                val photoBitmap: Bitmap? = try {
                    val request = ImageRequest.Builder(context).data(photoUri).size(photoSize.toInt()).allowHardware(false).build()
                    withContext(Dispatchers.IO) { context.imageLoader.execute(request).drawable?.toBitmap() }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                photoBitmap?.let {
                    if (currentX + photoSize > pagewidth - 40f) {
                        currentX = 40f
                        currentY += photoSize + padding
                    }
                    val srcRect = Rect(0, 0, it.width, it.height)
                    val destRect = Rect(currentX.toInt(), currentY.toInt(), (currentX + photoSize).toInt(), (currentY + photoSize).toInt())
                    canvas.drawBitmap(it, srcRect, destRect, photoPaint)
                    currentX += photoSize + padding
                }
            }
            currentY += photoSize + 20f
        }
        return currentY
    }

    private fun drawTotal(canvas: Canvas, invoice: InvoiceWithCustomerAndLineItems, businessInfo: BusinessInfo, headingPaint: TextPaint, pagewidth: Int, yPosition: Float): Float {
        var currentY = yPosition + 20f
        val total = invoice.lineItems.sumOf { it.quantity * it.unitPrice }
        val totalText = "TOTAL: ${businessInfo.currencySymbol}${String.format("%.2f", total)}"
        val totalPaint = TextPaint(headingPaint).apply {
            textSize = 20f
            color = Color.BLACK
        }
        val totalX = (pagewidth - 40f) - totalPaint.measureText(totalText)
        canvas.drawText(totalText, totalX, currentY, totalPaint)
        return currentY + 40f
    }

    private fun drawFooter(canvas: Canvas, businessInfo: BusinessInfo, footer: String, headingPaint: TextPaint, textPaint: TextPaint, pagewidth: Int, yPosition: Float) {
        var footerY = maxOf(yPosition, 900f)

        if (footer.isNotBlank()){
            canvas.drawText("Notes", 40f, footerY, headingPaint)
            footerY += 20
            val notesLayout = StaticLayout.Builder.obtain(footer, 0, footer.length, textPaint, pagewidth / 2).build()
            canvas.save()
            canvas.translate(40f, footerY)
            notesLayout.draw(canvas)
            canvas.restore()
            footerY += notesLayout.height
        }
        
        footerY += 20f

        businessInfo.paymentDetails?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Payment Details", 40f, footerY, headingPaint)
            footerY += 20
            val paymentLayout = StaticLayout.Builder.obtain(it, 0, it.length, textPaint, pagewidth / 2).build()
            canvas.save()
            canvas.translate(40f, footerY)
            paymentLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun savePdfToFile(context: Context, document: PdfDocument, isQuote: Boolean, customerName: String, invoice: InvoiceWithCustomerAndLineItems, invoiceViewModel: InvoiceViewModel): Uri? {
        val fileName = if (isQuote) "quote_${customerName}_${invoice.invoice.invoiceNumber}.pdf" else "invoice_${customerName}_${invoice.invoice.invoiceNumber}.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        return try {
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    document.writeTo(outputStream)
                }
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }.also {
            document.close()
        }
    }
}