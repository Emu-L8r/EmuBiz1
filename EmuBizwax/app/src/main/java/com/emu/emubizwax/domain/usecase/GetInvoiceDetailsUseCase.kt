package com.emu.emubizwax.domain.usecase

import com.emu.emubizwax.domain.model.InvoiceDetailsData
import com.emu.emubizwax.domain.repository.CustomerRepository
import com.emu.emubizwax.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetInvoiceDetailsUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository
) {
    operator fun invoke(invoiceId: Long): Flow<InvoiceDetailsData?> {
        val invoiceFlow = invoiceRepository.getInvoiceWithItems(invoiceId)
        return invoiceFlow.combine(customerRepository.getCustomers()) { invoiceWithItems, customers ->
            invoiceWithItems?.let {
                val customer = customers.find { it.id == invoiceWithItems.invoice.customerId }
                if (customer != null) {
                    InvoiceDetailsData(customer, it)
                } else {
                    null
                }
            }
        }
    }
}
