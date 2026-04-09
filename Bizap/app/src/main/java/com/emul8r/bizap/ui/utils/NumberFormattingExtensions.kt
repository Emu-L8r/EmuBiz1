package com.emul8r.bizap.ui.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Number formatting extensions using explicit US locale
 * Prevents calculation bugs on international devices
 *
 * Usage:
 * - amount.formatCurrency() → "$1,234.56"
 * - amount.formatDecimal() → "1,234.56"
 * - percentage.formatPercent() → "12.3%"
 */

fun Double.formatCurrency(): String {
    val df = DecimalFormat("$0.00", DecimalFormatSymbols(Locale.US))
    return df.format(this)
}

fun Double.formatDecimal(): String {
    val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))
    return df.format(this)
}

fun Long.formatCurrency(): String = (this / 100.0).formatCurrency()

fun Int.formatCurrency(): String = (this / 100.0).formatCurrency()

fun Double.formatPercent(): String {
    val df = DecimalFormat("0.0%", DecimalFormatSymbols(Locale.US))
    return df.format(this)
}

fun Double.formatCurrencyNoSymbol(): String {
    val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))
    return df.format(this)
}

fun Float.formatCurrency(): String = toDouble().formatCurrency()

fun Float.formatDecimal(): String = toDouble().formatDecimal()

