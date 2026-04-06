package com.emul8r.bizap.domain.model

/**
 * Value object representing monetary amounts.
 *
 * **Why this exists:**
 * - Always stores in cents to avoid floating-point rounding errors
 * - Encapsulates formatting logic (no scattered "$" calculations)
 * - Provides arithmetic operations safely
 * - Type-safe: can't accidentally pass Long where Money is needed
 *
 * **Usage:**
 * ```
 * val price = Money(amountCents = 1999)  // $19.99
 * println(price.toDollars())  // Outputs: $19.99
 * val total = price + Money(500)  // $24.99
 * ```
 */
data class Money(
    val amountCents: Long,
    val currency: String = "USD"
) {
    init {
        require(amountCents >= 0) { "Money amount cannot be negative" }
    }

    /**
     * Format as currency string (e.g., "$19.99")
     */
    fun toDollars(): String {
        val dollars = amountCents / 100
        val cents = amountCents % 100
        return "$$dollars.${cents.toString().padStart(2, '0')}"
    }

    /**
     * Get whole dollar amount (e.g., 19 for $19.99)
     */
    fun getDollars(): Long = amountCents / 100

    /**
     * Get cents portion (e.g., 99 for $19.99)
     */
    fun getCents(): Long = amountCents % 100

    /**
     * Safe addition of two money amounts
     */
    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot add $currency and ${other.currency}"
        }
        return copy(amountCents = amountCents + other.amountCents)
    }

    /**
     * Safe subtraction of two money amounts
     */
    operator fun minus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot subtract $currency and ${other.currency}"
        }
        val result = amountCents - other.amountCents
        require(result >= 0) { "Subtraction resulted in negative amount" }
        return copy(amountCents = result)
    }

    /**
     * Check if amount is positive
     */
    fun isPositive(): Boolean = amountCents > 0

    /**
     * Check if amount is zero
     */
    fun isZero(): Boolean = amountCents == 0L

    companion object {
        /**
         * Create Money from dollar amount
         * ```
         * Money.fromDollars(19.99)  // Creates Money(1999)
         * ```
         */
        fun fromDollars(dollars: Double): Money {
            val cents = (dollars * 100).toLong()
            return Money(amountCents = cents)
        }

        /**
         * Zero amount in given currency
         */
        fun zero(currency: String = "USD"): Money = Money(0, currency)
    }
}

