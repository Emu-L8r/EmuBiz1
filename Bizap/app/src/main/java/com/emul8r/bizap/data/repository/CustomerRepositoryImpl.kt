package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.CustomerDao
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {
    
    override fun getAllCustomers(): Flow<List<Customer>> = 
        customerDao.getAllCustomers().map { list -> 
            list.map { it.toDomain() } 
        }

    override suspend fun insert(customer: Customer): Long {
        return customerDao.insert(customer.toEntity())
    }

    override fun getCustomerById(id: Long): Flow<Customer?> = 
        customerDao.getCustomerById(id).map { it?.toDomain() }

    override suspend fun updateCustomer(customer: Customer) {
        customerDao.update(customer.toEntity())
    }

    override suspend fun deleteCustomer(id: Long) {
        customerDao.deleteCustomer(id)
    }
}

