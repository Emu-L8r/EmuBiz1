package com.emul8r.bizap.data.network

import android.content.Context
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network signal quality (WiFi strength and cellular signal) to enable
 * adaptive timeout and retry behavior.
 *
 * **How it works:**
 * - WiFi: Reads RSSI (Received Signal Strength Indicator) in dBm
 * - Cellular: Reads signal level (0-4 bars, convert to quality enum)
 * - Emits NetworkQuality enum every 2 seconds
 *
 * **Signal strength ranges (WiFi RSSI in dBm):**
 * ```
 * -30 to -50: EXCELLENT (very strong, close to router)
 * -50 to -70: GOOD (normal signal)
 * -70 to -90: POOR (weak signal, intermittent drops)
 * -90+:       POOR (very weak or lost)
 * ```
 *
 * **Cellular signal levels (Android API 31+):**
 * ```
 * 4+ bars: EXCELLENT
 * 3 bars:  GOOD
 * 1-2 bars: POOR
 * 0 bars:  POOR
 * ```
 *
 * @property networkQuality Flow<NetworkQuality> that emits quality every ~2 seconds
 */
@Singleton
class SignalQualityMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    /**
     * Emits NetworkQuality based on current WiFi/Cellular signal strength.
     * Polls every 2 seconds. Logs only when quality or RSSI value actually changes,
     * eliminating the every-2s spam previously caused by an unconditional Timber.d
     * inside getWiFiQuality().
     */
    val networkQuality: Flow<NetworkQuality> = flow {
        var lastQuality: NetworkQuality? = null
        var lastRssi: Int? = null

        while (true) {
            val quality = determineQuality()
            val currentRssi = getCurrentRssi()

            if (quality != lastQuality || currentRssi != lastRssi) {
                val emoji = when (quality) {
                    NetworkQuality.EXCELLENT -> "📶"
                    NetworkQuality.GOOD -> "📡"
                    NetworkQuality.POOR -> "⚠️"
                    NetworkQuality.UNKNOWN -> "❓"
                }
                val rssiStr = if (currentRssi != null) " (RSSI: ${currentRssi}dBm)" else ""
                Timber.i("$emoji Signal quality changed: $lastQuality → ${quality.name}$rssiStr (${describeQuality(quality)})")
                lastQuality = quality
                lastRssi = currentRssi
            }

            emit(quality)
            delay(2000)  // Poll every 2 seconds
        }
    }

    /**
     * Determine current network quality based on WiFi RSSI or cellular signal level.
     *
     * @return NetworkQuality.EXCELLENT, GOOD, POOR, or UNKNOWN
     */
    private suspend fun determineQuality(): NetworkQuality = withContext(Dispatchers.Default) {
        try {
            // Try WiFi first (more reliable indicator)
            val wifiQuality = getWiFiQuality()
            if (wifiQuality != NetworkQuality.UNKNOWN) {
                return@withContext wifiQuality
            }

            // Fall back to cellular
            val cellularQuality = getCellularQuality()
            if (cellularQuality != NetworkQuality.UNKNOWN) {
                return@withContext cellularQuality
            }

            // If we can't determine quality, assume GOOD (conservative)
            NetworkQuality.GOOD
        } catch (e: Exception) {
            Timber.e(e, "Error determining signal quality")
            NetworkQuality.UNKNOWN
        }
    }

    /**
     * Returns raw WiFi RSSI in dBm, or null if unavailable.
     * Used solely for change-detection in the flow; does not log.
     */
    @Suppress("NewApi", "DEPRECATION")
    private fun getCurrentRssi(): Int? = try {
        wifiManager.connectionInfo?.rssi
    } catch (_: Exception) {
        null
    }

    /**
     * Determine quality from WiFi RSSI (Received Signal Strength Indicator).
     *
     * RSSI is measured in dBm (decibels relative to 1 milliwatt).
     * More negative = weaker signal.
     *
     * @return EXCELLENT (-30 to -50), GOOD (-50 to -70), POOR (< -70), or UNKNOWN
     */
    private fun getWiFiQuality(): NetworkQuality {
        return try {
            @Suppress("NewApi", "DEPRECATION")
            val connectionInfo = wifiManager.connectionInfo ?: return NetworkQuality.UNKNOWN
            val rssi = connectionInfo.rssi

            when {
                rssi >= -50 -> NetworkQuality.EXCELLENT   // Very strong
                rssi >= -70 -> NetworkQuality.GOOD         // Normal
                rssi >= -80 -> NetworkQuality.POOR         // Weak
                else -> NetworkQuality.POOR                // Very weak
            }
            // Timber.d removed — was firing unconditionally every 2s regardless of change.
            // RSSI is now included in the quality-changed log emitted by the flow above.
        } catch (e: Exception) {
            Timber.w(e, "Could not read WiFi RSSI")
            NetworkQuality.UNKNOWN
        }
    }

    /**
     * Determine quality from cellular signal level.
     *
     * On Android 31+, SignalStrength API provides bars (0-4).
     * On older APIs, we try to estimate from level field.
     *
     * @return EXCELLENT (4 bars), GOOD (3 bars), POOR (1-2 bars or 0 bars), or UNKNOWN
     */
    private fun getCellularQuality(): NetworkQuality {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val signalStrength = telephonyManager.signalStrength
                if (signalStrength != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val level = signalStrength.level
                    return when (level) {
                        4 -> NetworkQuality.EXCELLENT
                        3 -> NetworkQuality.GOOD
                        1, 2 -> NetworkQuality.POOR
                        0 -> NetworkQuality.POOR
                        else -> NetworkQuality.GOOD
                    }
                }
            }
            NetworkQuality.UNKNOWN
        } catch (e: Exception) {
            Timber.w(e, "Could not read cellular signal level")
            NetworkQuality.UNKNOWN
        }
    }

    /**
     * Get human-readable description of signal quality (for logging).
     */
    fun describeQuality(quality: NetworkQuality): String = when (quality) {
        NetworkQuality.EXCELLENT -> "Excellent (strong signal)"
        NetworkQuality.GOOD -> "Good (normal signal)"
        NetworkQuality.POOR -> "Poor (weak signal)"
        NetworkQuality.UNKNOWN -> "Unknown (cannot determine)"
    }
}
