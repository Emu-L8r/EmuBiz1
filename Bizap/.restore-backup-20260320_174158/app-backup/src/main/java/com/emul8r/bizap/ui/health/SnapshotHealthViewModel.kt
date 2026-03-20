package com.emul8r.bizap.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.health.SnapshotHealthCheck
import com.emul8r.bizap.data.health.SnapshotHealthReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for managing snapshot health status and operations.
 *
 * Responsibilities:
 * - Run health checks on demand
 * - Monitor snapshot consistency
 * - Trigger backfill operations
 * - Update UI with health status
 */
@HiltViewModel
class SnapshotHealthViewModel @Inject constructor(
    private val healthCheck: SnapshotHealthCheck
) : ViewModel() {

    companion object {
        private const val CHECK_INTERVAL_MS = 5 * 60 * 1000L  // 5 minutes
    }

    // State management
    private val _healthReport = MutableStateFlow<SnapshotHealthReport?>(null)
    val healthReport: StateFlow<SnapshotHealthReport?> = _healthReport.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _lastCheckTime = MutableStateFlow<Long?>(null)
    val lastCheckTime: StateFlow<Long?> = _lastCheckTime.asStateFlow()

    private val _isBackfillRunning = MutableStateFlow(false)
    val isBackfillRunning: StateFlow<Boolean> = _isBackfillRunning.asStateFlow()

    init {
        // Run initial health check
        checkHealth()
    }

    /**
     * Runs a snapshot health check.
     * Updates UI state with results.
     */
    fun checkHealth() {
        viewModelScope.launch {
            try {
                _isChecking.value = true
                Timber.d("🏥 Running snapshot health check...")

                val report = healthCheck.checkHealth()
                _healthReport.value = report
                _lastCheckTime.value = System.currentTimeMillis()

                if (report.isHealthy) {
                    Timber.i("✅ Snapshot health check passed")
                } else {
                    Timber.w("⚠️ Snapshot health issues detected")
                    report.overallIssues.forEach { issue ->
                        Timber.w("   ❌ $issue")
                    }
                    report.recommendations.forEach { rec ->
                        Timber.i("   💡 $rec")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Health check failed")
            } finally {
                _isChecking.value = false
            }
        }
    }

    /**
     * Checks if a new health check should be run based on time elapsed.
     */
    fun shouldRecheck(): Boolean {
        val lastCheck = _lastCheckTime.value ?: return true
        val elapsedMs = System.currentTimeMillis() - lastCheck
        return elapsedMs >= CHECK_INTERVAL_MS
    }

    /**
     * Triggers backfill operation if health is unhealthy.
     */
    fun runBackfillIfNeeded() {
        viewModelScope.launch {
            val report = _healthReport.value
            if (report != null && !report.isHealthy) {
                runBackfill()
            }
        }
    }

    /**
     * Runs backfill operation and re-checks health.
     */
    fun runBackfill() {
        viewModelScope.launch {
            try {
                _isBackfillRunning.value = true
                Timber.d("🔄 Starting backfill operation...")

                // This would trigger the actual backfill (e.g., run migration)
                // For now, just log it
                Timber.i("✅ Backfill operation requested")

                // Re-check health after backfill
                checkHealth()
            } catch (e: Exception) {
                Timber.e(e, "❌ Backfill operation failed")
            } finally {
                _isBackfillRunning.value = false
            }
        }
    }

    /**
     * Dismisses current health warning.
     */
    fun dismissWarning() {
        _healthReport.value = null
    }
}

