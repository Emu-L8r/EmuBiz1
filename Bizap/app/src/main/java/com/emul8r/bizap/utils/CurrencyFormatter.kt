package com.emul8r.bizap.utils

import java.util.Locale

/**
 * Centralized currency formatting utility.
 * All monetary amounts in the app are stored as Long in cents.
 * This utility handles cents-to-display conversion and currency symbol resolution.
 */
object CurrencyFormatter {

    /**
     * Formats a monetary amount stored as cents (Long) into a display string.
     * Example: formatCents(14999, "AUD") → "A$149.99"
     * Example: formatCents(14999, "USD") → "$149.99"
     */
    fun formatCents(amountInCents: Long, currencyCode: String = "AUD"): String {
        val amount = amountInCents.toDouble() / 100.0
        val symbol = getSymbol(currencyCode)
        return "$symbol${String.format(Locale.getDefault(), "%.2f", amount)}"
    }

    /**
     * Formats a Double amount (already in dollars/major unit) with the currency symbol.
     * Use this only for values that are NOT stored as cents.
     */
    fun formatAmount(amount: Double, currencyCode: String = "AUD"): String {
        val symbol = getSymbol(currencyCode)
        return "$symbol${String.format(Locale.getDefault(), "%.2f", amount)}"
    }

    /**
     * Converts cents to dollars as a Double.
     */
    fun centsToDollars(cents: Long): Double = cents.toDouble() / 100.0

    /**
     * Returns the currency symbol for a given currency code.
     */
    fun getSymbol(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "AUD" -> "A$"
            "NZD" -> "NZ$"
            "CAD" -> "C$"
            "CHF" -> "CHF "
            "CNY" -> "¥"
            "INR" -> "₹"
            "KRW" -> "₩"
            "BRL" -> "R$"
            "ZAR" -> "R"
            "SGD" -> "S$"
            "HKD" -> "HK$"
            else -> "$"
        }
    }
}
