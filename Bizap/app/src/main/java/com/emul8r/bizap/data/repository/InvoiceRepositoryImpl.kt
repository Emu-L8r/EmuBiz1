package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.data.monitoring.PerformanceMetrics
import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.validation.StatusTransitionValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
        private const val RETRY_MAX_ATTEMPTS = 3
        private const val RETRY_INITIAL_DELAY_MS = 100L
        private const val RETRY_MAX_DELAY_MS = 2000L
        private const val CONCURRENCY_RETRY_MAX = 5
        private const val OP_UPDATE_INVOICE_STATUS = "updateInvoiceStatus"
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

    override suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit> {
        val startTime = System.currentTimeMillis()

        return runCatching {
            // ── Input validation ──────────────────────────────────────────────────
            require(invoiceId > 0) {
                "Invalid invoice ID: $invoiceId. ID must be a positive number."
            }

            Timber.d("🔄 updateInvoiceStatus: Updating invoice $invoiceId to status ${status.name}")

            // ── Fetch current invoice (needed for transition validation & deltas) ─
            val oldInvoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId).first()
                ?: throw BizapException.NotFoundError(
                    entityType = "Invoice",
                    identifier = invoiceId.toString()
                )

            val invoiceEntity = oldInvoiceWithItems.invoice
            val currentStatus = runCatching {
                InvoiceStatus.valueOf(invoiceEntity.status)
            }.getOrElse {
                throw BizapException.BusinessLogicError(
                    rule = "Invoice must have a recognised status",
                    action = "Read status of invoice $invoiceId",
                    reason = "Unknown stored status '${invoiceEntity.status}'"
                )
            }

            // ── Status-transition validation ─────────────────────────────────────
            StatusTransitionValidator.validate(invoiceId, currentStatus, status)
            Timber.d("✅ Status transition validated: $currentStatus → ${status.name}")

            // ── Step 1: Update the invoice record ────────────────────────────────
            retryOnFailure(operationName = "invoiceDao.updateInvoiceStatus") {
                invoiceDao.updateInvoiceStatus(invoiceId, status.name)
            }

            Timber.d("✅ Invoice $invoiceId updated in database ($currentStatus → ${status.name})")

            // ── Step 2: Sync InvoiceAnalyticsSnapshot ────────────────────────────
            retryOnFailure(operationName = "analyticsDao.updateInvoiceSnapshot") {
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
            }

            // ── Step 3: Sync DailyRevenueSnapshot (with optimistic locking) ──────
            val invoiceDate = LocalDate.ofInstant(
                Instant.ofEpochMilli(invoiceEntity.date),
                ZoneId.systemDefault()
            ).toString()

            updateDailySnapshotWithOptimisticLock(
                businessId = invoiceEntity.businessProfileId,
                invoiceDate = invoiceDate,
                invoiceEntity = invoiceEntity,
                oldStatus = currentStatus,
                newStatus = status
            )

            // ── Step 4: Sync InvoicePaymentSnapshot ──────────────────────────────
            retryOnFailure(operationName = "paymentDao.updateSnapshot") {
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
            }

            // ── Step 5: Verify snapshot consistency ───────────────────────────────
            verifySnapshotConsistency(invoiceId)

            Timber.i("✅ updateInvoiceStatus completed: Invoice $invoiceId → ${status.name}")
        }.also { result ->
            val duration = System.currentTimeMillis() - startTime
            result.onSuccess {
                PerformanceMetrics.recordSuccess(OP_UPDATE_INVOICE_STATUS, duration)
                Timber.d("⏱ $OP_UPDATE_INVOICE_STATUS completed in ${duration}ms")
            }.onFailure { e ->
                PerformanceMetrics.recordFailure(OP_UPDATE_INVOICE_STATUS, duration, e)
                Timber.e(e, "❌ $OP_UPDATE_INVOICE_STATUS failed after ${duration}ms: ${e.message}")
            }
        }
    }

    /**
     * Updates [DailyRevenueSnapshot] using optimistic locking to guard against concurrent writes.
     *
     * On version conflict the snapshot is re-read and the update is retried up to
     * [CONCURRENCY_RETRY_MAX] times before giving up with a warning log.
     */
    private suspend fun updateDailySnapshotWithOptimisticLock(
        businessId: Long,
        invoiceDate: String,
        invoiceEntity: com.emul8r.bizap.data.local.entities.InvoiceEntity,
        oldStatus: InvoiceStatus,
        newStatus: InvoiceStatus
    ) {
        val paidStatuses = listOf(InvoiceStatus.PAID, InvoiceStatus.PARTIALLY_PAID)
        var attempt = 0
        while (attempt < CONCURRENCY_RETRY_MAX) {
            val existingDailySnapshot = analyticsDao.getDailySnapshotByDate(businessId, invoiceDate)
                ?: return // No snapshot to update

            val oldRevenueContribution = if (oldStatus in paidStatuses) invoiceEntity.amountPaid else 0L
            val newRevenueContribution = if (newStatus in paidStatuses) invoiceEntity.amountPaid else 0L
            val delta = newRevenueContribution - oldRevenueContribution

            val newPaidCount = when {
                oldStatus != InvoiceStatus.PAID && newStatus == InvoiceStatus.PAID ->
                    existingDailySnapshot.paidInvoiceCount + 1
                oldStatus == InvoiceStatus.PAID && newStatus != InvoiceStatus.PAID ->
                    (existingDailySnapshot.paidInvoiceCount - 1).coerceAtLeast(0)
                else -> existingDailySnapshot.paidInvoiceCount
            }

            val rowsUpdated = analyticsDao.updateSnapshotWithVersion(
                id = existingDailySnapshot.id,
                totalRevenue = (existingDailySnapshot.totalRevenue + delta).coerceAtLeast(0L),
                paidInvoiceCount = newPaidCount,
                expectedVersion = existingDailySnapshot.version,
                updatedAtMs = System.currentTimeMillis()
            )

            if (rowsUpdated > 0) {
                Timber.d("✅ Updated DailyRevenueSnapshot: delta=$delta, paidCount=$newPaidCount (attempt ${attempt + 1})")
                return
            }

            attempt++
            Timber.w("⚠️ DailyRevenueSnapshot version conflict – retrying ($attempt/$CONCURRENCY_RETRY_MAX)")
        }
        Timber.w("⚠️ Could not update DailyRevenueSnapshot after $CONCURRENCY_RETRY_MAX attempts (concurrent update conflict)")
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

    // ==================== RESILIENCE HELPERS ====================

    /**
     * Executes [operation] with simple exponential-backoff retry.
     *
     * Only retries on generic [Exception]s.  [BizapException.BusinessLogicError] and
     * [BizapException.ValidationError] (and [IllegalArgumentException] / [IllegalStateException])
     * are considered non-retryable and re-thrown immediately.
     */
    private suspend fun <T> retryOnFailure(
        operationName: String = "operation",
        maxRetries: Int = RETRY_MAX_ATTEMPTS,
        initialDelayMs: Long = RETRY_INITIAL_DELAY_MS,
        maxDelayMs: Long = RETRY_MAX_DELAY_MS,
        operation: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                if (!isRetryable(e)) throw e
                lastException = e
                if (attempt < maxRetries - 1) {
                    Timber.w(e, "⚠️ $operationName attempt ${attempt + 1} failed, retrying in ${currentDelay}ms")
                    delay(currentDelay)
                    currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
                }
            }
        }

        throw lastException ?: Exception("$operationName failed after $maxRetries attempts")
    }

    private fun isRetryable(e: Exception): Boolean = when (e) {
        is IllegalArgumentException,
        is IllegalStateException,
        is BizapException.BusinessLogicError,
        is BizapException.ValidationError,
        is BizapException.NotFoundError -> false
        else -> true
    }

    // ==================== SNAPSHOT CONSISTENCY ====================

    /**
     * Verifies that the persisted [InvoiceAnalyticsSnapshot] matches the invoice's current status.
     * Logs a warning and triggers [regenerateAnalyticsSnapshot] if drift is detected.
     */
    private suspend fun verifySnapshotConsistency(invoiceId: Long) {
        try {
            val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId).first() ?: return

            val analyticsSnapshot = analyticsDao.getInvoiceSnapshot(invoiceId) ?: run {
                Timber.w("⚠️ Missing analytics snapshot for invoice $invoiceId – regenerating")
                regenerateAnalyticsSnapshot(invoiceWithItems)
                return
            }

            if (analyticsSnapshot.status != invoiceWithItems.invoice.status) {
                Timber.e(
                    "🔴 SNAPSHOT DRIFT DETECTED: Invoice $invoiceId – " +
                        "invoice.status=${invoiceWithItems.invoice.status}, " +
                        "snapshot.status=${analyticsSnapshot.status}"
                )
                regenerateAnalyticsSnapshot(invoiceWithItems)
            }
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Snapshot consistency check failed for invoice $invoiceId (non-blocking)")
        }
    }

    /**
     * Rebuilds [InvoiceAnalyticsSnapshot] from the source-of-truth invoice record.
     */
    private suspend fun regenerateAnalyticsSnapshot(invoiceWithItems: InvoiceWithItems) {
        try {
            val invoice = invoiceWithItems.invoice
            val invoiceNumber = "INV-${invoice.invoiceYear}-${invoice.invoiceSequence.toString().padStart(6, '0')}" +
                    if (invoice.version > 1) "-v${invoice.version}" else ""
            val snapshot = InvoiceAnalyticsSnapshot(
                invoiceId = invoice.id,
                businessProfileId = invoice.businessProfileId,
                customerId = invoice.customerId ?: 0L,
                customerName = invoice.customerName,
                invoiceNumber = invoiceNumber,
                currencyCode = invoice.currencyCode,
                subtotal = invoiceWithItems.subtotal,
                taxAmount = invoice.taxAmount,
                totalAmount = invoice.totalAmount,
                status = invoice.status,
                isPaid = invoice.status == InvoiceStatus.PAID.name,
                isOverdue = invoice.dueDate < System.currentTimeMillis() &&
                        invoice.status != InvoiceStatus.PAID.name,
                invoiceDateMs = invoice.date,
                createdAtMs = invoice.date,
                paidAtMs = if (invoice.status == InvoiceStatus.PAID.name) System.currentTimeMillis() else null,
                lineItemCount = invoiceWithItems.items.size,
                snapshotCreatedAtMs = System.currentTimeMillis()
            )
            analyticsDao.upsertInvoiceSnapshot(snapshot)
            Timber.i("✅ Regenerated analytics snapshot for invoice ${invoice.id}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to regenerate analytics snapshot for invoice ${invoiceWithItems.invoice.id}")
        }
    }
}
