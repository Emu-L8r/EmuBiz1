package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Invoice payment record - tracks each individual payment against an invoice.
 *
 * ✅ ENHANCED: Now supports notes and proof media
 */
@Entity(
    tableName = "invoice_payments",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class InvoicePaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val amountPaid: Long,  // Cents (e.g., 14999 = $149.99)
    val paymentDate: Long,  // Unix timestamp
    val paymentMethod: String,  // "CASH", "CHECK", "CREDIT_CARD", "ACH", "WIRE", etc.
    val transactionReference: String,
    val notes: String? = null,  // ✅ Payment context (why, agreed terms, etc.)
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

/**
 * ✅ NEW: Payment Media Attachment - Proof of payment
 *
 * Supports:
 * - Receipt photos
 * - Check photos
 * - Transaction confirmations
 * - Payment proof
 * - Before/after photos
 */
@Entity(
    tableName = "payment_media_attachments",
    foreignKeys = [
        ForeignKey(
            entity = InvoicePaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("paymentId"), Index("invoiceId")]
)
data class PaymentMediaAttachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paymentId: Long,          // Reference to invoice_payments.id
    val invoiceId: Long,          // Denormalized for queries
    val mediaUri: String,         // Local file path or content URI
    val mediaType: String,        // "image/jpeg", "image/png", etc.
    val caption: String? = null,  // "Receipt", "Check #456", "Payment proof", etc.
    val mediaSize: Long = 0,      // Bytes
    val uploadedAtMs: Long = System.currentTimeMillis()
)

/**
 * Payment status snapshot - fast lookup for payment status of invoices.
 */
@Entity(
    tableName = "invoice_payment_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("businessProfileId"),
        Index("customerId"),
        Index("paymentStatus"),
        Index("ageingBucket"),
        Index("isAtRisk")
    ]
)
data class InvoicePaymentSnapshot(
    @PrimaryKey
    val invoiceId: Long,
    val businessProfileId: Long,
    val customerId: Long,
    val customerName: String,
    val invoiceNumber: String,
    val invoiceDate: Long,
    val dueDate: Long,

    val totalAmount: Long,           // Cents
    val paidAmount: Long,            // Cents
    val outstandingAmount: Long,     // Cents

    val paymentStatus: String,
    val ageingBucket: String,
    val daysOverdue: Int,
    val daysSinceDue: Int,

    val lastPaymentDate: Long? = null,
    val lastPaymentAmount: Long = 0,  // Cents
    val paymentCount: Int = 0,

    val isAtRisk: Boolean = false,
    val riskScore: Double = 0.0,
    val riskFactors: String = "",

    val lastUpdatedMs: Long = System.currentTimeMillis(),
    val snapshotDateMs: Long = System.currentTimeMillis()
)

/**
 * Daily payment activity snapshot for cash flow tracking.
 */
@Entity(
    tableName = "daily_payment_snapshots",
    indices = [
        Index("businessProfileId"),
        Index("snapshotDate")
    ]
)
data class DailyPaymentSnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessProfileId: Long,
    val snapshotDate: Long,

    val paymentsReceivedCount: Int,
    val paymentsReceivedAmount: Long,      // Cents
    val invoicesDueCount: Int,
    val invoicesDueAmount: Long,           // Cents
    val invoicesOverdueCount: Int,
    val invoicesOverdueAmount: Long,       // Cents

    val outstandingCurrent: Long,          // Cents
    val outstandingPast30: Long,           // Cents
    val outstandingPast60: Long,           // Cents
    val outstandingPast90: Long,           // Cents

    val collectionRate: Double,
    val averagePaymentTime: Double,
    val projectedMonthlyRevenue: Long,     // Cents

    val createdAtMs: Long = System.currentTimeMillis()
)

/**
 * Collection efficiency metrics.
 */
@Entity(tableName = "collection_metrics")
data class CollectionMetrics(
    @PrimaryKey
    val businessProfileId: Long,
    val metricsDate: Long,

    val totalInvoicesIssued: Int,
    val totalInvoiceAmount: Long,      // Cents
    val totalPaidAmount: Long,         // Cents
    val totalOutstandingAmount: Long,  // Cents
    val collectionRate: Double,

    val ageingCurrent: Long,           // Cents
    val ageingPast30: Long,            // Cents
    val ageingPast60: Long,            // Cents
    val ageingPast90: Long,            // Cents

    val averageDaysToPayment: Double,
    val medianDaysToPayment: Double,
    val overdueInvoiceCount: Int,
    val overdueAmount: Long,           // Cents

    val collectionRateTrend: Double,
    val overdueTrend: Double,

    val projectedCollectionRate30Days: Double,
    val projectedOutstanding30Days: Long,  // Cents

    val lastUpdatedMs: Long = System.currentTimeMillis()
)
