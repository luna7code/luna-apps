# SECRET NOTES

Standalone, offline-first Android notebook in the `luna-apps` repository. This project is independent of FLY SECRET.

## Product
- Application ID: `com.luna.secretnotes`.
- Kotlin + Jetpack Compose, minSdk 26, targetSdk 35, JDK 17.
- Visual identity: Cosmic / Event Horizon — deep-black space, silver starlight, restrained pulsar-blue and nebula-violet accents.
- Launcher icon: the supplied silver planet with orbital ring.
- PIN/password unlock only. No biometrics, no recovery key, no cloud account, sync, analytics, ads or telemetry.
- The application manifest does not request INTERNET permission; Android backup is disabled.

## Implemented in the current feature branch
- Argon2id credential KDF, Android Keystore HMAC device binding, AES-GCM-wrapped random data key and progressive failed-attempt lockout.
- SQLCipher-encrypted SQLite storage for notes, folders, tags, timestamps, pin/archive/trash state and attachment metadata.
- Notes workspace with create/edit, Markdown-assisted plain text formatting, debounced local saving, search, folders, pin, archive, trash, restore and permanent deletion.
- Streaming attachments with per-file HKDF-derived key, chunked AES-256-GCM authentication, authenticated final marker and tamper/truncation detection.
- Built-in plain-text, raster-image and first-page PDF previews. External viewing requires a warning and uses a constrained FileProvider path. Temporary plaintext exports are deleted after 24 hours on a later app launch (best-effort).
- 30-second inactivity lock, grace period when temporarily leaving the app, immediate lock on screen-off, and FLAG_SECURE.
- Manual encrypted SNB backup export and restore. Backup uses Argon2id + AES-GCM, wraps the vault DEK, validates the restored SQLCipher database, stages files, and rolls back the current DB/attachments if the install fails.

## Build and test
Requires JDK 17, Android SDK Platform 35 and Gradle 8.9.

```bash
cd secret-notes
gradle :app:assembleDebug
gradle :app:assembleRelease
gradle :app:testDebugUnitTest
gradle :app:connectedDebugAndroidTest
```

CI additionally inspects the packaged APK manifest to verify that it has no INTERNET permission, disables Android backup and declares the launcher icon and FileProvider.

## Security caveats
This branch is still under active validation and is not security-certified. Some UI and lifecycle behaviors need physical-device QA; hardware-backed Keystore is device-dependent; external applications can retain files they receive; memory zeroization on the JVM is best-effort; and release signing/independent security review are not complete. Never claim data is unrecoverable after deletion on flash storage: secure deletion is best-effort because of filesystem wear-leveling and storage snapshots.

See [SECURITY_FOUNDATION.md](SECURITY_FOUNDATION.md) and [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) for architecture and design details.
