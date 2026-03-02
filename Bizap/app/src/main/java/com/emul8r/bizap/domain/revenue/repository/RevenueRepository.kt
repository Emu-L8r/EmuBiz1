package com.emul8r.bizap.domain.revenue.repository

import com.emul8r.bizap.domain.revenue.model.RevenueMetrics

/**
 * Repository for high-level revenue analysis.
 */
interface RevenueRepository {
    suspend fun getRevenueMetrics(businessProfileId: Long): RevenueMetrics
}

