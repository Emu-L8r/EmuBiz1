package com.emul8r.bizap.domain.model

/**
 * IMMUTABLE SNAPSHOT OF INVOICE AT GENERATION TIME
 * All monetary amounts are stored as Long (cents)
 */
data class InvoiceSnapshot(
    val invoiceId: Long,
    val invoiceNumber: String,
    val displayName: String = "",
    val customerName: String,
    val customerAddress: String,
    val customerEmail: String?,
    val date: Long,
    val dueDate: Long,
    val items: List<LineItemSnapshot>,
    val subtotal: Long,            // Cents
    val taxRate: Double,
    val taxAmount: Long,           // Cents
    val totalAmount: Long,         // Cents
    val businessName: String,
    val businessAbn: String,
    val businessEmail: String,
    val businessPhone: String,
    val businessAddress: String,
    val logoBase64: String?,
    val currencyCode: String = "AUD",
    // Standardized naming: Header/Subheader terminology (Phase 2.0.3)
    val header: String = "",
    val subheader: String = "",
    val footerText: String = "",
    val notes: String = "",
    // Bank / payment details — shown in the PDF "Payment Details" section
    val bankAccountName: String = "",
    val bankAccountNumber: String = "",
    val bankBsb: String = "",
    val bankName: String = "",
    // Invoice status for watermarks (DRAFT, SENT, PAID, OVERDUE, PARTIALLY_PAID, CANCELLED)
    val invoiceStatus: String = "DRAFT",
    // ✅ ROUND 3 — Settings-to-PDF Linkage (Compliance Critical)
    val taxName: String = "GST",                                    // From InvoiceSettings.taxName
    val paymentTermsDays: Int = 30,                                 // From InvoiceSettings.paymentTermsDays
    val taxHandling: com.emul8r.bizap.domain.model.TaxHandling = com.emul8r.bizap.domain.model.TaxHandling.EXCLUSIVE,  // From InvoiceSettings.taxHandling
    val defaultPaymentNotes: String = "",                           // From InvoiceSettings.defaultPaymentNotes
    val footerMessage: String = "Thank you for your business",     // From InvoiceSettings.footerMessage
    val companyWebsite: String = "",                                // From InvoiceSettings.companyWebsite
    // ✅ ROUND 3 PHASE 2 — Quick Wins (Visual Styling)
    val alternateRowColor: String = "#F5F5F5",                      // From InvoiceSettings.alternateRowColor
    val dividerColor: String = "#CCCCCC",                           // From InvoiceSettings.dividerColor
    val dividerThicknessPx: Float = 1f,                             // From InvoiceSettings.dividerThicknessPx
    val enableAlternatingRowColors: Boolean = true,                 // From InvoiceSettings.enableAlternatingRowColors
    val enableDividers: Boolean = true,                             // From InvoiceSettings.enableDividers
    val isQuote: Boolean = false,                                    // Is this a quote (not an invoice)?
     val primaryColor: String = "#6B4C9A",                            // From InvoiceSettings.primaryColor (theme)
     val secondaryColor: String = "#f5f5f5",                          // From InvoiceSettings.secondaryColor (background)
     val cornerRadiusDp: Float = 8f,                                  // From InvoiceSettings.cornerRadiusDp
     val shadowIntensity: Float = 0.15f,                              // From InvoiceSettings.shadowIntensity
     // ✅ PHASE 2 ADDITIONAL FIELDS (Quick Wins Part 2)
     val highlightTotals: Boolean = true,                             // From InvoiceSettings.highlightTotals
     val totalBoxStyle: com.emul8r.bizap.domain.model.TotalBoxStyle = com.emul8r.bizap.domain.model.TotalBoxStyle.SUBTLE_BACKGROUND,  // From InvoiceSettings.totalBoxStyle
     // ✅ PHASE 2 IMPLEMENTATION (Features 1-5)
     val selectedLocale: com.emul8r.bizap.domain.model.InvoiceLocale = com.emul8r.bizap.domain.model.InvoiceLocale.AUSTRALIAN,  // Feature 1: Locale Support
     val selectedTypography: com.emul8r.bizap.domain.model.Typography = com.emul8r.bizap.domain.model.Typography.MODERN,
     val selectedColorScheme: com.emul8r.bizap.domain.model.ColorScheme = com.emul8r.bizap.domain.model.ColorScheme.PROFESSIONAL,
     val selectedSpacingProfile: com.emul8r.bizap.domain.model.SpacingProfile = com.emul8r.bizap.domain.model.SpacingProfile.NORMAL,
     val visualAccentsJson: String = "",
     // ✅ PHASE 3 QUICK-WIN FIELDS — UI already built, now wiring to PDF renderer
     val enableMotto: Boolean = false,                                  // From InvoiceSettings.enableMotto
     val mottoText: String = "",                                        // From InvoiceSettings.mottoText
     val mottoFontSize: Float = 10f,                                    // From InvoiceSettings.mottoFontSize
     val mottoColor: String = "#666666",                                // From InvoiceSettings.mottoColor
     val enableSignatureArea: Boolean = false,                          // From InvoiceSettings.enableSignatureArea
     val signatureLabel: String = "Authorized By:",                     // From InvoiceSettings.signatureLabel
     val signatureLineLengthMm: Float = 40f,                           // From InvoiceSettings.signatureLineLengthMm
     val enableWatermarkText: Boolean = false,                          // From InvoiceSettings.enableWatermarkText
     val watermarkText: String = "",                                    // From InvoiceSettings.watermarkText
     val watermarkOpacity: Float = 0.1f,                               // From InvoiceSettings.watermarkOpacity
     val enableStatusBadges: Boolean = true,                           // From InvoiceSettings.enableStatusBadges
     val badgeStyle: com.emul8r.bizap.domain.model.BadgeStyle = com.emul8r.bizap.domain.model.BadgeStyle.ROUNDED_FILLED,  // From InvoiceSettings.badgeStyle
     val dividerStyle: com.emul8r.bizap.domain.model.DividerStyle = com.emul8r.bizap.domain.model.DividerStyle.SOLID,     // From InvoiceSettings.dividerStyle
     val enableGradientHeader: Boolean = true,                          // From InvoiceSettings.enableGradientHeader
     val headerGradientEndColor: String = "#FF9F43",                   // From InvoiceSettings.headerGradientEndColor
     // ✅ PHASE 3 COMPLEX FEATURES — Logo, QR, Payment Icons, Patterns
     val enableLogo: Boolean = false,                                  // From InvoiceSettings.enableLogo
     val logoUri: String = "",                                         // From InvoiceSettings.logoUri (file URI)
     val logoWidthMm: Float = 30f,                                     // From InvoiceSettings.logoWidthMm
     val logoHeightMm: Float = 30f,                                    // From InvoiceSettings.logoHeightMm
     val logoPosition: com.emul8r.bizap.domain.model.LogoPosition = com.emul8r.bizap.domain.model.LogoPosition.TOP_LEFT, // From InvoiceSettings.logoPosition
     val enableQrCode: Boolean = false,                                // From InvoiceSettings.enableQrCode
     val qrCodeContent: String = "",                                   // From InvoiceSettings.qrCodeContent
     val qrCodeSizeMm: Float = 20f,                                    // From InvoiceSettings.qrCodeSizeMm
     val qrCodePosition: com.emul8r.bizap.domain.model.QrCodePosition = com.emul8r.bizap.domain.model.QrCodePosition.BOTTOM_RIGHT, // From InvoiceSettings.qrCodePosition
     val enablePaymentIcons: Boolean = false,                          // From InvoiceSettings.enablePaymentIcons
     val acceptedPaymentMethodsJson: String = "[]",                    // From InvoiceSettings.acceptedPaymentMethodsJson
     val paymentIconsSize: Float = 16f,                                // From InvoiceSettings.paymentIconsSize
     val enableBackgroundPattern: Boolean = false,                     // From InvoiceSettings.enableBackgroundPattern
     val backgroundPatternType: com.emul8r.bizap.domain.model.BackgroundPattern = com.emul8r.bizap.domain.model.BackgroundPattern.WAVES, // From InvoiceSettings.backgroundPatternType
     val patternOpacity: Float = 0.08f                                 // From InvoiceSettings.patternOpacity
 )

data class LineItemSnapshot(
    val description: String,
    val quantity: Double,
    val unitPrice: Long,           // Cents
    val total: Long,               // Cents (unitPrice * quantity)
    val isDiscount: Boolean = false // ✅ PHASE 3 FEATURE #3: Discount Rows
)
