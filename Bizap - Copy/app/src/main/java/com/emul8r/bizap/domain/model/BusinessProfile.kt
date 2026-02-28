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
    val logoUri: String? = null,
    val signatureUri: String? = null
)
