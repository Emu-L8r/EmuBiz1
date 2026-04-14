package com.emul8r.bizap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.ui.theme.Dimensions
import com.emul8r.bizap.ui.theme.Spacing
import com.emul8r.bizap.ui.theme.TextSizes

/**
 * ============================================================================
 * STATUS INDICATOR COMPONENT - Visual status feedback
 * ============================================================================
 *
 * A component for showing status with icon and text:
 * - Success (green) - ✓ Valid/Complete
 * - Error (red) - ✗ Invalid/Problem
 * - Warning (amber) - ⚠️ Caution/Pending
 * - Info (blue) - ℹ️ Information
 *
 * Provides clear visual feedback about field/section state.
 */

enum class StatusType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

@Composable
fun StatusIndicator(
    status: StatusType,
    text: String = "",
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (status) {
        StatusType.SUCCESS -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        StatusType.ERROR -> Icons.Default.Error to MaterialTheme.colorScheme.error
        StatusType.WARNING -> Icons.Default.Error to MaterialTheme.colorScheme.tertiary
        StatusType.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.secondary
    }

    Row(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.name,
            tint = color,
            modifier = Modifier.size(Dimensions.iconSizeSmall)
        )

        if (text.isNotEmpty()) {
            Text(
                text = text,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                    fontSize = TextSizes.labelSmall,
                    fontWeight = FontWeight.SemiBold
                ),
                color = color
            )
        }
    }
}

