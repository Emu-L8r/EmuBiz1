package com.emul8r.bizap.tax

import com.emul8r.bizap.domain.model.BusinessProfile
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.math.roundToLong

/**
 * Unit tests for tax registration toggle and calculations using Long (cents)
 */
class TaxRegistrationTest {

    @Test
    fun testBusinessNotTaxRegistered_DefaultValue() {
        val business = BusinessProfile(
            id = 1,
            businessName = "Startup Co",
            abn = "12345678901",
            email = "test@startup.com",
            phone = "555-1234",
            address = "123 St"
        )

        // Default should be false (not registered)
        assertFalse(business.isTaxRegistered)
        assertEquals(0.10f, business.defaultTaxRate)
    }

    @Test
    fun testInvoiceCalculation_NoTax() {
        val subtotalCents = 300000L // $3000.00
        val isTaxRegistered = false
        val taxRate = 0.10f

        val taxAmount = if (isTaxRegistered) (subtotalCents * taxRate).toLong() else 0L
        val total = subtotalCents + taxAmount

        assertEquals(300000L, total)
    }

    @Test
    fun testInvoiceCalculation_WithTax10Percent() {
        val subtotalCents = 300000L
        val isTaxRegistered = true
        val taxRate = 0.10f

        val taxAmount = (subtotalCents * taxRate).toLong()
        val total = subtotalCents + taxAmount

        assertEquals(330000L, total)
    }

    @Test
    fun testInvoiceCalculation_WithTax15Percent() {
        val subtotalCents = 300000L
        val isTaxRegistered = true
        val taxRate = 0.15f

        val taxAmount = (subtotalCents * taxRate).toLong()
        val total = subtotalCents + taxAmount

        assertEquals(345000L, total)
    }

    @Test
    fun testInvoiceCalculation_WithTax20Percent() {
        val subtotalCents = 300000L
        val isTaxRegistered = true
        val taxRate = 0.20f

        val taxAmount = (subtotalCents * taxRate).toLong()
        val total = subtotalCents + taxAmount

        assertEquals(360000L, total)
    }

    @Test
    fun testTaxAmount_WhenRegistered() {
        val subtotalCents = 300000L
        val isTaxRegistered = true
        val taxRate = 0.10f

        val taxAmount = if (isTaxRegistered) (subtotalCents * taxRate).toLong() else 0L

        assertEquals(30000L, taxAmount)
    }

    @Test
    fun testCustomTaxRate_5Percent() {
        val business = BusinessProfile(
            id = 1,
            businessName = "Low Tax Co",
            abn = "11111111111",
            email = "low@tax.com",
            phone = "555-9999",
            address = "789 Rd",
            isTaxRegistered = true,
            defaultTaxRate = 0.05f
        )

        assertEquals(0.05f, business.defaultTaxRate)
    }

    @Test
    fun testZeroTaxRate() {
        val business = BusinessProfile(
            id = 1, businessName = "Zero Tax", abn = "0", email = "0@test.com",
            phone = "0", address = "0", isTaxRegistered = true, defaultTaxRate = 0.0f
        )

        val subtotalCents = 100000L
        val taxAmount = (subtotalCents * business.defaultTaxRate).toLong()
        val total = subtotalCents + taxAmount

        assertEquals(0L, taxAmount)
        assertEquals(100000L, total)
    }
}
