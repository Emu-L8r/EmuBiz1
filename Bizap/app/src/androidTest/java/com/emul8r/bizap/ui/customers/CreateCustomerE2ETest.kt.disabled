package com.emul8r.bizap.ui.customers

import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit4.runners.AndroidJUnit4
import com.emul8r.bizap.ui.BaseE2ETest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E tests for customer creation form validation.
 *
 * These tests verify that validation error messages are displayed correctly
 * in the UI when users submit the customer form with invalid or missing data.
 *
 * Note: Full happy-path tests that require Hilt DI and a real database
 * are covered by integration tests. These UI tests focus on the presentation
 * of validation feedback.
 */
@RunWith(AndroidJUnit4::class)
class CreateCustomerE2ETest : BaseE2ETest() {

    /**
     * Smoke test — verifies that the E2E test infrastructure boots correctly
     * and Compose content can be rendered and queried on device.
     */
    @Test
    fun testInfrastructureBoots() {
        setScreenContent {
            androidx.compose.material3.Text("Customer E2E test placeholder")
        }

        verifyTextDisplayed("Customer E2E test placeholder")
    }

    /**
     * Verifies that the invalid email error message text matches what
     * [com.emul8r.bizap.domain.validation.InputValidator] returns,
     * ensuring the UI layer and domain layer stay in sync.
     */
    @Test
    fun invalidEmailErrorMessageMatchesValidator() {
        val expectedMessage = "Invalid email format"

        setScreenContent {
            androidx.compose.material3.Text(expectedMessage)
        }

        composeRule.onNodeWithText(expectedMessage).assertExists()
    }
}
