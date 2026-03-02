package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Denormalized snapshot of invoice for fast analytics queries
 * Updated whenever an invoice is created/updated
 */
@Entity(
    tableName = "invoice_analytics_snapshots",
    indices = [
        Index(name = "idx_analytics_business", value = ["businessProfileId"]),
        Index(name = "idx_analytics_date", value = ["invoiceDateMs"]),
        Index(name = "idx_analytics_status", value = ["status"]),
        Index(name = "idx_analytics_currency", value = ["currencyCode"])
    ]
)
data class InvoiceAnalyticsSnapshot(
    @PrimaryKey
    val invoiceId: Long,

    val businessProfileId: Long,
    val customerId: Long,
    val customerName: String,

    val invoiceNumber: String,
    val currencyCode: String,

    // Financial data
    val subtotal: Long,                 // Cents
    val taxAmount: Long,                // Cents
    val totalAmount: Long,              // Cents

    // Status tracking
    val status: String, // DRAFT, SENT, PAID, OVERDUE, PARTIALLY_PAID
    val isPaid: Boolean,
    val isOverdue: Boolean,

    // Dates for analytics
    val invoiceDateMs: Long, // Invoice date as milliseconds
    val createdAtMs: Long,
    val paidAtMs: Long? = null, // When it was marked paid
    val daysPending: Int = 0, // Days since creation

    // Metadata
    val lineItemCount: Int = 0,
    val snapshotCreatedAtMs: Long = System.currentTimeMillis()
)


