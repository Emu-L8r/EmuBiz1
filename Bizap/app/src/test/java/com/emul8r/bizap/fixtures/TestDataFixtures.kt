package com.emul8r.bizap.fixtures

import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import java.util.*

/**
 * Comprehensive test data fixtures for Phase 6 Step 3 testing
 * Provides realistic sample data for testing invoice settings system
 */
object TestDataFixtures {

    // ============================================================================
    // SAMPLE COMPANY PROFILES
    // ============================================================================

    /**
     * Sample company 1: Technology consulting firm
     */
    val sampleCompany1 = InvoiceSettings(
        userId = "test_user_1",
        taxRate = 0.10,
        taxName = "GST",
        paymentTermsDays = 30,
        defaultPaymentNotes = "Payment due within 30 days of invoice date",
        footerMessage = "Thank you for your business!",
        invoiceNumberPrefix = "ACM-",
        selectedTheme = InvoiceTheme.CANVAS,
        primaryColor = "#0066CC",
        secondaryColor = "#E6F2FF"
    )

    /**
     * Sample company 2: Creative agency
     */
    val sampleCompany2 = InvoiceSettings(
        userId = "test_user_2",
        taxRate = 0.15,
        taxName = "VAT",
        paymentTermsDays = 45,
        defaultPaymentNotes = "Payment terms: 45 days net",
        footerMessage = "Creating amazing brands since 2015",
        invoiceNumberPrefix = "CS-",
        selectedTheme = InvoiceTheme.HTML_PDF,
        primaryColor = "#FF6600",
        secondaryColor = "#FFE6CC"
    )

    /**
     * Sample company 3: Services business with net-60 terms
     */
    val sampleCompany3 = InvoiceSettings(
        userId = "test_user_3",
        taxRate = 0.08,
        taxName = "Sales Tax",
        paymentTermsDays = 60,
        defaultPaymentNotes = "Net 60 terms. Early payment discount available.",
        footerMessage = "Excellence in Enterprise Solutions",
        invoiceNumberPrefix = "ESI-",
        selectedTheme = InvoiceTheme.CANVAS,
        primaryColor = "#006633",
        secondaryColor = "#E6F5E0"
    )

    // ============================================================================
    // TEST CUSTOMERS
    // ============================================================================

    data class TestCustomer(
        val id: Long,
        val name: String,
        val email: String,
        val phone: String,
        val address: String
    )

    val testCustomer1 = TestCustomer(
        id = 1,
        name = "John Smith Consulting",
        email = "john@smithconsulting.com",
        phone = "+1-555-1000",
        address = "789 Main Street, Boston, MA 02101"
    )

    val testCustomer2 = TestCustomer(
        id = 2,
        name = "Global Marketing Ltd",
        email = "accounting@globalmarketing.co.uk",
        phone = "+44-20-7946-0958",
        address = "123 Oxford Street, London, UK"
    )

    val testCustomer3 = TestCustomer(
        id = 3,
        name = "Tech Startup XYZ",
        email = "payments@techstartup.io",
        phone = "+1-408-555-0123",
        address = "456 Silicon Valley Road, Palo Alto, CA 94301"
    )

    val testCustomer4 = TestCustomer(
        id = 4,
        name = "Regional Healthcare Group",
        email = "billing@healthcare-group.org",
        phone = "+1-214-555-0100",
        address = "567 Medical Plaza, Dallas, TX 75201"
    )

    val testCustomer5 = TestCustomer(
        id = 5,
        name = "Educational Institute",
        email = "finance@educ-institute.edu",
        phone = "+1-206-555-0100",
        address = "890 University Road, Seattle, WA 98101"
    )

    // ============================================================================
    // TEST INVOICES
    // ============================================================================

    data class TestInvoiceItem(
        val description: String,
        val quantity: Double,
        val unitPrice: Double
    )

    data class TestInvoice(
        val id: Long,
        val customerId: Long,
        val invoiceNumber: String,
        val invoiceDate: Long,
        val dueDate: Long,
        val items: List<TestInvoiceItem>,
        val notes: String
    )

    fun createSimpleInvoice(): TestInvoice {
        val now = System.currentTimeMillis()
        val thirtyDaysFromNow = now + (30 * 24 * 60 * 60 * 1000)

        return TestInvoice(
            id = 1,
            customerId = 1,
            invoiceNumber = "INV-001",
            invoiceDate = now,
            dueDate = thirtyDaysFromNow,
            items = listOf(
                TestInvoiceItem("Web Development Services", 40.0, 150.0),
                TestInvoiceItem("UI/UX Design", 20.0, 125.0),
                TestInvoiceItem("Project Management", 10.0, 100.0)
            ),
            notes = "Thank you for your business!"
        )
    }

    fun createComplexInvoice(): TestInvoice {
        val now = System.currentTimeMillis()
        val sixtyDaysFromNow = now + (60 * 24 * 60 * 60 * 1000)

        return TestInvoice(
            id = 2,
            customerId = 2,
            invoiceNumber = "INV-002",
            invoiceDate = now,
            dueDate = sixtyDaysFromNow,
            items = listOf(
                TestInvoiceItem("Branding Strategy", 30.0, 200.0),
                TestInvoiceItem("Logo Design", 25.0, 150.0),
                TestInvoiceItem("Brand Guidelines", 20.0, 180.0),
                TestInvoiceItem("Marketing Materials", 15.0, 100.0),
                TestInvoiceItem("Revisions & Adjustments", 10.0, 75.0)
            ),
            notes = "Payment due within 60 days. Early payment discounts available."
        )
    }

    fun createLargeInvoice(): TestInvoice {
        val now = System.currentTimeMillis()
        val ninetyDaysFromNow = now + (90 * 24 * 60 * 60 * 1000)

        return TestInvoice(
            id = 3,
            customerId = 3,
            invoiceNumber = "INV-003",
            invoiceDate = now,
            dueDate = ninetyDaysFromNow,
            items = listOf(
                TestInvoiceItem("Enterprise Software License (Annual)", 1.0, 50000.0),
                TestInvoiceItem("Implementation & Setup", 80.0, 250.0),
                TestInvoiceItem("Staff Training (5 days)", 5.0, 3000.0),
                TestInvoiceItem("Data Migration Services", 60.0, 200.0),
                TestInvoiceItem("Customization & Integration", 120.0, 175.0),
                TestInvoiceItem("24/7 Support Package (12 months)", 1.0, 12000.0),
                TestInvoiceItem("Documentation & Knowledge Transfer", 40.0, 150.0)
            ),
            notes = "Major enterprise implementation. Net 90 terms. Contact for quarterly reviews."
        )
    }

    fun createEdgeCaseInvoice(): TestInvoice {
        val now = System.currentTimeMillis()
        val sevenDaysFromNow = now + (7 * 24 * 60 * 60 * 1000)

        return TestInvoice(
            id = 4,
            customerId = 4,
            invoiceNumber = "INV-004",
            invoiceDate = now,
            dueDate = sevenDaysFromNow,
            items = listOf(
                TestInvoiceItem("Single item invoice with very long description that tests text wrapping and line breaks in the invoice output rendering engine", 1.0, 999.99),
                TestInvoiceItem("Item with unicode: Zürich consulting €500", 2.0, 500.0),
                TestInvoiceItem("Item with special chars: & < > \" '", 1.0, 100.0),
                TestInvoiceItem("Very small fractional amount", 0.001, 50000.0)
            ),
            notes = "Edge case invoice testing:\n• Multiple lines in notes\n• Special characters\n• Unicode support\n• Fractional quantities"
        )
    }

    // ============================================================================
    // HELPER COLLECTIONS
    // ============================================================================

    val allSampleCompanies = listOf(sampleCompany1, sampleCompany2, sampleCompany3)
    val allTestCustomers = listOf(testCustomer1, testCustomer2, testCustomer3, testCustomer4, testCustomer5)

    fun getAllTestInvoices() = listOf(
        createSimpleInvoice(),
        createComplexInvoice(),
        createLargeInvoice(),
        createEdgeCaseInvoice()
    )

    // ============================================================================
    // UTILITY FUNCTIONS
    // ============================================================================

    /**
     * Get a sample company by index
     */
    fun getSampleCompany(index: Int): InvoiceSettings {
        return allSampleCompanies.getOrElse(index) { sampleCompany1 }
    }

    /**
     * Get a test customer by index
     */
    fun getTestCustomer(index: Int): TestCustomer {
        return allTestCustomers.getOrElse(index) { testCustomer1 }
    }

    /**
     * Get a test invoice by index
     */
    fun getTestInvoice(index: Int): TestInvoice {
        return getAllTestInvoices().getOrElse(index) { createSimpleInvoice() }
    }

    /**
     * Create multiple invoices for load testing
     */
    fun createMultipleInvoices(count: Int): List<TestInvoice> {
        return (1..count).map { index ->
            val now = System.currentTimeMillis()
            val daysFromNow = 30 * 24 * 60 * 60 * 1000L

            TestInvoice(
                id = index.toLong(),
                customerId = (index % allTestCustomers.size).toLong() + 1,
                invoiceNumber = "INV-${String.format("%04d", index)}",
                invoiceDate = now - (index * 24 * 60 * 60 * 1000L),
                dueDate = now + daysFromNow,
                items = listOf(
                    TestInvoiceItem("Service Item $index", 40.0, 150.0),
                    TestInvoiceItem("Product Item $index", 20.0, 100.0)
                ),
                notes = "Invoice #$index for load testing"
            )
        }
    }
}





