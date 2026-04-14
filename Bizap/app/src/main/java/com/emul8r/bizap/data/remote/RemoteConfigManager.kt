package com.emul8r.bizap.data.remote

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Remote Config manager for Matrix Canvas effects.
 * Provides centralized configuration for rollout control and A/B testing.
 */
@Singleton
class RemoteConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    init {
        // Set default values for feature flags
        remoteConfig.setDefaultsAsync(mapOf(
            // Canvas renderer main gate (default: off)
            "matrix_canvas_renderer_enabled" to false,

            // Effect toggles (default: on when renderer enabled)
            "matrix_rain_density" to 0.8,
            "matrix_glitch_intensity" to 0.5,
            "matrix_scanline_alpha" to 0.05,

            // Performance settings
            "matrix_adaptive_perf_enabled" to false,

            // Rollout percentage (0-100, default: 0%)
            "matrix_rollout_percentage" to 0L,

            // A/B test variants
            "matrix_glitch_intensity_variant_a" to 0.3,
            "matrix_glitch_intensity_variant_b" to 0.7,
            "matrix_use_variant_b" to false
        ))
    }

    /**
     * Check if canvas renderer is enabled (main gate).
     */
    fun isCanvasRendererEnabled(): Flow<Boolean> {
        return flowOf(remoteConfig.getBoolean("matrix_canvas_renderer_enabled"))
    }

    /**
     * Get rain particle density (0.3 - 1.5, default 0.8).
     */
    fun getRainDensity(): Flow<Float> {
        return flowOf(remoteConfig.getDouble("matrix_rain_density").toFloat())
    }

    /**
     * Get glitch effect intensity (0.0 - 1.0, default 0.5).
     */
    fun getGlitchIntensity(): Flow<Float> {
        return flowOf(remoteConfig.getDouble("matrix_glitch_intensity").toFloat())
    }

    /**
     * Get A/B test glitch intensity variant.
     */
    fun getGlitchIntensityVariant(): Flow<Float> {
        val useVariantB = remoteConfig.getBoolean("matrix_use_variant_b")
        val intensity = if (useVariantB) {
            remoteConfig.getDouble("matrix_glitch_intensity_variant_b")
        } else {
            remoteConfig.getDouble("matrix_glitch_intensity_variant_a")
        }
        return flowOf(intensity.toFloat())
    }

    /**
     * Get scanline effect alpha (0.0 - 0.2, default 0.05).
     */
    fun getScanlinesAlpha(): Flow<Float> {
        return flowOf(remoteConfig.getDouble("matrix_scanline_alpha").toFloat())
    }

    /**
     * Check if adaptive performance is enabled.
     */
    fun isAdaptivePerfEnabled(): Flow<Boolean> {
        return flowOf(remoteConfig.getBoolean("matrix_adaptive_perf_enabled"))
    }

    /**
     * Get rollout percentage (0-100, for staged rollout).
     */
    fun getRolloutPercentage(): Flow<Int> {
        return flowOf(remoteConfig.getLong("matrix_rollout_percentage").toInt())
    }

    /**
     * Check if user should see this feature based on rollout percentage.
     * Uses device hash for consistent assignment across sessions.
     */
    fun isInRollout(deviceId: String): Flow<Boolean> {
        val percentage = remoteConfig.getLong("matrix_rollout_percentage").toInt()
        val hash = deviceId.hashCode().let { if (it < 0) -it else it } % 100
        return flowOf(hash < percentage)
    }

    /**
     * Fetch and activate remote config updates.
     * Returns true if successful, false if error occurred.
     */
    suspend fun fetchAndActivateRemoteConfig(): Boolean {
        return runCatching {
            remoteConfig.fetch()
            remoteConfig.activate()
            true
        }.onFailure { e ->
            Timber.e(e, "Remote config fetch/activate failed")
        }.getOrDefault(false)
    }

    /**
     * Fetch remote config but don't activate (preview mode).
     */
    suspend fun fetchRemoteConfigPreview(): Boolean {
        return runCatching {
            remoteConfig.fetch()
            true
        }.onFailure { e ->
            Timber.e(e, "Remote config fetch failed")
        }.getOrDefault(false)
    }

    /**
     * Manually activate fetched config (after preview check).
     */
    fun activateFetchedConfig() {
        remoteConfig.activate()
        Timber.i("Remote config activated")
    }

    /**
     * Get all current config values as map (for debugging).
     */
    fun getAllConfigValues(): Map<String, Any> {
        return remoteConfig.all.mapValues { (_, value) ->
            value.asString()
        }
    }
}

