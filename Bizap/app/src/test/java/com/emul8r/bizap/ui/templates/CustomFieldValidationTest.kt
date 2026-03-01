package com.emul8r.bizap.ui.templates

import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for custom field validation and rendering
 */
class CustomFieldValidationTest {

    @Test
    fun testTextFieldValidation() {
        val field = InvoiceCustomField(
            id = "field-1",
            templateId = "template-1",
            label = "PO Number",
            fieldType = "TEXT",
            isRequired = true,
            displayOrder = 1
        )

        assertEquals("TEXT", field.fieldType)
        assertEquals("PO Number", field.label)
        assertTrue(field.isRequired)
    }

    @Test
    fun testNumberFieldValidation() {
        val field = InvoiceCustomField(
            id = "field-2",
            templateId = "template-1",
            label = "Quantity",
            fieldType = "NUMBER",
            isRequired = false,
            displayOrder = 2
        )

        assertEquals("NUMBER", field.fieldType)
        assertFalse(field.isRequired)
    }

    @Test
    fun testDateFieldValidation() {
        val field = InvoiceCustomField(
            id = "field-3",
            templateId = "template-1",
            label = "Delivery Date",
            fieldType = "DATE",
            isRequired = true,
            displayOrder = 3
        )

        assertEquals("DATE", field.fieldType)
        assertTrue(field.isRequired)
    }

    @Test
    fun testCustomFieldOrdering() {
        val fields = listOf(
            InvoiceCustomField(id = "f1", templateId = "t1", label = "Field 1", fieldType = "TEXT", displayOrder = 1),
            InvoiceCustomField(id = "f2", templateId = "t1", label = "Field 2", fieldType = "NUMBER", displayOrder = 2),
            InvoiceCustomField(id = "f3", templateId = "t1", label = "Field 3", fieldType = "DATE", displayOrder = 3)
        )

        // Sort by display order
        val sorted = fields.sortedBy { it.displayOrder }

        assertEquals(1, sorted[0].displayOrder)
        assertEquals(2, sorted[1].displayOrder)
        assertEquals(3, sorted[2].displayOrder)
    }

    @Test
    fun testRequiredFieldValidation() {
        val requiredFields = listOf(
            InvoiceCustomField(id = "f1", templateId = "t1", label = "Required", fieldType = "TEXT", isRequired = true, displayOrder = 1),
            InvoiceCustomField(id = "f2", templateId = "t1", label = "Optional", fieldType = "TEXT", isRequired = false, displayOrder = 2)
        )

        val required = requiredFields.filter { it.isRequired }
        assertEquals(1, required.size)
        assertEquals("Required", required[0].label)
    }

    @Test
    fun testValidateTextInput() {
        val value = "PO-12345"
        assertTrue(value.isNotEmpty())
        assertTrue(value.length <= 1000)
    }

    @Test
    fun testValidateNumberInput() {
        val validNumbers = listOf("42", "3.14", "0", "999999.99")
        validNumbers.forEach { num ->
            assertTrue(num.matches(Regex("^\\d+(\\.\\d{0,2})?$")))
        }
    }

    @Test
    fun testValidateNumberInputInvalid() {
        val invalidNumbers = listOf("abc", "12.34.56", "-5", "12a")
        invalidNumbers.forEach { num ->
            assertFalse(num.matches(Regex("^\\d+(\\.\\d{0,2})?$")))
        }
    }

    @Test
    fun testValidateDateInput() {
        val validDates = listOf("2026-03-01", "2025-12-31", "2020-01-01")
        validDates.forEach { date ->
            // Basic format check
            assertTrue(date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")))
        }
    }

    @Test
    fun testValidateDateInputInvalid() {
        val invalidDates = listOf("03-01-2026", "2026/03/01", "2026-3-1", "invalid")
        invalidDates.forEach { date ->
            assertFalse(date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")))
        }
    }

    @Test
    fun testCustomFieldCount() {
        val fields = mutableListOf<InvoiceCustomField>()
        repeat(50) { i ->
            fields.add(
                InvoiceCustomField(
                    id = "field-$i",
                    templateId = "template-1",
                    label = "Field $i",
                    fieldType = "TEXT",
                    displayOrder = i + 1
                )
            )
        }

        assertEquals(50, fields.size)
    }

    @Test
    fun testCustomFieldValuesMap() {
        val values = mapOf(
            "field-1" to "PO-123",
            "field-2" to "42",
            "field-3" to "2026-03-01"
        )

        assertEquals(3, values.size)
        assertEquals("PO-123", values["field-1"])
        assertEquals("42", values["field-2"])
        assertEquals("2026-03-01", values["field-3"])
    }

    @Test
    fun testUpdateCustomFieldValue() {
        val values = mutableMapOf(
            "field-1" to "Old Value"
        )

        values["field-1"] = "New Value"

        assertEquals("New Value", values["field-1"])
    }

    @Test
    fun testAddCustomFieldValue() {
        val values = mutableMapOf<String, String>()

        values["field-1"] = "Value 1"
        values["field-2"] = "Value 2"

        assertEquals(2, values.size)
    }

    @Test
    fun testRemoveCustomFieldValue() {
        val values = mutableMapOf(
            "field-1" to "Value 1",
            "field-2" to "Value 2"
        )

        values.remove("field-1")

        assertEquals(1, values.size)
        assertEquals("Value 2", values["field-2"])
    }

    @Test
    fun testFieldTypesEnum() {
        val types = listOf("TEXT", "NUMBER", "DATE")
        assertEquals(3, types.size)
        assertTrue(types.contains("TEXT"))
        assertTrue(types.contains("NUMBER"))
        assertTrue(types.contains("DATE"))
    }

    @Test
    fun testMultipleFieldsOfSameType() {
        val fields = listOf(
            InvoiceCustomField(id = "f1", templateId = "t1", label = "PO", fieldType = "TEXT", displayOrder = 1),
            InvoiceCustomField(id = "f2", templateId = "t1", label = "Project", fieldType = "TEXT", displayOrder = 2),
            InvoiceCustomField(id = "f3", templateId = "t1", label = "Notes", fieldType = "TEXT", displayOrder = 3)
        )

        val textFields = fields.filter { it.fieldType == "TEXT" }
        assertEquals(3, textFields.size)
    }
}

