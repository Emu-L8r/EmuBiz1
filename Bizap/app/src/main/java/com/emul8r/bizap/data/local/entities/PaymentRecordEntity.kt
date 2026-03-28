package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Payment record entity for tracking payments against invoices.
 *
 * Stores payment information including amount, method, and notes.
 * Links to invoices for payment tracking.
 */
@Entity(
    tableName = "payment_records",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PaymentRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val amount: Long, // in cents
    val paymentMethod: String, // CASH, CHECK, ACH_TRANSFER, WIRE_TRANSFER, CREDIT_CARD, DEBIT_CARD, MOBILE_PAYMENT, OTHER
    val notes: String? = null,
    val recordedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Payment method enum for categorizing payment types.
 */
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    CHECK("Check"),
    ACH_TRANSFER("ACH Transfer"),
    WIRE_TRANSFER("Wire Transfer"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    MOBILE_PAYMENT("Mobile Payment"),
    OTHER("Other")
}

