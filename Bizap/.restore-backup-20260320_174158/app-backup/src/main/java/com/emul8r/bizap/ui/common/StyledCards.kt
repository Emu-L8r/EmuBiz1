package com.emul8r.bizap.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

/**
 * Color-coded metric card with border, background tint, icon and value display.
 * Perfect for dashboard metric displays with visual hierarchy.
 */
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
 */
@Composable
fun StatusBadge(
    status: InvoiceStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (status) {
        InvoiceStatus.PAID -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.12f),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle
        )
        InvoiceStatus.SENT -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.12f),
            Color(0xFF1565C0),
            Icons.Default.Send
        )
        InvoiceStatus.DRAFT -> Triple(
            Color(0xFF999999).copy(alpha = 0.12f),
            Color(0xFF666666),
            Icons.Default.Edit
        )
        InvoiceStatus.OVERDUE -> Triple(
            Color(0xFFB3261E).copy(alpha = 0.12f),
            Color(0xFFC62828),
            Icons.Default.Error
        )
        InvoiceStatus.PARTIALLY_PAID -> Triple(
            Color(0xFFFFA500).copy(alpha = 0.12f),
            Color(0xFFE65100),
            Icons.Default.Schedule
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
 */
@Composable
fun StatusBadgeFromString(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (status.uppercase()) {
        "PAID" -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.12f),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle
        )
        "SENT" -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.12f),
            Color(0xFF1565C0),
            Icons.Default.Send
        )
        "DRAFT" -> Triple(
            Color(0xFF999999).copy(alpha = 0.12f),
            Color(0xFF666666),
            Icons.Default.Edit
        )
        "OVERDUE" -> Triple(
            Color(0xFFB3261E).copy(alpha = 0.12f),
            Color(0xFFC62828),
            Icons.Default.Error
        )
        "PARTIALLY_PAID" -> Triple(
            Color(0xFFFFA500).copy(alpha = 0.12f),
            Color(0xFFE65100),
            Icons.Default.Schedule
        )
        else -> Triple(
            Color(0xFF999999).copy(alpha = 0.12f),
            Color(0xFF666666),
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
