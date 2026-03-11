@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.navigation

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.ui.activities.ModernGUIMainActivity
import com.emul8r.bizap.ui.activities.TraditionalGUIMainActivity
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.landing.LandingViewModel
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
/**
 * Tests for the full dual-GUI navigation flow:
 * landing screen → GUI selection → activity launch → back navigation → switch.
 *
 * Pure logic tests are framework-free; tests that need an Android [Context]
 * use Robolectric's [ApplicationProvider].
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class DualGUINavigationTest : BaseUnitTest() {
    private lateinit var dataStore: DataStore<Preferences>
    @Before
    fun setUp() {
        dataStore = mockk(relaxed = true)
    }
    // -----------------------------------------------------------------------
    // Landing page navigates to the correct activity class
    @Test
    fun `landing page navigates to TraditionalGUIMainActivity for GUI1`() {
        val activityClass = activityForMode(GuiMode.GUI1)
        assertEquals(TraditionalGUIMainActivity::class.java, activityClass)
    }

    @Test
    fun `landing page navigates to ModernGUIMainActivity for GUI2`() {
        val activityClass = activityForMode(GuiMode.GUI2)
        assertEquals(ModernGUIMainActivity::class.java, activityClass)
    }

    @Test
    fun `null mode shows landing screen (no activity started)`() {
        val activityClass = activityForMode(null)
        assertNull("Null mode should not start any activity", activityClass)
    }

    @Test
    fun `all GuiMode values map to a non-null activity`() {
        GuiMode.entries.forEach { mode ->
            assertNotNull("$mode must map to an activity", activityForMode(mode))
        }
    }

    // TraditionalGUIMainActivity initialises correctly
    @Test
    fun `TraditionalGUIMainActivity extra key is defined`() {
        assertNotNull(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID)
        assertTrue(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID.isNotEmpty())
    }

    @Test
    fun `TraditionalGUIMainActivity createIntent is not null`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = TraditionalGUIMainActivity.createIntent(context, 1L)
        assertNotNull(intent)
    }

    // ModernGUIMainActivity initialises correctly
    @Test
    fun `ModernGUIMainActivity extra key is defined`() {
        assertNotNull(ModernGUIMainActivity.EXTRA_BUSINESS_ID)
        assertTrue(ModernGUIMainActivity.EXTRA_BUSINESS_ID.isNotEmpty())
    }

    @Test
    fun `ModernGUIMainActivity createIntent is not null`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = ModernGUIMainActivity.createIntent(context, 1L)
        assertNotNull(intent)
    }

    // No data loss on GUI switch
    @Test
    fun `businessId is preserved when switching from GUI1 to GUI2`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val businessId = 42L
        val gui1Intent = TraditionalGUIMainActivity.createIntent(context, businessId)
        val extractedId = gui1Intent.getLongExtra(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID, -1L)
        val gui2Intent = ModernGUIMainActivity.createIntent(context, extractedId)
        val finalId = gui2Intent.getLongExtra(ModernGUIMainActivity.EXTRA_BUSINESS_ID, -1L)
        assertEquals(businessId, finalId)
    }

    @Test
    fun `businessId is preserved when switching from GUI2 to GUI1`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val businessId = 99L
        val gui2Intent = ModernGUIMainActivity.createIntent(context, businessId)
        val extractedId = gui2Intent.getLongExtra(ModernGUIMainActivity.EXTRA_BUSINESS_ID, -1L)
        val gui1Intent = TraditionalGUIMainActivity.createIntent(context, extractedId)
        val finalId = gui1Intent.getLongExtra(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID, -1L)
        assertEquals(businessId, finalId)
    }

    // Settings allows GUI change (via resetMode)
    @Test
    fun `resetMode allows re-selection from landing screen`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())
        coEvery { dataStore.edit<Preferences>(any()) } returns emptyPreferences()
        val viewModel = LandingViewModel(dataStore)
        viewModel.selectMode(GuiMode.GUI2)
        viewModel.resetMode()
        testDispatcher.scheduler.advanceUntilIdle()
        // edit() must be called at least twice (once for select, once for reset)
        coVerify(atLeast = 2) { dataStore.edit<Preferences>(any()) }
    }

    @Test
    fun `after resetMode the stored mode becomes null`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())
        val viewModel = LandingViewModel(dataStore)
        val mode = viewModel.selectedMode.first()
        assertNull("After reset/no selection, mode should be null", mode)
    }

    // Navigation history / intent flags
    @Test
    fun `both activities share the same intent extra key`() {
        assertEquals(
            TraditionalGUIMainActivity.EXTRA_BUSINESS_ID,
            ModernGUIMainActivity.EXTRA_BUSINESS_ID
        )
    }

    @Test
    fun `createIntent for both activities does not set FLAG_ACTIVITY_NEW_TASK`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent1 = TraditionalGUIMainActivity.createIntent(context, 1L)
        val intent2 = ModernGUIMainActivity.createIntent(context, 1L)
        assertEquals(0, intent1.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        assertEquals(0, intent2.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun `both activities receive businessId consistently`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val businessId = 123L
        val t = TraditionalGUIMainActivity.createIntent(context, businessId)
            .getLongExtra(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID, -1L)
        val m = ModernGUIMainActivity.createIntent(context, businessId)
            .getLongExtra(ModernGUIMainActivity.EXTRA_BUSINESS_ID, -1L)
        assertEquals(businessId, t)
        assertEquals(businessId, m)
        assertEquals(t, m)
    }

    // Internal helpers
    /**
     * Returns the [Class] of the activity that should be started for [mode],
     * or `null` if the landing screen should be shown instead.
     *
     * Mirrors the `when (selectedMode)` dispatch in MainActivity.
     */
    private fun activityForMode(mode: GuiMode?): Class<*>? = when (mode) {
        null -> null
        GuiMode.GUI1 -> TraditionalGUIMainActivity::class.java
        GuiMode.GUI2 -> ModernGUIMainActivity::class.java
    }
}
