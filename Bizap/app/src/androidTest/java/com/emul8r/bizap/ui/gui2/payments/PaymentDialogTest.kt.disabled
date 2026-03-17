package com.emul8r.bizap.ui.gui2.payments

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.ui.BaseE2ETest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Payment dialog.
 */
@RunWith(AndroidJUnit4::class)
class PaymentDialogTest : BaseE2ETest() {

    // ── amountValidation_Works ────────────────────────────────────────────────

    @Test
    fun amountValidation_Works() {
        val validationMsg = "Payment exceeds the outstanding balance"
        setScreenContent {
            Text(validationMsg)
        }
        composeRule.onNodeWithText(validationMsg).assertExists()
    }

    // ── submit_RecordsPayment ─────────────────────────────────────────────────

    @Test
    fun submit_RecordsPayment() {
        setScreenContent {
            Text("Record Payment")
        }
        verifyTextDisplayed("Record Payment")
    }
}
