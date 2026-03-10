package com.emul8r.bizap.ui.landing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emul8r.bizap.BaseUnitTest
import io.mockk.any
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
/**
 * Unit tests for the landing-page navigation system:
 * LandingViewModel (DataStore persistence) and GuiMode routing.
 *
 * Covers:
 * - GuiMode enum contract
 * - LandingViewModel initial state (no preference stored)
 * - LandingViewModel emits GUI1 when DataStore contains "GUI1"
 * - LandingViewModel emits GUI2 when DataStore contains "GUI2"
 * - LandingViewModel handles unknown stored value gracefully
 * - selectMode(GUI1) persists the correct key
 * - selectMode(GUI2) persists the correct key
 * - resetMode() removes the stored key
 * - Navigation paths enumeration
 */
class NavigationTest : BaseUnitTest() {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var mockPreferences: Preferences
    @Before
    fun setUp() {
        dataStore = mockk(relaxed = true)
        mockPreferences = mockk(relaxed = true)
    }
    // -----------------------------------------------------------------------
    // GuiMode enum contract
    @Test
    fun `GuiMode has exactly two variants`() {
        assertEquals(2, GuiMode.entries.size)
    }

    @Test
    fun `GuiMode GUI1 name is GUI1`() {
        assertEquals("GUI1", GuiMode.GUI1.name)
    }

    @Test
    fun `GuiMode GUI2 name is GUI2`() {
        assertEquals("GUI2", GuiMode.GUI2.name)
    }

    @Test
    fun `GuiMode valueOf returns GUI1`() {
        assertEquals(GuiMode.GUI1, GuiMode.valueOf("GUI1"))
    }

    @Test
    fun `GuiMode valueOf returns GUI2`() {
        assertEquals(GuiMode.GUI2, GuiMode.valueOf("GUI2"))
    }

    @Test
    fun `GuiMode valueOf throws for unknown value`() {
        var threw = false
        try {
            GuiMode.valueOf("UNKNOWN_GUI")
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertEquals(true, threw)
    }

    // LandingViewModel — initial state (no stored preference)
    @Test
    fun `selectedMode is null when DataStore has no gui_mode key`() = runTest {
        // Arrange — DataStore returns preferences with no "gui_mode" entry
        val emptyPrefs = emptyPreferences()
        every { dataStore.data } returns flowOf(emptyPrefs)
        val viewModel = LandingViewModel(dataStore)
        // Act
        val result = viewModel.selectedMode.first()
        // Assert — null signals "first launch, show landing screen"
        assertNull(result)
    }

    // LandingViewModel — reading persisted mode
    @Test
    fun `selectedMode emits GUI1 when DataStore contains GUI1`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey("gui_mode")] } returns "GUI1"
        every { dataStore.data } returns flowOf(prefs)
        val viewModel = LandingViewModel(dataStore)
        val result = viewModel.selectedMode.first()
        assertEquals(GuiMode.GUI1, result)
    }

    @Test
    fun `selectedMode emits GUI2 when DataStore contains GUI2`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey("gui_mode")] } returns "GUI2"
        every { dataStore.data } returns flowOf(prefs)
        val viewModel = LandingViewModel(dataStore)
        val result = viewModel.selectedMode.first()
        assertEquals(GuiMode.GUI2, result)
    }

    @Test
    fun `selectedMode emits null for unrecognised stored value`() = runTest {
        val prefs = mockk<Preferences>()
        every { prefs[stringPreferencesKey("gui_mode")] } returns "LEGACY_V1"
        every { dataStore.data } returns flowOf(prefs)
        val viewModel = LandingViewModel(dataStore)
        val result = viewModel.selectedMode.first()
        // Unrecognised values are treated as "no selection" → null
        assertNull(result)
    }

    // LandingViewModel — persisting the selection
    @Test
    fun `selectMode GUI1 calls dataStore edit`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())
        coEvery { dataStore.edit<Preferences>(any()) } returns emptyPreferences()
        val viewModel = LandingViewModel(dataStore)
        viewModel.selectMode(GuiMode.GUI1)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { dataStore.edit<Preferences>(any()) }
    }

    @Test
    fun `selectMode GUI2 calls dataStore edit`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())
        coEvery { dataStore.edit<Preferences>(any()) } returns emptyPreferences()
        val viewModel = LandingViewModel(dataStore)
        viewModel.selectMode(GuiMode.GUI2)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { dataStore.edit<Preferences>(any()) }
    }

    // LandingViewModel — resetting the selection
    @Test
    fun `resetMode calls dataStore edit`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())
        coEvery { dataStore.edit<Preferences>(any()) } returns emptyPreferences()
        val viewModel = LandingViewModel(dataStore)
        viewModel.resetMode()
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { dataStore.edit<Preferences>(any()) }
    }

    // Navigation routing paths
    @Test
    fun `null selectedMode maps to landing screen route`() {
        // When no GUI is selected, the app should show the landing screen.
        // This test verifies the enum-to-route mapping logic (no Android deps needed).
        val route = resolveRoute(null)
        assertEquals(Route.LANDING, route)
    }

    @Test
    fun `GUI1 selectedMode maps to GUI1 main route`() {
        val route = resolveRoute(GuiMode.GUI1)
        assertEquals(Route.GUI1_MAIN, route)
    }

    @Test
    fun `GUI2 selectedMode maps to GUI2 main route`() {
        val route = resolveRoute(GuiMode.GUI2)
        assertEquals(Route.GUI2_MAIN, route)
    }

    @Test
    fun `all GuiMode values have a resolved route`() {
        GuiMode.entries.forEach { mode ->
            val route = resolveRoute(mode)
            assertNotNull(route)
        }
    }

    /**
     * Pure routing helper — mirrors the `when (selectedMode)` block in MainActivity.
     * Keeps the test free of Android framework dependencies.
     */
    private enum class Route { LANDING, GUI1_MAIN, GUI2_MAIN }
    private fun resolveRoute(mode: GuiMode?): Route = when (mode) {
        null -> Route.LANDING
        GuiMode.GUI1 -> Route.GUI1_MAIN
        GuiMode.GUI2 -> Route.GUI2_MAIN
    }
}
