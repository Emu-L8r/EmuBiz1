package com.emul8r.bizap.data.service

import android.content.Context
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.util.TestDataBuilder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Unit tests for [CsvExportService].
 *
 * Validates currency formatting, tax-rate column, notes/footer sections,
 * and optional header output.  Uses a [TemporaryFolder] rule so that
 * the service can create real files without hitting Android-only APIs.
 */
class CsvExportServiceTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var service: CsvExportService
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk {
            every { filesDir } returns tmpFolder.root
        }
        service = CsvExportService(context)
    }

    // ── Currency formatting ────────────────────────────────────────────────────

    @Test
    fun `exportSingleInvoice - amounts have Australian dollar A$ prefix`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice(
            totalAmount = 30000L,   // A$300.00
            amountPaid  = 15000L,   // A$150.00
            taxAmount   = 2727L     // A$27.27
        )

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("Subtotal should have A\$ prefix", content.contains("A\$"))
        assertTrue("Total A\$300.00 expected",   content.contains("A\$300.00"))
        assertTrue("AmountPaid A\$150.00 expected", content.contains("A\$150.00"))
        // Balance = 300 - 150 = A$150.00 also present
        assertTrue("Balance A\$150.00 expected", content.contains("A\$150.00"))
    }

    @Test
    fun `exportInvoiceList - amounts have A dollar prefix`() = runBlocking {
        val invoices = listOf(
            TestDataBuilder.buildInvoice(totalAmount = 30000L, amountPaid = 0L)
        )

        val file = service.exportInvoiceList(invoices)
        val content = file.readText()

        assertTrue("Total A\$300.00 expected in list export", content.contains("A\$300.00"))
    }

    // ── Tax Rate column ────────────────────────────────────────────────────────

    @Test
    fun `exportSingleInvoice - header row contains Tax Rate column`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice()

        val file = service.exportSingleInvoice(invoice)
        val firstLine = file.readLines().first()

        assertTrue("Header must contain 'Tax Rate'", firstLine.contains("Tax Rate"))
    }

    @Test
    fun `exportSingleInvoice - ten percent tax rate formatted correctly`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice(taxRate = 0.10)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("10% tax rate should appear as '10.0%'", content.contains("10.0%"))
    }

    @Test
    fun `exportSingleInvoice - fifteen percent tax rate formatted correctly`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice(taxRate = 0.15)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("15% tax rate should appear as '15.0%'", content.contains("15.0%"))
    }

    // ── Notes section ──────────────────────────────────────────────────────────

    @Test
    fun `exportSingleInvoice - notes section header always present`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice()

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("Notes section header expected", content.contains("Notes/Special Instructions"))
    }

    @Test
    fun `exportSingleInvoice - invoice notes text exported`() = runBlocking {
        val noteText = "Paint interior, 3 coats"
        val invoice = TestDataBuilder.buildInvoice().copy(notes = noteText)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("Notes text should be in export", content.contains(noteText))
    }

    @Test
    fun `exportSingleInvoice - null notes shows No notes placeholder`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice().copy(notes = null)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("'No notes' placeholder expected when notes is null", content.contains("No notes"))
    }

    @Test
    fun `exportSingleInvoice - blank notes shows No notes placeholder`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice().copy(notes = "")

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("'No notes' placeholder expected when notes is blank", content.contains("No notes"))
    }

    // ── Payment Terms / Footer section ────────────────────────────────────────

    @Test
    fun `exportSingleInvoice - payment terms section header always present`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice()

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("Payment Terms section header expected", content.contains("Payment Terms"))
    }

    @Test
    fun `exportSingleInvoice - footer text exported as payment terms`() = runBlocking {
        val footerText = "Net 30 from invoice date. 5% discount for cash payment."
        val invoice = TestDataBuilder.buildInvoice().copy(footer = footerText)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("Footer text should appear under Payment Terms", content.contains(footerText))
    }

    @Test
    fun `exportSingleInvoice - null footer shows No specific terms placeholder`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice().copy(footer = null)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("'No specific terms' expected when footer is null", content.contains("No specific terms"))
    }

    @Test
    fun `exportSingleInvoice - blank footer shows No specific terms placeholder`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice().copy(footer = "")

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("'No specific terms' expected when footer is blank", content.contains("No specific terms"))
    }

    // ── Invoice Header section ─────────────────────────────────────────────────

    @Test
    fun `exportSingleInvoice - header section emitted when header set`() = runBlocking {
        val headerText = "Custom Invoice Header"
        val invoice = TestDataBuilder.buildInvoice().copy(header = headerText)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertTrue("Invoice Header section label expected", content.contains("Invoice Header"))
        assertTrue("Header text should appear in export", content.contains(headerText))
    }

    @Test
    fun `exportSingleInvoice - header section omitted when header is null`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice().copy(header = null)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertFalse("Invoice Header section should not appear when header is null",
            content.contains("Invoice Header"))
    }

    @Test
    fun `exportSingleInvoice - header section omitted when header is blank`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice().copy(header = "")

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        assertFalse("Invoice Header section should not appear when header is blank",
            content.contains("Invoice Header"))
    }

    // ── RFC 4180 compliance ────────────────────────────────────────────────────

    @Test
    fun `exportSingleInvoice - commas in notes are properly escaped`() = runBlocking {
        val noteText = "Paint walls, ceiling, and trim"
        val invoice = TestDataBuilder.buildInvoice().copy(notes = noteText)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        // RFC 4180: fields containing commas must be enclosed in double-quotes
        assertTrue("Notes with commas should be quoted", content.contains("\"$noteText\""))
    }

    @Test
    fun `exportSingleInvoice - double quotes in notes are escaped per RFC 4180`() = runBlocking {
        val noteText = "He said \"urgent\""
        val invoice = TestDataBuilder.buildInvoice().copy(notes = noteText)

        val file = service.exportSingleInvoice(invoice)
        val content = file.readText()

        // Embedded double-quote → doubled double-quote inside enclosing quotes
        assertTrue("Embedded quotes should be doubled", content.contains("He said \"\"urgent\"\""))
    }

    // ── File creation ──────────────────────────────────────────────────────────

    @Test
    fun `exportSingleInvoice - returns existing File`() = runBlocking {
        val invoice = TestDataBuilder.buildInvoice()

        val file = service.exportSingleInvoice(invoice)

        assertTrue("Exported file must exist", file.exists())
        assertTrue("Exported file must not be empty", file.length() > 0)
    }

    @Test
    fun `exportInvoiceList - returns existing File`() = runBlocking {
        val invoices = TestDataBuilder.buildInvoiceList(count = 3)

        val file = service.exportInvoiceList(invoices)

        assertTrue("Exported list file must exist", file.exists())
        assertTrue("Exported list file must not be empty", file.length() > 0)
    }
}
