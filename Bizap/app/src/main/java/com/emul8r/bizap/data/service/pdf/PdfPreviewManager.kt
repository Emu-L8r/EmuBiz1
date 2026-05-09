package com.emul8r.bizap.data.service.pdf

import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.LineItemSnapshot
import com.emul8r.bizap.domain.model.InvoiceLocale
import com.emul8r.bizap.domain.model.Typography
import com.emul8r.bizap.domain.model.ColorScheme
import com.emul8r.bizap.domain.model.SpacingProfile
import com.emul8r.bizap.domain.model.TotalBoxStyle
import com.emul8r.bizap.domain.model.TaxHandling
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages PDF preview generation for the settings UI.
 *
 * Generates live previews as user changes settings, with intelligent debouncing
 * to avoid regenerating too frequently.
 */
@Singleton
class PdfPreviewManager @Inject constructor(
    private val pdfSettingsResolver: PdfSettingsResolver,
    private val cssGenerator: CssGenerator,
    private val layoutSelector: LayoutSelector
) {

    companion object {
        private const val TAG = "PdfPreviewManager"
        private const val PREVIEW_DEBOUNCE_MS = 1000L  // Don't regenerate too frequently
    }

    /**
     * Generate a sample invoice for preview purposes.
     * Uses consistent data so previews are stable and predictable.
     * Note: Monetary amounts are in cents (Long), as per InvoiceSnapshot spec.
     */
    private fun createSampleInvoice(): InvoiceSnapshot {
        return InvoiceSnapshot(
            invoiceId = 1L,
            invoiceNumber = "INV-2026-001",
            displayName = "Sample Invoice",
            customerName = "John Smith",
            customerAddress = "456 Client Avenue, Client City, CT 54321",
            customerEmail = "john@customer.com",
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),  // 30 days out

            items = listOf(
                // Sample line items for preview (amounts in cents)
                LineItemSnapshot(
                    description = "Professional Consulting Services - 10 hours @ $150/hr",
                    quantity = 10.0,
                    unitPrice = 15000,  // $150.00 in cents
                    total = 150000  // $1,500.00 in cents
                ),
                LineItemSnapshot(
                    description = "Software License - Annual",
                    quantity = 1.0,
                    unitPrice = 50000,  // $500.00 in cents
                    total = 50000  // $500.00 in cents
                ),
                LineItemSnapshot(
                    description = "Technical Support Package",
                    quantity = 1.0,
                    unitPrice = 20000,  // $200.00 in cents
                    total = 20000  // $200.00 in cents
                )
            ),

            subtotal = 220000,  // $2,200.00 in cents
            taxRate = 0.10,
            taxAmount = 22000,  // $220.00 in cents (10%)
            totalAmount = 242000,  // $2,420.00 in cents

            businessName = "Sample Business Inc.",
            businessAbn = "12 345 678 901",
            businessEmail = "contact@example.com",
            businessPhone = "+1 (555) 123-4567",
            businessAddress = "123 Business Street, Suite 100, City, ST 12345",

            logoBase64 = null,
            currencyCode = "AUD",
            header = "",
            subheader = "",
            footerText = "Thank you for your business!",
            notes = "Sample invoice for preview purposes",
            bankAccountName = "ABC Company",
            bankAccountNumber = "123456789",
            bankBsb = "123456",
            bankName = "Sample Bank",

            taxName = "GST",
            paymentTermsDays = 30,
            companyWebsite = "www.example.com"
        )
    }

    /**
     * Observe live PDF preview as settings change.
     *
     * Generates a preview with sample data, debounced to avoid excessive regeneration.
     * Returns the HTML string that would be rendered to PDF.
     */
    fun observePreview(
        userId: String,
        businessId: Long,
        settings: com.emul8r.bizap.domain.model.InvoiceSettings  // Settings to preview
    ): Flow<String?> = flow {
        Timber.tag(TAG).d("Creating preview PDF observable")

        try {
            val sampleInvoice = createSampleInvoice()

            // Resolve settings
            val resolved = pdfSettingsResolver.resolve(userId, businessId, sampleInvoice)

            // Generate CSS with current settings
            val css = cssGenerator.generateCss(
                baseTemplate = settings.selectedHtmlStyle,
                colorScheme = resolved.colorScheme,
                spacingProfile = resolved.spacingProfile,
                visualAccents = resolved.visualAccents,
                totalBoxStyle = resolved.totalBoxStyle
            )

            // Select layout
            val layout = resolved.selectedLayout

            // Generate HTML (this would be the actual HTML that gets converted to PDF)
            val html = layout.generateHtml(sampleInvoice, css)

            Timber.tag(TAG).d("✅ Preview PDF content generated (${html.length} bytes)")

            // Emit the HTML (can be rendered to PDF in UI layer)
            emit(html)

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Preview generation failed")
            emit(null)
        }
    }
        .debounce(PREVIEW_DEBOUNCE_MS)  // Don't regenerate on every keystroke
        .catch { e ->
            Timber.tag(TAG).e(e, "Error in preview flow")
            emit(null)
        }
}




