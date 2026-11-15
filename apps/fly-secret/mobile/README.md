# FLY SECRET Android

React Native 0.80 Android application shell.

## Build
From this directory, install dependencies with npm, then run:
- `npx react-native run-android` for a connected development device.
- `gradle -p android assembleDebug` when Gradle 8.14.3 and Android SDK are available.
- APK output: `android/app/build/outputs/apk/debug/app-debug.apk`.

The repository CI workflow builds the debug APK and uploads it as an artifact. The committed source does not include a release signing key. The debug APK is for development/testing only.

No authentication, message transport, cryptographic protocol, E2EE, or production security guarantees are implemented in this shell.