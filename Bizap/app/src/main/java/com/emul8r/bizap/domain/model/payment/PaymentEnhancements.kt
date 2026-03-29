package com.emul8r.bizap.domain.model.payment

import kotlinx.serialization.Serializable

/**
 * Supported payment methods in Bizap.
 */
@Serializable
enum class PaymentMethod {
    CASH,
    CHECK,
    BANK_TRANSFER,
    CREDIT_CARD,
    DEBIT_CARD,
    MOBILE_PAYMENT,
    WIRE_TRANSFER,
    OTHER;

    override fun toString(): String = when (this) {
        CASH -> "Cash"
        CHECK -> "Check"
        BANK_TRANSFER -> "Bank Transfer"
        CREDIT_CARD -> "Credit Card"
        DEBIT_CARD -> "Debit Card"
        MOBILE_PAYMENT -> "Mobile Payment"
        WIRE_TRANSFER -> "Wire Transfer"
        OTHER -> "Other"
    }

    companion object {
        fun fromLabel(label: String): PaymentMethod? {
            return values().find { it.toString() == label }
        }
    }
}

/**
 * Enhanced payment record with method tracking.
 */
@Serializable
data class PaymentRecordEnhanced(
    val paymentId: Long,
    val invoiceId: Long,
    val amount: Long,  // in cents
    val paymentMethod: PaymentMethod,
    val paymentDate: Long,  // ms
    val notes: String = "",
    val referenceNumber: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayAmount: String get() = "$${"%.2f".format(amount / 100.0)}"
}

/**
 * Payment summary by method.
 */
@Serializable
data class PaymentMethodSummary(
    val method: PaymentMethod,
    val totalAmount: Long,
    val transactionCount: Int,
    val averageAmount: Long,
    val lastPaymentDate: Long?
) {
    val displayAmount: String get() = "$${"%.2f".format(totalAmount / 100.0)}"
}

/**
 * Partial payment tracking.
 */
@Serializable
data class PartialPaymentInfo(
    val invoiceId: Long,
    val totalInvoiceAmount: Long,
    val amountPaid: Long,
    val remainingAmount: Long,
    val paymentHistory: List<PaymentRecordEnhanced>,
    val isFullyPaid: Boolean = amountPaid >= totalInvoiceAmount,
    val paymentPercent: Double = (amountPaid.toDouble() / totalInvoiceAmount) * 100
)

