package com.emul8r.bizap.ui.gui2.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Returns an infinite shimmer brush for skeleton loading effects.
 */
@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

/**
 * A single skeleton line placeholder with shimmer animation.
 *
 * @param modifier Additional modifier (controls width/height of the placeholder line).
 */
@Composable
fun SkeletonLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(shimmerBrush())
    )
}

/**
 * Skeleton loading card placeholder for dashboard metric cards.
 */
@Composable
fun SkeletonMetricCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkeletonLine(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
        SkeletonLine(modifier = Modifier.fillMaxWidth(0.7f).height(24.dp))
    }
}

/**
 * Full dashboard skeleton loading state: two rows of metric card placeholders.
 */
@Composable
fun DashboardSkeletonV2(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonLine(modifier = Modifier.fillMaxWidth(0.4f).height(20.dp))
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonMetricCard(modifier = Modifier.weight(1f))
                SkeletonMetricCard(modifier = Modifier.weight(1f))
            }
            SkeletonLine(modifier = Modifier.fillMaxWidth().height(36.dp))
        }
    }
}
