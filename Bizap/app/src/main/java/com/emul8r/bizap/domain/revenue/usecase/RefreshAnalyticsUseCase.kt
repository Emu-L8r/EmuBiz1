package com.emul8r.bizap.domain.revenue.usecase

import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.domain.repository.InvoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Regenerates analytics snapshots from raw invoice data.
 * This is the "Force Refresh" engine.
 */
class RefreshAnalyticsUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val analyticsDao: AnalyticsDao
) {
    suspend operator fun invoke(businessId: Long) = withContext(Dispatchers.IO) {
        try {
            Timber.d("📊 Regenerating analytics snapshots for business $businessId")
            
            // 1. Fetch all invoices for this business
            val invoices = invoiceRepository.getAllInvoicesWithItems().first()
            if (invoices.isEmpty()) {
                Timber.w("No invoices found, nothing to analyze.")
                return@withContext
            }

            // 2. Group invoices by date (YYYY-MM-DD)
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val invoicesByDate = invoices.groupBy { 
                Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
            }

            // 3. Process each day and generate snapshots
            invoicesByDate.forEach { (dateString, dayInvoices) ->
                val totalRevenue = dayInvoices.sumOf { it.totalAmount }
                val invoiceCount = dayInvoices.size
                
                // Build currency breakdown JSON
                val currencyMap = dayInvoices.groupBy { it.currencyCode }
                    .mapValues { (_, invs) -> invs.sumOf { it.totalAmount } }
                
                val breakdownJson = JSONObject().apply {
                    currencyMap.forEach { (code, amount) -> put(code, amount) }
                }.toString()

                val snapshot = DailyRevenueSnapshot(
                    businessProfileId = businessId,
                    dateString = dateString,
                    dateMs = Instant.ofEpochMilli(dayInvoices.first().date).toEpochMilli(),
                    totalRevenue = totalRevenue,
                    invoiceCount = invoiceCount,
                    currencyBreakdown = breakdownJson,
                    paidInvoiceCount = dayInvoices.count { it.status.name == "PAID" },
                    draftInvoiceCount = dayInvoices.count { it.status.name == "DRAFT" },
                    averageInvoiceAmount = if (invoiceCount > 0) totalRevenue / invoiceCount else 0L
                )

                analyticsDao.insertDailyRevenue(snapshot)
            }

            Timber.d("✅ Successfully regenerated snapshots for ${invoicesByDate.size} days.")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to refresh analytics")
            throw e
        }
    }
}
