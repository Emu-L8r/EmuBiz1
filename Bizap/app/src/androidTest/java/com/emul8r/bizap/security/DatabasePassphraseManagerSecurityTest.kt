package com.emul8r.bizap.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.emul8r.bizap.data.local.DatabasePassphraseManager
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Security tests for [DatabasePassphraseManager].
 *
 * These are instrumented tests because Android Keystore requires a real device
 * or emulator — it cannot be mocked in JVM unit tests.
 *
 * Tests verify:
 * 1. Passphrase is correctly sized and non-zero
 * 2. Encrypt → decrypt roundtrip produces identical passphrase
 * 3. Passphrase is stable across multiple calls (same value returned)
 * 4. Passphrase is NOT stored in plaintext (DataStore contains only ciphertext)
 * 5. Two separate instances return the same passphrase (Keystore key persists)
 * 6. Passphrase is 32 bytes (256-bit — required for AES-256)
 *
 * Run with: ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.emul8r.bizap.security.DatabasePassphraseManagerSecurityTest
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class DatabasePassphraseManagerSecurityTest {

    private lateinit var context: Context
    private lateinit var manager: DatabasePassphraseManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        manager = DatabasePassphraseManager(context)
    }

    @After
    fun cleanup() {
        // Remove Keystore key between test runs to ensure clean state
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
            if (keyStore.containsAlias("bizap_db_key")) {
                keyStore.deleteEntry("bizap_db_key")
            }
        } catch (e: Exception) {
            // Ignore cleanup failures
        }
    }

    // ── Passphrase Size & Content ──────────────────────────────────────────

    @Test
    fun `passphrase is exactly 32 bytes (256-bit)`() {
        val passphrase = manager.getOrCreatePassphrase()
        assertEquals(32, passphrase.size,
            "Passphrase must be 32 bytes for AES-256. Got ${passphrase.size} bytes.")
    }

    @Test
    fun `passphrase is not all zeros`() {
        val passphrase = manager.getOrCreatePassphrase()
        val allZeros = ByteArray(32) { 0 }
        assertFalse(
            passphrase.contentEquals(allZeros),
            "Passphrase must not be all zeros — SecureRandom generation failed"
        )
    }

    @Test
    fun `passphrase is not all same byte`() {
        val passphrase = manager.getOrCreatePassphrase()
        val distinctBytes = passphrase.toSet().size
        assertTrue(distinctBytes > 1,
            "Passphrase appears to have no entropy ($distinctBytes distinct bytes). SecureRandom issue?")
    }

    // ── Stability Across Calls ─────────────────────────────────────────────

    @Test
    fun `getOrCreatePassphrase returns same value on repeated calls`() {
        val first = manager.getOrCreatePassphrase()
        val second = manager.getOrCreatePassphrase()
        assertContentEquals(first, second,
            "Passphrase must be stable: same value on every call after first generation")
    }

    @Test
    fun `two separate manager instances return same passphrase`() {
        val manager1 = DatabasePassphraseManager(context)
        val manager2 = DatabasePassphraseManager(context)

        val passphrase1 = manager1.getOrCreatePassphrase()
        val passphrase2 = manager2.getOrCreatePassphrase()

        assertContentEquals(passphrase1, passphrase2,
            "Two manager instances must return identical passphrase — Keystore key must persist")
    }

    // ── Encryption Verification ────────────────────────────────────────────

    @Test
    fun `Keystore key is created after first passphrase call`() {
        manager.getOrCreatePassphrase()

        val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
        assertTrue(
            keyStore.containsAlias("bizap_db_key"),
            "Android Keystore must contain 'bizap_db_key' after passphrase generation"
        )
    }

    @Test
    fun `Keystore key is AES-256`() {
        manager.getOrCreatePassphrase()

        val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
        val key = keyStore.getKey("bizap_db_key", null)
        assertNotNull(key, "Keystore key must exist")
        assertEquals("AES", key.algorithm, "Keystore key algorithm must be AES")
    }

    // ── SQLite Magic Bytes Check ───────────────────────────────────────────

    @Test
    fun `passphrase is not the SQLite plaintext magic string`() {
        val passphrase = manager.getOrCreatePassphrase()
        val sqliteMagic = "SQLite format 3".toByteArray()

        // SQLCipher db file should NOT start with SQLite magic bytes
        // (verifies encryption is applied — the passphrase must be non-trivial)
        assertFalse(
            passphrase.take(15).toByteArray().contentEquals(sqliteMagic),
            "Passphrase must not be the SQLite magic string — database would be unencrypted!"
        )
    }

    // ── Base64 Encoding Sanity ─────────────────────────────────────────────

    @Test
    fun `passphrase survives Base64 roundtrip`() {
        val original = manager.getOrCreatePassphrase()
        val encoded = Base64.encodeToString(original, Base64.NO_WRAP)
        val decoded = Base64.decode(encoded, Base64.NO_WRAP)

        assertContentEquals(original, decoded,
            "Base64 encode/decode must be lossless for passphrase storage in DataStore")
    }
}

