package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.DisplayMode
import com.emul8r.bizap.domain.model.Settings
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.domain.model.UiDensity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRepositoryImplTest : BaseUnitTest() {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher),
            produceFile = { tmpFolder.newFile("test_settings.preferences_pb") }
        )
        repository = SettingsRepositoryImpl(dataStore)
    }

    @After
    fun teardown() {
        // TemporaryFolder @Rule cleans up the file
    }

    // ── Default values ─────────────────────────────────────────────────────

    @Test
    fun `settings emits defaults on first read`() = runTest {
        val settings = repository.settings.first()
        assertEquals(Settings(), settings)
    }

    @Test
    fun `default themePreference is AUTO`() = runTest {
        assertEquals(ThemePreference.AUTO, repository.settings.first().themePreference)
    }

    @Test
    fun `default displayMode is LIST_VIEW`() = runTest {
        assertEquals(DisplayMode.LIST_VIEW, repository.settings.first().displayMode)
    }

    @Test
    fun `default notificationsEnabled is true`() = runTest {
        assertTrue(repository.settings.first().notificationsEnabled)
    }

    @Test
    fun `default autoSyncEnabled is true`() = runTest {
        assertTrue(repository.settings.first().autoSyncEnabled)
    }

    @Test
    fun `default syncFrequencyMinutes is 15`() = runTest {
        assertEquals(15, repository.settings.first().syncFrequencyMinutes)
    }

    // ── Theme ──────────────────────────────────────────────────────────────

    @Test
    fun `updateThemePreference persists DARK`() = runTest {
        repository.updateThemePreference(ThemePreference.DARK)
        assertEquals(ThemePreference.DARK, repository.settings.first().themePreference)
    }

    @Test
    fun `updateThemePreference persists LIGHT`() = runTest {
        repository.updateThemePreference(ThemePreference.LIGHT)
        assertEquals(ThemePreference.LIGHT, repository.settings.first().themePreference)
    }

    // ── Display mode ───────────────────────────────────────────────────────

    @Test
    fun `updateDisplayMode persists GRID_VIEW`() = runTest {
        repository.updateDisplayMode(DisplayMode.GRID_VIEW)
        assertEquals(DisplayMode.GRID_VIEW, repository.settings.first().displayMode)
    }

    @Test
    fun `updateDisplayMode persists CARD_VIEW`() = runTest {
        repository.updateDisplayMode(DisplayMode.CARD_VIEW)
        assertEquals(DisplayMode.CARD_VIEW, repository.settings.first().displayMode)
    }

    // ── UI density ─────────────────────────────────────────────────────────

    @Test
    fun `updateUiDensity persists COMPACT`() = runTest {
        repository.updateUiDensity(UiDensity.COMPACT)
        assertEquals(UiDensity.COMPACT, repository.settings.first().uiDensity)
    }

    // ── Notifications ──────────────────────────────────────────────────────

    @Test
    fun `updateNotificationsEnabled persists false`() = runTest {
        repository.updateNotificationsEnabled(false)
        assertFalse(repository.settings.first().notificationsEnabled)
    }

    @Test
    fun `updateEmailNotificationsEnabled persists false`() = runTest {
        repository.updateEmailNotificationsEnabled(false)
        assertFalse(repository.settings.first().emailNotificationsEnabled)
    }

    // ── Sync ───────────────────────────────────────────────────────────────

    @Test
    fun `updateAutoSyncEnabled persists false`() = runTest {
        repository.updateAutoSyncEnabled(false)
        assertFalse(repository.settings.first().autoSyncEnabled)
    }

    @Test
    fun `updateSyncFrequencyMinutes persists 30`() = runTest {
        repository.updateSyncFrequencyMinutes(30)
        assertEquals(30, repository.settings.first().syncFrequencyMinutes)
    }

    // ── Currency / locale ──────────────────────────────────────────────────

    @Test
    fun `updateCurrencyCode persists GBP`() = runTest {
        repository.updateCurrencyCode("GBP")
        assertEquals("GBP", repository.settings.first().currencyCode)
    }

    @Test
    fun `updateLocaleLanguage persists fr`() = runTest {
        repository.updateLocaleLanguage("fr")
        assertEquals("fr", repository.settings.first().localeLanguage)
    }

    // ── Reset ──────────────────────────────────────────────────────────────

    @Test
    fun `resetToDefaults restores all values to defaults`() = runTest {
        // Change one setting to verify reset works
        val beforeReset = repository.settings.first()
        repository.updateThemePreference(ThemePreference.DARK)

        // Verify it changed
        val changed = repository.settings.first()
        assertEquals(ThemePreference.DARK, changed.themePreference)
        assertEquals(ThemePreference.AUTO, beforeReset.themePreference)  // Was different before

        // Reset to defaults
        repository.resetToDefaults()

        // Verify all fields are back to defaults
        val reset = repository.settings.first()
        val defaults = Settings()

        // Check all fields match defaults except lastUpdated
        assertEquals(defaults.themePreference, reset.themePreference)
        assertEquals(defaults.displayMode, reset.displayMode)
        assertEquals(defaults.defaultInvoiceStatusFilter, reset.defaultInvoiceStatusFilter)
        assertEquals(defaults.defaultDaysLookback, reset.defaultDaysLookback)
        assertEquals(defaults.uiDensity, reset.uiDensity)
        assertEquals(defaults.notificationsEnabled, reset.notificationsEnabled)
        assertEquals(defaults.emailNotificationsEnabled, reset.emailNotificationsEnabled)
        assertEquals(defaults.currencyCode, reset.currencyCode)
        assertEquals(defaults.localeLanguage, reset.localeLanguage)
        assertEquals(defaults.autoSyncEnabled, reset.autoSyncEnabled)
        assertEquals(defaults.syncFrequencyMinutes, reset.syncFrequencyMinutes)

        // lastUpdated should NOT be 0 (should be current time)
        assertTrue(reset.lastUpdated > 0, "lastUpdated should be updated after reset")
    }

    // ── lastUpdated ────────────────────────────────────────────────────────

    @Test
    fun `lastUpdated is set after any write`() = runTest {
        repository.updateThemePreference(ThemePreference.LIGHT)
        assertTrue(repository.settings.first().lastUpdated > 0)
    }
}
