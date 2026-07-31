package com.emul8r.bizap.data.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.LineItemSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HtmlPdfInvoiceServiceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val settings = InvoiceSettings.default(userId = "test-user")

    @Test
    fun buildPreviewHtml_preservesTestPrefixedHeaderAndSubheader() {
        val snapshot = snapshotWith(header = "testheader", subheader = "testsubheaderS")

        assertEquals("testheader", snapshot.header)
        assertEquals("testsubheaderS", snapshot.subheader)

        val html = HtmlPdfInvoiceService(context, settings).buildPreviewHtml(snapshot, isQuote = false)

        assertTrue(html.contains("Additional Information"))
        assertTrue(html.contains("testheader"))
        assertTrue(html.contains("testsubheaderS"))
    }

    @Test
    fun buildPreviewHtml_preservesValidHeaderSubheaderEdgeCases() {
        val cases = listOf(
            "a" to "b",
            "12345" to "67890",
            "PO-123/456 (Rev. 2)" to "€100.00 #special"
        )

        cases.forEach { (header, subheader) ->
            val html = HtmlPdfInvoiceService(context, settings)
                .buildPreviewHtml(snapshotWith(header = header, subheader = subheader), isQuote = false)

            assertTrue("Expected header to be rendered for '$header'", html.contains(header))
            assertTrue("Expected subheader to be rendered for '$subheader'", html.contains(subheader))
            assertTrue(html.contains("Additional Information"))
        }
    }

    @Test
    fun buildPreviewHtml_filtersExplicitPlaceholderValuesOnly() {
        val html = HtmlPdfInvoiceService(context, settings).buildPreviewHtml(
            snapshotWith(header = "N/A", subheader = "asdasdasd"),
            isQuote = false
        )

        assertFalse(html.contains("N/A"))
        assertFalse(html.contains("asdasdasd"))
        assertFalse(html.contains("Additional Information"))
    }

    private fun snapshotWith(header: String, subheader: String): InvoiceSnapshot {
        return InvoiceSnapshot(
            invoiceId = 101L,
            invoiceNumber = "INV-101",
            customerName = "Customer",
            customerAddress = "1 Main St",
            customerEmail = "customer@example.com",
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            items = listOf(
                LineItemSnapshot(
                    description = "Service",
                    quantity = 1.0,
                    unitPrice = 1000L,
                    total = 1000L
                )
            ),
            subtotal = 1000L,
            taxRate = 10.0,
            taxAmount = 100L,
            totalAmount = 1100L,
            businessName = "Biz",
            businessAbn = "12345678901",
            businessEmail = "biz@example.com",
            businessPhone = "0400000000",
            businessAddress = "2 Business Rd",
            logoBase64 = null,
            header = header,
            subheader = subheader,
            notes = "notes",
            footerText = "footer"
        )
    }
}
