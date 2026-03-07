package com.emul8r.bizap.ui.gui2.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Semantic status colours used in GUI2 screens. */
object GuiV2Colors {
    val healthy = Color(0xFF2E7D32)      // green
    val atRisk = Color(0xFFF57C00)       // amber
    val highRisk = Color(0xFFC62828)     // red
    val paid = Color(0xFF1565C0)         // blue
    val outstanding = Color(0xFF6A1B9A)  // purple
}

/**
 * Wraps GUI2 screens in the existing app theme.
 * Provides a convenient entry point for any future GUI2-specific token overrides.
 */
@Composable
fun GuiV2Theme(content: @Composable () -> Unit) {
    // Delegates to the app-level MaterialTheme already applied by BizapTheme.
    content()
}
