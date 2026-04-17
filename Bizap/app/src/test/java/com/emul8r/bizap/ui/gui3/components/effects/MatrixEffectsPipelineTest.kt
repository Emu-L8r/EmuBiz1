package com.emul8r.bizap.ui.gui3.components.effects

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.ui.gui3.util.MatrixBackgroundConfig
import com.emul8r.bizap.ui.gui3.util.MatrixEffect
import com.emul8r.bizap.ui.gui3.util.PerformanceProfiler
import com.emul8r.bizap.utils.FirebaseEventTracker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [MatrixEffectsPipeline].
 * Verifies rendering orchestration, error isolation, and profiler integration.
 */
class MatrixEffectsPipelineTest : BaseUnitTest() {

    private val effectRegistry: EffectRegistry = mockk()
    private val eventTracker: FirebaseEventTracker = mockk(relaxed = true)
    private val profiler: PerformanceProfiler = mockk(relaxed = true)

    private lateinit var pipeline: MatrixEffectsPipeline

    private val config = MatrixBackgroundConfig()

    @Before
    fun setUp() {
        every { profiler.recordFrame(any()) } returns Unit
        every { profiler.snapshot() } returns mockk(relaxed = true)
        pipeline = MatrixEffectsPipeline(effectRegistry, eventTracker, profiler)
    }

    // ── renderFrame — happy path ───────────────────────────────────────────

    @Test
    fun `renderFrame calls profiler recordFrame`() = runUnitTest {
        every { effectRegistry.getActiveEffects(any(), any()) } returns emptyList()
        val scope = mockk<androidx.compose.ui.graphics.drawscope.DrawScope>(relaxed = true)
        pipeline.renderFrame(scope, config)
        advanceUntilIdle()
        verify { profiler.recordFrame(any()) }
    }

    @Test
    fun `renderFrame renders all active effects`() = runUnitTest {
        val effect1 = mockk<MatrixEffect>(relaxed = true)
        val effect2 = mockk<MatrixEffect>(relaxed = true)
        every { effect1.effectName } returns "effect_1"
        every { effect2.effectName } returns "effect_2"
        every { effectRegistry.getActiveEffects(any(), any()) } returns listOf(effect1, effect2)

        val scope = mockk<androidx.compose.ui.graphics.drawscope.DrawScope>(relaxed = true)
        pipeline.renderFrame(scope, config)
        advanceUntilIdle()

        coVerify { effect1.render(scope, config) }
        coVerify { effect2.render(scope, config) }
    }

    @Test
    fun `renderFrame continues when one effect throws`() = runUnitTest {
        val failingEffect = mockk<MatrixEffect>(relaxed = true)
        val goodEffect = mockk<MatrixEffect>(relaxed = true)
        every { failingEffect.effectName } returns "failing_effect"
        every { goodEffect.effectName } returns "good_effect"
        coEvery { failingEffect.render(any(), any()) } throws RuntimeException("Render failed")

        every { effectRegistry.getActiveEffects(any(), any()) } returns listOf(failingEffect, goodEffect)

        val scope = mockk<androidx.compose.ui.graphics.drawscope.DrawScope>(relaxed = true)
        // Should not throw
        pipeline.renderFrame(scope, config)
        advanceUntilIdle()

        // Good effect should still be called despite the first one failing
        coVerify { goodEffect.render(scope, config) }
    }

    @Test
    fun `renderFrame with empty effects list still records frame time`() = runUnitTest {
        every { effectRegistry.getActiveEffects(any(), any()) } returns emptyList()
        val scope = mockk<androidx.compose.ui.graphics.drawscope.DrawScope>(relaxed = true)
        pipeline.renderFrame(scope, config)
        advanceUntilIdle()
        verify { profiler.recordFrame(any()) }
    }

    // ── getMetrics ─────────────────────────────────────────────────────────

    @Test
    fun `getMetrics delegates to profiler snapshot`() {
        val result = pipeline.getMetrics()
        verify { profiler.snapshot() }
    }

    // ── resetProfiler ──────────────────────────────────────────────────────

    @Test
    fun `resetProfiler calls profiler reset`() {
        every { profiler.reset() } returns Unit
        pipeline.resetProfiler()
        verify { profiler.reset() }
    }

    // ── glitch / scanline flags ────────────────────────────────────────────

    @Test
    fun `renderFrame passes enableGlitch=false to registry`() = runUnitTest {
        every { effectRegistry.getActiveEffects(enableGlitch = false, enableScanlines = true) } returns emptyList()
        val scope = mockk<androidx.compose.ui.graphics.drawscope.DrawScope>(relaxed = true)
        pipeline.renderFrame(scope, config, enableGlitch = false, enableScanlines = true)
        advanceUntilIdle()
        verify { effectRegistry.getActiveEffects(enableGlitch = false, enableScanlines = true) }
    }

    @Test
    fun `renderFrame passes enableScanlines=false to registry`() = runUnitTest {
        every { effectRegistry.getActiveEffects(enableGlitch = true, enableScanlines = false) } returns emptyList()
        val scope = mockk<androidx.compose.ui.graphics.drawscope.DrawScope>(relaxed = true)
        pipeline.renderFrame(scope, config, enableGlitch = true, enableScanlines = false)
        advanceUntilIdle()
        verify { effectRegistry.getActiveEffects(enableGlitch = true, enableScanlines = false) }
    }
}

