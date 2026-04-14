package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.domain.model.InvoiceStatus
import timber.log.Timber
import javax.inject.Inject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Helper class to centralize snapshot synchronization logic.
 * Eliminates code duplication across repository methods.
 *
 * Responsible for:
 * - Creating/updating InvoiceAnalyticsSnapshot (financial & status data)
 * - Creating/updating DailyRevenueSnapshot (daily aggregates)
 * - Creating/updating InvoicePaymentSnapshot (payment status & aging)
 */
class SnapshotSyncHelper @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val paymentDao: InvoicePaymentDao
) {

    companion object {
        private const val MILLIS_PER_DAY = 86400000L
    }

    /**
     * Synchronizes all three snapshot types for an invoice.
     * Called after any invoice write operation (create, update, delete).
     *
     * @param invoice The invoice entity with updated data
     * @param businessId The business profile ID
     */
    suspend fun syncAllSnapshots(invoice: InvoiceEntity, businessId: Long) {
        try {
            syncInvoiceAnalyticsSnapshot(invoice, businessId)
            syncDailyRevenueSnapshot(invoice, businessId)
            syncPaymentSnapshot(invoice, businessId)
            Timber.d("✅ All snapshots synced for invoice ${invoice.id}")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to sync all snapshots for invoice ${invoice.id}")
            throw e
        }
    }

    /**
     * Synchronizes InvoiceAnalyticsSnapshot.
     * Captures financial data and status information.
     */
    private suspend fun syncInvoiceAnalyticsSnapshot(invoice: InvoiceEntity, businessId: Long) {
        try {
            val existing = analyticsDao.getInvoiceSnapshot(invoice.id)

            if (existing != null) {
                // Update existing snapshot
                val updated = existing.copy(
                    status = invoice.status,
                    isPaid = invoice.status in listOf("PAID", "PARTIALLY_PAID"),
                    isOverdue = invoice.dueDate < System.currentTimeMillis() &&
                            invoice.status != "PAID",
                    snapshotCreatedAtMs = System.currentTimeMillis()
                )
                analyticsDao.updateInvoiceSnapshot(updated)
                Timber.d("✅ Updated InvoiceAnalyticsSnapshot for invoice ${invoice.id}")
            } else {
                // Create new snapshot
                val computedInvoiceNumber = invoice.invoiceNumber
                val snapshot = InvoiceAnalyticsSnapshot(
                    invoiceId = invoice.id,
                    businessProfileId = businessId,
                    customerId = invoice.customerId ?: 0L,
                    customerName = invoice.customerName,
                    invoiceNumber = computedInvoiceNumber,
                    currencyCode = invoice.currencyCode,
                    subtotal = (invoice.totalAmount - invoice.taxAmount),
                    taxAmount = invoice.taxAmount,
                    totalAmount = invoice.totalAmount,
                    status = invoice.status,
                    isPaid = invoice.status in listOf("PAID", "PARTIALLY_PAID"),
                    isOverdue = invoice.dueDate < System.currentTimeMillis() &&
                            invoice.status != "PAID",
                    invoiceDateMs = invoice.date,
                    createdAtMs = invoice.updatedAt,
                    paidAtMs = if (invoice.status == "PAID") System.currentTimeMillis() else null,
                    daysPending = if (invoice.status in listOf("SENT", "PARTIALLY_PAID")) {
                        ((System.currentTimeMillis() - invoice.date) / MILLIS_PER_DAY).toInt()
                    } else 0,
                    lineItemCount = 1,
                    snapshotCreatedAtMs = System.currentTimeMillis()
                )
                analyticsDao.insertInvoiceSnapshot(snapshot)
                Timber.d("✅ Created InvoiceAnalyticsSnapshot for invoice ${invoice.id}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync InvoiceAnalyticsSnapshot")
            throw e
        }
    }

    /**
     * Synchronizes DailyRevenueSnapshot.
     * Creates or updates daily revenue aggregates grouped by date and currency.
     */
    private suspend fun syncDailyRevenueSnapshot(invoice: InvoiceEntity, businessId: Long) {
        try {
            val dateString = Instant.ofEpochMilli(invoice.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString()

            val revenueContribution = if (invoice.status in listOf("PAID", "PARTIALLY_PAID")) {
                invoice.amountPaid
            } else 0L

            val existing = analyticsDao.getDailySnapshotByDate(businessId, dateString)

            if (existing != null) {
                // Update existing daily snapshot
                val updated = existing.copy(
                    totalRevenue = existing.totalRevenue + revenueContribution,
                    invoiceCount = existing.invoiceCount + 1,
                    paidInvoiceCount = existing.paidInvoiceCount + if (invoice.status == "PAID") 1 else 0,
                    snapshotCreatedAtMs = System.currentTimeMillis()
                )
                analyticsDao.insertDailySnapshot(updated)
                Timber.d("✅ Updated DailyRevenueSnapshot for $dateString")
            } else {
                // Create new daily snapshot
                val snapshot = DailyRevenueSnapshot(
                    businessProfileId = businessId,
                    dateString = dateString,
                    dateMs = invoice.date,
                    totalRevenue = revenueContribution,
                    invoiceCount = 1,
                    paidInvoiceCount = if (invoice.status == "PAID") 1 else 0,
                    currencyBreakdown = """{"${invoice.currencyCode}": $revenueContribution}""",
                    snapshotCreatedAtMs = System.currentTimeMillis()
                )
                analyticsDao.insertDailySnapshot(snapshot)
                Timber.d("✅ Created DailyRevenueSnapshot for $dateString")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync DailyRevenueSnapshot")
            throw e
        }
    }

    /**
     * Synchronizes InvoicePaymentSnapshot.
     * Creates or updates payment status and aging information.
     */
    private suspend fun syncPaymentSnapshot(invoice: InvoiceEntity, businessId: Long) {
        try {
            val daysOverdue = if (invoice.dueDate < System.currentTimeMillis()) {
                ((System.currentTimeMillis() - invoice.dueDate) / MILLIS_PER_DAY).toInt()
            } else 0

            val existing = paymentDao.getSnapshotByInvoiceId(invoice.id)

            if (existing != null) {
                // Update existing payment snapshot
                val updated = existing.copy(
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
                    isAtRisk = invoice.dueDate < System.currentTimeMillis() &&
                            invoice.status != "PAID",
                    riskScore = when {
                        daysOverdue <= 0 -> 0.0
                        daysOverdue <= 30 -> 0.3
                        daysOverdue <= 60 -> 0.6
                        daysOverdue <= 90 -> 0.8
                        else -> 1.0
                    },
                    lastUpdatedMs = System.currentTimeMillis(),
                    snapshotDateMs = System.currentTimeMillis()
                )
                paymentDao.updateSnapshot(updated)
                Timber.d("✅ Updated InvoicePaymentSnapshot for invoice ${invoice.id}")
            } else {
                // Create new payment snapshot
                val computedInvoiceNumber = invoice.invoiceNumber
                val snapshot = InvoicePaymentSnapshot(
                    invoiceId = invoice.id,
                    businessProfileId = businessId,
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
                    isAtRisk = invoice.dueDate < System.currentTimeMillis() &&
                            invoice.status != "PAID",
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
                paymentDao.insertSnapshots(listOf(snapshot))
                Timber.d("✅ Created InvoicePaymentSnapshot for invoice ${invoice.id}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync InvoicePaymentSnapshot")
            throw e
        }
    }
}

