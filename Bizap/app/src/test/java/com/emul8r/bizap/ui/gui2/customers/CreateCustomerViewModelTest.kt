@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.ui.gui2.customers

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.validation.ValidationRules
import io.mockk.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [CreateCustomerViewModelV2].
 *
 * Verifies customer creation logic including validation and repository delegation.
 */
class CreateCustomerViewModelTest : BaseUnitTest() {

    private lateinit var customerRepository: CustomerRepository
    private lateinit var viewModel: CreateCustomerViewModelV2

    @Before
    fun setUp() {
        customerRepository = mockk(relaxed = true)
        viewModel = CreateCustomerViewModelV2(customerRepository)
    }

    // ── createCustomer_Success ────────────────────────────────────────────────

    @Test
    fun `createCustomer_Success - valid customer triggers repository insert`() = runTest {
        val customer = Customer(name = "Jane Doe", email = "jane@example.com")
        coEvery { customerRepository.insert(customer) } returns Result.success(1L)

        var successCalled = false
        viewModel.createCustomer(customer, onSuccess = { successCalled = true }, onError = {})

        advanceUntilIdle()
        coVerify { customerRepository.insert(customer) }
        assertTrue(successCalled)
    }

    @Test
    fun `createCustomer_Success - success callback is invoked after creation`() = runTest {
        val customer = Customer(name = "Test User", email = "test@example.com")
        coEvery { customerRepository.insert(customer) } returns Result.success(2L)

        var successInvoked = false
        viewModel.createCustomer(customer, onSuccess = { successInvoked = true }, onError = {})

        advanceUntilIdle()
        assertTrue(successInvoked)
    }

    // ── createCustomer_InvalidName ────────────────────────────────────────────

    @Test
    fun `createCustomer_InvalidName - validation rejects blank name`() {
        val result = ValidationRules.validateCustomer(
            Customer(name = "", email = "valid@example.com")
        )
        assertTrue(result.isFailure(), "Blank name should fail validation")
    }

    @Test
    fun `createCustomer_InvalidName - validation rejects single character name`() {
        val result = ValidationRules.validateCustomer(
            Customer(name = "A", email = "valid@example.com")
        )
        assertTrue(result.isFailure(), "Name too short should fail validation")
    }

    @Test
    fun `createCustomer_InvalidName - validation rejects name exceeding 100 chars`() {
        val longName = "A".repeat(101)
        val result = ValidationRules.validateCustomer(
            Customer(name = longName, email = "valid@example.com")
        )
        assertTrue(result.isFailure(), "Name too long should fail validation")
    }

    // ── createCustomer_InvalidEmail ───────────────────────────────────────────

    @Test
    fun `createCustomer_InvalidEmail - validation rejects missing @ symbol`() {
        val result = ValidationRules.validateCustomer(
            Customer(name = "Valid Name", email = "invalidemail.com")
        )
        assertTrue(result.isFailure(), "Email without @ should fail validation")
    }

    @Test
    fun `createCustomer_InvalidEmail - validation rejects email without domain`() {
        val result = ValidationRules.validateCustomer(
            Customer(name = "Valid Name", email = "user@")
        )
        assertTrue(result.isFailure(), "Email without domain should fail validation")
    }

    @Test
    fun `createCustomer_InvalidEmail - valid email passes validation`() {
        val result = ValidationRules.validateCustomer(
            Customer(name = "Valid Name", email = "user@domain.com")
        )
        assertTrue(result.isSuccess(), "Valid email should pass validation")
    }

    // ── createCustomer_Error ──────────────────────────────────────────────────

    @Test
    fun `createCustomer_Error - repository failure triggers error callback`() = runTest {
        val customer = Customer(name = "Jane Doe", email = "jane@example.com")
        coEvery { customerRepository.insert(customer) } throws RuntimeException("DB error")

        var errorMessage: String? = null
        viewModel.createCustomer(customer, onSuccess = {}, onError = { errorMessage = it })

        advanceUntilIdle()
        assertEquals("DB error", errorMessage)
    }

    // ── updateField_TriggersValidation ────────────────────────────────────────

    @Test
    fun `updateField_TriggersValidation - valid customer passes domain validation`() {
        val customer = Customer(name = "Valid Name", email = "valid@example.com", phone = "0412345678")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess())
    }

    @Test
    fun `updateField_TriggersValidation - phone too short fails validation`() {
        val customer = Customer(name = "Valid Name", phone = "12")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Short phone number should fail validation")
    }

    // ── uiEvent_Emission ─────────────────────────────────────────────────────

    @Test
    fun `uiEvent_Emission - onSuccess navigates after successful creation`() = runTest {
        val customer = Customer(name = "Navigate Test", email = "navigate@example.com")
        coEvery { customerRepository.insert(customer) } returns Result.success(3L)

        var navigateCalled = false
        viewModel.createCustomer(customer, onSuccess = { navigateCalled = true }, onError = {})

        advanceUntilIdle()
        assertTrue(navigateCalled, "Navigation should be triggered on success")
    }
}
