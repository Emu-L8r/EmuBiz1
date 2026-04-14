package com.emul8r.bizap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.components.classic.ClassicLineItemsEditor
import com.emul8r.bizap.ui.components.modern.ModernLineItemsEditor

/**
 * Stateless line items editor component.
 *
 * This component is completely stateless - all required state is passed as parameters.
 * This makes it:
 * - Easy to preview in Compose preview
 * - Easy to test without mocking Hilt
 * - Reusable in different contexts
 * - Independent of theme injection
 *
 * @param items List of line items to edit
 * @param onItemsChange Callback when items change
 * @param isDarkMode Whether dark mode is enabled (determines UI)
 * @param modifier Modifier for layout customization
 */
@Composable
fun LineItemsEditor(
    items: List<InvoiceItem>,
    onItemsChange: (List<InvoiceItem>) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onAddPrefilledClick: (() -> Unit)? = null
) {
    // Determine which UI to show based on isDarkMode parameter
    // This is completely stateless - no injection, no global state access
    val isModernUi = isDarkMode  // Or use theme preference; adjust as needed

    if (isModernUi) {
        ModernLineItemsEditor(items, onItemsChange, modifier, onAddPrefilledClick)
    } else {
        ClassicLineItemsEditor(items, onItemsChange, modifier)
    }
}

