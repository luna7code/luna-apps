# SECRET NOTES

Standalone Android notes application inside the `luna-apps` repository.

> **Status: foundation scaffold.** This commit establishes the isolated Android application module and CI build. PIN authentication, encrypted persistence, attachments, and the full notes experience are not implemented yet.

## Project boundaries

- Independent Android application; not part of FLY SECRET.
- Application ID: `com.luna.secretnotes`.
- Native Kotlin + Jetpack Compose.
- Local-first direction; no account or network dependency in the initial release.
- All application source and build files live under `secret-notes/`.

## Build

Requires JDK 17, Android SDK Platform 35, and Gradle 8.9.

```bash
cd secret-notes
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

## Security implementation gates

Do not store a PIN or a plain hash of a PIN. Before shipping sensitive data storage, implement and test a documented key hierarchy using Android Keystore, a suitably configured password KDF, per-install random salts, and authenticated encryption (AES-GCM). Define lockout/rate limiting, backup exclusions, screenshot/recents protection, and recovery semantics. The current scaffold does not claim to provide these protections.
