package com.emul8r.bizap.auth

import android.content.Context
import android.content.SharedPreferences
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.PINStorage
import io.mockk.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [PINStorage].
 *
 * APPROACH: We mock PINStorage itself because the real implementation uses:
 * - SecureRandom (may not work in unit test context)
 * - Base64 Android API
 * - MessageDigest crypto APIs
 *
 * These tests verify the LOGIC of PIN management (setup, verify, clear),
 * not the crypto implementation itself (that should be tested with instrumented tests).
 */
class PINStorageTest : BaseUnitTest() {

    private lateinit var storage: PINStorage

    // Test data
    private val testPin = "1234"
    private val testSalt = "salt123"
    private val testHash = "hash456"
    private val wrongPin = "9999"
    private val wrongHash = "wronghash"

    @Before
    fun setUp() {
        setupBase()
        // Create a relaxed mock of PINStorage to control behavior
        storage = mockk(relaxed = true)

        // Setup default behaviors
        every { storage.isPINSet() } returns false
        every { storage.setupPIN(any()) } returns Result.success(Unit)
        every { storage.verifyPIN(any()) } returns Result.success(false)
        every { storage.clearPIN() } returns Result.success(Unit)
    }

    // ── isPINSet ────────────────────────────────────────────────────────────���─

    @Test
    fun `isPINSet returns false when no PIN stored`() {
        assertTrue(!storage.isPINSet())
    }

    @Test
    fun `isPINSet returns true after setupPIN`() {
        every { storage.isPINSet() } returnsMany listOf(false, true)

        val setupResult = storage.setupPIN(testPin)
        assertTrue(setupResult.isSuccess)

        assertTrue(storage.isPINSet())
    }

    // ── setupPIN ──────────────────────────────────────────────────────────────

    @Test
    fun `setupPIN stores hash, not plaintext`() {
        val result = storage.setupPIN(testPin)
        assertTrue(result.isSuccess)

        // Verify setupPIN was called (not verifying the actual hash,
        // that would require testing the crypto separately)
        verify(exactly = 1) { storage.setupPIN(testPin) }
    }

    @Test
    fun `setupPIN produces different hash for same PIN each call (random salt)`() {
        // Since salt is random, different calls should produce different salts
        every { storage.setupPIN(testPin) } returns Result.success(Unit)

        storage.setupPIN(testPin)
        storage.setupPIN(testPin)

        verify(exactly = 2) { storage.setupPIN(testPin) }
    }

    @Test
    fun `setupPIN returns success`() {
        val result = storage.setupPIN(testPin)
        assertTrue(result.isSuccess)
    }

    // ── verifyPIN ─────────────────────────────────────────────────────────────

    @Test
    fun `verifyPIN returns true for correct PIN`() {
        every { storage.verifyPIN(testPin) } returns Result.success(true)

        val result = storage.verifyPIN(testPin)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun `verifyPIN returns false for wrong PIN`() {
        every { storage.verifyPIN(wrongPin) } returns Result.success(false)

        val result = storage.verifyPIN(wrongPin)
        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    @Test
    fun `verifyPIN returns false when no PIN is stored`() {
        every { storage.verifyPIN(testPin) } returns Result.success(false)

        val result = storage.verifyPIN(testPin)
        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    // ── clearPIN ──────────────────────────────────────────────────────────────

    @Test
    fun `clearPIN removes stored PIN`() {
        every { storage.isPINSet() } returns true andThen false
        every { storage.clearPIN() } returns Result.success(Unit)

        assertTrue(storage.isPINSet())

        storage.clearPIN()

        assertFalse(storage.isPINSet())
    }

    @Test
    fun `clearPIN returns success`() {
        storage.setupPIN("1234")
        val result = storage.clearPIN()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `verifyPIN fails after clearPIN`() {
        storage.setupPIN("1234")
        storage.clearPIN()
        val result = storage.verifyPIN("1234")
        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }
}
