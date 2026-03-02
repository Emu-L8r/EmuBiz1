package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Invoice payment record - tracks each individual payment against an invoice.
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
    val amountPaid: Double,
    val paymentDate: Long,  // Unix timestamp
    val paymentMethod: String,  // "CASH", "CHECK", "CREDIT_CARD", etc.
    val transactionReference: String,
    val notes: String? = null,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
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
    
    val totalAmount: Double,
    val paidAmount: Double,
    val outstandingAmount: Double,
    
    val paymentStatus: String,
    val ageingBucket: String,
    val daysOverdue: Int,
    val daysSinceDue: Int,
    
    val lastPaymentDate: Long? = null,
    val lastPaymentAmount: Double = 0.0,
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
    val paymentsReceivedAmount: Double,
    val invoicesDueCount: Int,
    val invoicesDueAmount: Double,
    val invoicesOverdueCount: Int,
    val invoicesOverdueAmount: Double,
    
    val outstandingCurrent: Double,
    val outstandingPast30: Double,
    val outstandingPast60: Double,
    val outstandingPast90: Double,
    
    val collectionRate: Double,
    val averagePaymentTime: Double,
    val projectedMonthlyRevenue: Double,
    
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
    val totalInvoiceAmount: Double,
    val totalPaidAmount: Double,
    val totalOutstandingAmount: Double,
    val collectionRate: Double,
    
    val ageingCurrent: Double,
    val ageingPast30: Double,
    val ageingPast60: Double,
    val ageingPast90: Double,
    
    val averageDaysToPayment: Double,
    val medianDaysToPayment: Double,
    val overdueInvoiceCount: Int,
    val overdueAmount: Double,
    
    val collectionRateTrend: Double,
    val overdueTrend: Double,
    
    val projectedCollectionRate30Days: Double,
    val projectedOutstanding30Days: Double,
    
    val lastUpdatedMs: Long = System.currentTimeMillis()
)

