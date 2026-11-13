# PRO SEC 0.3.0 — major security expansion

## New capabilities

- Explainable local Exposure Index with severity breakdown and prioritized actions.
- FORENSIC SEAL: AES-GCM encrypted reports additionally receive an Android Keystore HMAC-SHA256 chain, with history-link verification and safe baseline reset when a previous seal fails.
- Device Integrity checks for secure screen lock and test-keys build metadata.
- Explicit limitation reporting for ADB / Developer Options because Android does not expose their state to ordinary third-party apps.
- Input Trust inventory for enabled input methods and current default keyboard.
- APK X-RAY now performs a bounded DEX API-reference scan for sensitive capabilities in addition to manifest checks.
- Notification listener discovery added to the main audit report.
- Direct remediation links from recommendations to Android settings and app details.
- Expanded consent disclosure for newly collected local audit metadata.

## Existing killer features retained

- CONTROL MAP
- CHANGE SENTINEL
- APK X-RAY
- LINK MIRROR
- TRUST TIMELINE
- PURA GUARD

## Validation still required

- Build, JVM tests and Android Lint in CI.
- Physical Pura 70 verification.
- Battery and background scheduling checks under EMUI.
- APK X-RAY test corpus, including malformed and multi-signer APK files.

This is a private-distribution development build. Do not treat it as a stable release until CI passes and the release variant is signed with the persistent private release key.
