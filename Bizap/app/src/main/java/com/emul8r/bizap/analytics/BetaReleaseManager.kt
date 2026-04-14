package com.emul8r.bizap.analytics

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages beta release workflow, user tracking, and feedback collection.
 *
 * Provides methods for:
 * - Beta enrollment management
 * - Session tracking
 * - Issue reporting
 * - Feedback collection
 */
@Singleton
class BetaReleaseManager @Inject constructor(
    private val prefs: SharedPreferences,
    private val analytics: FirebaseAnalytics,
    private val crashlyticsLogger: MatrixCrashlyticsLogger
) {

    /**
     * Enroll user as a beta tester.
     */
    fun enrollBetaTester(deviceId: String) {
        prefs.edit {
            putBoolean("is_beta_tester", true)
            putLong("beta_enrollment_time", System.currentTimeMillis())
        }

        analytics.logEvent("beta_enrollment") {
            param("device_id_hash", deviceId.hashCode().toString())
            param("timestamp", System.currentTimeMillis())
        }

        Timber.i("User enrolled in Matrix effects beta")
    }

    /**
     * Check if user is enrolled in beta testing.
     */
    fun isBetaTester(): Boolean {
        return prefs.getBoolean("is_beta_tester", false)
    }

    /**
     * Get time of beta enrollment.
     */
    fun getBetaEnrollmentTime(): Long {
        return prefs.getLong("beta_enrollment_time", 0L)
    }

    /**
     * Get duration in beta (in milliseconds).
     */
    fun getBetaDurationMs(): Long {
        return if (isBetaTester()) {
            System.currentTimeMillis() - getBetaEnrollmentTime()
        } else {
            0L
        }
    }

    /**
     * Submit beta feedback with optional rating.
     *
     * @param feedback User's feedback text
     * @param rating Optional 1-5 star rating
     */
    fun submitBetaFeedback(feedback: String, rating: Int = 0) {
        analytics.logEvent("beta_feedback_submitted") {
            param("feedback_length", feedback.length.toLong())
            param("rating", rating.toLong())
            param("duration_ms", getBetaDurationMs())
        }

        Timber.i("Beta feedback submitted: rating=$rating, length=${feedback.length}")
    }

    /**
     * Log the start of a beta testing session.
     */
    fun logBetaSessionStart() {
        analytics.logEvent("beta_session_start") {
            param("is_returning_tester", if (isBetaTester()) 1L else 0L)
        }
        Timber.i("Beta session started")
    }

    /**
     * Log the end of a beta testing session.
     */
    fun logBetaSessionEnd(durationSeconds: Long) {
        analytics.logEvent("beta_session_end") {
            param("duration_seconds", durationSeconds)
        }
        Timber.i("Beta session ended: ${durationSeconds}s")
    }

    /**
     * Report a crash or issue during beta testing.
     *
     * @param issueName Descriptive name of the issue
     * @param severity One of: LOW, MEDIUM, HIGH, CRITICAL
     * @param details Additional details about the issue
     */
    fun reportBetaIssue(issueName: String, severity: String, details: String = "") {
        analytics.logEvent("beta_issue_reported") {
            param("issue_name", issueName)
            param("severity", severity)
            param("details_length", details.length.toLong())
        }

        crashlyticsLogger.logEffectError(
            "BetaIssue: $issueName",
            Exception("$severity: $details")
        )

        Timber.w("Beta issue reported: $issueName ($severity)")
    }

    /**
     * Log a feature request from beta tester.
     */
    fun logFeatureRequest(featureName: String, description: String) {
        analytics.logEvent("beta_feature_request") {
            param("feature_name", featureName)
            param("description_length", description.length.toLong())
        }
        Timber.i("Feature request: $featureName")
    }

    /**
     * Log beta performance metrics collection.
     */
    fun logPerformanceData(
        avgFrameTimeMs: Float,
        jankRatePercent: Float,
        deviceTier: String
    ) {
        analytics.logEvent("beta_performance_data") {
            param("avg_frame_time_ms", (avgFrameTimeMs * 100).toLong())  // Store as fixed-point
            param("jank_rate_percent", (jankRatePercent * 100).toLong())
            param("device_tier", deviceTier)
            param("duration_in_beta_ms", getBetaDurationMs())
        }
        Timber.d("Perf data collected: avg=${avgFrameTimeMs}ms, jank=${jankRatePercent}%")
    }

    /**
     * Log opt-out from beta program.
     */
    fun optOutOfBeta(reason: String = "") {
        analytics.logEvent("beta_opt_out") {
            param("reason", reason)
            param("duration_in_beta_ms", getBetaDurationMs())
        }

        prefs.edit {
            putBoolean("is_beta_tester", false)
            putString("beta_opt_out_reason", reason)
            putLong("beta_opt_out_time", System.currentTimeMillis())
        }

        Timber.i("User opted out of beta: $reason")
    }

    /**
     * Get count of reported issues.
     */
    fun getReportedIssueCount(): Int {
        return prefs.getInt("beta_issue_count", 0)
    }

    /**
     * Increment issue report counter.
     */
    fun incrementIssueCount() {
        val count = getReportedIssueCount()
        prefs.edit { putInt("beta_issue_count", count + 1) }
    }

    /**
     * Get beta user's unique identifier (for tracking across sessions).
     */
    fun getBetaUserIdentifier(): String {
        return prefs.getString("beta_user_id", "")
            ?: run {
                val id = System.currentTimeMillis().toString()
                prefs.edit { putString("beta_user_id", id) }
                id
            }
    }

    /**
     * Reset all beta data (used for testing).
     */
    fun resetBetaData() {
        prefs.edit { clear() }
        Timber.i("Beta data reset")
    }
}



