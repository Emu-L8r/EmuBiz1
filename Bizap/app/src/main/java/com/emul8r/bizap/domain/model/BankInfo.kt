package com.emul8r.bizap.domain.model

/**
 * Bank account information for invoice display
 *
 * Represents payment details shown on invoices.
 * Can be set at user, business, or invoice level.
 */
data class BankInfo(
    val bank: String? = null,           // Bank name (e.g., "Commonwealth Bank")
    val accountName: String? = null,    // Account holder name
    val accountNumber: String? = null,  // Account number (masked for display)
    val bsb: String? = null,            // BSB code (Australian banking)
    val swiftCode: String? = null,      // SWIFT code (international)
    val iban: String? = null            // IBAN (European banking)
) {
    /**
     * Check if bank details are sufficiently populated
     */
    fun isValid(): Boolean {
        return !bank.isNullOrBlank() || !accountNumber.isNullOrBlank() || !bsb.isNullOrBlank()
    }

    /**
     * Format for display on invoice
     */
    fun formatForInvoice(): String {
        return listOfNotNull(
            if (!bank.isNullOrBlank()) "Bank: $bank" else null,
            if (!accountName.isNullOrBlank()) "Account: $accountName" else null,
            if (!accountNumber.isNullOrBlank()) "Number: $accountNumber" else null,
            if (!bsb.isNullOrBlank()) "BSB: $bsb" else null
        ).joinToString("\n")
    }
}

