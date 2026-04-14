package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.emul8r.bizap.data.local.entities.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Subscription DAO
 *
 * Manages local subscription/license storage. Since there's only one subscription
 * per app installation, queries use a fixed ID = 1L.
 *
 * All queries return Flow<> to enable reactive UI updates.
 */
@Dao
interface SubscriptionDao {

    /**
     * Observe current subscription status
     *
     * Returns a Flow that emits the subscription entity whenever it changes.
     * Completes with last known value if not cached.
     */
    @Query("SELECT * FROM subscriptions WHERE id = 1")
    fun observeSubscription(): Flow<SubscriptionEntity?>

    /**
     * Get subscription synchronously (avoid blocking calls)
     *
     * For initialization only. Prefer observeSubscription() in UI/ViewModel.
     */
    @Query("SELECT * FROM subscriptions WHERE id = 1")
    suspend fun getSubscriptionSync(): SubscriptionEntity?

    /**
     * Update subscription after purchase
     *
     * Used to store newly purchased license status.
     */
    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity): Int

    /**
     * Insert or update subscription (on first-run)
     *
     * Called during app initialization if subscription doesn't exist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubscription(subscription: SubscriptionEntity)

    /**
     * Delete subscription (for testing or factory reset)
     */
    @Query("DELETE FROM subscriptions WHERE id = 1")
    suspend fun deleteSubscription()

    /**
     * Check if user is premium (synchronous)
     *
     * Use this sparingly. Prefer observeSubscription().map { it?.tier == "PREMIUM" }
     * for reactive checks in UI.
     */
    @Query("SELECT tier FROM subscriptions WHERE id = 1")
    suspend fun getPremiumStatus(): String?
}

