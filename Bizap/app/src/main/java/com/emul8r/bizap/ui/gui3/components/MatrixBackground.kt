package com.emul8r.bizap.ui.gui3.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import kotlin.random.Random
import com.emul8r.bizap.ui.gui3.theme.MatrixBlack
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import timber.log.Timber

/**
 * MATRIX BACKGROUND - Multi-Layer Falling Character Animation
 *
 * Enhanced with:
 * - Background layer: slow, subtle (depth)
 * - Midground layer: medium speed (immersion)
 * - Foreground layer: fast, bright (presence)
 */
@Composable
fun MatrixBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1.2f,
    enableGlitch: Boolean = true,
    content: @Composable () -> Unit
) {
    Timber.d("MatrixBackground: Rendering animations")
    println("MatrixBackground: RENDERING - MULTI-LAYER")

    Box(modifier = modifier.fillMaxSize().background(MatrixBlack)) {
        // LAYER 1: Slow background columns (12 columns, subtle)
        repeat(12) { idx ->
            FallingCharacterColumnLayered(
                columnIndex = idx,
                xPosition = (idx * 35).dp,
                speed = 8000 + (idx * 200),
                alpha = 0.3f,
                fontSize = 12.sp
            )
        }

        // LAYER 2: Medium speed midground (10 columns, visible)
        repeat(10) { idx ->
            FallingCharacterColumnLayered(
                columnIndex = idx + 100,
                xPosition = (idx * 40).dp,
                speed = 5000 + (idx * 150),
                alpha = 0.6f,
                fontSize = 13.sp
            )
        }

        // LAYER 3: Fast foreground columns (8 columns, prominent)
        repeat(8) { idx ->
            FallingCharacterColumnLayered(
                columnIndex = idx + 200,
                xPosition = (idx * 45).dp,
                speed = 3000 + (idx * 100),
                alpha = 0.9f,
                fontSize = 14.sp
            )
        }

        content()
    }
}

@Composable
private fun FallingCharacterColumnLayered(
    columnIndex: Int,
    xPosition: Dp,
    speed: Int = 4000,
    alpha: Float = 0.9f,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    val characters = listOf("█", "▓", "▒", "░", "█", "0", "1", "█", "►", "◄")

    val animatedProgress = rememberInfiniteTransition(label = "layeredFall_$columnIndex")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = speed,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress_$columnIndex"
        )

    repeat(5) { charIdx ->
        val yOffset = (animatedProgress.value * 1200f).dp + (charIdx * 50).dp - 100.dp

        Text(
            text = characters[(columnIndex + charIdx) % characters.size],
            modifier = Modifier
                .offset(x = xPosition, y = yOffset)
                .alpha(alpha),
            color = MatrixGreen,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun FallingCharacterColumn(
    columnIndex: Int,
    xPosition: Dp
) {
    val characters = listOf("█", "▓", "▒", "░", "█", "0", "1", "█")

    val animatedProgress = rememberInfiniteTransition(label = "matrixFall$columnIndex")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4000 + (columnIndex * 300),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress$columnIndex"
        )

    repeat(4) { charIdx ->
        val yOffset = (animatedProgress.value * 1200f).dp + (charIdx * 60).dp - 120.dp

        Text(
            text = characters[(columnIndex + charIdx) % characters.size],
            modifier = Modifier
                .offset(x = xPosition, y = yOffset)
                .alpha(0.9f),
            color = MatrixGreen,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun MatrixBackgroundStatic(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(MatrixBlack)) {
        Box(modifier = Modifier.fillMaxSize().alpha(0.05f).background(MatrixGreen))
        content()
    }
}

fun Modifier.matrixGlow(intensity: Float = 0.15f): Modifier = this
    .alpha(1f - intensity)
    .blur(radius = 2.dp)






