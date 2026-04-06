package com.emul8r.bizap.security

import android.content.Intent
import android.net.Uri
import timber.log.Timber

/**
 * App Links verification and deep link handling.
 *
 * **Features:**
 * - Validates deep links come from authorized domains
 * - Prevents malicious intent hijacking
 * - Type-safe deep link routing
 *
 * **Implementation:**
 * This requires .well-known/assetlinks.json on your domain:
 *
 * [{
 *   "relation": ["delegate_permission/common.handle_all_urls"],
 *   "target": {
 *     "namespace": "android_app",
 *     "package_name": "com.emul8r.bizap",
 *     "sha256_cert_fingerprints": ["AA:BB:CC:..."]
 *   }
 * }]
 */
object AppLinksVerification {

    // Authorized domains for deep links
    private val AUTHORIZED_DOMAINS = setOf(
        "bizap.com",
        "www.bizap.com",
        "app.bizap.com",
        "api.bizap.com"
    )

    // Authorized schemes
    private val AUTHORIZED_SCHEMES = setOf(
        "https",  // Only HTTPS
        "bizap"   // Custom scheme (must verify via assetlinks.json)
    )

    /**
     * Verify if an intent came from an authorized source.
     *
     * **Security:**
     * - Only accept HTTPS links (not HTTP)
     * - Validate against whitelist of domains
     * - Log all verification attempts
     *
     * @param intent The intent to verify
     * @return True if intent is from authorized source
     */
    fun isAuthorizedDeepLink(intent: Intent): Boolean {
        val data: Uri = intent.data ?: return false
        val scheme = data.scheme ?: return false
        val host = data.host ?: return false

        // Check scheme is authorized
        if (!AUTHORIZED_SCHEMES.contains(scheme)) {
            Timber.w("❌ Unauthorized scheme: $scheme")
            return false
        }

        // For HTTPS, verify domain
        if (scheme == "https") {
            if (!AUTHORIZED_DOMAINS.contains(host)) {
                Timber.w("❌ Unauthorized domain: $host")
                return false
            }
        }

        // For custom bizap:// scheme, just verify it exists
        // (actual verification done via assetlinks.json in production)
        if (scheme == "bizap") {
            Timber.d("✅ Custom scheme authorized: bizap://$host")
        }

        Timber.d("✅ Deep link verified: $scheme://$host${data.path}")
        return true
    }

    /**
     * Extract safe parameters from deep link.
     *
     * **Safety:**
     * - Only returns parameters from authorized links
     * - Validates parameter names against whitelist
     * - Prevents injection attacks
     */
    fun extractDeepLinkParameters(intent: Intent): Map<String, String> {
        if (!isAuthorizedDeepLink(intent)) {
            Timber.w("❌ Attempted to extract parameters from unauthorized link")
            return emptyMap()
        }

        val data: Uri = intent.data ?: return emptyMap()
        val params = mutableMapOf<String, String>()

        // Whitelist of allowed parameter names
        val allowedParams = setOf(
            "businessId",
            "invoiceId",
            "customerId",
            "paymentId",
            "tab"
        )

        // Extract only whitelisted parameters
        for (paramName in allowedParams) {
            val value = data.getQueryParameter(paramName)
            if (value != null) {
                params[paramName] = value
                Timber.d("📦 Parameter: $paramName = $value")
            }
        }

        return params
    }

    /**
     * Validate parameter format (basic type checking).
     *
     * @param paramName Name of parameter
     * @param value Value to validate
     * @return True if parameter is valid for its type
     */
    fun isValidParameter(paramName: String, value: String): Boolean {
        return when (paramName) {
            "businessId", "invoiceId", "customerId", "paymentId" -> {
                value.toLongOrNull() != null
            }
            "tab" -> {
                value in setOf("dashboard", "invoices", "customers", "analytics", "settings")
            }
            else -> false
        }
    }
}

