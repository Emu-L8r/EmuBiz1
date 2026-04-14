package com.emul8r.bizap.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model for a line item on an invoice.
 * Represents a single item/service on an invoice.
 */
@Serializable
data class LineItem(
    val id: Long = 0,
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Long = 0L,  // Price per unit in cents (e.g., 4999 = $49.99)
    val currencyCode: String = "AUD"  // Optional: currency for this item
) {
    /**
     * Calculate the total price for this line item.
     * @return Total price in cents (quantity * unitPrice)
     */
    fun calculateTotal(): Long = (quantity * unitPrice).toLong()

    /**
     * Format the unit price as a human-readable string.
     * @return Formatted price (e.g., "$49.99")
     */
    fun formattedUnitPrice(): String {
        val dollars = unitPrice / 100
        val cents = unitPrice % 100
        return String.format("$%d.%02d", dollars, cents)
    }

    /**
     * Format the total price as a human-readable string.
     * @return Formatted price (e.g., "$74.99")
     */
    fun formattedTotal(): String {
        val total = calculateTotal()
        val dollars = total / 100
        val cents = total % 100
        return String.format("$%d.%02d", dollars, cents)
    }
}

