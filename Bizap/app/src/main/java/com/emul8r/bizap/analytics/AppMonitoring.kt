package com.emul8r.bizap.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production monitoring events for Bizap.
 *
 * Use this class to record meaningful signals for:
 * - Navigation errors (crash prevention)
 * - Feature flag changes (rollout tracking)
 * - Performance metrics (jank, slow frames)
 * - Security events (passphrase issues)
 * - Database health (query latency)
 *
 * All events are sent to both Firebase Analytics (trends) and
 * Crashlytics (breadcrumbs for crash context).
 *
 * Usage:
 *   appMonitoring.recordNavigationError(route = "InvoiceDetail", error = "Route not found")
 *   appMonitoring.recordFeatureFlagChanged("MATRIX_CANVAS_RENDERER", false, true)
 */
@Singleton
class AppMonitoring @Inject constructor(
    private val analytics: FirebaseAnalytics?,   // nullable — app works without Firebase
    private val crashlytics: FirebaseCrashlytics? // nullable — app works without Crashlytics
) {

    // ── Navigation ─────────────────────────────────────────────────────────

    /**
     * Record when a navigation route fails to resolve.
     * Alert threshold: >0 occurrences in 1 hour → investigate immediately.
     */
    fun recordNavigationError(route: String, error: String, guiMode: String = "unknown") {
        Timber.e("Navigation error on route='$route' gui='$guiMode': $error")
        crashlytics?.log("NAV_ERROR route=$route gui=$guiMode error=$error")
        analytics?.logEvent("navigation_error") {
            param("route", route.take(100))
            param("error_message", error.take(100))
            param("gui_mode", guiMode)
        }
    }

    /**
     * Record successful navigation for funnel analysis.
     * Helps identify which routes are most/least used.
     */
    fun recordNavigationSuccess(route: String, guiMode: String = "unknown") {
        crashlytics?.log("NAV_SUCCESS route=$route gui=$guiMode")
        analytics?.logEvent("navigation_success") {
            param("route", route.take(100))
            param("gui_mode", guiMode)
        }
    }

    // ── Feature Flags ──────────────────────────────────────────────────────

    /**
     * Record when a feature flag state changes (RemoteConfig update or local override).
     * Helps correlate feature rollouts with crash rate changes.
     */
    fun recordFeatureFlagChanged(flagName: String, oldValue: Boolean, newValue: Boolean) {
        Timber.d("Feature flag changed: $flagName $oldValue → $newValue")
        crashlytics?.log("FEATURE_FLAG $flagName: $oldValue → $newValue")
        analytics?.logEvent("feature_flag_changed") {
            param("flag_name", flagName)
            param("old_value", if (oldValue) "true" else "false")
            param("new_value", if (newValue) "true" else "false")
        }
        crashlytics?.setCustomKey("flag_$flagName", newValue)
    }

    // ── Performance ────────────────────────────────────────────────────────

    /**
     * Record a janky frame event from Matrix canvas effects.
     * Alert threshold: jank_rate > 10% on any device tier.
     */
    fun recordFrameJank(screenType: String, frameTimeMs: Long, deviceTier: String = "unknown") {
        if (frameTimeMs > 16L) {
            Timber.w("Frame jank on $screenType: ${frameTimeMs}ms (tier=$deviceTier)")
            analytics?.logEvent("matrix_frame_jank") {
                param("screen_type", screenType)
                param("frame_time_ms", frameTimeMs)
                param("device_tier", deviceTier)
            }
        }
    }

    /**
     * Record navigation transition latency.
     * Alert threshold: p99 > 500ms on any device.
     */
    fun recordNavigationLatency(route: String, latencyMs: Long) {
        if (latencyMs > 200L) Timber.w("Slow navigation to $route: ${latencyMs}ms")
        analytics?.logEvent("navigation_latency") {
            param("route", route.take(100))
            param("latency_ms", latencyMs)
            param("slow", if (latencyMs > 200L) "true" else "false")
        }
    }

    // ── Security / Database ────────────────────────────────────────────────

    /**
     * Record database passphrase generation status.
     * A failure here means the database may not be encrypted — critical alert.
     */
    fun recordPassphraseEvent(success: Boolean, durationMs: Long, isFallback: Boolean = false) {
        val status = when {
            !success -> "FAILED"
            isFallback -> "FALLBACK"
            else -> "SUCCESS"
        }
        Timber.d("DB passphrase: status=$status duration=${durationMs}ms")
        crashlytics?.log("DB_PASSPHRASE status=$status duration=${durationMs}ms")
        analytics?.logEvent("db_passphrase_generation") {
            param("success", if (success) "true" else "false")
            param("duration_ms", durationMs)
            param("is_fallback", if (isFallback) "true" else "false")
            param("status", status)
        }
        if (!success || isFallback) crashlytics?.setCustomKey("db_passphrase_status", status)
    }

    /**
     * Record when the adaptive performance manager reduces animation density.
     * Helps identify which devices need optimization.
     */
    fun recordAdaptationTriggered(oldDensity: Float, newDensity: Float, deviceModel: String) {
        Timber.d("Adaptive perf triggered on $deviceModel: $oldDensity → $newDensity")
        analytics?.logEvent("matrix_adaptation_triggered") {
            param("old_density", oldDensity.toDouble())
            param("new_density", newDensity.toDouble())
            param("device_model", deviceModel.take(50))
        }
    }

    // ── App Lifecycle ──────────────────────────────────────────────────────

    /**
     * Record which GUI the user chose on the landing screen.
     * Tracks adoption of GUI2 (modern) vs GUI3 (Matrix) vs GUI1 (legacy).
     */
    fun recordGuiSelected(guiMode: String) {
        crashlytics?.setCustomKey("active_gui", guiMode)
        analytics?.logEvent("gui_selected") {
            param("gui_mode", guiMode)
        }
    }

    /**
     * Record app startup completion time.
     * Alert threshold: cold start > 5s on mid-range device.
     */
    fun recordAppStartup(coldStartMs: Long, dbOpenMs: Long) {
        Timber.d("App startup: total=${coldStartMs}ms db_open=${dbOpenMs}ms")
        analytics?.logEvent("app_startup") {
            param("cold_start_ms", coldStartMs)
            param("db_open_ms", dbOpenMs)
            param("slow_start", if (coldStartMs > 3000L) "true" else "false")
        }
        if (coldStartMs > 5000L) {
            Timber.e("SLOW STARTUP: ${coldStartMs}ms on ${android.os.Build.MODEL}")
            crashlytics?.log("SLOW_STARTUP ${coldStartMs}ms on ${android.os.Build.MODEL}")
        }
    }
}
