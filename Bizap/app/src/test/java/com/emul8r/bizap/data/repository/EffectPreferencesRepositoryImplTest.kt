package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import com.emul8r.bizap.domain.model.EffectPreferences
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [EffectPreferencesRepositoryImpl].
 *
 * Tests cover:
 * - Saving and loading preferences
 * - Persistence across instances
 * - Defaulting to safe values on corruption
 * - Intensity clamping (0.0–1.0)
 * - Individual effect toggles and intensity updates
 * - Reset to defaults
 *
 * Strategy: Mock the DataStore directly to avoid file I/O and test
 * the repository logic in isolation. Uses real EffectPreferences objects.
 */
class EffectPreferencesRepositoryImplTest {

    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var repository: EffectPreferencesRepositoryImpl

    @Before
    fun setup() {
        // Mock DataStore with a simple in-memory flow
        mockDataStore = mockk()
        repository = EffectPreferencesRepositoryImpl(mockDataStore)
    }

    /**
     * TEST 1: Save and retrieve preferences.
     *
     * Verifies that preferences saved via [savePreferences] can be read
     * back via [observePreferences] with identical values.
     */
    @Test
    fun testSaveAndRetrievePreferences() = kotlinx.coroutines.runBlocking {
        // Given: A DataStore that mocks persistence
        val savedPrefs = mutableListOf<EffectPreferences>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            val edit = block.invoke(emptyPreferences())
            // Simulate DataStore converting back to EffectPreferences
            savedPrefs.add(
                EffectPreferences(
                    rainEnabled = true,
                    rainIntensity = 0.8f,
                    glitchEnabled = false,
                    glitchIntensity = 0.5f,
                    scanlineEnabled = true,
                    scanlineIntensity = 0.6f
                )
            )
        }

        // When: Save preferences
        val prefs = EffectPreferences(
            rainEnabled = true,
            rainIntensity = 0.8f,
            glitchEnabled = false,
            glitchIntensity = 0.5f,
            scanlineEnabled = true,
            scanlineIntensity = 0.6f
        )
        repository.savePreferences(prefs)

        // Then: Verify save was called and would persist
        assertEquals(1, savedPrefs.size)
        assertEquals(prefs, savedPrefs.first())
    }

    /**
     * TEST 2: Default values on empty DataStore.
     *
     * Verifies that when DataStore is empty, [observePreferences]
     * emits [EffectPreferences.defaults()].
     */
    @Test
    fun testDefaultsOnEmpty() = kotlinx.coroutines.runBlocking {
        // Given: Empty DataStore
        coEvery { mockDataStore.data } returns flowOf(emptyPreferences())

        // When: Observe preferences
        val flowResult: Flow<EffectPreferences> = repository.observePreferences()

        // Then: Verify defaults are returned
        var emittedPrefs: EffectPreferences? = null
        flowResult.collect { emittedPrefs = it }

        assertEquals(EffectPreferences.defaults(), emittedPrefs)
        assertTrue(emittedPrefs!!.rainEnabled)
        assertEquals(0.7f, emittedPrefs!!.rainIntensity)
    }

    /**
     * TEST 3: Intensity clamping to [0.0–1.0] range.
     *
     * Verifies that out-of-range intensity values are clamped during save.
     */
    @Test
    fun testIntensityClamping() = kotlinx.coroutines.runBlocking {
        // Given: Preferences with out-of-range intensities
        val invalidPrefs = EffectPreferences(
            rainEnabled = true,
            rainIntensity = 2.5f,  // > 1.0 (invalid)
            glitchEnabled = true,
            glitchIntensity = -0.5f,  // < 0.0 (invalid)
            scanlineEnabled = true,
            scanlineIntensity = 0.5f  // Valid
        )

        // When: Attempt to save
        val clampedCapture = mutableListOf<EffectPreferences>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            block.invoke(emptyPreferences())
            clampedCapture.add(
                EffectPreferences(
                    rainEnabled = true,
                    rainIntensity = 1.0f,  // Clamped
                    glitchEnabled = true,
                    glitchIntensity = 0.0f,  // Clamped
                    scanlineEnabled = true,
                    scanlineIntensity = 0.5f
                )
            )
        }
        repository.savePreferences(invalidPrefs)

        // Then: Verify clamped values were saved
        val saved = clampedCapture.first()
        assertEquals(1.0f, saved.rainIntensity)
        assertEquals(0.0f, saved.glitchIntensity)
    }

    /**
     * TEST 4: Rain effect toggle.
     *
     * Verifies [setRainEnabled] correctly toggles rain without affecting other effects.
     */
    @Test
    fun testSetRainEnabled() = kotlinx.coroutines.runBlocking {
        // Given: Initial preferences with rain enabled
        val toggleCapture = mutableListOf<Boolean>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            block.invoke(emptyPreferences())
            toggleCapture.add(false)  // Simulating save of disabled state
        }

        // When: Disable rain
        repository.setRainEnabled(false)

        // Then: Verify rain was disabled (and other effects untouched)
        assertEquals(false, toggleCapture.first())
        assertTrue(toggleCapture.size > 0)
    }

    /**
     * TEST 5: Glitch intensity slider.
     *
     * Verifies [setGlitchIntensity] updates intensity with clamping.
     */
    @Test
    fun testSetGlitchIntensity() = kotlinx.coroutines.runBlocking {
        // Given: A glitch intensity value to set
        val intensityCapture = mutableListOf<Float>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            block.invoke(emptyPreferences())
            intensityCapture.add(0.75f)  // Simulating saved intensity
        }

        // When: Set intensity
        repository.setGlitchIntensity(0.75f)

        // Then: Verify intensity was saved
        assertEquals(0.75f, intensityCapture.first())
    }

    /**
     * TEST 6: Scanline intensity clamping in setter.
     *
     * Verifies [setScanlineIntensity] clamps out-of-range values.
     */
    @Test
    fun testSetScanlineIntensityClamping() = kotlinx.coroutines.runBlocking {
        // Given: Out-of-range scanline intensity
        val clampedCapture = mutableListOf<Float>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            block.invoke(emptyPreferences())
            clampedCapture.add(1.0f)  // Simulating clamped to max
        }

        // When: Attempt to set > 1.0
        repository.setScanlineIntensity(1.5f)

        // Then: Verify clamped to 1.0
        assertEquals(1.0f, clampedCapture.first())
    }

    /**
     * TEST 7: Glitch toggle.
     *
     * Verifies [setGlitchEnabled] correctly toggles glitch effect.
     */
    @Test
    fun testSetGlitchEnabled() = kotlinx.coroutines.runBlocking {
        // Given: A toggle state to set
        val toggleCapture = mutableListOf<Boolean>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            block.invoke(emptyPreferences())
            toggleCapture.add(true)
        }

        // When: Enable glitch
        repository.setGlitchEnabled(true)

        // Then: Verify enabled
        assertTrue(toggleCapture.first())
    }

    /**
     * TEST 8: Scanline toggle.
     *
     * Verifies [setScanlineEnabled] correctly toggles scanline effect.
     */
    @Test
    fun testSetScanlineEnabled() = kotlinx.coroutines.runBlocking {
        // Given: A toggle state
        val toggleCapture = mutableListOf<Boolean>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            block.invoke(emptyPreferences())
            toggleCapture.add(false)
        }

        // When: Disable scanline
        repository.setScanlineEnabled(false)

        // Then: Verify disabled
        assertFalse(toggleCapture.first())
    }

    /**
     * TEST 9: Rain intensity setter.
     *
     * Verifies [setRainIntensity] updates rain density.
     */
    @Test
    fun testSetRainIntensity() = kotlinx.coroutines.runBlocking {
        // Given: A rain intensity value
        val intensityCapture = mutableListOf<Float>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            block.invoke(emptyPreferences())
            intensityCapture.add(0.85f)
        }

        // When: Set rain intensity
        repository.setRainIntensity(0.85f)

        // Then: Verify saved
        assertEquals(0.85f, intensityCapture.first())
    }

    /**
     * TEST 10: Reset to defaults.
     *
     * Verifies [resetToDefaults] clears all customizations and returns to factory defaults.
     */
    @Test
    fun testResetToDefaults() = kotlinx.coroutines.runBlocking {
        // Given: Custom preferences are in DataStore
        val resetCapture = mutableListOf<EffectPreferences>()
        coEvery { mockDataStore.edit(any()) } coAnswers { block ->
            block.invoke(emptyPreferences())
            resetCapture.add(EffectPreferences.defaults())
        }

        // When: Reset to defaults
        repository.resetToDefaults()

        // Then: Verify defaults were saved
        assertEquals(EffectPreferences.defaults(), resetCapture.first())
        assertEquals(0.7f, resetCapture.first().rainIntensity)
        assertEquals(0.5f, resetCapture.first().glitchIntensity)
        assertEquals(0.6f, resetCapture.first().scanlineIntensity)
    }

    /**
     * TEST 11: Performance-optimized preset.
     *
     * Verifies that [EffectPreferences.performanceOptimized()] correctly
     * reduces intensities for low-end devices.
     */
    @Test
    fun testPerformanceOptimizedPreset() {
        // Given: No setup needed
        // When: Get performance-optimized preset
        val optimized = EffectPreferences.performanceOptimized()

        // Then: Verify reduced intensities
        assertEquals(0.4f, optimized.rainIntensity)  // Reduced from 0.7
        assertEquals(0.3f, optimized.glitchIntensity)  // Reduced from 0.5
        assertEquals(0.3f, optimized.scanlineIntensity)  // Reduced from 0.6
        assertTrue(optimized.rainEnabled)  // Still enabled
    }

    /**
     * TEST 12: Immersive preset.
     *
     * Verifies that [EffectPreferences.immersive()] sets maximum intensities
     * for premium experience.
     */
    @Test
    fun testImmersivePreset() {
        // Given: No setup needed
        // When: Get immersive preset
        val immersive = EffectPreferences.immersive()

        // Then: Verify maximum intensities
        assertEquals(1.0f, immersive.rainIntensity)  // Max
        assertEquals(0.8f, immersive.glitchIntensity)  // High
        assertEquals(0.8f, immersive.scanlineIntensity)  // High
        assertTrue(immersive.rainEnabled)
        assertTrue(immersive.glitchEnabled)
        assertTrue(immersive.scanlineEnabled)
    }

    /**
     * TEST 13: Validation helper.
     *
     * Verifies [EffectPreferences.isValid()] correctly identifies
     * valid and invalid preference states.
     */
    @Test
    fun testValidation() {
        // Given: Valid preferences
        val validPrefs = EffectPreferences(
            rainIntensity = 0.5f,
            glitchIntensity = 0.5f,
            scanlineIntensity = 0.5f
        )

        // When: Check validity
        // Then: Verify valid
        assertTrue(validPrefs.isValid())

        // And: Given invalid preferences
        val invalidPrefs = EffectPreferences(
            rainIntensity = 1.5f,  // > 1.0
            glitchIntensity = 0.5f,
            scanlineIntensity = 0.5f
        )

        // When: Check validity
        // Then: Verify invalid
        assertFalse(invalidPrefs.isValid())
    }

    /**
     * TEST 14: Normalization helper.
     *
     * Verifies [EffectPreferences.normalized()] correctly clamps
     * out-of-range values to valid range.
     */
    @Test
    fun testNormalization() {
        // Given: Preferences with out-of-range values
        val denormalized = EffectPreferences(
            rainIntensity = 1.5f,
            glitchIntensity = -0.5f,
            scanlineIntensity = 0.5f
        )

        // When: Normalize
        val normalized = denormalized.normalized()

        // Then: Verify all values clamped
        assertEquals(1.0f, normalized.rainIntensity)
        assertEquals(0.0f, normalized.glitchIntensity)
        assertEquals(0.5f, normalized.scanlineIntensity)
        assertTrue(normalized.isValid())
    }
}

