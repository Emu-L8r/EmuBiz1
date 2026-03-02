package com.emul8r.bizap.domain.model

data class BusinessProfile(
    val id: Long = 0, // NEW: Support for multiple business identities
    val businessName: String = "",
    val abn: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val website: String = "",
    val bsbNumber: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null,
    val bankName: String? = null,
    val logoBase64: String? = null,
    val signatureUri: String? = null,
    // Tax Registration
    val isTaxRegistered: Boolean = false,        // Is business registered for VAT/GST/Sales Tax?
    val defaultTaxRate: Float = 0.10f            // Default tax rate (10%)
)

