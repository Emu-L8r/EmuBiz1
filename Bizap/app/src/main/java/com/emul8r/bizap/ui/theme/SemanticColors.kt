package com.emul8r.bizap.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val ColorScheme.invoiceStatusPaid: Color
    get() = if (this.primary.luminance() > 0.5) {
        Color(0xFF2E7D32) // Dark green for light themes
    } else {
        Color(0xFF81C784) // Light green for dark themes
    }

val ColorScheme.invoiceStatusOverdue: Color
    get() = error

val ColorScheme.invoiceStatusDraft: Color
    get() = outline

val ColorScheme.invoiceStatusSent: Color
    get() = primary

val ColorScheme.riskLow: Color
    get() = if (this.primary.luminance() > 0.5) Color(0xFF1976D2) else Color(0xFF64B5F6)

val ColorScheme.riskMedium: Color
    get() = if (this.primary.luminance() > 0.5) Color(0xFFF57C00) else Color(0xFFFFB74D)

val ColorScheme.riskHigh: Color
    get() = error
