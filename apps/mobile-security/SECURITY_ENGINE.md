# PRO SEC — architecture and security-engine status

## Product position and release state

PRO SEC is a privacy-first Android security audit and pre-install triage application. It is not represented as a full antivirus or a guarantee that a device is clean. Ordinary third-party Android applications cannot inspect other apps' private memory, bypass Android app isolation, or enumerate every privileged/system process without root or privileged platform integration.

Distribution target: private APK only. The current repository configuration remains version 0.3.0 and the CI artifact is a debug APK. A stable private release still requires a persistent release-signing key stored outside this repository and a separate release-build verification cycle. Do not treat a debug artifact as a release.

The primary device target is Huawei Pura 70. Physical-device verification has not yet been completed.

## Compose interface

- Shared Material 3 dark theme and monochrome palette: black background, near-black elevated surfaces, white primary content, restrained gray borders, muted amber/red severity accents.
- Main overview: scan state, severity counts, scan metrics, consent flow, monitoring opt-in, exposure index and evidence-seal state.
- Intelligence screen: control surfaces, change timeline, APK X-RAY, LINK MIRROR, input-method trust, device integrity and Pura Guard.
- Cards use a common border/surface system and a compact hierarchy intended for the phone form factor.
- The app interface is Russian-first; branded module names such as APK X-RAY and CHANGE SENTINEL remain product labels.

## Device audit engine

The scanner uses Android PackageManager and system settings exposed to ordinary applications.

- Installed-package inventory, application labels, version codes, last-update time and install-source metadata.
- SHA-256 fingerprints for current signing certificates.
- Digest of requested/granted permission state.
- Separate interpretation of sensitive permissions merely requested versus permissions reported as granted.
- Heuristics for a bounded list of recognized remote-support products. Their presence is an indicator to review, not evidence of malware.
- Inventory of enabled Accessibility services, notification listeners, active device administrators and active VPN transport.
- Declared Accessibility/notification-listener services are distinguished from services actually enabled in Android settings.
- Debuggable third-party app flags and selected sensitive permissions.
- Device integrity observations: secure lock-screen state and public build tags. ADB / Developer Options state is explicitly unavailable to ordinary third-party apps and is not reported as disabled.
- Scan duration and app count are written to the report for device-specific performance tracking.

Severity labels and the Exposure Index describe observed attack-surface indicators. They are not a malware probability. Requested permissions, declared services, API references and known remote-support packages may be legitimate and require context.

## APK X-RAY

APK X-RAY analyzes a user-selected APK locally without installing or executing it.

- Package ID, version, version code, min/target SDK and file size.
- Requested permissions and manifest flags such as debuggable, testOnly, backup and cleartext traffic.
- Exported Activity, Service, Receiver and Provider components; the screen shows the declared component permission information available from PackageManager.
- SHA-256 of the selected APK and current signer certificates.
- If the same package is installed, compare the candidate APK with Android's signing information for the installed package.
- On Android API 28+, use SigningInfo's platform-verified certificate history for single-signer packages. The history is in original-to-current rotation order. The comparison distinguishes exact match, platform-verified forward rotation, historical signer, mismatch and unknown. Multi-signer packages are compared as exact signer sets because Android does not provide a rotation history for that model.
- On older supported Android API levels, only legacy signing information is available; no proof-of-rotation inference is made.
- Bounded DEX API-reference scan for Accessibility, notification access, device administration, screen capture, dynamic DEX loading, SMS, call log, contacts, package installation, VPN, overlays, clipboard, camera, location, sensors and other selected APIs.
- DEX analysis is capped at 24 DEX files and 96 MiB of uncompressed DEX data. The user sees the scan count, byte count, duration and any truncation reason.
- Selected APK input is capped at 150 MiB, staged in app cache and deleted after analysis.

A certificate chain match is not a reputation check for the publisher. A historical signer may indicate an older package or downgrade and needs review. DEX references show potential capabilities, not execution or intent. APK X-RAY is static inspection, not a dynamic sandbox, full decompiler or exhaustive malware scanner. Obfuscation, packers, native code and dynamically downloaded code can evade these heuristics.

## Signed offline threat database

PRO SEC supports a manually imported, signed offline indicator feed. No network fetch is performed.

- The user imports an RSA X.509 SubjectPublicKeyInfo PEM public key from local storage.
- The app validates the key structure and requires RSA 2048 bits or stronger.
- Before pinning, the user must explicitly confirm that the displayed SHA-256 public-key fingerprint was compared against a value obtained through an independent trusted channel.
- The private signing key remains offline and is never stored in the app or this repository.
- Feed signature is SHA256withRSA over exact UTF-8/LF-framed content. Metadata, package names, optional APK/signer SHA-256 digests, severity, confidence, title and detail are strictly validated.
- Feed size is capped at 4 MiB and indicator count at 20,000. Duplicate IDs, invalid hashes, malformed fields, unknown keys, altered content and older versions are rejected.
- Imported feed data is encrypted locally with AES-GCM and written using AtomicFile. The saved version counter blocks ordinary import rollback.
- Matching can require package ID only, or the stronger APK/signer hashes. APK files are hashed only for packages whose feed entries require an APK digest; the digest is computed once per package scan.
- A match is an indicator requiring review, not proof of malicious execution.

No public-key trust root or threat feed is bundled by default. The feature remains inactive until the user pins the actual publisher key and imports a feed signed by that key. Full rollback protection against restoration of an older complete application-data image or device/Keystore compromise is not possible for an ordinary app.

The exact feed format is documented in `THREAT_DATABASE_FORMAT.md`.

## Change Sentinel and history integrity

- Change Sentinel compares package install/removal, version/update metadata, signer fingerprints, permission-state digest and newly appearing audit findings.
- A valid empty package inventory is compared normally; a missing inventory field is treated as unavailable data, not as an empty list.
- Reports are encrypted with AES-GCM using an Android Keystore key. The payload codec uses a 12-byte nonce and a 128-bit authentication tag.
- Each current report is separately sealed with HMAC-SHA256 using another Android Keystore key and references the previous tag.
- Adjacent history links within the retained window are verified; legacy-to-new-chain transition is allowed only when the new chain starts with an empty predecessor.
- Unreadable/malformed Room rows remain visible in the timeline with a failed integrity state rather than silently disappearing.
- If the previous report cannot be decrypted or its seal is invalid, the next scan does not trust it as a Change Sentinel baseline. It starts a new chain and adds an explicit finding.
- Room history is capped at 90 records; the overview reads a bounded recent window and the timeline displays the latest eight.

The seal can detect payload modification and broken links in retained history. It cannot prove that the entire database was never deleted or rolled back to an older complete valid copy. Loss/rotation of Android Keystore keys can produce unverifiable seals without proving malicious tampering.

## Other analysis modules

### LINK MIRROR
Offline URL structure review: scheme, user-info masking, Punycode, raw IPv4, URL shorteners, unusually deep subdomains, non-standard ports and encoded separators. It does not fetch DNS, reputation, redirects, page content or remote resources.

### CONTROL MAP / INPUT TRUST
Shows system control surfaces visible to the app: Accessibility, notification listeners, device administrators and VPN transport. Lists enabled input methods and the selected keyboard. A keyboard or enabled service is not proof of keylogging or malicious behavior.

### EXPOSURE INDEX
Explainable, bounded score from current findings and severity. It is not a probability of compromise or a safety guarantee. Recommendations open Android settings or app details; PRO SEC does not silently revoke another app's permissions.

### PURA GUARD
Device guidance for Huawei background reliability. It gives user-controlled routes and instructions for App launch, background activity and battery settings. It cannot override EMUI power management or guarantee exact-time WorkManager execution.

## Privacy and platform permissions

- No INTERNET permission.
- No telemetry, account, cloud sync or upload of reports.
- Scan results, pinned public key and imported feed stay in local app storage.
- Reports and feed payload are encrypted locally; Android backup and device-transfer inclusion are disabled.
- APK X-RAY reads only a user-selected file and removes its temporary cache copy after parsing.
- LINK MIRROR never opens a submitted URL.
- QUERY_ALL_PACKAGES is declared because full installed-app inventory is a core private-sideload feature.
- ACCESS_NETWORK_STATE is used only to inspect whether the active default network reports VPN transport.

## Background scheduling

Daily monitoring is opt-in. Selecting it is a pending choice; the periodic WorkManager task is scheduled only after confirmation in the scan consent dialog. Turning an already enabled schedule off cancels it immediately. WorkManager's 24-hour periodic interval is approximate; Doze, battery saver and Huawei EMUI background controls can delay or suspend execution.

## Validation state and remaining device work

CI run 132 passed debug APK assembly, JVM unit tests, Android Lint and debug APK artifact upload. CI does not verify actual Huawei device behavior or prove malware detection completeness.

Still required on the physical Pura 70:
- cold/warm launch and Compose interaction review at the device's actual resolution/font scale;
- scan duration, CPU, memory/PSS and battery measurements with a representative app inventory;
- WorkManager behavior after screen-off, Doze, battery saver, app swipe-away and device restart;
- Accessibility, notification listener, input method, device-admin and VPN observations against Android settings;
- APK X-RAY tests with benign, malformed, multi-signer, key-rotation and split APK samples;
- signed feed import with a real publisher key and known test indicators;
- local database corruption/recovery scenarios and history-chain behavior after key loss.

No dynamic sandbox, cloud reputation service, bundled malware signatures, root inspection, or guarantee against sophisticated spyware is currently implemented.
