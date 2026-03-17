package com.emul8r.bizap.domain.revenue.repository

import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import kotlinx.coroutines.flow.Flow

/**
 * Unified repository for high-level revenue analysis.
 * Single source of truth for both GUI1 and GUI2 revenue metrics,
 * backed by V2 calculation logic (InvoiceDaoV2 + AnalyticsCalculator).
 */
interface RevenueRepository {
    fun observeRevenueMetrics(businessId: Long): Flow<Result<RevenueMetricsV2>>
}
