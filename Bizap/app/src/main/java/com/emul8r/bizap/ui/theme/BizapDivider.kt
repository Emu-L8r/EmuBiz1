package com.emul8r.bizap.ui.theme

import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Bizap standard divider component.
 *
 * Provides consistent divider styling across the app.
 * Uses MaterialTheme.colorScheme.outlineVariant for theme-aware appearance.
 *
 * USAGE:
 * BizapDivider()  // Standard horizontal divider
 * BizapDivider(modifier = Modifier.padding(horizontal = 16.dp))  // With padding
 *
 * Benefits:
 * - Single source of truth for divider styling
 * - Automatically theme-aware (light/dark mode)
 * - Consistent thickness and color across app
 */
@Composable
fun BizapDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 0.5.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

