package com.emul8r.bizap.data.config

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.logging.ErrorLogger
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [RemoteConfigManagerImpl] (the production [FeatureFlagManager]).
 *
 * Firebase Task mocking is avoided by making fetchAndActivate() throw, which
 * causes [RemoteConfigManagerImpl.ensureFetched] to fall back to cached/default
 * values — the same path taken in production when Remote Config is unavailable.
 * Local flag reads (getBoolean/getString) are stubbed normally via mockk.
 *
 * Tests cover:
 * - Flags enabled / disabled via Remote Config
 * - Gradual rollout / A-B bucketing logic
 * - Local override (setEnabled)
 * - Fallback to default when Remote Config throws
 * - Flow observation
 */
class RemoteConfigManagerTest : BaseUnitTest() {

    private val remoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
    private val errorLogger: ErrorLogger = mockk(relaxed = true)

    private lateinit var manager: RemoteConfigManagerImpl

    @Before
    fun setUp() {
        // Make fetchAndActivate() throw so tests never wait for real network I/O.
        // ensureFetched() catches this and continues with default/cached values.
        every { remoteConfig.fetchAndActivate() } throws RuntimeException("RC unavailable in tests")

        // Default: all flags return false (mirrors Remote Config defaults)
        FeatureFlag.entries.forEach { flag ->
            every { remoteConfig.getBoolean(flag.key) } returns false
        }

        manager = RemoteConfigManagerImpl(remoteConfig, errorLogger)
    }

    // ─── isEnabled ───────────────────────────────────────────────────────────────

    @Test
    fun `isEnabled returns false when Remote Config flag is false`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.EMAIL_INTEGRATION.key) } returns false
        assertFalse(manager.isEnabled(FeatureFlag.EMAIL_INTEGRATION))
    }

    @Test
    fun `isEnabled returns true when Remote Config flag is true`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.EMAIL_INTEGRATION.key) } returns true
        assertTrue(manager.isEnabled(FeatureFlag.EMAIL_INTEGRATION))
    }

    @Test
    fun `isEnabled falls back to default when Remote Config getBoolean throws`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.ADVANCED_SEARCH.key) } throws RuntimeException("RC failure")
        // Default value for ADVANCED_SEARCH is false
        assertFalse(manager.isEnabled(FeatureFlag.ADVANCED_SEARCH))
        verify { errorLogger.logError(any<Throwable>(), any()) }
    }

    @Test
    fun `isEnabled returns local override when set`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.DARK_MODE.key) } returns false
        manager.setEnabled(FeatureFlag.DARK_MODE, true)
        assertTrue(manager.isEnabled(FeatureFlag.DARK_MODE))
    }

    @Test
    fun `isEnabled local override false overrides Remote Config true`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.DARK_MODE.key) } returns true
        manager.setEnabled(FeatureFlag.DARK_MODE, false)
        assertFalse(manager.isEnabled(FeatureFlag.DARK_MODE))
    }

    // ─── isEnabledForUser ────────────────────────────────────────────────────────

    @Test
    fun `isEnabledForUser returns false when flag is globally disabled`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.RECURRING_INVOICES.key) } returns false
        assertFalse(manager.isEnabledForUser(FeatureFlag.RECURRING_INVOICES, userId = 5L))
    }

    @Test
    fun `isEnabledForUser includes user in rollout when bucket is within percentage`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.RECURRING_INVOICES.key) } returns true
        // userId 10 → bucket = 10 % 100 = 10; rollout = 50 → 10 < 50 → included
        every { remoteConfig.getString("${FeatureFlag.RECURRING_INVOICES.key}_rollout_percentage") } returns "50"
        assertTrue(manager.isEnabledForUser(FeatureFlag.RECURRING_INVOICES, userId = 10L))
    }

    @Test
    fun `isEnabledForUser excludes user when bucket exceeds rollout percentage`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.RECURRING_INVOICES.key) } returns true
        // userId 90 → bucket = 90; rollout = 50 → 90 >= 50 → excluded
        every { remoteConfig.getString("${FeatureFlag.RECURRING_INVOICES.key}_rollout_percentage") } returns "50"
        assertFalse(manager.isEnabledForUser(FeatureFlag.RECURRING_INVOICES, userId = 90L))
    }

    @Test
    fun `isEnabledForUser 0 percent rollout excludes all users`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.EMAIL_INTEGRATION.key) } returns true
        every { remoteConfig.getString("${FeatureFlag.EMAIL_INTEGRATION.key}_rollout_percentage") } returns "0"
        assertFalse(manager.isEnabledForUser(FeatureFlag.EMAIL_INTEGRATION, userId = 0L))
    }

    @Test
    fun `isEnabledForUser 100 percent rollout includes all users`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.EMAIL_INTEGRATION.key) } returns true
        every { remoteConfig.getString("${FeatureFlag.EMAIL_INTEGRATION.key}_rollout_percentage") } returns "100"
        // userId 99 → bucket = 99 < 100 → included
        assertTrue(manager.isEnabledForUser(FeatureFlag.EMAIL_INTEGRATION, userId = 99L))
    }

    @Test
    fun `isEnabledForUser bucket is stable for the same userId`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.ADVANCED_SEARCH.key) } returns true
        every { remoteConfig.getString("${FeatureFlag.ADVANCED_SEARCH.key}_rollout_percentage") } returns "30"
        val userId = 12345L
        val first = manager.isEnabledForUser(FeatureFlag.ADVANCED_SEARCH, userId)
        val second = manager.isEnabledForUser(FeatureFlag.ADVANCED_SEARCH, userId)
        assertEquals(first, second)
    }

    @Test
    fun `isEnabledForUser falls back to false when rollout percentage getString throws`() = runTest {
        every { remoteConfig.getBoolean(FeatureFlag.CUSTOMER_PORTAL.key) } returns true
        every { remoteConfig.getString("${FeatureFlag.CUSTOMER_PORTAL.key}_rollout_percentage") } throws RuntimeException("RC failure")
        assertFalse(manager.isEnabledForUser(FeatureFlag.CUSTOMER_PORTAL, userId = 1L))
        verify { errorLogger.logError(any<Throwable>(), any()) }
    }

    // ─── setEnabled / local overrides ────────────────────────────────────────────

    @Test
    fun `setEnabled persists override for subsequent isEnabled calls`() = runTest {
        manager.setEnabled(FeatureFlag.PUSH_NOTIFICATIONS, true)
        assertTrue(manager.isEnabled(FeatureFlag.PUSH_NOTIFICATIONS))
        manager.setEnabled(FeatureFlag.PUSH_NOTIFICATIONS, false)
        assertFalse(manager.isEnabled(FeatureFlag.PUSH_NOTIFICATIONS))
    }

    // ─── observeFlag ─────────────────────────────────────────────────────────────

    @Test
    fun `observeFlag emits default value initially`() = runTest {
        val value = manager.observeFlag(FeatureFlag.LOCALIZATION).first()
        // Default for LOCALIZATION is false
        assertFalse(value)
    }

    @Test
    fun `observeFlag emits updated value after setEnabled`() = runTest {
        manager.setEnabled(FeatureFlag.MULTI_BUSINESS, true)
        val value = manager.observeFlag(FeatureFlag.MULTI_BUSINESS).first()
        assertTrue(value)
    }

    // ─── FeatureFlag enum ────────────────────────────────────────────────────────

    @Test
    fun `all FeatureFlag entries have non-blank keys`() {
        FeatureFlag.entries.forEach { flag ->
            assertTrue(flag.key.isNotBlank(), "Flag ${flag.name} has blank key")
        }
    }

    @Test
    fun `all FeatureFlag entries have unique keys`() {
        val keys = FeatureFlag.entries.map { it.key }
        assertEquals(keys.distinct().size, keys.size, "Duplicate flag keys detected")
    }

    @Test
    fun `all FeatureFlag entries have expected defaults`() {
        // EFFECT_* flags default to true (enabled by default per AGENTS.md)
        val trueByDefault = setOf(
            FeatureFlag.EFFECT_RAIN, FeatureFlag.EFFECT_GLITCH, FeatureFlag.EFFECT_SCANLINES
        )
        FeatureFlag.entries.forEach { flag ->
            if (flag in trueByDefault) {
                assertTrue(flag.defaultValue, "Flag ${flag.name} should default to true")
            } else {
                assertFalse(flag.defaultValue, "Flag ${flag.name} should default to false")
            }
        }
    }
}




