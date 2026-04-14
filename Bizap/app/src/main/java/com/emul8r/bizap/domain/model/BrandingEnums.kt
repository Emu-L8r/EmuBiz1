package com.emul8r.bizap.domain.model

/**
 * PHASE 3: BRANDING LAYER ENUMS
 *
 * These enums support advanced branding features:
 * - Logo placement
 * - QR code positioning
 * - Payment method icons
 * - Signature types
 * - Social media platforms
 */

/**
 * Logo position options - where to place logo on invoice
 *
 * TOP_LEFT: Logo in top-left corner (default, common position)
 * TOP_CENTER: Logo centered at top (professional look)
 * TOP_RIGHT: Logo in top-right corner (modern style)
 * SIDE_LEFT: Logo in left sidebar area (distinctive layout)
 * SIDE_RIGHT: Logo in right sidebar area (right-aligned branding)
 */
enum class LogoPosition(val displayName: String, val description: String) {
    TOP_LEFT("Top Left", "Logo in top-left corner (default)"),
    TOP_CENTER("Top Center", "Logo centered at top"),
    TOP_RIGHT("Top Right", "Logo in top-right corner"),
    SIDE_LEFT("Left Sidebar", "Logo in left sidebar"),
    SIDE_RIGHT("Right Sidebar", "Logo in right sidebar")
}

/**
 * QR code position options - where to place QR code on invoice
 * Common placements for scannable QR codes in professional invoices
 *
 * TOP_RIGHT: Header area (often paired with logo)
 * BOTTOM_RIGHT: Footer area (most common, standard position)
 * BOTTOM_LEFT: Alternative footer position
 * TOP_LEFT: Alternative header position
 * INLINE_PAYMENT: Next to payment instructions (most accessible for customers)
 */
enum class QrCodePosition(val displayName: String, val description: String) {
    TOP_RIGHT("Top Right", "QR code in top-right corner"),
    BOTTOM_RIGHT("Bottom Right", "QR code in bottom-right corner (most common)"),
    BOTTOM_LEFT("Bottom Left", "QR code in bottom-left corner"),
    TOP_LEFT("Top Left", "QR code in top-left corner"),
    INLINE_PAYMENT("Inline with Payment", "QR code next to payment instructions")
}

/**
 * Accepted payment method icons for invoices
 * Display what payment methods you accept on the invoice
 *
 * CREDIT_CARD: Visa, Mastercard, American Express
 * BANK_TRANSFER: Direct bank transfer / ACH
 * PAYPAL: PayPal payments
 * APPLE_PAY: Apple Pay
 * GOOGLE_PAY: Google Pay / Android Pay
 * CRYPTO: Bitcoin, Ethereum, or other cryptocurrency
 * CASH: Cash payment options
 * CHECK: Check payment
 * BANK_DEPOSIT: Bank deposit slip
 * WIRE_TRANSFER: International wire transfer
 */
enum class PaymentMethod(val displayName: String, val icon: String, val description: String) {
    CREDIT_CARD("Credit Card", "💳", "Visa, Mastercard, American Express"),
    BANK_TRANSFER("Bank Transfer", "🏦", "Direct bank transfer / ACH"),
    PAYPAL("PayPal", "🅿️", "PayPal payment"),
    APPLE_PAY("Apple Pay", "🍎", "Apple Pay"),
    GOOGLE_PAY("Google Pay", "🔵", "Google Pay / Android Pay"),
    CRYPTO("Cryptocurrency", "₿", "Bitcoin, Ethereum, etc"),
    CASH("Cash", "💵", "Cash payment"),
    CHECK("Check", "✓", "Check payment"),
    BANK_DEPOSIT("Bank Deposit", "📊", "Bank deposit slip"),
    WIRE_TRANSFER("Wire Transfer", "📞", "International wire transfer")
}

/**
 * Signature authorization types for invoice signing
 *
 * HANDWRITTEN: Traditional handwritten signature line
 * DIGITAL: Digital/electronic signature with QR verification
 * PRINTED_NAME: Printed name line only (no signature)
 * INITIALS: Initials only (compact)
 */
enum class SignatureType(val displayName: String, val description: String) {
    HANDWRITTEN("Handwritten Signature", "Traditional handwritten signature line"),
    DIGITAL("Digital Signature", "QR/digital signature verification"),
    PRINTED_NAME("Printed Name", "Printed name line only"),
    INITIALS("Initials", "Initials only (compact)")
}

/**
 * Social media platforms for company branding
 * Display company social media links/handles on invoice
 */
enum class SocialMediaPlatform(val displayName: String, val icon: String) {
    FACEBOOK("Facebook", "f"),
    TWITTER("Twitter/X", "𝕏"),
    INSTAGRAM("Instagram", "📸"),
    LINKEDIN("LinkedIn", "in"),
    YOUTUBE("YouTube", "▶️"),
    TIKTOK("TikTok", "♪"),
    WEBSITE("Website", "🌐"),
    EMAIL("Email", "✉️")
}

