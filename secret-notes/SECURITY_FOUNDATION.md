# SECRET NOTES — Security Foundation and Encrypted Database

## Credential and key hierarchy
- Unlock: numeric PIN or alphanumeric password; no biometrics.
- Enrollment defaults: 8 digits for PIN or 10 alphanumeric characters for password. These are implementation defaults, not separately user-approved release policy.
- Argon2id v1.3: 64 MiB, 3 passes, 1 lane, 128-bit random salt.
- Random 256-bit DEK is wrapped with AES-256-GCM; Android Keystore HMAC-SHA256 binds the local credential derivation to the installation.
- PIN/password and plaintext DEK are never persisted. Credential inputs are cleared best-effort; Compose temporarily holds immutable Strings.
- Five failed attempts trigger 30 seconds, with exponential increase capped at 15 minutes. Counter integrity is authenticated with a separate Keystore HMAC key.
- No recovery: loss of the credential means loss of access.

## Encrypted database
- Engine: `net.zetetic:sqlcipher-android:4.17.0@aar`, with `androidx.sqlite:sqlite:2.6.2`.
- The app's random 256-bit DEK from `VaultKeyMaterial` is the SQLCipher database passphrase. The database does not use the PIN directly.
- SQLCipher encrypts the database pages, schema, indexes and journal. Titles, bodies, tags, folder IDs, timestamps, pin/archive flags and trash metadata all live inside that encrypted database.
- The SQLCipher native library is loaded explicitly before opening the database.
- The temporary DEK byte array supplied to SQLCipher is cleared immediately after opening. SQLCipher's internal native key lifecycle remains controlled by the library and database connection.
- `cipher_memory_security`, `secure_delete`, foreign-key enforcement and DELETE journaling are enabled.
- No plaintext search index or separate metadata sidecar is created.
- Schema version 3 is applied transactionally: v1 creates notes, v2 adds folder and tags metadata, v3 adds pin/archive/trash fields, folders and indexes.
- Database is opened only after successful vault unlock and closed when the vault is locked.

## Compatibility rationale
SQLCipher for Android supports Android API 23+ and therefore covers this app's minSdk 26. The newer `sqlcipher-android` artifact is used instead of the deprecated `android-database-sqlcipher` project. Version 4.17.0 is paired with AndroidX SQLite 2.6.2 to avoid introducing the newer Kotlin 2.2 runtime metadata into this Kotlin 2.0.21 project.

## Validation status
- JVM unit tests cover migration ordering and supported version transitions.
- Android instrumentation tests cover encrypted round-trip, trash behavior, ciphertext-at-rest checks and wrong-key rejection. They are committed but are not executed by the current GitHub Actions workflow (which runs assembleDebug and JVM unit tests only).
- CI success does not equal real-device security certification.

## Remaining security work
- Encrypted attachments with authenticated streaming/chunking.
- Rich note editor, folders/tags management, search, archive and trash UI.
- 30-second auto-lock, immediate screen-off lock, FLAG_SECURE and clipboard/privacy controls.
- Portable encrypted backup/import with PIN-derived archive key.
- Verify merged manifest/APK has no INTERNET permission, backup exclusions and no network dependencies.
- Real-device instrumentation, Keystore hardware-security-level checks, native SQLCipher ABI/16 KB page-size review and independent security review.
