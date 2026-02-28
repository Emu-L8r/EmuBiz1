package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

class OfflineCustomersRepository(private val customerDao: CustomerDao) : CustomerRepository {
    override fun getAllCustomersStream(): Flow<List<Customer>> = customerDao.getAllCustomers()

    override fun getCustomerStream(id: Int): Flow<Customer?> = customerDao.getCustomerById(id)

    override suspend fun insertCustomer(customer: Customer) = customerDao.insert(customer)

    override suspend fun deleteCustomer(customer: Customer) = customerDao.softDelete(customer.id)

    override suspend fun updateCustomer(customer: Customer) = customerDao.update(customer)

    override fun getDeletedCustomersStream(): Flow<List<Customer>> = customerDao.getDeletedCustomers()

    override suspend fun restoreCustomer(customer: Customer) = customerDao.restoreCustomer(customer.id)

    override suspend fun permanentlyDeleteCustomer(customer: Customer) = customerDao.permanentlyDelete(customer.id)

    override fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
}
