package com.emul8r.bizap.domain.util

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.LineItemSnapshot
import timber.log.Timber

/**
 * UNIFIED SETTINGS → SNAPSHOT MAPPER
 *
 * Centralizes all settings field mappings from InvoiceSettings to InvoiceSnapshot.
 * Eliminates duplication across multiple ViewModels (CreateInvoiceViewModel, PrintPreviewViewModel).
 *
 * Benefits:
 * - Single source of truth for all settings mappings
 * - Ensures consistency across all PDF generation paths
 * - Simplifies future settings additions (only update this file)
 * - Enables proper nullability handling with sensible defaults
 *
 * Usage:
 *   val snapshot = invoiceSettings?.toSnapshot(invoice, profile)
 *       ?: createDefaultSnapshot(invoice, profile)
 */

/**
 * Extension function: Convert InvoiceSettings + Invoice + BusinessProfile → InvoiceSnapshot
 *
 * Maps all 60+ settings fields to the snapshot with proper elvis operators for null safety.
 * Non-settings fields (invoice data, profile data) are passed separately and copied as-is.
 *
 * @param invoice The invoice being converted (provides: id, number, customer, dates, items, etc.)
 * @param profile The business profile (provides: businessName, abn, email, phone, address, logo)
 * @return Fully-populated InvoiceSnapshot with all settings applied
 */
fun InvoiceSettings.toSnapshot(
    invoice: Invoice,
    profile: BusinessProfile
): InvoiceSnapshot {
    // Helper to convert ISO-8601 date string to epoch milliseconds
    fun String.toEpochMillis(): Long {
        return try {
            if (this.isBlank()) 0L
            else java.time.Instant.parse(this).toEpochMilli()
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse date string: $this")
            0L
        }
    }

    return InvoiceSnapshot(
        // ─────────────────────────────────────────────────────────────────
        // CORE INVOICE DATA (NOT from settings, passed directly)
        // ─────────────────────────────────────────────────────────────────
        invoiceId = invoice.id,
        invoiceNumber = invoice.invoiceNumber,
        displayName = invoice.customerName,  // Used for display purposes
        customerName = invoice.customerName,
        customerAddress = invoice.customerAddress,
        customerEmail = invoice.customerEmail ?: "",
        customerPhone = invoice.customerPhone,
        date = invoice.dateCreated.toEpochMillis(),  // ✅ ADDED: Invoice creation date for PDF naming and rendering
        dueDate = invoice.dueDate.toEpochMillis(),   // ✅ FIXED: Due date for PDF rendering
        items = invoice.items.map {
            val itemTotal = (it.unitPrice * it.quantity).toLong()
            LineItemSnapshot(
                description = it.description,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                total = itemTotal,
                isDiscount = false  // Invoice items don't have discount flag yet, default to false
            )
        },
        subtotal = invoice.totalAmount - invoice.taxAmount,
        taxRate = invoice.taxRate,
        taxAmount = invoice.taxAmount,
        totalAmount = invoice.totalAmount,

        // ─────────────────────────────────────────────────────────────────
        // BUSINESS PROFILE DATA (NOT from settings)
        // ─────────────────────────────────────────────────────────────────
        businessName = profile.businessName,
        businessAbn = profile.abn,
        businessEmail = profile.email,
        businessPhone = profile.phone,
        businessAddress = profile.address,
        logoBase64 = profile.logoBase64,
        currencyCode = invoice.currencyCode ?: "AUD",

        // ─────────────────────────────────────────────────────────────────
        // INVOICE CUSTOMIZATION (header/subheader/footer/notes)
        // NOTE: These come from the Invoice entity, not settings
        // ─────────────────────────────────────────────────────────────────
        header = invoice.header ?: "",
        subheader = invoice.subheader ?: "",
        footerText = invoice.footer ?: "",
        notes = invoice.notes ?: "",

        // ─────────────────────────────────────────────────────────────────
        // BANK / PAYMENT DETAILS (from business profile)
        // ─────────────────────────────────────────────────────────────────
        bankAccountName = profile.accountName ?: "",
        bankAccountNumber = profile.accountNumber ?: "",
        bankBsb = profile.bsbNumber ?: "",
        bankName = profile.bankName ?: "",

        // ─────────────────────────────────────────────────────────────────
        // ✅ ROUND 3 — SETTINGS-TO-PDF LINKAGE (Compliance Critical)
        // ─────────────────────────────────────────────────────────────────
        taxName = this.taxName,
        paymentTermsDays = this.paymentTermsDays,
        taxHandling = this.taxHandling,
        defaultPaymentNotes = this.defaultPaymentNotes,
        companyWebsite = this.companyWebsite,

        // ─────────────────────────────────────────────────────────────────
        // ✅ ROUND 3 PHASE 2 — QUICK WINS (Visual Styling)
        // ─────────────────────────────────────────────────────────────────
        alternateRowColor = this.alternateRowColor,
        dividerColor = this.dividerColor,
        dividerThicknessPx = this.dividerThicknessPx,
        enableAlternatingRowColors = this.enableAlternatingRowColors,
        enableDividers = this.enableDividers,

        // Color & Style Settings
        primaryColor = this.primaryColor,
        secondaryColor = this.secondaryColor,
        cornerRadiusDp = this.cornerRadiusDp,
        shadowIntensity = this.shadowIntensity,

        // Quick Win #1: Highlight Totals
        highlightTotals = this.highlightTotals,
        totalBoxStyle = this.totalBoxStyle,

        // ─────────────────────────────────────────────────────────────────
        // ✅ PHASE 2 IMPLEMENTATION (Features 1-5)
        // ─────────────────────────────────────────────────────────────────
        selectedLocale = this.selectedLocale,
        selectedTypography = this.selectedTypography,
        selectedColorScheme = this.selectedColorScheme,
        selectedSpacingProfile = this.selectedSpacingProfile,
        visualAccentsJson = this.visualAccentsJson,

        // ─────────────────────────────────────────────────────────────────
        // ✅ PHASE 3 QUICK-WIN FIELDS (Motto, Signature, Watermark, Badges)
        // ─────────────────────────────────────────────────────────────────
        enableMotto = this.enableMotto,
        mottoText = this.mottoText,
        mottoFontSize = this.mottoFontSize,
        mottoColor = this.mottoColor,

        enableSignatureArea = this.enableSignatureArea,
        signatureLabel = this.signatureLabel,
        signatureLineLengthMm = this.signatureLineLengthMm,

        enableWatermarkText = this.enableWatermarkText,
        watermarkText = this.watermarkText,
        watermarkOpacity = this.watermarkOpacity,

        enableStatusBadges = this.enableStatusBadges,
        badgeStyle = this.badgeStyle,
        dividerStyle = this.dividerStyle,

        enableGradientHeader = this.enableGradientHeader,
        headerGradientEndColor = this.headerGradientEndColor,

        // ─────────────────────────────────────────────────────────────────
        // ✅ PHASE 3 COMPLEX FEATURES (Logo, QR, Payment Icons, Patterns)
        // ─────────────────────────────────────────────────────────────────
        enableLogo = this.enableLogo,
        logoUri = this.logoUri,
        logoWidthMm = this.logoWidthMm,
        logoHeightMm = this.logoHeightMm,
        logoPosition = this.logoPosition,

        enableQrCode = this.enableQrCode,
        qrCodeContent = this.qrCodeContent,
        qrCodeSizeMm = this.qrCodeSizeMm,
        qrCodePosition = this.qrCodePosition,

        enablePaymentIcons = this.enablePaymentIcons,
        acceptedPaymentMethodsJson = this.acceptedPaymentMethodsJson,
        paymentIconsSize = this.paymentIconsSize,

        enableBackgroundPattern = this.enableBackgroundPattern,
        backgroundPatternType = this.backgroundPatternType,
        patternOpacity = this.patternOpacity,
        enableBrandWatermark = this.enableBrandWatermark,
        watermarkImage = this.watermarkImage,

        // Status (typically set by caller, default to DRAFT for new invoices)
        invoiceStatus = "DRAFT"
    ).also {
        Timber.d("✅ Generated snapshot with ${this::class.simpleName} settings applied")
    }
}

/**
 * Create a default snapshot with all settings using default values.
 * Used as fallback when InvoiceSettings can't be loaded.
 *
 * @param invoice The invoice being converted
 * @param profile The business profile
 * @return InvoiceSnapshot with all default InvoiceSettings values
 */
fun createDefaultSnapshot(invoice: Invoice, profile: BusinessProfile): InvoiceSnapshot {
    val defaultSettings = InvoiceSettings.default(userId = "unknown")
    return defaultSettings.toSnapshot(invoice, profile).also {
        Timber.w("⚠️ Using default settings for snapshot (settings not available)")
    }
}





