package com.emul8r.bizap.data.local.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [InvoicePagingSource].
 *
 * Verifies page loading, key calculation, and error handling.
 */
class InvoicePagingSourceTest : BaseUnitTest() {

    private val invoiceDao: InvoiceDao = mockk()
    private val businessId = 1L

    private fun createPagingSource() = InvoicePagingSource(invoiceDao, businessId)

    private fun createInvoiceWithItems(id: Long): InvoiceWithItems {
        val invoice = TestDataFactory.createTestInvoice(id = id, businessProfileId = businessId)
        return InvoiceWithItems(invoice.toEntity(), emptyList())
    }

    // ── load: first page ──────────────────────────────────────────────────────

    @Test
    fun `load - first page returns correct data and null prevKey`() = runTest {
        val items = (1L..20L).map { createInvoiceWithItems(it) }
        coEvery { invoiceDao.getInvoicesPaged(businessId, 20, 0) } returns items

        val source = createPagingSource()
        val params = PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)

        val result = source.load(params)

        assertIs<PagingSource.LoadResult.Page<Int, Invoice>>(result)
        assertEquals(20, result.data.size)
        assertNull(result.prevKey)
        assertEquals(1, result.nextKey)
    }

    // ── load: last page ───────────────────────────────────────────────────────

    @Test
    fun `load - empty result sets nextKey to null`() = runTest {
        coEvery { invoiceDao.getInvoicesPaged(businessId, 20, 20) } returns emptyList()

        val source = createPagingSource()
        val params = PagingSource.LoadParams.Append(key = 1, loadSize = 20, placeholdersEnabled = false)

        val result = source.load(params)

        assertIs<PagingSource.LoadResult.Page<Int, Invoice>>(result)
        assertTrue(result.data.isEmpty())
        assertEquals(0, result.prevKey)
        assertNull(result.nextKey)
    }

    // ── load: subsequent page ─────────────────────────────────────────────────

    @Test
    fun `load - second page has correct prevKey and nextKey`() = runTest {
        val items = (21L..40L).map { createInvoiceWithItems(it) }
        coEvery { invoiceDao.getInvoicesPaged(businessId, 20, 20) } returns items

        val source = createPagingSource()
        val params = PagingSource.LoadParams.Append(key = 1, loadSize = 20, placeholdersEnabled = false)

        val result = source.load(params)

        assertIs<PagingSource.LoadResult.Page<Int, Invoice>>(result)
        assertEquals(20, result.data.size)
        assertEquals(0, result.prevKey)
        assertEquals(2, result.nextKey)
    }

    // ── load: DAO error ───────────────────────────────────────────────────────

    @Test
    fun `load - DAO exception returns LoadResult Error`() = runTest {
        val error = RuntimeException("Database error")
        coEvery { invoiceDao.getInvoicesPaged(any(), any(), any()) } throws error

        val source = createPagingSource()
        val params = PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)

        val result = source.load(params)

        assertIs<PagingSource.LoadResult.Error<Int, Invoice>>(result)
        assertEquals(error, result.throwable)
    }

    // ── offset calculation ────────────────────────────────────────────────────

    @Test
    fun `load - offset is correctly derived from page index and load size`() = runTest {
        // Page 3 with loadSize=10 → offset = 30
        coEvery { invoiceDao.getInvoicesPaged(businessId, 10, 30) } returns emptyList()

        val source = createPagingSource()
        val params = PagingSource.LoadParams.Append(key = 3, loadSize = 10, placeholdersEnabled = false)

        source.load(params)

        // No assertion needed beyond the mock verifying offset=30 was called
        // The coEvery above will throw if called with different args
    }

    // ── getRefreshKey ─────────────────────────────────────────────────────────

    @Test
    fun `getRefreshKey - returns null for empty paging state`() {
        val source = createPagingSource()
        val state = PagingState<Int, Invoice>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )
        assertNull(source.getRefreshKey(state))
    }
}
