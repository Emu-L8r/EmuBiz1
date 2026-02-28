package com.example.databaser.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher

object KeystoreHelper {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun ensureKeyPair(alias: String) {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(alias)) {
                val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .setKeySize(2048)
                    .build()
                kpg.initialize(spec)
                kpg.generateKeyPair()
            }
        } catch (t: Throwable) {
            Log.e("KeystoreHelper", "Failed to ensure key pair: ${t.message}", t)
            throw t
        }
    }

    fun wrapWithPublicKey(alias: String, data: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getCertificate(alias)?.publicKey
            ?: throw IllegalStateException("Public key not found for alias: $alias")
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, entry)
        return cipher.doFinal(data)
    }

    fun unwrapWithPrivateKey(alias: String, ciphertext: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val priv = keyStore.getKey(alias, null)
            ?: throw IllegalStateException("Private key not found for alias: $alias")
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, priv)
        return cipher.doFinal(ciphertext)
    }
}

