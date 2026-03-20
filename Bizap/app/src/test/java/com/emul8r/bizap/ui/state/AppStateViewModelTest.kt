package com.emul8r.bizap.ui.state

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.AuthState
import com.emul8r.bizap.domain.service.AuthenticationManager
import com.emul8r.bizap.ui.landing.GuiMode
import io.mockk.every
import io.mockk.mockk
ication scrpt that import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AppStateViewModel].
 *
 * Verifies that the single state machine correctly computes the appropriate
 * [AppState] from the combination of auth state and DataStore preferences.
 *
 * Tests are framework-free (no Android context required) by using MockK to
 * stub [AuthenticationManager] and [DataStore].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppStateViewModelTest : BaseUnitTest() {

    private lateinit var authManager: AuthenticationManager
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        setupBase()
        authManager = mockk()
        dataStore = mockk()
        every { dataStore.data } returns flowOf(emptyPreferences())
    }

    // -------------------------------------------------------------------------
    // SplashLoading initial state
    // -------------------------------------------------------------------------

    @Test
    fun `initial appState is SplashLoading before DataStore emits`() {
        every { authManager.checkSessionValidity() } returns AuthState.Authenticated
        val viewModel = AppStateViewModel(authManager, dataStore)
        // Before DataStore emits, the stateIn initialValue is SplashLoading
        assertEquals(AppState.SplashLoading, viewModel.appState.value)
    }

    // -------------------------------------------------------------------------
    // PINSetup state
    // -------------------------------------------------------------------------

    @Test
    fun `appState is PINSetup when auth is NotInitialized`() = runTest {
        every { authManager.checkSessionValidity() } returns AuthState.NotInitialized
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns false
        every { prefs[stringPreferencesKey("gui_mode")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        job.cancel()

        assertEquals(AppState.PINSetup, viewModel.appState.value)
    }

    // -------------------------------------------------------------------------
    // Login state
    // -------------------------------------------------------------------------

    @Test
    fun `appState is Login when session is expired`() = runTest {
        every { authManager.checkSessionValidity() } returns AuthState.SessionExpired
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns true
        every { prefs[stringPreferencesKey("gui_mode")] } returns "GUI2"
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        job.cancel()

        assertEquals(AppState.Login, viewModel.appState.value)
    }

    @Test
    fun `appState is Login when PIN is invalid`() = runTest {
        every { authManager.checkSessionValidity() } returns AuthState.InvalidPIN
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns true
        every { prefs[stringPreferencesKey("gui_mode")] } returns "GUI1"
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        job.cancel()

        assertEquals(AppState.Login, viewModel.appState.value)
    }

    // -------------------------------------------------------------------------
    // FirstLaunchWarning state
    // -------------------------------------------------------------------------

    @Test
    fun `appState is FirstLaunchWarning when authenticated and warning not shown`() = runTest {
        every { authManager.checkSessionValidity() } returns AuthState.Authenticated
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns false
        every { prefs[stringPreferencesKey("gui_mode")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        job.cancel()

        assertEquals(AppState.FirstLaunchWarning, viewModel.appState.value)
    }

    // -------------------------------------------------------------------------
    // GUISelection state
    // -------------------------------------------------------------------------

    @Test
    fun `appState is GUISelection when authenticated, warning shown, no GUI stored`() = runTest {
        every { authManager.checkSessionValidity() } returns AuthState.Authenticated
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns true
        every { prefs[stringPreferencesKey("gui_mode")] } returns null
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        job.cancel()

        assertEquals(AppState.GUISelection, viewModel.appState.value)
    }

    // -------------------------------------------------------------------------
    // AppReady state
    // -------------------------------------------------------------------------

    @Test
    fun `appState is AppReady with GUI2 when authenticated and GUI2 stored`() = runTest {
        every { authManager.checkSessionValidity() } returns AuthState.Authenticated
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns true
        every { prefs[stringPreferencesKey("gui_mode")] } returns "GUI2"
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        job.cancel()

        assertEquals(AppState.AppReady(GuiMode.GUI2), viewModel.appState.value)
    }

    @Test
    fun `appState is AppReady with GUI1 when authenticated and GUI1 stored`() = runTest {
        every { authManager.checkSessionValidity() } returns AuthState.Authenticated
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns true
        every { prefs[stringPreferencesKey("gui_mode")] } returns "GUI1"
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        job.cancel()

        assertEquals(AppState.AppReady(GuiMode.GUI1), viewModel.appState.value)
    }

    @Test
    fun `appState is GUISelection when stored GUI mode is unrecognised`() = runTest {
        every { authManager.checkSessionValidity() } returns AuthState.Authenticated
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns true
        every { prefs[stringPreferencesKey("gui_mode")] } returns "LEGACY_UNKNOWN"
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        job.cancel()

        // Unrecognised mode falls back to null → GUISelection
        assertEquals(AppState.GUISelection, viewModel.appState.value)
    }

    // -------------------------------------------------------------------------
    // refreshAuth transitions
    // -------------------------------------------------------------------------

    @Test
    fun `refreshAuth transitions from PINSetup to AppReady when auth succeeds`() = runTest {
        every { authManager.checkSessionValidity() } returnsMany listOf(
            AuthState.NotInitialized,
            AuthState.Authenticated
        )
        val prefs = mockk<Preferences>(relaxed = true)
        every { prefs[booleanPreferencesKey("first_launch_warning_shown")] } returns true
        every { prefs[stringPreferencesKey("gui_mode")] } returns "GUI2"
        every { dataStore.data } returns flowOf(prefs)

        val viewModel = AppStateViewModel(authManager, dataStore)
        val job = launch { viewModel.appState.collect {} }
        advanceUntilIdle()
        assertEquals(AppState.PINSetup, viewModel.appState.value)

        viewModel.refreshAuth()
        advanceUntilIdle()
        job.cancel()
        assertEquals(AppState.AppReady(GuiMode.GUI2), viewModel.appState.value)
    }

    // -------------------------------------------------------------------------
    // AppState sealed class contract
    // -------------------------------------------------------------------------

    @Test
    fun `AppState SplashLoading is distinct from PINSetup`() {
        assertTrue(AppState.SplashLoading != AppState.PINSetup)
    }

    @Test
    fun `AppReady with GUI1 and GUI2 are distinct`() {
        val gui1 = AppState.AppReady(GuiMode.GUI1)
        val gui2 = AppState.AppReady(GuiMode.GUI2)
        assertTrue(gui1 != gui2)
    }

    @Test
    fun `AppReady data class equality holds`() {
        assertEquals(AppState.AppReady(GuiMode.GUI2), AppState.AppReady(GuiMode.GUI2))
    }
}
