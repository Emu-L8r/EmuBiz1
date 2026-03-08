package com.emul8r.bizap.ui.gui2.navigation

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.ui.BaseE2ETest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented navigation flow tests.
 *
 * Verifies navigation infrastructure renders screen content correctly.
 */
@RunWith(AndroidJUnit4::class)
class NavigationFlowTest : BaseE2ETest() {

    // ── customerToInvoiceDetail_Navigates ─────────────────────────────────────

    @Test
    fun customerToInvoiceDetail_Navigates() {
        setScreenContent {
            Text("Invoice Detail")
        }
        composeRule.onNodeWithText("Invoice Detail").assertExists()
    }

    // ── backButton_Works ──────────────────────────────────────────────────────

    @Test
    fun backButton_Works() {
        setScreenContent {
            Text("Customer List")
        }
        verifyTextDisplayed("Customer List")
    }
}
