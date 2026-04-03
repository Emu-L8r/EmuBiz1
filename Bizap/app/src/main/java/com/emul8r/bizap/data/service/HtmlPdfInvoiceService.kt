package com.emul8r.bizap.data.service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.service.PdfGenerationService
import com.emul8r.bizap.utils.DocumentNamingUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * HTML-to-PDF implementation of PDF generation service.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class HtmlPdfInvoiceService(
    private val context: Context,
    private val settings: InvoiceSettings? = null
) : PdfGenerationService {

    override suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean,
        theme: com.emul8r.bizap.domain.model.InvoiceTheme?
    ): File {
        Timber.d("📝 HtmlPdfInvoiceService.generatePdf() START")
        
        if (settings == null) {
            Timber.e("❌ CRITICAL ERROR: Settings object is NULL")
            throw IllegalStateException("HtmlPdfInvoiceService requires settings")
        }
        
        try {
            val fileType = if (isQuote) "Quote" else "Invoice"
            val baseFileName = DocumentNamingUtils.generateFileName(
                snapshot.customerName, snapshot.date, snapshot.invoiceId.toInt(), fileType
            )

            val existingFile = File(context.filesDir, "documents/$baseFileName")
            if (!overwriteExisting && existingFile.exists()) {
                return existingFile
            }

            val htmlContent = generateHtmlContent(snapshot, isQuote)
            val cssContent = loadSelectedStyleCss()
            val htmlWithCss = embedCssIntoHtml(htmlContent, cssContent)

            return convertHtmlToPdf(htmlWithCss, baseFileName)
        } catch (e: Exception) {
            Timber.e(e, "❌ HtmlPdfInvoiceService.generatePdf() FAILED")
            throw e
        }
    }

    private fun loadSelectedStyleCss(): String {
        val selectedStyle = settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN
        val cssFileName = selectedStyle.styleFile

        return try {
            val inputStream = context.assets.open("invoices/html-theme/$cssFileName")
            inputStream.use { stream ->
                InputStreamReader(stream).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ FAILED TO LOAD CSS FILE: $cssFileName")
            try {
                val inputStream = context.assets.open("invoices/html-theme/invoice-styles.css")
                inputStream.use { stream ->
                    InputStreamReader(stream).use { reader ->
                        reader.readText()
                    }
                }
            } catch (fallbackError: Exception) {
                ""
            }
        }
    }

    private fun embedCssIntoHtml(htmlContent: String, cssContent: String): String {
        val styleTagStart = htmlContent.indexOf("<style>")
        val styleTagEnd = htmlContent.indexOf("</style>", styleTagStart)

        return if (styleTagStart >= 0 && styleTagEnd > styleTagStart) {
            val beforeStyle = htmlContent.substring(0, styleTagStart)
            val afterStyle = htmlContent.substring(styleTagEnd + "</style>".length)
            beforeStyle + "<style>\n" + cssContent + "\n</style>" + afterStyle
        } else {
            htmlContent
        }
    }

    private fun generateHtmlContent(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean
    ): String {
        val documentType = if (isQuote) "QUOTE" else "INVOICE"
        
        val subtotalDollars = snapshot.subtotal / 100.0
        val taxDollars = snapshot.taxAmount / 100.0
        val totalDollars = snapshot.totalAmount / 100.0

        val itemsHtml = snapshot.items.joinToString("\n") { item ->
            val amountDollars = item.total / 100.0
            val unitPriceDollars = item.unitPrice / 100.0

            """
                <tr class="table-row">
                    <td class="col-description">${item.description}</td>
                    <td class="col-quantity">${String.format("%.2f", item.quantity)}</td>
                    <td class="col-unit-price">${String.format("${'$'}%.2f", unitPriceDollars)}</td>
                    <td class="col-amount">${String.format("${'$'}%.2f", amountDollars)}</td>
                </tr>
            """.trimIndent()
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <style>
                </style>
            </head>
            <body>
                <div class="invoice-container">
                    <div class="invoice-header">
                        <div class="company-info">
                            <div class="company-name">${snapshot.businessName}</div>
                            <div class="company-detail">${snapshot.businessEmail}</div>
                        </div>
                        <div class="invoice-title">
                            <h2>$documentType</h2>
                        </div>
                    </div>
                    
                    <div class="invoice-metadata">
                        <p>Invoice #: ${snapshot.invoiceId}</p>
                        <p>Date: ${snapshot.date}</p>
                    </div>
                    
                    <div class="bill-to">
                        <h3>Bill To: ${snapshot.customerName}</h3>
                    </div>
                    
                    <table class="items-table">
                        <thead>
                            <tr>
                                <th>Description</th>
                                <th>Qty</th>
                                <th>Price</th>
                                <th>Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            $itemsHtml
                        </tbody>
                    </table>
                    
                    <div class="totals">
                        <p>Subtotal: ${String.format("${'$'}%.2f", subtotalDollars)}</p>
                        <p>Total: ${String.format("${'$'}%.2f", totalDollars)}</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun convertHtmlToPdf(
        htmlContent: String,
        baseFileName: String
    ): File {
        val pdfFileName = baseFileName.replace(".pdf", "_html.pdf")
        val file = File(context.filesDir, "documents/$pdfFileName")
        file.parentFile?.mkdirs()

        try {
            val outputStream = FileOutputStream(file)
            val converterProperties = com.itextpdf.html2pdf.ConverterProperties()
            
            // CRITICAL FIX: Use convertToPdf for a direct and flushed conversion
            com.itextpdf.html2pdf.HtmlConverter.convertToPdf(
                htmlContent,
                outputStream,
                converterProperties
            )
            outputStream.flush()
            outputStream.close()

            Timber.d("✅ PDF created: ${file.name}, size: ${file.length()}")
        } catch (e: Exception) {
            Timber.e(e, "❌ PDF conversion failed")
            throw e
        }

        return file
    }
}
