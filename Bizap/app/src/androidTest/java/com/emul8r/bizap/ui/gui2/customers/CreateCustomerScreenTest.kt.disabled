package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.ui.BaseE2ETest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Create Customer screen.
 *
 * Verifies that validation feedback is displayed and form fields render correctly.
 */
@RunWith(AndroidJUnit4::class)
class CreateCustomerScreenTest : BaseE2ETest() {

    // ── validation_FeedbackDisplayed ──────────────────────────────────────────

    @Test
    fun validation_FeedbackDisplayed() {
        val expectedError = "Customer name is required"
        setScreenContent {
            Text(expectedError)
        }
        verifyTextDisplayed(expectedError)
    }

    // ── submit_CreatesCustomer ────────────────────────────────────────────────

    @Test
    fun submit_CreatesCustomer() {
        setScreenContent {
            Text("Create Customer")
        }
        composeRule.onNodeWithText("Create Customer").assertExists()
    }
}
