package com.emu.emubizwax.domain.usecase

import com.emu.emubizwax.domain.model.Customer
import com.emu.emubizwax.domain.repository.CustomerRepository
import javax.inject.Inject

class SaveCustomerUseCase @Inject constructor(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(customer: Customer) {
        if (customer.name.isBlank()) {
            throw Exception("Customer name cannot be empty")
        }
        repository.upsertCustomer(customer)
    }
}
