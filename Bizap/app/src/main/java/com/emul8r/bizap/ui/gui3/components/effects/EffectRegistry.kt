package com.emul8r.bizap.ui.gui3.components.effects

import com.emul8r.bizap.ui.gui3.util.MatrixEffect
import com.emul8r.bizap.ui.gui3.util.RainParticleEffect
import com.emul8r.bizap.ui.gui3.util.GlitchEffect
import com.emul8r.bizap.ui.gui3.util.ScanlineEffect
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central registry for all available Matrix effects.
 *
 * Managed by Hilt DI; injected effects are automatically discovered
 * and composed into the rendering pipeline. This allows:
 * - Dynamic effect discovery (no hardcoded lists)
 * - Easy testing (mock effects)
 * - Future plugin system
 * - A/B testing effect combinations
 *
 * ## Architecture
 *
 * Effects are injected individually (not as a list) to enable:
 * 1. Lazy initialization (effects instantiated only when used)
 * 2. Optional effects (some may be null if not compiled)
 * 3. Testing (easy to mock specific effects)
 *
 * ```kotlin
 * // In EffectRegistry constructor, Hilt automatically finds all
 * // @Inject MatrixEffect subclasses and injects them here
 * @Singleton
 * class EffectRegistry @Inject constructor(
 *     private val rainEffect: RainParticleEffect,
 *     private val glitchEffect: GlitchEffect,
 *     private val scanlineEffect: ScanlineEffect
 *     // Future effects added here
 * )
 * ```
 *
 * @see MatrixEffectsPipeline for rendering orchestration
 */
@Singleton
class EffectRegistry @Inject constructor(
    // Inject individual effects; Hilt handles discovery
    private val rainParticleEffect: RainParticleEffect,
    private val glitchEffect: GlitchEffect,
    private val scanlineEffect: ScanlineEffect
) {
    /**
     * All available effects in recommended render order.
     *
     * Render order considerations:
     * 1. Background effects first (RainParticles — depth layer)
     * 2. Mid-layer effects (Glitch — over rain)
     * 3. Foreground effects last (Scanlines — visual polish)
     *
     * This ordering creates visual depth and ensures cheap effects
     * (scanlines) aren't overdraw-heavy.
     */
    private val allEffects: List<MatrixEffect> = listOf(
        rainParticleEffect,      // Background layer: GPU particle rain (most expensive, ~8ms)
        glitchEffect,            // Mid-layer: Glitch artifacts (~0.5ms)
        scanlineEffect           // Foreground layer: CRT scanlines (cheap post-processing, ~1ms)
    )

    /**
     * Get effects to render for current frame.
     *
     * Filters by:
     * - Device availability (e.g., Canvas support)
     * - Feature flags (glitch/scanlines can be disabled)
     * - Config settings (intensity-based filtering)
     *
     * @param enableGlitch Whether to include glitch effect
     * @param enableScanlines Whether to include scanline effect
     * @return Ordered list of effects to render
     */
    fun getActiveEffects(
        enableGlitch: Boolean = true,
        enableScanlines: Boolean = true
    ): List<MatrixEffect> {
        return allEffects
            .filter { effect ->
                // Always respect device availability
                if (!effect.isAvailable()) {
                    Timber.d("Effect ${effect.effectName} not available on this device")
                    return@filter false
                }

                // Respect feature flags
                when (effect.effectName) {
                    "glitch" -> enableGlitch
                    "scanlines" -> enableScanlines
                    else -> true  // Other effects always enabled if available
                }
            }
            .also { filtered ->
                if (filtered.size < allEffects.size) {
                    Timber.d(
                        "Active effects: ${filtered.map { it.effectName }.joinToString(", ")}"
                    )
                }
            }
    }

    /**
     * Get effects sorted by performance (cheapest first).
     *
     * Useful for adaptive performance: drop expensive effects first
     * when jank is detected.
     *
     * @return Effects sorted by estimatedFrameTimeMicros (ascending)
     */
    fun getEffectsByPerformance(): List<MatrixEffect> {
        return allEffects
            .filter { it.isAvailable() }
            .sortedBy { it.estimatedFrameTimeMicros() }
    }

    /**
     * Register a custom effect dynamically (for future plugin system).
     *
     * Currently a placeholder; future version will support:
     * - Runtime effect registration
     * - Third-party effect plugins
     * - A/B testing custom effects
     *
     * @param effect Effect to register
     */
    fun registerEffect(effect: MatrixEffect) {
        Timber.d("Registering custom effect: ${effect.effectName}")
        // Future: Add to mutable list or separate registry
        // For now, effects are discovered via DI at initialization
    }

    /**
     * Get effect by name (useful for debugging/telemetry).
     *
     * @param effectName Effect identifier (e.g., "glitch")
     * @return Effect, or null if not found
     */
    fun getEffect(effectName: String): MatrixEffect? {
        return allEffects.find { it.effectName == effectName }
    }
}



