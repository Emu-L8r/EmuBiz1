package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem

/**
 * TEST DATA FACTORY
 *
 * Centralized factory for creating realistic test data.
 * Benefits:
 *  ✅ DRY: Create test data once, use in many tests
 *  ✅ Maintainable: Change data format in one place
 *  ✅ Realistic: Uses real-world values
 *  ✅ Flexible: Easy to override specific fields
 *
 * USAGE:
 * ======
 * val invoice = TestDataFactory.createValidInvoice()
 * val modified = invoice.copy(totalAmount = 0)  // Override one field
 *
 * WHY FACTORY PATTERN?
 * ====================
 * Without factory (BAD):
 *   @Test fun test1() {
 *       val invoice = Invoice(...)  // 20 lines of setup
 *   }
 *   @Test fun test2() {
 *       val invoice = Invoice(...)  // Same 20 lines again!
 *   }
 *
 * With factory (GOOD):
 *   @Test fun test1() {
 *       val invoice = TestDataFactory.createValidInvoice()
 *   }
 *   @Test fun test2() {
 *       val invoice = TestDataFactory.createValidInvoice()
 *   }
 */
object TestDataFactory {

    // ===============================
    // BASE TIME UTILITIES
    // ===============================

    /**
     * Get a consistent "now" for testing
     *
     * WHY CONSISTENT TIME?
     * ====================
     * System.currentTimeMillis() returns different values each time.
     * This makes tests non-deterministic (different result each run).
     *
     * EXAMPLE (BAD):
     *   val invoice1 = Invoice(date = System.currentTimeMillis())
     *   val invoice2 = Invoice(date = System.currentTimeMillis())
     *   invoice1.date != invoice2.date  // Different times!
     *
     * EXAMPLE (GOOD):
     *   val now = System.currentTimeMillis()
     *   val invoice1 = Invoice(date = now)
     *   val invoice2 = Invoice(date = now)
     *   invoice1.date == invoice2.date  // Same time, consistent!
     */
    private fun getNow(): Long = 1709596800000  // Fixed timestamp for testing

    /**
     * Get a valid due date (30 days after invoice date)
     */
    private fun getValidDueDate(invoiceDate: Long): Long = invoiceDate + (30L * 24 * 60 * 60 * 1000)

    // ===============================
    // INVOICE FACTORIES
    // ===============================

    /**
     * Create a VALID invoice with all required fields
     *
     * Use this as your base in tests, then modify specific fields:
     *
     * EXAMPLE:
     *   val invoice = createValidInvoice()
     *   val invoiceWithZeroAmount = invoice.copy(totalAmount = 0)
     *   val invoiceWithNoItems = invoice.copy(items = emptyList())
     *
     * FIELDS:
     *   businessProfileId: 1 (any positive number works)
     *   customerId: 1 (any positive number works)
     *   customerName: "Test Customer" (not blank, 2-100 chars)
     *   date: now (current time)
     *   dueDate: now + 30 days (valid: >= date)
     *   totalAmount: 10000 (positive, in cents = $100)
     *   items: [one test item] (not empty)
     *   isQuote: false (not a quote)
     *   status: DRAFT (standard status)
     *   currencyCode: "AUD" (3 letters, valid ISO 4217)
     */
    fun createValidInvoice(): Invoice {
        val now = getNow()

        return Invoice(
            businessProfileId = 1,
            customerId = 1,
            customerName = "Test Customer",
            date = now,
            dueDate = getValidDueDate(now),
            totalAmount = 10000,                    // $100 in cents
            items = listOf(createValidLineItem()),  // At least one item
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            currencyCode = "AUD"
        )
    }

    /**
     * Create an invoice with EMPTY ITEMS
     *
     * Use when testing: "invoices must have at least one item"
     */
    fun createInvoiceWithEmptyItems(): Invoice {
        return createValidInvoice().copy(
            totalAmount = 0,      // No items = no amount
            items = emptyList()   // ❌ Empty items - violates rule
        )
    }

    /**
     * Create an invoice with ZERO AMOUNT
     *
     * Use when testing: "invoice total must be positive"
     */
    fun createInvoiceWithZeroAmount(): Invoice {
        return createValidInvoice().copy(
            totalAmount = 0  // ❌ Zero - violates rule
        )
    }

    /**
     * Create an invoice with INVALID DUE DATE
     *
     * Use when testing: "due date must be >= invoice date"
     */
    fun createInvoiceWithInvalidDueDate(): Invoice {
        val now = getNow()
        return createValidInvoice().copy(
            date = now,
            dueDate = now - 86400000  // ❌ Yesterday - before invoice date!
        )
    }

    /**
     * Create an invoice with BLANK CUSTOMER NAME
     *
     * Use when testing: "customer name is required"
     */
    fun createInvoiceWithBlankCustomerName(): Invoice {
        return createValidInvoice().copy(
            customerName = ""  // ❌ Blank - violates rule
        )
    }

    /**
     * Create an invoice with INVALID CURRENCY CODE
     *
     * Use when testing: "currency code must be 3 letters"
     */
    fun createInvoiceWithInvalidCurrencyCode(): Invoice {
        return createValidInvoice().copy(
            currencyCode = "US"  // ❌ Only 2 letters!
        )
    }

    // ===============================
    // CUSTOMER FACTORIES
    // ===============================

    /**
     * Create a VALID customer with all fields
     *
     * FIELDS:
     *   name: "John Doe" (2-100 chars, not blank)
     *   email: "john@example.com" (valid format if provided)
     *   phone: "+61298765432" (5-20 chars if provided)
     *   businessName: null (optional, <= 100 chars)
     */
    fun createValidCustomer(): Customer {
        return Customer(
            name = "John Doe",
            email = "john@example.com",
            phone = "+61298765432"
        )
    }

    /**
     * Create a customer with BLANK NAME
     *
     * Use when testing: "customer name is required"
     */
    fun createCustomerWithBlankName(): Customer {
        return createValidCustomer().copy(
            name = ""  // ❌ Blank - violates rule
        )
    }

    /**
     * Create a customer with NAME TOO SHORT
     *
     * Use when testing: "name must be at least 2 characters"
     */
    fun createCustomerWithNameTooShort(): Customer {
        return createValidCustomer().copy(
            name = "A"  // ❌ Only 1 character
        )
    }

    /**
     * Create a customer with NAME TOO LONG
     *
     * Use when testing: "name must be 100 characters or less"
     */
    fun createCustomerWithNameTooLong(): Customer {
        return createValidCustomer().copy(
            name = "A".repeat(101)  // ❌ 101 characters
        )
    }

    /**
     * Create a customer with INVALID EMAIL
     *
     * Use when testing: "email format must be valid"
     */
    fun createCustomerWithInvalidEmail(): Customer {
        return createValidCustomer().copy(
            email = "not-an-email"  // ❌ No @ or .
        )
    }

    /**
     * Create a customer with INVALID PHONE
     *
     * Use when testing: "phone must be 5-20 characters"
     */
    fun createCustomerWithInvalidPhone(): Customer {
        return createValidCustomer().copy(
            phone = "123"  // ❌ Too short
        )
    }

    /**
     * Create a customer with BLANK OPTIONAL EMAIL
     *
     * Use when testing: "blank optional fields are OK"
     */
    fun createCustomerWithBlankEmail(): Customer {
        return createValidCustomer().copy(
            email = ""  // ✅ Blank is OK for optional field
        )
    }

    // ===============================
    // LINE ITEM FACTORIES
    // ===============================

    /**
     * Create a VALID line item
     *
     * FIELDS:
     *   description: "Test Item" (1-500 chars, not blank)
     *   quantity: 1.0 (positive, can be fractional like 1.5)
     *   unitPrice: 10000 (positive, in cents = $100)
     */
    fun createValidLineItem(): LineItem {
        return LineItem(
            description = "Test Item",
            quantity = 1.0,
            unitPrice = 10000  // $100 in cents
        )
    }

    /**
     * Create a line item with BLANK DESCRIPTION
     *
     * Use when testing: "item description is required"
     */
    fun createLineItemWithBlankDescription(): LineItem {
        return createValidLineItem().copy(
            description = ""  // ❌ Blank
        )
    }

    /**
     * Create a line item with ZERO QUANTITY
     *
     * Use when testing: "quantity must be positive"
     */
    fun createLineItemWithZeroQuantity(): LineItem {
        return createValidLineItem().copy(
            quantity = 0.0  // ❌ Zero
        )
    }

    /**
     * Create a line item with NEGATIVE PRICE
     *
     * Use when testing: "price must be positive"
     */
    fun createLineItemWithNegativePrice(): LineItem {
        return createValidLineItem().copy(
            unitPrice = -5000  // ❌ Negative
        )
    }

    /**
     * Create a line item with EXCESSIVE TOTAL
     *
     * Use when testing: "item total cannot exceed $1,000,000"
     */
    fun createLineItemWithExcessiveTotal(): LineItem {
        return createValidLineItem().copy(
            quantity = 1000000.0,      // Million quantity
            unitPrice = 1000000        // $10,000 per unit = $10 billion total!
        )
    }

    /**
     * Create a line item with FRACTIONAL QUANTITY
     *
     * Use when testing: "fractional quantities are OK (e.g., 1.5 hours)"
     */
    fun createLineItemWithFractionalQuantity(): LineItem {
        return createValidLineItem().copy(
            quantity = 2.5  // 2.5 hours of work
        )
    }

    // ===============================
    // BATCH FACTORIES
    // ===============================

    /**
     * Create multiple VALID customers
     *
     * Use when testing: batch validation
     */
    fun createValidCustomers(count: Int): List<Customer> {
        return (1..count).map { i ->
            Customer(
                name = "Customer $i",
                email = "customer$i@example.com",
                phone = "+6129876543$i"
            )
        }
    }

    /**
     * Create multiple VALID invoices
     *
     * Use when testing: batch validation
     */
    fun createValidInvoices(count: Int): List<Invoice> {
        val now = getNow()
        return (1..count).map { i ->
            createValidInvoice().copy(
                customerId = i.toLong(),
                customerName = "Customer $i",
                totalAmount = (i * 10000).toLong()  // Varying amounts
            )
        }
    }

    // ===============================
    // UTILITY METHODS
    // ===============================

    /**
     * Create a customer with VALID name but missing email
     *
     * Use when testing: "optional fields can be null"
     */
    fun createCustomerWithoutEmail(): Customer {
        return createValidCustomer().copy(email = null)
    }

    /**
     * Create a customer with VALID name but missing phone
     *
     * Use when testing: "optional fields can be null"
     */
    fun createCustomerWithoutPhone(): Customer {
        return createValidCustomer().copy(phone = null)
    }

    // ===============================
    // BUSINESS PROFILE FACTORIES
    // ===============================

    /**
     * Create a VALID business profile
     *
     * FIELDS:
     *   id: 1 (auto-generated usually)
     *   businessName: "Tech Solutions Pty Ltd" (1-100 chars)
     *   abn: "12345678901" (11 digits for Australian Business Number)
     *   email: "info@techsolutions.com.au" (valid format)
     *   phone: "+61298765432" (valid international format)
     *   website: "www.techsolutions.com.au" (optional)
     *   address: "123 Business St, Sydney NSW 2000" (optional)
     *   taxRate: 10L (10% GST in cents, 1000 = 10%)
     */
    fun createValidBusinessProfile(): com.emul8r.bizap.domain.model.BusinessProfile {
        return com.emul8r.bizap.domain.model.BusinessProfile(
            id = 1,
            businessName = "Tech Solutions Pty Ltd",
            email = "info@techsolutions.com.au",
            phone = "+61298765432",
            website = "www.techsolutions.com.au",
            address = "123 Business St, Sydney NSW 2000",
            abn = "12345678901",
            taxRate = 1000  // 10% in basis points
        )
    }

    /**
     * Create a business profile with BLANK NAME
     *
     * Use when testing: "business name is required"
     */
    fun createBusinessProfileWithBlankName(): com.emul8r.bizap.domain.model.BusinessProfile {
        return createValidBusinessProfile().copy(businessName = "")
    }

    /**
     * Create a business profile with INVALID EMAIL
     *
     * Use when testing: "email must be valid if provided"
     */
    fun createBusinessProfileWithInvalidEmail(): com.emul8r.bizap.domain.model.BusinessProfile {
        return createValidBusinessProfile().copy(email = "not-an-email")
    }
}
