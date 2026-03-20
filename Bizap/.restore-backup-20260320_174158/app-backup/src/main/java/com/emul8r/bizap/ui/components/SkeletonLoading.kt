package com.emul8r.bizap.ui.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Infinite shimmer brush used by all skeleton placeholders. */
@Composable
private fun shimmerBrush(): Brush {
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
 * A single rectangular shimmer placeholder, useful as a list-item skeleton.
 *
 * @param modifier Modifier applied to the placeholder box.
 * @param height   Height of the placeholder row. Defaults to 60.dp.
 */
@Composable
fun SkeletonLoadingItem(
    modifier: Modifier = Modifier,
    height: Dp = 60.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(shimmerBrush())
    )
}

/**
 * A dashboard-style skeleton loading layout: three shimmer rows mimicking
 * typical metric cards seen on a dashboard screen.
 */
@Composable
fun SkeletonLoadingDashboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading placeholder
        SkeletonLoadingItem(height = 24.dp, modifier = Modifier.fillMaxWidth(0.4f))
        // Three metric row placeholders
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLoadingItem(modifier = Modifier.weight(1f), height = 72.dp)
                SkeletonLoadingItem(modifier = Modifier.weight(1f), height = 72.dp)
            }
        }
    }
}
