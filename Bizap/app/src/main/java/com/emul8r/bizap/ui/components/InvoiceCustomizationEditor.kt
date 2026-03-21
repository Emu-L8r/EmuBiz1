package com.emul8r.bizap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.emul8r.bizap.domain.model.InvoiceCustomization
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.theme.ThemeManager
import com.emul8r.bizap.ui.components.classic.ClassicInvoiceCustomizationEditor
import com.emul8r.bizap.ui.components.modern.ModernInvoiceCustomizationEditor
import dagger.hilt.android.EntryPointAccessors

@Composable
fun InvoiceCustomizationEditor(
    customization: InvoiceCustomization,
    onCustomizationChange: (InvoiceCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        ThemeManagerEntryPoint::class.java
    )
    val themeManager = entryPoint.themeManager()
    val theme = themeManager.theme.collectAsStateWithLifecycle().value

    when (theme) {
        AppTheme.CLASSIC -> ClassicInvoiceCustomizationEditor(customization, onCustomizationChange, modifier)
        AppTheme.MODERN -> ModernInvoiceCustomizationEditor(customization, onCustomizationChange, modifier)
    }
}

