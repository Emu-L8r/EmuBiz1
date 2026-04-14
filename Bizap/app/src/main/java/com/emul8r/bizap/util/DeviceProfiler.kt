package com.emul8r.bizap.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Stable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collects device profile information for performance-based feature adaptation.
 *
 * Used to:
 * - Classify device tier (premium/mid-range/low-end)
 * - Monitor memory usage
 * - Adjust effect quality based on device capabilities
 */
@Singleton
class DeviceProfiler @Inject constructor(
    private val context: Context
) {

    private val runtime = Runtime.getRuntime()
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    /**
     * Get device model name.
     */
    fun getDeviceModel(): String = Build.MODEL ?: "Unknown"

    /**
     * Get device manufacturer.
     */
    fun getDeviceManufacturer(): String = Build.MANUFACTURER ?: "Unknown"

    /**
     * Get Android API level.
     */
    fun getApiLevel(): Int = Build.VERSION.SDK_INT

    /**
     * Get full device identifier for analytics.
     */
    fun getDeviceIdentifier(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
    }

    /**
     * Estimate device performance tier based on specs.
     */
    fun estimateDeviceTier(): DeviceTier {
        val totalMemoryGb = getTotalMemoryMb() / 1000f
        val cpuCores = getCpuCores()

        return when {
            // Premium: 8+ GB RAM and 8+ cores
            totalMemoryGb >= 8f && cpuCores >= 8 -> DeviceTier.PREMIUM
            // Mid-range: 4-8 GB RAM or 6+ cores
            (totalMemoryGb >= 4f && totalMemoryGb < 8f) || cpuCores >= 6 -> DeviceTier.MID_RANGE
            // Low-end: < 4 GB RAM or < 6 cores
            else -> DeviceTier.LOW_END
        }
    }

    /**
     * Get total RAM in MB.
     */
    fun getTotalMemoryMb(): Int {
        return (runtime.totalMemory() / 1_000_000L).toInt()
    }

    /**
     * Get available RAM in MB.
     */
    fun getAvailableMemoryMb(): Int {
        return ((runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()) / 1_000_000L).toInt()
    }

    /**
     * Get memory utilization as percentage (0-100).
     */
    fun getMemoryUtilizationPercent(): Int {
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val totalMemory = runtime.maxMemory()
        return ((usedMemory * 100) / totalMemory).toInt()
    }

    /**
     * Get number of CPU cores.
     */
    fun getCpuCores(): Int = runtime.availableProcessors()

    /**
     * Get memory info via ActivityManager.
     */
    fun getMemoryInfo(): MemoryInfo {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        return MemoryInfo(
            totalMemoryMb = (memInfo.totalMem / 1_000_000L).toInt(),
            availableMemoryMb = (memInfo.availMem / 1_000_000L).toInt(),
            isLowMemory = memInfo.lowMemory,
            totalMemoryGb = memInfo.totalMem / 1_000_000_000.0
        )
    }

    /**
     * Check if device is low-memory.
     */
    fun isLowMemoryDevice(): Boolean {
        return getMemoryInfo().isLowMemory
    }

    /**
     * Log device profile to Timber.
     */
    fun logDeviceProfile() {
        val memInfo = getMemoryInfo()
        val tier = estimateDeviceTier()

        Timber.i(
            "Device profile: model=%s, tier=%s, cores=%d, memory=%.1fGB, available=%dMB",
            getDeviceModel(),
            tier,
            getCpuCores(),
            memInfo.totalMemoryGb,
            memInfo.availableMemoryMb
        )
    }

    /**
     * Get device profile as map for analytics.
     */
    fun getDeviceProfileMap(): Map<String, String> {
        val memInfo = getMemoryInfo()
        return mapOf(
            "model" to getDeviceModel(),
            "manufacturer" to getDeviceManufacturer(),
            "api_level" to getApiLevel().toString(),
            "tier" to estimateDeviceTier().name,
            "cpu_cores" to getCpuCores().toString(),
            "total_memory_mb" to memInfo.totalMemoryMb.toString(),
            "available_memory_mb" to memInfo.availableMemoryMb.toString(),
            "memory_percent" to getMemoryUtilizationPercent().toString(),
            "is_low_memory" to memInfo.isLowMemory.toString()
        )
    }

    /**
     * Device memory information.
     */
    @Stable
    data class MemoryInfo(
        val totalMemoryMb: Int,
        val availableMemoryMb: Int,
        val isLowMemory: Boolean,
        val totalMemoryGb: Double
    ) {
        /**
         * Calculate available memory percentage (0-100).
         */
        fun getAvailablePercentage(): Int {
            return if (totalMemoryMb > 0) {
                ((availableMemoryMb * 100) / totalMemoryMb)
            } else {
                0
            }
        }
    }

    /**
     * Device performance tier classification.
     */
    enum class DeviceTier(val displayName: String) {
        PREMIUM("Premium (High-end)"),
        MID_RANGE("Mid-Range"),
        LOW_END("Low-End (Budget)")
    }
}




