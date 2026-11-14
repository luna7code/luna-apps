# FLY SECRET — Phase 0.6
## Crypto Integration Feasibility & Native Boundary Design

**Status:** DESIGN COMPLETE — IMPLEMENTATION BLOCKED  
**Baseline:** `security/fly-secret-crypto-dependency-evaluation-0.5`  
**Phase branch:** `security/fly-secret-crypto-integration-feasibility-0.6`  
**Decision:** `libsignal` remains a conditional evaluation candidate only. No production dependency is selected or approved.

> This document is an engineering feasibility and boundary-design artifact. It is not legal advice, an upstream support commitment, a cryptographic audit, a compatibility test, or evidence of a completed technical spike. No application code, native integration, build, or test was run in this phase.

---

## 1. Executive decision

Phase 0.6 defines how FLY SECRET could evaluate a vetted 1:1 E2EE implementation behind a narrow React Native native boundary, without exposing private identity keys, signed/pre-keys, root keys, chain keys, message keys, ratchet state, or Recovery Kit secrets to JavaScript.

The primary candidate remains Signal `libsignal` because it is the closest protocol fit and documents Java and Swift surfaces. It remains **BLOCKED** for two independent reasons:

1. **Legal blocker:** upstream repository metadata identifies `AGPL-3.0-only`. FLY SECRET has no recorded written legal opinion covering the intended mobile distribution, proprietary application, modifications/linkage, and hosted-service model.
2. **Upstream/support blocker:** the upstream project explicitly says use outside Signal is unsupported and APIs/implementations may change without notice. No written response or stable third-party integration commitment has been obtained.

There is also an architectural feasibility gate: verify that the pinned upstream release can support FLY's identity, device lifecycle, persistence, message envelope, and custom ciphertext relay without importing assumptions that require Signal's own service infrastructure. The public existence of Java/Swift bindings does not prove this.

**No-go rule:** do not fetch or pin a production dependency, write integration code, start a spike, or change application architecture until the legal and architecture owners explicitly clear the blockers and authorize a bounded spike. This phase only prepares the plan and boundary contract.

## 2. Scope and non-goals

### In scope
- Legal and upstream conditions that must be answered before integration.
- Architecture compatibility questions for `libsignal`.
- Isolated, disposable Android/Kotlin and iOS/Swift spike plans.
- A platform-neutral React Native native-module contract.
- Key custody, state ownership, error handling, lifecycle and test evidence requirements.
- Admission criteria for a future technical spike.

### Explicitly out of scope
- Production application code or native module implementation.
- Adding `libsignal`, Matrix SDK, Rust crates, Maven/CocoaPods/SPM dependencies, or generated bindings to the app.
- Running a technical spike, compiling native artifacts, benchmarking, fuzzing, or executing tests.
- Selecting a production crypto library or closing ADR D-01.
- Groups/MLS, calls/media E2EE, Web/Desktop crypto implementation, or decentralization.
- TOR MODE, which remains excluded from product, architecture, UI, configuration, tests and roadmap.

Decentralization remains future-only. No new decentralized component or dependency is proposed.

## 3. Baseline facts and confidence

The Phase 0.5 dossier recorded the following source-based findings:

| Topic | Recorded finding | Phase 0.6 treatment |
|---|---|---|
| Protocol | Signal Protocol implementation, including Double Ratchet; Rust internals | Strong candidate fit, not FLY-validated |
| Android | Java surface and published Android artifacts are documented | Kotlin feasibility still untested |
| iOS | Swift surface is documented | Swift feasibility still untested |
| License | AGPL-3.0-only | Hard legal blocker |
| Third-party support | Upstream states external use is unsupported | Hard maintenance/support risk |
| API stability | APIs and bridge layers may change without notice | Exact version and upgrade ownership required |
| FLY integration | No FLY-specific integration audit or spike exists | Must not imply compatibility |

These are inherited findings, not fresh legal advice or current upstream guarantees. Before any spike, re-check the exact immutable tag/commit, license files and dependency metadata. Do not infer a stable API contract from current Signal client usage.

## 4. Blocking conditions — must be resolved first

### 4.1 Legal gate L-01 — AGPL-3.0-only

Obtain a written opinion from counsel experienced in AGPL, mobile app distribution, native linking/FFI, modifications, and hosted services. The opinion must address, at minimum:

- Distribution of Android APK/AAB and iOS application binaries.
- Whether and how the intended linking/embedding model affects source-code obligations.
- Treatment of modifications, wrappers, generated bindings and native bridge code.
- Whether the proprietary React Native application and other modules can remain under the intended licenses.
- Hosted-service/network interaction obligations and the boundary between distributed client and server components.
- Notices, source-offer/source-access obligations, corresponding source scope and delivery process.
- Transitive dependencies, bundled native artifacts, build tooling and license compatibility.
- Whether the intended commercial, open-source, or mixed distribution model is acceptable.

**Evidence required:** dated written counsel memo, reviewed dependency/license inventory, and explicit product-owner acceptance of any obligations.

**Do not:** assume that putting `libsignal` behind Kotlin/Swift avoids AGPL obligations; assume AGPL automatically relicenses every unrelated component; or treat this document as legal clearance.

### 4.2 Upstream gate U-01 — support and API contract

Before integration, request clarification through appropriate public upstream channels:

1. Is third-party use permitted in practice, even though it is explicitly unsupported?
2. Is there a documented supported integration surface for non-Signal clients?
3. Which Android Java and iOS Swift APIs are considered intended surfaces?
4. What compatibility/versioning guarantees, if any, apply to those surfaces?
5. What security advisory and vulnerability disclosure process applies to downstream integrators?
6. Are there restrictions or assumptions around service-side protocol support, account registration, key publication, device linking, or message formats?
7. What migration notice can downstream integrators reasonably expect for breaking changes?

**Evidence required:** archived upstream response or a documented absence of response, plus an explicit FLY risk-acceptance decision. Silence is not approval. A statement that third-party use is unsupported remains a material operational risk even if legal review passes.

### 4.3 Architecture gate A-01 — protocol and service compatibility

A security architect must map the exact pinned library revision to FLY's existing architecture and answer:

- Can FLY use the protocol state machine without depending on Signal-operated identity, directory, key-transparency, message-relay or account services?
- Which protocol operations and data structures are exposed by the exact Android/iOS APIs?
- Can FLY maintain its own opaque FLY ID, independent device identifiers, key directory and key-transparency proof workflow?
- Can per-device sessions, device revocation, re-establishment, replay/reorder handling and concurrent session updates be implemented safely?
- What state must be persisted atomically, and what are the crash/recovery semantics?
- Can the native layer own all secret-bearing state without exporting it through the RN bridge?
- Are there protocol-version, serialization, pre-key replenishment, skipped-key, or migration constraints?
- What happens when a device is revoked, restored, reinstalled, or loses local state?
- Can the custom relay carry only authenticated ciphertext and minimum routing metadata without changing protocol security properties?

Any required FLY-specific cryptographic protocol modification is a rejection condition. Do not fork or modify protocol cryptography to make the integration fit.

### 4.4 Supply-chain gate S-01

Before a spike is authorized, record:

- Immutable upstream tag and commit SHA.
- Exact Maven/CocoaPods/SPM/Cargo artifacts, versions and checksums.
- Root and transitive SPDX identifiers and license texts.
- Native ABI/architecture coverage and minimum OS/SDK requirements.
- Build provenance, reproducibility limits, signing and artifact verification.
- Security advisories, known issues and audit scope mapped to the exact revision.
- Ownership and SLA for dependency updates and emergency security patches.

No mutable branch reference such as `main` is an acceptable dependency pin.

## 5. Proposed native boundary

### 5.1 Security ownership model

**React Native / TypeScript owns:**
- Presentation, navigation and user interaction.
- Non-secret orchestration and validation of UI-level inputs.
- Display of message text after the user has unlocked the app and authorized access.
- Opaque conversation/device references and message delivery state.
- Ciphertext transport through the networking layer.

**Native security module owns:**
- Protocol library instance and all protocol state.
- Identity private keys, signed pre-keys, one-time pre-keys and device private keys.
- Root keys, sending/receiving chain keys, message keys and skipped-message keys.
- Session creation, ratchet advancement, encryption, decryption and state transitions.
- Secure persistence, atomic state commits, key rotation and local secret deletion.
- Biometric/PIN authorization integration and native lock-state enforcement.
- Recovery-secret processing, if separately approved; Recovery Kit secrets must never be bridged to JS.

**Backend owns only:**
- Opaque FLY ID/device routing metadata, bounded directory records and transparency artifacts.
- Ciphertext envelopes and delivery/expiry metadata.
- Encrypted file blobs and opaque object references.
- No plaintext, private keys, ratchet secrets, Recovery Kit secrets or media decryption keys.

### 5.2 Boundary invariants

1. No API returns private key bytes, secret key handles, root/chain/message keys, ratchet snapshots, skipped-key material, or Recovery Kit secrets to JavaScript.
2. No secret is serialized into JSON, Redux/Zustand state, AsyncStorage, logs, analytics, crash reports, clipboard, deep links or navigation parameters.
3. JS never selects cryptographic primitives, nonces, KDF parameters, ratchet counters, or protocol state transitions.
4. Native code accepts high-level intent and bounded protocol inputs; it does not expose a generic signing/decrypt/raw-key API.
5. Secret-bearing buffers are minimized, zeroized where the implementation permits, and never retained in JS-owned buffers. Zeroization guarantees must be verified, not assumed.
6. Native persistent state is versioned, authenticated, rollback-aware where feasible, and updated atomically with ciphertext/outbox state.
7. A bridge timeout or JS reload must not cause duplicate ratchet advancement or inconsistent session state.
8. Every operation is scoped to an authorized local account/device and a specific session reference.
9. Errors exposed to JS are typed and sanitized; they do not contain secret bytes, serialized protocol state or sensitive internal dumps.
10. Native API version and protocol dependency version are separately tracked.

### 5.3 Proposed API surface — conceptual, not implemented

Names below are illustrative contract labels, not claims about existing `libsignal` methods.

| Operation | Input visible to JS | Output visible to JS | Native-only behavior |
|---|---|---|---|
| `getCryptoStatus()` | none | readiness, lock state, storage health, API version | Inspect native state; never return key material |
| `initializeLocalDevice()` | non-secret account/device context | opaque local device reference, public registration bundle | Generate and persist private identity/device material |
| `publishableKeyBundle()` | opaque local device reference | explicitly public, protocol-required bundle | Select public pre-key material; private parts remain native |
| `processPeerKeyBundle()` | opaque session intent, validated public peer bundle | opaque session reference, trust/status result | Authenticate peer material and create session state |
| `encryptMessage()` | opaque session reference, plaintext payload, idempotency token | ciphertext envelope, opaque message reference | Encrypt and atomically advance/persist ratchet |
| `decryptMessage()` | opaque session reference, ciphertext envelope | plaintext payload, delivery/security metadata | Authenticate, decrypt and atomically persist ratchet |
| `getSessionStatus()` | opaque session reference | status, peer/device reference, last state-change category | Read sanitized metadata only |
| `revokePeerDevice()` | peer/device reference, explicit user confirmation | operation result | Apply protocol/device lifecycle policy; invalidate affected trust |
| `lockCrypto()` / `unlockCrypto()` | user authorization intent | success/failure, lock state | Gate native secret access; use OS protected storage |
| `deleteLocalCryptoState()` | explicit confirmation token | deletion result | Destroy local secret state and related protected records |

**Important plaintext boundary:** message composition and rendering currently require the UI to handle plaintext. This design therefore permits plaintext payloads to cross the bridge for encrypt/decrypt operations, but it strictly forbids secret key and ratchet-state export. The bridge must use short-lived, bounded payloads; avoid debug logging and persistent JS caching; clear references promptly; and document that a compromised JS runtime can read displayed plaintext. If the product requires plaintext never to enter JS, that is a separate native-rendering architecture decision and is not silently assumed here.

### 5.4 Prohibited API patterns

Do not expose methods such as:
- `exportPrivateKey()`, `getRootKey()`, `getChainKey()`, `getMessageKey()`, `exportRatchetState()`.
- Generic `sign(bytes, keyId)`, `deriveKey(...)`, `decryptRaw(...)` or caller-selected algorithm APIs.
- JS-managed ratchet counters, pre-key private material, or skipped-message key collections.
- Secret-bearing debug dumps, test-only exports compiled into release, or generic native storage read/write APIs.

Opaque references are identifiers, not bearer authorization tokens. Every native call must re-check local authorization and ownership.

## 6. Android spike plan — Kotlin

**Workspace:** disposable isolated repository/worktree, not the production app. No changes to app source, Gradle production configuration, lockfiles or release pipeline.

### A-Android-1 — artifact and API inspection
- Pin an immutable candidate release only after gates L-01/U-01/A-01/S-01 are cleared.
- Inspect published Maven coordinates, POM metadata, checksums, licenses, transitive dependencies and ABI packaging.
- Confirm minSdk, compileSdk, JDK, NDK, Kotlin/Java compatibility and supported ABIs against the approved FLY device matrix.
- Inspect exact public Java API signatures and threading/lifecycle requirements; do not guess method names from README examples.

**Evidence:** dependency manifest, API inventory, artifact hashes, compatibility matrix and unresolved-gap list.

### A-Android-2 — native-only lifecycle prototype
- Implement a throwaway Kotlin host harness, not an app feature.
- Instantiate the candidate through its documented Android surface.
- Exercise create/open/close session lifecycle with test identities and deterministic fixtures.
- Keep private and ratchet state inside the native process and protected storage abstraction.
- Simulate process death, app restart, concurrent requests, duplicate idempotency tokens and storage write failure.

**Evidence:** reproducible harness instructions, state-transition traces with secrets redacted, crash/restart results and failure analysis.

### A-Android-3 — bridge contract probe
- Expose only the conceptual operations in §5.3 through a minimal React Native native module.
- Add instrumentation asserting that no secret-bearing type can be returned or serialized.
- Inspect JS heap/logging/crash paths using synthetic test secrets only.
- Test bridge cancellation, JS reload, duplicate requests, oversized payload rejection and native lock transitions.

**Evidence:** API contract test report, bridge serialization review and negative tests proving prohibited fields are absent.

### A-Android-4 — storage and platform security
- Evaluate Android Keystore use for wrapping/protecting native persisted state, subject to exact library requirements.
- Verify backup exclusions, app data clear behavior, reinstall behavior, biometric lock policy and screen/recents handling.
- Test atomic persistence and recovery after process kill or low-storage failure.
- Record hardware-backed availability as device-dependent; do not promise uniform hardware-backed guarantees.

**Evidence:** storage threat analysis, device matrix, backup inspection and failure-mode report.

## 7. iOS spike plan — Swift

**Workspace:** separate disposable Xcode project/worktree. No production app, signing, provisioning, release workflow or App Store configuration changes.

### I-iOS-1 — artifact and API inspection
- Pin the same protocol release/revision as Android where technically supported.
- Inspect Swift package/CocoaPods source, checksums, license files, transitive dependencies and XCFramework slices.
- Confirm deployment target, architectures, Swift/Xcode toolchain and memory/lifecycle requirements.
- Inspect exact Swift API signatures and ownership semantics from the pinned revision.

**Evidence:** dependency manifest, API inventory, artifact hashes and platform compatibility matrix.

### I-iOS-2 — native-only lifecycle prototype
- Build a disposable Swift harness around the documented Swift surface.
- Exercise local identity initialization, session setup, encrypt/decrypt and persistence lifecycle with synthetic test identities.
- Keep all private and ratchet state within Swift/native ownership.
- Simulate app termination, background/foreground transitions, storage failures and duplicate operations.

**Evidence:** harness instructions, redacted state traces and failure analysis.

### I-iOS-3 — bridge contract probe
- Expose the same platform-neutral operation semantics as Android through a minimal RN native module.
- Verify no secret-bearing objects, Data buffers or serialized state are returned to JS.
- Test bridge cancellation, JS reload, concurrency, bounded payloads and sanitized error mapping.
- Review Swift ARC ownership, temporary buffer lifetime and copies across bridge serialization.

**Evidence:** contract conformance report, memory ownership review and negative serialization tests.

### I-iOS-4 — protected storage and lifecycle
- Evaluate Keychain accessibility classes and device-lock behavior against the chosen threat model.
- Assess Secure Enclave only for operations it actually supports; do not assume it can store or execute arbitrary protocol state.
- Verify backup/migration behavior, device restore, reinstall and local deletion semantics.
- Define behavior when Keychain items survive app reinstall or become inaccessible after device passcode changes.

**Evidence:** storage matrix, restore/reinstall tests and documented residual risks.

## 8. Cross-platform spike acceptance criteria

A future spike may pass only if all criteria below are evidenced on both platforms:

| ID | Acceptance criterion | Required evidence |
|---|---|---|
| SPIKE-01 | Same pinned protocol revision and compatible message/session semantics | Version/SHA manifest and interoperability fixtures |
| SPIKE-02 | No private or ratchet secret crosses RN boundary | Bridge schema review, serialization negative tests, runtime inspection |
| SPIKE-03 | Session state survives normal restart and fails safely on interrupted writes | Crash/restart and atomicity report |
| SPIKE-04 | Duplicate/reordered/replayed messages handled according to protocol | Deterministic protocol test vectors |
| SPIKE-05 | Multi-device identity and revocation semantics map to FLY model | Device lifecycle design review and tests |
| SPIKE-06 | Public bundle authenticity and key-change handling are explicit | Key verification/transparency integration review |
| SPIKE-07 | No secret-bearing logs, analytics or crash payloads | Instrumentation review and synthetic-secret scan |
| SPIKE-08 | Errors are typed, bounded and sanitized | Contract tests |
| SPIKE-09 | Payload sizes, concurrency and backpressure are bounded | Stress/negative tests |
| SPIKE-10 | Dependency provenance and licenses are fully inventoried | SBOM, checksums, license report |
| SPIKE-11 | Android/iOS packaging and minimum OS/device support are acceptable | Build artifacts and compatibility matrix |
| SPIKE-12 | Upgrade/rollback and state migration strategy is safe | Version migration and rollback analysis |
| SPIKE-13 | No custom cryptography or protocol fork is required | Independent architecture review |
| SPIKE-14 | Performance meets product budgets without weakening security | Measured p50/p95/p99 latency, memory and binary size |

A spike is not a cryptographic audit. Passing this table only supports a separate decision request; it does not authorize production adoption.

## 9. Threats and failure modes to explicitly test later

- JS bridge accidentally serializes a secret-bearing native object.
- Native module exposes a generic storage or key-export escape hatch.
- Ratchet advances but ciphertext/outbox persistence fails, causing unrecoverable state divergence.
- Concurrent encrypt/decrypt calls race and overwrite session state.
- Duplicate bridge retry causes duplicate state transition.
- App restore or backup rolls protocol state backward.
- Device revocation does not invalidate old sessions or trust state.
- Peer key change leaves stale Verified UI state.
- Pre-key exhaustion or replenishment failure creates a denial-of-service or unsafe fallback.
- Native exception/crash dump contains key material or plaintext.
- Dependency update silently changes API behavior or state serialization.
- Android ABI or iOS slice mismatch produces platform-specific fallback behavior.
- Lock/unlock transitions permit background access while UI is locked.
- Plaintext remains in JS state, logs, notifications, screenshots or crash reporting longer than intended.

All test scenarios must be mapped to the existing Red-Team catalog and assigned explicit status only when executed. This phase executes none.

## 10. Decision register update

| Decision | Phase 0.6 disposition |
|---|---|
| D-01 — exact 1:1 crypto implementation | **OPEN / BLOCKED** — no library selected |
| D-09 — React Native/native boundary | Boundary contract proposed; implementation and platform compatibility unverified |
| D-11 — native API and storage | Proposed ownership model; exact APIs and persistence remain open |
| Legal approval | **BLOCKED** — written counsel opinion not present |
| Upstream third-party support | **BLOCKED / UNKNOWN** — no written upstream clarification present |
| Android feasibility | Not tested; spike plan only |
| iOS feasibility | Not tested; spike plan only |

No ADR is closed by this document. Matrix `vodozemac` and Matrix Rust SDK remain non-equivalent alternatives; selecting either would require reopening protocol and architecture decisions. No fallback to custom cryptography is permitted.

## 11. Authorization checklist before any technical spike

All boxes are mandatory. A checked box means evidence exists and has been reviewed, not that someone intends to do it.

- [ ] Written AGPL-3.0 legal opinion approved by product owner.
- [ ] Upstream support/API clarification archived; residual unsupported-use risk explicitly accepted or candidate rejected.
- [ ] Exact immutable upstream revision and artifacts selected for evaluation.
- [ ] Protocol/service compatibility review signed by security architect.
- [ ] Native boundary contract approved by mobile and security owners.
- [ ] Android/iOS device and OS support matrix approved.
- [ ] Isolated spike scope, owner, timebox and stop conditions approved.
- [ ] Synthetic test data and secret-handling procedures approved.
- [ ] SBOM, provenance and license review process ready.
- [ ] Explicit written authorization to begin the spike.

Until then: no dependency addition, no application code, no integration, no builds or tests.

## 12. Red-Team and project guardrails

- Existing Red-Team catalog contains **27 unique scenario IDs**.
- All 27 remain **NOT_RUN**. Phase 0.6 did not execute or mark any test as passed.
- Existing release gates remain in force; NOT_RUN/BLOCKED never count as PASS.
- TOR MODE remains excluded.
- Decentralization remains future-only.
- No PR created; `main` must remain unchanged.
- No private signing key, keystore or Recovery Kit secret is requested or stored in this phase.

## 13. Phase completion and next decision

**Phase 0.6 deliverable:** a legal/architecture blocker register, platform-isolated spike plans, and a narrow RN/native boundary contract.

**Phase status:** documentation design complete; feasibility not demonstrated; implementation admission blocked.

The next valid action is to obtain the legal and upstream evidence, close the architecture compatibility review, and then request explicit authorization for the isolated spike. If legal review rejects AGPL or upstream risk is unacceptable, stop the libsignal path and reopen candidate/protocol selection rather than silently substituting another library.

---

## References

- Phase 0.5 dependency evaluation dossier: `docs/fly-secret/architecture/phase-0.5/FLY_CRYPTOGRAPHIC_DEPENDENCY_EVALUATION_PHASE_0.5.md`
- Phase 0.2 ADRs: `docs/fly-secret/architecture/phase-0.2/FLY_ARCHITECTURE_DECISION_RECORDS_PHASE_0.2.md`
- Phase 0.3 security blueprint: `docs/fly-secret/architecture/phase-0.3/FLY_ARCHITECTURE_CONSOLIDATION_SECURITY_BLUEPRINT_PHASE_0.3.md`
- Red-Team test catalog: `docs/fly-secret/red-team/v1.0/FLY_RED_TEAM_TEST_CASES.md`
- Release gates: `docs/fly-secret/red-team/v1.0/FLY_SECURITY_RELEASE_GATES.md`
- Upstream libsignal repository: https://github.com/signalapp/libsignal
- Upstream README: https://github.com/signalapp/libsignal/blob/main/README.md
