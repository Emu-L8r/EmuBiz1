package com.emul8r.bizap.ui.gui2.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.emul8r.bizap.ui.designsystem.BizapColors

/** Semantic status colours used in GUI2 screens. Now uses BizapColors for theme consistency */
object GuiV2Colors {
    val healthy = BizapColors.StatusPaidDark        // green - Excellent health
    val atRisk = BizapColors.AnalyticsWarning       // amber - Warning level
    val highRisk = BizapColors.AnalyticsAtRisk      // red - At risk level
    val paid = BizapColors.StatusSentDark           // blue - Paid/Collected
    val outstanding = BizapColors.Presets.Purple    // purple - Outstanding balance
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
