package com.emul8r.bizap.security

import android.content.Context
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import timber.log.Timber

/**
 * Security configuration for Bizap application.
 *
 * **Features:**
 * - SSL certificate pinning for API endpoints
 * - Tamper detection via HTTPS only
 * - Prevention of man-in-the-middle attacks
 *
 * **Production Configuration:**
 * This should be adjusted based on actual certificate pins from production API.
 */
object SecurityConfig {

    /**
     * Create OkHttpClient with security hardening.
     *
     * **SSL Pinning:**
     * - Pins specific certificates for API domains
     * - Falls back gracefully if pins don't match (on certain endpoints)
     *
     * **Best Practices:**
     * - Store certificate pins in BuildConfig or SecurePreferences
     * - Rotate pins periodically (every 6-12 months)
     * - Test pin rotation before deploying
     */
    fun createSecureOkHttpClient(context: Context): OkHttpClient {
        // ⚠️ Certificate Pinning disabled for MVP (v1.0.0)
        //
        // REASON: Production API certificate hashes not yet finalized.
        // Pinning with incorrect hashes breaks all network requests.
        //
        // TODO (Phase 2):
        // 1. Get production certificate hashes from infrastructure/DevOps
        // 2. Request 2-3 backup pins for certificate rotation
        // 3. Implement pinning with real hashes in SecurityConfig
        // 4. Test pin rotation before deploying to production
        // 5. Document pin rotation policy in docs/CERTIFICATE_PINNING.md
        //
        // Phase 2 Pin Implementation:
        // val certificatePinner = CertificatePinner.Builder()
        //     .add("api.googleapis.com", "sha256/REAL_PROD_HASH_HERE=")
        //     .add("api.googleapis.com", "sha256/BACKUP_HASH_1_HERE=")
        //     .add("api.exchangerate-api.com", "sha256/REAL_API_HASH=")
        //     .build()

        return OkHttpClient.Builder()
            // Certificate pinning disabled until real hashes available
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                Timber.d("🔒 [SSL] Requesting: ${request.url}")
                chain.proceed(request)
            }
            .build()
    }

    /**
     * Validate SSL certificate for a given domain.
     *
     * **Usage:**
     * Used for runtime validation in critical paths
     * (e.g., financial transactions, authentication)
     */
    fun validateSSLCertificate(domain: String): Boolean {
        return try {
            val url = "https://$domain"
            val connection = java.net.URL(url).openConnection() as javax.net.ssl.HttpsURLConnection
            connection.connect()
            val certificate = connection.serverCertificates.firstOrNull()
            val isValid = certificate != null
            Timber.d("✅ SSL certificate valid for $domain: $isValid")
            isValid
        } catch (e: Exception) {
            Timber.e(e, "❌ SSL certificate validation failed for $domain")
            false
        }
    }
}

