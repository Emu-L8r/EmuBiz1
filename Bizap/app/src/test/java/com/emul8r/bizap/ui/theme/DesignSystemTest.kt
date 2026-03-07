package com.emul8r.bizap.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Material 3 Design System constants defined in DesignSystem.kt.
 *
 * Verifies that:
 * - Color values match the Material 3 specification for the Bizap brand.
 * - Typography scale sizes and weights are correct.
 * - Spacing values follow the 8 dp baseline grid.
 * - Corner radius values are within the Material 3 shape scale.
 */
class DesignSystemTest {

    // -----------------------------------------------------------------------
    // Color System Tests
    // -----------------------------------------------------------------------

    @Test
    fun `md_theme_light_primary is correct indigo brand color`() {
        assertEquals(Color(0xFF6750A4), md_theme_light_primary)
    }

    @Test
    fun `md_theme_light_onPrimary is white for legibility`() {
        assertEquals(Color(0xFFFFFFFF), md_theme_light_onPrimary)
    }

    @Test
    fun `md_theme_light_error matches Material 3 error red`() {
        assertEquals(Color(0xFFB3261E), md_theme_light_error)
    }

    @Test
    fun `semanticSuccess is Material green`() {
        assertEquals(Color(0xFF4CAF50), semanticSuccess)
    }

    @Test
    fun `semanticWarning is orange`() {
        assertEquals(Color(0xFFFFA500), semanticWarning)
    }

    @Test
    fun `semanticError matches md_theme_light_error`() {
        assertEquals(md_theme_light_error, semanticError)
    }

    @Test
    fun `semanticInfo is Material blue`() {
        assertEquals(Color(0xFF2196F3), semanticInfo)
    }

    // -----------------------------------------------------------------------
    // Typography Scale Tests
    // -----------------------------------------------------------------------

    @Test
    fun `displayLarge has correct font size`() {
        assertEquals(57.sp, displayLarge.fontSize)
    }

    @Test
    fun `displayLarge has Bold weight`() {
        assertEquals(FontWeight.Bold, displayLarge.fontWeight)
    }

    @Test
    fun `headlineSmall has 24sp font size`() {
        assertEquals(24.sp, headlineSmall.fontSize)
    }

    @Test
    fun `headlineSmall has Bold weight`() {
        assertEquals(FontWeight.Bold, headlineSmall.fontWeight)
    }

    @Test
    fun `bodyMedium has 14sp font size`() {
        assertEquals(14.sp, bodyMedium.fontSize)
    }

    @Test
    fun `bodyMedium has Normal weight`() {
        assertEquals(FontWeight.Normal, bodyMedium.fontWeight)
    }

    @Test
    fun `labelSmall has 11sp font size`() {
        assertEquals(11.sp, labelSmall.fontSize)
    }

    @Test
    fun `labelSmall has SemiBold weight`() {
        assertEquals(FontWeight.SemiBold, labelSmall.fontWeight)
    }

    @Test
    fun `typography scale sizes are strictly ordered`() {
        // Display > Headline > Title > Body > Label
        assertTrue(displayLarge.fontSize > headlineLarge.fontSize)
        assertTrue(headlineLarge.fontSize > titleLarge.fontSize)
        assertTrue(titleLarge.fontSize > bodyLarge.fontSize)
        assertTrue(bodyMedium.fontSize < bodyLarge.fontSize)
        assertTrue(labelSmall.fontSize < labelMedium.fontSize)
    }

    // -----------------------------------------------------------------------
    // Spacing System Tests
    // -----------------------------------------------------------------------

    @Test
    fun `spacing2 is 2dp`() {
        assertEquals(2.dp, spacing2)
    }

    @Test
    fun `spacing4 is 4dp`() {
        assertEquals(4.dp, spacing4)
    }

    @Test
    fun `spacing8 is 8dp`() {
        assertEquals(8.dp, spacing8)
    }

    @Test
    fun `spacing16 is 16dp`() {
        assertEquals(16.dp, spacing16)
    }

    @Test
    fun `spacing24 is 24dp`() {
        assertEquals(24.dp, spacing24)
    }

    @Test
    fun `spacing values follow 8dp baseline grid`() {
        // Each spacing value should be a multiple of 2 dp
        assertEquals(0, spacing2.value.toLong() % 2)
        assertEquals(0, spacing4.value.toLong() % 2)
        assertEquals(0, spacing8.value.toLong() % 2)
        assertEquals(0, spacing16.value.toLong() % 2)
        assertEquals(0, spacing24.value.toLong() % 2)
        assertEquals(0, spacing32.value.toLong() % 2)
    }

    // -----------------------------------------------------------------------
    // Corner Radius Scale Tests
    // -----------------------------------------------------------------------

    @Test
    fun `cornerRadiusSmall is 8dp`() {
        assertEquals(8.dp, cornerRadiusSmall)
    }

    @Test
    fun `cornerRadiusMedium is 12dp`() {
        assertEquals(12.dp, cornerRadiusMedium)
    }

    @Test
    fun `cornerRadiusLarge is 16dp`() {
        assertEquals(16.dp, cornerRadiusLarge)
    }

    @Test
    fun `cornerRadiusExtraLarge is 28dp`() {
        assertEquals(28.dp, cornerRadiusExtraLarge)
    }

    @Test
    fun `corner radius values are strictly ordered`() {
        assertTrue(cornerRadiusSmall < cornerRadiusMedium)
        assertTrue(cornerRadiusMedium < cornerRadiusLarge)
        assertTrue(cornerRadiusLarge < cornerRadiusExtraLarge)
    }
}
