# PRO SEC — security engine notes

## Implemented in the current foundation

- Installed-package inventory via Android PackageManager.
- Heuristic flags for a small, explicit set of known remote-support products.
- Review flags for apps requesting multiple sensitive permissions.
- Debuggable-app indicator.
- Enabled Accessibility service inventory from Settings.Secure.
- Active device administrator inventory via DevicePolicyManager.
- Active VPN transport detection via ConnectivityManager.
- Local scan history in Room; scan payload encrypted with AES-GCM using a key held by Android Keystore.
- Daily periodic audit scheduled with WorkManager and a manual scan queue from the dashboard.
- Dark hybrid interface and a shield-style PRO SEC launcher mark.

## Privacy model

- No INTERNET permission is declared in the application manifest.
- No telemetry, account, cloud sync, or upload of scan results.
- Scan payloads are encrypted before being written to the local Room database.
- Threat intelligence updates are intentionally not fetched from a server in strict-privacy mode. A later update path should accept a user-imported, signed offline database package and verify its signature before activation.

## Android capability boundaries

PRO SEC is an ordinary app and does not have root or privileged system access. It cannot reliably inspect other apps' private memory, all running processes, protected system state, or prove that no hidden remote-control mechanism exists.

An enabled Accessibility service, device administrator, VPN, or known remote-support app is an indicator for user review, not proof of malware. Requested permissions are not proof that a permission is currently granted or actively used.

Android 11+ filters package visibility by default. This build declares QUERY_ALL_PACKAGES because broad app inventory is a core antivirus/security-audit feature. Google Play treats the installed-app inventory as sensitive data and requires the appropriate permission declaration and review; the app must provide prominent disclosure and consent before inventory collection if distributed through Google Play.

## Daily scheduling

WorkManager periodic work is inexact. Android battery optimizations, Doze, device idle state, and system scheduling can delay the daily check. The UI must describe it as an approximate 24-hour schedule, not a guaranteed exact-time alarm.

## Current limitations / next milestones

- Build and instrumentation test verification on CI and a physical Android 12 device.
- Permission and special-access review UI with user-friendly package labels and Settings deep links.
- Decryption and rendering of the latest local scan report.
- Baseline comparison and local change timeline.
- Signed offline threat database format, versioning, rollback protection, and import UX.
- APK certificate, manifest, and component inspection for user-selected APK files.
- More precise VPN package attribution where Android APIs and device vendor behavior permit it.
- Threat scoring with evidence, confidence, and false-positive controls.
