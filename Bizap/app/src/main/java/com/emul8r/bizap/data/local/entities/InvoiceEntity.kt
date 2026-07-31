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
        Index(name = "idx_invoices_business_date", value = ["businessProfileId", "date"]),
        Index(name = "idx_invoices_date_sequence", value = ["date", "dailySequence"]),
        Index(name = "idx_invoices_date", value = ["date"]),
        Index(name = "idx_invoices_customer_date", value = ["customerId", "date"]),
        Index(name = "idx_invoices_number_business", value = ["invoiceNumber", "businessProfileId"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessProfileId: Long = 1,
    val customerId: Long?,  // Nullable to support SET_NULL when customer is deleted
    val customerName: String = "",
    val customerAddress: String = "",
    val customerEmail: String? = null,
    val customerPhone: String? = null,
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
    val dailySequence: Int,             // Sequence for the day (1-99)
    val invoiceYear: Int = 0,           // Year for yearly invoice numbering
    val invoiceSequence: Int = 0,       // Annual sequence number
    val currencyCode: String = "AUD",
    // Template integration fields (Phase 5)
    val templateId: String? = null,              // Reference to InvoiceTemplate used
    val templateSnapshot: String? = null,        // JSON snapshot of template at creation
    val customFieldValues: String? = null,       // JSON map of custom field values {fieldId: value}
    // GUI2 Phase 2 fields
    val invoiceNumber: String = "",              // Human-readable number, unique per business
    val isActive: Boolean = true,               // Soft-delete flag
    val createdAt: Long = 0,                    // Creation timestamp (ms)
    // v1.0.1 display name fields
    val dailyCounter: Int = 0,                  // Daily reset counter (1, 2, 3…)
    val displayName: String = "",                // Computed display name: customername-ddMMyyyy-01
    val discountAmount: Long = 0L               // Discount in cents; default 0 (no discount)
) {
    init {
        // Validate that dueDate is not before invoice date (WIN #8: Validate dates)
        if (dueDate > 0 && date > 0 && dueDate < date) {
            throw IllegalArgumentException(
                "Invoice due date ($dueDate) cannot be before invoice date ($date)"
            )
        }
    }
}
