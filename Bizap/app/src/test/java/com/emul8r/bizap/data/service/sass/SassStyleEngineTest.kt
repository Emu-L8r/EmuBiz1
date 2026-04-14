package com.emul8r.bizap.data.service.sass

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SassStyleEngine], [SassTokens], and [SassMixins].
 *
 * These tests verify that the SASS-inspired engine correctly compiles design
 * tokens into valid, iText7-safe CSS strings.  All tests are pure JVM —
 * no Android or Room dependencies required.
 */
class SassStyleEngineTest {

    // ─────────────────────────────────────────────────────────────────────────
    // SassTokens
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `sassprofessional tokens have expected primary colour`() {
        val tokens = SassTokens.sassprofessional()
        assertTrue(
            "Expected deep-navy primary colour",
            tokens.colorPrimary.equals("#0A2540", ignoreCase = true)
        )
    }

    @Test
    fun `sassprofessional tokens have expected accent colour`() {
        val tokens = SassTokens.sassprofessional()
        assertTrue(
            "Expected electric-blue accent colour",
            tokens.colorAccent.equals("#0066FF", ignoreCase = true)
        )
    }

    @Test
    fun `sassLight tokens have different primary to sassprofessional`() {
        val professional = SassTokens.sassprofessional()
        val light = SassTokens.sassLight()
        assertFalse(
            "Light and professional themes should have different primaries",
            professional.colorPrimary == light.colorPrimary
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SassMixins
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `pageSetup mixin contains @page rule`() {
        val css = SassMixins.pageSetup("14mm")
        assertTrue("Should produce @page rule", css.contains("@page"))
        assertTrue("Should include margin value", css.contains("14mm"))
    }

    @Test
    fun `bodyBase mixin contains font-family`() {
        val css = SassMixins.bodyBase(
            family = "Arial, sans-serif",
            size = "10pt",
            color = "#333333",
            lineHeight = "1.8"
        )
        assertTrue("Should include font-family", css.contains("font-family"))
        assertTrue("Should include line-height", css.contains("line-height"))
    }

    @Test
    fun `tableReset mixin contains border-collapse`() {
        val css = SassMixins.tableReset()
        assertTrue("Should include border-collapse", css.contains("border-collapse"))
    }

    @Test
    fun `headerBand mixin includes background-color and accent class`() {
        val css = SassMixins.headerBand("#0A2540", "#0066FF", "4px")
        assertTrue("Should include header-band class", css.contains(".header-band"))
        assertTrue("Should include header-accent class", css.contains(".header-accent"))
        assertTrue("Should include primary bg colour", css.contains("#0A2540"))
    }

    @Test
    fun `totalsRow mixin contains total-row class`() {
        val css = SassMixins.totalsRow("#0A2540", "#FFFFFF", "12pt", "700")
        assertTrue("Should include .total-row", css.contains(".total-row"))
        assertTrue("Should include font-size", css.contains("12pt"))
        assertTrue("Should include font-weight", css.contains("700"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SassStyleEngine — compile()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `compile produces non-empty CSS string`() {
        val css = SassStyleEngine(SassTokens.sassprofessional()).compile()
        assertTrue("Compiled CSS must not be empty", css.isNotBlank())
    }

    @Test
    fun `compile includes all required CSS rules`() {
        val css = SassStyleEngine(SassTokens.sassprofessional()).compile()
        assertTrue("Should include @page rule", css.contains("@page"))
        assertTrue("Should include body rule", css.contains("body"))
        assertTrue("Should include border-collapse", css.contains("border-collapse"))
        assertTrue("Should include .header-band", css.contains(".header-band"))
        assertTrue("Should include .table-header", css.contains(".table-header"))
        assertTrue("Should include .total-row", css.contains(".total-row"))
        assertTrue("Should include utility classes", css.contains(".text-muted"))
        assertTrue("Should include .bg-muted utility class", css.contains(".bg-muted"))
        assertTrue("Should include .text-highlight utility class", css.contains(".text-highlight"))
    }

    @Test
    fun `compile does not produce CSS custom properties (iText7 unsafe)`() {
        val css = SassStyleEngine(SassTokens.sassprofessional()).compile()
        assertFalse(
            "iText7 does not support CSS custom properties (var(--x))",
            css.contains("var(--")
        )
    }

    @Test
    fun `compile does not produce flexbox rules (iText7 unsafe)`() {
        val css = SassStyleEngine(SassTokens.sassprofessional()).compile()
        assertFalse(
            "iText7 does not support flexbox",
            css.contains("display: flex") || css.contains("display:flex")
        )
    }

    @Test
    fun `compile for sassLight produces different primary colour than sassprofessional`() {
        val cssProfessional = SassStyleEngine(SassTokens.sassprofessional()).compile()
        val cssLight = SassStyleEngine(SassTokens.sassLight()).compile()
        assertFalse(
            "Different token sets should produce different CSS",
            cssProfessional == cssLight
        )
    }

    @Test
    fun `compiled CSS embeds token colour values as hex literals`() {
        val tokens = SassTokens.sassprofessional()
        val css = SassStyleEngine(tokens).compile()
        assertTrue(
            "Compiled CSS should contain primary hex colour",
            css.contains(tokens.colorPrimary)
        )
        assertTrue(
            "Compiled CSS should contain accent hex colour",
            css.contains(tokens.colorAccent)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // New design tokens
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `sassprofessional tokens have colorMuted field`() {
        val tokens = SassTokens.sassprofessional()
        assertTrue("colorMuted must not be blank", tokens.colorMuted.isNotBlank())
    }

    @Test
    fun `sassprofessional tokens have colorHighlight field`() {
        val tokens = SassTokens.sassprofessional()
        assertTrue("colorHighlight must not be blank", tokens.colorHighlight.isNotBlank())
    }

    @Test
    fun `sassprofessional tokens have colorAccentBorder field`() {
        val tokens = SassTokens.sassprofessional()
        assertTrue("colorAccentBorder must not be blank", tokens.colorAccentBorder.isNotBlank())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // New SassMixins
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `accentBorder mixin contains accent-border class`() {
        val css = SassMixins.accentBorder("#0066FF", "4px")
        assertTrue("Should include .accent-border class", css.contains(".accent-border"))
        assertTrue("Should include border-left", css.contains("border-left"))
        assertTrue("Should embed provided color", css.contains("#0066FF"))
    }

    @Test
    fun `tableRowAlternating mixin contains row-even and row-odd classes`() {
        val css = SassMixins.tableRowAlternating("#FFFFFF", "#F7F9FC", "#E2E8F0")
        assertTrue("Should include .row-even class", css.contains(".row-even"))
        assertTrue("Should include .row-odd class", css.contains(".row-odd"))
    }

    @Test
    fun `sectionDivider mixin contains section-divider class`() {
        val css = SassMixins.sectionDivider("#E2E8F0", "16px")
        assertTrue("Should include .section-divider class", css.contains(".section-divider"))
        assertTrue("Should include border-top", css.contains("border-top"))
    }

    @Test
    fun `textHighlight mixin contains text-highlight class`() {
        val css = SassMixins.textHighlight("#0066FF", "700")
        assertTrue("Should include .text-highlight class", css.contains(".text-highlight"))
        assertTrue("Should include font-weight", css.contains("700"))
    }

    @Test
    fun `footerBand mixin contains footer-band class`() {
        val css = SassMixins.footerBand("#F1F5F9", "#6B7280", "#0066FF", "4px")
        assertTrue("Should include .footer-band class", css.contains(".footer-band"))
        assertTrue("Should include background-color", css.contains("background-color"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // New utility classes in compile()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `compile includes new utility classes`() {
        val css = SassStyleEngine(SassTokens.sassprofessional()).compile()
        assertTrue("Should include .bg-muted utility", css.contains(".bg-muted"))
        assertTrue("Should include .text-highlight utility", css.contains(".text-highlight"))
        assertTrue("Should include .border-accent utility", css.contains(".border-accent"))
        assertTrue("Should include .section-divider class", css.contains(".section-divider"))
    }

    @Test
    fun `compile includes new mixin output classes`() {
        val css = SassStyleEngine(SassTokens.sassprofessional()).compile()
        assertTrue("Should include .accent-border from accentBorder mixin", css.contains(".accent-border"))
        assertTrue("Should include .row-even from tableRowAlternating mixin", css.contains(".row-even"))
        assertTrue("Should include .footer-band from footerBand mixin", css.contains(".footer-band"))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // generateColorHarmony()
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `generateColorHarmony returns map with required keys`() {
        val harmony = SassStyleEngine.generateColorHarmony("#0A2540")
        val requiredKeys = listOf(
            "primary", "accent", "surface", "muted", "text", "textMuted",
            "border", "totalBg", "totalText", "rowAlt", "highlight", "accentBorder"
        )
        requiredKeys.forEach { key ->
            assertTrue("Missing key '$key' in harmony map", harmony.containsKey(key))
        }
    }

    @Test
    fun `generateColorHarmony preserves primary color`() {
        val harmony = SassStyleEngine.generateColorHarmony("#0A2540")
        assertTrue(
            "Primary should match input (upper-cased)",
            harmony["primary"].equals("#0A2540", ignoreCase = true)
        )
    }

    @Test
    fun `generateColorHarmony handles hex without hash prefix`() {
        val harmony = SassStyleEngine.generateColorHarmony("0A2540")
        assertTrue("Should still return a valid primary", harmony.containsKey("primary"))
        assertTrue("Primary should not be blank", harmony["primary"]!!.isNotBlank())
    }

    @Test
    fun `generateColorHarmony does not produce CSS custom properties`() {
        val harmony = SassStyleEngine.generateColorHarmony("#6B4C9A")
        harmony.values.forEach { value ->
            assertFalse(
                "Harmony values must be plain hex, not CSS variables: $value",
                value.contains("var(--")
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase 2: New SassMixins (signatureLine, authorizationSection,
    //          paymentMethodBadge, sectionCard)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `signatureLine mixin contains signature-line class`() {
        val css = SassMixins.signatureLine("#00C9A7")
        assertTrue("Should include .signature-line class", css.contains(".signature-line"))
        assertTrue("Should include border-bottom", css.contains("border-bottom"))
        assertTrue("Should embed provided color", css.contains("#00C9A7"))
    }

    @Test
    fun `signatureLine mixin contains signature-label class`() {
        val css = SassMixins.signatureLine("#00C9A7")
        assertTrue("Should include .signature-label class", css.contains(".signature-label"))
    }

    @Test
    fun `authorizationSection mixin contains auth-section class`() {
        val css = SassMixins.authorizationSection("#DEE2E6", "#FFFFFF")
        assertTrue("Should include .auth-section class", css.contains(".auth-section"))
        assertTrue("Should include border-top", css.contains("border-top"))
    }

    @Test
    fun `paymentMethodBadge mixin contains payment-badge class`() {
        val css = SassMixins.paymentMethodBadge("#0066FF", "#EFF6FF")
        assertTrue("Should include .payment-badge class", css.contains(".payment-badge"))
        assertTrue("Should include background-color", css.contains("background-color"))
        assertTrue("Should embed accent color", css.contains("#0066FF"))
    }

    @Test
    fun `sectionCard mixin contains section-card class`() {
        val css = SassMixins.sectionCard("#F1F5F9", "#0066FF", "4px")
        assertTrue("Should include .section-card class", css.contains(".section-card"))
        assertTrue("Should include border-left", css.contains("border-left"))
        assertTrue("Should embed provided color", css.contains("#0066FF"))
    }

    @Test
    fun `compile includes Phase 2 signature and payment mixin output`() {
        val css = SassStyleEngine(SassTokens.sassprofessional()).compile()
        assertTrue("Should include .signature-line from signatureLine mixin", css.contains(".signature-line"))
        assertTrue("Should include .auth-section from authorizationSection mixin", css.contains(".auth-section"))
        assertTrue("Should include .payment-badge from paymentMethodBadge mixin", css.contains(".payment-badge"))
        assertTrue("Should include .section-card from sectionCard mixin", css.contains(".section-card"))
    }

    @Test
    fun `compile Phase 2 output remains iText7-safe (no flexbox or CSS vars)`() {
        val css = SassStyleEngine(SassTokens.sassprofessional()).compile()
        assertFalse("iText7 does not support CSS variables", css.contains("var(--"))
        assertFalse("iText7 does not support flexbox", css.contains("display: flex") || css.contains("display:flex"))
    }
}



