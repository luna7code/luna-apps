# PRO SEC Signer-Lineage and Scan-Efficiency Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make signer-change analysis rotation-aware and avoid redundant APK hashing during offline threat-feed matching.

**Architecture:** Android PackageManager supplies current signer certificates and, on API 28+, the platform-verified certificate history. The scanner stores that data in each report; Change Sentinel interprets it through the existing pure comparator. A pure threat-indicator matcher performs package/signer filtering before requesting an APK digest.

**Tech Stack:** Kotlin, Android PackageManager/SigningInfo, Compose UI, JUnit 4, GitHub Actions, Android Gradle Plugin.

**Spec:** `docs/superpowers/specs/2026-11-13-pro-sec-lineage-and-scan-efficiency.md`

## Global Constraints

- Preserve Russian-only interface, monochrome dark palette and private-APK distribution.
- No INTERNET permission, telemetry, account, cloud, or upload.
- Min SDK 26; only API 28+ may use SigningInfo proof-of-rotation history.
- Legacy reports remain readable; unknown lineage is never silently trusted.
- Do not install or execute user-selected APK files.
- Preserve the offline threat database and Android Keystore data format.
- Do not publish or sign a release APK in this phase.

## Review Focus

- **Legacy snapshots with comma-separated current signer digests:** parse as a signer set, but do not invent a historic lineage.
- **API 26–27 devices:** use current signer data only and report rotation as unknown.
- **Multi-signer packages:** require exact signer-set matching; do not apply single-signer rotation semantics.
- **Indicators with APK digest but mismatched signer:** do not read/hash the installed APK.
- **Several indicators for one package requiring APK digest:** compute SHA-256 once and reuse the result.

---

### Task 1: Capture and compare signing lineage

**Files:**
- Modify: `apps/mobile-security/src/main/java/com/lunacode/mobilesecurity/scanner/DeviceAuditScanner.kt`
- Modify: `apps/mobile-security/src/main/java/com/lunacode/mobilesecurity/intelligence/ChangeSentinel.kt`
- Modify: `apps/mobile-security/src/main/java/com/lunacode/mobilesecurity/intelligence/SigningLineage.kt`
- Test: `apps/mobile-security/src/test/java/com/lunacode/mobilesecurity/intelligence/ChangeSentinelTest.kt`
- Test: `apps/mobile-security/src/test/java/com/lunacode/mobilesecurity/intelligence/SigningLineageTest.kt`

**Interfaces:**
- Scanner writes `signerSha256`, `signerHistorySha256`, and nullable `multipleSigners` into each app inventory record.
- `SentinelApp` reads those values; missing fields in legacy reports remain unknown.
- `SigningLineageComparator.compare(installed, candidate)` returns the existing explicit `SigningLineageResult` values, extended only if an additional result is required.

- [ ] **Step 1: Write failing Change Sentinel tests** for verified forward rotation (INFO event), historical signer (HIGH event), unrelated signer (HIGH event), and missing legacy lineage (MEDIUM/unverified event).
- [ ] **Step 2: Run the focused JVM tests** and verify that they fail for the expected missing behavior.
- [ ] **Step 3: Extend signer capture** in `DeviceAuditScanner.kt`: use `SigningInfo.apkContentsSigners`, `signingCertificateHistory`, and `hasMultipleSigners()` on API 28+; retain legacy current-signature capture below API 28.
- [ ] **Step 4: Extend `SentinelApp` parsing and comparison** in `ChangeSentinel.kt`, preserving compatibility with old `signerSha256`-only snapshots.
- [ ] **Step 5: Update lineage comparison** so `multipleSigners == null` is treated as unknown when a changed signer set would otherwise imply rotation.
- [ ] **Step 6: Run all JVM unit tests** and verify that rotation, downgrade and legacy cases pass.
- [ ] **Step 7: Commit** the lineage-aware scanner and regression tests.

### Task 2: Bound offline indicator hashing

**Files:**
- Create: `apps/mobile-security/src/main/java/com/lunacode/mobilesecurity/intelligence/ThreatIndicatorMatcher.kt`
- Create: `apps/mobile-security/src/test/java/com/lunacode/mobilesecurity/intelligence/ThreatIndicatorMatcherTest.kt`
- Modify: `apps/mobile-security/src/main/java/com/lunacode/mobilesecurity/scanner/DeviceAuditScanner.kt`
- Modify: `apps/mobile-security/src/main/java/com/lunacode/mobilesecurity/intelligence/KillerIntel.kt`

**Interfaces:**
- `ThreatIndicatorMatcher.match(packageName: String, signerDigests: Set<String>, indicators: List<ThreatIndicator>, apkSha256Provider: () -> String?): List<ThreatIndicator>`.
- The provider is invoked at most once and only when a package/signer-compatible indicator contains a non-null APK digest.

- [ ] **Step 1: Write failing matcher tests** for package mismatch, signer mismatch, package-only match, one digest-required match and multiple digest-required matches.
- [ ] **Step 2: Run the focused tests** and verify the expected missing matcher behavior.
- [ ] **Step 3: Implement the pure matcher** with package and signer filtering before any digest request.
- [ ] **Step 4: Wire the matcher into the installed-app scanner** and APK X-RAY; reuse an already calculated APK digest in APK X-RAY.
- [ ] **Step 5: Run all JVM unit tests** and verify the digest-provider call-count assertions.
- [ ] **Step 6: Commit** the bounded matcher and tests.

### Task 3: Verify and document the phase

**Files:**
- Modify: `apps/mobile-security/SECURITY_ENGINE.md`
- Modify: `apps/mobile-security/RELEASE_NOTES_0.3.0.md`

- [ ] **Step 1: Document** signer-lineage interpretation, legacy uncertainty, multi-signer handling and APK-hash cost controls.
- [ ] **Step 2: Run the full GitHub Actions workflow** for debug assembly, JVM unit tests and Android Lint.
- [ ] **Step 3: Inspect the workflow job logs and artifacts** for the exact branch commit; record successes and any uncompleted checks.
- [ ] **Step 4: Review the diff** for privacy regressions, incorrect severity claims, and accidental release/secret material.
- [ ] **Step 5: Commit** documentation updates.

## Review Focus Acceptance

- Legacy history missing the `multipleSigners` field is not treated as verified proof-of-rotation.
- Older Android releases never claim platform-verified rotation.
- Multi-signer packages never use the single-signer proof-of-rotation path.
- APK digest providers are not called for package/signer mismatches.
- APK SHA-256 is computed at most once per package for matching indicators.
