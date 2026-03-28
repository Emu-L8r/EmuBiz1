package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.analytics.SearchResult

/**
 * Repository for searching invoices and customers.
 *
 * Provides methods to search across invoices and customers
 * for a specific business, with results limited to top matches.
 */
interface SearchRepository {
    /**
     * Search invoices by number or customer name.
     *
     * @param query Search keyword (invoice number, amount, etc.)
     * @param businessId Business context for scoped search
     * @param limit Maximum number of results (default 10)
     * @return List of matching invoice search results
     */
    suspend fun searchInvoices(
        query: String,
        businessId: Long,
        limit: Int = 10
    ): List<SearchResult>

    /**
     * Search customers by name or email.
     *
     * @param query Search keyword (customer name, email, etc.)
     * @param businessId Business context for scoped search
     * @param limit Maximum number of results (default 10)
     * @return List of matching customer search results
     */
    suspend fun searchCustomers(
        query: String,
        businessId: Long,
        limit: Int = 10
    ): List<SearchResult>

    /**
     * Combined search across both invoices and customers.
     *
     * @param query Search keyword
     * @param businessId Business context for scoped search
     * @param limit Maximum number of results per type (default 5 each)
     * @return Combined list of matching results from both types
     */
    suspend fun searchAll(
        query: String,
        businessId: Long,
        limit: Int = 5
    ): List<SearchResult>
}

