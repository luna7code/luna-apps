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


## Development update — 2026-11-13

- Change Sentinel distinguishes Android-verified forward certificate rotation from an unrelated signer replacement and a historical signer.
- Historic reports without signer-lineage metadata remain explicitly unverified when signer identity changes.
- Offline threat-feed matching filters by package and signer before hashing; APK SHA-256 is computed at most once per package only when required by a surviving indicator.
- This development phase is validated through GitHub Actions before it is eligible for the main branch. Physical Huawei Pura 70 testing and private release signing remain separate gates.


## APK X-RAY expansion (development branch)

- Added bounded offline extraction of HTTP(S) URL literals and capability-related strings from DEX and selected text-like APK entries.
- Added grouped indicators for shell/process execution, dynamic code loading, Accessibility, notification access, SMS, screen capture, overlays, package installation, device identifiers, and networking APIs.
- Added explicit scan limits and truncation reporting; findings are labeled as context indicators, not proof of behavior.
- Expanded the APK X-RAY screen with extracted URLs and string findings.
- Added JVM tests for URL extraction, indicator grouping, ignored archive entries, and bounded reads. CI validation is pending for the final branch head.
- No APK installation or execution. No INTERNET permission, telemetry, or cloud upload added.

## APK X-RAY expansion

- Added bounded ZIP inventory and ELF header inspection for native .so entries.
- Added detection of nested APK/split candidates, unsafe archive paths, high compression ratios, and truncated scans.
- Added best-effort binary AndroidManifest parsing for package/SDK values, declared custom permission protection levels, intent filters, and App Link autoVerify declarations.
- Added UI sections for native ABI/ELF information, archive warnings, manifest permissions and intent filters.
- Added tests for ELF architecture, invalid ELF data, nested APK detection, malformed ZIP files, and malformed binary manifests.
- Static-only: no extraction to persistent storage, installation, execution, INTERNET permission, telemetry, or cloud upload.