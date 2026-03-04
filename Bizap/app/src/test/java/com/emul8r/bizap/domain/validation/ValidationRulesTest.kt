package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.domain.model.Result
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * VALIDATION RULES TEST SUITE
 *
 * Tests cover:
 * 1. Happy path (all valid data)
 * 2. Each validation rule failure
 * 3. Edge cases (boundary values)
 * 4. Collection validation (batch operations)
 *
 * TESTING STRATEGY:
 * =================
 * - Test each rule independently
 * - Use realistic data
 * - Cover both success and failure paths
 * - Check error messages are helpful
 * - Use descriptive test names
 */
class ValidationRulesTest {

    // ===============================
    // INVOICE VALIDATION TESTS
    // ===============================

    @Test
    fun validateInvoice_validInvoice_returnsSuccess() {
        // ARRANGE: Create a completely valid invoice
        val invoice = Invoice(
            businessProfileId = 1,
            customerId = 1,
            customerName = "John Doe",
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 86400000,  // 1 day later
            totalAmount = 10000,  // $100 in cents
            items = listOf(
                LineItem(description = "Service A", quantity = 1.0, unitPrice = 10000)
            ),
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            currencyCode = "AUD"
        )

        // ACT
        val result = ValidationRules.validateInvoice(invoice)

        // ASSERT
        assertTrue("Invoice should be valid", result.isSuccess())
        assertFalse("Invoice should not have errors", result.isFailure())
    }

    @Test
    fun validateInvoice_emptyItems_returnsFailure() {
        // ARRANGE: Invoice with NO line items
        val invoice = Invoice(
            businessProfileId = 1,
            customerId = 1,
            customerName = "John Doe",
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 86400000,
            totalAmount = 0,  // No items = no amount
            items = emptyList(),  // ❌ EMPTY!
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            currencyCode = "AUD"
        )

        // ACT
        val result = ValidationRules.validateInvoice(invoice)

        // ASSERT
        assertTrue("Invoice should fail validation", result.isFailure())
        assertTrue(
            "Error should mention line items",
            result.getErrorOrNull()?.contains("line item") ?: false
        )
    }

    @Test
    fun validateInvoice_zeroAmount_returnsFailure() {
        // ARRANGE: Invoice with $0 total
        val invoice = Invoice(
            businessProfileId = 1,
            customerId = 1,
            customerName = "John Doe",
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 86400000,
            totalAmount = 0,  // ❌ ZERO!
            items = listOf(
                LineItem(description = "Service", quantity = 1.0, unitPrice = 0)
            ),
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            currencyCode = "AUD"
        )

        // ACT
        val result = ValidationRules.validateInvoice(invoice)

        // ASSERT
        assertTrue("Invoice should fail validation", result.isFailure())
        assertTrue(
            "Error should mention positive amount",
            result.getErrorOrNull()?.contains("greater than zero") ?: false
        )
    }

    @Test
    fun validateInvoice_dueDateBeforeInvoiceDate_returnsFailure() {
        val now = System.currentTimeMillis()

        // ARRANGE: Due date is BEFORE invoice date (impossible!)
        val invoice = Invoice(
            businessProfileId = 1,
            customerId = 1,
            customerName = "John Doe",
            date = now,
            dueDate = now - 86400000,  // ❌ YESTERDAY!
            totalAmount = 10000,
            items = listOf(
                LineItem(description = "Service", quantity = 1.0, unitPrice = 10000)
            ),
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            currencyCode = "AUD"
        )

        // ACT
        val result = ValidationRules.validateInvoice(invoice)

        // ASSERT
        assertTrue("Invoice should fail validation", result.isFailure())
        assertTrue(
            "Error should mention due date",
            result.getErrorOrNull()?.contains("due date") ?: false
        )
    }

    @Test
    fun validateInvoice_blankCustomerName_returnsFailure() {
        // ARRANGE: No customer name
        val invoice = Invoice(
            businessProfileId = 1,
            customerId = 1,
            customerName = "",  // ❌ BLANK!
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 86400000,
            totalAmount = 10000,
            items = listOf(
                LineItem(description = "Service", quantity = 1.0, unitPrice = 10000)
            ),
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            currencyCode = "AUD"
        )

        // ACT
        val result = ValidationRules.validateInvoice(invoice)

        // ASSERT
        assertTrue("Invoice should fail validation", result.isFailure())
        assertTrue(
            "Error should mention customer name",
            result.getErrorOrNull()?.contains("customer") ?: false
        )
    }

    @Test
    fun validateInvoice_invalidCurrencyCode_returnsFailure() {
        // ARRANGE: Currency code must be exactly 3 letters
        val invoice = Invoice(
            businessProfileId = 1,
            customerId = 1,
            customerName = "John Doe",
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 86400000,
            totalAmount = 10000,
            items = listOf(
                LineItem(description = "Service", quantity = 1.0, unitPrice = 10000)
            ),
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            currencyCode = "US"  // ❌ Only 2 letters!
        )

        // ACT
        val result = ValidationRules.validateInvoice(invoice)

        // ASSERT
        assertTrue("Invoice should fail validation", result.isFailure())
        assertTrue(
            "Error should mention currency code",
            result.getErrorOrNull()?.contains("currency") ?: false
        )
    }

    // ===============================
    // CUSTOMER VALIDATION TESTS
    // ===============================

    @Test
    fun validateCustomer_validCustomer_returnsSuccess() {
        // ARRANGE
        val customer = Customer(
            name = "John Doe",
            email = "john@example.com",
            phone = "+61298765432"
        )

        // ACT
        val result = ValidationRules.validateCustomer(customer)

        // ASSERT
        assertTrue("Customer should be valid", result.isSuccess())
    }

    @Test
    fun validateCustomer_blankName_returnsFailure() {
        // ARRANGE
        val customer = Customer(
            name = "",  // ❌ BLANK!
            email = "john@example.com"
        )

        // ACT
        val result = ValidationRules.validateCustomer(customer)

        // ASSERT
        assertTrue("Customer should fail validation", result.isFailure())
    }

    @Test
    fun validateCustomer_nameTooShort_returnsFailure() {
        // ARRANGE: Name must be at least 2 characters
        val customer = Customer(
            name = "A",  // ❌ Only 1 character!
            email = "john@example.com"
        )

        // ACT
        val result = ValidationRules.validateCustomer(customer)

        // ASSERT
        assertTrue("Customer should fail validation", result.isFailure())
        assertTrue(
            "Error should mention minimum length",
            result.getErrorOrNull()?.contains("2 characters") ?: false
        )
    }

    @Test
    fun validateCustomer_nameTooLong_returnsFailure() {
        // ARRANGE: Name must be <= 100 characters
        val customer = Customer(
            name = "A".repeat(101),  // ❌ 101 characters!
            email = "john@example.com"
        )

        // ACT
        val result = ValidationRules.validateCustomer(customer)

        // ASSERT
        assertTrue("Customer should fail validation", result.isFailure())
    }

    @Test
    fun validateCustomer_invalidEmail_returnsFailure() {
        // ARRANGE: Email must have @ and dot
        val customer = Customer(
            name = "John Doe",
            email = "not-an-email"  // ❌ No @ or dot!
        )

        // ACT
        val result = ValidationRules.validateCustomer(customer)

        // ASSERT
        assertTrue("Customer should fail validation", result.isFailure())
        assertTrue(
            "Error should mention invalid email",
            result.getErrorOrNull()?.contains("email") ?: false
        )
    }

    @Test
    fun validateCustomer_invalidPhone_returnsFailure() {
        // ARRANGE: Phone must be 5-20 characters
        val customer = Customer(
            name = "John Doe",
            phone = "123"  // ❌ Too short!
        )

        // ACT
        val result = ValidationRules.validateCustomer(customer)

        // ASSERT
        assertTrue("Customer should fail validation", result.isFailure())
    }

    @Test
    fun validateCustomer_optionalEmailBlank_returnsSuccess() {
        // ARRANGE: Email is optional - blank is OK
        val customer = Customer(
            name = "John Doe",
            email = ""  // ✅ Blank is OK for optional field
        )

        // ACT
        val result = ValidationRules.validateCustomer(customer)

        // ASSERT
        assertTrue("Customer should be valid", result.isSuccess())
    }

    // ===============================
    // LINE ITEM VALIDATION TESTS
    // ===============================

    @Test
    fun validateLineItem_validItem_returnsSuccess() {
        // ARRANGE
        val item = LineItem(
            description = "Consulting Services",
            quantity = 2.5,
            unitPrice = 10000  // $100 in cents
        )

        // ACT
        val result = ValidationRules.validateLineItem(item)

        // ASSERT
        assertTrue("Line item should be valid", result.isSuccess())
    }

    @Test
    fun validateLineItem_blankDescription_returnsFailure() {
        // ARRANGE
        val item = LineItem(
            description = "",  // ❌ BLANK!
            quantity = 1.0,
            unitPrice = 10000
        )

        // ACT
        val result = ValidationRules.validateLineItem(item)

        // ASSERT
        assertTrue("Line item should fail validation", result.isFailure())
    }

    @Test
    fun validateLineItem_zeroQuantity_returnsFailure() {
        // ARRANGE
        val item = LineItem(
            description = "Service",
            quantity = 0.0,  // ❌ ZERO!
            unitPrice = 10000
        )

        // ACT
        val result = ValidationRules.validateLineItem(item)

        // ASSERT
        assertTrue("Line item should fail validation", result.isFailure())
    }

    @Test
    fun validateLineItem_negativePrice_returnsFailure() {
        // ARRANGE
        val item = LineItem(
            description = "Service",
            quantity = 1.0,
            unitPrice = -5000  // ❌ NEGATIVE!
        )

        // ACT
        val result = ValidationRules.validateLineItem(item)

        // ASSERT
        assertTrue("Line item should fail validation", result.isFailure())
    }

    @Test
    fun validateLineItem_excessiveTotal_returnsFailure() {
        // ARRANGE: Total cannot exceed $1,000,000
        val item = LineItem(
            description = "Service",
            quantity = 1000000.0,
            unitPrice = 1000000  // ❌ Unreasonably large!
        )

        // ACT
        val result = ValidationRules.validateLineItem(item)

        // ASSERT
        assertTrue("Line item should fail validation", result.isFailure())
    }

    // ===============================
    // RESULT PATTERN TESTS
    // ===============================

    @Test
    fun result_map_transformsSuccessData() {
        // ARRANGE
        val result: Result<Int> = Result.Success(5)

        // ACT
        val transformed = result.map { it * 2 }

        // ASSERT
        assertEquals("Map should transform data", Result.Success(10), transformed)
    }

    @Test
    fun result_map_preservesFailure() {
        // ARRANGE
        val result: Result<Int> = Result.Failure("Error")

        // ACT
        val transformed = result.map { it * 2 }

        // ASSERT
        assertTrue("Map should preserve failure", transformed.isFailure())
    }

    @Test
    fun result_fold_handlesSuccessAndFailure() {
        // ARRANGE - Success case
        val successResult: Result<Int> = Result.Success(5)

        // ACT
        val successValue = successResult.fold(
            onSuccess = { it * 2 },
            onFailure = { -1 }
        )

        // ASSERT
        assertEquals("Fold should call onSuccess", 10, successValue)

        // ARRANGE - Failure case
        val failureResult: Result<Int> = Result.Failure("Error")

        // ACT
        val failureValue = failureResult.fold(
            onSuccess = { it * 2 },
            onFailure = { -1 }
        )

        // ASSERT
        assertEquals("Fold should call onFailure", -1, failureValue)
    }

    // ===============================
    // BATCH VALIDATION TESTS
    // ===============================

    @Test
    fun validateCustomers_allValid_returnsSuccess() {
        // ARRANGE
        val customers = listOf(
            Customer(name = "John Doe", email = "john@example.com"),
            Customer(name = "Jane Smith", email = "jane@example.com"),
            Customer(name = "Bob Johnson", email = "bob@example.com")
        )

        // ACT
        val result = ValidationRules.validateCustomers(customers)

        // ASSERT
        assertTrue("All customers should be valid", result.isSuccess())
    }

    @Test
    fun validateCustomers_oneInvalid_returnsFailure() {
        // ARRANGE
        val customers = listOf(
            Customer(name = "John Doe", email = "john@example.com"),
            Customer(name = "A", email = "jane@example.com"),  // ❌ Invalid!
            Customer(name = "Bob Johnson", email = "bob@example.com")
        )

        // ACT
        val result = ValidationRules.validateCustomers(customers)

        // ASSERT
        assertTrue("Batch should fail if any customer is invalid", result.isFailure())
    }
}

