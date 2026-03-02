package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(name = "idx_invoices_business", value = ["businessProfileId"]),
        Index(name = "idx_invoices_customer", value = ["customerId"]),
        Index(name = "idx_invoices_status", value = ["status"]),
        Index(name = "idx_invoices_business_status", value = ["businessProfileId", "status"]),
        Index(name = "idx_invoices_year_sequence", value = ["invoiceYear", "invoiceSequence", "businessProfileId"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessProfileId: Long = 1,
    val customerId: Long?,  // Nullable to support SET_NULL when customer is deleted
    val customerName: String = "",
    val customerAddress: String = "",
    val customerEmail: String? = null,
    val date: Long,
    val totalAmount: Long,              // Store as cents (e.g., 14999 = $149.99)
    val isQuote: Boolean,
    val status: String,
    val header: String? = null,
    val subheader: String? = null,
    val notes: String? = null,
    val footer: String? = null,
    val photoUris: String? = null,
    val pdfUri: String? = null,
    val dueDate: Long = 0,
    val taxRate: Double = 0.1,          // Rate stays Double (e.g., 0.1 for 10%)
    val taxAmount: Long = 0,            // Store as cents
    val companyLogoPath: String? = null,
    val updatedAt: Long = 0,
    val amountPaid: Long = 0,           // Store as cents
    val parentInvoiceId: Long? = null,
    val version: Int = 1,
    val invoiceYear: Int = 0,
    val invoiceSequence: Int = 0,
    val currencyCode: String = "AUD",
    // Template integration fields (Phase 5)
    val templateId: String? = null,              // Reference to InvoiceTemplate used
    val templateSnapshot: String? = null,        // JSON snapshot of template at creation
    val customFieldValues: String? = null        // JSON map of custom field values {fieldId: value}
)

