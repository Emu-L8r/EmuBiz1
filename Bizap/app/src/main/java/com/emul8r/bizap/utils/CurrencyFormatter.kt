package com.emul8r.bizap.utils

object CurrencyFormatter {
    fun getSymbol(code: String): String = when (code) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "AUD" -> "$"
        else -> "$"
    }
}
