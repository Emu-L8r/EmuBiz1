package com.emul8r.bizap.util.feature

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.config.FeatureFlag
import com.emul8r.bizap.data.config.FeatureFlagManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [FeatureFlag] enum defaults and [FeatureFlagManager] interface contract.
 */
class FeatureFlagsTest : BaseUnitTest() {

    private val manager: FeatureFlagManager = mockk()

    // ── FeatureFlag defaults ────────────────────────────────────────────────

    @Test
    fun `EFFECT_RAIN default is true`() {
        assertTrue(FeatureFlag.EFFECT_RAIN.defaultValue)
    }

    @Test
    fun `EFFECT_GLITCH default is true`() {
        assertTrue(FeatureFlag.EFFECT_GLITCH.defaultValue)
    }

    @Test
    fun `EFFECT_SCANLINES default is true`() {
        assertTrue(FeatureFlag.EFFECT_SCANLINES.defaultValue)
    }

    @Test
    fun `MATRIX_CANVAS_RENDERER default is false`() {
        assertFalse(FeatureFlag.MATRIX_CANVAS_RENDERER.defaultValue)
    }

    @Test
    fun `MATRIX_DEBUG_PANEL default is false`() {
        assertFalse(FeatureFlag.MATRIX_DEBUG_PANEL.defaultValue)
    }

    @Test
    fun `MATRIX_ADAPTIVE_PERF default is false`() {
        assertFalse(FeatureFlag.MATRIX_ADAPTIVE_PERF.defaultValue)
    }

    @Test
    fun `RECURRING_INVOICES default is false`() {
        assertFalse(FeatureFlag.RECURRING_INVOICES.defaultValue)
    }

    @Test
    fun `all flags have non-blank keys`() {
        FeatureFlag.entries.forEach { flag ->
            assertTrue(flag.key.isNotBlank(), "Flag ${flag.name} has blank key")
        }
    }

    @Test
    fun `all flag keys are unique`() {
        val keys = FeatureFlag.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size, "Duplicate keys found in FeatureFlag enum")
    }

    // ── FeatureFlagManager mock contract ───────────────────────────────────

    @Test
    fun `isEnabled returns flag state from manager`() = runUnitTest {
        coEvery { manager.isEnabled(FeatureFlag.EFFECT_RAIN) } returns true
        val result = manager.isEnabled(FeatureFlag.EFFECT_RAIN)
        assertTrue(result)
        coVerify { manager.isEnabled(FeatureFlag.EFFECT_RAIN) }
    }

    @Test
    fun `isEnabled returns false when flag is disabled`() = runUnitTest {
        coEvery { manager.isEnabled(FeatureFlag.MATRIX_CANVAS_RENDERER) } returns false
        val result = manager.isEnabled(FeatureFlag.MATRIX_CANVAS_RENDERER)
        assertFalse(result)
    }

    @Test
    fun `isEnabledForUser returns flag state for specific user`() = runUnitTest {
        coEvery { manager.isEnabledForUser(FeatureFlag.MATRIX_CANVAS_RENDERER, 42L) } returns true
        val result = manager.isEnabledForUser(FeatureFlag.MATRIX_CANVAS_RENDERER, 42L)
        assertTrue(result)
        coVerify { manager.isEnabledForUser(FeatureFlag.MATRIX_CANVAS_RENDERER, 42L) }
    }

    @Test
    fun `setEnabled is called with correct parameters`() = runUnitTest {
        coEvery { manager.setEnabled(FeatureFlag.MATRIX_DEBUG_PANEL, true) } returns Unit
        manager.setEnabled(FeatureFlag.MATRIX_DEBUG_PANEL, true)
        coVerify { manager.setEnabled(FeatureFlag.MATRIX_DEBUG_PANEL, true) }
    }

    @Test
    fun `observeFlag emits current flag value`() = runUnitTest {
        coEvery { manager.observeFlag(FeatureFlag.EFFECT_RAIN) } returns flowOf(true)
        val result = manager.observeFlag(FeatureFlag.EFFECT_RAIN).first()
        assertTrue(result)
    }

    @Test
    fun `observeFlag emits false when flag disabled`() = runUnitTest {
        coEvery { manager.observeFlag(FeatureFlag.MATRIX_CANVAS_RENDERER) } returns flowOf(false)
        val result = manager.observeFlag(FeatureFlag.MATRIX_CANVAS_RENDERER).first()
        assertFalse(result)
    }

    // ── Flag key naming convention ─────────────────────────────────────────

    @Test
    fun `all flag keys use lowercase underscore format`() {
        FeatureFlag.entries.forEach { flag ->
            assertTrue(
                flag.key.matches(Regex("[a-z][a-z0-9_]*")),
                "Flag ${flag.name} key '${flag.key}' does not follow lowercase_underscore convention"
            )
        }
    }
}

