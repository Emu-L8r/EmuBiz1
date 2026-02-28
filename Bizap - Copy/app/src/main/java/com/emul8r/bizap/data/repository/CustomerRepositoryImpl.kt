package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.CustomerDao
import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {
    override fun getAllCustomers(): Flow<List<CustomerEntity>> = 
        customerDao.getCustomersWithInvoices().map { list -> 
            list.map { it.customer } 
        }

    override suspend fun saveCustomer(customer: CustomerEntity) {
        customerDao.insertCustomer(customer)
    }

    override suspend fun getCustomerById(id: Long) = customerDao.getCustomerById(id)
}
