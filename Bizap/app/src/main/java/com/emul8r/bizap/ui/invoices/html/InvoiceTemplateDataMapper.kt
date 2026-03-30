package com.emul8r.bizap.ui.invoices.html

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSettings
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Maps Invoice domain objects to HTML template data format.
 *
 * Converts:
 * - Invoice entity → Template variables
 * - Numbers → Formatted strings (currency, percentages, etc.)
 * - Dates → Readable format
 * - Settings → Company/payment details
 *
 * This mapper prepares all data needed for the Freemarker template rendering.
 */
class InvoiceTemplateDataMapper {

    /**
     * Map invoice and settings to template data.
     *
     * @param invoice The invoice to map
     * @param settings The invoice settings for customization
     * @return Map of template variables ready for Freemarker processing
     */
    fun mapToTemplateData(
        invoice: Invoice,
        settings: InvoiceSettings
    ): Map<String, Any> {
        return try {
            val mappedData = mutableMapOf<String, Any>()

            // ===== COMPANY/BUSINESS INFORMATION =====
            mappedData["companyName"] = settings.businessName
            mappedData["businessEmail"] = settings.businessEmail
            mappedData["businessPhone"] = settings.businessPhone
            mappedData["businessAddress"] = settings.businessAddress

            if (!settings.businessWebsite.isNullOrBlank()) {
                mappedData["businessWebsite"] = settings.businessWebsite
            }
            if (!settings.taxId.isNullOrBlank()) {
                mappedData["taxId"] = settings.taxId
            }

            // ===== CLIENT/CUSTOMER INFORMATION =====
            mappedData["clientName"] = invoice.customerName
            mappedData["clientAddress"] = invoice.customerAddress
            mappedData["clientEmail"] = invoice.customerEmail.orEmpty()

            // ===== INVOICE DETAILS =====
            mappedData["invoiceNumber"] = invoice.invoiceNumber
            mappedData["invoiceDate"] = formatDate(invoice.date)
            mappedData["dueDate"] = formatDate(invoice.dueDate)
            mappedData["invoiceStatus"] = invoice.status.name

            // ===== LINE ITEMS =====
            mappedData["items"] = invoice.items.mapIndexed { index, lineItem ->
                mapLineItem(lineItem, invoice.currencyCode, index + 1)
            }

            // ===== FINANCIAL CALCULATIONS =====
            val subtotal = calculateSubtotal(invoice.items)
            val tax = calculateTax(subtotal, invoice.taxRate)
            val total = subtotal + tax

            mappedData["subtotal"] = formatCurrency(subtotal)
            mappedData["subtotalRaw"] = subtotal.toLong()

            mappedData["taxRate"] = invoice.taxRate
            mappedData["taxRatePercent"] = formatPercentage(invoice.taxRate)
            mappedData["taxName"] = settings.taxName
            mappedData["taxAmount"] = formatCurrency(tax)

            mappedData["total"] = formatCurrency(total)
            mappedData["totalRaw"] = total.toLong()

            // ===== PAYMENT INFORMATION =====
            if (settings.paymentTermsDays > 0) {
                mappedData["paymentTerms"] = "${settings.paymentTermsDays} days"
            }

            if (!settings.bankName.isNullOrBlank()) {
                mappedData["bankName"] = settings.bankName
            }
            if (!settings.accountNumber.isNullOrBlank()) {
                mappedData["accountNumber"] = maskAccountNumber(settings.accountNumber)
            }

            // ===== ADDITIONAL INFORMATION =====
            if (!invoice.notes.isNullOrBlank()) {
                mappedData["notes"] = invoice.notes
            }

            // ===== STYLING & BRANDING =====
            mappedData["primaryColor"] = settings.primaryColor
            mappedData["currencyCode"] = invoice.currencyCode

            // ===== AMOUNT PAID (if applicable) =====
            if (invoice.amountPaid > 0) {
                mappedData["amountPaid"] = formatCurrencyFromCents(invoice.amountPaid)
            }

            Timber.d("Invoice data mapped successfully for template - invoice: ${invoice.invoiceNumber}")
            mappedData
        } catch (e: Exception) {
            Timber.e(e, "Error mapping invoice to template data")
            emptyMap()
        }
    }

    /**
     * Map a single line item to template format.
     */
    private fun mapLineItem(
        lineItem: com.emul8r.bizap.domain.model.LineItem,
        currencyCode: String,
        itemNumber: Int
    ): Map<String, Any> {
        return mapOf(
            "itemNumber" to itemNumber,
            "description" to lineItem.description,
            "quantity" to formatQuantity(lineItem.quantity),
            "quantityRaw" to lineItem.quantity,
            "unitPrice" to formatCurrencyFromCents(lineItem.unitPrice),
            "unitPriceRaw" to lineItem.unitPrice,
            "total" to formatCurrencyFromCents(lineItem.calculateTotal()),
            "totalRaw" to lineItem.calculateTotal()
        )
    }

    /**
     * Calculate total from cents (Long).
     */
    private fun com.emul8r.bizap.domain.model.LineItem.calculateTotal(): Long {
        return (unitPrice.toDouble() * quantity).toLong()
    }

    /**
     * Calculate subtotal from all line items (in dollars).
     */
    private fun calculateSubtotal(items: List<com.emul8r.bizap.domain.model.LineItem>): Double {
        return items.sumOf { (it.unitPrice.toDouble() * it.quantity) / 100.0 }
    }

    /**
     * Calculate tax amount based on subtotal and rate (in dollars).
     */
    private fun calculateTax(subtotal: Double, taxRate: Double): Double {
        return subtotal * taxRate
    }

    /**
     * Format currency amount (in dollars) as string with symbol.
     */
    private fun formatCurrency(amount: Double): String {
        return try {
            String.format(Locale.US, "$%.2f", amount)
        } catch (e: Exception) {
            Timber.e(e, "Error formatting currency: $amount")
            "$0.00"
        }
    }

    /**
     * Format currency from cents (Long) to string.
     */
    private fun formatCurrencyFromCents(cents: Long): String {
        val dollars = cents.toDouble() / 100.0
        return formatCurrency(dollars)
    }

    /**
     * Format quantity with appropriate decimal places.
     */
    private fun formatQuantity(quantity: Double): String {
        return try {
            if (quantity == quantity.toLong().toDouble()) {
                quantity.toLong().toString()
            } else {
                String.format(Locale.US, "%.2f", quantity)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error formatting quantity: $quantity")
            quantity.toString()
        }
    }

    /**
     * Format percentage.
     */
    private fun formatPercentage(rate: Double): String {
        return try {
            String.format(Locale.US, "%.1f%%", rate * 100)
        } catch (e: Exception) {
            Timber.e(e, "Error formatting percentage: $rate")
            "0%"
        }
    }

    /**
     * Format date from Long timestamp to readable format.
     */
    private fun formatDate(timestamp: Long): String {
        return try {
            if (timestamp <= 0) return ""
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            Timber.e(e, "Error formatting date: $timestamp")
            ""
        }
    }

    /**
     * Mask account number for privacy.
     */
    private fun maskAccountNumber(accountNumber: String): String {
        return try {
            if (accountNumber.length <= 4) return accountNumber
            val lastFour = accountNumber.takeLast(4)
            "**** **** **** $lastFour"
        } catch (e: Exception) {
            Timber.e(e, "Error masking account number")
            accountNumber
        }
    }
}

