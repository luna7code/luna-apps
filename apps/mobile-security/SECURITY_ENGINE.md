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
- Per-app local fingerprint: package ID, label, version, last update time, signing certificate SHA-256 and digest of requested/granted permission state.
- Scan report encrypted with AES-GCM using an Android Keystore key.
- Room history bounded to the latest 90 reports.

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
The APK is staged in app cache, with a 150 MB input limit, analyzed locally, and deleted afterwards. This is static manifest/metadata triage, not dynamic sandboxing, full code decompilation, malware signature scanning, or proof of signer trust.

### 4. LINK MIRROR
Offline URL-structure inspection:
- Plain HTTP, unusual URI schemes, user-info masking, Punycode, raw IPv4 hosts, URL shorteners, unusually deep subdomains, non-standard ports, and encoded path separators.
No network request is made; domain reputation, DNS, redirects, and page contents are not checked. A clean structural result is not a safety verdict.

### 5. PURA GUARD
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
- Still required: install/run on the target Pura 70, verify notification listener and Accessibility discovery on-device, measure scan duration/battery/memory, exercise APK X-RAY with benign and intentionally malformed samples, and test WorkManager while screen-off / power-saver / app-swiped-away.
- No malware signature database or dynamic sandbox is currently implemented.

## Next milestones

1. Complete CI verification and resolve all compiler, test and lint findings.
2. Add local scan history browser with retention and report export controls.
3. Add a signed offline threat package format with anti-rollback and explicit import confirmation.
4. Add APK signing lineage / installed-package certificate comparison and safer exported-component permission inspection.
5. Add guided response actions that open precise Android settings pages without attempting to change another app's grants silently.
6. Establish a persistent private release signing key and versioned APK release process.
