# SECRET NOTES — Security and Storage Architecture

## Product security contract
- Standalone local notebook. No account, cloud sync, network service, analytics, ads, crash telemetry or remote configuration.
- No biometric authentication. Unlock uses only the user PIN or alphanumeric password.
- Recovery is intentionally unavailable: losing the credential means losing access to the encrypted data.
- Manual encrypted backup only. The backup password is the app credential at the time of export.
- Android backup is disabled and the application does not declare INTERNET permission.

## Credential and key hierarchy
- Argon2id v1.3 defaults: 64 MiB, 3 iterations, 1 lane, 16-byte random salt, 32-byte result.
- An installation-bound Android Keystore HMAC-SHA256 key derives a device factor from the Argon2id output.
- The random 256-bit Data Encryption Key (DEK) is wrapped with AES-256-GCM; the PIN is not used directly as the SQLCipher key.
- The vault record stores only KDF parameters, salt, nonce and authenticated wrapped DEK. No plaintext PIN or DEK is persisted.
- Credential input is processed off the main thread; char arrays and byte arrays are cleared best-effort. Compose text state uses immutable Strings temporarily, so complete in-memory erasure is not guaranteed.
- Failed unlock attempts: five attempts trigger progressive delay, beginning at 30 seconds and capped at 15 minutes. State integrity is protected with an Android Keystore HMAC.
- PIN/password defaults in the current implementation are 8 digits or 10 alphanumeric characters; these were engineering defaults rather than separately confirmed product policy.
- Keystore-backed HMAC material may be invalidated by device reset, app reinstall, OS/secure-lock changes or hardware behavior. There is no recovery mechanism.

## SQLCipher database
- Engine: `net.zetetic:sqlcipher-android:4.17.0@aar` with `androidx.sqlite:sqlite:2.6.2`.
- minSdk 26 is above the library's Android API 23+ minimum.
- Uses the vault's random 256-bit DEK.
- SQLCipher encrypts database pages, schema, indexes and data. Notes, titles, tags, folder names/IDs, timestamps, pin/archive/trash state and attachment metadata are all in the encrypted database.
- Database pragmas: `cipher_memory_security = ON`, `secure_delete = ON`, foreign keys, DELETE journaling.
- Schema version 4 migration is transactional. Versions:
  - v1: notes table.
  - v2: folder reference and tags.
  - v3: pin/archive/trash state, folders and note indexes.
  - v4: attachments metadata and note index.
- No plaintext FTS or metadata sidecar exists. Search runs against the encrypted database while unlocked.
- Database connection is opened after successful unlock and closed when the vault locks.

## Attachment encryption
- Attachments are stored under opaque UUID-derived filenames in app-private no-backup storage.
- Each file has a fresh random salt and a per-file key derived from the vault DEK using HKDF-SHA256.
- Payload is streamed in 256 KiB chunks by default (bounded 16 KiB–1 MiB). Each chunk uses AES-256-GCM with its own random 96-bit nonce.
- AAD authenticates format version, attachment ID, chunk index, chunk length and final-marker state.
- An authenticated terminal record detects truncation. Unexpected trailing bytes are rejected.
- Encrypted attachment metadata (display name, MIME type, size, note ID) lives in SQLCipher.
- Import writes to a temporary encrypted file and only renames into place after a successful full stream. Built-in preview decrypts into private cache only after all authentication tags and terminal marker verify.
- Built-in preview covers text, common raster images and first PDF page. Other file types require explicit external-app warning. External handoff creates a plaintext cache copy restricted by FileProvider. It is cleaned up best-effort after 24 hours on a subsequent launch; third-party viewers may retain their own copies.

## Privacy and inactivity
- `FLAG_SECURE` blocks normal screenshots and screen recording on supported Android paths.
- User inactivity timeout: 30 seconds.
- Returning from a temporary system picker/background excursion before timeout preserves the session; inactivity continues to count while away.
- Screen-off event immediately closes the database and clears the in-memory vault session.
- Lifecycle, keyboard and IME behavior still require real-device QA.
- Sensitive notifications are not implemented.

## Portable SNB backup
- Export uses a SQLCipher DB snapshot and copies only encrypted attachment files.
- The vault DEK is wrapped with a distinct subkey derived from the Argon2id backup key.
- Entire archive payload is AES-256-GCM encrypted and authenticated, including ZIP entries and manifest. Header authenticates fixed KDF parameters, salt and nonce as AAD.
- Restore decrypts to a private staging directory; validates the manifest and entry paths; rejects duplicate/unexpected entries; unwraps the DEK; and only then calls the install coordinator.
- The coordinator opens the staged SQLCipher DB with the restored DEK, runs `PRAGMA integrity_check`, verifies the notes table and schema version, copies files into same-volume staging paths, and closes the existing DB connection.
- It preserves rollback copies of the existing database and attachments. It swaps staged files and rewraps the restored DEK under the current device's Keystore key only after the current vault credential has been verified. On failure it restores the prior DB/files and the gate reopens the previous vault.
- Import prompts separately for the archive password (PIN when the backup was made) and current vault password, allowing a user to restore an older archive after changing the current PIN.
- This design requires emulator and physical-device restore/tamper tests before release.

## CI / QA requirements
- Debug and release variants must assemble.
- JVM unit suite covers Argon2id, lockout, password policy, vault wrapping, migrations, attachment streaming/tampering and backup archive round-trip/tampering.
- Android emulator suite covers SQLCipher round-trip, wrong-key rejection, ciphertext-at-rest check, note/folder/search/trash and attachment metadata.
- CI must inspect the merged APK manifest for no INTERNET permission, `allowBackup=false`, launcher icon and FileProvider.
- Remaining release work: ensure instrumentation coverage of full restore coordinator; verify lockout persistence across process/app-data edge cases; confirm 16 KiB page compatibility and ABI splits; run physical-device QA; inspect dependencies/native libs; configure release signing; conduct independent security review.

## Deletion semantics
- Notes moved to trash remain encrypted until manually emptied.
- Permanent deletion removes DB rows and related ciphertext files.
- `secure_delete` is enabled, but flash wear-leveling, filesystem journaling, device backups outside this app and storage-controller behavior mean byte-level secure erasure cannot be guaranteed.
