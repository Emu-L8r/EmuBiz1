package com.emul8r.bizap.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InvoiceCustomization(
    val headerText: String = "",
    val footerText: String = "",
    val companyName: String = "",
    val templateType: String = "standard"
)

