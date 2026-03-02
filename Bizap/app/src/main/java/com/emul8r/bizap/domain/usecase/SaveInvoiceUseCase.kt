package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.InvoiceRepository
import javax.inject.Inject

class SaveInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(invoice: Invoice): Result<Long> {
        if (invoice.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invoice must have at least one line item"))
        }

        if (invoice.customerName.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name cannot be empty"))
        }

        return try {
            val id = repository.saveInvoice(invoice)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

