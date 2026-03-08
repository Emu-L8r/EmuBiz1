package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.data.monitoring.PerformanceMetrics
import com.emul8r.bizap.data.remote.api.InvoiceApi
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
    private val paymentDao: InvoicePaymentDao,
    private val snapshotSyncHelper: SnapshotSyncHelper,
    private val invoiceApi: InvoiceApi
) : InvoiceRepository {

    companion object {
        private const val MILLIS_PER_DAY = 86400000L
        private const val RETRY_MAX_ATTEMPTS = 3
        private const val RETRY_INITIAL_DELAY_MS = 100L
        private const val RETRY_MAX_DELAY_MS = 2000L
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
                Timber.e(e, "❌ CRITICAL: Failed to create snapshots for invoice $newId")
                throw e
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

        // Step 1: Update invoices table
        invoiceDao.updateInvoice(updatedEntity)
        Timber.d("✅ Payment recorded for invoice $invoiceId: amount=$amount cents")

        // Step 2: Sync payment snapshots (with fallback to create if missing)
        try {
            val existingPaymentSnapshot = paymentDao.getSnapshotByInvoiceId(invoiceId)

            if (existingPaymentSnapshot != null) {
                // ✅ Snapshot exists: update it with new payment data
                updatePaymentSnapshots(updatedEntity)
                Timber.d("✅ Updated existing payment snapshot for invoice $invoiceId")
            } else {
                // ⚠️ Snapshot missing: create it as fallback
                Timber.e("⚠️ Payment snapshot missing for invoice $invoiceId, creating fallback")
                createPaymentSnapshot(updatedEntity)
                Timber.d("✅ Created missing payment snapshot (fallback) for invoice $invoiceId")
            }
        } catch (e: Exception) {
            // 🚨 CRITICAL: Expose the exception so we can debug outstanding amount calculation
            Timber.e(e, "❌ CRITICAL: Failed to sync payment snapshots for invoice $invoiceId")
            throw e  // ← Re-throw to expose the actual error in logs
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
            val analyticsSnapshot = analyticsDao.getInvoiceSnapshot(invoiceId)
            if (analyticsSnapshot != null) {
                analyticsDao.updateInvoiceSnapshot(
                    analyticsSnapshot.copy(
                        status = status.name,
                        isPaid = status == InvoiceStatus.PAID || status == InvoiceStatus.PARTIALLY_PAID,
                        isOverdue = invoiceEntity.dueDate < System.currentTimeMillis() &&
                                status != InvoiceStatus.PAID,
                        snapshotCreatedAtMs = System.currentTimeMillis()
                    )
                )
                Timber.d("✅ Updated InvoiceAnalyticsSnapshot for invoice $invoiceId")
            }

            // ── Step 3: Sync DailyRevenueSnapshot (optimistic locking) ───────────
            val invoiceDate = Instant.ofEpochMilli(invoiceEntity.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString()
            analyticsDao.updateDailySnapshotWithOptimisticLock(
                businessId = invoiceEntity.businessProfileId,
                invoiceDate = invoiceDate,
                invoiceEntity = invoiceEntity,
                oldStatus = currentStatus,
                newStatus = status
            )

            // ── Step 4: Sync InvoicePaymentSnapshot ──────────────────────────────
            val paymentSnapshot = paymentDao.getSnapshotByInvoiceId(invoiceId)
            if (paymentSnapshot != null) {
                val daysOverdue = if (invoiceEntity.dueDate < System.currentTimeMillis()) {
                    ((System.currentTimeMillis() - invoiceEntity.dueDate) / MILLIS_PER_DAY).toInt()
                } else 0
                paymentDao.updateSnapshot(
                    paymentSnapshot.copy(
                        paymentStatus = when {
                            status == InvoiceStatus.PAID -> "PAID"
                            status == InvoiceStatus.PARTIALLY_PAID -> "PARTIALLY_PAID"
                            status == InvoiceStatus.SENT -> "UNPAID"
                            status == InvoiceStatus.OVERDUE -> "OVERDUE"
                            else -> "UNPAID"
                        },
                        daysOverdue = daysOverdue,
                        isAtRisk = invoiceEntity.dueDate < System.currentTimeMillis() &&
                                status != InvoiceStatus.PAID,
                        lastUpdatedMs = System.currentTimeMillis()
                    )
                )
                Timber.d("✅ Updated InvoicePaymentSnapshot for invoice $invoiceId")
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

    override suspend fun updatePdfPath(invoiceId: Long, pdfPath: String): Result<Unit> = runCatching {
        invoiceDao.updatePdfPath(invoiceId, pdfPath)
    }.also { result ->
        result.onFailure { e -> Timber.e(e, "Database operation failed during updatePdfPath") }
    }

    override suspend fun deleteInvoice(id: Long): Result<Unit> = runCatching {
        Timber.d("🗑️ Deleting invoice $id and associated snapshots")

        try {
            // Step 1: Delete individual invoice snapshots
            // (These are specific to this invoice and should be cleaned up)
            analyticsDao.deleteInvoiceSnapshot(id)
            Timber.d("✅ Deleted InvoiceAnalyticsSnapshot for invoice $id")

            paymentDao.deleteSnapshotByInvoiceId(id)
            Timber.d("✅ Deleted InvoicePaymentSnapshot for invoice $id")

            // Step 2: Delete the invoice and line items
            // (Do this after snapshots in case there are FK constraints)
            invoiceDao.deleteInvoiceWithItems(id)
            Timber.d("✅ Deleted invoice $id and line items")

            // Note: We intentionally do NOT delete DailyRevenueSnapshot
            // Reason: It's aggregate daily data (historical record)
            // Deleting one invoice shouldn't erase daily revenue records
            // The aggregates are still valid for historical reporting
            Timber.d("ℹ️ DailyRevenueSnapshot kept (aggregate historical data)")

        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to delete snapshots for invoice $id")
            throw e  // Re-throw to trigger onFailure handler
        }

        Unit
    }.also { result ->
        result.onFailure { e -> Timber.e(e, "Database operation failed during deleteInvoice") }
    }

    // --- PHASE 2: Remote Sync ---

    override suspend fun createInvoiceRemote(invoice: Invoice): Result<Invoice> = runCatching {
        val response = invoiceApi.createInvoice(invoice)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun updateInvoiceRemote(invoice: Invoice): Result<Invoice> = runCatching {
        val response = invoiceApi.updateInvoice(invoice.id, invoice, invoice.updatedAt)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun deleteInvoiceRemote(id: Long): Result<Unit> = runCatching {
        val response = invoiceApi.deleteInvoice(id)
        if (!response.isSuccessful) {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getInvoiceRemote(id: Long): Result<Invoice> = runCatching {
        val response = invoiceApi.getInvoice(id)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun recordPaymentRemote(
        invoiceId: Long,
        amount: Long,
        paymentDate: Long,
        notes: String?
    ): Result<Unit> = runCatching {
        val response = invoiceApi.recordPayment(invoiceId, amount, paymentDate, notes)
        if (!response.isSuccessful) {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }

    // ==================== SNAPSHOT HELPERS ====================

    /**
     * Creates initial analytics snapshots when a new invoice is saved.
     * Calls DAO methods directly to ensure testability and visibility of failures.
     */
    private suspend fun createAnalyticsSnapshots(
        invoice: com.emul8r.bizap.data.local.entities.InvoiceEntity,
        businessProfileId: Long
    ) {
        val computedInvoiceNumber = "INV-${invoice.invoiceYear}-${invoice.invoiceSequence.toString().padStart(6, '0')}"
        val paidStatuses = listOf(InvoiceStatus.PAID.name, InvoiceStatus.PARTIALLY_PAID.name)

        // 1. Create or update InvoiceAnalyticsSnapshot
        val existingAnalyticsSnapshot = analyticsDao.getInvoiceSnapshot(invoice.id)
        if (existingAnalyticsSnapshot != null) {
            analyticsDao.updateInvoiceSnapshot(
                existingAnalyticsSnapshot.copy(
                    status = invoice.status,
                    isPaid = invoice.status in paidStatuses,
                    isOverdue = invoice.dueDate < System.currentTimeMillis() &&
                            invoice.status != InvoiceStatus.PAID.name,
                    snapshotCreatedAtMs = System.currentTimeMillis()
                )
            )
        } else {
            analyticsDao.insertInvoiceSnapshot(
                InvoiceAnalyticsSnapshot(
                    invoiceId = invoice.id,
                    businessProfileId = businessProfileId,
                    customerId = invoice.customerId ?: 0L,
                    customerName = invoice.customerName,
                    invoiceNumber = computedInvoiceNumber,
                    currencyCode = invoice.currencyCode,
                    subtotal = invoice.totalAmount - invoice.taxAmount,
                    taxAmount = invoice.taxAmount,
                    totalAmount = invoice.totalAmount,
                    status = invoice.status,
                    isPaid = invoice.status in paidStatuses,
                    isOverdue = invoice.dueDate < System.currentTimeMillis() &&
                            invoice.status != InvoiceStatus.PAID.name,
                    invoiceDateMs = invoice.date,
                    createdAtMs = invoice.updatedAt,
                    paidAtMs = if (invoice.status == InvoiceStatus.PAID.name) System.currentTimeMillis() else null,
                    lineItemCount = 1,
                    snapshotCreatedAtMs = System.currentTimeMillis()
                )
            )
        }
        Timber.d("✅ Synced InvoiceAnalyticsSnapshot for invoice ${invoice.id}")

        // 2. Create or update DailyRevenueSnapshot
        val dateString = Instant.ofEpochMilli(invoice.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString()
        val revenueContribution = if (invoice.status in paidStatuses) invoice.amountPaid else 0L
        val existingDailySnapshot = analyticsDao.getDailySnapshotByDate(businessProfileId, dateString)
        if (existingDailySnapshot != null) {
            analyticsDao.updateDailySnapshot(
                existingDailySnapshot.copy(
                    totalRevenue = existingDailySnapshot.totalRevenue + revenueContribution,
                    invoiceCount = existingDailySnapshot.invoiceCount + 1,
                    paidInvoiceCount = existingDailySnapshot.paidInvoiceCount +
                            if (invoice.status == InvoiceStatus.PAID.name) 1 else 0,
                    snapshotCreatedAtMs = System.currentTimeMillis()
                )
            )
        } else {
            analyticsDao.insertDailySnapshot(
                DailyRevenueSnapshot(
                    businessProfileId = businessProfileId,
                    dateString = dateString,
                    dateMs = invoice.date,
                    totalRevenue = revenueContribution,
                    invoiceCount = 1,
                    paidInvoiceCount = if (invoice.status == InvoiceStatus.PAID.name) 1 else 0,
                    currencyBreakdown = """{"${invoice.currencyCode}": $revenueContribution}""",
                    snapshotCreatedAtMs = System.currentTimeMillis()
                )
            )
        }
        Timber.d("✅ Synced DailyRevenueSnapshot for invoice ${invoice.id} on $dateString")

        // 3. Create or update InvoicePaymentSnapshot
        val daysOverdue = if (invoice.dueDate < System.currentTimeMillis()) {
            ((System.currentTimeMillis() - invoice.dueDate) / MILLIS_PER_DAY).toInt()
        } else 0
        val paymentStatusStr = when (invoice.status) {
            InvoiceStatus.PAID.name -> "PAID"
            InvoiceStatus.PARTIALLY_PAID.name -> "PARTIALLY_PAID"
            InvoiceStatus.SENT.name -> "UNPAID"
            InvoiceStatus.OVERDUE.name -> "OVERDUE"
            else -> "UNPAID"
        }
        val existingPaymentSnapshot = paymentDao.getSnapshotByInvoiceId(invoice.id)
        if (existingPaymentSnapshot != null) {
            // ✅ SAFE: Type-safe calculation of outstanding amount
            val totalAmount: Long = invoice.totalAmount ?: 0L
            val amountPaid: Long = invoice.amountPaid ?: 0L
            val outstandingAmount: Long = (totalAmount - amountPaid).coerceAtLeast(0L)

            // ✅ Validation: Check for logical errors
            if (amountPaid > totalAmount) {
                Timber.e("⚠️ Warning: Payment ($amountPaid) exceeds total ($totalAmount) for invoice ${invoice.id}")
            }

            paymentDao.updateSnapshot(
                existingPaymentSnapshot.copy(
                    paidAmount = amountPaid,
                    outstandingAmount = outstandingAmount,
                    paymentStatus = paymentStatusStr,
                    lastUpdatedMs = System.currentTimeMillis()
                )
            )
        } else {
            // ✅ SAFE: Type-safe calculation for new snapshot
            val totalAmount: Long = invoice.totalAmount ?: 0L
            val amountPaid: Long = invoice.amountPaid ?: 0L
            val outstandingAmount: Long = (totalAmount - amountPaid).coerceAtLeast(0L)

            paymentDao.insertSnapshots(
                listOf(
                    InvoicePaymentSnapshot(
                        invoiceId = invoice.id,
                        businessProfileId = businessProfileId,
                        customerId = invoice.customerId ?: 0L,
                        customerName = invoice.customerName,
                        invoiceNumber = computedInvoiceNumber,
                        invoiceDate = invoice.date,
                        dueDate = invoice.dueDate,
                        totalAmount = totalAmount,
                        paidAmount = amountPaid,
                        outstandingAmount = outstandingAmount,
                        paymentStatus = paymentStatusStr,
                        ageingBucket = when {
                            daysOverdue <= 0 -> "CURRENT"
                            daysOverdue <= 30 -> "PAST_30"
                            daysOverdue <= 60 -> "PAST_60"
                            else -> "PAST_90"
                        },
                        daysOverdue = daysOverdue,
                        daysSinceDue = maxOf(0, daysOverdue),
                        lastPaymentDate = if (amountPaid > 0) System.currentTimeMillis() else null,
                        lastPaymentAmount = if (amountPaid > 0) amountPaid else 0L,
                        paymentCount = if (amountPaid > 0) 1 else 0,
                        isAtRisk = invoice.dueDate < System.currentTimeMillis() &&
                                invoice.status != InvoiceStatus.PAID.name,
                        riskScore = when {
                            daysOverdue <= 0 -> 0.0
                            daysOverdue <= 30 -> 0.3
                            daysOverdue <= 60 -> 0.6
                            daysOverdue <= 90 -> 0.8
                            else -> 1.0
                        },
                        riskFactors = "",
                        lastUpdatedMs = System.currentTimeMillis(),
                        snapshotDateMs = System.currentTimeMillis()
                    )
                )
            )
        }
        Timber.d("✅ Synced InvoicePaymentSnapshot for invoice ${invoice.id}")
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

    /**
     * Creates payment snapshot when it's missing (fallback for edge cases).
     * Called when payment is recorded but snapshot was never created.
     */
    private suspend fun createPaymentSnapshot(invoice: com.emul8r.bizap.data.local.entities.InvoiceEntity) {
        try {
            val daysOverdue = if (invoice.dueDate < System.currentTimeMillis()) {
                ((System.currentTimeMillis() - invoice.dueDate) / MILLIS_PER_DAY).toInt()
            } else 0

            val computedInvoiceNumber = "INV-${invoice.invoiceYear}-${invoice.invoiceSequence.toString().padStart(6, '0')}"
            val paymentSnapshot = InvoicePaymentSnapshot(
                invoiceId = invoice.id,
                businessProfileId = invoice.businessProfileId,
                customerId = invoice.customerId ?: 0L,
                customerName = invoice.customerName,
                invoiceNumber = computedInvoiceNumber,
                invoiceDate = invoice.date,
                dueDate = invoice.dueDate,
                totalAmount = invoice.totalAmount,
                paidAmount = invoice.amountPaid,
                outstandingAmount = invoice.totalAmount - invoice.amountPaid,
                paymentStatus = when {
                    invoice.status == "PAID" -> "PAID"
                    invoice.status == "PARTIALLY_PAID" -> "PARTIALLY_PAID"
                    invoice.status == "SENT" -> "UNPAID"
                    invoice.status == "OVERDUE" -> "OVERDUE"
                    else -> "UNPAID"
                },
                ageingBucket = when {
                    daysOverdue <= 0 -> "CURRENT"
                    daysOverdue <= 30 -> "PAST_30"
                    daysOverdue <= 60 -> "PAST_60"
                    else -> "PAST_90"
                },
                daysOverdue = daysOverdue,
                daysSinceDue = maxOf(0, daysOverdue),
                lastPaymentDate = if (invoice.amountPaid > 0) System.currentTimeMillis() else null,
                lastPaymentAmount = if (invoice.amountPaid > 0) invoice.amountPaid else 0L,
                paymentCount = if (invoice.amountPaid > 0) 1 else 0,
                isAtRisk = invoice.dueDate < System.currentTimeMillis() && invoice.status != "PAID",
                riskScore = when {
                    daysOverdue <= 0 -> 0.0
                    daysOverdue <= 30 -> 0.3
                    daysOverdue <= 60 -> 0.6
                    daysOverdue <= 90 -> 0.8
                    else -> 1.0
                },
                riskFactors = "",
                lastUpdatedMs = System.currentTimeMillis(),
                snapshotDateMs = System.currentTimeMillis()
            )
            paymentDao.insertSnapshots(listOf(paymentSnapshot))
            Timber.d("✅ Created missing payment snapshot for invoice ${invoice.id}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to create payment snapshot")
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

        throw lastException
            ?: Exception("'$operationName' failed after $maxRetries retry attempts with no captured exception")
    }

    private fun isRetryable(e: Exception): Boolean = when (e) {
        // Programming errors: never retry
        is IllegalArgumentException,
        is IllegalStateException -> false
        // Domain logic violations: never retry
        is BizapException.BusinessLogicError,
        is BizapException.ValidationError,
        is BizapException.NotFoundError,
        is BizapException.InvalidInvoiceError,
        is BizapException.DuplicateError -> false
        // Any other BizapException: do not retry by default (conservative)
        is BizapException -> false
        // Generic RuntimeException from Room/SQLite – potentially transient (DB lock, etc.)
        is RuntimeException -> true
        // Anything else: do not retry
        else -> false
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
            val snapshot = InvoiceAnalyticsSnapshot(
                invoiceId = invoice.id,
                businessProfileId = invoice.businessProfileId,
                customerId = invoice.customerId ?: 0L,
                customerName = invoice.customerName,
                invoiceNumber = invoice.formattedInvoiceNumber(),
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

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Formats an [InvoiceEntity] invoice number in the canonical form
 * `INV-YYYY-NNNNNN` (with optional `-vN` suffix for versions > 1).
 *
 * Centralises the formatting logic that is also used in [Invoice.invoiceNumber]
 * so that entity-layer code does not duplicate the pattern.
 */
private fun com.emul8r.bizap.data.local.entities.InvoiceEntity.formattedInvoiceNumber(): String {
    val base = "INV-$invoiceYear-${invoiceSequence.toString().padStart(6, '0')}"
    return if (version > 1) "$base-v$version" else base
}
