package com.emul8r.bizap.data.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [PaymentMethodIconProvider].
 *
 * Validates icon selection logic and badge HTML generation.
 * All tests are pure JVM — no Android dependencies required.
 */
class PaymentMethodIconProviderTest {

    // ─────────────────────────────────────────────────────────────────────────
    // iconFor()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `iconFor returns bank symbol for bank transfer`() {
        val icon = PaymentMethodIconProvider.iconFor("Bank Transfer")
        assertTrue("Bank Transfer should map to 🏦 entity", icon.contains("1F3E6"))
    }

    @Test
    fun `iconFor returns card symbol for credit card`() {
        val icon = PaymentMethodIconProvider.iconFor("Credit Card")
        assertTrue("Credit Card should map to 💳 entity", icon.contains("1F4B3"))
    }

    @Test
    fun `iconFor returns cash symbol for cash payment`() {
        val icon = PaymentMethodIconProvider.iconFor("Cash")
        assertTrue("Cash should map to 💰 entity", icon.contains("1F4B0"))
    }

    @Test
    fun `iconFor returns card symbol as fallback for unknown method`() {
        val icon = PaymentMethodIconProvider.iconFor("Unknown Payment")
        assertTrue("Unknown method should fall back to card entity", icon.contains("1F4B3"))
    }

    @Test
    fun `iconFor is case-insensitive`() {
        val lower = PaymentMethodIconProvider.iconFor("bank transfer")
        val upper = PaymentMethodIconProvider.iconFor("BANK TRANSFER")
        assertTrue("Lower case bank should return bank icon", lower.contains("1F3E6"))
        assertTrue("Upper case bank should return bank icon", upper.contains("1F3E6"))
    }

    @Test
    fun `iconFor handles EFT variation`() {
        val icon = PaymentMethodIconProvider.iconFor("EFT")
        assertTrue("EFT should map to bank icon", icon.contains("1F3E6"))
    }

    @Test
    fun `iconFor handles direct debit`() {
        val icon = PaymentMethodIconProvider.iconFor("Direct Debit")
        assertTrue("Direct Debit should map to arrow entity", icon.contains("21C4"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildPaymentMethodBadge()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `buildPaymentMethodBadge returns empty string for blank method`() {
        val badge = PaymentMethodIconProvider.buildPaymentMethodBadge("")
        assertTrue("Blank method name should return empty HTML", badge.isEmpty())
    }

    @Test
    fun `buildPaymentMethodBadge returns non-empty HTML for valid method`() {
        val badge = PaymentMethodIconProvider.buildPaymentMethodBadge("Bank Transfer")
        assertTrue("Valid method should produce non-empty badge HTML", badge.isNotBlank())
    }

    @Test
    fun `buildPaymentMethodBadge HTML contains method name`() {
        val badge = PaymentMethodIconProvider.buildPaymentMethodBadge("PayPal")
        assertTrue("Badge HTML should contain the method name", badge.contains("PayPal"))
    }

    @Test
    fun `buildPaymentMethodBadge does not contain CSS variables`() {
        val badge = PaymentMethodIconProvider.buildPaymentMethodBadge("Bank Transfer", "#0066FF")
        assertFalse("Badge HTML must not use CSS variables (iText7 unsafe)", badge.contains("var(--"))
    }

    @Test
    fun `buildPaymentMethodBadge does not use flexbox`() {
        val badge = PaymentMethodIconProvider.buildPaymentMethodBadge("Bank Transfer", "#0066FF")
        assertFalse("Badge HTML must not use flexbox (iText7 unsafe)",
            badge.contains("display: flex") || badge.contains("display:flex"))
    }

    @Test
    fun `buildPaymentMethodBadge uses provided accent color`() {
        val badge = PaymentMethodIconProvider.buildPaymentMethodBadge("Cash", "#FF5500")
        assertTrue("Badge should contain the provided accent color", badge.contains("#FF5500"))
    }
}



