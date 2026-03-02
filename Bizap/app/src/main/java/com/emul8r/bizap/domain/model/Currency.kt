package com.emul8r.bizap.domain.model

data class Currency(
    val code: String,        // ISO 4217 (e.g., "AUD", "USD")
    val symbol: String,      // Visual (e.g., "$", "€")
    val name: String,        // Full name (e.g., "Australian Dollar")
    val isSystemDefault: Boolean = false
)

