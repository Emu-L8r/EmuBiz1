package com.emul8r.bizap.ui.settings.invoice_theme

import timber.log.Timber
import java.text.DecimalFormat
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
 * - Number formatting
 *
 * Phase 6 Enhancement: Added comprehensive helper functions and formatting
 */
class TemplateDataMapper {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val longDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    private val currencyFormatter = DecimalFormat("0.00")
    private val percentageFormatter = DecimalFormat("0.00")

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
     * Format currency value with proper formatting.
     *
     * @param amount Amount to format
     * @return Formatted currency string (e.g., "1,234.56")
     */
    fun formatCurrency(amount: Double): String {
        return try {
            String.format(Locale.getDefault(), "%,.2f", amount)
        } catch (e: Exception) {
            Timber.e(e, "Failed to format currency: $amount")
            "0.00"
        }
    }

    /**
     * Format percentage value.
     *
     * @param percentage Percentage value (0-100)
     * @return Formatted percentage string
     */
    fun formatPercentage(percentage: Double): String {
        return try {
            percentageFormatter.format(percentage)
        } catch (e: Exception) {
            Timber.e(e, "Failed to format percentage: $percentage")
            "0.00"
        }
    }

    /**
     * Format quantity with proper decimal places.
     *
     * @param quantity Quantity value
     * @return Formatted quantity string
     */
    fun formatQuantity(quantity: Double): String {
        return try {
            if (quantity == quantity.toLong().toDouble()) {
                quantity.toLong().toString()
            } else {
                String.format(Locale.getDefault(), "%.2f", quantity)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to format quantity: $quantity")
            "1"
        }
    }

    /**
     * Format date for display in long format (e.g., "March 30, 2026").
     *
     * @param date Date to format
     * @return Formatted date string
     */
    fun formatDateLong(date: Date): String {
        return try {
            longDateFormat.format(date)
        } catch (e: Exception) {
            Timber.e(e, "Failed to format date (long): $date")
            dateFormat.format(date)
        }
    }

    /**
     * Format date for display in short format (e.g., "Mar 30, 2026").
     *
     * @param date Date to format
     * @return Formatted date string
     */
    fun formatDateShort(date: Date): String {
        return try {
            dateFormat.format(date)
        } catch (e: Exception) {
            Timber.e(e, "Failed to format date (short): $date")
            "Invalid date"
        }
    }

    /**
     * Check if value is null or empty string.
     *
     * @param value Value to check
     * @return True if null or empty
     */
    fun isEmpty(value: Any?): Boolean {
        return when (value) {
            null -> true
            is String -> value.trim().isEmpty()
            else -> false
        }
    }

    /**
     * Get safe string value with default.
     *
     * @param value Value to convert
     * @param default Default value if null/empty
     * @return String value or default
     */
    fun safeString(value: Any?, default: String = ""): String {
        return when {
            value == null -> default
            value is String -> if (value.trim().isEmpty()) default else value
            else -> value.toString()
        }
    }
}
