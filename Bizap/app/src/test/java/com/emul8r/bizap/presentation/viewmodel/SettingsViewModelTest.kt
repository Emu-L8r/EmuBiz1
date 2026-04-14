package com.emul8r.bizap.presentation.viewmodel

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.Settings
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.domain.repository.SettingsRepository
import com.emul8r.bizap.domain.usecase.settings.GetSettingsUseCase
import com.emul8r.bizap.domain.usecase.settings.ResetSettingsToDefaultUseCase
import com.emul8r.bizap.domain.usecase.settings.UpdateDisplayModeUseCase
import com.emul8r.bizap.domain.usecase.settings.UpdateNotificationSettingsUseCase
import com.emul8r.bizap.domain.usecase.settings.UpdateSyncSettingsUseCase
import com.emul8r.bizap.domain.usecase.settings.UpdateThemeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SettingsViewModelTest : BaseUnitTest() {
@OptIn(ExperimentalCoroutinesApi::class)

    private val repository: SettingsRepository = mockk(relaxed = true) {
        coEvery { settings } returns settingsFlow
    }

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel(
            getSettingsUseCase = GetSettingsUseCase(repository),
            settingsRepository = repository,
            updateThemeUseCase = UpdateThemeUseCase(repository),
            updateDisplayModeUseCase = UpdateDisplayModeUseCase(repository),
            updateNotificationSettingsUseCase = UpdateNotificationSettingsUseCase(repository),
            updateSyncSettingsUseCase = UpdateSyncSettingsUseCase(repository),
            resetSettingsToDefaultUseCase = ResetSettingsToDefaultUseCase(repository)
        )
    }

    // ── Initial state ──────────────────────────────────────────────────────

    @Test
    fun `settings exposes initial defaults`() = runTest {
        advanceUntilIdle()
        assertEquals(Settings(), viewModel.settings.first())
    }

    @Test
    fun `themePreference derived flow emits AUTO by default`() = runTest {
        advanceUntilIdle()
        assertEquals(ThemePreference.AUTO, viewModel.themePreference.first())
    }

    @Test
    fun `displayMode derived flow emits LIST_VIEW by default`() = runTest {
        advanceUntilIdle()
        assertEquals(DisplayMode.LIST_VIEW, viewModel.displayMode.first())
    }

    @Test
    fun `notificationsEnabled derived flow is true by default`() = runTest {
        advanceUntilIdle()
        assertEquals(true, viewModel.notificationsEnabled.first())
    }

    @Test
    fun `autoSyncEnabled derived flow is true by default`() = runTest {
        advanceUntilIdle()
        assertEquals(true, viewModel.autoSyncEnabled.first())
    }

    // ── Delegating to repository ───────────────────────────────────────────

    @Test
    fun `setThemePreference delegates to repository`() = runTest {
        viewModel.setThemePreference(ThemePreference.DARK)
        advanceUntilIdle()
        coVerify { repository.updateThemePreference(ThemePreference.DARK) }
    }

    @Test
    fun `setDisplayMode delegates to repository`() = runTest {
        viewModel.setDisplayMode(DisplayMode.GRID_VIEW)
        advanceUntilIdle()
        coVerify { repository.updateDisplayMode(DisplayMode.GRID_VIEW) }
    }

    @Test
    fun `setNotificationsEnabled delegates to repository`() = runTest {
        viewModel.setNotificationsEnabled(false)
        advanceUntilIdle()
        coVerify { repository.updateNotificationsEnabled(false) }
    }

    @Test
    fun `setEmailNotificationsEnabled delegates to repository`() = runTest {
        viewModel.setEmailNotificationsEnabled(false)
        advanceUntilIdle()
        coVerify { repository.updateEmailNotificationsEnabled(false) }
    }

    @Test
    fun `setAutoSyncEnabled delegates to repository`() = runTest {
        viewModel.setAutoSyncEnabled(false)
        advanceUntilIdle()
        coVerify { repository.updateAutoSyncEnabled(false) }
    }

    @Test
    fun `setSyncFrequencyMinutes delegates to repository`() = runTest {
        viewModel.setSyncFrequencyMinutes(30)
        advanceUntilIdle()
        coVerify { repository.updateSyncFrequencyMinutes(30) }
    }

    @Test
    fun `resetToDefaults delegates to repository`() = runTest {
        viewModel.resetToDefaults()
        advanceUntilIdle()
        coVerify { repository.resetToDefaults() }
    }

    // ── Reactive updates ───────────────────────────────────────────────────

    @Test
    fun `settings flow updates when repository emits new value`() = runTest {
        advanceUntilIdle()
        val updated = Settings(themePreference = ThemePreference.DARK)
        settingsFlow.value = updated
        advanceUntilIdle()
        // Verify that the derived themePreference flow picks up the emitted change
        assertEquals(ThemePreference.DARK, viewModel.themePreference.first())
    }
}



