package com.emul8r.bizap.data.config

import kotlinx.coroutines.flow.Flow

/**
 * Enumeration of all feature flags tracked via Firebase Remote Config.
 *
 * Each flag carries a [key] (the Remote Config parameter name) and a [defaultValue]
 * that is used when Remote Config is unavailable or the flag has not been configured.
 *
 * Naming convention: lowercase_underscore keys matching the Remote Config console.
 */
enum class FeatureFlag(val key: String, val defaultValue: Boolean) {
    RECURRING_INVOICES("recurring_invoices", false),
    EMAIL_INTEGRATION("email_integration", false),
    ADVANCED_SEARCH("advanced_search", false),
    CUSTOMER_PORTAL("customer_portal", false),
    PUSH_NOTIFICATIONS("push_notifications", false),
    MULTI_BUSINESS("multi_business", false),
    DARK_MODE("dark_mode", false),
    LOCALIZATION("localization", false)
}

/**
 * FeatureFlagManager — controls feature availability at runtime.
 *
 * Implementations may use Firebase Remote Config for remote control or fall back
 * to local defaults, making the interface suitable for both production and testing.
 */
interface FeatureFlagManager {
    /**
     * Returns whether [flag] is enabled globally.
     *
     * Falls back to [FeatureFlag.defaultValue] on any error.
     */
    suspend fun isEnabled(flag: FeatureFlag): Boolean

    /**
     * Returns whether [flag] is enabled for a specific user as part of a gradual rollout.
     *
     * The rollout percentage is read from a companion Remote Config key
     * `<flag.key>_rollout_percentage`. A [userId] is placed into a stable bucket
     * (0–99) so the same user consistently sees (or does not see) the feature.
     *
     * @param flag   The feature to check
     * @param userId Stable identifier for bucketing (e.g. business / user ID)
     */
    suspend fun isEnabledForUser(flag: FeatureFlag, userId: Long): Boolean

    /**
     * Override a flag value locally (useful for QA / debug builds).
     *
     * This value persists only for the current process lifetime unless the
     * implementation stores it persistently.
     */
    suspend fun setEnabled(flag: FeatureFlag, enabled: Boolean)

    /**
     * Observe flag changes as a [Flow].
     *
     * Emits the current value immediately and then emits each subsequent update.
     */
    fun observeFlag(flag: FeatureFlag): Flow<Boolean>
}
