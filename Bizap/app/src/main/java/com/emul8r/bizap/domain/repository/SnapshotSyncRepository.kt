package com.emul8r.bizap.domain.repository

/**
 * Domain interface for snapshot synchronization.
 * Abstracts the data layer's snapshot sync functionality.
 *
 * SPRINT 3: Created to allow domain use cases to depend on domain abstractions,
 * not data layer implementations.
 */
interface SnapshotSyncRepository {
    /**
     * Synchronize all snapshots for a given invoice entity and business context.
     */
    suspend fun syncAllSnapshots(invoiceEntity: Any, businessId: Long)
}

