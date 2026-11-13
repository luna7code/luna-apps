# PRO SEC — signer-lineage and scan-efficiency design

## Intent

Improve the correctness and bounded cost of two existing analysis paths: Change Sentinel's installed-app identity comparison and matching against a verified offline threat feed.

## User outcome

A user should not receive a high-severity alert merely because an app legitimately rotated its signing key with an Android-verified proof-of-rotation lineage. A genuine downgrade or unrelated signer change must remain visible. Scanning should compute an installed APK digest at most once per package and only when at least one relevant threat indicator requires that digest.

## Goals

- Persist Android's current signing-certificate set, verified certificate history and multi-signer state in new scan snapshots.
- Compare signer state using Android-provided lineage data, not only the current certificate fingerprint.
- Preserve compatibility with historic snapshots that have only `signerSha256`.
- Distinguish exact match, verified forward rotation, historical signer, signer mismatch, multi-signer mismatch and insufficient historical data.
- Add a pure threat-indicator matcher that filters by package and signer before invoking the APK digest provider.
- Compute an APK digest once per package scan when multiple indicators require it.
- Add deterministic JVM tests for lineage transitions and digest-provider call counts.
- Keep the Compose result language clear: a legitimate rotation is informational; a historical signer/downgrade or unrelated signer mismatch remains a review-worthy alert.

## Non-goals

- No network reputation requests, cloud lookup, telemetry or account.
- No malware-execution claims from package names or API-reference matches.
- No root-only inspection or dynamic APK sandbox.
- No automatic permission revocation or app removal.
- No release signing or private APK release in this phase.

## Constraints

- Android minSdk remains 26; API 28+ may use `SigningInfo` proof-of-rotation metadata.
- API levels below 28 must not infer proof-of-rotation.
- Existing JSON fields and Room records remain readable; missing lineage fields are unknown, not silently treated as verified.
- Keep the `INTERNET` permission absent.
- The Pura 70 remains the physical-device target.
- The publisher's private RSA key remains offline and outside the repository.

## Approaches considered

1. **Extend the existing scanner and Change Sentinel models (selected).** Keep PackageManager as the source of truth, persist its verified signing history, and reuse the existing pure `SigningLineageComparator`. Lowest migration risk and preserves the current report/Room architecture.
2. Introduce a new certificate registry and a report-schema migration. More extensible, but creates unnecessary state duplication and migration risk before there is a second trust consumer.
3. Keep current signer-only comparison and lower its severity. Rejected because it hides the distinction between verified rotation, downgrade and unrelated signer replacement.

## Design decisions

- Persist current signer SHA-256 values as a deterministic sorted comma-separated set, plus ordered lineage SHA-256 values and a nullable multi-signer flag. The nullable flag preserves uncertainty when comparing legacy records.
- A verified forward rotation produces an informational Change Sentinel event, not a high-severity warning.
- A candidate that uses a historical signer, an unrelated signer, or a mismatched multi-signer set remains a high-priority indicator.
- If historic data cannot establish whether rotation is valid, report an unverified signer change at medium severity; do not claim that rotation is safe.
- Threat matching first narrows by package and signer. It invokes the APK digest provider no more than once and only when a surviving indicator requires an exact APK digest.
- The matcher remains pure Kotlin so it can be tested without Android framework stubs.

## Acceptance criteria

- Existing legacy snapshots without lineage fields continue to parse.
- An Android-verified A→B rotation is not reported as an unrelated high-severity signer mismatch.
- A historical A signer observed after installed B, an unrelated C signer, and a mismatched multi-signer set are still distinguishable.
- Missing lineage metadata yields an explicit unknown/unverified result.
- A signer mismatch prevents unnecessary APK hashing.
- Multiple matching indicators requiring APK SHA-256 cause exactly one digest-provider invocation.
- Full CI passes: debug assembly, JVM unit tests and Android Lint.
- No claim is made that a successful CI run verifies Huawei-specific runtime behavior.
