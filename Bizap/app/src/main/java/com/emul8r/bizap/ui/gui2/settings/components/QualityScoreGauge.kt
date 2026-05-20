package com.emul8r.bizap.ui.gui2.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Quality score gauge displaying PDF quality rating (0.0 - 1.0).
 * Provides visual feedback and color-coded status.
 *
 * Phase 3.5: PDF Quality Visualization
 */
@Composable
fun QualityScoreGauge(
    qualityScore: Float,
    modifier: Modifier = Modifier
) {
    val clampedScore = qualityScore.coerceIn(0.0f, 1.0f)

    // Animated color based on score
    val gaugeColor by animateColorAsState(
        targetValue = when {
            clampedScore >= 0.9f -> Color(0xFF4CAF50)  // Green - Excellent
            clampedScore >= 0.75f -> Color(0xFF8BC34A)  // Light Green - Good
            clampedScore >= 0.6f -> Color(0xFFFFC107)   // Amber - Fair
            clampedScore >= 0.4f -> Color(0xFFFF9800)   // Orange - Poor
            else -> Color(0xFFf44336)                    // Red - Critical
        },
        label = "gaugeColor"
    )

    // Animated progress value
    val animatedProgress by animateFloatAsState(
        targetValue = clampedScore,
        label = "animatedProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header with status icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PDF Quality Score",
                style = MaterialTheme.typography.titleMedium
            )

            Icon(
                imageVector = when {
                    clampedScore >= 0.85f -> Icons.Default.CheckCircle
                    clampedScore >= 0.6f -> Icons.Default.Info
                    else -> Icons.Default.Error
                },
                contentDescription = "Quality Status",
                tint = gaugeColor,
                modifier = Modifier.size(24.dp)
            )
        }

        // Progress bar with rounded corners
        ClipShape(shape = RoundedCornerShape(8.dp)) {
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                color = gaugeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        // Score display and status text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${(clampedScore * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                color = gaugeColor
            )

            Text(
                when {
                    clampedScore >= 0.9f -> "Excellent ✨"
                    clampedScore >= 0.75f -> "Good 👍"
                    clampedScore >= 0.6f -> "Fair ⚠️"
                    clampedScore >= 0.4f -> "Poor 😕"
                    else -> "Critical ❌"
                },
                style = MaterialTheme.typography.labelMedium,
                color = gaugeColor,
                textAlign = TextAlign.End
            )
        }

        // Status description
        Text(
            when {
                clampedScore >= 0.9f -> "Your PDF settings are excellent. Ready to export with confidence!"
                clampedScore >= 0.75f -> "Your PDF settings look good. Minor improvements available."
                clampedScore >= 0.6f -> "Your PDF has some issues. Review warnings below."
                clampedScore >= 0.4f -> "Your PDF needs attention. Address the highlighted issues."
                else -> "Critical issues detected. Please fix errors before exporting."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Compact inline quality indicator (for use in tabs/headers).
 */
@Composable
fun CompactQualityIndicator(
    qualityScore: Float,
    modifier: Modifier = Modifier
) {
    val clampedScore = qualityScore.coerceIn(0.0f, 1.0f)

    val indicatorColor = when {
        clampedScore >= 0.9f -> Color(0xFF4CAF50)
        clampedScore >= 0.75f -> Color(0xFF8BC34A)
        clampedScore >= 0.6f -> Color(0xFFFFC107)
        clampedScore >= 0.4f -> Color(0xFFFF9800)
        else -> Color(0xFFf44336)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                clampedScore >= 0.85f -> Icons.Default.CheckCircle
                clampedScore >= 0.6f -> Icons.Default.Info
                else -> Icons.Default.Error
            },
            contentDescription = "Quality",
            tint = indicatorColor,
            modifier = Modifier.size(16.dp)
        )

        Text(
            "${(clampedScore * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = indicatorColor
        )
    }
}

/**
 * Full width quality status banner.
 */
@Composable
fun QualityStatusBanner(
    qualityScore: Float,
    modifier: Modifier = Modifier
) {
    val clampedScore = qualityScore.coerceIn(0.0f, 1.0f)

    val (backgroundColor, textColor) = when {
        clampedScore >= 0.85f -> Pair(Color(0xFF4CAF50), Color.White)
        clampedScore >= 0.6f -> Pair(Color(0xFFFFC107), Color.Black)
        else -> Pair(Color(0xFFf44336), Color.White)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                when {
                    clampedScore >= 0.85f -> "✅ PDF quality is excellent!"
                    clampedScore >= 0.6f -> "⚠️ Some PDF issues detected. Review the Quality Check tab."
                    else -> "❌ Critical PDF issues. Fix before exporting."
                },
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {},
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    "View Details",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Helper composable for clipping content to a shape.
 */
@Composable
private fun ClipShape(
    shape: RoundedCornerShape,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.clip(shape)) {
        content()
    }
}

