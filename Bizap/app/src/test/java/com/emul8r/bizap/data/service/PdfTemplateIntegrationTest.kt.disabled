package com.emul8r.bizap.data.service

import com.emul8r.bizap.ui.templates.TemplateSnapshot
import com.emul8r.bizap.ui.templates.TemplateSnapshotManager
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for PDF generation with templates
 */
class PdfTemplateIntegrationTest {

    private val snapshotManager = TemplateSnapshotManager()
    private val pdfStyler = PdfStyler()

    @Test
    fun testPdfGenerationWithoutTemplate() {
        // Old invoice (before Phase 5) with no template
        val templateSnapshot: String? = null
        val customFieldValues: String? = null

        val colors = pdfStyler.extractColors(snapshotManager.restoreSnapshot(templateSnapshot))

        assertNotNull(colors)
        assertTrue(colors.primary != 0)
        // Should use default styling
    }

    @Test
    fun testPdfGenerationWithTemplate() {
        val template = TemplateSnapshot(
            id = "template-pdf",
            name = "Professional PDF",
            designType = "PROFESSIONAL",
            primaryColor = "#FF5722",
            secondaryColor = "#FFF9C4",
            fontFamily = "SANS_SERIF",
            companyName = "PDF Test Co",
            companyAddress = "123 PDF St",
            companyPhone = "555-PDF1",
            companyEmail = "pdf@test.com",
            hideLineItems = false,
            hidePaymentTerms = false
        )

        val snapshot = snapshotManager.createSnapshot(template)
        val restored = snapshotManager.restoreSnapshot(snapshot)

        assertNotNull(restored)
        assertEquals("Professional PDF", restored?.name)
        assertEquals("#FF5722", restored?.primaryColor)
    }

    @Test
    fun testPdfWithCustomFields() {
        val fieldValues = mapOf(
            "field-1" to "PO-12345",
            "field-2" to "42",
            "field-3" to "2026-03-01"
        )

        val valuesJson = snapshotManager.createCustomFieldValuesMap(fieldValues)
        val restored = snapshotManager.restoreCustomFieldValues(valuesJson)

        assertEquals(3, restored.size)
        assertEquals("PO-12345", restored["field-1"])
    }

    @Test
    fun testPdfWithHiddenLineItems() {
        val template = TemplateSnapshot(
            id = "hide-items", name = "Minimal", designType = "MINIMAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SERIF",
            companyName = "Minimal Co", companyAddress = "456 Elm", companyPhone = "555-1111",
            companyEmail = "minimal@test.com", hideLineItems = true, hidePaymentTerms = false
        )

        assertTrue(pdfStyler.shouldHideLineItems(template))
    }

    @Test
    fun testPdfWithHiddenPaymentTerms() {
        val template = TemplateSnapshot(
            id = "hide-terms", name = "No Terms", designType = "PROFESSIONAL",
            primaryColor = "#FF0000", secondaryColor = "#00FF00", fontFamily = "SANS_SERIF",
            companyName = "No Terms Co", companyAddress = "789 Oak", companyPhone = "555-2222",
            companyEmail = "noterms@test.com", hideLineItems = false, hidePaymentTerms = true
        )

        assertTrue(pdfStyler.shouldHidePaymentTerms(template))
    }

    @Test
    fun testPdfWithLogo() {
        val template = TemplateSnapshot(
            id = "with-logo", name = "Logo Template", designType = "BRANDED",
            primaryColor = "#0000FF", secondaryColor = "#FFFF00", fontFamily = "SANS_SERIF",
            companyName = "Logo Co", companyAddress = "321 Logo", companyPhone = "555-3333",
            companyEmail = "logo@test.com", logoFileName = "logo-123.png",
            hideLineItems = false, hidePaymentTerms = false
        )

        assertEquals("logo-123.png", pdfStyler.getLogoFileName(template))
    }

    @Test
    fun testPdfBackwardCompatibility() {
        // Old invoice - no template fields
        val templateSnapshot: String? = null
        val customFieldValues: String? = null

        val colors = pdfStyler.extractColors(snapshotManager.restoreSnapshot(templateSnapshot))
        val hideItems = pdfStyler.shouldHideLineItems(snapshotManager.restoreSnapshot(templateSnapshot))
        val hideTerms = pdfStyler.shouldHidePaymentTerms(snapshotManager.restoreSnapshot(templateSnapshot))

        // Should work with defaults
        assertNotNull(colors)
        assertEquals(false, hideItems)
        assertEquals(false, hideTerms)
    }

    @Test
    fun testPdfFullDataFlow() {
        // Create template
        val template = TemplateSnapshot(
            id = "full-flow", name = "Full Flow", designType = "PROFESSIONAL",
            primaryColor = "#FF5722", secondaryColor = "#FFF9C4", fontFamily = "SANS_SERIF",
            companyName = "Full Flow Co", companyAddress = "555 Main",
            companyPhone = "555-0000", companyEmail = "flow@test.com",
            logoFileName = "flow-logo.png", hideLineItems = false, hidePaymentTerms = false
        )

        // Create snapshot
        val templateSnapshot = snapshotManager.createSnapshot(template)

        // Create custom fields
        val customFieldValues = mapOf(
            "packing-method" to "FedEx",
            "order-number" to "ORD-99999",
            "delivery-date" to "2026-04-01"
        )
        val customFieldsJson = snapshotManager.createCustomFieldValuesMap(customFieldValues)

        // Restore and verify
        val restoredTemplate = snapshotManager.restoreSnapshot(templateSnapshot)
        val restoredFields = snapshotManager.restoreCustomFieldValues(customFieldsJson)

        assertNotNull(restoredTemplate)
        assertEquals("Full Flow", restoredTemplate?.name)
        assertEquals(3, restoredFields.size)
        assertEquals("FedEx", restoredFields["packing-method"])
        assertEquals("flow-logo.png", restoredTemplate?.logoFileName)
    }

    @Test
    fun testMultiplePdfsWithDifferentTemplates() {
        val template1 = TemplateSnapshot(
            id = "t1", name = "Template 1", designType = "PROFESSIONAL",
            primaryColor = "#FF5722", secondaryColor = "#FFF9C4", fontFamily = "SANS_SERIF",
            companyName = "Company 1", companyAddress = "111", companyPhone = "111",
            companyEmail = "1@test.com", hideLineItems = false, hidePaymentTerms = false
        )

        val template2 = TemplateSnapshot(
            id = "t2", name = "Template 2", designType = "MINIMAL",
            primaryColor = "#000000", secondaryColor = "#FFFFFF", fontFamily = "SERIF",
            companyName = "Company 2", companyAddress = "222", companyPhone = "222",
            companyEmail = "2@test.com", hideLineItems = true, hidePaymentTerms = false
        )

        val snap1 = snapshotManager.createSnapshot(template1)
        val snap2 = snapshotManager.createSnapshot(template2)

        val restored1 = snapshotManager.restoreSnapshot(snap1)
        val restored2 = snapshotManager.restoreSnapshot(snap2)

        assertEquals("Template 1", restored1?.name)
        assertEquals("Template 2", restored2?.name)
        assertEquals(false, pdfStyler.shouldHideLineItems(restored1))
        assertEquals(true, pdfStyler.shouldHideLineItems(restored2))
    }
}

