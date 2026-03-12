package com.emul8r.bizap.auth

import android.content.Context
import android.content.SharedPreferences
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
 * Uses Mockk to stub [SharedPreferences] so no Android framework is needed.
 */
class PINStorageTest {

    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockContext: Context
    private lateinit var storage: PINStorage

    // Backing map that simulates the SharedPreferences store
    private val prefData = mutableMapOf<String, String?>()

    @Before
    fun setUp() {
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } answers {
            val key = firstArg<String>()
            val value = secondArg<String?>()
            prefData[key] = value
            mockEditor
        }
        every { mockEditor.remove(any()) } answers {
            prefData.remove(firstArg<String>())
            mockEditor
        }
        every { mockEditor.apply() } just Runs

        every { mockPrefs.getString(any(), null) } answers { prefData[firstArg<String>()] }
        every { mockPrefs.contains(any()) } answers { prefData.containsKey(firstArg<String>()) }

        storage = PINStorage(mockContext)
    }

    // ── isPINSet ──────────────────────────────────────────────────────────────

    @Test
    fun `isPINSet returns false when no PIN stored`() {
        assertFalse(storage.isPINSet())
    }

    @Test
    fun `isPINSet returns true after setupPIN`() {
        storage.setupPIN("1234")
        assertTrue(storage.isPINSet())
    }

    // ── setupPIN ──────────────────────────────────────────────────────────────

    @Test
    fun `setupPIN stores hash, not plaintext`() {
        storage.setupPIN("1234")
        val storedHash = prefData["pin_hash"]
        assertFalse(storedHash == "1234", "PIN should not be stored as plaintext")
    }

    @Test
    fun `setupPIN produces different hash for same PIN each call (random salt)`() {
        storage.setupPIN("1234")
        val hash1 = prefData["pin_hash"]
        val salt1 = prefData["pin_salt"]

        prefData.clear()

        storage.setupPIN("1234")
        val hash2 = prefData["pin_hash"]
        val salt2 = prefData["pin_salt"]

        // Different salts should produce different hashes for the same PIN
        assertFalse(salt1 == salt2, "Salts should differ between calls")
        assertFalse(hash1 == hash2, "Hashes should differ when salts differ")
    }

    @Test
    fun `setupPIN returns success`() {
        val result = storage.setupPIN("1234")
        assertTrue(result.isSuccess)
    }

    // ── verifyPIN ─────────────────────────────────────────────────────────────

    @Test
    fun `verifyPIN returns true for correct PIN`() {
        storage.setupPIN("1234")
        val result = storage.verifyPIN("1234")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun `verifyPIN returns false for wrong PIN`() {
        storage.setupPIN("1234")
        val result = storage.verifyPIN("9999")
        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    @Test
    fun `verifyPIN returns false when no PIN is stored`() {
        val result = storage.verifyPIN("1234")
        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    // ── clearPIN ──────────────────────────────────────────────────────────────

    @Test
    fun `clearPIN removes stored PIN`() {
        storage.setupPIN("1234")
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
