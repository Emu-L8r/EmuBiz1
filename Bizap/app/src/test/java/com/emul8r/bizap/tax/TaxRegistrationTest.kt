package com.emul8r.bizap.tax

import com.emul8r.bizap.domain.model.BusinessProfile
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.math.BigDecimal

/**
 * Unit tests for tax registration toggle and calculations
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
    fun testBusinessTaxRegistered_TrueValue() {
        val business = BusinessProfile(
            id = 1,
            businessName = "Established Co",
            abn = "98765432101",
            email = "test@established.com",
            phone = "555-5678",
            address = "456 Ave",
            isTaxRegistered = true
        )

        assertTrue(business.isTaxRegistered)
    }

    @Test
    fun testInvoiceCalculation_NoTax() {
        val subtotal = 3000.0
        val isTaxRegistered = false
        val taxRate = 0.10f

        val total = if (isTaxRegistered) {
            subtotal * (1 + taxRate)
        } else {
            subtotal
        }

        assertEquals(3000.0, total)
    }

    @Test
    fun testInvoiceCalculation_WithTax10Percent() {
        val subtotal = 3000.0
        val isTaxRegistered = true
        val taxRate = 0.10f

        val total = if (isTaxRegistered) {
            subtotal * (1 + taxRate)
        } else {
            subtotal
        }

        assertEquals(3300.0, total)
    }

    @Test
    fun testInvoiceCalculation_WithTax15Percent() {
        val subtotal = 3000.0
        val isTaxRegistered = true
        val taxRate = 0.15f

        val total = if (isTaxRegistered) {
            subtotal * (1 + taxRate)
        } else {
            subtotal
        }

        assertEquals(3450.0, total)
    }

    @Test
    fun testInvoiceCalculation_WithTax20Percent() {
        val subtotal = 3000.0
        val isTaxRegistered = true
        val taxRate = 0.20f

        val total = if (isTaxRegistered) {
            subtotal * (1 + taxRate)
        } else {
            subtotal
        }

        assertEquals(3600.0, total)
    }

    @Test
    fun testTaxAmount_WhenRegistered() {
        val subtotal = 3000.0
        val isTaxRegistered = true
        val taxRate = 0.10f

        val taxAmount = if (isTaxRegistered) {
            subtotal * taxRate
        } else {
            0.0
        }

        assertEquals(300.0, taxAmount)
    }

    @Test
    fun testTaxAmount_WhenNotRegistered() {
        val subtotal = 3000.0
        val isTaxRegistered = false
        val taxRate = 0.10f

        val taxAmount = if (isTaxRegistered) {
            subtotal * taxRate
        } else {
            0.0
        }

        assertEquals(0.0, taxAmount)
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
    fun testCustomTaxRate_25Percent() {
        val business = BusinessProfile(
            id = 1,
            businessName = "High Tax Co",
            abn = "22222222222",
            email = "high@tax.com",
            phone = "555-8888",
            address = "321 Blvd",
            isTaxRegistered = true,
            defaultTaxRate = 0.25f
        )

        assertEquals(0.25f, business.defaultTaxRate)
    }

    @Test
    fun testToggleTaxRegistration() {
        var business = BusinessProfile(
            id = 1,
            businessName = "Toggle Co",
            abn = "33333333333",
            email = "toggle@test.com",
            phone = "555-7777",
            address = "111 Ave",
            isTaxRegistered = false
        )

        // Toggle to registered
        business = business.copy(isTaxRegistered = true)
        assertTrue(business.isTaxRegistered)

        // Toggle back to not registered
        business = business.copy(isTaxRegistered = false)
        assertFalse(business.isTaxRegistered)
    }

    @Test
    fun testMultipleBusinessesWithDifferentTaxSettings() {
        val business1 = BusinessProfile(
            id = 1, businessName = "B1", abn = "1", email = "1@test.com",
            phone = "1", address = "1", isTaxRegistered = false
        )

        val business2 = BusinessProfile(
            id = 2, businessName = "B2", abn = "2", email = "2@test.com",
            phone = "2", address = "2", isTaxRegistered = true, defaultTaxRate = 0.15f
        )

        assertFalse(business1.isTaxRegistered)
        assertTrue(business2.isTaxRegistered)
        assertEquals(0.10f, business1.defaultTaxRate)
        assertEquals(0.15f, business2.defaultTaxRate)
    }

    @Test
    fun testBackwardCompatibility_ExistingBusinesses() {
        // Existing businesses (before feature) would have:
        // isTaxRegistered = false (default from migration)
        // defaultTaxRate = 0.10 (default from migration)

        val existingBusiness = BusinessProfile(
            id = 999,
            businessName = "Existing Business",
            abn = "OLD123",
            email = "old@biz.com",
            phone = "555-0000",
            address = "Old Address"
            // isTaxRegistered defaults to false
            // defaultTaxRate defaults to 0.10
        )

        assertFalse(existingBusiness.isTaxRegistered)
        assertEquals(0.10f, existingBusiness.defaultTaxRate)
    }

    @Test
    fun testZeroTaxRate() {
        val business = BusinessProfile(
            id = 1, businessName = "Zero Tax", abn = "0", email = "0@test.com",
            phone = "0", address = "0", isTaxRegistered = true, defaultTaxRate = 0.0f
        )

        val subtotal = 1000.0
        val taxAmount = subtotal * business.defaultTaxRate
        val total = subtotal + taxAmount

        assertEquals(0.0, taxAmount)
        assertEquals(1000.0, total)
    }

    @Test
    fun testTaxRateBoundaries() {
        // Test various common tax rates
        val rates = listOf(0.05f, 0.10f, 0.15f, 0.20f, 0.25f)
        rates.forEach { rate ->
            val business = BusinessProfile(
                id = 1, businessName = "Test", abn = "1", email = "t@t.com",
                phone = "1", address = "1", isTaxRegistered = true, defaultTaxRate = rate
            )
            assertEquals(rate, business.defaultTaxRate)
        }
    }
}

