package com.emul8r.bizap.data.mapper

import com.emul8r.bizap.data.local.entities.DailyRevenueTrendV2
import com.emul8r.bizap.data.local.entities.InvoiceStatusCountV2
import com.emul8r.bizap.domain.model.gui2.DailyTrendPointV2
import com.emul8r.bizap.domain.model.gui2.StatusBreakdownV2

/**
 * Mapper functions for converting GUI2 DAO result models to domain models.
 */

fun DailyRevenueTrendV2.toDomainV2() = DailyTrendPointV2(
    date = dateString,
    revenueCents = revenue,
    invoiceCount = invoiceCount
)

fun InvoiceStatusCountV2.toDomainV2() = StatusBreakdownV2(
    status = status,
    count = count
)

fun List<DailyRevenueTrendV2>.toDailyTrendV2(): List<DailyTrendPointV2> =
    map { it.toDomainV2() }

fun List<InvoiceStatusCountV2>.toStatusBreakdownV2(): List<StatusBreakdownV2> =
    map { it.toDomainV2() }
