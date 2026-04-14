package com.emul8r.bizap.ui.gui3.components.effects

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import timber.log.Timber

/**
 * MatrixEffect: Sealed interface for GPU-accelerated background effects
 *
 * All effects must:
 * 1. Implement render() and catch their own exceptions
 * 2. Estimate frame time (< 5ms budget)
 * 3. Report availability (device capability check)
 */
sealed interface MatrixEffect {
    val effectName: String

    /**
     * Render the effect on the provided DrawScope
     * MUST catch and log exceptions internally
     */
    suspend fun render(scope: DrawScope, config: MatrixBackgroundConfig)

    /**
     * Estimated frame time in microseconds
     * Must be < 5000 μs (5ms) to stay within 60 FPS budget
     */
    fun estimatedFrameTimeMicros(): Int

    /**
     * Check if effect is available on this device
     * Consider: API level, GPU support, RAM, etc.
     */
    fun isAvailable(): Boolean {
        return true  // Default: available
    }
}

/**
 * Central configuration object for all Matrix background parameters.
 *
 * **Source of truth:** Remote Config (with local overrides for testing)
 *
 * All effects read from a single config object → easy to tune, profile, A/B test.
 * No coupling between effects and external state.
 *
 * @property canvasEnabled Whether Canvas rendering is enabled (vs. text fallback)
 * @property rainDensity Rain particle count multiplier (0.3–1.5)
 * @property rainSpeed Animation speed multiplier (0.5–2.0)
 * @property glitchIntensity Glitch effect intensity (0.0–1.0)
 * @property scanlineHeight Vertical spacing of CRT scanlines (1.5–4.0 dp)
 * @property scanlineAlpha Opacity of scanlines (0.0–0.2)
 * @property charsetProvider Source for character glyphs (Katakana, alphanumeric, etc.)
 * @property enableAdaptivePerf Auto-reduce density on jank detection
 * @property debugLogging Verbose logging for development/debugging
 */
data class MatrixBackgroundConfig(
    val canvasEnabled: Boolean = true,
    val rainDensity: Float = 0.8f,
    val rainSpeed: Float = 1.0f,
    val glitchIntensity: Float = 0.5f,
    val scanlineHeight: Dp = 2.0.dp,
    val scanlineAlpha: Float = 0.05f,
    val charsetProvider: MatrixCharsetProvider = MatrixCharsetProvider.ALPHANUMERIC,
    val enableAdaptivePerf: Boolean = false,
    val debugLogging: Boolean = false
)

/**
 * Charset provider for Matrix glyph rendering.
 *
 * Allows switching between different character sets without code changes.
 * Extendable for holidays, branding, accessibility, etc.
 */
enum class MatrixCharsetProvider(val characters: List<String>) {
    KATAKANA(
        listOf(
            "█", "▓", "▒", "░",
            "ゲ", "ロ", "ス", "モ", "ナ",
            "ニ", "ハ", "ミ", "ヨ", "リ",
            "ル", "ヲ", "ン"
        )
    ),
    ALPHANUMERIC(
        listOf(
            "█", "▓", "▒", "░",
            "0", "1", "A", "Z",
            "►", "◄", "↑", "↓",
            "@", "#", "$", "%"
        )
    ),
    BINARY(listOf("0", "1", "█", "░")),
    CYBER(
        listOf(
            "◊", "▢", "▣", "▤",
            "[", "]", "{", "}",
            "<", ">", "/", "\\",
            "●", "○", "⬤", "⬯"
        )
    )
}

