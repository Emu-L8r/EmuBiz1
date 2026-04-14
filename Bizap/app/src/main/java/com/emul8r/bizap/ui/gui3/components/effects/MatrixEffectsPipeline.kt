package com.emul8r.bizap.ui.gui3.components.effects

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.emul8r.bizap.ui.gui3.util.MatrixBackgroundConfig
import com.emul8r.bizap.ui.gui3.util.PerformanceProfiler
import com.emul8r.bizap.utils.FirebaseEventTracker
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the rendering pipeline for all Matrix effects.
 *
 * ## Responsibilities
 *
 * 1. **Effect Ordering:** Render effects in optimal order (background → foreground)
 * 2. **Error Isolation:** Catch exceptions per-effect (fail-safe pipeline)
 * 3. **Performance Monitoring:** Measure total frame time, detect jank
 * 4. **Telemetry:** Log errors, jank events, adaptation decisions
 * 5. **Adaptive Rendering:** Skip expensive effects on jank (future)
 *
 * ## Pipeline Diagram
 *
 * ```
 * renderFrame()
 *   ├─ effect1.render()  → On error: log, skip
 *   ├─ effect2.render()  → On error: log, skip
 *   └─ effect3.render()  → On error: log, skip
 *   └─ Measure total frame time
 *   └─ Report to profiler + analytics
 * ```
 *
 * ## Usage
 *
 * ```kotlin
 * @Inject lateinit var pipeline: MatrixEffectsPipeline
 *
 * Canvas(Modifier.fillMaxSize()) { scope ->
 *     val config = MatrixBackgroundConfig(...)
 *     pipeline.renderFrame(
 *         scope = scope,
 *         config = config,
 *         enableGlitch = true
 *     )
 * }
 * ```
 *
 * @see EffectRegistry for effect discovery
 * @see PerformanceProfiler for metrics
 * @see FirebaseEventTracker for analytics
 */
@Singleton
class MatrixEffectsPipeline @Inject constructor(
    private val effectRegistry: EffectRegistry,
    private val eventTracker: FirebaseEventTracker?,
    private val profiler: PerformanceProfiler
) {

    /**
     * Render all active effects for current frame.
     *
     * Each effect is rendered independently; if one fails, others continue.
     *
     * ## Frame Timing
     *
     * - Records total frame time via PerformanceProfiler
     * - Detects jank (> 16.67ms = 60 FPS drop)
     * - Logs jank events to Crashlytics
     *
     * @param scope Canvas DrawScope for rendering
     * @param config Configuration (densities, intensities, etc.)
     * @param enableGlitch Whether to render glitch effect
     * @param enableScanlines Whether to render scanline effect
     */
    suspend fun renderFrame(
        scope: DrawScope,
        config: MatrixBackgroundConfig,
        enableGlitch: Boolean = true,
        enableScanlines: Boolean = true
    ) {
        val startTimeNano = System.nanoTime()
        var effectsRendered = 0
        var effectsSkipped = 0

        // Get active effects in render order
        val effects = effectRegistry.getActiveEffects(
            enableGlitch = enableGlitch,
            enableScanlines = enableScanlines
        )

        if (config.debugLogging) {
            Timber.d("Rendering ${effects.size} effects")
        }

        // Render each effect, catching exceptions per effect
        coroutineScope {
            for (effect in effects) {
                try {
                    effect.render(scope, config)
                    effectsRendered++

                    if (config.debugLogging) {
                        Timber.d("Effect rendered: ${effect.effectName} (est. ${effect.estimatedFrameTimeMicros() / 1000}ms)")
                    }
                } catch (e: Exception) {
                    effectsSkipped++
                    Timber.e(e, "Effect ${effect.effectName} failed; skipping")
                    eventTracker?.trackMatrixEffectError(effect.effectName, e)
                }
            }
        }

        // Measure total frame time
        val frameTimeNano = System.nanoTime() - startTimeNano
        val frameTimeMs = frameTimeNano / 1_000_000.0

        // Record for profiling
        profiler.recordFrame(frameTimeMs)

        if (config.debugLogging) {
            Timber.d(
                "Frame complete: ${frameTimeMs.format(2)}ms " +
                        "(${effectsRendered}/${ effects.size} effects, $effectsSkipped skipped)"
            )
        }

        // Detect jank
        if (frameTimeMs > 16.67) {
            Timber.w("🔴 Frame jank: ${frameTimeMs.format(2)}ms (target: < 16.67ms)")
            eventTracker?.trackMatrixFrameJank(frameTimeMs)
        }
    }

    /**
     * Get current profiling snapshot.
     *
     * @return PerformanceMetrics with frame time stats
     */
    fun getMetrics() = profiler.snapshot()

    /**
     * Reset profiler (for session boundaries or testing).
     */
    fun resetProfiler() {
        profiler.reset()
        Timber.d("Matrix pipeline profiler reset")
    }
}

/**
 * Extension function for clean double formatting.
 */
internal fun Double.format(digits: Int): String = "%.${digits}f".format(this)

