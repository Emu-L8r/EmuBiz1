package com.emul8r.bizap.data.repository.gui2

import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.domain.model.gui2.InvoiceMetricsV2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides reactive invoice count metrics for the GUI2 dashboard.
 * Reads directly from [InvoiceDaoV2] (no snapshot dependency).
 */
@Singleton
class InvoiceMetricsRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2
) {
    fun observeInvoiceMetrics(businessId: Long): Flow<InvoiceMetricsV2> {
        return combine(
            invoiceDaoV2.observeTotalInvoiceCount(businessId),
            invoiceDaoV2.observePaidInvoiceCount(businessId),
            invoiceDaoV2.observeSentInvoiceCount(businessId)
        ) { total, paid, pending ->
            Timber.d("InvoiceMetricsRepositoryV2: total=$total paid=$paid pending=$pending")
            InvoiceMetricsV2(
                businessProfileId = businessId,
                totalInvoices = total,
                paidCount = paid,
                pendingCount = pending
            )
        }
    }
}
