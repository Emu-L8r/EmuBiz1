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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import timber.log.Timber

// Vignette gradient — darkens screen edges, frames content cinematically
private val MATRIX_VIGNETTE_GRADIENT = Brush.radialGradient(
    colorStops = arrayOf(
        0.00f to Color.Transparent,
        0.52f to Color.Transparent,
        0.78f to Color(0xFF000000).copy(alpha = 0.22f),
        1.00f to Color(0xFF000000).copy(alpha = 0.64f)
    )
)

// Head chars mutate 4× faster than trail for a "flickering tip" effect
private const val MATRIX_RAIN_HEAD_MUTATION_MULTIPLIER = 4

/**
 * MATRIX BACKGROUND - Multi-Layer Falling Character Animation
 *
 * Enhanced with:
 * - Responsive BoxWithConstraints for all screen sizes
 * - Background layer: slow, subtle (depth)
 * - Midground layer: medium speed (immersion)
 * - Foreground layer: fast, bright (presence)
 * - Hero columns with glow, glitch, and ghost-flicker effects
 * - Cubic tail falloff for sharp "comet tail" look
 */
private const val MATRIX_RAIN_CANVAS_HEIGHT_DP = 1200f
private const val MATRIX_RAIN_VIEWPORT_HEIGHT_MULTIPLIER = 1.08f
private const val MATRIX_RAIN_MUTATION_BUCKETS = 8
private const val MATRIX_RAIN_BASE_TRAIL_SPACING_MULTIPLIER = 1.85f  // dense comet tail (was 3.45f)
private const val MATRIX_RAIN_HEAD_GLOW_ALPHA = 0.22f
private const val MATRIX_RAIN_GAP_CHANCE_BASE = 0.035f
private const val MATRIX_RAIN_MIN_COLUMN_SPACING_DP = 24f
private const val MATRIX_RAIN_MAX_COLUMN_SPACING_DP = 52f
private const val MATRIX_RAIN_HEAD_GLOW_OFFSET_DP = 0.75f

private val MATRIX_BINARY_GLYPHS = listOf("0", "1")
private val MATRIX_SYMBOL_GLYPHS = listOf("*", "+", "-", "/", "\\", "!", "~", "^")
private val MATRIX_KATAKANA_GLYPHS = listOf(
    "ニ", "ハ", "ミ", "ヲ", "ネ", "ホ", "ヘ", "レ", "カ", "キ",
    "ク", "サ", "ス", "シ", "ツ", "テ", "ト", "ナ", "ヌ", "ノ",
    "マ", "ム", "モ", "ヤ", "ユ", "ラ", "リ", "ル", "ロ", "ワ",
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
)

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
    Timber.d("MatrixBackground: Rendering animations (enhanced rain engine)")
    println("MatrixBackground: RENDERING - ENHANCED MULTI-LAYER")

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
            // Layer 1: far backdrop — blurred for depth-of-field (background recedes visually)
            Box(modifier = Modifier.fillMaxSize().blur(1.5.dp)) {
                repeat(farColumns) { idx ->
                    val xPos = farSpacingValue * idx.toFloat()
                    FallingCharacterColumnLayered(
                        columnIndex = idx,
                        xPosition = xPos.dp,
                        speed = 3200 + (idx % 4) * 140 + ((idx * 37) % 180),
                        alpha = 0.12f,
                        fontSize = 11.sp,
                        densityScale = densityScale,
                        enableGlitch = enableGlitch,
                        canvasHeightDp = viewportCanvasHeightDp,
                        normalizedXPos = (xPos / viewportWidthValue).coerceIn(0f, 1f)
                    )
                }
            }

            // Layer 2: midground — readable motion, denser katakana stream
            repeat(midColumns) { idx ->
                val xPos = midSpacingValue * idx.toFloat()
                FallingCharacterColumnLayered(
                    columnIndex = idx + 100,
                    xPosition = xPos.dp,
                    speed = 5200 + (idx % 5) * 150 + ((idx * 29) % 160),
                    alpha = 0.30f,
                    fontSize = 12.sp,
                    densityScale = densityScale,
                    enableGlitch = enableGlitch,
                    canvasHeightDp = viewportCanvasHeightDp,
                    normalizedXPos = (xPos / viewportWidthValue).coerceIn(0f, 1f)
                )
            }

            // Layer 3: foreground — slower, brighter, most Matrix-like
            repeat(nearColumns) { idx ->
                val xPos = nearSpacingValue * idx.toFloat()
                FallingCharacterColumnLayered(
                    columnIndex = idx + 200,
                    xPosition = xPos.dp,
                    speed = 7600 + (idx % 6) * 160 + ((idx * 41) % 220),
                    alpha = 0.55f,
                    fontSize = 13.sp,
                    densityScale = densityScale,
                    enableGlitch = enableGlitch,
                    canvasHeightDp = viewportCanvasHeightDp,
                    normalizedXPos = (xPos / viewportWidthValue).coerceIn(0f, 1f)
                )
            }

            content()

            // Cinematic edge vignette — zero animated state, one draw call, frames the UI
            Box(modifier = Modifier.fillMaxSize().background(MATRIX_VIGNETTE_GRADIENT))
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
    canvasHeightDp: Dp = MATRIX_RAIN_CANVAS_HEIGHT_DP.dp,
    normalizedXPos: Float = 0.5f  // 0=left edge, 1=right edge — used for edge brightness boost
) {
    val columnState = remember(columnIndex, densityScale) {
        val seedRandom = Random(columnIndex.toLong() * 31L + (densityScale * 1000).toLong())
        val layerIndex = columnIndex / 100
        val trailBase = when {
            layerIndex >= 2 -> 18  // foreground: long dramatic tail (comet effect)
            layerIndex >= 1 -> 14  // midground: medium tail
            else -> 10             // background: shorter, adds depth without clutter
        }
        val densityBoost = when {
            densityScale > 1.18f -> 3
            densityScale > 1.02f -> 2
            else -> 1
        }
        val trailJitter = seedRandom.nextInt(0, 5)  // wider variation for organic feel
        val trailLength = (trailBase + densityBoost + trailJitter).coerceAtLeast(8)
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
                animation = tween(durationMillis = speed, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress_$columnIndex"
        )

    val glitchFrame = (animatedProgress.value * 100).toInt()
    val glitchSeed = columnState.glyphSeed xor (densityScale * 1000).toInt()
    val glitchPhase = (glitchSeed and Int.MAX_VALUE) % 100

    val shouldGlitch = enableGlitch && columnState.heroColumn &&
        (glitchPhase < 15) && (glitchFrame % 18) < 6   // glitch visible ~15% of hero frames
    val shouldGlitchBinary = enableGlitch && columnState.heroColumn &&
        glitchPhase < 10 && (glitchFrame % 14) < 5     // binary burst more frequent
    val shouldGhostFlicker = enableGlitch && columnState.heroColumn &&
        glitchPhase < 30 && (glitchFrame % 22) < 7     // ghost: visible ~30% of frames

    if (columnIndex == 0) {
        LaunchedEffect(animatedProgress.value) {
            MatrixCascadeState.updateCascadePosition(animatedProgress.value * MATRIX_RAIN_CANVAS_HEIGHT_DP)
            MatrixCascadeState.updateCascadeVisibility(animatedProgress.value)
        }
    }

    val headY = animatedProgress.value * MATRIX_RAIN_CANVAS_HEIGHT_DP
    val xWave = sin(((animatedProgress.value * 6.2831855f) + columnState.phaseOffset * 4f).toDouble()).toFloat() * columnState.driftDp.value
    val trailSpacing = fontSize.value * MATRIX_RAIN_BASE_TRAIL_SPACING_MULTIPLIER
    val cycleBucket = (animatedProgress.value * MATRIX_RAIN_MUTATION_BUCKETS).toInt()
    // Head chars mutate faster — creates the classic "flickering green tip" effect
    val headCycleBucket = (animatedProgress.value * MATRIX_RAIN_MUTATION_BUCKETS * MATRIX_RAIN_HEAD_MUTATION_MULTIPLIER).toInt()
    val viewportHeightLimit = canvasHeightDp.value + 150f

    repeat(columnState.trailLength) { charIdx ->
        val trailPosition = if (columnState.trailLength <= 1) 0f else charIdx.toFloat() / (columnState.trailLength - 1).toFloat()
        val yOffsetPx = headY - (charIdx * trailSpacing) - 100f

        if (yOffsetPx < -150f || yOffsetPx > viewportHeightLimit) return@repeat

        val isHead = charIdx == 0
        val isNearHead = charIdx <= 2  // top 2 trail chars get bright green (white→green gradient)

        val displayChar = resolveMatrixGlyph(
            layerIndex = columnIndex / 100,
            columnIndex = columnIndex,
            segmentIndex = charIdx,
            cycleBucket = if (isHead) headCycleBucket else cycleBucket,
            heroColumn = columnState.heroColumn,
            trailPosition = trailPosition,
            shouldGlitch = shouldGlitch,
            shouldGlitchBinary = shouldGlitchBinary
        )
        if (displayChar.isBlank()) return@repeat

        val trailAlpha = matrixTrailAlpha(alpha, charIdx, columnState.trailLength, isHead)
        val finalColor = when {
            isHead -> Color.White                // White leading character — always full
            isNearHead -> MatrixGreenBright      // Bright green gradient near head
            shouldGlitchBinary -> MatrixGreenBright
            else -> MatrixGreen
        }

        val glitchOffsetX = if (shouldGlitch) matrixDeterministicOffset(
            seed = matrixGlyphHash(columnIndex / 100, columnIndex, charIdx, cycleBucket, columnState.heroColumn),
            axisSeed = 0x51, heroColumn = columnState.heroColumn, heroRange = 2, regularRange = 3
        ) else 0.dp
        val glitchOffsetY = if (shouldGlitch) matrixDeterministicOffset(
            seed = matrixGlyphHash(columnIndex / 100, columnIndex, charIdx * 2, cycleBucket, columnState.heroColumn),
            axisSeed = 0xA7, heroColumn = columnState.heroColumn, heroRange = 1, regularRange = 2
        ) else 0.dp

        val headGlowBoost = if (isHead) columnState.speedMultiplier + if (columnState.heroColumn) 0.12f else 0f else 1f

        // Dormancy: each column breathes in/out on its own sine phase — organic living field
        val dormancyFactor = (0.30f + 0.70f * kotlin.math.abs(
            sin((animatedProgress.value * 6.2831855f * 0.45f + columnState.phaseOffset * 9f).toDouble()).toFloat()
        )).coerceIn(0.15f, 1f)

        // Edge boost: outer 15% columns are brighter, draws eye toward center content
        val isEdge = normalizedXPos < 0.15f || normalizedXPos > 0.85f
        val edgeBoost = if (isEdge) 1.25f else 1f

        val finalAlpha = when {
            isHead -> (1.0f * dormancyFactor.coerceAtLeast(0.4f) * edgeBoost).coerceAtMost(1f)
            else -> (trailAlpha * headGlowBoost * dormancyFactor * edgeBoost).coerceIn(0.02f, 1f)
        }

        Text(
            text = displayChar,
            modifier = Modifier
                .offset(x = xPosition + xWave.dp + glitchOffsetX, y = yOffsetPx.dp + glitchOffsetY)
                .alpha(finalAlpha),
            color = finalColor,
            fontSize = if (isHead) fontSize * 1.05f else fontSize,
            fontFamily = FontFamily.Monospace
        )

        // Hero head glow (shadow text)
        if (isHead && !shouldGlitch && columnState.heroColumn) {
            Text(
                text = displayChar,
                modifier = Modifier
                    .offset(x = xPosition + xWave.dp + MATRIX_RAIN_HEAD_GLOW_OFFSET_DP.dp,
                            y = yOffsetPx.dp + MATRIX_RAIN_HEAD_GLOW_OFFSET_DP.dp)
                    .alpha(MATRIX_RAIN_HEAD_GLOW_ALPHA),
                color = MatrixGreenBright,
                fontSize = fontSize * 1.08f,
                fontFamily = FontFamily.Monospace
            )
        }

        // Ghost flicker on hero trail — visible displaced afterimage
        if (shouldGhostFlicker && charIdx > 0) {
            Text(
                text = displayChar,
                modifier = Modifier
                    .offset(x = xPosition + xWave.dp + 5.dp, y = yOffsetPx.dp - 3.dp)
                    .alpha((trailAlpha * 0.55f).coerceAtLeast(0.04f)),
                color = MatrixGreen,
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace
            )
        }
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
    if (shouldGlitch && (normalized % 100) < if (heroColumn) 14 else 10)
        return MATRIX_SYMBOL_GLYPHS[normalized % MATRIX_SYMBOL_GLYPHS.size]
    if (shouldGlitchBinary)
        return MATRIX_BINARY_GLYPHS[normalized and 1]
    val layerBias = when (layerIndex) { 0 -> 0; 1 -> 4; else -> 8 }
    val headBias = ((1f - trailPosition.coerceIn(0f, 1f)) * if (heroColumn) 24f else 18f).toInt()
    val roll = (normalized % 100) + headBias + layerBias + if (heroColumn) 6 else 0
    return when {
        // Katakana: reduced from 76/72/66 to give binary more room
        roll < if (heroColumn) 60 else if (layerIndex >= 2) 56 else 50 ->
            MATRIX_KATAKANA_GLYPHS[(normalized ushr 1) % MATRIX_KATAKANA_GLYPHS.size]
        // Symbols: unchanged relative share
        roll < if (layerIndex >= 2) 78 else 74 ->
            MATRIX_SYMBOL_GLYPHS[(normalized ushr 2) % MATRIX_SYMBOL_GLYPHS.size]
        // Binary: ~22-26% of non-head rolls — visibly present throughout stream
        else -> MATRIX_BINARY_GLYPHS[(normalized ushr 3) and 1]
    }
}

private fun matrixGlyphHash(
    layerIndex: Int, columnIndex: Int, segmentIndex: Int, cycleBucket: Int, heroColumn: Boolean
): Int {
    var hash = columnIndex * 31 + layerIndex * 17 + segmentIndex * 13 + cycleBucket * 19
    if (heroColumn) hash = hash xor 0x5A5A5A5A
    return hash
}

private fun matrixTrailAlpha(layerPresence: Float, segmentIndex: Int, trailLength: Int, isHead: Boolean): Float {
    if (isHead) return layerPresence
    val normalized = segmentIndex.toFloat() / maxOf(1, trailLength - 1).toFloat()
    val falloff = (1f - normalized).coerceIn(0f, 1f)
    val shapedCurve = falloff * falloff * falloff  // cubic = sharp comet tail
    // peakBrightness scales with layer presence: near≈0.88, mid≈0.48, far≈0.19
    val peakBrightness = (layerPresence * 1.6f).coerceIn(0.19f, 0.88f)
    return (shapedCurve * peakBrightness).coerceIn(0.005f, 0.92f)
}

private fun matrixDeterministicOffset(
    seed: Int, axisSeed: Int, heroColumn: Boolean, heroRange: Int, regularRange: Int
): Dp {
    val normalized = (seed xor axisSeed) and Int.MAX_VALUE
    val range = if (heroColumn) heroRange else regularRange
    return ((normalized % (range * 2 + 1)) - range).dp
}
