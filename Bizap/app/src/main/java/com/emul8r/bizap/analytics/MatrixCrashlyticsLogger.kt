package com.emul8r.bizap.analytics

import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crashlytics event logger for Matrix Canvas effects.
 * Tracks errors, performance metrics, and user interactions.
 */
@Singleton
class MatrixCrashlyticsLogger @Inject constructor() {

    /**
     * Log effect-specific errors.
     */
    fun logEffectError(effectName: String, error: Throwable) {
        Firebase.crashlytics.apply {
            recordException(error)
            log("Matrix effect error: $effectName - ${error.message}")
            setCustomKey("effect_name", effectName)
            setCustomKey("error_type", error::class.simpleName ?: "Unknown")
            setCustomKey("device_model", Build.MODEL)
            setCustomKey("device_api", Build.VERSION.SDK_INT)
        }
        Timber.e(error, "Matrix effect $effectName failed")
    }

    /**
     * Log frame jank detection.
     */
    fun logFrameJank(frameTimeMs: Float, deviceModel: String) {
        Firebase.crashlytics.apply {
            log("Frame jank detected: ${frameTimeMs}ms on $deviceModel")
            setCustomKey("frame_time_ms", frameTimeMs)
            setCustomKey("device_model", deviceModel)
            setCustomKey("device_api", Build.VERSION.SDK_INT)
        }
        Timber.w("Frame jank: ${frameTimeMs}ms on $deviceModel")
    }

    /**
     * Log when Matrix canvas background is toggled.
     */
    fun logMatrixBackgroundToggled(enabled: Boolean, deviceModel: String, memoryMb: Int) {
        Firebase.crashlytics.apply {
            log("Matrix background toggled: enabled=$enabled, device=$deviceModel, memory=${memoryMb}MB")
            setCustomKey("canvas_enabled", enabled)
            setCustomKey("device_model", deviceModel)
            setCustomKey("memory_mb", memoryMb)
            setCustomKey("device_api", Build.VERSION.SDK_INT)
        }
        Timber.i("Matrix background: enabled=$enabled, memory=${memoryMb}MB")
    }

    /**
     * Log adaptive performance trigger event.
     */
    fun logAdaptationTriggered(oldDensity: Float, newDensity: Float, device: String, reason: String) {
        Firebase.crashlytics.apply {
            log("Adaptive performance triggered: $oldDensity → $newDensity on $device ($reason)")
            setCustomKey("old_density", oldDensity)
            setCustomKey("new_density", newDensity)
            setCustomKey("device_model", device)
            setCustomKey("reason", reason)
            setCustomKey("device_api", Build.VERSION.SDK_INT)
        }
        Timber.i("Adaptation: $oldDensity → $newDensity ($reason)")
    }

    /**
     * Log effect configuration change.
     */
    fun logEffectConfigChanged(effectName: String, key: String, oldValue: Float, newValue: Float) {
        Firebase.crashlytics.apply {
            log("Effect config changed: $effectName.$key: $oldValue → $newValue")
            setCustomKey("effect_name", effectName)
            setCustomKey("config_key", key)
            setCustomKey("old_value", oldValue)
            setCustomKey("new_value", newValue)
        }
        Timber.d("Config changed: $effectName.$key: $oldValue → $newValue")
    }

    /**
     * Log feature flag status at startup.
     */
    fun logFeatureFlagStatus(
        canvasEnabled: Boolean,
        rainEnabled: Boolean,
        glitchEnabled: Boolean,
        scanlinesEnabled: Boolean,
        adaptivePerfEnabled: Boolean
    ) {
        Firebase.crashlytics.apply {
            log("Feature flags: canvas=$canvasEnabled, rain=$rainEnabled, glitch=$glitchEnabled, scanlines=$scanlinesEnabled, adaptive=$adaptivePerfEnabled")
            setCustomKey("feature_canvas", canvasEnabled)
            setCustomKey("feature_rain", rainEnabled)
            setCustomKey("feature_glitch", glitchEnabled)
            setCustomKey("feature_scanlines", scanlinesEnabled)
            setCustomKey("feature_adaptive", adaptivePerfEnabled)
        }
        Timber.i("Feature flags initialized: canvas=$canvasEnabled")
    }

    /**
     * Log performance metrics snapshot.
     */
    fun logPerformanceMetrics(
        avgFrameTimeMs: Float,
        maxFrameTimeMs: Float,
        jankRatePercent: Float,
        deviceModel: String
    ) {
        Firebase.crashlytics.apply {
            log("Perf metrics: avg=${avgFrameTimeMs}ms, max=${maxFrameTimeMs}ms, jank=${jankRatePercent}%")
            setCustomKey("avg_frame_time_ms", avgFrameTimeMs)
            setCustomKey("max_frame_time_ms", maxFrameTimeMs)
            setCustomKey("jank_rate_percent", jankRatePercent)
            setCustomKey("device_model", deviceModel)
        }
        Timber.d("Perf: avg=${avgFrameTimeMs}ms, jank=${jankRatePercent}%")
    }

    /**
     * Log rollout event (when effect becomes visible to user).
     */
    fun logRolloutEvent(percentage: Int, userInRollout: Boolean, deviceId: String) {
        Firebase.crashlytics.apply {
            log("Rollout: $percentage% enabled, user_in_rollout=$userInRollout")
            setCustomKey("rollout_percentage", percentage)
            setCustomKey("user_in_rollout", userInRollout)
            setCustomKey("device_hash", deviceId.hashCode())
        }
        Timber.i("Rollout: $percentage%, user included: $userInRollout")
    }

    /**
     * Log A/B test assignment.
     */
    fun logABTestAssignment(testName: String, variant: String, config: String) {
        Firebase.crashlytics.apply {
            log("A/B test: $testName assigned to $variant")
            setCustomKey("ab_test_name", testName)
            setCustomKey("ab_variant", variant)
            setCustomKey("ab_config", config)
        }
        Timber.i("A/B test $testName: variant=$variant")
    }

    /**
     * Log memory usage warning.
     */
    fun logMemoryWarning(currentMb: Int, thresholdMb: Int) {
        Firebase.crashlytics.apply {
            log("Memory warning: $currentMb MB (threshold: $thresholdMb MB)")
            setCustomKey("memory_current_mb", currentMb)
            setCustomKey("memory_threshold_mb", thresholdMb)
        }
        Timber.w("Memory warning: $currentMb MB")
    }
}

