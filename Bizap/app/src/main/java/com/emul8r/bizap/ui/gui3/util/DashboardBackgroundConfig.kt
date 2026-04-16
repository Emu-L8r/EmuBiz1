package com.emul8r.bizap.ui.gui3.util

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import timber.log.Timber

/**
 * Dashboard Background Configuration
 *
 * Maps Firebase Remote Config keys to effect pipeline parameters.
 * Supports 3 immersion presets: "minimal", "balanced" (default), "intense"
 *
 * Performance targets:
 * - minimal: ~10ms total frame time, 60 FPS stable on all devices
 * - balanced: ~12-13ms total frame time, 60 FPS stable on most devices
 * - intense: ~14-16ms total frame time, 60 FPS on premium devices, adaptive on budget
 */
data class DashboardBackgroundConfig(
    val rainDensity: Float,
    val glitchIntensity: Float,
    val scanlineAlpha: Float,
    val immersionLevel: String
) {
    companion object {
        private const val TAG = "DashboardBgConfig"

        /**
         * Create config from Remote Config settings
         * Defaults to "balanced" preset if key is missing or invalid
         */
        fun fromRemoteConfig(): DashboardBackgroundConfig {
            return try {
                val remoteConfig = FirebaseRemoteConfig.getInstance()
                val immersionLevel = remoteConfig.getString("matrix_dashboard_immersion_level")
                    .takeIf { it.isNotEmpty() } ?: "balanced"

                when (immersionLevel) {
                    "minimal" -> {
                        Timber.d("$TAG: Using MINIMAL preset")
                        DashboardBackgroundConfig(
                            rainDensity = 0.3f,
                            glitchIntensity = 0.2f,
                            scanlineAlpha = 0.02f,
                            immersionLevel = "minimal"
                        )
                    }
                    "intense" -> {
                        Timber.d("$TAG: Using INTENSE preset")
                        DashboardBackgroundConfig(
                            rainDensity = 1.2f,
                            glitchIntensity = 0.7f,
                            scanlineAlpha = 0.08f,
                            immersionLevel = "intense"
                        )
                    }
                    else -> {
                        Timber.d("$TAG: Using BALANCED preset (default)")
                        DashboardBackgroundConfig(
                            rainDensity = 0.8f,
                            glitchIntensity = 0.5f,
                            scanlineAlpha = 0.05f,
                            immersionLevel = "balanced"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.w("$TAG: Failed to read from Remote Config, using BALANCED default: $e")
                DashboardBackgroundConfig(
                    rainDensity = 0.8f,
                    glitchIntensity = 0.5f,
                    scanlineAlpha = 0.05f,
                    immersionLevel = "balanced"
                )
            }
        }
    }
}


