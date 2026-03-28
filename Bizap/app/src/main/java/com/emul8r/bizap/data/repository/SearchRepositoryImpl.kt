package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.CustomerDaoV2
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.domain.analytics.SearchResult
import com.emul8r.bizap.domain.analytics.SearchType
import com.emul8r.bizap.domain.repository.SearchRepository
import com.emul8r.bizap.utils.CentsFormatter
import javax.inject.Inject

/**
 * Implementation of SearchRepository using CustomerDaoV2 and InvoiceDaoV2.
 *
 * Provides real-time search across customers and invoices for a business.
 * All searches are scoped to the active business ID.
 */
class SearchRepositoryImpl @Inject constructor(
    private val customerDaoV2: CustomerDaoV2,
    private val invoiceDaoV2: InvoiceDaoV2
) : SearchRepository {

    /**
     * Search invoices by number.
     */
    override suspend fun searchInvoices(
        query: String,
        businessId: Long,
        limit: Int
    ): List<SearchResult> {
        if (query.trim().isEmpty()) return emptyList()

        return try {
            val invoices = invoiceDaoV2.searchByNumber(businessId, query, limit)
            invoices.map { invoice ->
                SearchResult(
                    id = invoice.id,
                    title = "Invoice #${invoice.invoiceNumber}",
                    subtitle = CentsFormatter.formatCents(invoice.totalAmount),
                    type = SearchType.INVOICE,
                    iconType = "invoice"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Search customers by name or email.
     */
    override suspend fun searchCustomers(
        query: String,
        businessId: Long,
        limit: Int
    ): List<SearchResult> {
        if (query.trim().isEmpty()) return emptyList()

        return try {
            val customers = customerDaoV2.searchByNameOrEmail(businessId, query, limit)
            customers.map { customer ->
                SearchResult(
                    id = customer.id,
                    title = customer.name,
                    subtitle = customer.email ?: "No email",
                    type = SearchType.CUSTOMER,
                    iconType = "customer"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Combined search across invoices and customers.
     */
    override suspend fun searchAll(
        query: String,
        businessId: Long,
        limit: Int
    ): List<SearchResult> {
        val invoiceResults = searchInvoices(query, businessId, limit)
        val customerResults = searchCustomers(query, businessId, limit)

        return (invoiceResults + customerResults).take(limit * 2)
    }
}




