package com.emul8r.bizap.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [InvoiceSnapshot] — verifies Phase 3.2 Problem 2 fix:
 * bank / payment details are present on the snapshot and have correct defaults.
 */
class InvoiceSnapshotBankDetailsTest {

    private fun minimalSnapshot(
        bankAccountName: String = "",
        bankAccountNumber: String = "",
        bankBsb: String = "",
        bankName: String = ""
    ) = InvoiceSnapshot(
        invoiceId = 1L,
        invoiceNumber = "INV-001",
        customerName = "Test Customer",
        customerAddress = "1 Test St",
        customerEmail = null,
        date = 1_000_000L,
        dueDate = 2_000_000L,
        items = emptyList(),
        subtotal = 10_000L,
        taxRate = 0.10,
        taxAmount = 1_000L,
        totalAmount = 11_000L,
        businessName = "Acme Pty Ltd",
        businessAbn = "12 345 678 901",
        businessEmail = "hello@acme.com",
        businessPhone = "0400 000 000",
        businessAddress = "1 Acme St, Sydney NSW 2000",
        logoBase64 = null,
        bankAccountName = bankAccountName,
        bankAccountNumber = bankAccountNumber,
        bankBsb = bankBsb,
        bankName = bankName
    )

    @Test
    fun `bank details default to empty strings`() {
        val snapshot = minimalSnapshot()
        assertEquals("", snapshot.bankAccountName)
        assertEquals("", snapshot.bankAccountNumber)
        assertEquals("", snapshot.bankBsb)
        assertEquals("", snapshot.bankName)
    }

    @Test
    fun `bank details are stored correctly when provided`() {
        val snapshot = minimalSnapshot(
            bankAccountName = "Acme Pty Ltd",
            bankAccountNumber = "123456789",
            bankBsb = "062-000",
            bankName = "Commonwealth Bank"
        )

        assertEquals("Acme Pty Ltd", snapshot.bankAccountName)
        assertEquals("123456789", snapshot.bankAccountNumber)
        assertEquals("062-000", snapshot.bankBsb)
        assertEquals("Commonwealth Bank", snapshot.bankName)
    }

    @Test
    fun `snapshot with bank details has non-blank account number`() {
        val snapshot = minimalSnapshot(bankAccountNumber = "987654321", bankBsb = "032-001")
        assertTrue(snapshot.bankAccountNumber.isNotBlank(),
            "Account number should not be blank when set")
        assertTrue(snapshot.bankBsb.isNotBlank(),
            "BSB should not be blank when set")
    }

    @Test
    fun `snapshot without bank details has all blank bank fields`() {
        val snapshot = minimalSnapshot()
        val hasBankDetails = snapshot.bankAccountNumber.isNotBlank() || snapshot.bankBsb.isNotBlank()
        assertTrue(!hasBankDetails,
            "A snapshot with no bank info should not trigger bank section rendering")
    }

    @Test
    fun `snapshot preserves all standard fields alongside bank details`() {
        val snapshot = minimalSnapshot(
            bankAccountName = "Test Account",
            bankAccountNumber = "111222333",
            bankBsb = "012-345",
            bankName = "ANZ"
        )

        // Standard fields should be unaffected
        assertEquals("Acme Pty Ltd", snapshot.businessName)
        assertEquals("12 345 678 901", snapshot.businessAbn)
        assertEquals("hello@acme.com", snapshot.businessEmail)

        // Bank fields should be set
        assertEquals("Test Account", snapshot.bankAccountName)
        assertEquals("ANZ", snapshot.bankName)
    }
}
