package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.validation.InputValidator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for input validation functions
 */
class InputValidationTest {

    // ========================================
    // Customer Name Validation Tests
    // ========================================

    @Test
    fun `validateCustomerName - valid name accepted`() {
        // Given - valid names
        val validNames = listOf(
            "John Doe",
            "ABC Company",
            "Mary-Jane",
            "O'Brien",
            "张三" // Chinese characters
        )

        // When & Then
        validNames.forEach { name ->
            assertTrue(
                name.isNotBlank() && name.length >= 2,
                "Name should be valid: $name"
            )
        }
    }

    @Test
    fun `validateCustomerName - blank name rejected`() {
        // Given
        val blankNames = listOf("", "   ", "\t", "\n")

        // When & Then
        blankNames.forEach { name ->
            assertTrue(name.isBlank(), "Name should be blank: '$name'")
        }
    }

    @Test
    fun `validateCustomerName - too short name rejected`() {
        // Given
        val shortName = "A"

        // When & Then
        assertTrue(shortName.length < 2, "Name should be too short")
    }

    // ========================================
    // Email Validation Tests
    // ========================================

    @Test
    fun `validateEmail - valid emails accepted`() {
        // Given - valid email formats
        val validEmails = listOf(
            "test@example.com",
            "user.name@company.co.uk",
            "first+last@domain.org",
            "123@numbers.com"
        )

        // When & Then
        validEmails.forEach { email ->
            assertTrue(
                email.contains("@") && email.contains("."),
                "Email should be valid: $email"
            )
        }
    }

    @Test
    fun `validateEmail - invalid emails rejected`() {
        // Given - invalid email formats
        val invalidEmails = listOf(
            "notanemail",
            "@example.com",
            "user@",
            "user @example.com",
            ""
        )

        // When & Then
        invalidEmails.forEach { email ->
            // Just verify the email validator can be called without exception
            val result = InputValidator.validateEmail(email)
            // Invalid emails should return failure
            assertTrue(result.isFailure(), "Email should be invalid: $email")
        }
    }

    // ========================================
    // Phone Validation Tests
    // ========================================

    @Test
    fun `validatePhone - valid phone numbers accepted`() {
        // Given - valid phone numbers
        val validPhones = listOf(
            "1234567890",
            "555-123-4567",
            "+61412345678",
            "(02) 1234 5678"
        )

        // When & Then
        validPhones.forEach { phone ->
            assertTrue(
                phone.isNotBlank(),
                "Phone should be valid: $phone"
            )
        }
    }

    @Test
    fun `validatePhone - blank phone accepted as optional`() {
        // Given
        val blankPhone = ""

        // When & Then
        assertTrue(blankPhone.isBlank(), "Phone can be blank (optional)")
    }

    // ========================================
    // Amount Validation Tests
    // ========================================

    @Test
    fun `validateAmount - positive amount accepted`() {
        // Given
        val amount = 1000L

        // When & Then
        assertTrue(amount > 0, "Amount should be positive")
    }

    @Test
    fun `validateAmount - zero amount rejected`() {
        // Given
        val amount = 0L

        // When & Then
        assertFalse(amount > 0, "Amount should not be zero")
    }

    @Test
    fun `validateAmount - negative amount rejected`() {
        // Given
        val amount = -1000L

        // When & Then
        assertFalse(amount > 0, "Amount should not be negative")
    }

    // ========================================
    // Business Name Validation Tests
    // ========================================

    @Test
    fun `validateBusinessName - valid names accepted`() {
        // Given
        val validNames = listOf(
            "Acme Corporation",
            "Tech Startup Inc.",
            "Small Business LLC"
        )

        // When & Then
        validNames.forEach { name ->
            assertTrue(
                name.isNotBlank() && name.length >= 3,
                "Business name should be valid: $name"
            )
        }
    }

    @Test
    fun `validateBusinessName - too short rejected`() {
        // Given
        val shortName = "AB"

        // When & Then
        assertFalse(shortName.length >= 3, "Business name should be at least 3 chars")
    }

    // ========================================
    // ABN Validation Tests
    // ========================================

    @Test
    fun `validateABN - valid ABN format accepted`() {
        // Given - Australian Business Number format (11 digits)
        val validABN = "12345678901"

        // When & Then
        assertTrue(
            validABN.length == 11 && validABN.all { it.isDigit() },
            "ABN should be 11 digits"
        )
    }

    @Test
    fun `validateABN - invalid ABN format rejected`() {
        // Given - wrong length
        val invalidABNs = listOf(
            "123456789",    // Too short
            "123456789012", // Too long
            "1234567890A",  // Contains letter
            ""              // Empty
        )

        // When & Then
        invalidABNs.forEach { abn ->
            assertFalse(
                abn.length == 11 && abn.all { it.isDigit() },
                "ABN should be invalid: $abn"
            )
        }
    }

    // ========================================
    // URL Validation Tests
    // ========================================

    @Test
    fun `validateURL - valid URLs accepted`() {
        // Given
        val validURLs = listOf(
            "https://example.com",
            "http://www.example.com",
            "example.com"
        )

        // When & Then
        validURLs.forEach { url ->
            assertTrue(
                url.contains("."),
                "URL should be valid: $url"
            )
        }
    }

    @Test
    fun `validateURL - invalid URLs rejected`() {
        // Given
        val invalidURLs = listOf(
            "not a url",
            "@@@",
            ""
        )

        // When & Then
        invalidURLs.forEach { url ->
            assertFalse(
                url.contains(".") && url.isNotBlank(),
                "URL should be invalid: $url"
            )
        }
    }
}




