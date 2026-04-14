package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Subscription/Licensing Entity
 *
 * Stores user subscription status locally (encrypted via SQLCipher).
 * All users start as free-tier; premium status is set after in-app purchase.
 *
 * This entity represents the local license record. Do NOT expose to UI directly;
 * use SubscriptionRepository instead.
 */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 1L,  // Always 1 (single subscription per app installation)

    // License tier: "FREE" or "PREMIUM"
    val tier: String = "FREE",

    // Purchase timestamp (seconds since epoch)
    val purchaseTimestamp: Long? = null,

    // Expiry timestamp (null = no expiry for premium)
    val expiryTimestamp: Long? = null,

    // In-app billing product ID (e.g., "bizap_premium_monthly")
    val productId: String? = null,

    // Google Play purchase token (for verification/cancellation)
    val purchaseToken: String? = null,

    // Last sync timestamp with billing service
    val lastBillingSyncTimestamp: Long? = null,

    // Human-readable status for debugging
    val statusDescription: String = "Free tier"
) {
    companion object {
        const val TIER_FREE = "FREE"
        const val TIER_PREMIUM = "PREMIUM"
    }
}


