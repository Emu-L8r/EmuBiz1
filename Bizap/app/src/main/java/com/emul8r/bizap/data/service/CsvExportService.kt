package com.emul8r.bizap.data.service

import android.content.Context
import com.emul8r.bizap.domain.model.Invoice
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service that generates CSV files from invoice data.
 *
 * Two export modes:
 * - Single invoice with line-item detail.
 * - Invoice list summary (one row per invoice).
 */
@Singleton
class CsvExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Exports a single [invoice] to a CSV file with full line-item detail.
     * Returns the generated [File] so callers can share it via an Android intent.
     */
    suspend fun exportSingleInvoice(invoice: Invoice): File {
        Timber.d("CsvExportService: exporting invoice ${invoice.id}")

        val displayId = invoice.displayName.ifBlank { invoice.invoiceNumber }
        val fileName = "invoice-${sanitizeForFilename(displayId)}.csv"
        val file = File(context.filesDir, "exports/$fileName")
        file.parentFile?.mkdirs()

        file.bufferedWriter().use { writer ->
            // ── Invoice header section ─────────────────────────────────────────
            writer.appendLine("Invoice ID,Date,Due Date,Customer,Address,Email,Status,Currency,Subtotal,Tax,Total,Amount Paid,Balance")
            writer.appendLine(
                listOf(
                    csvEscape(displayId),
                    dateFormat.format(Date(invoice.date)),
                    if (invoice.dueDate > 0) dateFormat.format(Date(invoice.dueDate)) else "",
                    csvEscape(invoice.customerName),
                    csvEscape(invoice.customerAddress),
                    csvEscape(invoice.customerEmail ?: ""),
                    invoice.status.name,
                    invoice.currencyCode,
                    centsToDecimal(invoice.totalAmount - invoice.taxAmount),
                    centsToDecimal(invoice.taxAmount),
                    centsToDecimal(invoice.totalAmount),
                    centsToDecimal(invoice.amountPaid),
                    centsToDecimal(invoice.totalAmount - invoice.amountPaid)
                ).joinToString(",")
            )

            // ── Line items section ─────────────────────────────────────────────
            writer.appendLine("")
            writer.appendLine("Line Items")
            writer.appendLine("Description,Quantity,Unit Price,Total")
            invoice.items.forEach { item ->
                writer.appendLine(
                    listOf(
                        csvEscape(item.description),
                        item.quantity.toString(),
                        centsToDecimal(item.unitPrice),
                        centsToDecimal((item.unitPrice * item.quantity).toLong())
                    ).joinToString(",")
                )
            }
        }

        Timber.d("CsvExportService: exported single invoice to ${file.absolutePath}")
        return file
    }

    /**
     * Exports a [list] of invoices as a summary CSV (one row per invoice, no line items).
     * Returns the generated [File].
     */
    suspend fun exportInvoiceList(list: List<Invoice>): File {
        Timber.d("CsvExportService: exporting ${list.size} invoices")

        val fileName = "invoices-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.filesDir, "exports/$fileName")
        file.parentFile?.mkdirs()

        file.bufferedWriter().use { writer ->
            writer.appendLine("Invoice ID,Date,Due Date,Customer,Status,Currency,Total,Amount Paid,Balance")
            list.forEach { invoice ->
                val displayId = invoice.displayName.ifBlank { invoice.invoiceNumber }
                writer.appendLine(
                    listOf(
                        csvEscape(displayId),
                        dateFormat.format(Date(invoice.date)),
                        if (invoice.dueDate > 0) dateFormat.format(Date(invoice.dueDate)) else "",
                        csvEscape(invoice.customerName),
                        invoice.status.name,
                        invoice.currencyCode,
                        centsToDecimal(invoice.totalAmount),
                        centsToDecimal(invoice.amountPaid),
                        centsToDecimal(invoice.totalAmount - invoice.amountPaid)
                    ).joinToString(",")
                )
            }
        }

        Timber.d("CsvExportService: exported invoice list to ${file.absolutePath}")
        return file
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Converts cents (Long) to a decimal string, e.g. 14999 → "149.99". */
    private fun centsToDecimal(cents: Long): String =
        String.format(Locale.US, "%.2f", cents / 100.0)

    /**
     * Wraps [value] in double-quotes and escapes any embedded double-quotes
     * per RFC 4180 (doubled double-quote).
     */
    private fun csvEscape(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    /** Strips characters that are unsafe in file names. */
    private fun sanitizeForFilename(value: String): String =
        value.replace(Regex("[^a-zA-Z0-9._-]"), "_")
}
