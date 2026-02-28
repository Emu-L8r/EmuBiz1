package com.emul8r.emubizzz.data.repository

import com.emul8r.emubizzz.data.local.db.dao.CustomerDao
import com.emul8r.emubizzz.data.local.db.entities.CustomerEntity
import com.emul8r.emubizzz.data.local.db.relations.CustomerWithInvoices
import com.emul8r.emubizzz.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {
    override fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    override fun getCustomerById(customerId: Long): Flow<CustomerEntity> = customerDao.getCustomerById(customerId)
    override fun getCustomersWithInvoices(): Flow<List<CustomerWithInvoices>> = customerDao.getCustomersWithInvoices()
    override suspend fun insert(customer: CustomerEntity) = customerDao.insertCustomer(customer)
    override suspend fun update(customer: CustomerEntity) = customerDao.update(customer)
    override suspend fun delete(customerId: Long) = customerDao.delete(customerId)
}