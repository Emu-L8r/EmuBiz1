package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.domain.model.Result
import timber.log.Timber

/**
 * VALIDATION RULES - Domain-Level Input Validation
 *
 * This is the single source of truth for all validation logic.
 * All validation happens at the DOMAIN layer (not UI, not DB).
 *
 * WHY DOMAIN LAYER?
 * =================
 * 1. Independence: Validation logic doesn't depend on UI framework
 * 2. Consistency: All layers (UI, API, DB) use same rules
 * 3. Testability: Easy to test validation without Compose/Room
 * 4. Reusability: Can use in ViewModels, Use Cases, APIs, etc.
 * 5. Maintainability: Change rules in one place, everywhere updates
 *
 * ARCHITECTURE:
 * =============
 *  ┌─ Input (Invoice, Customer, LineItem)
 *  │
 *  └─→ ValidationRules.validate*()
 *      │
 *      ├─ Private validation helpers (isValidEmail, etc.)
 *      │
 *      └─→ Result<Unit> (Success or Failure with message)
 *
 * USAGE PATTERN:
 * ==============
 *   val result = ValidationRules.validateInvoice(invoice)
 *
 *   if (result.isFailure()) {
 *       Timber.w("Validation failed: ${result.getErrorOrNull()}")
 *       _errorState.emit(result.getErrorOrNull())
 *       return@launch
 *   }
 *
 *   // Safe to proceed - data is validated
 *   repository.save(invoice)
 */
object ValidationRules {

    // ===============================
    // INVOICE VALIDATION
    // ===============================

    /**
     * VALIDATE INVOICE - Check if invoice is ready to save/send
     *
     * RULES:
     * ======
     * 1. Must have at least one line item
     * 2. Total amount must be > 0
     * 3. Due date must be >= invoice date
     * 4. Customer name required (even if no customer ID)
     * 5. All line items must be valid
     * 6. Currency code must be 3 characters (ISO 4217)
     *
     * RATIONALE:
     * ==========
     * - Empty invoices confuse users and systems
     * - Zero/negative amounts indicate data corruption
     * - Due before invoice makes no sense
     * - Invoice without customer is incomplete
     * - Each item must meet its own standards
     * - Currency tracking is critical for reports
     */
    fun validateInvoice(invoice: Invoice): Result<Unit> {
        // Rule 1: At least one item
        if (invoice.items.isEmpty()) {
            val message = "Invoice must have at least one line item"
            Timber.w("❌ $message")
            return Result.Failure(message)
        }

        // Rule 2: Amount must be positive
        if (invoice.totalAmount <= 0) {
            val message = "Invoice total must be greater than zero"
            Timber.w("❌ $message (current: ${invoice.totalAmount} cents)")
            return Result.Failure(message)
        }

        // Rule 3: Due date >= invoice date
        if (invoice.dueDate < invoice.date) {
            val message = "Due date must be on or after invoice date"
            Timber.w("❌ $message")
            return Result.Failure(message)
        }

        // Rule 4: Customer name required
        if (invoice.customerName.isBlank()) {
            val message = "Customer name is required"
            Timber.w("❌ $message")
            return Result.Failure(message)
        }

        // Rule 5: All items must be valid
        for (item in invoice.items) {
            val itemValidation = validateLineItem(item)
            if (itemValidation.isFailure()) {
                return itemValidation
            }
        }

        // Rule 6: Currency code format
        if (invoice.currencyCode.length != 3 || !invoice.currencyCode.all { it.isLetter() }) {
            val message = "Currency code must be 3 letters (e.g., AUD, USD, EUR)"
            Timber.w("❌ $message (provided: ${invoice.currencyCode})")
            return Result.Failure(message)
        }

        Timber.d("✅ Invoice validation passed")
        return Result.Success(Unit)
    }

    // ===============================
    // CUSTOMER VALIDATION
    // ===============================

    /**
     * VALIDATE CUSTOMER - Check if customer data is valid
     *
     * RULES:
     * ======
     * 1. Name is required and not blank
     * 2. Name must be <= 100 characters
     * 3. Name must be >= 2 characters (avoid typos like "A")
     * 4. Email (if provided) must be valid format
     * 5. Phone (if provided) must be valid length
     * 6. Business name (if provided) must be <= 100 characters
     *
     * RATIONALE:
     * ==========
     * - Blank names cause confusion and merge issues
     * - 100 char limit prevents database/display issues
     * - Single char name likely user error
     * - Email validation catches obvious typos
     * - Phone length prevents invalid entries
     */
    fun validateCustomer(customer: Customer): Result<Unit> {
        // Rule 1 & 2: Name required and not blank
        if (customer.name.isBlank()) {
            val message = "Customer name is required"
            Timber.w("❌ $message")
            return Result.Failure(message)
        }

        // Rule 3: Name length bounds
        if (customer.name.length < 2) {
            val message = "Customer name must be at least 2 characters"
            Timber.w("❌ $message (provided: '${customer.name}')")
            return Result.Failure(message)
        }

        if (customer.name.length > 100) {
            val message = "Customer name must be 100 characters or less"
            Timber.w("❌ $message (provided: ${customer.name.length} chars)")
            return Result.Failure(message)
        }

        // Rule 4: Email validation (if provided)
        customer.email?.let { email ->
            if (email.isNotBlank() && !isValidEmail(email)) {
                val message = "Invalid email format: $email"
                Timber.w("❌ $message")
                return Result.Failure(message)
            }
        }

        // Rule 5: Phone validation (if provided)
        customer.phone?.let { phone ->
            if (phone.isNotBlank() && !isValidPhone(phone)) {
                val message = "Phone must be between 5 and 20 characters"
                Timber.w("❌ $message (provided: ${phone.length} chars)")
                return Result.Failure(message)
            }
        }

        // Rule 6: Business name length (if provided)
        customer.businessName?.let { business ->
            if (business.length > 100) {
                val message = "Business name must be 100 characters or less"
                Timber.w("❌ $message (provided: ${business.length} chars)")
                return Result.Failure(message)
            }
        }

        Timber.d("✅ Customer validation passed: ${customer.name}")
        return Result.Success(Unit)
    }

    // ===============================
    // LINE ITEM VALIDATION
    // ===============================

    /**
     * VALIDATE LINE ITEM - Check if item is valid
     *
     * RULES:
     * ======
     * 1. Description is required and not blank
     * 2. Description must be <= 500 characters (PDF display limits)
     * 3. Quantity must be > 0
     * 4. Unit price must be > 0 (in cents, so >= 1)
     * 5. Total (quantity * unitPrice) must be reasonable (< 1 billion cents)
     *
     * RATIONALE:
     * ==========
     * - Blank description confuses users viewing invoice
     * - Long descriptions cause text wrapping issues in PDF
     * - Zero or negative quantity = data error
     * - Zero or negative price = data error or user forgot to enter
     * - Excessive totals indicate bad data (e.g., 99999999 cents = $999,999)
     */
    fun validateLineItem(item: LineItem): Result<Unit> {
        // Rule 1 & 2: Description required and length limit
        if (item.description.isBlank()) {
            val message = "Item description is required"
            Timber.w("❌ $message")
            return Result.Failure(message)
        }

        if (item.description.length > 500) {
            val message = "Item description must be 500 characters or less"
            Timber.w("❌ $message (provided: ${item.description.length} chars)")
            return Result.Failure(message)
        }

        // Rule 3: Quantity must be positive
        if (item.quantity <= 0) {
            val message = "Item quantity must be greater than zero (received: ${item.quantity})"
            Timber.w("❌ $message")
            return Result.Failure(message)
        }

        // Rule 4: Price must be positive
        if (item.unitPrice <= 0) {
            val message = "Item unit price must be greater than zero (received: ${item.unitPrice} cents)"
            Timber.w("❌ $message")
            return Result.Failure(message)
        }

        // Rule 5: Total shouldn't be unreasonably large
        val itemTotal = item.unitPrice.toDouble() * item.quantity
        if (itemTotal > 100_000_000) {  // $1,000,000 in cents
            val message = "Item total is unreasonably large: $${"%.2f".format(itemTotal / 100)}"
            Timber.w("❌ $message")
            return Result.Failure(message)
        }

        Timber.d("✅ Line item validation passed: ${item.description}")
        return Result.Success(Unit)
    }

    // ===============================
    // PRIVATE VALIDATION HELPERS
    // ===============================

    /**
     * CHECK EMAIL FORMAT
     *
     * RULES:
     * ======
     * - Must contain @ symbol
     * - Must contain . (dot) after @
     * - @ must not be first or last character
     * - Must have text before @ and after .
     *
     * NOTE: This is basic validation (RFC 5322 is extremely complex).
     * For production, use a proper email validation library.
     * This catches obvious typos like:
     *   ✅ john@example.com
     *   ❌ john@example     (no dot)
     *   ❌ @example.com     (no name)
     *   ❌ john.example.com (no @)
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[^@]+@[^@]+\\.[^@]+$".toRegex()
        return email.matches(emailRegex)
    }

    /**
     * CHECK PHONE FORMAT
     *
     * RULES:
     * ======
     * - Must be 5-20 characters
     * - Allows digits, spaces, dashes, plus sign, parentheses
     * - Examples:
     *   ✅ +61 2 9876 5432
     *   ✅ (02) 9876-5432
     *   ✅ 0298765432
     *   ✅ +61298765432
     *   ❌ 12               (too short)
     *   ❌ abc              (no digits)
     */
    private fun isValidPhone(phone: String): Boolean {
        val length = phone.length
        if (length < 5 || length > 20) return false

        // Must contain at least one digit
        val hasDigit = phone.any { it.isDigit() }
        if (!hasDigit) return false

        // Only allow: digits, spaces, dashes, plus, parentheses
        val validChars = phone.all { it.isDigit() || it == ' ' || it == '-' || it == '+' || it == '(' || it == ')' }
        return validChars
    }

    // ===============================
    // COLLECTION VALIDATION
    // ===============================

    /**
     * VALIDATE ALL CUSTOMERS - Check multiple customers at once
     *
     * USE: When importing or validating batches
     */
    fun validateCustomers(customers: List<Customer>): Result<Unit> {
        for ((index, customer) in customers.withIndex()) {
            val result = validateCustomer(customer)
            if (result.isFailure()) {
                val message = "Customer #$index failed validation: ${result.getErrorOrNull()}"
                Timber.w("❌ $message")
                return Result.Failure(message)
            }
        }

        Timber.d("✅ All ${customers.size} customers validated")
        return Result.Success(Unit)
    }

    /**
     * VALIDATE ALL INVOICES - Check multiple invoices at once
     *
     * USE: When importing or validating batches
     */
    fun validateInvoices(invoices: List<Invoice>): Result<Unit> {
        for ((index, invoice) in invoices.withIndex()) {
            val result = validateInvoice(invoice)
            if (result.isFailure()) {
                val message = "Invoice #$index (${invoice.invoiceNumber}) failed validation: ${result.getErrorOrNull()}"
                Timber.w("❌ $message")
                return Result.Failure(message)
            }
        }

        Timber.d("✅ All ${invoices.size} invoices validated")
        return Result.Success(Unit)
    }
}

