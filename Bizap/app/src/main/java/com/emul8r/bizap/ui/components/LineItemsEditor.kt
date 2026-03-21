package com.emul8r.bizap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.presentation.ui.theme.AppTheme
import com.emul8r.bizap.ui.theme.ThemeManager
import com.emul8r.bizap.ui.components.classic.ClassicLineItemsEditor
import com.emul8r.bizap.ui.components.modern.ModernLineItemsEditor

@Composable
fun LineItemsEditor(
    items: List<LineItem>,
    onItemsChange: (List<LineItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeManager: ThemeManager = hiltViewModel()
    val theme by themeManager.theme.collectAsStateWithLifecycle()

    when (theme) {
        AppTheme.CLASSIC -> ClassicLineItemsEditor(items, onItemsChange, modifier)
        AppTheme.MODERN -> ModernLineItemsEditor(items, onItemsChange, modifier)
    }
}

