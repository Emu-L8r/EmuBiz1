package com.emul8r.bizap.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.domain.model.LineItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LineItemsEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun classicLineItemsEditor_renders() {
        composeTestRule.setContent {
            ClassicLineItemsEditor(
                items = emptyList(),
                onItemsChange = {}
            )
        }

        composeTestRule.onNodeWithText("Add Item").assertExists()
        composeTestRule.onNodeWithText("Line Items").assertExists()
    }

    @Test
    fun modernLineItemsEditor_renders() {
        composeTestRule.setContent {
            ModernLineItemsEditor(
                items = emptyList(),
                onItemsChange = {}
            )
        }

        composeTestRule.onNodeWithText("+ Add Item").assertExists()
        composeTestRule.onNodeWithText("Line Items").assertExists()
    }

    @Test
    fun addLineItem_increasesListSize() {
        var items = listOf(
            LineItem(1, "Item 1", 1, 100.0)
        )
        var callCount = 0

        composeTestRule.setContent {
            ClassicLineItemsEditor(
                items = items,
                onItemsChange = {
                    items = it
                    callCount++
                }
            )
        }

        composeTestRule.onNodeWithText("Add Item").performClick()

        composeTestRule.waitUntil(timeoutMillis = 1000) { callCount > 0 }
        assert(items.size > 1) { "Expected items to increase after adding" }
    }

    @Test
    fun deleteLineItem_removesFromList() {
        var items = listOf(
            LineItem(1, "Item 1", 1, 100.0),
            LineItem(2, "Item 2", 2, 200.0)
        )
        var callCount = 0

        composeTestRule.setContent {
            ClassicLineItemsEditor(
                items = items,
                onItemsChange = {
                    items = it
                    callCount++
                }
            )
        }

        // Find and click delete button for first item
        composeTestRule.onAllNodes(hasTestTag("delete_button")).onFirst().performClick()

        composeTestRule.waitUntil(timeoutMillis = 1000) { callCount > 0 }
        assert(items.size == 1) { "Expected items to decrease after deleting" }
    }

    @Test
    fun lineItem_totalCalculatesCorrectly() {
        val item = LineItem(1, "Item", 3, 50.0)
        assert(item.total == 150.0) { "Expected total to be 150.0 (3 × 50)" }
    }
}

