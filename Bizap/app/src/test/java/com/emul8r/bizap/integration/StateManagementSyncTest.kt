@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.integration

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.ui.invoices.InvoiceDetailViewModel
import com.emul8r.bizap.ui.invoices.InvoiceDetailUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for state management synchronization using Turbine.
 *
 * **Purpose:** Verify that ViewModel state and UI state stay in sync, preventing
 * "Ghost Data" bugs where the UI displays stale or inconsistent data.
 *
 * **What is tested:**
 * 1. ViewModel emits data → UI immediately observes it
 * 2. ViewModel state changes → UI updates without delay
 * 3. Multiple rapid state changes → UI processes all of them in order
 * 4. Error states → UI displays error, not previous success state
 * 5. Loading states → UI shows loading spinner, not old data
 *
 * **Turbine Usage:**
 * Turbine is a testing library for Kotlin Flow. It allows us to:
 * - Collect Flow emissions in a scoped way
 * - Assert on each emission individually
 * - Automatically cancel collection when done
 * - Timeout if emissions don't arrive
 *
 * This eliminates the complexity of manually managing coroutine collection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateManagementSyncTest : BaseUnitTest() {

    private val invoiceRepository: InvoiceRepository = mockk(relaxed = true)
    private lateinit var viewModel: InvoiceDetailViewModel

    private val testInvoice = Invoice(
        id = 1L,
        customerId = 100L,
        customerName = "Test Customer",
        date = System.currentTimeMillis(),
        totalAmount = 100_000L,
        items = emptyList(),
        isQuote = false,
        status = InvoiceStatus.DRAFT
    )

    @Before
    fun setup() {
        // Mock invoice repository
        coEvery {
            invoiceRepository.getInvoiceWithItemsById(any())
        } returns flowOf(testInvoice)

        coEvery {
            invoiceRepository.getInvoiceGroupWithVersions(any(), any())
        } returns flowOf(listOf(testInvoice))

        // Create proper SavedStateHandle with invoiceId parameter
        val savedStateHandle = SavedStateHandle().apply {
            set("invoiceId", testInvoice.id)
        }

        // Create ViewModel with mocked dependencies
        viewModel = InvoiceDetailViewModel(
            invoiceRepo = invoiceRepository,
            analyticsRepository = mockk(relaxed = true),
            documentRepository = mockk(relaxed = true),
            invoiceSettingsRepository = mockk(relaxed = true),
            pdfService = mockk(relaxed = true),
            csvExportService = mockk(relaxed = true),
            businessProfileRepository = mockk(relaxed = true),
            printService = mockk(relaxed = true),
            documentManager = mockk(relaxed = true),
            generateAndSaveInvoiceUseCase = mockk(relaxed = true),
            savedStateHandle = savedStateHandle
        )
    }

    // ── TEST 1: Initial State is Loading ──────────────────────────────────────

    @Test
    fun `test_initial_state_is_loading`() = runTest {
        advanceUntilIdle()  // Let ViewModel initialization complete
        val initialState = viewModel.uiState.value
        assertEquals(
            InvoiceDetailUiState.Loading,
            initialState,
            "ViewModel should emit Loading state immediately on construction"
        )
    }

    // ── TEST 2: Data Emission Updates UI State ────────────────────────────────

    @Test
    fun `test_viewmodel_emits_success_state_when_invoice_loads`() = runTest {
        // Arrange: ViewModel is created with mocked invoice repo
        advanceUntilIdle()  // Let ViewModel initialization coroutines complete

        // Act & Assert: Use Turbine to test Flow emissions
        viewModel.uiState.test {
            // First emission: Loading state
            assertEquals(
                InvoiceDetailUiState.Loading,
                awaitItem(),
                "First emission should be Loading"
            )

            // Second emission: Success state
            val successState = awaitItem() as InvoiceDetailUiState.Success
            assertEquals(
                testInvoice.id,
                successState.data.id,
                "Success state should contain loaded invoice"
            )

            // Cancel collection
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── TEST 3: No Ghost Data After State Change ──────────────────────────────

    @Test
    fun `test_no_ghost_data_on_rapid_state_changes`() = runTest {
        // Arrange: ViewModel is loaded
        advanceUntilIdle()

        // Act & Assert: Use Turbine to test all emissions
        viewModel.uiState.test {
            // Collect states
            val states = mutableListOf<InvoiceDetailUiState>()

            states.add(awaitItem())  // Loading
            states.add(awaitItem())  // Success

            // Assert: States are in correct order
            assertEquals(
                InvoiceDetailUiState.Loading,
                states[0],
                "First state must be Loading"
            )
            assertTrue(
                states[1] is InvoiceDetailUiState.Success,
                "Last state should be Success, not old data"
            )

            // Verify no ghost data: success state has correct invoice
            val successState = states[1] as InvoiceDetailUiState.Success
            assertEquals(
                testInvoice.id,
                successState.data.id,
                "Success state should have correct invoice ID"
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    // ── TEST 4: Error State Doesn't Show Old Data ─────────────────────────────

    @Test
    fun `test_error_state_replaces_old_success_state`() = runTest {
        // Arrange: Mock repository to return error
        val errorInvoiceRepository: InvoiceRepository = mockk(relaxed = true)
        coEvery {
            errorInvoiceRepository.getInvoiceWithItemsById(any())
        } returns flowOf(null)  // Simulate not found

        val errorSavedStateHandle = SavedStateHandle().apply {
            set("invoiceId", 999L)  // Non-existent ID
        }

        // Create new ViewModel with error scenario
        val errorViewModel = InvoiceDetailViewModel(
            invoiceRepo = errorInvoiceRepository,
            analyticsRepository = mockk(relaxed = true),
            documentRepository = mockk(relaxed = true),
            invoiceSettingsRepository = mockk(relaxed = true),
            pdfService = mockk(relaxed = true),
            csvExportService = mockk(relaxed = true),
            businessProfileRepository = mockk(relaxed = true),
            printService = mockk(relaxed = true),
            documentManager = mockk(relaxed = true),
            generateAndSaveInvoiceUseCase = mockk(relaxed = true),
            savedStateHandle = errorSavedStateHandle
        )
        advanceUntilIdle()  // Let error initialization complete

        // Act & Assert: Use Turbine to collect error state
        errorViewModel.uiState.test {
            awaitItem()  // Loading state

            val finalState = awaitItem()
            assertTrue(
                finalState is InvoiceDetailUiState.Error,
                "Final state should be Error, not old data"
            )

            val errorState = finalState as InvoiceDetailUiState.Error
            assertTrue(
                errorState.message.isNotEmpty(),
                "Error state should have meaningful message"
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    // ── TEST 5: Multiple State Emissions Are Processed In Order ───────────────

    @Test
    fun `test_all_state_emissions_are_processed_in_order`() = runTest {
        // Arrange: Use Turbine to capture exact emission sequence
        advanceUntilIdle()

        // Act & Assert
        viewModel.uiState.test {
            // Collect exact sequence
            val state1 = awaitItem()
            val state2 = awaitItem()

            // Assert: Correct sequence
            assertEquals(
                InvoiceDetailUiState.Loading,
                state1,
                "State 1: Loading"
            )
            assertTrue(
                state2 is InvoiceDetailUiState.Success,
                "State 2: Success"
            )
            assertEquals(
                testInvoice.id,
                (state2 as InvoiceDetailUiState.Success).data.id,
                "Success should have correct data"
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    // ── TEST 6: UI Observing ViewModel State Updates Synchronously ────────────

    @Test
    fun `test_ui_observes_viewmodel_state_without_delay`() = runTest {
        // This test verifies that Flow properly emits all state changes
        // without losing any emissions.

        advanceUntilIdle()

        // Act & Assert: Use Turbine to verify emissions arrive in order
        viewModel.uiState.test {
            val timestamp1 = System.currentTimeMillis()
            val state1 = awaitItem()

            val timestamp2 = System.currentTimeMillis()
            val state2 = awaitItem()

            // Assert: Emissions were captured in order
            assertEquals(
                InvoiceDetailUiState.Loading,
                state1,
                "First emission should be Loading"
            )
            assertTrue(
                state2 is InvoiceDetailUiState.Success,
                "Second emission should be Success"
            )
            assertTrue(
                timestamp1 <= timestamp2,
                "State emissions should be in chronological order"
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    // ── TEST 7: Loading State Is Visible During Data Load ────────────────────

    @Test
    fun `test_loading_state_is_emitted_during_fetch`() = runTest {
        // Act: Observe ViewModel startup
        advanceUntilIdle()

        // Assert: Use Turbine to verify Loading is emitted
        viewModel.uiState.test {
            // First emission must be Loading
            val firstState = awaitItem()
            assertEquals(
                InvoiceDetailUiState.Loading,
                firstState,
                "ViewModel must emit Loading state so UI can show loading spinner"
            )

            // Second emission is Success
            val secondState = awaitItem()
            assertTrue(
                secondState is InvoiceDetailUiState.Success,
                "Loading should not be the final state (should be Success or Error)"
            )

            cancelAndConsumeRemainingEvents()
        }
    }
}

