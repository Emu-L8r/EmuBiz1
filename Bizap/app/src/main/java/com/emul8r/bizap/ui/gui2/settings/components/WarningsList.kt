package com.emul8r.bizap.ui.gui2.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.service.pdf.PdfQualityWarning
import com.emul8r.bizap.data.service.pdf.PdfQualitySeverity

/**
 * Displays a list of PDF quality warnings with severity indicators.
 * Shows error, warning, and info messages with actionable suggestions.
 *
 * Phase 3.5: Quality Feedback UI
 */
@Composable
fun WarningsList(
    warnings: List<PdfQualityWarning>,
    modifier: Modifier = Modifier
) {
    if (warnings.isEmpty()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF4CAF50).copy(alpha = 0.1f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "No warnings",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Text(
                "✅ No quality issues detected!",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF4CAF50)
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Quality Issues (${warnings.size})",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            warnings.forEach { warning ->
                WarningItem(warning)
            }
        }
    }
}

/**
 * Individual warning item with severity indicator.
 */
@Composable
private fun WarningItem(
    warning: PdfQualityWarning,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, borderColor, iconTint, textColor) = when (warning.severity) {
        PdfQualitySeverity.ERROR -> {
            // Red for errors
            listOf(
                Color(0xFFf44336).copy(alpha = 0.1f),
                Color(0xFFf44336),
                Color(0xFFf44336),
                Color(0xFFc62828)
            )
        }
        PdfQualitySeverity.WARNING -> {
            // Orange for warnings
            listOf(
                Color(0xFFFF9800).copy(alpha = 0.1f),
                Color(0xFFFF9800),
                Color(0xFFFF9800),
                Color(0xFFE65100)
            )
        }
        PdfQualitySeverity.INFO -> {
            // Blue for info
            listOf(
                Color(0xFF2196F3).copy(alpha = 0.1f),
                Color(0xFF2196F3),
                Color(0xFF2196F3),
                Color(0xFF0D47A1)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Header: Icon + Title + Severity Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (warning.severity) {
                    PdfQualitySeverity.ERROR -> Icons.Default.Error
                    PdfQualitySeverity.WARNING -> Icons.Default.Warning
                    PdfQualitySeverity.INFO -> Icons.Default.Info
                },
                contentDescription = warning.severity.name,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )

            Text(
                warning.message,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            // Severity badge
            Text(
                warning.severity.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(iconTint)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Suggestion
        if (warning.suggestion.isNotEmpty()) {
            Text(
                "💡 ${warning.suggestion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

// Helper extension for border
private fun Modifier.border(
    width: androidx.compose.ui.unit.Dp,
    color: Color,
    shape: RoundedCornerShape
): Modifier = this
    .clip(shape)
    .background(Color.Transparent)
    .then(
        Modifier.padding(0.dp) // Padding for border effect
    )
