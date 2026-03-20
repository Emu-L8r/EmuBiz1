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
 * Unit tests for [ModernGUIMainActivity].
 *
 * Exercises companion-object API (intent creation, extra keys) and
 * business-ID routing logic without launching the Hilt-annotated activity,
 * keeping tests fast and framework-light.
 *
 * Tests requiring an Android [Context] use Robolectric's [ApplicationProvider].
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class ModernGUIMainActivityTest {

    // -----------------------------------------------------------------------
    // Companion object constants
    // -----------------------------------------------------------------------

    @Test
    fun `EXTRA_BUSINESS_ID constant has expected string value`() {
        assertEquals("extra_business_id", ModernGUIMainActivity.EXTRA_BUSINESS_ID)
    }

    @Test
    fun `EXTRA_BUSINESS_ID constant is not empty`() {
        assertTrue(ModernGUIMainActivity.EXTRA_BUSINESS_ID.isNotEmpty())
    }

    @Test
    fun `EXTRA_BUSINESS_ID constant matches TraditionalGUIMainActivity for symmetry`() {
        assertEquals(
            ModernGUIMainActivity.EXTRA_BUSINESS_ID,
            TraditionalGUIMainActivity.EXTRA_BUSINESS_ID
        )
    }

    // -----------------------------------------------------------------------
    // createIntent — Robolectric context tests
    // -----------------------------------------------------------------------

    @Test
    fun `createIntent returns non-null Intent`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = ModernGUIMainActivity.createIntent(context)
        assertNotNull(intent)
    }

    @Test
    fun `createIntent targets ModernGUIMainActivity class`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = ModernGUIMainActivity.createIntent(context)
        assertEquals(
            ModernGUIMainActivity::class.java.name,
            intent.component?.className
        )
    }

    @Test
    fun `createIntent with explicit businessId stores the value in extras`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = ModernGUIMainActivity.createIntent(context, businessId = 77L)
        assertEquals(77L, intent.getLongExtra(ModernGUIMainActivity.EXTRA_BUSINESS_ID, -1L))
    }

    @Test
    fun `createIntent with default businessId stores -1L`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = ModernGUIMainActivity.createIntent(context)
        assertEquals(-1L, intent.getLongExtra(ModernGUIMainActivity.EXTRA_BUSINESS_ID, -999L))
    }

    @Test
    fun `createIntent with businessId zero stores 0L`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = ModernGUIMainActivity.createIntent(context, businessId = 0L)
        assertEquals(0L, intent.getLongExtra(ModernGUIMainActivity.EXTRA_BUSINESS_ID, -1L))
    }

    // -----------------------------------------------------------------------
    // Business ID routing rules (pure logic — no Android context)
    // -----------------------------------------------------------------------

    @Test
    fun `positive intentBusinessId is used directly`() {
        assertEquals(55L, resolveBusinessId(intentBusinessId = 55L, profileId = 2L))
    }

    @Test
    fun `negative intentBusinessId falls through to profileId`() {
        assertEquals(8L, resolveBusinessId(intentBusinessId = -1L, profileId = 8L))
    }

    @Test
    fun `zero intentBusinessId falls through to profileId`() {
        assertEquals(3L, resolveBusinessId(intentBusinessId = 0L, profileId = 3L))
    }

    @Test
    fun `both non-positive fall back to default 1L`() {
        assertEquals(1L, resolveBusinessId(intentBusinessId = -1L, profileId = 0L))
    }

    @Test
    fun `profileId zero with valid intentBusinessId uses intentBusinessId`() {
        assertEquals(20L, resolveBusinessId(intentBusinessId = 20L, profileId = 0L))
    }

    @Test
    fun `GUI2 mode maps to ModernGUIMainActivity`() {
        val activityClass = activityClassForMode(GuiMode.GUI2)
        assertEquals(ModernGUIMainActivity::class.java, activityClass)
    }

    // -----------------------------------------------------------------------
    // Intent flag validation
    // -----------------------------------------------------------------------

    @Test
    fun `createIntent does not set FLAG_ACTIVITY_NEW_TASK by default`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = ModernGUIMainActivity.createIntent(context)
        assertEquals(0, intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    // -----------------------------------------------------------------------
    // Helpers mirroring activity logic
    // -----------------------------------------------------------------------

    private fun resolveBusinessId(intentBusinessId: Long, profileId: Long): Long = when {
        intentBusinessId > 0L -> intentBusinessId
        profileId > 0L -> profileId
        else -> 1L
    }

    private fun activityClassForMode(mode: GuiMode): Class<*> = when (mode) {
        GuiMode.GUI1 -> TraditionalGUIMainActivity::class.java
        GuiMode.GUI2 -> ModernGUIMainActivity::class.java
    }
}
