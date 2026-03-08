package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Invoice List screen.
 */
@RunWith(AndroidJUnit4::class)
class InvoiceListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // ── renders_List ──────────────────────────────────────────────────────────

    @Test
    fun renders_List() {
        composeRule.waitForIdle()
        // Verify the activity renders without crashing
    }
}
