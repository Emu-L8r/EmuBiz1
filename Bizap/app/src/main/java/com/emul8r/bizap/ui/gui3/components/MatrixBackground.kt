package com.emul8r.bizap.ui.gui3.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.emul8r.bizap.ui.gui3.theme.MatrixGreenBright
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui3.util.MatrixCascadeState
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * MATRIX BACKGROUND - Multi-Layer Falling Character Animation
 *
 * Enhanced with:
 * - Background layer: slow, subtle (depth)
 * - Midground layer: medium speed (immersion)
 * - Foreground layer: fast, bright (presence)
 */
private const val MATRIX_RAIN_CANVAS_HEIGHT_DP = 1200f
private const val MATRIX_RAIN_VIEWPORT_HEIGHT_MULTIPLIER = 1.08f
private const val MATRIX_RAIN_MUTATION_BUCKETS = 8
private const val MATRIX_RAIN_BASE_TRAIL_SPACING_MULTIPLIER = 3.45f
private const val MATRIX_RAIN_HEAD_GLOW_ALPHA = 0.22f
private const val MATRIX_RAIN_GAP_CHANCE_BASE = 0.035f
private const val MATRIX_RAIN_MIN_COLUMN_SPACING_DP = 24f
private const val MATRIX_RAIN_MAX_COLUMN_SPACING_DP = 52f
private const val MATRIX_RAIN_HEAD_GLOW_OFFSET_DP = 0.75f

private val MATRIX_BINARY_GLYPHS = listOf("0", "1")
private val MATRIX_SYMBOL_GLYPHS = listOf("*", "+", "-", "/", "\\", "|", "~", "^")
private val MATRIX_KATAKANA_GLYPHS = listOf("ニ", "ハ", "ミ", "ヲ", "ネ", "ホ", "ヘ", "レ", "カ", "キ")

private data class MatrixRainColumnState(
    val trailLength: Int,
    val driftDp: Dp,
    val phaseOffset: Float,
    val speedMultiplier: Float,
    val gapChance: Float,
    val glyphSeed: Int,
    val heroColumn: Boolean
)

@Composable
fun MatrixBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1.2f,
    enableGlitch: Boolean = true,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MatrixBlack)) {
        val densityScale = intensity.coerceIn(0.75f, 1.35f)
        val viewportWidthValue = maxWidth.value.coerceAtLeast(1f)
        val viewportHeightValue = maxOf(maxHeight.value.coerceAtLeast(1f), MATRIX_RAIN_CANVAS_HEIGHT_DP)
        val viewportCanvasHeightDp = (viewportHeightValue * MATRIX_RAIN_VIEWPORT_HEIGHT_MULTIPLIER).dp

        val farSpacingValue = (viewportWidthValue / (13f + (densityScale * 2.0f)))
            .coerceIn(MATRIX_RAIN_MIN_COLUMN_SPACING_DP, MATRIX_RAIN_MAX_COLUMN_SPACING_DP)
        val midSpacingValue = (farSpacingValue * 0.86f).coerceIn(22f, 48f)
        val nearSpacingValue = (farSpacingValue * 0.74f).coerceIn(20f, 42f)

        val farColumns = (viewportWidthValue / farSpacingValue).roundToInt().coerceIn(6, 16)
        val midColumns = (viewportWidthValue / midSpacingValue).roundToInt().coerceIn(5, 14)
        val nearColumns = (viewportWidthValue / nearSpacingValue).roundToInt().coerceIn(5, 12)

        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1: fast backdrop for depth, but with a softer alpha so UI stays readable.
            repeat(farColumns) { idx ->
                FallingCharacterColumnLayered(
                    columnIndex = idx,
                    xPosition = (farSpacingValue * idx.toFloat()).dp,
                    speed = 3200 + (idx % 4) * 140 + ((idx * 37) % 180),
                    alpha = 0.12f,
                    fontSize = 11.sp,
                    densityScale = densityScale,
                    enableGlitch = enableGlitch,
                    canvasHeightDp = viewportCanvasHeightDp
                )
            }

            // Layer 2: midground with readable motion and denser katakana stream.
            repeat(midColumns) { idx ->
                FallingCharacterColumnLayered(
                    columnIndex = idx + 100,
                    xPosition = (midSpacingValue * idx.toFloat()).dp,
                    speed = 5200 + (idx % 5) * 150 + ((idx * 29) % 160),
                    alpha = 0.24f,
                    fontSize = 12.sp,
                    densityScale = densityScale,
                    enableGlitch = enableGlitch,
                    canvasHeightDp = viewportCanvasHeightDp
                )
            }

            // Layer 3: foreground streams are slower, brighter, and more Matrix-like.
            repeat(nearColumns) { idx ->
                FallingCharacterColumnLayered(
                    columnIndex = idx + 200,
                    xPosition = (nearSpacingValue * idx.toFloat()).dp,
                    speed = 7600 + (idx % 6) * 160 + ((idx * 41) % 220),
                    alpha = 0.36f,
                    fontSize = 13.sp,
                    densityScale = densityScale,
                    enableGlitch = enableGlitch,
                    canvasHeightDp = viewportCanvasHeightDp
                )
            }

            content()
        }
    }
}

@Composable
private fun FallingCharacterColumnLayered(
    columnIndex: Int,
    xPosition: Dp,
    speed: Int = 4000,
    alpha: Float = 0.9f,
    fontSize: TextUnit = 14.sp,
    densityScale: Float = 1f,
    enableGlitch: Boolean = true,
    canvasHeightDp: Dp = MATRIX_RAIN_CANVAS_HEIGHT_DP.dp
) {
    val columnState = remember(columnIndex, densityScale) {
        val seedRandom = Random(columnIndex.toLong() * 31L + (densityScale * 1000).toLong())
        val layerIndex = columnIndex / 100
        val trailBase = when {
            layerIndex >= 2 -> 6    // Reduced from 8 to 6 for punchier heads
            layerIndex >= 1 -> 5    // Reduced from 7 to 5
            else -> 5               // Reduced from 6 to 5
        }
        val densityBoost = when {
            densityScale > 1.18f -> 1  // Reduced from 2 to 1
            densityScale > 1.02f -> 0  // Reduced from 1 to 0
            else -> 0
        }
        val trailJitter = seedRandom.nextInt(0, 2)  // Reduced from 3 to 2
        val trailLength = (trailBase + densityBoost + trailJitter + if (layerIndex >= 2) 0 else 0).coerceAtLeast(3)  // Min 3 instead of 4
        val heroChance = when {
            layerIndex >= 2 -> 10
            layerIndex >= 1 -> 13
            else -> 16
        }

        MatrixRainColumnState(
            trailLength = trailLength,
            driftDp = (seedRandom.nextInt(-5, 6)).dp,
            phaseOffset = seedRandom.nextFloat(),
            speedMultiplier = 0.75f + seedRandom.nextFloat() * 0.55f,
            gapChance = (MATRIX_RAIN_GAP_CHANCE_BASE + seedRandom.nextFloat() * 0.04f) *
                if (densityScale > 1f) 0.85f else 1.1f,
            glyphSeed = seedRandom.nextInt(),
            heroColumn = seedRandom.nextInt(heroChance) == 0
        )
    }

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

    val glitchFrame = (animatedProgress.value * 100).toInt()  // Frame number for glitch trigger
    val glitchSeed = columnState.glyphSeed xor (densityScale * 1000).toInt()
    val glitchPhase = (glitchSeed and Int.MAX_VALUE) % 100

    // Glitch effect: rare and hero-only for visual interest
    val shouldGlitch = enableGlitch && columnState.heroColumn &&
        (glitchPhase < 4) && (glitchFrame % 32) < 4

    val shouldGlitchBinary = enableGlitch && columnState.heroColumn &&
        glitchPhase < 3 && (glitchFrame % 28) < 3

    val shouldGhostFlicker = enableGlitch && columnState.heroColumn &&
        glitchPhase < 6 && (glitchFrame % 40) < 4

    if (columnIndex == 0) {
        LaunchedEffect(animatedProgress.value) {
            val cascadeY = (animatedProgress.value * MATRIX_RAIN_CANVAS_HEIGHT_DP)
            MatrixCascadeState.updateCascadePosition(cascadeY)
        }
    }

    val headY = animatedProgress.value * MATRIX_RAIN_CANVAS_HEIGHT_DP
    val xWave = (sin(((animatedProgress.value * 6.2831855f) + columnState.phaseOffset * 4f).toDouble()).toFloat()) * columnState.driftDp.value
    val trailSpacing = fontSize.value * MATRIX_RAIN_BASE_TRAIL_SPACING_MULTIPLIER
    val cycleBucket = (animatedProgress.value * MATRIX_RAIN_MUTATION_BUCKETS).toInt()
    val viewportHeightLimit = canvasHeightDp.value + 150f

    repeat(columnState.trailLength) { charIdx ->
        val trailPosition = if (columnState.trailLength <= 1) 0f else charIdx.toFloat() / (columnState.trailLength - 1).toFloat()
        val yOffsetPx = headY - (charIdx * trailSpacing) - 100f

        if (yOffsetPx < -150f || yOffsetPx > viewportHeightLimit) {
            return@repeat
        }

        val displayChar = resolveMatrixGlyph(
            layerIndex = columnIndex / 100,
            columnIndex = columnIndex,
            segmentIndex = charIdx,
            cycleBucket = cycleBucket,
            heroColumn = columnState.heroColumn,
            trailPosition = trailPosition,
            shouldGlitch = shouldGlitch,
            shouldGlitchBinary = shouldGlitchBinary
        )

        if (displayChar.isBlank()) {
            return@repeat
        }

        val isHead = charIdx == 0
        val trailAlpha = matrixTrailAlpha(alpha, charIdx, columnState.trailLength, isHead)
        val finalColor = when {
            isHead -> MatrixGreenBright
            shouldGlitchBinary -> MatrixGreenBright.copy(alpha = trailAlpha)
            else -> MatrixGreen.copy(alpha = trailAlpha)
        }

        val glitchOffsetX = if (shouldGlitch) {
            matrixDeterministicOffset(
                seed = matrixGlyphHash(columnIndex / 100, columnIndex, charIdx, cycleBucket, columnState.heroColumn),
                axisSeed = 0x51,
                heroColumn = columnState.heroColumn,
                heroRange = 2,
                regularRange = 3
            )
        } else 0.dp
        val glitchOffsetY = if (shouldGlitch) {
            matrixDeterministicOffset(
                seed = matrixGlyphHash(columnIndex / 100, columnIndex, charIdx * 2, cycleBucket, columnState.heroColumn),
                axisSeed = 0xA7,
                heroColumn = columnState.heroColumn,
                heroRange = 1,
                regularRange = 2
            )
        } else 0.dp

        val headGlowBoost = if (isHead) columnState.speedMultiplier + if (columnState.heroColumn) 0.12f else 0f else 1f
        val finalAlpha = (trailAlpha * headGlowBoost).coerceIn(0.02f, 1f)

        Text(
            text = displayChar,
            modifier = Modifier
                .offset(
                    x = xPosition + xWave.dp + glitchOffsetX,
                    y = yOffsetPx.dp + glitchOffsetY
                )
                .alpha(finalAlpha),
            color = finalColor,
            fontSize = if (isHead) fontSize * 1.05f else fontSize,
            fontFamily = FontFamily.Monospace
        )

        if (isHead && !shouldGlitch && columnState.heroColumn) {
            Text(
                text = displayChar,
                modifier = Modifier
                    .offset(x = xPosition + xWave.dp + MATRIX_RAIN_HEAD_GLOW_OFFSET_DP.dp, y = yOffsetPx.dp + MATRIX_RAIN_HEAD_GLOW_OFFSET_DP.dp)
                    .alpha(MATRIX_RAIN_HEAD_GLOW_ALPHA),
                color = MatrixGreenBright,
                fontSize = fontSize * 1.08f,
                fontFamily = FontFamily.Monospace
            )
        }

        if (shouldGhostFlicker && charIdx > 0) {
            Text(
                text = displayChar,
                modifier = Modifier
                    .offset(x = xPosition + xWave.dp + 2.dp, y = yOffsetPx.dp + 3.dp)
                    .alpha(trailAlpha * 0.25f),
                color = MatrixGreen.copy(alpha = 0.4f),
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
@Suppress("unused")
private fun FallingCharacterColumn(
    columnIndex: Int,
    xPosition: Dp
) {
    // Enhanced character mix: 50% binary + 30% hacker symbols + 20% Katakana
    val characters = listOf(
        // Binary (50%)
        "0", "1", "0", "1", "0", "1", "0", "1", "0", "1",
        // Hacker symbols (30%)
        "@", "#", "$", "%", "&", "*", "(", ")", "[", "]",
        "{", "}", "<", ">", "/", "\\", "|", "+", "=", "~",
        // More binary
        "0", "1", "0", "1", "0", "1", "0", "1",
        // Katakana (20%)
        "ニ", "ハ", "ミ", "ヲ", "ネ", "ホ", "ヘ", "レ", "カ", "キ"
    )

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
@Suppress("unused")
fun MatrixBackgroundStatic(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(MatrixBlack)) {
        Box(modifier = Modifier.fillMaxSize().alpha(0.05f).background(MatrixGreen))
        content()
    }
}

@Suppress("unused")
fun Modifier.matrixGlow(intensity: Float = 0.15f): Modifier = this
    .alpha(1f - intensity)
    .blur(radius = 2.dp)

private fun resolveMatrixGlyph(
    layerIndex: Int,
    columnIndex: Int,
    segmentIndex: Int,
    cycleBucket: Int,
    heroColumn: Boolean,
    trailPosition: Float,
    shouldGlitch: Boolean,
    shouldGlitchBinary: Boolean
): String {
    val hash = matrixGlyphHash(layerIndex, columnIndex, segmentIndex, cycleBucket, heroColumn)
    val normalized = hash and Int.MAX_VALUE

    if (shouldGlitch && (normalized % 100) < if (heroColumn) 14 else 10) {
        return MATRIX_SYMBOL_GLYPHS[normalized % MATRIX_SYMBOL_GLYPHS.size]
    }

    if (shouldGlitchBinary) {
        return MATRIX_BINARY_GLYPHS[normalized and 1]
    }

    val layerBias = when (layerIndex) {
        0 -> 0
        1 -> 4
        else -> 8
    }
    val headBias = ((1f - trailPosition.coerceIn(0f, 1f)) * if (heroColumn) 24f else 18f).toInt()
    val roll = (normalized % 100) + headBias + layerBias + if (heroColumn) 6 else 0
    return when {
        roll < if (heroColumn) 76 else if (layerIndex >= 2) 72 else 66 -> MATRIX_KATAKANA_GLYPHS[(normalized ushr 1) % MATRIX_KATAKANA_GLYPHS.size]
        roll < if (layerIndex >= 2) 91 else 88 -> MATRIX_SYMBOL_GLYPHS[(normalized ushr 2) % MATRIX_SYMBOL_GLYPHS.size]
        else -> MATRIX_BINARY_GLYPHS[(normalized ushr 3) and 1]
    }
}

private fun matrixGlyphHash(
    layerIndex: Int,
    columnIndex: Int,
    segmentIndex: Int,
    cycleBucket: Int,
    heroColumn: Boolean
): Int {
    var hash = columnIndex * 31 + layerIndex * 17 + segmentIndex * 13 + cycleBucket * 19
    if (heroColumn) {
        hash = hash xor 0x5A5A5A5A
    }
    return hash
}

private fun matrixTrailAlpha(
    baseAlpha: Float,
    segmentIndex: Int,
    trailLength: Int,
    isHead: Boolean
): Float {
    if (isHead) {
        // Increased head brightness: 0.22f (was 0.14f) for more prominent heads
        return baseAlpha.coerceIn(0.22f, 1f)
    }

    val normalized = segmentIndex.toFloat() / maxOf(1, trailLength - 1).toFloat()
    val falloff = (1f - normalized).coerceIn(0f, 1f)

    // Enhanced exponential falloff: cubic falloff creates "comet tail" effect
    // Quick fade-out after head (90% of trail invisible by mid-point)
    val shapedCurve = falloff * falloff * falloff  // Cubic power for sharp fade
    val enhancedAlpha = baseAlpha * (0.04f + (shapedCurve * 0.88f))  // Reduced min to 0.04f

    return enhancedAlpha.coerceIn(0.008f, 0.72f)
}

private fun matrixDeterministicOffset(
    seed: Int,
    axisSeed: Int,
    heroColumn: Boolean,
    heroRange: Int,
    regularRange: Int
): Dp {
    val normalized = (seed xor axisSeed) and Int.MAX_VALUE
    val range = if (heroColumn) heroRange else regularRange
    val offset = (normalized % (range * 2 + 1)) - range
    return offset.dp
}





