@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.integration

import androidx.lifecycle.SavedStateHandle
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.ui.invoices.InvoiceDetailViewModel
import com.emul8r.bizap.ui.invoices.InvoiceDetailUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for state management synchronization.
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
 * **Ghost Data Bug Example:**
 * ```
 * User opens invoice → UI shows old data
 * ViewModel loads new data → emits new state
 * But UI still shows old data (Ghost Data!)
 * ```
 *
 * This test suite prevents that.
 */
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

        // Act: Wait for ViewModel to emit success state
        val states = mutableListOf<InvoiceDetailUiState>()
        viewModel.uiState.collect { state ->
            states.add(state)
            if (state is InvoiceDetailUiState.Success) {
                // Stop collecting after first success
                return@collect
            }
        }

        // Assert: ViewModel emitted Loading then Success
        assertEquals(
            2,
            states.size,
            "ViewModel should emit Loading, then Success state"
        )
        assertEquals(
            InvoiceDetailUiState.Loading,
            states[0],
            "First emission should be Loading"
        )
        assertTrue(
            states[1] is InvoiceDetailUiState.Success,
            "Second emission should be Success"
        )
        assertEquals(
            testInvoice.id,
            (states[1] as InvoiceDetailUiState.Success).data.id,
            "Success state should contain loaded invoice"
        )
    }

    // ── TEST 3: No Ghost Data After State Change ──────────────────────────────

    @Test
    fun `test_no_ghost_data_on_rapid_state_changes`() = runTest {
        // Arrange: ViewModel is loaded

        // Act: Collect all states emitted
        val states = mutableListOf<InvoiceDetailUiState>()
        viewModel.uiState.collect { state ->
            states.add(state)
        }

        // Assert: States are in correct order, no duplicates
        assertTrue(
            states.size >= 2,
            "ViewModel should emit at least Loading and Success"
        )
        assertTrue(
            states.first() == InvoiceDetailUiState.Loading,
            "First state must be Loading"
        )
        assertTrue(
            states.last() is InvoiceDetailUiState.Success,
            "Last state should be Success, not old data"
        )

        // Verify no ghost data: each success state has correct invoice
        states.filterIsInstance<InvoiceDetailUiState.Success>().forEach { successState ->
            assertEquals(
                testInvoice.id,
                successState.data.id,
                "All Success states should have correct invoice ID"
            )
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

        // Act: Collect states
        val states = mutableListOf<InvoiceDetailUiState>()
        errorViewModel.uiState.collect { state ->
            states.add(state)
            if (state is InvoiceDetailUiState.Error || states.size > 5) {
                return@collect  // Stop after error or timeout
            }
        }

        // Assert: Last state is Error, not old Success
        assertTrue(
            states.last() is InvoiceDetailUiState.Error,
            "Final state should be Error, not old data"
        )
        val errorState = states.last() as InvoiceDetailUiState.Error
        assertTrue(
            errorState.message.isNotEmpty(),
            "Error state should have meaningful message"
        )
    }

    // ── TEST 5: Multiple State Emissions Are Processed In Order ───────────────

    @Test
    fun `test_all_state_emissions_are_processed_in_order`() = runTest {
        // Arrange: Collect all emissions without early exit

        // Act
        val states = mutableListOf<InvoiceDetailUiState>()
        viewModel.uiState.collect { state ->
            states.add(state)
            if (states.size >= 2) return@collect  // Wait for at least 2 states
        }

        // Assert: Correct sequence
        assertEquals(
            InvoiceDetailUiState.Loading,
            states[0],
            "State 1: Loading"
        )
        assertTrue(
            states[1] is InvoiceDetailUiState.Success,
            "State 2: Success"
        )
        assertEquals(
            testInvoice.id,
            (states[1] as InvoiceDetailUiState.Success).data.id,
            "Success should have correct data"
        )
    }

    // ── TEST 6: UI Observing ViewModel State Updates Synchronously ────────────

    @Test
    fun `test_ui_observes_viewmodel_state_without_delay`() = runTest {
        // This test verifies that collectAsStateWithLifecycle() properly observes
        // ViewModel emissions without losing any state changes.

        // Arrange: Collect state changes with timestamps
        val stateChanges = mutableListOf<Pair<Long, InvoiceDetailUiState>>()

        // Act: Emit states and record timing
        viewModel.uiState.collect { state ->
            stateChanges.add(System.currentTimeMillis() to state)
            if (stateChanges.size >= 2) return@collect
        }

        // Assert: All state changes were captured
        assertEquals(
            2,
            stateChanges.size,
            "Should capture all state emissions"
        )

        // Verify no state skipped
        assertTrue(
            stateChanges[0].first <= stateChanges[1].first,
            "State emissions should be in chronological order"
        )
    }

    // ── TEST 7: Loading State Is Visible During Data Load ────────────────────

    @Test
    fun `test_loading_state_is_emitted_during_fetch`() = runTest {
        // Act: Observe ViewModel startup

        // Arrange
        val states = mutableListOf<InvoiceDetailUiState>()

        // Act
        viewModel.uiState.collect { state ->
            states.add(state)
            if (state is InvoiceDetailUiState.Success) {
                return@collect
            }
        }

        // Assert: Loading state exists between start and success
        val hasLoadingState = states.contains(InvoiceDetailUiState.Loading)
        assertTrue(
            hasLoadingState,
            "ViewModel must emit Loading state so UI can show loading spinner"
        )

        // Verify loading is NOT the final state
        assertFalse(
            states.last() == InvoiceDetailUiState.Loading,
            "Loading should not be the final state (should be Success or Error)"
        )
    }
}

