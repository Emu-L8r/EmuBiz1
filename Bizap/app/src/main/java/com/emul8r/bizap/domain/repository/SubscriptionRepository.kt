package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Subscription Repository Interface
 *
 * Defines contracts for subscription/licensing operations.
 * Implementation handles all subscription logic (verification, caching, etc).
 *
 * UI layers and ViewModels should ONLY use this interface, never the DAO.
 */
interface SubscriptionRepository {

    /**
     * Observe current subscription status
     *
     * Emits immediately with cached value, then updates on changes.
     * Use this in ViewModels to drive UI state.
     */
    fun observeSubscriptionStatus(): Flow<SubscriptionStatus>

    /**
     * Check if current user is premium (as a Flow)
     *
     * Convenient shortcut for: observeSubscriptionStatus().map { it.isPremium }
     */
    fun isPremiumUser(): Flow<Boolean>

    /**
     * Upgrade to premium tier (after successful IAP purchase)
     *
     * Call this when Google Play Billing confirms purchase.
     * Updates local database with purchase details.
     */
    suspend fun upgradeToPremium(
        productId: String,
        purchaseToken: String,
        purchaseTimestamp: Long = System.currentTimeMillis()
    ): Result<Unit>

    /**
     * Downgrade to free tier (for testing or subscription cancellation)
     *
     * Typically called when:
     * - User manually cancels subscription in Google Play
     * - Subscription expires
     * - Factory reset is triggered
     */
    suspend fun downgradeToFree(): Result<Unit>

    /**
     * Verify purchase with Google Play Billing
     *
     * Validates purchase token and updates license status.
     * Called during initialization and periodically in background.
     */
    suspend fun verifyPurchaseWithBilling(purchaseToken: String): Result<Boolean>

    /**
     * Reset subscription to default free tier (testing only)
     */
    suspend fun resetSubscription(): Result<Unit>
}


