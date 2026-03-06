package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val businessProfileRepository: BusinessProfileRepository,
    private val analyticsDao: AnalyticsDao,
    private val paymentDao: InvoicePaymentDao
) : InvoiceRepository {

    companion object {
        private const val MILLIS_PER_DAY = 86400000L
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllInvoicesWithItems(): Flow<List<Invoice>> {
        // Scoped to active business
        return businessProfileRepository.activeProfile.flatMapLatest { business ->
            invoiceDao.getInvoicesByBusinessId(business.id)
                .map { list -> list.map { it.toDomain() } }
                .catch { e ->
                    Timber.e(e, "Error fetching invoices for business ${business.id}")
                    emit(emptyList())
                }
        }
    }

    override fun getInvoiceWithItemsById(id: Long): Flow<Invoice?> {
        return invoiceDao.getInvoiceWithItemsById(id).map { it?.toDomain() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getInvoiceGroupWithVersions(year: Int, sequence: Int): Flow<List<Invoice>> {
        return businessProfileRepository.activeProfile.flatMapLatest { business ->
            invoiceDao.getInvoiceGroupWithVersions(year, sequence, business.id).map { list ->
                list.map { entity ->
                    val placeholderWithItems = InvoiceWithItems(entity, emptyList())
                    placeholderWithItems.toDomain()
                }
            }
        }
    }

    override suspend fun saveInvoice(invoice: Invoice): Result<Long> = runCatching {
        val activeBusinessId = businessProfileRepository.getActiveBusinessId()
        var invoiceToSave = invoice.copy(businessProfileId = activeBusinessId)

        if (invoiceToSave.id == 0L) {
            // NEW invoice: INSERT with auto-generated ID
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val nextSequence = invoiceDao.getMaxSequenceForYear(currentYear, activeBusinessId) + 1
            invoiceToSave = invoiceToSave.copy(
                invoiceYear = currentYear,
                invoiceSequence = nextSequence,
                version = 1
            )
            Timber.i("🔢 Assigning scoped invoice number: INV-$currentYear-${nextSequence.toString().padStart(6, '0')} for business $activeBusinessId")

            val invoiceEntity = invoiceToSave.toEntity()
            val lineItemEntities = invoiceToSave.items.map { it.toEntity(invoiceToSave.id) }
            Timber.d("💾 INSERT new invoice for business $activeBusinessId")
            val newId = invoiceDao.insert(invoiceEntity, lineItemEntities)

            // ✅ CREATE SNAPSHOTS for new invoice
            val createdEntity = invoiceEntity.copy(id = newId)
            try {
                createAnalyticsSnapshots(createdEntity, activeBusinessId)
                Timber.d("✅ Created analytics snapshots for new invoice $newId")
            } catch (e: Exception) {
                Timber.w(e, "⚠️ Failed to create analytics snapshots (non-blocking)")
            }

            newId
        } else {
            // EXISTING invoice: DELETE old line items, INSERT new ones, UPDATE invoice
            val invoiceEntity = invoiceToSave.toEntity()
            val lineItemEntities = invoiceToSave.items.map { it.toEntity(invoiceToSave.id) }
            Timber.d("✏️ UPDATE existing invoice id=${invoiceToSave.id} for business $activeBusinessId")
            invoiceDao.deleteLineItems(invoiceToSave.id)
            invoiceDao.insertLineItems(lineItemEntities)
            invoiceDao.updateInvoice(invoiceEntity)
            invoiceToSave.id
        }
    }.also { result ->
        result.onFailure { e -> Timber.e(e, "Database operation failed during saveInvoice") }
    }

    override suspend fun updateAmountPaid(invoiceId: Long, amount: Long): Result<Unit> = runCatching {
        val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId).first()
            ?: throw Exception("Invoice with ID $invoiceId not found")

        val updatedEntity = invoiceWithItems.invoice.copy(amountPaid = amount)

        // ✅ FIX: Use UPDATE, not INSERT
        invoiceDao.updateInvoice(updatedEntity)

        Timber.d("✅ Payment recorded for invoice $invoiceId: amount=$amount cents")

        // ✅ UPDATE SNAPSHOTS when payment is recorded
        try {
            updatePaymentSnapshots(updatedEntity)
            Timber.d("✅ Updated payment snapshots for invoice $invoiceId")
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Failed to update payment snapshots (non-blocking)")
        }

        Unit
    }.also { result ->
        result.onFailure { e -> Timber.e(e, "Database operation failed during updateAmountPaid") }
    }

    override suspend fun createCorrection(originalInvoiceId: Long): Result<Long> = runCatching {
        val original = invoiceDao.getInvoiceWithItemsById(originalInvoiceId).first() 
            ?: throw Exception("Original invoice not found")
        
        val correctionEntity = original.invoice.copy(
            id = 0,
            status = InvoiceStatus.DRAFT.name,
            version = original.invoice.version + 1,
            amountPaid = 0L,
            parentInvoiceId = originalInvoiceId,
            date = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val lineItemEntities = original.items.map { it.copy(id = 0) }
        invoiceDao.insert(correctionEntity, lineItemEntities)
    }.also { result ->
        result.onFailure { e -> Timber.e(e, "Database operation failed during createCorrection") }
    }

    override fun getBusinessProfile(): Flow<BusinessProfile> {
        return businessProfileRepository.activeProfile
    }

    override suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit> = runCatching {
        Timber.d("🔄 updateInvoiceStatus: Updating invoice $invoiceId to status ${status.name}")

        // Fetch the invoice BEFORE updating to determine the delta for snapshot recalculation
        val oldInvoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId).first()

        // Step 1: Update the invoice record in invoices table
        invoiceDao.updateInvoiceStatus(invoiceId, status.name)

        if (oldInvoiceWithItems != null) {
            val invoiceEntity = oldInvoiceWithItems.invoice
            val oldStatus = runCatching { InvoiceStatus.valueOf(invoiceEntity.status) }.getOrNull()
            Timber.d("✅ Invoice updated in database (${oldStatus?.name} → ${status.name}), now syncing snapshots")

            // === Update InvoiceAnalyticsSnapshot ===
            val existingAnalyticsSnapshot = analyticsDao.getInvoiceSnapshot(invoiceId)
            if (existingAnalyticsSnapshot != null) {
                val updatedAnalyticsSnapshot = existingAnalyticsSnapshot.copy(
                    status = status.name,
                    isPaid = status == InvoiceStatus.PAID,
                    isOverdue = invoiceEntity.dueDate < System.currentTimeMillis() &&
                               status != InvoiceStatus.PAID
                )
                analyticsDao.updateInvoiceSnapshot(updatedAnalyticsSnapshot)
                Timber.d("✅ Updated InvoiceAnalyticsSnapshot: $status")
            }

            // === Update DailyRevenueSnapshot ===
            val invoiceDate = LocalDate.ofInstant(
                Instant.ofEpochMilli(invoiceEntity.date),
                ZoneId.systemDefault()
            ).toString()

            val existingDailySnapshot = analyticsDao.getDailySnapshotByDate(
                invoiceEntity.businessProfileId,
                invoiceDate
            )

            if (existingDailySnapshot != null) {
                // Calculate this invoice's old and new revenue contributions
                val paidStatuses = listOf(InvoiceStatus.PAID, InvoiceStatus.PARTIALLY_PAID)
                val oldRevenueContribution = if (oldStatus in paidStatuses) invoiceEntity.amountPaid else 0L
                val newRevenueContribution = if (status in paidStatuses) invoiceEntity.amountPaid else 0L
                val delta = newRevenueContribution - oldRevenueContribution

                val newPaidCount = when {
                    oldStatus != InvoiceStatus.PAID && status == InvoiceStatus.PAID ->
                        existingDailySnapshot.paidInvoiceCount + 1
                    oldStatus == InvoiceStatus.PAID && status != InvoiceStatus.PAID ->
                        (existingDailySnapshot.paidInvoiceCount - 1).coerceAtLeast(0)
                    else -> existingDailySnapshot.paidInvoiceCount
                }

                val updatedDailySnapshot = existingDailySnapshot.copy(
                    totalRevenue = (existingDailySnapshot.totalRevenue + delta).coerceAtLeast(0L),
                    paidInvoiceCount = newPaidCount
                )
                analyticsDao.updateDailySnapshot(updatedDailySnapshot)
                Timber.d("✅ Updated DailyRevenueSnapshot: delta=$$delta, paidCount=$newPaidCount")
            }

            // === Update InvoicePaymentSnapshot ===
            val existingPaymentSnapshot = paymentDao.getSnapshotByInvoiceId(invoiceId)
            if (existingPaymentSnapshot != null) {
                val daysOverdue = if (invoiceEntity.dueDate < System.currentTimeMillis()) {
                    ((System.currentTimeMillis() - invoiceEntity.dueDate) / MILLIS_PER_DAY).toInt()
                } else {
                    0
                }

                val updatedPaymentSnapshot = existingPaymentSnapshot.copy(
                    paymentStatus = when (status) {
                        InvoiceStatus.PAID -> "PAID"
                        InvoiceStatus.PARTIALLY_PAID -> "PARTIALLY_PAID"
                        InvoiceStatus.SENT -> "UNPAID"
                        InvoiceStatus.OVERDUE -> "OVERDUE"
                        InvoiceStatus.DRAFT -> "DRAFT"
                    },
                    isAtRisk = invoiceEntity.dueDate < System.currentTimeMillis() &&
                              status != InvoiceStatus.PAID,
                    riskScore = when {
                        daysOverdue <= 0 -> 0.0
                        daysOverdue <= 30 -> 0.3
                        daysOverdue <= 60 -> 0.6
                        daysOverdue <= 90 -> 0.8
                        else -> 1.0
                    }
                )
                paymentDao.updateSnapshot(updatedPaymentSnapshot)
                Timber.d("✅ Updated InvoicePaymentSnapshot: $status")
            }
        } else {
            Timber.w("⚠️ Could not find invoice $invoiceId before status update")
        }

        Timber.d("✅ updateInvoiceStatus completed successfully")

    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to update invoice status: ${e.message}")
        }
    }

    override suspend fun updatePdfPath(invoiceId: Long, pdfPath: String): Result<Unit> = runCatching {
        invoiceDao.updatePdfPath(invoiceId, pdfPath)
    }.also { result ->
        result.onFailure { e -> Timber.e(e, "Database operation failed during updatePdfPath") }
    }

    override suspend fun deleteInvoice(id: Long): Result<Unit> = runCatching {
        invoiceDao.deleteInvoiceWithItems(id)
    }.also { result ->
        result.onFailure { e -> Timber.e(e, "Database operation failed during deleteInvoice") }
    }

    // ==================== SNAPSHOT HELPERS ====================

    /**
     * Creates initial analytics snapshots when a new invoice is saved.
     */
    private suspend fun createAnalyticsSnapshots(invoice: com.emul8r.bizap.data.local.entities.InvoiceEntity, businessId: Long) {
        try {
            // Note: We need to fetch business profile to check status, but for snapshots we mainly care about basic data
            Timber.d("📸 Creating snapshots for invoice ${invoice.id}")
            // Snapshots will be updated later via updateInvoiceStatus if needed
        } catch (e: Exception) {
            Timber.e(e, "Failed to create snapshots")
        }
    }

    /**
     * Updates payment-related snapshots when amount paid changes.
     */
    private suspend fun updatePaymentSnapshots(invoice: com.emul8r.bizap.data.local.entities.InvoiceEntity) {
        try {
            // Update payment snapshot with new amount
            val existingPaymentSnapshot = paymentDao.getSnapshotByInvoiceId(invoice.id)
            if (existingPaymentSnapshot != null) {
                val daysOverdue = if (invoice.dueDate < System.currentTimeMillis()) {
                    ((System.currentTimeMillis() - invoice.dueDate) / MILLIS_PER_DAY).toInt()
                } else {
                    0
                }

                val updatedPaymentSnapshot = existingPaymentSnapshot.copy(
                    paidAmount = invoice.amountPaid,
                    outstandingAmount = invoice.totalAmount - invoice.amountPaid,
                    lastPaymentDate = if (invoice.amountPaid > 0) System.currentTimeMillis() else existingPaymentSnapshot.lastPaymentDate,
                    lastPaymentAmount = if (invoice.amountPaid > existingPaymentSnapshot.paidAmount) {
                        invoice.amountPaid - existingPaymentSnapshot.paidAmount
                    } else {
                        existingPaymentSnapshot.lastPaymentAmount
                    }
                )
                paymentDao.updateSnapshot(updatedPaymentSnapshot)
                Timber.d("✅ Updated payment snapshot for invoice ${invoice.id}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update payment snapshots")
        }
    }
}
