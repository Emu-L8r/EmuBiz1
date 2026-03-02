package com.emul8r.bizap.utils

import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

/**
 * Formats monetary values stored as Long (cents) to user-readable strings.
 *
 * WHY CENTS?
 * - Avoids IEEE 754 floating-point errors (0.1 + 0.2 ≠ 0.3)
 * - Matches Stripe, Square, PayPal, every financial API
 * - Eliminates rounding bugs in calculations
 *
 * USAGE:
 *   val cents = 149_99L  // $1.49.99
 *   CentsFormatter.formatCents(cents, currencyCode = "USD")  // "$1,499.99"
 *   CentsFormatter.formatCents(cents, symbol = "$")           // "$1,499.99"
 */
object CentsFormatter {

    /**
     * Format cents to currency string using currency code.
     *
     * @param cents Amount in cents (e.g., 14999 = $149.99)
     * @param currencyCode ISO 4217 code (e.g., "USD", "AUD", "EUR")
     * @return Formatted string with currency symbol and thousand separators
     */
    fun formatCents(cents: Long, currencyCode: String = "AUD"): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val symbol = currency.symbol
            val dollars = cents / 100.0

            val formatter = DecimalFormat()
            formatter.currency = currency
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            formatter.isGroupingUsed = true

            "$symbol${formatter.format(dollars).removePrefix(symbol)}"
        } catch (e: Exception) {
            // Fallback if currency not found
            formatCentsWithSymbol(cents, "$")
        }
    }

    /**
     * Format cents with a custom symbol.
     *
     * @param cents Amount in cents (e.g., 14999 = $149.99)
     * @param symbol Currency symbol (e.g., "$", "€", "£")
     * @return Formatted string with symbol and thousand separators
     */
    fun formatCentsWithSymbol(cents: Long, symbol: String = "$"): String {
        val dollars = cents / 100.0
        val formatter = DecimalFormat("#,##0.00")
        return "$symbol${formatter.format(dollars)}"
    }

    /**
     * Convert dollars (Double) to cents (Long).
     *
     * @param dollars Amount in dollars (e.g., 149.99)
     * @return Amount in cents (e.g., 14999L)
     */
    fun dollarsToCents(dollars: Double): Long {
        return (dollars * 100).toLong()
    }

    /**
     * Convert cents (Long) to dollars (Double).
     *
     * @param cents Amount in cents (e.g., 14999L)
     * @return Amount in dollars (e.g., 149.99)
     */
    fun centsToDollars(cents: Long): Double {
        return cents / 100.0
    }

    /**
     * Parse a currency string back to cents.
     * Removes currency symbol and commas, then multiplies by 100.
     *
     * @param formatted Formatted string (e.g., "$1,499.99")
     * @return Amount in cents (e.g., 149999L)
     */
    fun parseTocents(formatted: String): Long {
        val cleaned = formatted
            .replace(Regex("[^0-9.]"), "")  // Keep only digits and decimal
            .trim()

        return if (cleaned.isEmpty()) 0L
        else (cleaned.toDoubleOrNull() ?: 0.0 * 100).toLong()
    }
}


