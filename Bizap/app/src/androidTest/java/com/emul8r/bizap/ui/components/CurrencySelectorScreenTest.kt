package com.emul8r.bizap.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencySelectorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun classiccurrencySelector_renders() {
        composeTestRule.setContent {
            ClassicCurrencySelector(
                selectedCurrency = "USD",
                onCurrencyChange = {}
            )
        }

        composeTestRule.onNodeWithText("Currency").assertExists()
        composeTestRule.onNodeWithText("USD").assertExists()
    }

    @Test
    fun modernCurrencySelector_renders() {
        composeTestRule.setContent {
            ModernCurrencySelector(
                selectedCurrency = "USD",
                onCurrencyChange = {}
            )
        }

        composeTestRule.onNodeWithText("Currency").assertExists()
        composeTestRule.onNodeWithText("USD").assertExists()
    }

    @Test
    fun selectCurrency_callsCallback() {
        var callbackCalled = false
        var selectedCurrency = "USD"

        composeTestRule.setContent {
            ClassicCurrencySelector(
                selectedCurrency = "USD",
                onCurrencyChange = {
                    callbackCalled = true
                    selectedCurrency = it
                }
            )
        }

        composeTestRule.onNodeWithText("USD").performClick()
        composeTestRule.onNodeWithText("EUR").performClick()

        composeTestRule.waitUntil(timeoutMillis = 1000) { callbackCalled }
        assert(selectedCurrency == "EUR")
    }

    @Test
    fun currencyOptions_allAvailable() {
        composeTestRule.setContent {
            ClassicCurrencySelector(
                selectedCurrency = "USD",
                onCurrencyChange = {}
            )
        }

        composeTestRule.onNodeWithText("USD").performClick()

        val currencies = listOf("USD", "EUR", "GBP", "AUD", "CAD", "JPY", "CHF", "INR")
        currencies.forEach { currency ->
            composeTestRule.onNodeWithText(currency).assertExists()
        }
    }
}

