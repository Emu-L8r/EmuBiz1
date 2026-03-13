# Security

This document describes the security measures implemented in Bizap.

## Database Encryption

The local SQLite database is encrypted using **SQLCipher 4.5.4**.

### Passphrase Management

The database passphrase is managed by `DatabasePassphraseManager`:

1. A 32-byte cryptographically random passphrase is generated using `SecureRandom` on first launch.
2. The passphrase is **never stored in plaintext**. It is encrypted with an AES-256-GCM key stored in the **Android Keystore** (hardware-backed on supported devices).
3. Only the encrypted ciphertext and IV are persisted in private `SharedPreferences`.
4. On subsequent launches, the ciphertext is decrypted by the Keystore key and passed to SQLCipher to open the database.

### Verification

To confirm the database is encrypted, inspect the database file on a rooted device or emulator:

```bash
adb shell run-as com.emul8r.bizap ls databases/
adb shell run-as com.emul8r.bizap cat databases/bizap-db | head -c 20 | xxd
```

An encrypted database will show binary data, not the `SQLite format 3` magic string.

## PIN Authentication

User access is protected by a PIN:

- The PIN is **never stored in plaintext**.
- A 16-byte random salt is generated using `SecureRandom`.
- The PIN is hashed using **SHA-256** combined with the salt.
- Only the salt and hash are stored in SharedPreferences.
- Verification re-hashes the input and compares to the stored hash.

**Lockout**: After 5 failed attempts, the user is locked out for 30 seconds.

**Session timeout**: The authenticated session expires after 5 minutes of inactivity.

## Release Build Hardening

- ProGuard/R8 obfuscation enabled for release builds.
- Database migration failures cause a loud crash in release (no silent data destruction).
- No hardcoded secrets in source code.
- API keys passed via `buildConfigField` from environment variables.
