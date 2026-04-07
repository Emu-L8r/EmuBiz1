package com.emul8r.bizap.data.network

/**
 * Represents the current state of the device's network connectivity.
 *
 * Provides comprehensive information about network availability, type, and quality hints
 * to enable intelligent request adaptation (e.g., smaller payloads on cellular, aggressive
 * caching on poor connections).
 *
 * This replaces the old "is online?" boolean with rich, observable network state.
 *
 * Example:
 * ```
 * networkMonitor.networkState.collect { state ->
 *     when {
 *         !state.isOnline -> showOfflineUI()
 *         state.type == NetworkType.CELLULAR && state.quality == NetworkQuality.POOR ->
 *             fetchSmallPayload() // Adapt request size to connection
 *         else -> fetchNormalPayload()
 *     }
 * }
 * ```
 *
 * @property isOnline True if device has an active internet connection
 * @property type The type of network connection (WiFi, Cellular, Ethernet, or None)
 * @property quality A hint about connection quality (used for adaptive behavior)
 */
data class NetworkState(
    val isOnline: Boolean,
    val type: NetworkType,
    val quality: NetworkQuality
) {
    companion object {
        /** Offline state (no network available). */
        val Offline = NetworkState(
            isOnline = false,
            type = NetworkType.NONE,
            quality = NetworkQuality.UNKNOWN
        )

        /** Excellent connection state. */
        fun excellent(type: NetworkType) = NetworkState(
            isOnline = true,
            type = type,
            quality = NetworkQuality.EXCELLENT
        )

        /** Good connection state. */
        fun good(type: NetworkType) = NetworkState(
            isOnline = true,
            type = type,
            quality = NetworkQuality.GOOD
        )

        /** Poor connection state. */
        fun poor(type: NetworkType) = NetworkState(
            isOnline = true,
            type = type,
            quality = NetworkQuality.POOR
        )
    }
}

/**
 * Type of network connection.
 *
 * Used to adapt request strategy:
 * - WIFI: Larger payloads, less aggressive caching
 * - CELLULAR: Smaller payloads, aggressive caching
 * - ETHERNET: Similar to WiFi
 * - NONE: Offline queue requests
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    NONE
}

/**
 * Hint about network connection quality.
 *
 * Derived from signal strength and latency observations.
 * Used to adapt timeouts and retry strategies.
 *
 * Examples:
 * - EXCELLENT: Strong signal, low latency → use standard timeouts
 * - GOOD: Normal signal, acceptable latency → use standard timeouts
 * - POOR: Weak signal, high latency → use extended timeouts
 * - UNKNOWN: Unable to determine → assume GOOD
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    POOR,
    UNKNOWN
}

