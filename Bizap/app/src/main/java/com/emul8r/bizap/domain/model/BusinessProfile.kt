package com.emul8r.bizap.domain.model

data class BusinessProfile(
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
    val logoBase64: String? = null, // Changed from logoUri to Base64-encoded image
    val signatureUri: String? = null
)
