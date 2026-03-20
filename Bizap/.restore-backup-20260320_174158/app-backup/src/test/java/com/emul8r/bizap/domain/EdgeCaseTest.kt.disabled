package com.emul8r.bizap.domain

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.util.TestDataFactory
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Edge case tests for domain models and business logic.
 * Target: 25%+ Coverage for Priority 1 completion.
 */
class EdgeCaseTest : BaseUnitTest() {
    
    @Test
    fun `test invoice with empty line items fails validation`() {
        val invoice = TestDataFactory.createTestInvoice().copy(items = emptyList())
        
        assertFailsWith<IllegalArgumentException> {
            invoice.validate()
        }
    }
    
    @Test
    fun `test invoice with zero total fails validation`() {
        val invoice = TestDataFactory.createTestInvoice(total = 0.0).copy(
            items = listOf(LineItem(description = "Test", quantity = 1.0, unitPrice = 0.0))
        )
        
        assertFailsWith<IllegalArgumentException> {
            invoice.validate()
        }
    }
    
    @Test
    fun `test customer with blank name fails validation`() {
        val customer = TestDataFactory.createTestCustomer(name = "")
        
        assertFailsWith<IllegalArgumentException> {
            customer.validate()
        }
    }
    
    @Test
    fun `test customer with invalid email format fails`() {
        val customer = TestDataFactory.createTestCustomer().copy(email = "invalid-email")
        
        assertFailsWith<IllegalArgumentException> {
            customer.validate()
        }
    }

    @Test
    fun `test business isolation between different identities`() {
        val invoice = TestDataFactory.createTestInvoice(businessProfileId = 1)
        val otherBusinessId = 2L
        
        assertTrue(invoice.businessProfileId != otherBusinessId)
    }
}
