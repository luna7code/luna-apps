# SECRET NOTES — Security and Storage Architecture

## Credentials and vault key
- Numeric PIN or alphanumeric password; no biometrics, no recovery.
- Current enrollment defaults: 8 digits for PIN or 10 alphanumeric characters for password. Release policy can be revisited; these were technical defaults, not an explicit product minimum from the user.
- Argon2id v1.3 uses 64 MiB, three passes, one lane and a 128-bit random salt.
- A random 256-bit DEK is wrapped using AES-256-GCM and a non-exportable Android Keystore HMAC-SHA256 device factor.
- PIN/password and plaintext DEK are not persisted. Memory zeroization is best-effort; Compose input uses immutable strings temporarily.
- After five incorrect attempts, first delay is 30 seconds. Delay increases exponentially and caps at 15 minutes. Retry state is authenticated by a separate Keystore HMAC.
- Credentials are checked off the main thread.

## SQLCipher database
- Engine: `net.zetetic:sqlcipher-android:4.17.0@aar` with `androidx.sqlite:sqlite:2.6.2`.
- Android API 23+ support covers minSdk 26. Pinned 4.17.0 aligns with the project's current Kotlin 2.0.21 build and the artifact's published AndroidX SQLite dependency.
- The vault's random 256-bit DEK is supplied to SQLCipher; the PIN is not the database encryption key.
- SQLCipher encrypts pages, schema, indexes, note text, titles, tags, folders and attachment metadata.
- Database uses cipher memory security, secure delete, foreign keys and DELETE journaling.
- Schema v4 includes notes, folders, flags, trash state, encrypted attachment metadata and indexes.
- Database opens only after vault unlock and closes on manual lock, 30 seconds of inactivity, or application background transition.
- No plaintext sidecar search index is created; search executes against the encrypted database.

## Attachment encryption
- Each attachment is encrypted as independent authenticated chunks using AES-256-GCM.
- A per-file encryption key is derived from the vault DEK using HKDF-SHA256 and a fresh file salt.
- Each chunk has a random 96-bit nonce; AAD binds format version, attachment ID, chunk index, plaintext size and terminal marker.
- An authenticated final marker detects truncated files.
- Files use opaque UUID-based encrypted names; display name, MIME type and size are metadata in SQLCipher.
- Streaming avoids loading whole attachments into memory.

## Privacy layer
- `FLAG_SECURE` blocks normal screenshots and recording surfaces.
- App locks at 30 seconds without interaction and when the activity moves to stopped/background state.
- These controls do not prevent compromised/rooted operating systems or external cameras from observing the screen.

## Remaining release tasks
- Implement SAF attachment import/export UI and ensure attachment ciphertext deletion is coordinated with metadata.
- Rich text/formatting support, refined note navigation and attachment preview.
- Portable authenticated backup/restore with the same PIN as archive password, independent of device Keystore.
- Verify INTERNET permission absence, backup exclusions and dependency graph.
- Execute Android instrumentation tests on emulator and physical device.
- Check native SQLCipher ABI coverage and 16 KB page-size behavior on supported Android 15+ devices.
- Security review and release signing.
