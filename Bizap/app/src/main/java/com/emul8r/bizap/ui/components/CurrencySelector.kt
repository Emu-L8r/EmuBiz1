package com.emul8r.bizap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emul8r.bizap.ui.components.classic.ClassicCurrencySelector
import com.emul8r.bizap.ui.components.modern.ModernCurrencySelector

/**
 * Stateless currency selector component.
 *
 * This component is completely stateless - all required state is passed as parameters.
 * This makes it:
 * - Easy to preview in Compose preview
 * - Easy to test without mocking Hilt
 * - Reusable in different contexts
 * - Independent of theme injection
 *
 * @param selectedCurrency Currently selected currency code (e.g., "USD", "EUR")
 * @param onCurrencyChange Callback when currency is changed
 * @param isDarkMode Whether dark mode is enabled (determines UI style)
 * @param modifier Modifier for layout customization
 */
@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    // Determine which UI to show based on isDarkMode parameter
    // This is completely stateless - no injection, no global state access
    val isModernUi = isDarkMode  // Or use theme preference; adjust as needed

    if (isModernUi) {
        ModernCurrencySelector(selectedCurrency, onCurrencyChange, modifier)
    } else {
        ClassicCurrencySelector(selectedCurrency, onCurrencyChange, modifier)
    }
}

