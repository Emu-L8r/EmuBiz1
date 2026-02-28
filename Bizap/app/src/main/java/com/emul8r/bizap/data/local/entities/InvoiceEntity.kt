package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String = "",
    val customerAddress: String = "",
    val customerEmail: String? = null,
    val date: Long,
    val totalAmount: Double,
    val isQuote: Boolean,
    val status: String,
    val header: String? = null,
    val subheader: String? = null,
    val notes: String? = null,
    val footer: String? = null,
    val photoUris: String? = null,
    val pdfUri: String? = null,
    val dueDate: Long = 0,
    val taxRate: Double = 0.1,
    val taxAmount: Double = 0.0,
    val companyLogoPath: String? = null,
    val updatedAt: Long = 0,
    
    // PHASE 3A: Management & Audit Fields
    val amountPaid: Double = 0.0,
    val parentInvoiceId: Long? = null,
    val version: Int = 1,

    // NEW: Professional numbering fields
    val invoiceYear: Int = 0,
    val invoiceSequence: Int = 0
)
