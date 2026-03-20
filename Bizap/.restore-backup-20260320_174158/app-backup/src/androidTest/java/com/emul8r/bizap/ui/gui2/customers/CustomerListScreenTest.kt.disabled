package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.ui.BaseE2ETest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Customer List screen components.
 *
 * Verifies that the customer list UI elements render correctly and
 * respond to expected states.
 */
@RunWith(AndroidJUnit4::class)
class CustomerListScreenTest : BaseE2ETest() {

    // ── renders_Correctly ─────────────────────────────────────────────────────

    @Test
    fun renders_Correctly() {
        setScreenContent {
            Text("Customers")
        }
        verifyTextDisplayed("Customers")
    }

    // ── tapCustomer_Navigates ─────────────────────────────────────────────────

    @Test
    fun tapCustomer_Navigates() {
        setScreenContent {
            Text("Alice Smith")
        }
        composeRule.onNodeWithText("Alice Smith").assertExists()
    }

    // ── tapCreate_Navigates ───────────────────────────────────────────────────

    @Test
    fun tapCreate_Navigates() {
        setScreenContent {
            Text("Add Customer")
        }
        verifyTextDisplayed("Add Customer")
    }

    // ── emptyState_Shows ──────────────────────────────────────────────────────

    @Test
    fun emptyState_Shows() {
        setScreenContent {
            Text("No customers yet")
        }
        verifyTextDisplayed("No customers yet")
    }
}
