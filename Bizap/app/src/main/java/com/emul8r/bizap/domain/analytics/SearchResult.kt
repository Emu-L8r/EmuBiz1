package com.emul8r.bizap.domain.analytics

/**
 * Represents a search query for analytics.
 *
 * Used to filter invoices and customers based on user input.
 * Supports filtering by keyword and type (invoice, customer).
 */
data class SearchQuery(
    val keyword: String,
    val searchType: SearchType = SearchType.ALL
) {
    /**
     * Whether the query is valid (non-empty keyword).
     */
    val isValid: Boolean get() = keyword.trim().isNotEmpty()
}

/**
 * Types of entities that can be searched.
 */
enum class SearchType {
    /**
     * Search invoices only.
     */
    INVOICE,

    /**
     * Search customers only.
     */
    CUSTOMER,

    /**
     * Search both invoices and customers.
     */
    ALL
}

/**
 * Represents a single search result.
 *
 * Contains the essential information to display a search result
 * and navigate to the relevant detail screen.
 */
data class SearchResult(
    /**
     * Unique identifier (invoice ID or customer ID).
     */
    val id: Long,

    /**
     * Primary text to display (invoice number or customer name).
     */
    val title: String,

    /**
     * Secondary text to display (invoice amount or customer email).
     */
    val subtitle: String,

    /**
     * Type of result (INVOICE or CUSTOMER).
     */
    val type: SearchType,

    /**
     * Optional icon identifier for visual representation.
     */
    val iconType: String = when (type) {
        SearchType.INVOICE -> "invoice"
        SearchType.CUSTOMER -> "customer"
        SearchType.ALL -> "default"
    }
) {
    /**
     * Returns the navigation route for this result.
     *
     * Examples:
     * - Invoice #123 → "invoice/123"
     * - Customer ABC → "customer/456"
     */
    fun getNavigationRoute(): String = when (type) {
        SearchType.INVOICE -> "invoice/$id"
        SearchType.CUSTOMER -> "customer/$id"
        SearchType.ALL -> "home"
    }
}

