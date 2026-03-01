package com.emul8r.bizap.ui.templates

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for TemplateFormState validation
 */
class TemplateFormStateTest {

    @Test
    fun testValidFormState() {
        val state = TemplateFormState(
            name = "Professional Template",
            designType = "PROFESSIONAL",
            primaryColor = "#FF5722",
            secondaryColor = "#FFF9C4",
            fontFamily = "SANS_SERIF",
            companyName = "Test Company"
        )

        assertTrue(state.isFormValid())
        assertEquals(0, state.validate().size)
    }

    @Test
    fun testEmptyNameValidation() {
        val state = TemplateFormState(name = "")
        val errors = state.validate()

        assertTrue(errors.containsKey("name"))
        assertEquals("Template name is required", errors["name"])
    }

    @Test
    fun testNameTooLongValidation() {
        val longName = "A".repeat(101)
        val state = TemplateFormState(name = longName)
        val errors = state.validate()

        assertTrue(errors.containsKey("name"))
    }

    @Test
    fun testInvalidPrimaryColorValidation() {
        val state = TemplateFormState(
            name = "Test",
            primaryColor = "invalid-color",
            companyName = "Test"
        )
        val errors = state.validate()

        assertTrue(errors.containsKey("primaryColor"))
    }

    @Test
    fun testInvalidSecondaryColorValidation() {
        val state = TemplateFormState(
            name = "Test",
            secondaryColor = "#GGGGGG",
            companyName = "Test"
        )
        val errors = state.validate()

        assertTrue(errors.containsKey("secondaryColor"))
    }

    @Test
    fun testInvalidDesignTypeValidation() {
        val state = TemplateFormState(
            name = "Test",
            designType = "INVALID",
            companyName = "Test"
        )
        val errors = state.validate()

        assertTrue(errors.containsKey("designType"))
    }

    @Test
    fun testInvalidFontFamilyValidation() {
        val state = TemplateFormState(
            name = "Test",
            fontFamily = "MONOSPACE",
            companyName = "Test"
        )
        val errors = state.validate()

        assertTrue(errors.containsKey("fontFamily"))
    }

    @Test
    fun testEmptyCompanyNameValidation() {
        val state = TemplateFormState(
            name = "Test",
            companyName = ""
        )
        val errors = state.validate()

        assertTrue(errors.containsKey("companyName"))
    }

    @Test
    fun testInvalidEmailValidation() {
        val state = TemplateFormState(
            name = "Test",
            companyEmail = "invalid-email",
            companyName = "Test"
        )
        val errors = state.validate()

        assertTrue(errors.containsKey("companyEmail"))
    }

    @Test
    fun testValidEmailValidation() {
        val state = TemplateFormState(
            name = "Test",
            companyEmail = "test@example.com",
            companyName = "Test"
        )
        val errors = state.validate()

        assertFalse(errors.containsKey("companyEmail"))
    }

    @Test
    fun testCustomFieldsCountValidation() {
        val fields = mutableListOf<com.emul8r.bizap.data.local.entities.InvoiceCustomField>()
        repeat(51) { i ->
            fields.add(
                com.emul8r.bizap.data.local.entities.InvoiceCustomField(
                    id = "field-$i",
                    templateId = "template",
                    label = "Field $i",
                    fieldType = "TEXT",
                    displayOrder = i + 1
                )
            )
        }

        val state = TemplateFormState(
            name = "Test",
            customFields = fields,
            companyName = "Test"
        )
        val errors = state.validate()

        assertTrue(errors.containsKey("customFields"))
    }

    @Test
    fun testUpdateFieldName() {
        val state = TemplateFormState(name = "Old Name")
        val updated = state.updateField("name", "New Name")

        assertEquals("New Name", updated.name)
        assertEquals("Old Name", state.name) // Original unchanged
    }

    @Test
    fun testUpdateFieldDesignType() {
        val state = TemplateFormState(designType = "PROFESSIONAL")
        val updated = state.updateField("designType", "MINIMAL")

        assertEquals("MINIMAL", updated.designType)
    }

    @Test
    fun testUpdateFieldPrimaryColor() {
        val state = TemplateFormState(primaryColor = "#FF5722")
        val updated = state.updateField("primaryColor", "#000000")

        assertEquals("#000000", updated.primaryColor)
    }

    @Test
    fun testUpdateFieldToggle() {
        val state = TemplateFormState(hideLineItems = false)
        val updated = state.updateField("hideLineItems", true)

        assertTrue(updated.hideLineItems)
        assertFalse(state.hideLineItems)
    }

    @Test
    fun testUpdateMultipleFields() {
        var state = TemplateFormState(name = "Old", designType = "PROFESSIONAL")
        state = state.updateField("name", "New")
        state = state.updateField("designType", "MINIMAL")

        assertEquals("New", state.name)
        assertEquals("MINIMAL", state.designType)
    }

    @Test
    fun testValidHexColor() {
        val validColors = listOf(
            "#FF5722",
            "#000000",
            "#FFFFFF",
            "#ff5722",
            "#abc123"
        )

        validColors.forEach { color ->
            val state = TemplateFormState(
                name = "Test",
                primaryColor = color,
                companyName = "Test"
            )
            val errors = state.validate()
            assertFalse(
                errors.containsKey("primaryColor"),
                "Color $color should be valid"
            )
        }
    }

    @Test
    fun testInvalidHexColors() {
        val invalidColors = listOf(
            "FF5722", // Missing #
            "#FF572", // Too short
            "#FF57222", // Too long
            "#GGGGGG", // Invalid chars
            "red" // Color name
        )

        invalidColors.forEach { color ->
            val state = TemplateFormState(
                name = "Test",
                primaryColor = color,
                companyName = "Test"
            )
            val errors = state.validate()
            assertTrue(
                errors.containsKey("primaryColor"),
                "Color $color should be invalid"
            )
        }
    }

    @Test
    fun testAllFieldTypes() {
        val fieldTypes = listOf("TEXT", "NUMBER", "DATE")
        fieldTypes.forEach { type ->
            val field = com.emul8r.bizap.data.local.entities.InvoiceCustomField(
                id = "field",
                templateId = "template",
                label = "Label",
                fieldType = type,
                displayOrder = 1
            )
            // Just verify they can be created without error
            assertEquals(type, field.fieldType)
        }
    }

    @Test
    fun testDesignTypeEnum() {
        val state1 = TemplateFormState(designType = DesignType.PROFESSIONAL.name)
        val state2 = TemplateFormState(designType = DesignType.MINIMAL.name)
        val state3 = TemplateFormState(designType = DesignType.BRANDED.name)

        assertEquals("PROFESSIONAL", state1.designType)
        assertEquals("MINIMAL", state2.designType)
        assertEquals("BRANDED", state3.designType)
    }

    @Test
    fun testFontFamilyEnum() {
        val state1 = TemplateFormState(fontFamily = FontFamily.SANS_SERIF.name)
        val state2 = TemplateFormState(fontFamily = FontFamily.SERIF.name)

        assertEquals("SANS_SERIF", state1.fontFamily)
        assertEquals("SERIF", state2.fontFamily)
    }
}

