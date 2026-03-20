package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service that rebuilds all analytics snapshots from scratch using the current invoice data.
 *
 * Use this when snapshots are missing or inconsistent with the actual invoice records.
 * The rebuild is idempotent: it clears existing snapshots for the business and recreates
 * them from every invoice stored in the database.
 */
@Singleton
class SnapshotRebuildService @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val analyticsDao: AnalyticsDao,
    private val paymentDao: InvoicePaymentDao,
    private val snapshotSyncHelper: SnapshotSyncHelper,
    private val businessProfileRepository: BusinessProfileRepository
) {

    /**
     * Rebuilds all invoice analytics, daily revenue, and payment snapshots for the active business.
     *
     * @return [RebuildReport] with counts of snapshots created.
     */
    suspend fun rebuildAllSnapshots(): RebuildReport {
        Timber.d("🔄 Starting full snapshot rebuild...")

        val businessProfile = businessProfileRepository.activeProfile.first()
        val businessId = businessProfile.id

        val invoicesWithItems = invoiceDao.getInvoicesByBusinessId(businessId).first()
        Timber.d("  Found ${invoicesWithItems.size} invoices to process")

        // Clear existing snapshots so the additive sync starts from zero
        analyticsDao.deleteAllInvoiceSnapshots(businessId)
        analyticsDao.deleteAllDailySnapshots(businessId)
        paymentDao.deleteAllSnapshots(businessId)
        Timber.d("  ✅ Cleared existing snapshots")

        var syncedCount = 0
        invoicesWithItems.forEach { invoiceWithItems ->
            try {
                snapshotSyncHelper.syncAllSnapshots(invoiceWithItems.invoice, businessId)
                syncedCount++
            } catch (e: Exception) {
                Timber.e(e, "  ❌ Failed to sync invoice ${invoiceWithItems.invoice.id}")
            }
        }

        Timber.d("  ✅ Rebuild complete: $syncedCount/${invoicesWithItems.size} invoices synced")

        return RebuildReport(
            invoicesProcessed = invoicesWithItems.size,
            snapshotsSynced = syncedCount
        )
    }
}

data class RebuildReport(
    val invoicesProcessed: Int,
    val snapshotsSynced: Int
)
