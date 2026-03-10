package com.emul8r.bizap.ui.gui2.customers

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for CreateCustomerViewModelV2
 * Tests customer creation and validation
 */
class CreateCustomerViewModelV2Test : BaseUnitTest() {

    private lateinit var viewModel: CreateCustomerViewModelV2
    private val customerRepository = mockk<CustomerRepository>()

    @Before
    fun setup() {
        viewModel = CreateCustomerViewModelV2(customerRepository)
    }

    // ========================================
    // Customer Creation Tests
    // ========================================

    @Test
    fun `createCustomer - should successfully create customer`() = runTest {
        // Given
        val customer = Customer(
            id = 0L,
            name = "Test Customer",
            email = "test@example.com",
            phone = "1234567890",
            address = "123 Test St"
        )

        coEvery { customerRepository.insert(customer) } returns Result.success(1L)

        var successCalled = false
        var errorCalled = false

        // When
        viewModel.createCustomer(
            customer = customer,
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { customerRepository.insert(customer) }
        assertTrue(successCalled)
        assertEquals(false, errorCalled)
    }

    @Test
    fun `createCustomer - should handle creation error`() = runTest {
        // Given
        val customer = Customer(
            id = 0L,
            name = "Test Customer",
            email = "test@example.com",
            phone = "1234567890",
            address = "123 Test St"
        )

        coEvery { customerRepository.insert(customer) } throws Exception("Database error")

        var successCalled = false
        var errorMessage: String? = null

        // When
        viewModel.createCustomer(
            customer = customer,
            onSuccess = { successCalled = true },
            onError = { error -> errorMessage = error }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(false, successCalled)
        assertEquals("Database error", errorMessage)
    }

    @Test
    fun `createCustomer - should handle null error message`() = runTest {
        // Given
        val customer = Customer(
            id = 0L,
            name = "Test Customer",
            email = "test@example.com",
            phone = "1234567890",
            address = "123 Test St"
        )

        coEvery { customerRepository.insert(customer) } throws Exception()

        var errorMessage: String? = null

        // When
        viewModel.createCustomer(
            customer = customer,
            onSuccess = { },
            onError = { error -> errorMessage = error }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Unknown error", errorMessage)
    }

    @Test
    fun `createCustomer - should create customer with minimal data`() = runTest {
        // Given
        val customer = Customer(
            id = 0L,
            name = "Minimal",
            email = null,
            phone = null,
            address = null
        )

        coEvery { customerRepository.insert(customer) } returns Result.success(2L)

        var successCalled = false

        // When
        viewModel.createCustomer(
            customer = customer,
            onSuccess = { successCalled = true },
            onError = { }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(successCalled)
        coVerify { customerRepository.insert(customer) }
    }
}

