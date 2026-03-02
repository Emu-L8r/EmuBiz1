package com.emul8r.bizap.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Invoice(
    val id: Long = 0,
    val businessProfileId: Long = 0,
    val customerId: Long?,  // Nullable when customer is deleted
    val customerName: String,
    val customerAddress: String = "",
    val customerEmail: String? = null,
    val date: Long,
    val dueDate: Long = 0,
    val totalAmount: Long,              // Cents (e.g., 14999 = $149.99)
    val items: List<LineItem>,
    val isQuote: Boolean,
    val status: InvoiceStatus,
    val header: String? = null,
    val subheader: String? = null,
    val notes: String? = null,
    val footer: String? = null,
    val photoUris: List<String> = emptyList(),
    val pdfUri: String? = null,
    val taxRate: Double = 0.1,          // Rate only (e.g., 0.1 = 10%)
    val taxAmount: Long = 0,            // Cents
    val companyLogoPath: String? = null,
    val updatedAt: Long = 0,
    val amountPaid: Long = 0,           // Cents
    val parentInvoiceId: Long? = null,
    val version: Int = 1,
    val invoiceYear: Int = 0,
    val invoiceSequence: Int = 0,
    val currencyCode: String = "AUD"
) {
    val invoiceId: Long get() = id
    val total: Long get() = totalAmount
    val balanceRemaining: Long get() = totalAmount - amountPaid
    val isFullyPaid: Boolean get() = balanceRemaining <= 0

    val invoiceNumber: String
        get() {
            val base = "INV-$invoiceYear-${invoiceSequence.toString().padStart(6, '0')}"
            return if (version > 1) "$base-v$version" else base
        }

    fun validate() {
        require(businessProfileId > 0) { "Business ID is required" }
        require(customerId == null || customerId > 0) { "Customer ID must be valid or null" }
        require(totalAmount > 0) { "Total amount must be greater than zero" }
        require(currencyCode.length == 3) { "Currency code must be 3 characters" }
        require(items.isNotEmpty()) { "Invoice must have at least one line item" }
        items.forEach { it.validate() }
    }

    fun getFormattedInvoiceNumber(): String {
        return invoiceNumber
    }
}

data class LineItem(
    val id: Long = 0,
    val description: String,
    val quantity: Double,
    val unitPrice: Long,                // Cents (e.g., 4999 = $49.99)
    val transientId: String = java.util.UUID.randomUUID().toString()
) {
    val itemId: Long get() = id
    
    fun validate() {
        require(description.isNotBlank()) { "Item description is required" }
        require(quantity > 0) { "Quantity must be greater than zero" }
        require(unitPrice >= 0) { "Unit price cannot be negative" }
    }
}

enum class InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    OVERDUE,
    PARTIALLY_PAID
}
