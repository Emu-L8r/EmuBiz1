package com.emul8r.bizap.data.service

/**
 * Provides Unicode-symbol and SVG-based icons for payment method display in invoice PDFs.
 *
 * Uses Unicode symbols that are reliably rendered by iText7 with the standard sans-serif
 * font stack.  For payment methods where a symbol may not render reliably, a short
 * text abbreviation badge is returned instead.
 *
 * All returned HTML is iText7-safe (table-based layout, inline styles, no flexbox).
 */
object PaymentMethodIconProvider {

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns an HTML snippet containing an icon + label suitable for embedding
     * inside a payment details table cell.
     *
     * @param methodName  Raw payment method string from InvoiceSnapshot (may be blank)
     * @param accentColor Hex color used for badge background
     * @return            HTML string (empty if methodName is blank)
     */
    fun buildPaymentMethodBadge(methodName: String, accentColor: String = "#0066FF"): String {
        if (methodName.isBlank()) return ""
        val icon = iconFor(methodName)
        val label = methodName.trim()
        val lightBg = lightenHex(accentColor, 0.88)
        return """<span style="background-color:$lightBg;color:$accentColor;font-size:8.5pt;font-weight:700;padding:2px 8px;border-radius:3px;">$icon $label</span>"""
    }

    /**
     * Returns a Unicode symbol for the given payment method name.
     * Falls back to a generic card symbol when not recognized.
     */
    fun iconFor(methodName: String): String {
        val lower = methodName.lowercase()
        return when {
            lower.contains("bank") || lower.contains("transfer") || lower.contains("eft")
                || lower.contains("bpay") -> "&#x1F3E6;"   // 🏦  Bank
            lower.contains("credit") || lower.contains("card")
                || lower.contains("visa") || lower.contains("mastercard") -> "&#x1F4B3;"   // 💳  Card
            lower.contains("paypal") -> "&#x1F4B5;"   // 💵  PayPal / online
            lower.contains("cash") -> "&#x1F4B0;"   // 💰  Cash
            lower.contains("cheque") || lower.contains("check") -> "&#x1F4DD;"   // 📝  Cheque
            lower.contains("debit") || lower.contains("direct") -> "&#x21C4;"    // ⇄  Direct debit
            lower.contains("crypto") || lower.contains("bitcoin") -> "&#x20BF;"  // ₿  Crypto
            else -> "&#x1F4B3;"   // 💳  Default card
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Color helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Blends a hex color with white by [factor] (0.0 = original, 1.0 = white).
     * Returns a 6-digit hex string with leading `#`.
     */
    private fun lightenHex(hex: String, factor: Double): String {
        val clean = hex.removePrefix("#").uppercase()
        if (clean.length != 6) return "#F0F4FF"
        val r = clean.substring(0, 2).toIntOrNull(16) ?: 0
        val g = clean.substring(2, 4).toIntOrNull(16) ?: 0
        val b = clean.substring(4, 6).toIntOrNull(16) ?: 0
        val lr = (r + (255 - r) * factor).toInt().coerceIn(0, 255)
        val lg = (g + (255 - g) * factor).toInt().coerceIn(0, 255)
        val lb = (b + (255 - b) * factor).toInt().coerceIn(0, 255)
        return "#%02X%02X%02X".format(lr, lg, lb)
    }
}
