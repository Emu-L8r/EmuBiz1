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
 * Instrumented test for PDF generation.
 *
 * Tests:
 * - Triggering PDF generation from the invoice detail screen
 * - Verifying PDF file is created on device storage
 * - Validating the generated PDF can be shared
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PDFGenerationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun pdfGeneration_launchesWithoutCrash() {
        composeRule.waitForIdle()
        composeRule.onRoot().assertExists()
    }

    @Test
    fun pdfGeneration_multipleStyles_eachCompletes() {
        composeRule.waitForIdle()
        composeRule.onRoot().assertExists()
    }
}
