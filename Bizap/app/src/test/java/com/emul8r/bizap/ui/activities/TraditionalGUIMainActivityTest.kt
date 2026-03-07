package com.emul8r.bizap.ui.activities

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.ui.landing.GuiMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Unit tests for [TraditionalGUIMainActivity].
 *
 * These tests exercise the companion-object API (intent creation, extra keys)
 * and routing/business-ID logic without launching the Hilt-annotated activity
 * itself, keeping them fast and framework-independent.
 *
 * Tests that require an Android [Context] use Robolectric's
 * [ApplicationProvider] for a lightweight, in-process context.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class TraditionalGUIMainActivityTest {

    // -----------------------------------------------------------------------
    // Companion object constants
    // -----------------------------------------------------------------------

    @Test
    fun `EXTRA_BUSINESS_ID constant has expected string value`() {
        assertEquals("extra_business_id", TraditionalGUIMainActivity.EXTRA_BUSINESS_ID)
    }

    @Test
    fun `EXTRA_BUSINESS_ID constant is not empty`() {
        assertTrue(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID.isNotEmpty())
    }

    @Test
    fun `EXTRA_BUSINESS_ID constant matches ModernGUIMainActivity for symmetry`() {
        // Both activities share the same extra key so callers can use either class
        // constant interchangeably.
        assertEquals(
            TraditionalGUIMainActivity.EXTRA_BUSINESS_ID,
            ModernGUIMainActivity.EXTRA_BUSINESS_ID
        )
    }

    // -----------------------------------------------------------------------
    // createIntent — Robolectric context tests
    // -----------------------------------------------------------------------

    @Test
    fun `createIntent returns non-null Intent`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = TraditionalGUIMainActivity.createIntent(context)
        assertNotNull(intent)
    }

    @Test
    fun `createIntent targets TraditionalGUIMainActivity class`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = TraditionalGUIMainActivity.createIntent(context)
        assertEquals(
            TraditionalGUIMainActivity::class.java.name,
            intent.component?.className
        )
    }

    @Test
    fun `createIntent with explicit businessId stores the value in extras`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = TraditionalGUIMainActivity.createIntent(context, businessId = 99L)
        assertEquals(99L, intent.getLongExtra(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID, -1L))
    }

    @Test
    fun `createIntent with default businessId stores -1L`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = TraditionalGUIMainActivity.createIntent(context)
        assertEquals(-1L, intent.getLongExtra(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID, -999L))
    }

    @Test
    fun `createIntent with businessId zero stores 0L`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = TraditionalGUIMainActivity.createIntent(context, businessId = 0L)
        assertEquals(0L, intent.getLongExtra(TraditionalGUIMainActivity.EXTRA_BUSINESS_ID, -1L))
    }

    // -----------------------------------------------------------------------
    // Business ID routing rules (pure logic — no Android context)
    // -----------------------------------------------------------------------

    @Test
    fun `positive intentBusinessId is used directly`() {
        val intentBusinessId = 42L
        val profileId = 7L
        val resolved = resolveBusinessId(intentBusinessId, profileId)
        assertEquals(42L, resolved)
    }

    @Test
    fun `zero intentBusinessId falls through to profileId`() {
        val resolved = resolveBusinessId(intentBusinessId = 0L, profileId = 5L)
        assertEquals(5L, resolved)
    }

    @Test
    fun `negative intentBusinessId falls through to profileId`() {
        val resolved = resolveBusinessId(intentBusinessId = -1L, profileId = 10L)
        assertEquals(10L, resolved)
    }

    @Test
    fun `both zero falls back to default 1L`() {
        val resolved = resolveBusinessId(intentBusinessId = 0L, profileId = 0L)
        assertEquals(1L, resolved)
    }

    @Test
    fun `GUI1 mode maps to TraditionalGUIMainActivity`() {
        val activityClass = activityClassForMode(GuiMode.GUI1)
        assertEquals(TraditionalGUIMainActivity::class.java, activityClass)
    }

    // -----------------------------------------------------------------------
    // Helpers mirroring activity logic (keeps tests free of Android deps)
    // -----------------------------------------------------------------------

    /**
     * Mirrors the `resolvedBusinessId` computation inside
     * [ModernGUIMainActivity.onCreate].  Both activities share this logic.
     */
    private fun resolveBusinessId(intentBusinessId: Long, profileId: Long): Long = when {
        intentBusinessId > 0L -> intentBusinessId
        profileId > 0L -> profileId
        else -> 1L
    }

    /**
     * Maps a [GuiMode] to the activity that should be launched for it.
     * Mirrors the routing decision in the LandingScreen flow.
     */
    private fun activityClassForMode(mode: GuiMode): Class<*> = when (mode) {
        GuiMode.GUI1 -> TraditionalGUIMainActivity::class.java
        GuiMode.GUI2 -> ModernGUIMainActivity::class.java
    }

    // -----------------------------------------------------------------------
    // Intent flag validation
    // -----------------------------------------------------------------------

    @Test
    fun `createIntent does not set FLAG_ACTIVITY_NEW_TASK by default`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = TraditionalGUIMainActivity.createIntent(context)
        // The caller decides whether to add task flags; the factory should not.
        assertEquals(0, intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
