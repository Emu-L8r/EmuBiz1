package com.emul8r.bizap.ui.theme

import androidx.compose.ui.graphics.Color
import com.emul8r.bizap.domain.model.InvoiceStatus

/**
 * Status color definitions for consistent visual indicators across the app.
 * These colors are used for badges, cards, and metric displays.
 */
object StatusColors {
    // Primary status colors
    val Paid = Color(0xFF4CAF50)           // Green - Positive, collected revenue
    val Sent = Color(0xFF2196F3)           // Blue - Awaiting payment
    val Draft = Color(0xFF999999)          // Gray - Not yet sent
    val Overdue = Color(0xFFB3261E)        // Red - Past due, at risk
    val Outstanding = Color(0xFFFFA500)    // Orange - Expected but not collected
    val PartiallyPaid = Color(0xFFFFA500)  // Orange - Partial payment received
    
    // Darker variants for text
    val PaidDark = Color(0xFF2E7D32)
    val SentDark = Color(0xFF1565C0)
    val DraftDark = Color(0xFF666666)
    val OverdueDark = Color(0xFFC62828)
    val OutstandingDark = Color(0xFFE65100)
    val PartiallyPaidDark = Color(0xFFE65100)
}

/**
 * Extension functions to get colors based on invoice status.
 */
fun InvoiceStatus.getStatusColor(): Color {
    return when (this) {
        InvoiceStatus.PAID -> StatusColors.Paid
        InvoiceStatus.SENT -> StatusColors.Sent
        InvoiceStatus.DRAFT -> StatusColors.Draft
        InvoiceStatus.OVERDUE -> StatusColors.Overdue
        InvoiceStatus.PARTIALLY_PAID -> StatusColors.PartiallyPaid
    }
}

fun InvoiceStatus.getStatusColorDark(): Color {
    return when (this) {
        InvoiceStatus.PAID -> StatusColors.PaidDark
        InvoiceStatus.SENT -> StatusColors.SentDark
        InvoiceStatus.DRAFT -> StatusColors.DraftDark
        InvoiceStatus.OVERDUE -> StatusColors.OverdueDark
        InvoiceStatus.PARTIALLY_PAID -> StatusColors.PartiallyPaidDark
    }
}

fun InvoiceStatus.getBackgroundColor(): Color {
    return this.getStatusColor().copy(alpha = 0.12f)
}

fun InvoiceStatus.getBorderColor(): Color {
    return this.getStatusColor().copy(alpha = 0.3f)
}

/**
 * Get status color from string (for compatibility with existing code).
 */
fun String.getStatusColor(): Color {
    return when (this.uppercase()) {
        "PAID" -> StatusColors.Paid
        "SENT" -> StatusColors.Sent
        "DRAFT" -> StatusColors.Draft
        "OVERDUE" -> StatusColors.Overdue
        "PARTIALLY_PAID" -> StatusColors.PartiallyPaid
        else -> StatusColors.Draft
    }
}

fun String.getStatusColorDark(): Color {
    return when (this.uppercase()) {
        "PAID" -> StatusColors.PaidDark
        "SENT" -> StatusColors.SentDark
        "DRAFT" -> StatusColors.DraftDark
        "OVERDUE" -> StatusColors.OverdueDark
        "PARTIALLY_PAID" -> StatusColors.PartiallyPaidDark
        else -> StatusColors.DraftDark
    }
}
