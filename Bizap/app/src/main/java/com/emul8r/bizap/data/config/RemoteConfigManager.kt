package com.emul8r.bizap.data.config

import com.emul8r.bizap.data.logging.ErrorLogger
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RemoteConfigManager — production [FeatureFlagManager] backed by Firebase Remote Config.
 *
 * Behaviour:
 * - On first access the Remote Config cache is fetched and activated.
 * - Each flag read uses [FirebaseRemoteConfig.getBoolean] with a safe fallback
 *   to [FeatureFlag.defaultValue] if Remote Config throws.
 * - Gradual rollouts use a `<flag.key>_rollout_percentage` companion key.
 * - Local overrides (via [setEnabled]) are held in memory and shadow Remote Config.
 * - [observeFlag] exposes flag values as a cold [Flow].
 *
 * All exceptions are swallowed after logging via [ErrorLogger] so the app never
 * crashes because of a Remote Config read failure.
 */
@Singleton
class RemoteConfigManagerImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val errorLogger: ErrorLogger
) : FeatureFlagManager {

    // In-memory overrides; shadows Remote Config for QA / debug use
    private val localOverrides = ConcurrentHashMap<FeatureFlag, Boolean>()

    // State flows per flag so observers can be notified of override changes
    private val flagFlows: Map<FeatureFlag, MutableStateFlow<Boolean>> =
        FeatureFlag.entries.associateWith { flag ->
            MutableStateFlow(flag.defaultValue)
        }

    override suspend fun isEnabled(flag: FeatureFlag): Boolean {
        // Local override takes priority
        localOverrides[flag]?.let { return it }

        return try {
            ensureFetched()
            remoteConfig.getBoolean(flag.key)
        } catch (e: Exception) {
            errorLogger.logError(
                e,
                mapOf("flag" to flag.key, "operation" to "isEnabled")
            )
            flag.defaultValue
        }
    }

    override suspend fun isEnabledForUser(flag: FeatureFlag, userId: Long): Boolean {
        if (!isEnabled(flag)) return false

        return try {
            val rolloutPct =
                remoteConfig.getString("${flag.key}_rollout_percentage").toIntOrNull() ?: 0
            val bucket = (userId % 100).toInt()
            bucket < rolloutPct
        } catch (e: Exception) {
            errorLogger.logError(
                e,
                mapOf("flag" to flag.key, "operation" to "isEnabledForUser", "userId" to userId.toString())
            )
            false
        }
    }

    override suspend fun setEnabled(flag: FeatureFlag, enabled: Boolean) {
        localOverrides[flag] = enabled
        flagFlows[flag]?.emit(enabled)
        Timber.d("FeatureFlag override: ${flag.key} = $enabled")
    }

    override fun observeFlag(flag: FeatureFlag): Flow<Boolean> {
        return flagFlows[flag]?.asStateFlow()
            ?: MutableStateFlow(flag.defaultValue).asStateFlow()
    }

    // ---------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------

    private var fetched = false

    private suspend fun ensureFetched() {
        if (fetched) return
        try {
            remoteConfig.fetchAndActivate().await()
            fetched = true
            // Sync state flows with freshly fetched values
            FeatureFlag.entries.forEach { flag ->
                flagFlows[flag]?.emit(remoteConfig.getBoolean(flag.key))
            }
        } catch (e: Exception) {
            Timber.w(e, "RemoteConfig fetch failed — using cached/default values")
        }
    }
}
