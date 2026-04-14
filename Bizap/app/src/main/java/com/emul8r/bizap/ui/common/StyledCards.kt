package com.emul8r.bizap.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.ui.designsystem.BizapColors

/**
 * Color-coded metric card with border, background tint, icon and value display.
 * Perfect for dashboard metric displays with visual hierarchy.
 *
 * @deprecated Use BizapMetricCard from BizapDesignSystem instead for theme-aware colors.
 * This function is kept for screens that need custom background/border colors.
 */
@Deprecated(
    "Use BizapMetricCard from com.emul8r.bizap.ui.designsystem.BizapDesignSystem instead",
    ReplaceWith("BizapMetricCard(title, value, icon, modifier)")
)
@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    borderColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Status badge with icon and colored background.
 * Used to visually indicate invoice status in lists and cards.
 *
 * @deprecated Use BizapStatusBadge from BizapDesignSystem instead.
 * Migrating to centralized design system for consistency.
 */
@Deprecated(
    "Use BizapStatusBadge from com.emul8r.bizap.ui.designsystem.BizapDesignSystem instead",
    ReplaceWith("BizapStatusBadge(status, modifier)", "com.emul8r.bizap.ui.designsystem.BizapStatusBadge")
)
@Composable
fun StatusBadge(
    status: InvoiceStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (status) {
        InvoiceStatus.PAID -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle
        )
        InvoiceStatus.SENT -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.secondary,
            Icons.AutoMirrored.Filled.Send
        )
        InvoiceStatus.DRAFT -> Triple(
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.outline,
            Icons.Default.Edit
        )
        InvoiceStatus.OVERDUE -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.error,
            Icons.Default.Error
        )
        InvoiceStatus.PARTIALLY_PAID -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Schedule
        )
        InvoiceStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.surfaceVariant,
            Icons.Default.Close
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                tint = textColor,
                modifier = Modifier.size(16.dp),
                contentDescription = null
            )
            Text(
                text = status.name.replace("_", " "),
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Status badge for string-based status (for compatibility with existing code).
 *
 * @deprecated Use BizapStatusBadge from BizapDesignSystem instead.
 * This function is maintained for backward compatibility only.
 */
@Deprecated(
    "Use BizapStatusBadge from com.emul8r.bizap.ui.designsystem.BizapDesignSystem instead",
    ReplaceWith("BizapStatusBadge(InvoiceStatus.valueOf(status), modifier)")
)
@Composable
fun StatusBadgeFromString(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (status.uppercase()) {
        "PAID" -> Triple(
            BizapColors.StatusPaid.copy(alpha = 0.12f),
            BizapColors.StatusPaid,
            Icons.Default.CheckCircle
        )
        "SENT" -> Triple(
            BizapColors.StatusSent.copy(alpha = 0.12f),
            BizapColors.StatusSent,
            Icons.AutoMirrored.Filled.Send
        )
        "DRAFT" -> Triple(
            BizapColors.StatusDraft.copy(alpha = 0.12f),
            BizapColors.StatusDraft,
            Icons.Default.Edit
        )
        "OVERDUE" -> Triple(
            BizapColors.StatusOverdue.copy(alpha = 0.12f),
            BizapColors.StatusOverdue,
            Icons.Default.Error
        )
        "PARTIALLY_PAID" -> Triple(
            BizapColors.StatusPartiallyPaid.copy(alpha = 0.12f),
            BizapColors.StatusPartiallyPaid,
            Icons.Default.Schedule
        )
        else -> Triple(
            BizapColors.StatusDraft.copy(alpha = 0.12f),
            BizapColors.StatusDraft,
            Icons.Default.Info
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                tint = textColor,
                modifier = Modifier.size(16.dp),
                contentDescription = null
            )
            Text(
                text = status.replace("_", " "),
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Colored card wrapper with accent color border and optional title.
 * Provides consistent styling for all content cards throughout the app.
 */
@Composable
fun ColoredCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(2.dp, accentColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            content()
        }
    }
}
