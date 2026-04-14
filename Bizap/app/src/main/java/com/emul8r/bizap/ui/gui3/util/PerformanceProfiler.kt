package com.emul8r.bizap.ui.gui3.util

import android.os.Build
import com.emul8r.bizap.utils.FirebaseEventTracker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Measures and profiles Matrix background rendering performance.
 *
 * Responsibilities:
 * - Track frame times (rolling window: last 60 frames)
 * - Detect jank (frame time > 16.67ms = 60 FPS threshold)
 * - Report metrics to Crashlytics for production observability
 * - Provide data for adaptive performance decisions
 *
 * ## Key Metrics
 *
 * - **avgFrameTime:** Average frame time (ms) over last 60 frames
 * - **maxFrameTime:** Peak frame time in rolling window
 * - **jankCount:** Number of frames > 16.67ms
 * - **jankRate:** Percentage of jank frames (target: < 5%)
 *
 * ## Usage
 *
 * ```kotlin
 * @Inject lateinit var profiler: PerformanceProfiler
 *
 * Canvas(Modifier.fillMaxSize()) { scope ->
 *     val startNano = System.nanoTime()
 *
 *     // Render effects...
 *     pipeline.renderFrame(scope, config)
 *
 *     val frameTimeMs = (System.nanoTime() - startNano) / 1_000_000.0
 *     profiler.recordFrame(frameTimeMs)
 * }
 * ```
 *
 * @see AdaptivePerformanceManager for adaptive behavior based on profiling data
 * @see FirebaseEventTracker for telemetry integration
 */
@Singleton
class PerformanceProfiler @Inject constructor(
    private val crashlytics: FirebaseCrashlytics?,
    private val eventTracker: FirebaseEventTracker?
) {
    private val frameTimestamps = mutableListOf<Double>()
    private val MAX_WINDOW_SIZE = 120  // Track last 120 frames (2 seconds at 60 FPS)

    private var totalFramesRecorded = 0L
    private var totalJankFrames = 0L

    companion object {
        private const val FRAME_TIME_THRESHOLD_MS = 16.67  // 60 FPS
        private const val CRITICAL_JANK_MS = 33.33  // 30 FPS (double frame drop)
    }

    /**
     * Record a frame's render time.
     *
     * Should be called once per frame after rendering completes.
     *
     * @param frameTimeMs Frame render time in milliseconds
     */
    fun recordFrame(frameTimeMs: Double) {
        // Add to rolling window
        frameTimestamps.add(frameTimeMs)
        totalFramesRecorded++

        // Keep window size bounded
        if (frameTimestamps.size > MAX_WINDOW_SIZE) {
            frameTimestamps.removeAt(0)
        }

        // Track jank
        if (frameTimeMs > FRAME_TIME_THRESHOLD_MS) {
            totalJankFrames++

            // Log critical jank (major frame drop)
            if (frameTimeMs > CRITICAL_JANK_MS) {
                Timber.w("🔴 CRITICAL JANK: ${frameTimeMs.format(2)}ms (30 FPS drop)")
                eventTracker?.trackMatrixFrameJank(frameTimeMs)
            }
        }

        // Periodic reporting (every 60 frames = ~1 second at 60 FPS)
        if (frameTimestamps.size % 60 == 0) {
            reportMetrics()
        }
    }

    /**
     * Get average frame time (ms) for last N frames.
     *
     * @return Average frame time, or 0.0 if no frames recorded
     */
    fun avgFrameTime(): Double {
        return if (frameTimestamps.isNotEmpty()) {
            frameTimestamps.average()
        } else {
            0.0
        }
    }

    /**
     * Get maximum frame time (ms) in rolling window.
     *
     * @return Peak frame time, or 0.0 if no frames recorded
     */
    fun maxFrameTime(): Double {
        return frameTimestamps.maxOrNull() ?: 0.0
    }

    /**
     * Get jank frame count (frames > 16.67ms) in rolling window.
     *
     * @return Number of jank frames
     */
    fun jankCount(): Int {
        return frameTimestamps.count { it > FRAME_TIME_THRESHOLD_MS }
    }

    /**
     * Get jank rate (percentage of frames > 16.67ms).
     *
     * @return Jank rate as percentage (0–100)
     */
    fun jankRate(): Double {
        return if (frameTimestamps.isNotEmpty()) {
            (jankCount().toDouble() / frameTimestamps.size) * 100
        } else {
            0.0
        }
    }

    /**
     * Check if current performance is satisfactory (< 5% jank).
     *
     * @return true if jank rate <= 5%; false otherwise
     */
    fun isSatisfactory(): Boolean {
        return jankRate() <= 5.0
    }

    /**
     * Report current metrics to Crashlytics and Firebase Analytics.
     *
     * Called periodically (every 60 frames) and on-demand.
     */
    private fun reportMetrics() {
        val metrics = PerformanceMetrics(
            avgFrameTimeMs = avgFrameTime(),
            maxFrameTimeMs = maxFrameTime(),
            jankCount = jankCount(),
            jankRate = jankRate(),
            deviceModel = Build.MODEL,
            totalFramesRecorded = totalFramesRecorded
        )

        // Log to Crashlytics for dashboard visibility
        crashlytics?.recordException(PerformanceMetricsException(metrics))

        // Log to Firebase Analytics
        eventTracker?.trackFrameTimeMetric(
            avgFrameTimeMs = metrics.avgFrameTimeMs,
            device = metrics.deviceModel,
            dropFrameCount = metrics.jankCount
        )

        if (!isSatisfactory()) {
            Timber.w("⚠️ Performance degradation: ${metrics.jankRate.format(1)}% jank on ${metrics.deviceModel}")
        }
    }

    /**
     * Reset profiler state (useful for session boundaries or testing).
     */
    fun reset() {
        frameTimestamps.clear()
        totalFramesRecorded = 0L
        totalJankFrames = 0L
    }

    /**
     * Get comprehensive performance snapshot.
     *
     * @return PerformanceMetrics object with all current stats
     */
    fun snapshot(): PerformanceMetrics {
        return PerformanceMetrics(
            avgFrameTimeMs = avgFrameTime(),
            maxFrameTimeMs = maxFrameTime(),
            jankCount = jankCount(),
            jankRate = jankRate(),
            deviceModel = Build.MODEL,
            totalFramesRecorded = totalFramesRecorded
        )
    }
}

/**
 * Immutable snapshot of performance metrics at a point in time.
 */
data class PerformanceMetrics(
    val avgFrameTimeMs: Double,
    val maxFrameTimeMs: Double,
    val jankCount: Int,
    val jankRate: Double,
    val deviceModel: String,
    val totalFramesRecorded: Long
) {
    override fun toString(): String = """
        ╔════ MATRIX PERFORMANCE METRICS ════╗
        ║ Device: $deviceModel
        ║ Avg Frame Time: ${avgFrameTimeMs.format(2)}ms
        ║ Max Frame Time: ${maxFrameTimeMs.format(2)}ms
        ║ Jank Frames: $jankCount
        ║ Jank Rate: ${jankRate.format(2)}%
        ║ Total Frames: $totalFramesRecorded
        ╚════════════════════════════════════╝
    """.trimIndent()
}

/**
 * Exception wrapper for Crashlytics metric reporting.
 *
 * This allows performance metrics to appear in Crashlytics dashboard
 * as "non-fatal" events that can be filtered and analyzed.
 */
class PerformanceMetricsException(val metrics: PerformanceMetrics) :
    Exception("Matrix Performance: ${metrics.jankRate.format(1)}% jank on ${metrics.deviceModel}")

/**
 * Extension function for clean float formatting.
 */
internal fun Double.format(digits: Int): String = "%.${digits}f".format(this)

