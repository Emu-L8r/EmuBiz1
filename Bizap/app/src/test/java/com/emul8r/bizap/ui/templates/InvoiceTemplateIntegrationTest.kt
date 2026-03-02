package com.emul8r.bizap.ui.templates

import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Integration tests for invoice + template integration
 */
class InvoiceTemplateIntegrationTest {

    private val snapshotManager = TemplateSnapshotManager()

    @Test
    fun testCreateInvoiceWithTemplate() {
        // Create a template
        val template = InvoiceTemplate(
            id = "template-1",
            businessProfileId = 1L,
            name = "Professional",
            designType = "PROFESSIONAL",
            primaryColor = "#FF5722",
            companyName = "Test Co",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Create snapshot
        val snapshot = snapshotManager.createSnapshot(template)

        // Create invoice with template
        val invoice = InvoiceEntity(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Customer",
            date = System.currentTimeMillis(),
            totalAmount = 100000L,  // $1000 in cents
            isQuote = false,
            status = "DRAFT",
            templateId = template.id,
            templateSnapshot = snapshot,
            customFieldValues = "{}"
        )

        assertNotNull(invoice.templateId)
        assertNotNull(invoice.templateSnapshot)
        assertEquals("template-1", invoice.templateId)
    }

    @Test
    fun testInvoiceWithoutTemplate() {
        val invoice = InvoiceEntity(
            id = 2L,
            businessProfileId = 1L,
            customerId = 2L,
            customerName = "Another Customer",
            date = System.currentTimeMillis(),
            totalAmount = 50000L,  // $500 in cents
            isQuote = false,
            status = "DRAFT",
            templateId = null,
            templateSnapshot = null,
            customFieldValues = null
        )

        assertNull(invoice.templateId)
        assertNull(invoice.templateSnapshot)
    }

    @Test
    fun testInvoiceWithCustomFieldValues() {
        val customFieldValues = mapOf(
            "field-1" to "PO-12345",
            "field-2" to "42"
        )

        val valuesJson = snapshotManager.createCustomFieldValuesMap(customFieldValues)

        val invoice = InvoiceEntity(
            id = 3L,
            businessProfileId = 1L,
            customerId = 3L,
            customerName = "Customer",
            date = System.currentTimeMillis(),
            totalAmount = 75000L,  // $750 in cents
            isQuote = false,
            status = "DRAFT",
            templateId = "template-2",
            templateSnapshot = "{}",
            customFieldValues = valuesJson
        )

        assertNotNull(invoice.customFieldValues)
        val restored = snapshotManager.restoreCustomFieldValues(invoice.customFieldValues)
        assertEquals(2, restored.size)
    }

    @Test
    fun testTemplateSnapshotPreservation() {
        val template = InvoiceTemplate(
            id = "template-3",
            businessProfileId = 1L,
            name = "Original Name",
            designType = "MINIMAL",
            primaryColor = "#000000",
            companyName = "Original Company",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Create snapshot at invoice creation time
        val snapshot = snapshotManager.createSnapshot(template)

        // Later, template is modified
        val modifiedTemplate = template.copy(
            name = "Modified Name",
            primaryColor = "#FFFFFF"
        )

        // Invoice snapshot should preserve original
        val restoredSnapshot = snapshotManager.restoreSnapshot(snapshot)
        val restoredModified = snapshotManager.restoreSnapshot(snapshotManager.createSnapshot(modifiedTemplate))

        assertNotNull(restoredSnapshot)
        assertNotNull(restoredModified)
        assertEquals("Original Name", restoredSnapshot?.name)
        assertEquals("Modified Name", restoredModified?.name)
        assertEquals("#000000", restoredSnapshot?.primaryColor)
        assertEquals("#FFFFFF", restoredModified?.primaryColor)
    }

    @Test
    fun testInvoiceTemplateDataFlow() {
        // Setup: Create template with custom fields
        val template = InvoiceTemplate(
            id = "template-flow",
            businessProfileId = 1L,
            name = "Flow Test",
            designType = "BRANDED",
            companyName = "Flow Co",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val customFields = listOf(
            InvoiceCustomField(id = "f1", templateId = "template-flow", label = "PO", fieldType = "TEXT", displayOrder = 1),
            InvoiceCustomField(id = "f2", templateId = "template-flow", label = "Delivery Date", fieldType = "DATE", displayOrder = 2)
        )

        // User fills in custom fields
        val fieldValues = mapOf(
            "f1" to "PO-99999",
            "f2" to "2026-04-01"
        )

        // Create invoice with all data
        val snapshot = snapshotManager.createSnapshot(template)
        val valuesJson = snapshotManager.createCustomFieldValuesMap(fieldValues)

        val invoice = InvoiceEntity(
            id = 10L,
            businessProfileId = 1L,
            customerId = 10L,
            customerName = "Flow Customer",
            date = System.currentTimeMillis(),
            totalAmount = 200000L,  // $2000 in cents
            isQuote = false,
            status = "DRAFT",
            templateId = template.id,
            templateSnapshot = snapshot,
            customFieldValues = valuesJson
        )

        // Verify all data is preserved
        assertEquals("template-flow", invoice.templateId)
        assertNotNull(invoice.templateSnapshot)
        assertNotNull(invoice.customFieldValues)

        val restoredTemplate = snapshotManager.restoreSnapshot(invoice.templateSnapshot)
        val restoredValues = snapshotManager.restoreCustomFieldValues(invoice.customFieldValues)

        assertEquals("Flow Test", restoredTemplate?.name)
        assertEquals(2, restoredValues.size)
        assertEquals("PO-99999", restoredValues["f1"])
        assertEquals("2026-04-01", restoredValues["f2"])
    }

    @Test
    fun testMultipleInvoicesWithDifferentTemplates() {
        val template1 = InvoiceTemplate(
            id = "t1", businessProfileId = 1L, name = "Template 1", designType = "PROFESSIONAL",
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
        )
        val template2 = InvoiceTemplate(
            id = "t2", businessProfileId = 1L, name = "Template 2", designType = "MINIMAL",
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
        )

        val snapshot1 = snapshotManager.createSnapshot(template1)
        val snapshot2 = snapshotManager.createSnapshot(template2)

        val invoice1 = InvoiceEntity(
            id = 20L, businessProfileId = 1L, customerId = 20L, customerName = "Customer 1",
            date = System.currentTimeMillis(), totalAmount = 100000L, isQuote = false, status = "DRAFT",
            templateId = "t1", templateSnapshot = snapshot1, customFieldValues = null
        )

        val invoice2 = InvoiceEntity(
            id = 21L, businessProfileId = 1L, customerId = 21L, customerName = "Customer 2",
            date = System.currentTimeMillis(), totalAmount = 200000L, isQuote = false, status = "DRAFT",
            templateId = "t2", templateSnapshot = snapshot2, customFieldValues = null
        )

        assertEquals("t1", invoice1.templateId)
        assertEquals("t2", invoice2.templateId)

        val restored1 = snapshotManager.restoreSnapshot(invoice1.templateSnapshot)
        val restored2 = snapshotManager.restoreSnapshot(invoice2.templateSnapshot)

        assertEquals("Template 1", restored1?.name)
        assertEquals("Template 2", restored2?.name)
    }

    @Test
    fun testInvoiceBackwardCompatibility() {
        // Old invoices (before Phase 5) have NULL template fields
        val oldInvoice = InvoiceEntity(
            id = 30L,
            businessProfileId = 1L,
            customerId = 30L,
            customerName = "Old Invoice",
            date = System.currentTimeMillis(),
            totalAmount = 50000L,  // $500 in cents
            isQuote = false,
            status = "PAID"
            // templateId, templateSnapshot, customFieldValues are NULL by default
        )

        assertNull(oldInvoice.templateId)
        assertNull(oldInvoice.templateSnapshot)
        assertNull(oldInvoice.customFieldValues)
    }

    @Test
    fun testEmptyCustomFieldValues() {
        val emptyValues = snapshotManager.createCustomFieldValuesMap(emptyMap())

        val invoice = InvoiceEntity(
            id = 40L,
            businessProfileId = 1L,
            customerId = 40L,
            customerName = "Empty Fields",
            date = System.currentTimeMillis(),
            totalAmount = 30000L,  // $300 in cents
            isQuote = false,
            status = "DRAFT",
            templateId = "template-x",
            templateSnapshot = "{}",
            customFieldValues = emptyValues
        )

        val restored = snapshotManager.restoreCustomFieldValues(invoice.customFieldValues)
        assertEquals(0, restored.size)
    }
}

