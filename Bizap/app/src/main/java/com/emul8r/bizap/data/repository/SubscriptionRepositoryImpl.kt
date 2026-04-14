package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.SubscriptionDao
import com.emul8r.bizap.data.local.entities.SubscriptionEntity
import com.emul8r.bizap.domain.model.SubscriptionStatus
import com.emul8r.bizap.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Subscription Repository Implementation
 *
 * Manages all subscription/licensing logic:
 * - Caches subscription status locally
 * - Converts between entity and domain models
 * - Handles purchase upgrades and verifications
 * - Integrates with Google Play Billing (via BillingManager, injected separately)
 *
 * Note: IAP verification is delegated to BillingManager (not in this class).
 * This repo focuses on local cache management.
 */
@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) : SubscriptionRepository {

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> {
        return subscriptionDao.observeSubscription()
            .map { entity ->
                entity?.let { mapEntityToDomain(it) }
                    ?: SubscriptionStatus.free()  // Default to free if not found
            }
    }

    override fun isPremiumUser(): Flow<Boolean> {
        return observeSubscriptionStatus()
            .map { it.isPremium }
    }

    override suspend fun upgradeToPremium(
        productId: String,
        purchaseToken: String,
        purchaseTimestamp: Long
    ): Result<Unit> = try {
        Timber.d("Upgrading to premium: productId=$productId")

        val premiumEntity = SubscriptionEntity(
            id = 1L,
            tier = SubscriptionEntity.TIER_PREMIUM,
            purchaseTimestamp = purchaseTimestamp,
            expiryTimestamp = null,  // No expiry for permanent premium
            productId = productId,
            purchaseToken = purchaseToken,
            lastBillingSyncTimestamp = System.currentTimeMillis(),
            statusDescription = "Premium (purchased: $productId)"
        )

        subscriptionDao.updateSubscription(premiumEntity)
        Timber.i("Premium upgrade complete")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Failed to upgrade to premium")
        Result.failure(e)
    }

    override suspend fun downgradeToFree(): Result<Unit> = try {
        Timber.d("Downgrading to free tier")

        val freeEntity = SubscriptionEntity(
            id = 1L,
            tier = SubscriptionEntity.TIER_FREE,
            purchaseTimestamp = null,
            expiryTimestamp = null,
            productId = null,
            purchaseToken = null,
            lastBillingSyncTimestamp = System.currentTimeMillis(),
            statusDescription = "Downgraded to free"
        )

        subscriptionDao.updateSubscription(freeEntity)
        Timber.i("Downgrade to free complete")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Failed to downgrade to free")
        Result.failure(e)
    }

    override suspend fun verifyPurchaseWithBilling(purchaseToken: String): Result<Boolean> {
        // Stubbed for now - actual verification is done via BillingManager
        // This would call BillingManager.verifyPurchaseToken(purchaseToken)
        Timber.d("Verifying purchase token (stubbed): $purchaseToken")
        return Result.success(true)  // TODO: Integrate with actual BillingManager
    }

    override suspend fun resetSubscription(): Result<Unit> = try {
        Timber.w("Resetting subscription to free (testing only)")
        subscriptionDao.deleteSubscription()
        subscriptionDao.insertOrUpdateSubscription(
            SubscriptionEntity(
                id = 1L,
                tier = SubscriptionEntity.TIER_FREE
            )
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Failed to reset subscription")
        Result.failure(e)
    }

    /**
     * Convert database entity to domain model
     */
    private fun mapEntityToDomain(entity: SubscriptionEntity): SubscriptionStatus {
        return when (entity.tier) {
            SubscriptionEntity.TIER_PREMIUM -> SubscriptionStatus.premium(
                purchaseTimestamp = entity.purchaseTimestamp ?: System.currentTimeMillis(),
                expiryTimestamp = entity.expiryTimestamp,
                productId = entity.productId
            )
            else -> SubscriptionStatus.free()
        }
    }
}



