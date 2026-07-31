package com.emul8r.bizap.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class Invoice(
    // Core Invoice Fields
    val id: Long = 0,
    val businessProfileId: Long = 1,
    val customerId: Long?,
    val invoiceNumber: String = "",
    val dateCreated: String = "", // ISO-8601
    val dueDate: String = "",      // ISO-8601
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val items: List<InvoiceItem> = emptyList(),

    // Customer Information
    val customerName: String = "",
    val customerAddress: String = "",
    val customerEmail: String? = null,
    val customerPhone: String? = null,

    // Amount Fields (in cents)
    val totalAmount: Long = 0L,
    val taxRate: Double = 0.0,
    val taxAmount: Long = 0L,
    val amountPaid: Long = 0L,
    val discount: Double = 0.0,
    val discountAmount: Long = 0L,

    // Template Fields
    val notes: String? = null,
    val header: String? = null,
    val subheader: String? = null,
    val footer: String? = null,
    val photoUris: List<String> = emptyList(),
    val pdfUri: String? = null,
    val companyLogoPath: String? = null,

    // Versioning & Sequence Fields
    val version: Int = 1,
    val dailySequence: Int = 0,
    val invoiceYear: Int = 0,
    val invoiceSequence: Int = 0,
    val dailyCounter: Int = 0,
    val parentInvoiceId: Long? = null,

    // Display & Format Fields
    val currency: String = "AUD",
    val currencyCode: String? = null,  // Alias/override for currency (nullable for backward compatibility)
    val displayName: String = "",
    val isQuote: Boolean = false,
    val isActive: Boolean = true,

    // Timestamps
    val datePaid: String? = null,
    val updatedAt: Long = 0L,
    val createdAt: Long = 0L
)

@Serializable
data class InvoiceItem(
    val id: Long = 0,
    val description: String,
    val quantity: Double,
    val unitPrice: Long, // Cents
    val taxRate: Double = 0.0
)

@Serializable
enum class InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    OVERDUE,
    PARTIALLY_PAID,
    CANCELLED
}
