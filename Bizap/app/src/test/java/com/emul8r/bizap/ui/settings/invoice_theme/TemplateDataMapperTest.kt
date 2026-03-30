package com.emul8r.bizap.ui.settings.invoice_theme

import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for TemplateDataMapper.
 *
 * Tests:
 * - Currency formatting
 * - Date formatting
 * - Percentage formatting
 * - Quantity formatting
 * - Safe string handling
 * - Invoice data mapping
 */
class TemplateDataMapperTest {

    private lateinit var mapper: TemplateDataMapper

    @Before
    fun setup() {
        mapper = TemplateDataMapper()
    }

    // ==================== Currency Formatting Tests ====================

    @Test
    fun testFormatCurrency_SimpleAmount() {
        val result = mapper.formatCurrency(1234.56)
        assertEquals("1,234.56", result)
    }

    @Test
    fun testFormatCurrency_WholeNumber() {
        val result = mapper.formatCurrency(1000.0)
        assertEquals("1,000.00", result)
    }

    @Test
    fun testFormatCurrency_SmallAmount() {
        val result = mapper.formatCurrency(5.99)
        assertEquals("5.99", result)
    }

    @Test
    fun testFormatCurrency_Zero() {
        val result = mapper.formatCurrency(0.0)
        assertEquals("0.00", result)
    }

    @Test
    fun testFormatCurrency_NegativeAmount() {
        val result = mapper.formatCurrency(-100.50)
        assertEquals("-100.50", result)
    }

    // ==================== Percentage Formatting Tests ====================

    @Test
    fun testFormatPercentage_StandardRate() {
        val result = mapper.formatPercentage(10.0)
        assertEquals("10.00", result)
    }

    @Test
    fun testFormatPercentage_DecimalRate() {
        val result = mapper.formatPercentage(15.5)
        assertEquals("15.50", result)
    }

    @Test
    fun testFormatPercentage_ZeroPercent() {
        val result = mapper.formatPercentage(0.0)
        assertEquals("0.00", result)
    }

    @Test
    fun testFormatPercentage_HighPercent() {
        val result = mapper.formatPercentage(99.99)
        assertEquals("99.99", result)
    }

    // ==================== Quantity Formatting Tests ====================

    @Test
    fun testFormatQuantity_WholeNumber() {
        val result = mapper.formatQuantity(5.0)
        assertEquals("5", result)
    }

    @Test
    fun testFormatQuantity_DecimalValue() {
        val result = mapper.formatQuantity(5.5)
        assertEquals("5.50", result)
    }

    @Test
    fun testFormatQuantity_OneUnit() {
        val result = mapper.formatQuantity(1.0)
        assertEquals("1", result)
    }

    @Test
    fun testFormatQuantity_SmallDecimal() {
        val result = mapper.formatQuantity(0.25)
        assertEquals("0.25", result)
    }

    // ==================== Date Formatting Tests ====================

    @Test
    fun testFormatDateShort() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 30)
        }
        val date = calendar.time
        val result = mapper.formatDateShort(date)
        assertTrue(result.contains("Mar") || result.contains("March"))
        assertTrue(result.contains("30"))
        assertTrue(result.contains("2026"))
    }

    @Test
    fun testFormatDateLong() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 30)
        }
        val date = calendar.time
        val result = mapper.formatDateLong(date)
        assertTrue(result.contains("March"))
        assertTrue(result.contains("30"))
        assertTrue(result.contains("2026"))
    }

    // ==================== Safe String Handling Tests ====================

    @Test
    fun testIsEmpty_WithNull() {
        assertTrue(mapper.isEmpty(null))
    }

    @Test
    fun testIsEmpty_WithEmptyString() {
        assertTrue(mapper.isEmpty(""))
    }

    @Test
    fun testIsEmpty_WithWhitespace() {
        assertTrue(mapper.isEmpty("   "))
    }

    @Test
    fun testIsEmpty_WithValidString() {
        assertFalse(mapper.isEmpty("test"))
    }

    @Test
    fun testSafeString_WithNull() {
        val result = mapper.safeString(null)
        assertEquals("", result)
    }

    @Test
    fun testSafeString_WithNullAndDefault() {
        val result = mapper.safeString(null, "default")
        assertEquals("default", result)
    }

    @Test
    fun testSafeString_WithEmptyString() {
        val result = mapper.safeString("", "default")
        assertEquals("default", result)
    }

    @Test
    fun testSafeString_WithValidString() {
        val result = mapper.safeString("test")
        assertEquals("test", result)
    }

    @Test
    fun testSafeString_WithWhitespace() {
        val result = mapper.safeString("   ", "default")
        assertEquals("default", result)
    }

    // ==================== Item Formatting Tests ====================

    @Test
    fun testFormatItem_Complete() {
        val item = mapper.formatItem(
            "Sample Item",
            5.0,
            100.0,
            500.0
        )

        assertEquals("Sample Item", item["description"])
        assertEquals(5.0, item["quantity"])
        assertEquals(100.0, item["unitPrice"])
        assertEquals(500.0, item["amount"])
    }

    @Test
    fun testFormatItem_WithDecimals() {
        val item = mapper.formatItem(
            "Fractional Item",
            2.5,
            49.99,
            124.975
        )

        assertEquals("Fractional Item", item["description"])
        assertEquals(2.5, item["quantity"])
        assertEquals(49.99, item["unitPrice"])
        assertEquals(124.975, item["amount"])
    }

    // ==================== Template Mapping Tests ====================

    @Test
    fun testMapToTemplate_Complete() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 30)
        }
        val invoiceDate = calendar.time
        val dueDate = calendar.apply { add(Calendar.DAY_OF_MONTH, 30) }.time

        val items = listOf(
            mapper.formatItem("Item 1", 2.0, 50.0, 100.0),
            mapper.formatItem("Item 2", 1.0, 75.0, 75.0)
        )

        val result = mapper.mapToTemplate(
            invoiceNumber = "INV-001",
            invoiceDate = invoiceDate,
            dueDate = dueDate,
            customerName = "John Doe",
            customerEmail = "john@example.com",
            customerPhone = "+1 555-1234",
            customerAddress = "123 Main St, City, ST 12345",
            companyName = "Test Company",
            businessEmail = "contact@test.com",
            businessPhone = "+1 555-5678",
            businessAddress = "456 Business Ave, City, ST 54321",
            items = items,
            subtotal = 175.0,
            taxAmount = 17.50,
            totalAmount = 192.50,
            taxRate = 10.0,
            taxName = "GST",
            paymentTermsDays = 30,
            bankName = "Test Bank",
            accountNumber = "1234567890",
            notes = "Thank you for your business",
            status = "Unpaid",
            websiteUrl = "https://test.com"
        )

        // Verify all fields are present
        assertTrue(result.containsKey("invoiceNumber"))
        assertTrue(result.containsKey("invoiceDate"))
        assertTrue(result.containsKey("dueDate"))
        assertTrue(result.containsKey("customerName"))
        assertTrue(result.containsKey("items"))
        assertTrue(result.containsKey("subtotal"))
        assertTrue(result.containsKey("taxAmount"))
        assertTrue(result.containsKey("totalAmount"))
        assertTrue(result.containsKey("paymentTermsDays"))
        assertTrue(result.containsKey("showPaymentDetails"))

        // Verify values
        assertEquals("INV-001", result["invoiceNumber"])
        assertEquals("John Doe", result["customerName"])
        assertEquals(175.0, result["subtotal"])
        assertEquals(17.50, result["taxAmount"])
        assertEquals(192.50, result["totalAmount"])
        assertEquals(10.0, result["taxRate"])
        assertEquals("GST", result["taxName"])
        assertEquals(30, result["paymentTermsDays"])
        assertEquals(true, result["showPaymentDetails"])
    }

    @Test
    fun testMapToTemplate_WithoutOptionalFields() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 30)
        }
        val invoiceDate = calendar.time
        val dueDate = calendar.apply { add(Calendar.DAY_OF_MONTH, 30) }.time

        val result = mapper.mapToTemplate(
            invoiceNumber = "INV-002",
            invoiceDate = invoiceDate,
            dueDate = dueDate,
            customerName = "Jane Smith",
            customerEmail = "jane@example.com",
            customerPhone = "+1 555-9876",
            customerAddress = "789 Oak St, Town, ST 98765",
            companyName = "My Company",
            businessEmail = "info@mycompany.com",
            businessPhone = "+1 555-4321",
            businessAddress = "321 Commerce Blvd, Town, ST 65432",
            items = emptyList(),
            subtotal = 0.0,
            taxAmount = 0.0,
            totalAmount = 0.0,
            taxRate = 10.0,
            taxName = "VAT",
            paymentTermsDays = 45
        )

        // Verify optional fields are null or not shown
        assertEquals(null, result["bankName"])
        assertEquals(null, result["accountNumber"])
        assertEquals(null, result["notes"])
        assertEquals(null, result["websiteUrl"])
        assertEquals(false, result["showPaymentDetails"])
    }
}

