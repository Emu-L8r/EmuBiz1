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
    val PaidDark = Color(0xFF1B5E20)       // Darker green for better contrast
    val SentDark = Color(0xFF0D47A1)       // Darker blue for better contrast
    val DraftDark = Color(0xFF424242)      // Darker gray for better contrast
    val OverdueDark = Color(0xFF7B1217)    // Darker red for better contrast
    val OutstandingDark = Color(0xFFBF360C) // Darker orange for better contrast
    val PartiallyPaidDark = Color(0xFFBF360C) // Darker orange for better contrast

    // Background colors (soft, light) for cards
    val PaidBackground = Color(0xFFD4EDDA)      // Soft green
    val SentBackground = Color(0xFFCCE5FF)      // Soft blue
    val DraftBackground = Color(0xFFF5F5F5)     // Soft gray
    val OverdueBackground = Color(0xFFF8D7DA)   // Soft red/pink
    val PartiallyPaidBackground = Color(0xFFFFE0B2) // Soft orange

    // Border colors (darker/saturated) for cards
    val PaidBorder = Color(0xFF81C784)     // Green border
    val SentBorder = Color(0xFF9ECBFF)     // Blue border
    val DraftBorder = Color(0xFFCCCCCC)    // Gray border
    val OverdueBorder = Color(0xFFEF9A9A)  // Red border
    val PartiallyPaidBorder = Color(0xFFFFCC80) // Orange border
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
        InvoiceStatus.CANCELLED -> Color(0xFF555555) // Dark gray for cancelled
    }
}

fun InvoiceStatus.getStatusColorDark(): Color {
    return when (this) {
        InvoiceStatus.PAID -> StatusColors.PaidDark
        InvoiceStatus.SENT -> StatusColors.SentDark
        InvoiceStatus.DRAFT -> StatusColors.DraftDark
        InvoiceStatus.OVERDUE -> StatusColors.OverdueDark
        InvoiceStatus.PARTIALLY_PAID -> StatusColors.PartiallyPaidDark
        InvoiceStatus.CANCELLED -> Color(0xFF2A2A2A) // Darker gray for cancelled
    }
}

fun InvoiceStatus.getBackgroundColor(): Color {
    return when (this) {
        InvoiceStatus.PAID -> StatusColors.PaidBackground
        InvoiceStatus.SENT -> StatusColors.SentBackground
        InvoiceStatus.DRAFT -> StatusColors.DraftBackground
        InvoiceStatus.OVERDUE -> StatusColors.OverdueBackground
        InvoiceStatus.PARTIALLY_PAID -> StatusColors.PartiallyPaidBackground
        InvoiceStatus.CANCELLED -> Color(0xFFE8E8E8) // Light gray background for cancelled
    }
}

fun InvoiceStatus.getBorderColor(): Color {
    return when (this) {
        InvoiceStatus.PAID -> StatusColors.PaidBorder
        InvoiceStatus.SENT -> StatusColors.SentBorder
        InvoiceStatus.DRAFT -> StatusColors.DraftBorder
        InvoiceStatus.OVERDUE -> StatusColors.OverdueBorder
        InvoiceStatus.PARTIALLY_PAID -> StatusColors.PartiallyPaidBorder
        InvoiceStatus.CANCELLED -> Color(0xFFAAAAAA) // Gray border for cancelled
    }
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
