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
}
