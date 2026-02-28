package com.emu.emubizwax.data.repository

import com.emu.emubizwax.data.local.dao.CustomerDao
import com.emu.emubizwax.data.mapper.CustomerMapper
import com.emu.emubizwax.domain.model.Customer
import com.emu.emubizwax.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val mapper: CustomerMapper
) : CustomerRepository {

    override fun getCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }

    override fun getCustomerById(id: Long): Flow<Customer?> {
        return customerDao.getCustomerById(id).map { mapper.toDomain(it) }
    }

    override suspend fun upsertCustomer(customer: Customer) {
        customerDao.upsertCustomer(mapper.toEntity(customer))
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(mapper.toEntity(customer))
    }
}
