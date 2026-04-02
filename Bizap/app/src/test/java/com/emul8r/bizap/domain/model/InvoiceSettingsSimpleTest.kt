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
        val updated = original.copy(businessName = "New Name")
        assertThat(original.businessName).isEmpty()  // Default is empty
        assertThat(updated.businessName).isEqualTo("New Name")
    }

    @Test
    fun testValidation() {
        // Default settings have empty required fields, so they're invalid
        val defaultSettings = InvoiceSettings.default("user1")
        assertThat(defaultSettings.isValid()).isFalse()

        // Valid settings must have all required fields
        val validSettings = defaultSettings.copy(
            businessName = "Company",
            businessEmail = "test@company.com",
            businessPhone = "555-1234",
            businessAddress = "123 Main St"
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
    fun testBankDetailsStorage() {
        val settings = InvoiceSettings.default("user1").copy(
            bankName = "Test Bank",
            accountNumber = "123456",
            accountHolder = "John Doe"
        )

        assertThat(settings.bankName).isEqualTo("Test Bank")
        assertThat(settings.accountNumber).isEqualTo("123456")
        assertThat(settings.accountHolder).isEqualTo("John Doe")
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
    fun testBusinessInfo() {
        val settings = InvoiceSettings.default("user1").copy(
            businessName = "Test Company",
            businessEmail = "test@company.com",
            businessPhone = "555-1234",
            businessAddress = "123 Main St"
        )

        assertThat(settings.businessName).isEqualTo("Test Company")
        assertThat(settings.businessEmail).isEqualTo("test@company.com")
        assertThat(settings.businessPhone).isEqualTo("555-1234")
        assertThat(settings.businessAddress).isEqualTo("123 Main St")
    }

    @Test
    fun testNullOptionalFields() {
        val settings = InvoiceSettings.default("user1").copy(
            businessWebsite = null,
            secondaryColor = null,
            bankName = null
        )

        assertThat(settings.businessWebsite).isNull()
        assertThat(settings.secondaryColor).isNull()
        assertThat(settings.bankName).isNull()
    }

    @Test
    fun testInvoiceNumberPrefix() {
        val settings = InvoiceSettings.default("user1").copy(
            invoiceNumberPrefix = "INV-"
        )

        assertThat(settings.invoiceNumberPrefix).isEqualTo("INV-")
    }
}


