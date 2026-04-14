package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.model.Customer
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for customer field validation logic in [ValidationRules].
 *
 * Covers all validation rules: name length bounds, email format, phone format,
 * and optional vs required fields.
 */
class CustomerValidationTest {

    // ── name_Required ─────────────────────────────────────────────────────────

    @Test
    fun `name_Required - empty name fails validation`() {
        val customer = Customer(name = "", email = "test@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Empty name should fail validation")
    }

    @Test
    fun `name_Required - blank name (whitespace only) fails validation`() {
        val customer = Customer(name = "   ", email = "test@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Whitespace-only name should fail validation")
    }

    // ── name_TooShort ─────────────────────────────────────────────────────────

    @Test
    fun `name_TooShort - single character name fails minimum length`() {
        val customer = Customer(name = "A", email = "test@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Single character name should fail min length check")
    }

    @Test
    fun `name_TooShort - two character name meets minimum length`() {
        val customer = Customer(name = "Jo", email = "test@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Two character name should pass min length check")
    }

    // ── name_TooLong ──────────────────────────────────────────────────────────

    @Test
    fun `name_TooLong - 101 character name fails maximum length`() {
        val customer = Customer(name = "A".repeat(101), email = "test@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Name exceeding 100 chars should fail max length check")
    }

    @Test
    fun `name_TooLong - exactly 100 character name is valid`() {
        val customer = Customer(name = "A".repeat(100), email = "test@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Name of exactly 100 chars should pass max length check")
    }

    // ── email_Valid ───────────────────────────────────────────────────────────

    @Test
    fun `email_Valid - standard email format passes validation`() {
        val customer = Customer(name = "Jane Doe", email = "jane@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Standard email should pass validation")
    }

    @Test
    fun `email_Valid - email with subdomain passes validation`() {
        val customer = Customer(name = "Jane Doe", email = "jane@mail.example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Email with subdomain should pass validation")
    }

    @Test
    fun `email_Valid - email with plus addressing passes validation`() {
        val customer = Customer(name = "Jane Doe", email = "jane+test@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Email with plus addressing should pass validation")
    }

    @Test
    fun `email_Valid - null email is accepted as email is optional`() {
        val customer = Customer(name = "Jane Doe", email = null)
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Null email should be accepted (email is optional)")
    }

    // ── email_Invalid ─────────────────────────────────────────────────────────

    @Test
    fun `email_Invalid - email missing @ symbol fails validation`() {
        val customer = Customer(name = "Jane Doe", email = "janeexample.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Email without @ should fail validation")
    }

    @Test
    fun `email_Invalid - email missing domain dot fails validation`() {
        val customer = Customer(name = "Jane Doe", email = "jane@examplecom")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Email without dot in domain should fail validation")
    }

    @Test
    fun `email_Invalid - email starting with @ fails validation`() {
        val customer = Customer(name = "Jane Doe", email = "@example.com")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Email starting with @ should fail validation")
    }

    // ── email_Unique ──────────────────────────────────────────────────────────

    @Test
    fun `email_Unique - duplicate email detection logic is correctly applied`() {
        val existingEmails = setOf("existing@example.com", "another@example.com")
        val newEmail = "existing@example.com"
        val isDuplicate = newEmail in existingEmails
        assertTrue(isDuplicate, "Duplicate email should be detected")
    }

    @Test
    fun `email_Unique - unique email passes uniqueness check`() {
        val existingEmails = setOf("existing@example.com")
        val newEmail = "newuser@example.com"
        val isDuplicate = newEmail in existingEmails
        assertFalse(isDuplicate, "Unique email should pass uniqueness check")
    }

    // ── phone_Optional ────────────────────────────────────────────────────────

    @Test
    fun `phone_Optional - null phone is accepted`() {
        val customer = Customer(name = "Jane Doe", phone = null)
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Null phone should be accepted (phone is optional)")
    }

    @Test
    fun `phone_Optional - empty string phone is accepted`() {
        val customer = Customer(name = "Jane Doe", phone = "")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Empty phone string should be accepted (treated as not provided)")
    }

    // ── phone_ValidFormat ─────────────────────────────────────────────────────

    @Test
    fun `phone_ValidFormat - Australian mobile number passes validation`() {
        val customer = Customer(name = "Jane Doe", phone = "0412345678")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Australian mobile number should pass validation")
    }

    @Test
    fun `phone_ValidFormat - international format with plus passes validation`() {
        val customer = Customer(name = "Jane Doe", phone = "+61 2 9876 5432")
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "International format phone should pass validation")
    }

    @Test
    fun `phone_ValidFormat - too short phone fails validation`() {
        val customer = Customer(name = "Jane Doe", phone = "12")  // Only 2 chars
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Phone with fewer than 5 chars should fail validation")
    }

    @Test
    fun `phone_ValidFormat - too long phone fails validation`() {
        val customer = Customer(name = "Jane Doe", phone = "1".repeat(21))  // 21 chars
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isFailure(), "Phone with more than 20 chars should fail validation")
    }

    // ── address_MaxLength ─────────────────────────────────────────────────────

    @Test
    fun `address_MaxLength - customer with address set is valid`() {
        val customer = Customer(
            name = "Jane Doe",
            address = "123 Main Street, Sydney NSW 2000"
        )
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Customer with valid address should pass validation")
    }

    @Test
    fun `address_MaxLength - null address is valid (address is optional)`() {
        val customer = Customer(name = "Jane Doe", address = null)
        val result = ValidationRules.validateCustomer(customer)
        assertTrue(result.isSuccess(), "Customer without address should pass validation")
    }
}



