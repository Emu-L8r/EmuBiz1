package com.emul8r.bizap.domain.model

/**
 * Public domain model for subscription status
 * This is what ViewModels and UIs should use
 *
 * This model is pure Kotlin and has no dependencies on Android or data layer.
 */
data class SubscriptionStatus(
    val tier: String,  // "FREE" or "PREMIUM"
    val isPremium: Boolean,
    val purchaseTimestamp: Long? = null,
    val expiryTimestamp: Long? = null,
    val productId: String? = null
) {
    companion object {
        const val TIER_FREE = "FREE"
        const val TIER_PREMIUM = "PREMIUM"

        fun free() = SubscriptionStatus(
            tier = TIER_FREE,
            isPremium = false
        )

        fun premium(
            purchaseTimestamp: Long,
            expiryTimestamp: Long? = null,
            productId: String? = null
        ) = SubscriptionStatus(
            tier = TIER_PREMIUM,
            isPremium = true,
            purchaseTimestamp = purchaseTimestamp,
            expiryTimestamp = expiryTimestamp,
            productId = productId
        )
    }
}

