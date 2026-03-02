package com.emul8r.bizap.data.service

import com.emul8r.bizap.domain.model.Invoice
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility to convert invoice data into RFC 4180 compliant CSV format.
 */
object InvoiceCsvExporter {

    fun generateCsv(invoices: List<Invoice>): String {
        val sb = StringBuilder()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // CSV Header
        sb.append("Invoice Number,Date,Customer,Total Amount,Currency,Status,Items Count\n")

        // CSV Rows
        invoices.forEach { invoice ->
            sb.append(escapeCsv(invoice.invoiceNumber)).append(",")
            sb.append(escapeCsv(dateFormatter.format(Date(invoice.date)))).append(",")
            sb.append(escapeCsv(invoice.customerName)).append(",")
            sb.append(String.format(Locale.US, "%.2f", invoice.totalAmount / 100.0)).append(",")
            sb.append(escapeCsv(invoice.currencyCode)).append(",")
            sb.append(escapeCsv(invoice.status.name)).append(",")
            sb.append(invoice.items.size).append("\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (needsQuotes) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
