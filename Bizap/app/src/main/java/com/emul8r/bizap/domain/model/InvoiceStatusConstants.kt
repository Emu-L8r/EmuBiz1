package com.emul8r.bizap.domain.model

/**
 * Invoice status constants - single source of truth for valid statuses.
 * Using constants prevents typos and enables IDE autocomplete.
 *
 * Usage:
 * ```kotlin
 * val status = InvoiceStatusConstants.DRAFT
 * val isValid = InvoiceStatusConstants.isValid(someStatus)
 * val display = InvoiceStatusConstants.getDisplayName(someStatus)
 * ```
 */
object InvoiceStatusConstants {
    const val DRAFT = "DRAFT"
    const val SENT = "SENT"
    const val PAID = "PAID"
    const val OVERDUE = "OVERDUE"
    const val PARTIALLY_PAID = "PARTIALLY_PAID"

    val ALL = setOf(DRAFT, SENT, PAID, OVERDUE, PARTIALLY_PAID)

    /**
     * Validates if a status string is valid.
     * @return true if status is in the set of valid statuses
     */
    fun isValid(status: String): Boolean = status in ALL

    /**
     * Gets display name for UI.
     * @param status The status string to format
     * @return User-friendly display name
     */
    fun getDisplayName(status: String): String = when (status) {
        DRAFT -> "Draft"
        SENT -> "Sent"
        PAID -> "Paid"
        OVERDUE -> "Overdue"
        PARTIALLY_PAID -> "Partially Paid"
        else -> status // Fallback
    }

    /**
     * Gets color emoji indicator for status
     */
    fun getEmoji(status: String): String = when (status) {
        DRAFT -> "📝"
        SENT -> "✉️"
        PAID -> "✅"
        OVERDUE -> "⚠️"
        PARTIALLY_PAID -> "⏳"
        else -> "❓"
    }
}

