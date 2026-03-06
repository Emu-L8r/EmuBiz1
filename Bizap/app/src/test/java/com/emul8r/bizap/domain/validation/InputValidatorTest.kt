package com.emul8r.bizap.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [InputValidator].
 *
 * Each test method covers a single validation function and exercises both the happy path
 * and the expected failure paths so that any regression in the rules is caught immediately.
 */
class InputValidatorTest {

    // ── validateInvoiceNumber ────────────────────────────────────────────────────

    @Test
    fun validateInvoiceNumber_validNumber_returnsSuccess() {
        val result = InputValidator.validateInvoiceNumber("INV-001")
        assertTrue(result.isSuccess())
        assertEquals("INV-001", result.getOrNull())
    }

    @Test
    fun validateInvoiceNumber_blank_returnsFailure() {
        val result = InputValidator.validateInvoiceNumber("   ")
        assertTrue(result.isFailure())
        assertEquals("Invoice number is required", result.getErrorOrNull())
    }

    @Test
    fun validateInvoiceNumber_tooLong_returnsFailure() {
        val result = InputValidator.validateInvoiceNumber("A".repeat(51))
        assertTrue(result.isFailure())
        assertEquals("Invoice number must be 50 characters or less", result.getErrorOrNull())
    }

    @Test
    fun validateInvoiceNumber_invalidCharacters_returnsFailure() {
        val result = InputValidator.validateInvoiceNumber("INV!001")
        assertTrue(result.isFailure())
        assertEquals("Invoice number contains invalid characters", result.getErrorOrNull())
    }

    @Test
    fun validateInvoiceNumber_maxLength_returnsSuccess() {
        val result = InputValidator.validateInvoiceNumber("A".repeat(50))
        assertTrue(result.isSuccess())
    }

    // ── validateEmail ────────────────────────────────────────────────────────────

    @Test
    fun validateEmail_validEmail_returnsSuccess() {
        val result = InputValidator.validateEmail("user@example.com")
        assertTrue(result.isSuccess())
        assertEquals("user@example.com", result.getOrNull())
    }

    @Test
    fun validateEmail_blank_returnsFailure() {
        val result = InputValidator.validateEmail("")
        assertTrue(result.isFailure())
        assertEquals("Email is required", result.getErrorOrNull())
    }

    @Test
    fun validateEmail_invalidFormat_returnsFailure() {
        val result = InputValidator.validateEmail("not-an-email")
        assertTrue(result.isFailure())
        assertEquals("Invalid email format", result.getErrorOrNull())
    }

    @Test
    fun validateEmail_noAtSymbol_returnsFailure() {
        val result = InputValidator.validateEmail("userexample.com")
        assertTrue(result.isFailure())
    }

    @Test
    fun validateEmail_tooLong_returnsFailure() {
        val longEmail = "a".repeat(250) + "@b.com"
        val result = InputValidator.validateEmail(longEmail)
        assertTrue(result.isFailure())
        assertEquals("Email is too long", result.getErrorOrNull())
    }

    // ── validateCustomerName ─────────────────────────────────────────────────────

    @Test
    fun validateCustomerName_validName_returnsSuccess() {
        val result = InputValidator.validateCustomerName("Acme Corporation")
        assertTrue(result.isSuccess())
        assertEquals("Acme Corporation", result.getOrNull())
    }

    @Test
    fun validateCustomerName_blank_returnsFailure() {
        val result = InputValidator.validateCustomerName("  ")
        assertTrue(result.isFailure())
        assertEquals("Customer name is required", result.getErrorOrNull())
    }

    @Test
    fun validateCustomerName_tooShort_returnsFailure() {
        val result = InputValidator.validateCustomerName("A")
        assertTrue(result.isFailure())
        assertEquals("Name must be at least 2 characters", result.getErrorOrNull())
    }

    @Test
    fun validateCustomerName_tooLong_returnsFailure() {
        val result = InputValidator.validateCustomerName("A".repeat(101))
        assertTrue(result.isFailure())
        assertEquals("Name must be 100 characters or less", result.getErrorOrNull())
    }

    @Test
    fun validateCustomerName_exactMinLength_returnsSuccess() {
        val result = InputValidator.validateCustomerName("AB")
        assertTrue(result.isSuccess())
    }

    @Test
    fun validateCustomerName_exactMaxLength_returnsSuccess() {
        val result = InputValidator.validateCustomerName("A".repeat(100))
        assertTrue(result.isSuccess())
    }

    // ── validatePhone ─────────────────────────────────────────────────────────────

    @Test
    fun validatePhone_null_returnsSuccessWithNull() {
        val result = InputValidator.validatePhone(null)
        assertTrue(result.isSuccess())
        assertNull(result.getOrNull())
    }

    @Test
    fun validatePhone_blank_returnsSuccessWithNull() {
        val result = InputValidator.validatePhone("  ")
        assertTrue(result.isSuccess())
        assertNull(result.getOrNull())
    }

    @Test
    fun validatePhone_validNumber_returnsSuccess() {
        val result = InputValidator.validatePhone("+1 555 123 4567")
        assertTrue(result.isSuccess())
    }

    @Test
    fun validatePhone_invalidFormat_returnsFailure() {
        val result = InputValidator.validatePhone("abc-def-ghij")
        assertTrue(result.isFailure())
        assertEquals("Invalid phone number format", result.getErrorOrNull())
    }

    // ── validateAmount ───────────────────────────────────────────────────────────

    @Test
    fun validateAmount_validAmount_returnsSuccess() {
        val result = InputValidator.validateAmount(1000L)
        assertTrue(result.isSuccess())
        assertEquals(1000L, result.getOrNull())
    }

    @Test
    fun validateAmount_zero_returnsFailure() {
        val result = InputValidator.validateAmount(0L)
        assertTrue(result.isFailure())
        assertEquals("Amount must be greater than zero", result.getErrorOrNull())
    }

    @Test
    fun validateAmount_negative_returnsFailure() {
        val result = InputValidator.validateAmount(-100L)
        assertTrue(result.isFailure())
    }

    @Test
    fun validateAmount_tooLarge_returnsFailure() {
        val result = InputValidator.validateAmount(1_000_000_000L)
        assertTrue(result.isFailure())
        assertEquals("Amount is too large", result.getErrorOrNull())
    }

    // ── validateQuantity ─────────────────────────────────────────────────────────

    @Test
    fun validateQuantity_validQuantity_returnsSuccess() {
        val result = InputValidator.validateQuantity(2.5)
        assertTrue(result.isSuccess())
        assertEquals(2.5, result.getOrNull()!!, 0.001)
    }

    @Test
    fun validateQuantity_zero_returnsFailure() {
        val result = InputValidator.validateQuantity(0.0)
        assertTrue(result.isFailure())
        assertEquals("Quantity must be greater than zero", result.getErrorOrNull())
    }

    @Test
    fun validateQuantity_tooLarge_returnsFailure() {
        val result = InputValidator.validateQuantity(100_000.0)
        assertTrue(result.isFailure())
        assertEquals("Quantity is too large", result.getErrorOrNull())
    }

    // ── validateTaxRate ──────────────────────────────────────────────────────────

    @Test
    fun validateTaxRate_validRate_returnsSuccess() {
        val result = InputValidator.validateTaxRate(10.0)
        assertTrue(result.isSuccess())
        assertEquals(10.0, result.getOrNull()!!, 0.001)
    }

    @Test
    fun validateTaxRate_zero_returnsSuccess() {
        val result = InputValidator.validateTaxRate(0.0)
        assertTrue(result.isSuccess())
    }

    @Test
    fun validateTaxRate_hundredPercent_returnsSuccess() {
        val result = InputValidator.validateTaxRate(100.0)
        assertTrue(result.isSuccess())
    }

    @Test
    fun validateTaxRate_negative_returnsFailure() {
        val result = InputValidator.validateTaxRate(-1.0)
        assertTrue(result.isFailure())
        assertEquals("Tax rate cannot be negative", result.getErrorOrNull())
    }

    @Test
    fun validateTaxRate_overHundred_returnsFailure() {
        val result = InputValidator.validateTaxRate(101.0)
        assertTrue(result.isFailure())
        assertEquals("Tax rate cannot exceed 100%", result.getErrorOrNull())
    }

    // ── Extension helpers ────────────────────────────────────────────────────────

    @Test
    fun extensionHelpers_successResult_workCorrectly() {
        val result: ValidationResult<String> = ValidationResult.Success("hello")
        assertTrue(result.isSuccess())
        assertTrue(!result.isFailure())
        assertEquals("hello", result.getOrNull())
        assertNull(result.getErrorOrNull())
    }

    @Test
    fun extensionHelpers_failureResult_workCorrectly() {
        val result: ValidationResult<String> = ValidationResult.Failure("bad input")
        assertTrue(result.isFailure())
        assertTrue(!result.isSuccess())
        assertNull(result.getOrNull())
        assertEquals("bad input", result.getErrorOrNull())
    }
}
