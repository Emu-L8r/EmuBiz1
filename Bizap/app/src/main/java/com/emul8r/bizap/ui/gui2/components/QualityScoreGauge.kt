package com.emul8r.bizap.ui.gui2.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quality Score Gauge Component (Phase 3)
 *
 * Displays PDF quality score with visual feedback:
 * - 0.90-1.00: Green (Excellent)
 * - 0.75-0.89: Amber (Good)
 * - 0.50-0.74: Orange (Fair)
 * - 0.00-0.49: Red (Poor)
 *
 * **Features:**
 * - Animated color transitions
 * - Percentage display
 * - Status label
 * - Linear progress indicator
 */
@Composable
fun QualityScoreGauge(
    score: Float,
    modifier: Modifier = Modifier
) {
    // Determine color based on score
    val (color, statusLabel) = when {
        score >= 0.90f -> Color(0xFF27AE60) to "Excellent" // Green
        score >= 0.75f -> Color(0xFFF39C12) to "Good"      // Amber
        score >= 0.50f -> Color(0xFFE67E22) to "Fair"      // Orange
        else -> Color(0xFFE74C3C) to "Poor"                 // Red
    }

    // Animate color transition
    val animatedColor by animateColorAsState(
        targetValue = color,
        label = "ScoreColorTransition"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PDF Quality Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = animatedColor,
                fontWeight = FontWeight.Bold
            )
        }

        // Score display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Percentage text
            Text(
                text = "${(score * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                color = animatedColor,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp
            )

            // Status icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(animatedColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        score >= 0.90f -> "✅"
                        score >= 0.75f -> "✓"
                        score >= 0.50f -> "⚠️"
                        else -> "❌"
                    },
                    fontSize = 28.sp
                )
            }
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { score },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = animatedColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Description text
        Text(
            text = when {
                score >= 0.90f -> "✅ Perfect! Your PDF is ready to export."
                score >= 0.75f -> "✓ Good! Minor improvements possible."
                score >= 0.50f -> "⚠️ Fair. Consider the suggestions below."
                else -> "❌ Poor. Address the issues to improve quality."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Preview composable for QualityScoreGauge
 */
@Composable
fun QualityScoreGaugePreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QualityScoreGauge(score = 0.95f) // Excellent
        QualityScoreGauge(score = 0.80f) // Good
        QualityScoreGauge(score = 0.60f) // Fair
        QualityScoreGauge(score = 0.35f) // Poor
    }
}

