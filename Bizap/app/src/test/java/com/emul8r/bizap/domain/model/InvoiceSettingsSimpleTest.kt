package com.emul8r.bizap.data.model

import com.google.common.truth.Truth.assertThat
import com.emul8r.bizap.domain.model.CanvasInvoiceTemplate
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

    @Test
    fun testDefaultCanvasTemplateIsModern() {
        val settings = InvoiceSettings.default("user1")
        assertThat(settings.selectedCanvasTemplate).isEqualTo(CanvasInvoiceTemplate.MODERN)
    }

    @Test
    fun testCanvasTemplateSelection() {
        val settings = InvoiceSettings.default("user1")

        val modern = settings.copy(selectedCanvasTemplate = CanvasInvoiceTemplate.MODERN)
        val professional = settings.copy(selectedCanvasTemplate = CanvasInvoiceTemplate.PROFESSIONAL)
        val creative = settings.copy(selectedCanvasTemplate = CanvasInvoiceTemplate.CREATIVE)
        val minimal = settings.copy(selectedCanvasTemplate = CanvasInvoiceTemplate.MINIMAL)

        assertThat(modern.selectedCanvasTemplate).isEqualTo(CanvasInvoiceTemplate.MODERN)
        assertThat(professional.selectedCanvasTemplate).isEqualTo(CanvasInvoiceTemplate.PROFESSIONAL)
        assertThat(creative.selectedCanvasTemplate).isEqualTo(CanvasInvoiceTemplate.CREATIVE)
        assertThat(minimal.selectedCanvasTemplate).isEqualTo(CanvasInvoiceTemplate.MINIMAL)
    }

    @Test
    fun testCanvasTemplateHasValidColorHex() {
        for (template in CanvasInvoiceTemplate.values()) {
            assertThat(template.primaryHex).startsWith("#")
            assertThat(template.accentHex).startsWith("#")
            assertThat(template.primaryHex).hasLength(7)
            assertThat(template.accentHex).hasLength(7)
        }
    }

    @Test
    fun testCanvasTemplateHasDisplayName() {
        for (template in CanvasInvoiceTemplate.values()) {
            assertThat(template.displayName).isNotEmpty()
            assertThat(template.description).isNotEmpty()
            assertThat(template.colorScheme).isNotEmpty()
        }
    }

    @Test
    fun testFourCanvasTemplatesExist() {
        assertThat(CanvasInvoiceTemplate.values()).hasLength(4)
    }
}




