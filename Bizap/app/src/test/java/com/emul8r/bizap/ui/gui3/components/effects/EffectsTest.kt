package com.emul8r.bizap.ui.gui3.components.effects

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.ui.gui3.util.GlitchEffect
import com.emul8r.bizap.ui.gui3.util.MatrixBackgroundConfig
import com.emul8r.bizap.ui.gui3.util.RainParticleEffect
import com.emul8r.bizap.ui.gui3.util.ScanlineEffect
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for Matrix effects: [RainParticleEffect], [GlitchEffect], [ScanlineEffect].
 * Verifies effectName, isAvailable, and estimatedFrameTimeMicros contract.
 */
class EffectsTest : BaseUnitTest() {

    // ── RainParticleEffect ─────────────────────────────────────────────────

    private val rainEffect = RainParticleEffect()
    private val glitchEffect = GlitchEffect()
    private val scanlineEffect = ScanlineEffect()

    @Test
    fun `RainParticleEffect effectName is rain_particles`() {
        assertEquals("rain_particles", rainEffect.effectName)
    }

    @Test
    fun `RainParticleEffect isAvailable returns true`() {
        assertTrue(rainEffect.isAvailable())
    }

    @Test
    fun `RainParticleEffect estimatedFrameTimeMicros is within budget`() {
        // Must be < 10ms (10_000 micros) to meet performance budget
        assertTrue(rainEffect.estimatedFrameTimeMicros() < 10_000L)
    }

    @Test
    fun `RainParticleEffect estimatedFrameTimeMicros is positive`() {
        assertTrue(rainEffect.estimatedFrameTimeMicros() > 0L)
    }

    // ── GlitchEffect ───────────────────────────────────────────────────────

    @Test
    fun `GlitchEffect effectName is glitch`() {
        assertEquals("glitch", glitchEffect.effectName)
    }

    @Test
    fun `GlitchEffect isAvailable returns true`() {
        assertTrue(glitchEffect.isAvailable())
    }

    @Test
    fun `GlitchEffect estimatedFrameTimeMicros is within budget`() {
        // Glitch is cheap — must be < 3ms
        assertTrue(glitchEffect.estimatedFrameTimeMicros() < 3_000L)
    }

    @Test
    fun `GlitchEffect estimatedFrameTimeMicros is positive`() {
        assertTrue(glitchEffect.estimatedFrameTimeMicros() > 0L)
    }

    // ── ScanlineEffect ─────────────────────────────────────────────────────

    @Test
    fun `ScanlineEffect effectName is scanlines`() {
        assertEquals("scanlines", scanlineEffect.effectName)
    }

    @Test
    fun `ScanlineEffect isAvailable returns true`() {
        assertTrue(scanlineEffect.isAvailable())
    }

    @Test
    fun `ScanlineEffect estimatedFrameTimeMicros is within budget`() {
        // Scanlines are cheap — must be < 3ms
        assertTrue(scanlineEffect.estimatedFrameTimeMicros() < 3_000L)
    }

    @Test
    fun `ScanlineEffect estimatedFrameTimeMicros is positive`() {
        assertTrue(scanlineEffect.estimatedFrameTimeMicros() > 0L)
    }

    // ── MatrixBackgroundConfig defaults ───────────────────────────────────

    @Test
    fun `MatrixBackgroundConfig default rainDensity is 0_8f`() {
        val config = MatrixBackgroundConfig()
        assertEquals(0.8f, config.rainDensity)
    }

    @Test
    fun `MatrixBackgroundConfig default glitchIntensity is 0_5f`() {
        val config = MatrixBackgroundConfig()
        assertEquals(0.5f, config.glitchIntensity)
    }

    @Test
    fun `MatrixBackgroundConfig default scanlineAlpha is 0_05f`() {
        val config = MatrixBackgroundConfig()
        assertEquals(0.05f, config.scanlineAlpha)
    }

    @Test
    fun `MatrixBackgroundConfig canvasEnabled defaults to true`() {
        assertTrue(MatrixBackgroundConfig().canvasEnabled)
    }

    @Test
    fun `MatrixBackgroundConfig debugLogging defaults to false`() {
        assertFalse(MatrixBackgroundConfig().debugLogging)
    }

    @Test
    fun `MatrixBackgroundConfig enableAdaptivePerf defaults to false`() {
        assertFalse(MatrixBackgroundConfig().enableAdaptivePerf)
    }

    // ── all effects distinct ───────────────────────────────────────────────

    @Test
    fun `all effect names are unique`() {
        val names = listOf(
            rainEffect.effectName,
            glitchEffect.effectName,
            scanlineEffect.effectName
        )
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `all effects are available by default`() {
        listOf(rainEffect, glitchEffect, scanlineEffect).forEach {
            assertTrue(it.isAvailable(), "${it.effectName} should be available")
        }
    }
}

