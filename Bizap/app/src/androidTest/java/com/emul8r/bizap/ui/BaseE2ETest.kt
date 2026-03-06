package com.emul8r.bizap.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertDoesNotExist
import org.junit.Rule

/**
 * Base class for all Compose UI / E2E tests.
 *
 * Provides common helper methods for interacting with the Compose test rule,
 * keeping individual test classes concise and consistent.
 */
open class BaseE2ETest {

    @get:Rule
    val composeRule: ComposeTestRule = createComposeRule()

    /**
     * Sets the Compose content under test, wrapped in [MaterialTheme] defaults.
     * Because [BizapTheme] requires a [ThemeConfig], we use plain [MaterialTheme]
     * here to keep E2E tests self-contained and free from DI setup.
     */
    protected fun setScreenContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            content()
        }
    }

    protected fun clickButton(text: String) {
        composeRule.onNodeWithText(text).performClick()
    }

    protected fun fillTextField(tag: String, text: String) {
        composeRule.onNodeWithTag(tag).performTextInput(text)
    }

    protected fun verifyTextDisplayed(text: String) {
        composeRule.onNodeWithText(text).assertExists()
    }

    protected fun verifyTextNotDisplayed(text: String) {
        composeRule.onNodeWithText(text).assertDoesNotExist()
    }
}
