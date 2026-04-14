package com.emul8r.bizap.ui.gui3.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Base interface for composable Matrix background effects.
 *
 * Each effect is a plug-n-play module that:
 * - Renders to a Canvas DrawScope
 * - Fails gracefully (never crashes the pipeline)
 * - Reports performance metrics
 * - Respects configuration (Remote Config + device constraints)
 *
 * ## Example: Creating a New Effect
 *
 * ```kotlin
 * class YourEffect @Inject constructor() : MatrixEffect {
 *     override val effectName: String = "your_effect"
 *
 *     override suspend fun render(scope: DrawScope, config: MatrixBackgroundConfig) {
 *         try {
 *             // Your effect rendering logic
 *             scope.drawRect(color = Color.Green, size = ...)
 *         } catch (e: Exception) {
 *             // Silently fail; pipeline will skip this effect
 *             Timber.e(e, "YourEffect render failed")
 *         }
 *     }
 *
 *     override fun estimatedFrameTimeMicros(): Long = 2000  // 2ms
 *     override fun isAvailable(): Boolean = true
 * }
 * ```
 *
 * ## Maturity Checklist (Required for PR Merge)
 *
 * - [ ] Effect implements `MatrixEffect` interface
 * - [ ] Performance < 5ms per frame (measure on Pixel 6a)
 * - [ ] Unit tests pass (nominal + error cases)
 * - [ ] Compose UI test verifies visual output
 * - [ ] KDoc comments + Remote Config parameter docs
 * - [ ] Error handling (never crashes pipeline)
 * - [ ] Accessibility review (color-blind safe)
 *
 * @see MatrixBackgroundConfig for tunable parameters
 * @see MatrixEffectsPipeline for orchestration details
 */
sealed interface MatrixEffect {
    /**
     * Unique identifier for this effect.
     * Used for logging, telemetry, and pipeline ordering.
     * Example: "rain_particles", "glitch", "scanlines"
     */
    val effectName: String

    /**
     * Render this effect on the given Canvas DrawScope.
     *
     * **Important:** If rendering fails, implementations MUST:
     * 1. Catch the exception
     * 2. Log via Timber.e()
     * 3. Return gracefully (never throw)
     *
     * The pipeline will skip this effect and continue with others.
     *
     * @param scope Canvas DrawScope for rendering
     * @param config All tunable parameters (density, intensity, colors, etc.)
     */
    suspend fun render(scope: DrawScope, config: MatrixBackgroundConfig)

    /**
     * Estimate this effect's frame time impact in microseconds.
     *
     * Used by the pipeline for:
     * - Rendering order optimization (cheapest effects first)
     * - Adaptive performance decisions (skip expensive effects on jank)
     *
     * @return Frame time in microseconds (e.g., 2000 = 2ms)
     */
    fun estimatedFrameTimeMicros(): Long

    /**
     * Check if this effect is available on the current device.
     *
     * Return false if:
     * - Required device capability unavailable (e.g., Canvas on API < 21)
     * - Insufficient memory
     * - Device-specific constraint not met
     *
     * @return true if effect can render safely; false otherwise
     */
    fun isAvailable(): Boolean
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
    // Future: HALLOWEEN, HOLIDAYS, GREEK, EMOJI, CUSTOM_BRAND
}

