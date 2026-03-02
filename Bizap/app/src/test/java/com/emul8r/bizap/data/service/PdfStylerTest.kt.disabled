package com.emul8r.bizap.data.service

import android.graphics.Color
import com.emul8r.bizap.ui.templates.TemplateSnapshot
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for PDF styling with templates
 */
class PdfStylerTest {

    private val styler = PdfStyler()

    @Test
    fun testExtractColorsFromSnapshot() {
        val snapshot = TemplateSnapshot(
            id = "template-1",
            name = "Professional",
            designType = "PROFESSIONAL",
            primaryColor = "#FF5722",
            secondaryColor = "#FFF9C4",
            fontFamily = "SANS_SERIF",
            companyName = "Test Co",
            companyAddress = "123 St",
            companyPhone = "555-1234",
            companyEmail = "test@example.com",
            hideLineItems = false,
            hidePaymentTerms = false
        )

        val colors = styler.extractColors(snapshot)

        assertEquals(Color.parseColor("#FF5722"), colors.primary)
        assertEquals(Color.parseColor("#FFF9C4"), colors.secondary)
        assertEquals(Color.BLACK, colors.text)
    }

    @Test
    fun testExtractColorsDefaultWhenNull() {
        val colors = styler.extractColors(null)

        assertTrue(colors.primary != 0)
        assertTrue(colors.secondary != 0)
        assertEquals(Color.BLACK, colors.text)
    }

    @Test
    fun testInvalidColorHandling() {
        val snapshot = TemplateSnapshot(
            id = "template-bad",
            name = "Bad Colors",
            designType = "PROFESSIONAL",
            primaryColor = "invalid-color",
            secondaryColor = "#GGGGGG",
            fontFamily = "SANS_SERIF",
            companyName = "Test",
            companyAddress = "123",
            companyPhone = "555",
            companyEmail = "test@test.com",
            hideLineItems = false,
            hidePaymentTerms = false
        )

        val colors = styler.extractColors(snapshot)
        // Should return default colors instead of crashing
        assertTrue(colors.primary != 0)
    }

    @Test
    fun testHideLineItems() {
        val snapshot = TemplateSnapshot(
            id = "t1", name = "test", designType = "PROFESSIONAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SANS_SERIF",
            companyName = "c", companyAddress = "a", companyPhone = "p", companyEmail = "e",
            hideLineItems = true, hidePaymentTerms = false
        )

        assertTrue(styler.shouldHideLineItems(snapshot))
    }

    @Test
    fun testShowLineItems() {
        val snapshot = TemplateSnapshot(
            id = "t1", name = "test", designType = "PROFESSIONAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SANS_SERIF",
            companyName = "c", companyAddress = "a", companyPhone = "p", companyEmail = "e",
            hideLineItems = false, hidePaymentTerms = false
        )

        assertFalse(styler.shouldHideLineItems(snapshot))
    }

    @Test
    fun testHidePaymentTerms() {
        val snapshot = TemplateSnapshot(
            id = "t1", name = "test", designType = "PROFESSIONAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SANS_SERIF",
            companyName = "c", companyAddress = "a", companyPhone = "p", companyEmail = "e",
            hideLineItems = false, hidePaymentTerms = true
        )

        assertTrue(styler.shouldHidePaymentTerms(snapshot))
    }

    @Test
    fun testGetCompanyInfo() {
        val snapshot = TemplateSnapshot(
            id = "t1", name = "test", designType = "PROFESSIONAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SANS_SERIF",
            companyName = "My Company", companyAddress = "456 Oak Ave", companyPhone = "555-5678",
            companyEmail = "company@example.com", taxId = "12-3456789", bankDetails = "Bank details here",
            hideLineItems = false, hidePaymentTerms = false
        )

        val info = styler.getCompanyInfo(snapshot)

        assertEquals("My Company", info.name)
        assertEquals("456 Oak Ave", info.address)
        assertEquals("555-5678", info.phone)
        assertEquals("company@example.com", info.email)
        assertEquals("12-3456789", info.taxId)
        assertEquals("Bank details here", info.bankDetails)
    }

    @Test
    fun testGetCompanyInfoDefault() {
        val info = styler.getCompanyInfo(null)

        assertEquals("", info.name)
        assertEquals("", info.address)
        assertNull(info.taxId)
    }

    @Test
    fun testGetLogoFileName() {
        val snapshot = TemplateSnapshot(
            id = "t1", name = "test", designType = "PROFESSIONAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SANS_SERIF",
            companyName = "c", companyAddress = "a", companyPhone = "p", companyEmail = "e",
            logoFileName = "logo-123.png", hideLineItems = false, hidePaymentTerms = false
        )

        assertEquals("logo-123.png", styler.getLogoFileName(snapshot))
    }

    @Test
    fun testGetLogoFileNameNull() {
        assertNull(styler.getLogoFileName(null))
    }

    @Test
    fun testPdfColorsDataClass() {
        val colors = PdfColors(
            primary = Color.BLUE,
            secondary = Color.RED,
            text = Color.BLACK,
            textLight = Color.GRAY
        )

        assertEquals(Color.BLUE, colors.primary)
        assertEquals(Color.RED, colors.secondary)
    }

    @Test
    fun testDesignTypeVariations() {
        val professional = TemplateSnapshot(
            id = "1", name = "Prof", designType = "PROFESSIONAL",
            primaryColor = "#FF5722", secondaryColor = "#FFF9C4", fontFamily = "SANS_SERIF",
            companyName = "c", companyAddress = "a", companyPhone = "p", companyEmail = "e",
            hideLineItems = false, hidePaymentTerms = false
        )

        val minimal = TemplateSnapshot(
            id = "2", name = "Min", designType = "MINIMAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SERIF",
            companyName = "c", companyAddress = "a", companyPhone = "p", companyEmail = "e",
            hideLineItems = true, hidePaymentTerms = false
        )

        assertEquals("PROFESSIONAL", professional.designType)
        assertEquals("MINIMAL", minimal.designType)
        assertTrue(styler.shouldHideLineItems(minimal))
        assertFalse(styler.shouldHideLineItems(professional))
    }

    @Test
    fun testFontFamilyVariations() {
        val sansSnapshot = TemplateSnapshot(
            id = "1", name = "S", designType = "PROFESSIONAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SANS_SERIF",
            companyName = "c", companyAddress = "a", companyPhone = "p", companyEmail = "e",
            hideLineItems = false, hidePaymentTerms = false
        )

        val serifSnapshot = TemplateSnapshot(
            id = "2", name = "S", designType = "PROFESSIONAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SERIF",
            companyName = "c", companyAddress = "a", companyPhone = "p", companyEmail = "e",
            hideLineItems = false, hidePaymentTerms = false
        )

        assertEquals("SANS_SERIF", sansSnapshot.fontFamily)
        assertEquals("SERIF", serifSnapshot.fontFamily)
    }
}

