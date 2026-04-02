package com.emul8r.bizap.data.model

import com.google.common.truth.Truth.assertThat
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import org.junit.Test

/**
 * Simple tests for InvoiceSettings data model.
 * Verifies basic functionality without complexity.
 */
class InvoiceSettingsSimpleTest {

    @Test
    fun testDefaultSettingsCreation() {
        val settings = InvoiceSettings.default("test_user")
        assertThat(settings.userId).isEqualTo("test_user")
        assertThat(settings.selectedTheme).isEqualTo(InvoiceTheme.CANVAS)
        assertThat(settings.primaryColor).isEqualTo("#6B4C9A")  // Default purple
    }

    @Test
    fun testSettingsCopy() {
        val original = InvoiceSettings.default("user1")
        val updated = original.copy(primaryColor = "#FF5722")
        assertThat(original.primaryColor).isEqualTo("#6B4C9A")  // Default purple
        assertThat(updated.primaryColor).isEqualTo("#FF5722")
    }

    @Test
    fun testValidation() {
        // Default settings are valid (no required fields in InvoiceSettings)
        val defaultSettings = InvoiceSettings.default("user1")
        assertThat(defaultSettings.isValid()).isTrue()

        // Valid settings with theme selection
        val validSettings = defaultSettings.copy(
            selectedTheme = InvoiceTheme.HTML_PDF,
            primaryColor = "#FF5722"
        )
        assertThat(validSettings.isValid()).isTrue()
    }

    @Test
    fun testThemeSelection() {
        val canvas = InvoiceSettings.default("user1").copy(selectedTheme = InvoiceTheme.CANVAS)
        val html = InvoiceSettings.default("user1").copy(selectedTheme = InvoiceTheme.HTML_PDF)

        assertThat(canvas.selectedTheme).isEqualTo(InvoiceTheme.CANVAS)
        assertThat(html.selectedTheme).isEqualTo(InvoiceTheme.HTML_PDF)
    }

    @Test
    fun testColorConfiguration() {
        val settings = InvoiceSettings.default("user1").copy(
            primaryColor = "#FF0000",
            secondaryColor = "#00FF00"
        )

        assertThat(settings.primaryColor).isEqualTo("#FF0000")
        assertThat(settings.secondaryColor).isEqualTo("#00FF00")
    }

    @Test
    fun testPaymentTerms() {
        val settings = InvoiceSettings.default("user1").copy(
            paymentTermsDays = 45
        )

        assertThat(settings.paymentTermsDays).isEqualTo(45)
    }

    @Test
    fun testTaxConfiguration() {
        val settings = InvoiceSettings.default("user1").copy(
            taxRate = 0.15,
            taxName = "VAT"
        )

        assertThat(settings.taxRate).isEqualTo(0.15)
        assertThat(settings.taxName).isEqualTo("VAT")
    }

    @Test
    fun testInvoiceNumberPrefix() {
        val settings = InvoiceSettings.default("user1").copy(
            invoiceNumberPrefix = "INV-"
        )

        assertThat(settings.invoiceNumberPrefix).isEqualTo("INV-")
    }
}
