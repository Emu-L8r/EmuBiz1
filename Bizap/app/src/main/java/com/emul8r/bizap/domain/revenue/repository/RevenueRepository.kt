package com.emul8r.bizap.domain.revenue.repository

import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import kotlinx.coroutines.flow.Flow

/**
 * Repository for high-level revenue analysis.
 */
interface RevenueRepository {
    /**
     * Returns a reactive stream of revenue metrics for the given business.
     */
    fun getRevenueMetrics(businessProfileId: Long): Flow<RevenueMetrics>
    
    /**
     * Immediate one-shot fetch (used for workers or specific refreshes).
     */
    suspend fun getRevenueMetricsSnapshot(businessProfileId: Long): RevenueMetrics
}
