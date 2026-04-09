package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Integration tests for [CalculateInvoiceMetricsUseCase].
 *
 * Verifies that the unified calculation logic produces consistent results
 * for both GUI1 and GUI2 invoice flows.
 */
class CalculateInvoiceMetricsUseCaseTest {

    private lateinit var useCase: CalculateInvoiceMetricsUseCase

    private fun buildInvoice(
        items: List<LineItem>,
        taxRate: Double = 0.0
    ) = Invoice(
        customerId = 1L,
        customerName = "Test Customer",
        date = System.currentTimeMillis(),
        totalAmount = 0L,  // Placeholder — calculated by the use case
        items = items,
        isQuote = false,
        status = InvoiceStatus.DRAFT,
        taxRate = taxRate,
        currencyCode = "AUD"
    )

    @Before
    fun setUp() {
        useCase = CalculateInvoiceMetricsUseCase()
    }

    // ── calculate_simpleInvoice ───────────────────────────────────────────────

    @Test
    fun `calculate_simpleInvoice - calculates correct subtotal`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Item 1", quantity = 2.0, unitPrice = 5000),  // 10000 cents
                LineItem(description = "Item 2", quantity = 1.0, unitPrice = 5000)   // 5000 cents
            ),
            taxRate = 0.10
        )

        val metrics = useCase(invoice)

        assertEquals(15000L, metrics.subtotal)    // 10000 + 5000
    }

    @Test
    fun `calculate_simpleInvoice - calculates correct tax amount`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Item 1", quantity = 2.0, unitPrice = 5000),
                LineItem(description = "Item 2", quantity = 1.0, unitPrice = 5000)
            ),
            taxRate = 0.10
        )

        val metrics = useCase(invoice)

        assertEquals(1500L, metrics.taxAmount)    // 15000 * 0.10
    }

    @Test
    fun `calculate_simpleInvoice - calculates correct total`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Item 1", quantity = 2.0, unitPrice = 5000),
                LineItem(description = "Item 2", quantity = 1.0, unitPrice = 5000)
            ),
            taxRate = 0.10
        )

        val metrics = useCase(invoice)

        assertEquals(16500L, metrics.totalAmount) // 15000 + 1500
    }

    // ── calculate_noTax ───────────────────────────────────────────────────────

    @Test
    fun `calculate_noTax - skips tax when taxRate is zero`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Item 1", quantity = 2.0, unitPrice = 5000)
            ),
            taxRate = 0.0
        )

        val metrics = useCase(invoice)

        assertEquals(10000L, metrics.subtotal)
        assertEquals(0L, metrics.taxAmount)       // Tax skipped
        assertEquals(10000L, metrics.totalAmount)
    }

    @Test
    fun `calculate_noTax - default taxRate of zero means no tax`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Service", quantity = 1.0, unitPrice = 20000)
            )
            // taxRate defaults to 0.0
        )

        val metrics = useCase(invoice)

        assertEquals(0L, metrics.taxAmount)
        assertEquals(20000L, metrics.totalAmount)
    }

    // ── calculate_discountAmount ──────────────────────────────────────────────

    @Test
    fun `calculate_discountAmount - discount is zero by default`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Item", quantity = 1.0, unitPrice = 10000)
            )
        )

        val metrics = useCase(invoice)

        assertEquals(0L, metrics.discountAmount)
    }

    @Test
    fun `calculate_discountAmount - discount reduces total when no tax`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Item", quantity = 1.0, unitPrice = 10000)
            )
        ).copy(discountAmount = 2000L)  // $20 discount on $100 item

        val metrics = useCase(invoice)

        assertEquals(10000L, metrics.subtotal)
        assertEquals(2000L, metrics.discountAmount)
        assertEquals(8000L, metrics.totalAmount)   // 10000 - 2000
    }

    @Test
    fun `calculate_discountAmount - tax applies to discounted subtotal`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Service", quantity = 1.0, unitPrice = 10000)
            ),
            taxRate = 0.10
        ).copy(discountAmount = 2000L)  // $20 discount

        val metrics = useCase(invoice)

        // discountedSubtotal = 10000 - 2000 = 8000
        // taxAmount = 8000 * 0.10 = 800
        // totalAmount = 8000 + 800 = 8800
        assertEquals(10000L, metrics.subtotal)
        assertEquals(2000L, metrics.discountAmount)
        assertEquals(800L, metrics.taxAmount)
        assertEquals(8800L, metrics.totalAmount)
    }

    @Test
    fun `calculate_discountAmount - discount exceeding subtotal is clamped to zero total`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Item", quantity = 1.0, unitPrice = 5000)
            )
        ).copy(discountAmount = 9999L)  // Discount larger than subtotal

        val metrics = useCase(invoice)

        // discountedSubtotal = max(5000 - 9999, 0) = 0
        assertEquals(0L, metrics.totalAmount)
    }

    // ── calculate_singleItem ──────────────────────────────────────────────────

    @Test
    fun `calculate_singleItem - single item invoice calculates correctly`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Consulting", quantity = 3.0, unitPrice = 10000) // 30000 cents
            ),
            taxRate = 0.10
        )

        val metrics = useCase(invoice)

        assertEquals(30000L, metrics.subtotal)
        assertEquals(3000L, metrics.taxAmount)    // 30000 * 0.10
        assertEquals(33000L, metrics.totalAmount) // 30000 + 3000
    }

    // ── calculate_fractionalQuantity ──────────────────────────────────────────

    @Test
    fun `calculate_fractionalQuantity - handles fractional quantities correctly`() {
        val invoice = buildInvoice(
            items = listOf(
                LineItem(description = "Half day", quantity = 0.5, unitPrice = 20000) // 10000 cents
            ),
            taxRate = 0.0
        )

        val metrics = useCase(invoice)

        assertEquals(10000L, metrics.subtotal)
        assertEquals(10000L, metrics.totalAmount)
    }
}
