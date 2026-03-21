package com.emul8r.bizap.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.domain.model.InvoiceCustomization
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvoiceCustomizationEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun classicCustomizationEditor_renders() {
        composeTestRule.setContent {
            ClassicInvoiceCustomizationEditor(
                customization = InvoiceCustomization(),
                onCustomizationChange = {}
            )
        }

        composeTestRule.onNodeWithText("Invoice Customization").assertExists()
        composeTestRule.onNodeWithText("Company Name").assertExists()
        composeTestRule.onNodeWithText("Template Type").assertExists()
    }

    @Test
    fun modernCustomizationEditor_renders() {
        composeTestRule.setContent {
            ModernInvoiceCustomizationEditor(
                customization = InvoiceCustomization(),
                onCustomizationChange = {}
            )
        }

        composeTestRule.onNodeWithText("Invoice Customization").assertExists()
        composeTestRule.onNodeWithText("Company Name").assertExists()
    }

    @Test
    fun updateCompanyName_callsCallback() {
        var callbackCalled = false
        var updatedCustomization: InvoiceCustomization? = null

        composeTestRule.setContent {
            ClassicInvoiceCustomizationEditor(
                customization = InvoiceCustomization(companyName = ""),
                onCustomizationChange = {
                    callbackCalled = true
                    updatedCustomization = it
                }
            )
        }

        composeTestRule.onNodeWithText("Company Name").performClick()
        composeTestRule.onNodeWithText("Company Name").performTextInput("Test Company")

        composeTestRule.waitUntil(timeoutMillis = 1000) { callbackCalled }
        assert(updatedCustomization?.companyName == "Test Company")
    }

    @Test
    fun templateSelection_updates() {
        var callbackCalled = false
        var updatedTemplate = ""

        composeTestRule.setContent {
            ClassicInvoiceCustomizationEditor(
                customization = InvoiceCustomization(templateType = "standard"),
                onCustomizationChange = {
                    callbackCalled = true
                    updatedTemplate = it.templateType
                }
            )
        }

        composeTestRule.onNodeWithText("Minimal").performClick()

        composeTestRule.waitUntil(timeoutMillis = 1000) { callbackCalled }
        assert(updatedTemplate == "minimal")
    }
}

