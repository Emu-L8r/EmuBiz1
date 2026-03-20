package com.emul8r.bizap.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.domain.model.Invoice
import timber.log.Timber

/**
 * [PagingSource] that loads [Invoice] pages from [InvoiceDao].
 *
 * Uses zero-based page indices and delegates offset calculation to this class so the
 * DAO only needs to understand limit/offset semantics.
 */
class InvoicePagingSource(
    private val invoiceDao: InvoiceDao,
    private val businessId: Long
) : PagingSource<Int, Invoice>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Invoice> {
        return try {
            val pageIndex = params.key ?: 0
            val pageSize = params.loadSize
            val offset = pageIndex * pageSize

            Timber.d("Loading invoice page $pageIndex (offset=$offset, size=$pageSize)")

            val invoices = invoiceDao.getInvoicesPaged(
                businessId = businessId,
                limit = pageSize,
                offset = offset
            )

            LoadResult.Page(
                data = invoices.map { it.toDomain() },
                prevKey = if (pageIndex > 0) pageIndex - 1 else null,
                nextKey = if (invoices.isNotEmpty()) pageIndex + 1 else null
            )
        } catch (e: Exception) {
            Timber.e(e, "Error loading invoice page")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Invoice>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
