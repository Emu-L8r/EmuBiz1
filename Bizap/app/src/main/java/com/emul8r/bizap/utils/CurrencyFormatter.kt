package com.emul8r.bizap.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Standardized currency formatting for the entire app.
 * Handles Long (cents) to String (dollars) conversion.
 */
object CurrencyFormatter {
    /**
     * Formats an amount in cents to a human-readable currency string.
     * 
     * @param amountCents The amount in cents (e.g., 100L = $1.00)
     * @param currencyCode ISO 4217 currency code (default: "AUD")
     * @return Formatted string (e.g., "$1.00" or "€1.00")
     */
    fun formatCents(amountCents: Long, currencyCode: String = "AUD"): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
            format.currency = currency
            format.format(amountCents / 100.0)
        } catch (e: Exception) {
            // Fallback for invalid currency codes
            val dollars = amountCents / 100.0
            String.format(Locale.getDefault(), "$%.2f", dollars)
        }
    }
}
