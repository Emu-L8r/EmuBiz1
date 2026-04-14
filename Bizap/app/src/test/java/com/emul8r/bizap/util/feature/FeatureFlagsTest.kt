package com.emul8r.bizap.util.feature

import android.content.SharedPreferences
import com.emul8r.bizap.BaseUnitTest
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for FeatureFlags implementation.
 * Tests state management, persistence, and reactive updates.
 */
class FeatureFlagsTest : BaseUnitTest() {

    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var flags: FeatureFlagsImpl

    @Before
    fun setup() {
        mockEditor = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)

        every { mockPrefs.edit(any<SharedPreferences.Editor.() -> Unit>()) } returns mockEditor
        every { mockPrefs.getBoolean(any(), any()) } returns false

        flags = FeatureFlagsImpl(mockPrefs)
    }

    @Test
    fun testCanvasRendererDefaultDisabled() = runUnitTest {
        every { mockPrefs.getBoolean("matrix_canvas_renderer_enabled", false) } returns false
        val newFlags = FeatureFlagsImpl(mockPrefs)

        val enabled = newFlags.matrixCanvasRendererEnabled.first()
        assertFalse(enabled, "Canvas renderer should default to disabled")
    }

    @Test
    fun testCanvasRendererCanBeEnabled() = runUnitTest {
        every { mockPrefs.getBoolean("matrix_canvas_renderer_enabled", false) } returns true
        val newFlags = FeatureFlagsImpl(mockPrefs)

        val enabled = newFlags.matrixCanvasRendererEnabled.first()
        assertTrue(enabled, "Canvas renderer should be enabled")
    }

    @Test
    fun testEffectRainDefaultEnabled() = runUnitTest {
        every { mockPrefs.getBoolean("effect_rain_enabled", true) } returns true
        val newFlags = FeatureFlagsImpl(mockPrefs)

        val enabled = newFlags.effectRainEnabled.first()
        assertTrue(enabled, "Rain effect should default to enabled")
    }

    @Test
    fun testEffectGlitchDefaultEnabled() = runUnitTest {
        every { mockPrefs.getBoolean("effect_glitch_enabled", true) } returns true
        val newFlags = FeatureFlagsImpl(mockPrefs)

        val enabled = newFlags.effectGlitchEnabled.first()
        assertTrue(enabled, "Glitch effect should default to enabled")
    }

    @Test
    fun testEffectScanlinesDefaultEnabled() = runUnitTest {
        every { mockPrefs.getBoolean("effect_scanlines_enabled", true) } returns true
        val newFlags = FeatureFlagsImpl(mockPrefs)

        val enabled = newFlags.effectScanlinesEnabled.first()
        assertTrue(enabled, "Scanline effect should default to enabled")
    }

    @Test
    fun testAdaptivePerfDefaultDisabled() = runUnitTest {
        every { mockPrefs.getBoolean("adaptive_perf_enabled", false) } returns false
        val newFlags = FeatureFlagsImpl(mockPrefs)

        val enabled = newFlags.adaptivePerfEnabled.first()
        assertFalse(enabled, "Adaptive perf should default to disabled")
    }

    @Test
    fun testSetCanvasRendererEnabled() = runUnitTest {
        flags.setMatrixCanvasRendererEnabled(true)

        // Verify value was written to prefs
        verify(atLeast = 1) { mockEditor.putBoolean(any(), any()) }
    }

    @Test
    fun testSetEffectRainEnabled() = runUnitTest {
        flags.setEffectRainEnabled(false)

        // Verify value was written to prefs
        verify(atLeast = 1) { mockEditor.putBoolean(any(), any()) }
    }

    @Test
    fun testSetEffectGlitchEnabled() = runUnitTest {
        flags.setEffectGlitchEnabled(false)

        // Verify value was written to prefs
        verify(atLeast = 1) { mockEditor.putBoolean(any(), any()) }
    }

    @Test
    fun testSetEffectScanlinesEnabled() = runUnitTest {
        flags.setEffectScanlinesEnabled(false)

        // Verify value was written to prefs
        verify(atLeast = 1) { mockEditor.putBoolean(any(), any()) }
    }

    @Test
    fun testSetAdaptivePerfEnabled() = runUnitTest {
        flags.setAdaptivePerfEnabled(true)

        // Verify value was written to prefs
        verify(atLeast = 1) { mockEditor.putBoolean(any(), any()) }
    }

    @Test
    fun testResetToDefaults() = runUnitTest {
        flags.resetToDefaults()

        // Verify preferences were cleared
        verify(atLeast = 1) { mockEditor.clear() }
    }
}

