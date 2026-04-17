package com.emul8r.bizap.utils

import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

class InvoiceNumberingUtilsTest {

    @Test
    fun `generateCustomerCode extracts last name`() {
        assertEquals("Smith", InvoiceNumberingUtils.generateCustomerCode("John Smith"))
        assertEquals("Associates", InvoiceNumberingUtils.generateCustomerCode("Smith & Associates"))
    }

    @Test
    fun `generateCustomerCode handles single name`() {
        assertEquals("John", InvoiceNumberingUtils.generateCustomerCode("John"))
    }

    @Test
    fun `generateCustomerCode handles multi-word company names`() {
        // "Corp" is a common suffix and is filtered out; last non-suffix word is returned
        val result = InvoiceNumberingUtils.generateCustomerCode("Acme Corp")
        assertEquals("Acme", result)
        val result2 = InvoiceNumberingUtils.generateCustomerCode("Acme Corp Inc.")
        assertEquals("Inc.", result2)
    }

    @Test
    fun `generateInvoiceNumber returns correct format`() {
        val april10_2026 = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 10, 0, 0, 0)
        }.timeInMillis

        val number = InvoiceNumberingUtils.generateInvoiceNumber(
            date = april10_2026,
            dailySequence = 1,
            customerName = "John Smith",
            version = 1
        )

        assertEquals("26-0410-01-Smith", number)
    }

    @Test
    fun `generateInvoiceNumber with version includes v suffix`() {
        val april10_2026 = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 10, 0, 0, 0)
        }.timeInMillis

        val number = InvoiceNumberingUtils.generateInvoiceNumber(
            date = april10_2026,
            dailySequence = 5,
            customerName = "Acme Corp",
            version = 2
        )

        // "Corp" is filtered as common suffix, customer code becomes "Acme"
        assertEquals("26-0410-05-Acme-v2", number)
    }

    @Test
    fun `extractVersion works correctly`() {
        assertEquals(1, InvoiceNumberingUtils.extractVersion("26-0410-01-Smith"))
        assertEquals(2, InvoiceNumberingUtils.extractVersion("26-0410-01-Smith-v2"))
        assertEquals(3, InvoiceNumberingUtils.extractVersion("26-0410-01-Smith-v3"))
    }

    @Test
    fun `toFileSafeFormat replaces dashes`() {
        assertEquals(
            "26_0410_01_Smith",
            InvoiceNumberingUtils.toFileSafeFormat("26-0410-01-Smith")
        )
    }

    @Test
    fun `getBaseNumber removes version suffix`() {
        assertEquals(
            "26-0410-01-Smith",
            InvoiceNumberingUtils.getBaseNumber("26-0410-01-Smith-v2")
        )
    }
}




