package com.example.databaser.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generatePdf(context: Context, text: String): File? {
        return try {
            val file = File(context.cacheDir, "document.pdf")
            val fileOutputStream = FileOutputStream(file)

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = android.graphics.Paint()
            paint.textSize = 12f

            var y = 25f
            for (line in text.split('\n')) {
                canvas.drawText(line, 10f, y, paint)
                y += paint.descent() - paint.ascent()
            }

            document.finishPage(page)
            document.writeTo(fileOutputStream)
            document.close()
            fileOutputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
