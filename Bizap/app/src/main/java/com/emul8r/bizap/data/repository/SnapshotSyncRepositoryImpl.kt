package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.domain.repository.SnapshotSyncRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data layer implementation of SnapshotSyncRepository domain interface.
 *
 * SPRINT 3: Created to satisfy Hilt DI and allow UseCases to depend on
 * domain SnapshotSyncRepository interface instead of data SnapshotSyncHelper directly.
 */
@Singleton
class SnapshotSyncRepositoryImpl @Inject constructor(
    private val snapshotSyncHelper: SnapshotSyncHelper
) : SnapshotSyncRepository {

    override suspend fun syncAllSnapshots(invoiceEntity: Any, businessId: Long) {
        // Type-safe casting: only sync if it's actually an InvoiceEntity
        if (invoiceEntity is InvoiceEntity) {
            snapshotSyncHelper.syncAllSnapshots(invoiceEntity, businessId)
        }
    }
}


