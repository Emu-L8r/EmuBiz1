package com.emul8r.bizap.ui.settings.invoice_theme

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Maps invoice data to template variables for HTML rendering.
 *
 * Handles:
 * - Data transformation and formatting
 * - Currency formatting
 * - Date formatting
 * - Null value handling
 */
class TemplateDataMapper {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencyFormat = SimpleDateFormat("0.00", Locale.getDefault())

    /**
     * Map raw invoice data to template variables.
     *
     * @param invoiceNumber Invoice number
     * @param invoiceDate Invoice date
     * @param dueDate Due date
     * @param customerName Customer name
     * @param customerEmail Customer email
     * @param customerPhone Customer phone
     * @param customerAddress Customer address
     * @param companyName Company name
     * @param businessEmail Business email
     * @param businessPhone Business phone
     * @param businessAddress Business address
     * @param items List of invoice items
     * @param subtotal Subtotal amount
     * @param taxAmount Tax amount
     * @param totalAmount Total amount
     * @param taxRate Tax rate percentage
     * @param taxName Tax name (GST, VAT, etc.)
     * @param paymentTermsDays Payment terms in days
     * @param bankName Bank name (optional)
     * @param accountNumber Account number (optional)
     * @param notes Invoice notes (optional)
     * @param status Invoice status (optional)
     * @param websiteUrl Company website (optional)
     * @return Map of template variables
     */
    fun mapToTemplate(
        invoiceNumber: String,
        invoiceDate: Date,
        dueDate: Date,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        customerAddress: String,
        companyName: String,
        businessEmail: String,
        businessPhone: String,
        businessAddress: String,
        items: List<Map<String, Any>>,
        subtotal: Double,
        taxAmount: Double,
        totalAmount: Double,
        taxRate: Double,
        taxName: String,
        paymentTermsDays: Int,
        bankName: String? = null,
        accountNumber: String? = null,
        notes: String? = null,
        status: String = "Unpaid",
        websiteUrl: String? = null
    ): Map<String, Any?> {
        return try {
            Timber.d("Mapping invoice data to template variables")

            mapOf(
                // Invoice metadata
                "invoiceNumber" to invoiceNumber,
                "invoiceDate" to formatDate(invoiceDate),
                "dueDate" to formatDate(dueDate),
                "status" to status,

                // Customer information
                "customerName" to customerName,
                "customerEmail" to customerEmail,
                "customerPhone" to customerPhone,
                "customerAddress" to customerAddress,

                // Company information
                "companyName" to companyName,
                "businessEmail" to businessEmail,
                "businessPhone" to businessPhone,
                "businessAddress" to businessAddress,
                "websiteUrl" to websiteUrl,

                // Items
                "items" to items,

                // Totals
                "subtotal" to subtotal,
                "taxAmount" to taxAmount,
                "totalAmount" to totalAmount,
                "taxRate" to taxRate,
                "taxName" to taxName,

                // Payment details
                "paymentTermsDays" to paymentTermsDays,
                "bankName" to bankName,
                "accountNumber" to accountNumber,
                "showPaymentDetails" to (bankName != null || accountNumber != null),

                // Notes
                "notes" to notes
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to map invoice data to template")
            throw e
        }
    }

    /**
     * Format invoice item for template.
     *
     * @param description Item description
     * @param quantity Item quantity
     * @param unitPrice Unit price
     * @param amount Total amount
     * @return Formatted item map
     */
    fun formatItem(
        description: String,
        quantity: Double,
        unitPrice: Double,
        amount: Double
    ): Map<String, Any> {
        return mapOf(
            "description" to description,
            "quantity" to quantity,
            "unitPrice" to unitPrice,
            "amount" to amount
        )
    }

    /**
     * Format date for display.
     *
     * @param date Date to format
     * @return Formatted date string
     */
    private fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    /**
     * Format currency value.
     *
     * @param amount Amount to format
     * @return Formatted amount string
     */
    fun formatCurrency(amount: Double): String {
        return String.format(Locale.getDefault(), "%.2f", amount)
    }
}

