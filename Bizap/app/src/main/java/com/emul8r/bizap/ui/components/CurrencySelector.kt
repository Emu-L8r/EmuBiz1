package com.emul8r.bizap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.emul8r.bizap.presentation.ui.theme.AppTheme
import com.emul8r.bizap.ui.theme.ThemeManager
import com.emul8r.bizap.ui.components.classic.ClassicCurrencySelector
import com.emul8r.bizap.ui.components.modern.ModernCurrencySelector

@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeManager: ThemeManager = hiltViewModel()
    val theme by themeManager.theme.collectAsStateWithLifecycle()

    when (theme) {
        AppTheme.CLASSIC -> ClassicCurrencySelector(selectedCurrency, onCurrencyChange, modifier)
        AppTheme.MODERN -> ModernCurrencySelector(selectedCurrency, onCurrencyChange, modifier)
    }
}

