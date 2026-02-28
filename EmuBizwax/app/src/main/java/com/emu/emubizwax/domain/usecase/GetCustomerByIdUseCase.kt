package com.emu.emubizwax.domain.usecase

import com.emu.emubizwax.domain.model.Customer
import com.emu.emubizwax.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCustomerByIdUseCase @Inject constructor(
    private val repository: CustomerRepository
) {
    operator fun invoke(id: Long): Flow<Customer?> {
        return repository.getCustomerById(id)
    }
}
