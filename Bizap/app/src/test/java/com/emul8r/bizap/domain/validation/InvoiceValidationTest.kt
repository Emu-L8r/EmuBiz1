package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.util.TestDataFactory
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Unit tests for Invoice validation
 * Verifies that domain model validation rules are enforced.
 */
class InvoiceValidationTest : BaseUnitTest() {
    
    @Test
    fun `test valid invoice passes validation`() {
        val invoice = TestDataFactory.createTestInvoice(
            businessProfileId = 1,
            total = 1000.0
        ).copy(
            items = listOf(LineItem(description = "Test Service", quantity = 1.0, unitPrice = 1000.0))
        )
        
        // Act & Assert
        invoice.validate() // Should not throw
        assertTrue(true)
    }
    
    @Test
    fun `test invoice with invalid business id fails`() {
        val invoice = TestDataFactory.createTestInvoice(businessProfileId = 0)
        
        assertFailsWith<IllegalArgumentException> {
            invoice.validate()
        }
    }
    
    @Test
    fun `test invoice with invalid currency code fails`() {
        val invoice = TestDataFactory.createTestInvoice().copy(currencyCode = "AU") // Too short
        
        assertFailsWith<IllegalArgumentException> {
            invoice.validate()
        }
    }
}
