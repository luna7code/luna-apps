# FLY SECRET Android

React Native 0.80 Android client. Phase 1.7 introduces a navigable local-only product shell.

## Current Phase 1.7 scope

- Chats, Contacts, and Security tabs with working in-app navigation.
- Create a contact record from a display name and user-entered FLY ID.
- Start a local conversation and append local-only message records.
- Explicit UI warnings: messages are not transmitted, not persisted across process restarts, and are not E2EE-protected.
- Security status screen reports unimplemented identity, key verification, multi-device, Recovery Kit, and network transport honestly.

## Deliberate security boundaries

This build is a UI/state prototype, **not a secure messenger**. Do not enter real personal, confidential, or secret data. Contact and message state is held in process memory and is lost when the app process exits. No network transport or cryptographic protocol is included. No security guarantee is made.

The Signal/libsignal integration remains blocked pending the legal/upstream decision gate. Do not replace it with custom cryptography. TOR MODE is excluded. Decentralization is future-only and is not a current feature.

## Validation

From this directory:
- `npm install --no-audit --no-fund --no-package-lock`
- `npm test`
- `npm run typecheck`
- `gradle -p android assembleDebug`

The debug APK is for development/testing only. A successful build does not prove runtime correctness; Android launch testing is a separate acceptance gate.
