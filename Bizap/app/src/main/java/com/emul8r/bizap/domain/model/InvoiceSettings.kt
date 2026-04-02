package com.emul8r.bizap.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Centralized invoice settings and customization.
 * Stored per user, applied to all invoices unless overridden.
 *
 * This is the single source of truth for all invoice-related settings:
 * - Theme selection (Canvas or HTML-to-PDF)
 * - Company branding (logo, colors, contact info)
 * - Payment details (terms, bank info)
 * - Tax configuration (rate, name, handling mode)
 * - Invoice defaults (prefix, notes, footer message)
 */
@Entity(tableName = "invoice_settings")
data class InvoiceSettings(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,

    // PDF TEMPLATE SELECTION
    @ColumnInfo(name = "selected_theme")
    val selectedTheme: InvoiceTheme = InvoiceTheme.CANVAS,

    // PDF STYLING (kept for invoice appearance)
    @ColumnInfo(name = "primary_color")
    val primaryColor: String = "#6B4C9A",      // Default purple
    @ColumnInfo(name = "secondary_color")
    val secondaryColor: String? = null,
    @ColumnInfo(name = "accent_color")
    val accentColor: String? = null,
    @ColumnInfo(name = "font_family")
    val fontFamily: String? = null,

    // PDF INVOICE CONFIGURATION
    @ColumnInfo(name = "payment_terms_days")
    val paymentTermsDays: Int = 30,
    @ColumnInfo(name = "default_payment_notes")
    val defaultPaymentNotes: String = "",
    @ColumnInfo(name = "footer_message")
    val footerMessage: String = "Thank you for your business",
    @ColumnInfo(name = "invoice_number_prefix")
    val invoiceNumberPrefix: String = "INV-",

    // TAX CONFIGURATION (for PDF display)
    @ColumnInfo(name = "tax_rate")
    val taxRate: Double = 0.10,
    @ColumnInfo(name = "tax_name")
    val taxName: String = "GST",
    @ColumnInfo(name = "tax_handling")
    val taxHandling: TaxHandling = TaxHandling.EXCLUSIVE,

    // METADATA
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    /**
     * Validate settings for PDF-specific fields only.
     * Business information validation happens in BusinessProfile, not here.
     */
    fun isValid(): Boolean {
        return selectedTheme != null
    }

    /**
     * Get default settings for user.
     */
    companion object {
        fun default(userId: String) = InvoiceSettings(userId = userId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InvoiceSettings

        if (userId != other.userId) return false
        if (selectedTheme != other.selectedTheme) return false
        if (primaryColor != other.primaryColor) return false
        if (taxRate != other.taxRate) return false
        if (paymentTermsDays != other.paymentTermsDays) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + selectedTheme.hashCode()
        result = 31 * result + primaryColor.hashCode()
        result = 31 * result + taxRate.hashCode()
        result = 31 * result + paymentTermsDays
        return result
    }
}

/**
 * Enum for invoice theme selection.
 *
 * CANVAS: Existing Canvas-based PDF generation (Phase 9 implementation)
 * HTML_PDF: New HTML-to-PDF modern style (Phase 6 implementation)
 */
enum class InvoiceTheme {
    CANVAS,      // Existing Canvas-based PDF
    HTML_PDF     // New HTML-to-PDF modern style
}

/**
 * Enum for tax handling mode.
 *
 * INCLUSIVE: Tax is included in the amount shown
 * EXCLUSIVE: Tax is added to the amount (default, Australian style)
 */
enum class TaxHandling {
    INCLUSIVE,   // Tax included in amount
    EXCLUSIVE    // Tax added to amount
}


