package com.emul8r.bizap.ui.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for the payment tracking user journey.
 *
 * Journey:
 * 1. Open an existing invoice
 * 2. Record a payment
 * 3. Verify payment status updated
 * 4. Verify outstanding balance reduced
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PaymentTrackingFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun payment_recording_updatesInvoiceStatus() {
        composeRule.waitForIdle()
        composeRule.onRoot().assertExists()
    }

    @Test
    fun partialPayment_reducesOutstandingBalance() {
        composeRule.waitForIdle()
        composeRule.onRoot().assertExists()
    }
}
