package com.emul8r.bizap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.theme.Spacing

/**
 * Status Badge component for displaying invoice/payment status with semantic coloring.
 *
 * IMPROVES:
 * - Consistent status display across all screens
 * - Semantic coloring (green=paid, amber=pending, red=overdue, etc.)
 * - Optional icon for visual clarity
 * - Compact and readable design
 * - Accessibility with proper contrast
 *
 * Usage:
 * ```
 * StatusBadge(status = "Paid", style = StatusStyle.SUCCESS)
 * StatusBadge(status = "Overdue", style = StatusStyle.ERROR, icon = Icons.Default.Warning)
 * StatusBadge(status = "Draft", style = StatusStyle.NEUTRAL)
 * ```
 */

enum class StatusStyle {
    SUCCESS,     // Green - paid, completed
    WARNING,     // Amber - pending, needs attention
    ERROR,       // Red - overdue, failed, critical
    INFO,        // Blue - informational
    NEUTRAL      // Gray - draft, neutral
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    style: StatusStyle = StatusStyle.NEUTRAL,
    icon: ImageVector? = null,
    showIcon: Boolean = true
) {
    val (backgroundColor, textColor, iconColor) = when (style) {
        StatusStyle.SUCCESS -> Triple(
            Color(0xFF2E7D32).copy(alpha = 0.12f),
            Color(0xFF1B5E20),
            Color(0xFF2E7D32)
        )
        StatusStyle.WARNING -> Triple(
            Color(0xFFF57F17).copy(alpha = 0.12f),
            Color(0xFFE65100),
            Color(0xFFF57F17)
        )
        StatusStyle.ERROR -> Triple(
            Color(0xFFD32F2F).copy(alpha = 0.12f),
            Color(0xFF9C2C2C),
            Color(0xFFD32F2F)
        )
        StatusStyle.INFO -> Triple(
            Color(0xFF1976D2).copy(alpha = 0.12f),
            Color(0xFF0D47A1),
            Color(0xFF1976D2)
        )
        StatusStyle.NEUTRAL -> Triple(
            Color.Gray.copy(alpha = 0.12f),
            Color.DarkGray,
            Color.Gray
        )
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                .height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            if (showIcon && icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = iconColor
                )
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = textColor
            )
        }
    }
}

/**
 * Inline status indicator - minimal badge for compact spaces.
 */
@Composable
fun InlineStatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    style: StatusStyle = StatusStyle.NEUTRAL
) {
    val (backgroundColor, textColor) = when (style) {
        StatusStyle.SUCCESS -> Pair(
            Color(0xFF2E7D32).copy(alpha = 0.15f),
            Color(0xFF1B5E20)
        )
        StatusStyle.WARNING -> Pair(
            Color(0xFFF57F17).copy(alpha = 0.15f),
            Color(0xFFE65100)
        )
        StatusStyle.ERROR -> Pair(
            Color(0xFFD32F2F).copy(alpha = 0.15f),
            Color(0xFF9C2C2C)
        )
        StatusStyle.INFO -> Pair(
            Color(0xFF1976D2).copy(alpha = 0.15f),
            Color(0xFF0D47A1)
        )
        StatusStyle.NEUTRAL -> Pair(
            Color.Gray.copy(alpha = 0.15f),
            Color.DarkGray
        )
    }

    Box(
        modifier = modifier
            .background(backgroundColor, shape = MaterialTheme.shapes.extraSmall)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}

/**
 * Circular status indicator for dashboard or list items.
 */
@Composable
fun CircularStatusIndicator(
    status: String,
    modifier: Modifier = Modifier,
    style: StatusStyle = StatusStyle.NEUTRAL,
    size: Float = 32f
) {
    val color = when (style) {
        StatusStyle.SUCCESS -> Color(0xFF2E7D32)
        StatusStyle.WARNING -> Color(0xFFF57F17)
        StatusStyle.ERROR -> Color(0xFFD32F2F)
        StatusStyle.INFO -> Color(0xFF1976D2)
        StatusStyle.NEUTRAL -> Color.Gray
    }

    Surface(
        modifier = modifier.size(size.dp),
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = status.take(1).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}

/**
 * Status pill with optional description text.
 */
@Composable
fun StatusPill(
    status: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    style: StatusStyle = StatusStyle.NEUTRAL,
    icon: ImageVector? = null
) {
    val (backgroundColor, textColor, iconColor) = when (style) {
        StatusStyle.SUCCESS -> Triple(
            Color(0xFF2E7D32).copy(alpha = 0.12f),
            Color(0xFF1B5E20),
            Color(0xFF2E7D32)
        )
        StatusStyle.WARNING -> Triple(
            Color(0xFFF57F17).copy(alpha = 0.12f),
            Color(0xFFE65100),
            Color(0xFFF57F17)
        )
        StatusStyle.ERROR -> Triple(
            Color(0xFFD32F2F).copy(alpha = 0.12f),
            Color(0xFF9C2C2C),
            Color(0xFFD32F2F)
        )
        StatusStyle.INFO -> Triple(
            Color(0xFF1976D2).copy(alpha = 0.12f),
            Color(0xFF0D47A1),
            Color(0xFF1976D2)
        )
        StatusStyle.NEUTRAL -> Triple(
            Color.Gray.copy(alpha = 0.12f),
            Color.DarkGray,
            Color.Gray
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textColor
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}






