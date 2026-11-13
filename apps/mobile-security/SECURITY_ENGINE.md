# PRO SEC — security engine and killer features

## Product position

PRO SEC is a privacy-first Android security audit and pre-install triage tool. It is not marketed as a guaranteed full antivirus: ordinary third-party Android apps cannot inspect other apps' private memory or bypass platform isolation without privileged access.

Release direction: private APK distribution. Current development artifact is a debug APK; a stable private release must be signed with a persistent release key before installation/update cycles.

## Current audit engine

- Installed-package inventory via Android PackageManager.
- Heuristic flags for a small, explicit set of known remote-support products.
- Review flags for apps requesting multiple sensitive permissions.
- Debuggable-app indicator.
- Enabled Accessibility service inventory from Settings.Secure.
- Active notification-listener inventory from Settings.Secure.
- Active device administrator inventory via DevicePolicyManager.
- Active default-network VPN transport check via ConnectivityManager.
- Per-app local fingerprint: package ID, label, version, last update time, signing certificate SHA-256, installer package provenance, and digest of requested/granted permission state.
- Separate indication for multiple sensitive permissions currently marked as granted, distinct from merely requested permissions.
- Explainable local Exposure Index (0–100) with bounded severity deductions and prioritized remediation actions; explicitly not a malware probability or a guarantee of safety.
- Scan report encrypted with AES-GCM using an Android Keystore key.
- Room history bounded to the latest 90 reports; the Killer Features tab displays the latest eight timeline entries.

## Killer Features tab

### 1. CONTROL MAP
Builds a local map of active Android control surfaces:
- Accessibility services: can inspect screen UI and perform actions within Android's accessibility model.
- Notification listeners: can receive notification content.
- Device administrators: can apply the administrator policies exposed by Android.
- VPN transport: shows whether the active default network uses a VPN.
The map identifies mechanisms and packages where the platform exposes them. It does not infer that a listed app is malicious.

### 2. CHANGE SENTINEL
Compares the current encrypted scan snapshot with the previous snapshot:
- Newly installed and removed packages.
- Version / update-time changes.
- Signing certificate fingerprint changes.
- Changes to requested or granted permission state.
- Installer package information reported by Android for newly discovered packages.
- Newly appearing audit finding IDs.
First scan creates the baseline. A certificate change may be legitimate key rotation; new installation is not proof of malware.

### 3. APK X-RAY
Uses Android PackageManager to inspect a user-selected APK without installing or executing it:
- Package name, version, target/min SDK.
- Requested permissions.
- Exported Activity/Service/Receiver/Provider count.
- Debuggable, backup, and cleartext-traffic manifest flags.
- SHA-256 digest of the APK.
- SHA-256 fingerprints of signing certificates.
- If the same package is already installed, compare the selected APK's current signer certificate with the installed package's current signer and flag a mismatch. Key-rotation lineage validation is not yet included; a legitimate signing-key rotation may require manual review.
The APK is staged in app cache, with a 150 MB input limit, analyzed locally, and deleted afterwards. This is static manifest/metadata triage, not dynamic sandboxing, full code decompilation, malware signature scanning, or proof of signer trust.

### 4. LINK MIRROR
Offline URL-structure inspection:
- Plain HTTP, unusual URI schemes, user-info masking, Punycode, raw IPv4 hosts, URL shorteners, unusually deep subdomains, non-standard ports, and encoded path separators.
No network request is made; domain reputation, DNS, redirects, and page contents are not checked. A clean structural result is not a safety verdict.

### 5. TRUST TIMELINE
Shows the latest eight local scan snapshots with finding counts and newly detected changes. The database retains at most 90 reports; each payload is encrypted locally.

### 6. DEVICE INTEGRITY
Checks whether Android reports a secure lock screen and whether public Build tags contain `test-keys`. It explicitly reports ADB / Developer Options as not observable from an ordinary third-party app: Android's public Settings.Global API returns zero for those keys to third-party callers. It does not pretend that this means debugging is disabled.

### 7. INPUT TRUST
Lists the currently enabled input methods and identifies the selected keyboard from Android's default input method setting. A keyboard can process typed content, but its presence is not proof of keylogging. Users can open Android's input-method settings to review or disable keyboards.

### 8. EXPOSURE INDEX
An explainable score computed from the current findings with bounded deductions by severity. The score only describes observed attack-surface exposure within PRO SEC's available coverage, not malware probability and not a general device safety verdict. Recommendations point to the relevant Android settings or application details screen; the app does not silently revoke permissions.

### 9. PURA GUARD
Device-specific background reliability guide for Huawei Pura 70 / EMUI. It provides user-controlled shortcuts to Android app details and battery optimization settings, plus manual instructions for App launch, Run in background and locking the app in Recents. It does not silently disable battery protections or guarantee exact-time background execution.

## Privacy model

- No INTERNET permission is declared by the app.
- No telemetry, account, cloud sync, or upload of scan results.
- Package inventory, permission fingerprints and reports are stored locally; report payloads are encrypted before Room insertion.
- APK X-RAY copies only a selected APK into app cache for local parsing and deletes the temporary file after analysis.
- LINK MIRROR never opens or fetches the submitted URL.
- Threat intelligence updates are not fetched from a server in strict-privacy mode. A later update path should accept a user-imported signed offline database and verify signature/version before activation.

## Android / Huawei limitations

- Android 11+ filters package visibility by default. PRO SEC declares QUERY_ALL_PACKAGES because broad inventory is core to the audit function.
- ACCESS_NETWORK_STATE is required for the VPN transport query. The app does not declare INTERNET.
- Android can delay WorkManager work under Doze, battery saver and vendor background management. Huawei EMUI may suspend/terminate background apps unless the user adjusts App launch and battery settings. The daily interval is approximate, not a guaranteed alarm.
- Pura Guard gives the user steps and shortcuts only; actual menu names differ by EMUI/HarmonyOS version and region.

## Current validation status

- CI currently builds debug APK, runs local JVM unit tests for URL heuristics, and runs Android Lint.
- CI does not replace physical-device tests.
- Still required: install/run on the target Pura 70, verify notification listener, Accessibility, input method and lock-screen discovery on-device, measure scan duration/battery/memory, exercise APK X-RAY with benign and intentionally malformed samples, and test WorkManager while screen-off / power-saver / app-swiped-away.
- Android's Settings.Global ADB_ENABLED and DEVELOPMENT_SETTINGS_ENABLED are not usable as a third-party detection source: official API documentation specifies that both return 0 to third-party apps. Do not infer ADB-disabled from these values.
- No malware signature database or dynamic sandbox is currently implemented.

## Next milestones

1. Complete CI verification and resolve all compiler, test and lint findings.
2. Add a signed offline threat package format with an app-pinned public key, version anti-rollback, strict schema validation, and explicit import confirmation.
3. Implement deeper APK analysis: signing-certificate lineage validation, exported-component permission inspection, DEX/static indicators, and a known-good signer trust store.
4. Add guided response actions that open precise Android settings pages without attempting to change another app's grants silently.
5. Establish a persistent private release signing key and versioned APK release process; keep the signing key outside GitHub and the APK itself.
