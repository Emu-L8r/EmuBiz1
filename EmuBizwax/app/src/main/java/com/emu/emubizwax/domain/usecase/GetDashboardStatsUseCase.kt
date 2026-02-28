package com.emu.emubizwax.domain.usecase

import com.emu.emubizwax.domain.model.DashboardStats
import com.emu.emubizwax.domain.model.InvoiceStatus
import com.emu.emubizwax.domain.repository.CustomerRepository
import com.emu.emubizwax.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val customerRepo: CustomerRepository,
    private val invoiceRepo: InvoiceRepository
) {
    operator fun invoke(): Flow<DashboardStats> {
        return combine(
            customerRepo.getCustomers(),
            invoiceRepo.getInvoices()
        ) { customers, invoices ->
            DashboardStats(
                totalCustomers = customers.size,
                activeInvoices = invoices.count { it.status == InvoiceStatus.SENT },
                pendingQuotes = invoices.count { it.isQuote && it.status == InvoiceStatus.DRAFT },
                totalRevenue = invoices.filter { it.status == InvoiceStatus.PAID }.map { it.totalAmount }.fold(BigDecimal.ZERO, BigDecimal::add)
            )
        }
    }
}
