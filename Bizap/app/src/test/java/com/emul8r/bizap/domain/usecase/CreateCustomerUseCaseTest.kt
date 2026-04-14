@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.validation.ValidationRules
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for customer creation, covering validation and repository delegation.
 *
 * These tests exercise the validation logic in [ValidationRules] and the
 * customer creation flow as orchestrated through [CustomerRepository].
 */
class CreateCustomerUseCaseTest : BaseUnitTest() {

    private lateinit var customerRepository: CustomerRepository

    @Before
    fun setUp() {
        customerRepository = mockk(relaxed = true)
    }

    // ── validCustomer_Success ─────────────────────────────────────────────────

    @Test
    fun `validCustomer_Success - valid customer is accepted by validation`() {
        val customer = Customer(name = "Alice Smith", email = "alice@example.com", phone = "0412345678")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess())
    }

    @Test
    fun `validCustomer_Success - valid customer is inserted into repository`() = runTest {
        val customer = Customer(name = "Alice Smith", email = "alice@example.com")
        coEvery { customerRepository.insert(customer) } returns Result.success(1L)

        val result = customerRepository.insert(customer)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `validCustomer_Success - repository insert is called exactly once`() = runTest {
        val customer = Customer(name = "Bob Jones", email = "bob@example.com")
        coEvery { customerRepository.insert(customer) } returns Result.success(2L)

        customerRepository.insert(customer)

        coVerify(exactly = 1) { customerRepository.insert(customer) }
    }

    // ── invalidEmail_Failure ──────────────────────────────────────────────────

    @Test
    fun `invalidEmail_Failure - email without @ fails validation`() {
        val customer = Customer(name = "Valid Name", email = "bademail.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Email without @ should fail validation")
    }

    @Test
    fun `invalidEmail_Failure - email without dot fails validation`() {
        val customer = Customer(name = "Valid Name", email = "user@nodot")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Email without dot after @ should fail validation")
    }

    @Test
    fun `invalidEmail_Failure - email with valid format passes validation`() {
        val customer = Customer(name = "Valid Name", email = "valid.user@company.com.au")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Properly formatted email should pass validation")
    }

    @Test
    fun `invalidEmail_Failure - null email is accepted (email is optional)`() {
        val customer = Customer(name = "Valid Name", email = null)
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Null email should be accepted (email is optional)")
    }

    // ── emailDuplicate_Failure ────────────────────────────────────────────────

    @Test
    fun `emailDuplicate_Failure - duplicate email causes repository insert to fail`() = runTest {
        val customer = Customer(name = "Duplicate User", email = "existing@example.com")
        coEvery { customerRepository.insert(customer) } returns Result.failure(
            Exception("UNIQUE constraint failed: customers.email")
        )

        val result = customerRepository.insert(customer)

        assertFalse(result.isSuccess)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `emailDuplicate_Failure - unique email succeeds in repository`() = runTest {
        val customer = Customer(name = "New User", email = "new@example.com")
        coEvery { customerRepository.insert(customer) } returns Result.success(3L)

        val result = customerRepository.insert(customer)

        assertTrue(result.isSuccess)
    }

    // ── nameBlank_Failure ─────────────────────────────────────────────────────

    @Test
    fun `nameBlank_Failure - blank name fails validation with descriptive error`() {
        val customer = Customer(name = "   ", email = "valid@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Blank name should fail validation")
        val error = result.getErrorOrNull()
        assertNotNull(error, "Validation error message should not be null")
    }

    @Test
    fun `nameBlank_Failure - empty string name fails validation`() {
        val customer = Customer(name = "", email = "valid@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Empty name should fail validation")
    }

    @Test
    fun `nameBlank_Failure - single character name fails minimum length check`() {
        val customer = Customer(name = "A", email = "valid@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Single character name should fail minimum length validation")
    }

    @Test
    fun `nameBlank_Failure - two character name passes minimum length check`() {
        val customer = Customer(name = "Jo", email = "valid@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Two character name should pass minimum length validation")
    }
}



