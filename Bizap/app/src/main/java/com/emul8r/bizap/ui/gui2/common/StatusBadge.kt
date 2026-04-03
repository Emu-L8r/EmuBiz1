package com.emul8r.bizap.ui.gui2.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.InvoiceStatusConstants

/**
 * WIN #14: Status Badge with Colors and Emoji
 *
 * Visual badge for invoice status with:
 * - Color coding (DRAFT=gray, SENT=blue, PAID=green, OVERDUE=red, etc)
 * - Emoji indicator for instant visual recognition
 * - Professional appearance
 *
 * Usage:
 * StatusBadge(status = "PAID")
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, emoji) = getStatusColorAndEmoji(status)

    Row(
        modifier = modifier
            .background(
                color = backgroundColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = InvoiceStatusConstants.getDisplayName(status),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

/**
 * Returns color and emoji for a given invoice status
 */
private fun getStatusColorAndEmoji(status: String): Triple<Color, Color, String> {
    return when (status) {
        InvoiceStatusConstants.DRAFT -> Triple(
            Color(0xFF999999),  // Gray
            Color(0xFF666666),  // Dark gray text
            "📝"                // Draft emoji
        )
        InvoiceStatusConstants.SENT -> Triple(
            Color(0xFF2196F3),  // Blue
            Color(0xFF1565C0),  // Dark blue text
            "✉️"                // Envelope emoji
        )
        InvoiceStatusConstants.PAID -> Triple(
            Color(0xFF4CAF50),  // Green
            Color(0xFF2E7D32),  // Dark green text
            "✅"                // Checkmark emoji
        )
        InvoiceStatusConstants.OVERDUE -> Triple(
            Color(0xFFF44336),  // Red
            Color(0xFFC62828),  // Dark red text
            "⚠️"                // Warning emoji
        )
        InvoiceStatusConstants.PARTIALLY_PAID -> Triple(
            Color(0xFFFF9800),  // Orange
            Color(0xFFE65100),  // Dark orange text
            "⏳"                // Hourglass emoji
        )
        else -> Triple(
            Color(0xFF999999),  // Default gray
            Color(0xFF666666),
            "❓"                // Question mark emoji
        )
    }
}

/**
 * Payment Progress Bar - Shows % of invoice paid
 * Used with invoice cards to show payment status visually
 *
 * Usage:
 * PaymentProgressBar(percent = 75.0f)  // 75% paid
 */
@Composable
fun PaymentProgressBar(
    percent: Float,
    modifier: Modifier = Modifier,
    animateToPercent: Boolean = true
) {
    val displayPercent = percent.coerceIn(0f, 100f)
    val progressColor = when {
        displayPercent >= 100f -> Color(0xFF4CAF50)   // Green when fully paid
        displayPercent >= 50f -> Color(0xFF8BC34A)    // Light green for > 50%
        displayPercent > 0f -> Color(0xFFFF9800)      // Orange for partial
        else -> Color(0xFFF44336)                     // Red for none
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Progress bar
        androidx.compose.material3.LinearProgressIndicator(
            progress = { displayPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            drawStopIndicator = {}
        )

        // Percentage text
        Text(
            text = "Payment: ${displayPercent.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

