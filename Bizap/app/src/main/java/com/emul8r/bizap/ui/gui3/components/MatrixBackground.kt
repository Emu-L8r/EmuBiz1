package com.emul8r.bizap.ui.gui3.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import com.emul8r.bizap.ui.gui3.theme.MatrixBlack
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen

/**
 * IMMERSIVE MATRIX BACKGROUND - Enhanced with Intensity Control
 *
 * Creates authentic Matrix aesthetic with:
 * - Adjustable cascading code columns (intensity 0.5 to 1.5)
 * - Variable animation speeds for parallax effect
 * - Varied character sets per column
 * - Layered rendering for depth
 * - GPU-optimized smooth animation
 */
@Composable
fun MatrixBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1.0f,  // Range: 0.5 (subtle) to 1.5 (intense)
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(MatrixBlack)) {
        // LAYER 1: Slow background columns (depth/parallax)
        repeat((15 * intensity).toInt()) { idx ->
            FallingCharacterColumn(
                columnIndex = idx,
                speed = "slow",
                depth = "background",
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // LAYER 2: Medium speed columns (mid-ground)
        repeat((10 * intensity).toInt()) { idx ->
            FallingCharacterColumn(
                columnIndex = idx + 15,
                speed = "medium",
                depth = "midground",
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // LAYER 3: Fast foreground columns (immersion)
        repeat((15 * intensity).toInt()) { idx ->
            FallingCharacterColumn(
                columnIndex = idx + 25,
                speed = "fast",
                depth = "foreground",
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // Optional: Add subtle glitch effects for very high intensity
        if (intensity > 1.2f) {
            GlitchEffectLayer()
        }

        // Content rendered on top
        content()
    }
}

@Composable
private fun GlitchEffectLayer() {
    val glitchAnimation = rememberInfiniteTransition(label = "glitch")
    val glitchAlpha by glitchAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchAlpha"
    )

    // Random glitch line effect
    if (glitchAlpha > 0.95f) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((Random.nextInt(3) + 1).dp)
                .offset(y = Random.nextInt(800).dp)
                .background(MatrixGreen.copy(alpha = 0.08f))
        )
    }
}

@Composable
private fun FallingCharacterColumn(
    columnIndex: Int,
    speed: String = "normal",
    depth: String = "normal",
    modifier: Modifier = Modifier
) {
    // Varied character sets for authenticity
    val characterSet = when (columnIndex % 5) {
        0 -> listOf("0", "1", "0", "1", "1", "0", "1", "0", "1", "0")
        1 -> listOf("ア", "イ", "ウ", "エ", "オ", "カ", "キ", "ク", "ケ")
        2 -> listOf("█", "▓", "▒", "░", "▀", "▄", "█", "▓", "▒")
        3 -> listOf("+", "-", "*", "/", "=", ">", "<", "|", "~")
        else -> listOf("█", "▓", "▒", "░", "█", "▓", "▒", "░", "█")
    }

    // Varied speeds for parallax effect
    val durationMs = when (speed) {
        "slow" -> 14000 + Random.nextInt(7000)    // 14-21 sec (background)
        "medium" -> 10000 + Random.nextInt(4000)  // 10-14 sec (midground)
        "fast" -> 6000 + Random.nextInt(3000)     // 6-9 sec (foreground)
        else -> 8000 + Random.nextInt(4000)       // 8-12 sec (normal)
    }

    val delayMs = (columnIndex * 100 + Random.nextInt(150)).toLong()

    val animatedOffset = rememberInfiniteTransition(label = "matrixFall$columnIndex")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMs,
                    delayMillis = delayMs.toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "matrixFallOffset$columnIndex"
        )

    // Alpha varies by depth for immersive layering
    val alphaValue = when (depth) {
        "background" -> 0.06f   // Very subtle background
        "midground" -> 0.12f    // Medium visibility
        "foreground" -> 0.28f   // Prominent foreground
        else -> 0.15f
    }

    // Column spacing for better visibility
    val xOffset = when (depth) {
        "background" -> (columnIndex * 20).dp    // Tight spacing (background)
        "midground" -> (columnIndex * 22).dp     // Medium spacing
        "foreground" -> (columnIndex * 24).dp    // Wider spacing (foreground)
        else -> (columnIndex * 22).dp
    }

    Text(
        text = characterSet[Random.nextInt(characterSet.size)],
        modifier = modifier
            .offset(
                x = xOffset,
                y = (animatedOffset.value * 1100).dp
            )
            .alpha(alphaValue),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = MatrixGreen
    )
}

@Composable
fun MatrixBackgroundStatic(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(MatrixBlack)) {
        Box(modifier = Modifier.fillMaxSize().alpha(0.05f).background(MatrixGreen))
        content()
    }
}

/**
 * Apply subtle glow effect to Matrix components
 * Creates a glowing border effect for cyberpunk aesthetic
 */
fun Modifier.matrixGlow(intensity: Float = 0.15f): Modifier = this
    .alpha(1f - intensity)
    .blur(radius = 2.dp)



