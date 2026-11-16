# SECRET NOTES — Product, Security and Release Status

## Current product direction
- Android offline private notebook; no account, cloud sync, analytics, ads, telemetry, biometrics or internet access.
- Cosmic / Event Horizon identity: black-hole black, star-silver, pulsar-blue and restrained nebula-violet.
- Launcher icon: user-provided silver planet with orbital ring.

## Delivered foundations in source
- Kotlin + Jetpack Compose Android foundation, minSdk 26, targetSdk 35.
- PIN/password vault using Argon2id, Android Keystore HMAC factor, AES-GCM DEK wrapping, and progressive lockout.
- SQLCipher encrypted database with schema migrations and encrypted note/folder/attachment metadata.
- Note workspace: create/edit, tags, folders, search, pin, archive, trash, restore and permanent deletion.
- Chunked AES-256-GCM attachment encryption, derived per-file keys, authenticated end marker, tamper detection and bounded-memory streaming.
- Built-in text/image/PDF preview paths. External viewing requires a warning and shares only through a constrained FileProvider path.
- 30-second inactivity lock, lock on activity background, and FLAG_SECURE.
- Portable SNB backup engine: Argon2id-derived archive key, AES-GCM authenticated outer archive, wrapped data key, encrypted DB and ciphertext attachments, staged restore output.

## Known limits / not yet release-ready
- Rich text editing is currently a Markdown-assisted plain text editor, not a full rich-text document model.
- Attachment preview supports plain text, common raster images and first page of PDF. Other binary formats need an OS viewer; external handoff leaves a decrypted temporary cache file until cleanup.
- Backup engine is implemented as a storage primitive. Full end-user export/import flow, verified database validation before atomic install, and UX recovery paths remain to be integrated and tested.
- Search executes against SQLCipher data while unlocked; no plaintext index.
- 30-second timer is based on UI interaction and lifecycle stop; complete touch/keyboard/IME/activity coverage needs device QA.
- No proof of hardware-backed Keystore on every device. Recovery is intentionally impossible if the credential is lost.
- App release signing, physical-device QA, manifest/dependency audit, ABI / 16 KB page-size review and independent security review remain open.

## CI test coverage
- Debug APK assembly and JVM unit tests.
- Android emulator instrumentation tests for database round-trip, ciphertext-at-rest check, wrong-key rejection, folders, search, attachment metadata and trash restore.
- Attachment cipher unit tests for multi-chunk round-trip, tampering and empty-file terminal marker.
- Backup tests for archive round-trip, wrong password and tampering.
- CI results must be checked against the latest exact feature HEAD before marking release-ready.

## No-network policy
The app does not declare INTERNET permission. Backup is manual and local via Android document providers. The Android system document provider handles the user-selected destination; the application itself does not upload data.