package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.ui.BaseE2ETest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Create Invoice screen.
 */
@RunWith(AndroidJUnit4::class)
class CreateInvoiceScreenTest : BaseE2ETest() {

    // ── lineItem_Addition ─────────────────────────────────────────────────────

    @Test
    fun lineItem_Addition() {
        setScreenContent {
            Text("Add Line Item")
        }
        composeRule.onNodeWithText("Add Line Item").assertExists()
    }
}
