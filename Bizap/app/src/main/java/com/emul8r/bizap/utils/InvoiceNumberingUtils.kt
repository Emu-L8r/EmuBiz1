package com.emul8r.bizap.utils

import java.util.Calendar

/**
 * Invoice numbering utility - Date-based compact format
 * Format: YY-MMDD-SEQ-CUSTOMER[-vVERSION]
 * Example: 26-0410-01-Smith (April 10, 2026, invoice #1 for Smith)
 *
 * Benefits:
 * - Compact: Only ~16 characters vs 23+ for old format
 * - Date embedded: Know when issued at a glance
 * - Customer identifier: Easy to identify who the invoice is for
 * - Version tracking: Corrections marked with -v2, -v3, etc.
 * - Searchable: Can find by date, customer, or full number
 */
object InvoiceNumberingUtils {

    /**
     * Extract customer last name or abbreviation
     * "John Smith" → "Smith"
     * "Smith & Associates" → "Smith"
     * "Acme Corp Inc." → "Corp"
     * "John" → "John"
     * "" → "Unknown"
     */
    fun generateCustomerCode(customerName: String, maxLength: Int = 20): String {
        if (customerName.isBlank()) return "Unknown"

        // Extract last word (usually last name)
        val parts = customerName.split(Regex("[\\s&,]"))
            .filter { it.isNotBlank() && !isCommonSuffix(it) }

        val code = if (parts.size > 1) {
            parts.last()  // Last name
        } else if (parts.isNotEmpty()) {
            parts.first()  // Single name
        } else {
            customerName.replace(Regex("[^A-Za-z0-9]"), "")
        }

        return code
            .take(maxLength)
            .ifBlank { "Unknown" }
    }

    private fun isCommonSuffix(word: String): Boolean {
        return word.equals("inc", ignoreCase = true) ||
               word.equals("ltd", ignoreCase = true) ||
               word.equals("llc", ignoreCase = true) ||
               word.equals("pty", ignoreCase = true) ||
               word.equals("co", ignoreCase = true) ||
               word.equals("corp", ignoreCase = true)
    }

    /**
     * Generate invoice number: YY-MMDD-SEQ-CUSTOMER[-vVERSION]
     *
     * @param date Invoice date (milliseconds)
     * @param dailySequence Sequence number for this date (1-99)
     * @param customerName Full customer name
     * @param version Invoice version (default 1, >1 for corrections)
     * @return Formatted invoice number like "26-0410-01-Smith"
     */
    fun generateInvoiceNumber(
        date: Long,
        dailySequence: Int,
        customerName: String,
        version: Int = 1
    ): String {
        val cal = Calendar.getInstance().apply { timeInMillis = date }

        // Extract date components
        val yy = String.format("%02d", cal.get(Calendar.YEAR) % 100)
        val mm = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val dd = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
        val seq = String.format("%02d", dailySequence.coerceIn(1, 99))
        val customer = generateCustomerCode(customerName)

        val base = "$yy-$mm$dd-$seq-$customer"
        return if (version > 1) "$base-v$version" else base
    }

    /**
     * Extract version from invoice number
     * "26-0410-01-Smith-v2" → 2
     * "26-0410-01-Smith" → 1
     */
    fun extractVersion(invoiceNumber: String): Int {
        val versionPart = invoiceNumber.split("-v")
        return if (versionPart.size > 1) {
            versionPart.last().toIntOrNull() ?: 1
        } else {
            1
        }
    }

    /**
     * Get base number without version
     * "26-0410-01-Smith-v2" → "26-0410-01-Smith"
     * "26-0410-01-Smith" → "26-0410-01-Smith"
     */
    fun getBaseNumber(invoiceNumber: String): String {
        return invoiceNumber.split("-v").first()
    }

    /**
     * Convert to file-safe format (replace dashes with underscores)
     * "26-0410-01-Smith" → "26_0410_01_Smith"
     */
    fun toFileSafeFormat(invoiceNumber: String): String {
        return invoiceNumber.replace("-", "_")
    }

    /**
     * Get date from invoice number
     * "26-0410-01-Smith" → Calendar with April 10, 2026
     */
    fun extractDate(invoiceNumber: String): Calendar {
        val parts = invoiceNumber.split("-")
        if (parts.size < 2) return Calendar.getInstance()

        return try {
            val yy = parts[0].toInt()
            val mmdd = parts[1]

            val mm = mmdd.take(2).toInt()
            val dd = mmdd.takeLast(2).toInt()

            // Assume 2000s for YY
            val yyyy = if (yy > 50) 1900 + yy else 2000 + yy

            Calendar.getInstance().apply {
                set(Calendar.YEAR, yyyy)
                set(Calendar.MONTH, mm - 1)
                set(Calendar.DAY_OF_MONTH, dd)
            }
        } catch (e: Exception) {
            Calendar.getInstance()
        }
    }
}

