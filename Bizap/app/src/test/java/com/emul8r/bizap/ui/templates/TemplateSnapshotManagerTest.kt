package com.emul8r.bizap.ui.templates

import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for TemplateSnapshotManager
 */
class TemplateSnapshotManagerTest {

    private val manager = TemplateSnapshotManager()

    @Test
    fun testCreateSnapshot() {
        val template = InvoiceTemplate(
            id = "template-1",
            businessProfileId = 1L,
            name = "Professional",
            designType = "PROFESSIONAL",
            primaryColor = "#FF5722",
            secondaryColor = "#FFF9C4",
            fontFamily = "SANS_SERIF",
            companyName = "Test Company",
            companyAddress = "123 Main St",
            companyPhone = "555-1234",
            companyEmail = "test@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val snapshot = manager.createSnapshot(template)

        assertNotNull(snapshot)
        assertTrue(snapshot.contains("Professional"))
        assertTrue(snapshot.contains("#FF5722"))
    }

    @Test
    fun testCreateAndRestoreSnapshot() {
        val template = InvoiceTemplate(
            id = "template-2",
            businessProfileId = 1L,
            name = "Minimal Design",
            designType = "MINIMAL",
            primaryColor = "#000000",
            secondaryColor = "#FFFFFF",
            fontFamily = "SERIF",
            companyName = "My Business",
            companyAddress = "456 Oak Ave",
            companyPhone = "555-5678",
            companyEmail = "business@example.com",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Create snapshot
        val jsonSnapshot = manager.createSnapshot(template)

        // Restore snapshot
        val restored = manager.restoreSnapshot(jsonSnapshot)

        assertNotNull(restored)
        assertEquals("Minimal Design", restored?.name)
        assertEquals("MINIMAL", restored?.designType)
        assertEquals("#000000", restored?.primaryColor)
        assertEquals("SERIF", restored?.fontFamily)
    }

    @Test
    fun testRestoreNullSnapshot() {
        val restored = manager.restoreSnapshot(null)
        assertNull(restored)
    }

    @Test
    fun testRestoreEmptySnapshot() {
        val restored = manager.restoreSnapshot("")
        assertNull(restored)
    }

    @Test
    fun testCreateCustomFieldValuesMap() {
        val values = mapOf(
            "field-1" to "PO-12345",
            "field-2" to "42",
            "field-3" to "2026-03-01"
        )

        val jsonMap = manager.createCustomFieldValuesMap(values)

        assertNotNull(jsonMap)
        assertTrue(jsonMap.contains("field-1"))
        assertTrue(jsonMap.contains("PO-12345"))
    }

    @Test
    fun testCreateAndRestoreCustomFieldValues() {
        val original = mapOf(
            "field-1" to "Value 1",
            "field-2" to "Value 2"
        )

        // Create map
        val jsonMap = manager.createCustomFieldValuesMap(original)

        // Restore map
        val restored = manager.restoreCustomFieldValues(jsonMap)

        assertEquals(2, restored.size)
        assertEquals("Value 1", restored["field-1"])
        assertEquals("Value 2", restored["field-2"])
    }

    @Test
    fun testRestoreNullCustomFieldValues() {
        val restored = manager.restoreCustomFieldValues(null)
        assertEquals(0, restored.size)
    }

    @Test
    fun testRestoreEmptyCustomFieldValues() {
        val restored = manager.restoreCustomFieldValues("")
        assertEquals(0, restored.size)
    }

    @Test
    fun testIsValidSnapshot() {
        val template = InvoiceTemplate(
            id = "template-3",
            businessProfileId = 1L,
            name = "Valid Template",
            designType = "BRANDED",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val snapshot = manager.createSnapshot(template)
        assertTrue(manager.isValidSnapshot(snapshot))
    }

    @Test
    fun testIsInvalidSnapshot() {
        assertFalse(manager.isValidSnapshot(null))
        assertFalse(manager.isValidSnapshot(""))
        assertFalse(manager.isValidSnapshot("invalid json"))
    }

    @Test
    fun testSnapshotPreservesAllFields() {
        val template = InvoiceTemplate(
            id = "template-4",
            businessProfileId = 2L,
            name = "Complete Template",
            designType = "PROFESSIONAL",
            logoFileName = "logo.png",
            primaryColor = "#FF0000",
            secondaryColor = "#00FF00",
            fontFamily = "SANS_SERIF",
            companyName = "Complete Co",
            companyAddress = "789 Elm St",
            companyPhone = "555-9999",
            companyEmail = "complete@example.com",
            taxId = "12-3456789",
            bankDetails = "Bank: ABC, Account: 123",
            hideLineItems = true,
            hidePaymentTerms = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val snapshot = manager.createSnapshot(template)
        val restored = manager.restoreSnapshot(snapshot)

        assertNotNull(restored)
        assertEquals("Complete Template", restored?.name)
        assertEquals("PROFESSIONAL", restored?.designType)
        assertEquals("logo.png", restored?.logoFileName)
        assertEquals("#FF0000", restored?.primaryColor)
        assertEquals("#00FF00", restored?.secondaryColor)
        assertEquals("SANS_SERIF", restored?.fontFamily)
        assertEquals("Complete Co", restored?.companyName)
        assertEquals("789 Elm St", restored?.companyAddress)
        assertEquals("12-3456789", restored?.taxId)
        assertEquals(true, restored?.hideLineItems)
        assertEquals(true, restored?.hidePaymentTerms)
    }
}
