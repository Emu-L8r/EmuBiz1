package com.emul8r.bizap.data.network

import com.emul8r.bizap.data.network.NetworkQuality.POOR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * Defines timeout strategies for OkHttpClient configuration.
 *
 * This replaces hardcoded magic numbers with explicit, testable timeout policies.
 * Timeouts can be fixed (same for all connections) or adaptive (based on network quality).
 *
 * Rationale: A 30-second timeout is reasonable for fiber/WiFi but aggressive for 3G.
 * However, for Bizap's use case (frequent small API calls for invoices/customers),
 * a fixed conservative timeout (30s) is appropriate for now. Adaptive tuning is deferred
 * to Phase 2 once real RTT metrics are available.
 *
 * Example:
 * ```
 * val config = HttpClientConfiguration.fixed(
 *     connectTimeoutMs = 30_000,
 *     readTimeoutMs = 30_000,
 *     writeTimeoutMs = 30_000
 * )
 * ```
 */
interface HttpClientConfiguration {
    /** Time allowed for TCP handshake (ms) */
    val connectTimeoutMs: Long

    /** Time allowed to receive response data (ms) */
    val readTimeoutMs: Long

    /** Time allowed to send request data (ms) */
    val writeTimeoutMs: Long

    /** Time allowed for entire call (ms) */
    val callTimeoutMs: Long

    companion object {
        /**
         * Fixed timeout strategy—same timeouts regardless of network quality.
         *
         * **Rationale for 30-second defaults:**
         * - Conservative for 3G (gives slow connections time to complete)
         * - Acceptable for WiFi/fiber (not eternity)
         * - Matches Bizap's use case: small, frequent API calls
         *   (exchange rates, invoice metadata, customer data)
         *
         * **When to use:** For now. Adaptive tuning in Phase 2 with real metrics.
         *
         * @param connectTimeoutMs Default 30s—TCP handshake timeout
         * @param readTimeoutMs Default 30s—waiting for response data
         * @param writeTimeoutMs Default 30s—sending request data
         * @param callTimeoutMs Default 30s—total request deadline
         */
        fun fixed(
            connectTimeoutMs: Long = 30_000,
            readTimeoutMs: Long = 30_000,
            writeTimeoutMs: Long = 30_000,
            callTimeoutMs: Long = 30_000
        ): HttpClientConfiguration {
            return FixedHttpClientConfiguration(
                connectTimeoutMs = connectTimeoutMs,
                readTimeoutMs = readTimeoutMs,
                writeTimeoutMs = writeTimeoutMs,
                callTimeoutMs = callTimeoutMs
            )
        }

        /**
         * Adaptive timeout strategy—adjust timeouts based on network quality.
         *
         * **Quality mapping:**
         * - EXCELLENT/GOOD: Use standard timeouts (30s)
         * - POOR: Double timeouts (60s) to accommodate slow connections
         * - UNKNOWN: Assume GOOD (30s)
         *
         * **Future Enhancement:** Could also use signal strength and latency history
         * to compute adaptive backoff, but current implementation tracks network type
         * (WiFi vs. Cellular) via NetworkState.
         *
         * **Status:** Deferred to Phase 2 once NetworkState quality hints are reliable.
         *
         * @param networkState Flow of NetworkState to adapt to
         * @return Flow<HttpClientConfiguration> that updates as network changes
         */
        fun adaptive(networkState: Flow<NetworkState>): Flow<HttpClientConfiguration> {
            return networkState.map { state ->
                if (state.quality == POOR) {
                    // Double timeouts for poor connections
                    fixed(
                        connectTimeoutMs = 60_000,
                        readTimeoutMs = 60_000,
                        writeTimeoutMs = 60_000,
                        callTimeoutMs = 60_000
                    )
                } else {
                    // Standard timeouts for good/excellent/unknown
                    fixed()
                }
            }
        }
    }
}

/**
 * Fixed timeout configuration—same timeouts always.
 */
internal data class FixedHttpClientConfiguration(
    override val connectTimeoutMs: Long = 30_000,
    override val readTimeoutMs: Long = 30_000,
    override val writeTimeoutMs: Long = 30_000,
    override val callTimeoutMs: Long = 30_000
) : HttpClientConfiguration



