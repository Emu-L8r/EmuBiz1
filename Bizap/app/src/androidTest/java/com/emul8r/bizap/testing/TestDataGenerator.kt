package com.emul8r.bizap.testing

import com.emul8r.bizap.domain.model.*
import timber.log.Timber
import java.util.UUID
import kotlin.random.Random

/**
 * Phase 2: Test Data Generator
 *
 * Generates realistic test data for:
 * - Database load testing (100k-500k invoices)
 * - PDF robustness testing (various item counts)
 * - Performance profiling
 *
 * Status: 🚀 WEEK 3-4 SUPPORT
 */
object TestDataGenerator {

    private val random = Random(12345) // Deterministic for reproducibility
    private val companyNames = listOf(
        "Acme Corp", "TechStart Inc", "Global Services Ltd", "Innovation Labs",
        "Prime Solutions", "Digital Enterprises", "CloudWorks Inc", "NextGen Systems"
    )
    private val descriptions = listOf(
        "Professional consulting services", "Software development", "Design services",
        "Marketing campaign", "Training and workshops", "Maintenance support",
        "Licensing fees", "Subscription services", "API access", "Custom development"
    )

    /**
     * Generate test invoices for database load testing
     *
     * Supports:
     * - 0 items: Edge case (empty invoice)
     * - 15 items: Typical invoice
     * - 50 items: Medium invoice
     * - 100 items: Large invoice
     * - 500k items: Stress test data
     */
    fun generateTestInvoices(
        count: Int,
        businessId: Long = 1L,
        itemsPerInvoice: Int = 15
    ): List<Invoice> {
        Timber.d("🔨 Generating $count test invoices ($itemsPerInvoice items each)...")

        val invoices = mutableListOf<Invoice>()
        val now = System.currentTimeMillis()
        val oneMonthAgo = now - (30L * 24 * 60 * 60 * 1000)

        repeat(count) { idx ->
            val invoiceId = idx.toLong() + 1000
            val dateOffset = random.nextLong(oneMonthAgo, now)

            // Generate line items
            val items = mutableListOf<LineItem>()
            repeat(itemsPerInvoice) { itemIdx ->
                items.add(
                    LineItem(
                        id = (invoiceId * 1000) + itemIdx,
                        invoiceId = invoiceId,
                        description = descriptions[random.nextInt(descriptions.size)],
                        quantity = random.nextInt(1, 10).toDouble(),
                        unitPrice = random.nextDouble(50.0, 5000.0),
                        taxRate = 0.10
                    )
                )
            }

            // Create invoice
            val invoice = Invoice(
                id = invoiceId,
                businessId = businessId,
                invoiceNumber = "INV-${String.format("%06d", idx)}",
                customerId = (random.nextLong(1, 100)),
                date = dateOffset,
                dueDate = dateOffset + (30L * 24 * 60 * 60 * 1000), // 30 days later
                status = InvoiceStatus.entries[random.nextInt(InvoiceStatus.entries.size)],
                items = items,
                amount = items.sumOf { it.quantity * it.unitPrice },
                taxAmount = items.sumOf { (it.quantity * it.unitPrice) * it.taxRate },
                notes = "Test invoice #$idx"
            )

            invoices.add(invoice)

            // Progress logging every 10k invoices
            if ((idx + 1) % 10_000 == 0) {
                Timber.d("  ✓ Generated ${idx + 1}/$count invoices")
            }
        }

        Timber.d("✅ Generated $count invoices total")
        return invoices
    }

    /**
     * Generate a single test invoice with specified item count
     *
     * Used for PDF robustness testing
     */
    fun generateTestInvoice(
        itemCount: Int,
        invoiceId: Long = random.nextLong(1000, 999999),
        businessId: Long = 1L,
        withLongDescription: Boolean = false
    ): Invoice {
        Timber.d("🔨 Generating test invoice with $itemCount items...")

        val items = mutableListOf<LineItem>()
        repeat(itemCount) { itemIdx ->
            val description = if (withLongDescription) {
                descriptions[random.nextInt(descriptions.size)] + " - " +
                "x".repeat(300) // 500+ chars total
            } else {
                descriptions[random.nextInt(descriptions.size)]
            }

            items.add(
                LineItem(
                    id = (invoiceId * 1000) + itemIdx,
                    invoiceId = invoiceId,
                    description = description,
                    quantity = random.nextInt(1, 10).toDouble(),
                    unitPrice = random.nextDouble(50.0, 5000.0),
                    taxRate = 0.10
                )
            )
        }

        val invoice = Invoice(
            id = invoiceId,
            businessId = businessId,
            invoiceNumber = "TEST-INV-${UUID.randomUUID().toString().take(8)}",
            customerId = random.nextLong(1, 100),
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
            status = InvoiceStatus.DRAFT,
            items = items,
            amount = items.sumOf { it.quantity * it.unitPrice },
            taxAmount = items.sumOf { (it.quantity * it.unitPrice) * it.taxRate },
            notes = "Test invoice with $itemCount items"
        )

        Timber.d("✅ Generated invoice with $itemCount items")
        return invoice
    }

    /**
     * Generate test PDF bytes
     *
     * Returns mock PDF data for testing (real PDF generation tested separately)
     */
    fun generateTestPdfBytes(
        layout: PageLayout,
        colorScheme: ColorScheme,
        itemCount: Int
    ): ByteArray {
        Timber.d("📄 Generating test PDF ($layout + $colorScheme, $itemCount items)...")

        // Create mock PDF header
        val pdfContent = buildString {
            append("%PDF-1.4\n")
            append("1 0 obj\n")
            append("<< /Type /Catalog >>\n")
            append("endobj\n")
            append("Test PDF: $layout x $colorScheme x $itemCount items\n")
        }

        Timber.d("✅ Generated ${pdfContent.length} byte PDF")
        return pdfContent.toByteArray()
    }

    /**
     * Generate test settings for PDF variant testing
     */
    fun generateTestSettings(
        layout: PageLayout = PageLayout.MODERN,
        colorScheme: ColorScheme = ColorScheme.PROFESSIONAL
    ): InvoiceSettings {
        Timber.d("⚙️  Generating test settings ($layout + $colorScheme)...")

        return InvoiceSettings(
            userId = "test-user",
            selectedPageLayout = layout,
            selectedColorScheme = colorScheme,
            selectedSpacingProfile = SpacingProfile.NORMAL,
            totalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
            enableAlternatingRowColors = true,
            enableDividers = true,
            footerMessage = "Test Settings"
        )
    }

    /**
     * Generate test business profile
     */
    fun generateTestBusinessProfile(
        businessId: Long = 1L
    ): BusinessProfile {
        Timber.d("🏢 Generating test business profile...")

        return BusinessProfile(
            id = businessId,
            businessName = companyNames[random.nextInt(companyNames.size)],
            email = "contact@testbiz.com",
            phone = "555-0100",
            address = "123 Test Street, Test City, TC 12345",
            website = "https://testbiz.com",
            bankName = "Test Bank",
            accountName = "Test Account",
            accountNumber = "1234567890",
            bsbNumber = "123456",
            defaultTaxRate = 0.10f
        )
    }

    /**
     * Generate test customer data
     */
    fun generateTestCustomer(
        customerId: Long = random.nextLong(1, 1000),
        businessId: Long = 1L
    ): Customer {
        Timber.d("👤 Generating test customer...")

        return Customer(
            id = customerId,
            businessId = businessId,
            name = "Test Customer $customerId",
            email = "customer$customerId@test.com",
            phone = "555-${String.format("%04d", customerId)}",
            address = "$customerId Main St, Test City, TC 12345"
        )
    }

    /**
     * Generate test data for all 54 PDF combinations
     *
     * Used by Week 4 robustness testing
     */
    fun generateAllPdfVariants(): List<Pair<PageLayout, ColorScheme>> {
        val layouts = listOf(
            PageLayout.MINIMAL_TABLES,
            PageLayout.CLASSIC,
            PageLayout.MODERN,
            PageLayout.SPACIOUS,
            PageLayout.SIDEBAR,
            PageLayout.CARDS,
            PageLayout.DETAILED,
            PageLayout.PROFESSIONAL,
            PageLayout.ADVANCED_PAGINATED
        )

        val colorSchemes = listOf(
            ColorScheme.PROFESSIONAL,
            ColorScheme.VIBRANT,
            ColorScheme.MINIMAL,
            ColorScheme.WARM,
            ColorScheme.COOL,
            ColorScheme.CUSTOM
        )

        val combinations = mutableListOf<Pair<PageLayout, ColorScheme>>()
        for (layout in layouts) {
            for (scheme in colorSchemes) {
                combinations.add(layout to scheme)
            }
        }

        Timber.d("📊 Generated ${combinations.size} PDF variant combinations")
        return combinations
    }

    /**
     * Generate summary statistics
     */
    fun printSummaryStatistics(invoiceCount: Int, itemsPerInvoice: Int) {
        val totalItems = invoiceCount * itemsPerInvoice
        val estimatedSize = (invoiceCount * 1024) / 1024 // Rough estimate in MB

        Timber.d("")
        Timber.d("📊 TEST DATA SUMMARY")
        Timber.d("=====================================")
        Timber.d("Invoices:        $invoiceCount")
        Timber.d("Items per inv:   $itemsPerInvoice")
        Timber.d("Total items:     $totalItems")
        Timber.d("Est. DB size:    ~${estimatedSize}MB")
        Timber.d("=====================================")
        Timber.d("")
    }
}

/**
 * Test model classes (minimal - these would extend from domain models)
 */
data class Invoice(
    val id: Long,
    val businessId: Long,
    val invoiceNumber: String,
    val customerId: Long,
    val date: Long,
    val dueDate: Long,
    val status: InvoiceStatus,
    val items: List<LineItem>,
    val amount: Double,
    val taxAmount: Double,
    val notes: String
)

data class LineItem(
    val id: Long,
    val invoiceId: Long,
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val taxRate: Double
)

data class Customer(
    val id: Long,
    val businessId: Long,
    val name: String,
    val email: String,
    val phone: String,
    val address: String
)

data class BusinessProfile(
    val id: Long,
    val businessName: String,
    val email: String,
    val phone: String,
    val address: String,
    val website: String,
    val bankName: String,
    val accountName: String,
    val accountNumber: String,
    val bsbNumber: String,
    val defaultTaxRate: Float
)

enum class InvoiceStatus {
    DRAFT, SENT, VIEWED, PAID, OVERDUE, CANCELLED
}

