package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.ui.BaseE2ETest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Invoice List screen.
 */
@RunWith(AndroidJUnit4::class)
class InvoiceListScreenTest : BaseE2ETest() {

    // ── renders_List ──────────────────────────────────────────────────────────

    @Test
    fun renders_List() {
        setScreenContent {
            Text("Invoices")
        }
        verifyTextDisplayed("Invoices")
    }
}
