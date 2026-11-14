# FLY SECRET — Cryptographic Dependency Evaluation & Platform Compatibility
- Phase: 0.5
- Version: 0.5
- Base branch: `security/fly-secret-decision-closure-0.4`
- Working branch: `security/fly-secret-crypto-dependency-evaluation-0.5`
- Evaluation snapshot: 2026-11-14
- Status: Dependency decision dossier; no dependency selected
- Scope: 1:1 E2EE protocol/library candidates, licensing, APIs, Android/iOS integration, maintenance, security evidence and selection gates
- Application code: not written
- Prototype / spike: not created or run
- Red-Team scenarios: 27 × `NOT_RUN`
- TOR MODE: excluded
- Decentralization: future-only

> This is a source-based engineering due-diligence dossier, not legal advice, a cryptographic audit, a compatibility test, or evidence that any candidate has been integrated into FLY SECRET. Public repository status and version data are a point-in-time snapshot and must be rechecked against immutable tags/commits before adoption.

---

## 1. Executive conclusion

### 1.1 Decision

**Do not select or pin a production cryptographic dependency in Phase 0.5.**

Keep `libsignal` as the primary 1:1 evaluation candidate, but mark it **CONDITIONAL / BLOCKED** until the AGPL-3.0 license implications and unsupported third-party-use warning are resolved in writing. The official repository exposes Java, Swift and TypeScript interfaces over Rust and is used by Signal's own Android/iOS/Desktop clients, but explicitly says use outside Signal is unsupported and APIs/bridge layers may change without notice. The repository is AGPL-3.0-only. These constraints prevent treating it as a turnkey, supported third-party SDK.

Keep `vodozemac` as a secondary technical comparator, **not a drop-in Signal replacement**. It implements Matrix Olm/Megolm ratchets in Rust and is Apache-2.0, but its protocol ecosystem, handshake/identity semantics and integration model differ. Its standalone mobile binding story is not turnkey; the separate `vodozemac-bindings` repository states that it is no longer actively maintained. A 2022 Least Authority audit exists, but the report left some issues unresolved at that audited revision and excluded high-level application integration and the Olm/Megolm cryptographic design from scope.

The Matrix Rust SDK / `matrix-sdk-crypto` is a third comparator for teams willing to adopt Matrix protocol semantics. It has active Android/iOS FFI paths and Apache-2.0 licensing at the SDK level, but it is a Matrix client SDK and its crypto state machine is not a generic Signal-compatible 1:1 engine for FLY's custom relay protocol.

**No custom protocol, no primitive-level composition, and no legacy libolm adoption.** If legal review rejects libsignal and the team is unwilling to adopt Matrix protocol semantics, Phase 0.5 must not force a selection. Commission a focused candidate search and keep implementation admission blocked.

### 1.2 Recommended next decision

1. Obtain written legal analysis of AGPL-3.0 for the intended FLY SECRET distribution and hosted-service model.
2. Ask the libsignal maintainers, through their documented public support/security channels as appropriate, whether third-party integration is supported or whether a stable integration contract is available. Do not assume a positive answer.
3. Run a non-production integration spike only after approval, using the official Android and Swift surfaces and a minimal native adapter. Do not expose key/ratchet secrets to React Native JavaScript.
4. In parallel, evaluate whether adopting Matrix's Olm protocol and its server/client semantics is acceptable as a product-level architecture change. This is not a library substitution; it would reopen protocol and backend ADRs.
5. Return to the security owner and product owner with evidence. No production dependency is approved by this dossier.

---

## 2. Evaluation scope and method

### 2.1 Required FLY SECRET properties

The selected implementation must support or safely compose with an approved design for:
- asynchronous 1:1 sessions;
- authenticated initial key agreement and identity binding;
- per-message key evolution, forward secrecy and post-compromise recovery;
- multi-device session lifecycle without cloning device private keys;
- bounded handling of delayed, reordered, duplicated and replayed messages;
- crash-consistent state persistence and rollback resistance;
- explicit protocol/version negotiation and downgrade rejection;
- safe key rotation, device revocation and recovery lineage;
- Android and iOS support through maintained native APIs;
- a narrow bridge that does not export private keys or ratchet secrets to JS;
- documented licensing, maintenance, security response and reproducible dependency pinning.

A ratchet implementation alone is not a complete messaging protocol. Identity binding, prekey publication, server behavior, message envelope, device directory, key transparency, recovery, metadata handling and state persistence remain FLY SECRET responsibilities unless explicitly and safely supplied by a selected SDK.

### 2.2 Candidate set

| Candidate | Role in evaluation | Initial disposition |
|---|---|---|
| Signal `libsignal` | Signal-family 1:1 protocol implementation and native APIs | Primary candidate; conditional/blocking license and support issue |
| Matrix `vodozemac` | Olm/Megolm Rust ratchets | Secondary comparator; not wire/protocol drop-in |
| Matrix Rust SDK / `matrix-sdk-crypto` | Higher-level Matrix E2EE state machine and mobile FFI | Alternative only if adopting Matrix semantics |
| Legacy `libolm` | Older Matrix Olm implementation | Reject; deprecated |
| `libsodium`, Noise libraries, RustCrypto primitives | Cryptographic building blocks | Not protocol candidates; do not assemble a bespoke protocol |
| OpenMLS | Group protocol implementation | Not a 1:1 candidate; separate future group evaluation |

### 2.3 Evidence quality labels

- **Documented:** stated in official repository/specification or published audit.
- **Inferred:** engineering implication from the documented integration model; must be validated in a spike.
- **Unknown:** not established by reviewed public evidence; must not be claimed as supported.
- **Not evaluated:** no FLY SECRET build, integration or tests were run.

---

## 3. Candidate A — Signal `libsignal`

### 3.1 Protocol and API surface

Official repository describes `libsignal-protocol` as implementing the Signal Protocol, including Double Ratchet, with underlying implementation in Rust. Public interfaces are exposed as Java, Swift and TypeScript libraries. The repository is used by Signal's own Android, iOS and Desktop clients.

The README explicitly warns:
- use outside Signal is unsupported;
- APIs and implementations can change without notice;
- JNI, C and Node bridge layers can change;
- compatibility expectations for non-bridge APIs are best-effort versioned, not a stable third-party SDK promise.

This is a major product risk for FLY SECRET. The existence of Java and Swift interfaces does not mean the maintainers promise third-party API stability or support.

### 3.2 Licensing

Repository and Cargo metadata identify `AGPL-3.0-only` / GNU AGPLv3.

Implications:
- This is not a permissive dependency.
- Obligations can differ depending on whether FLY distributes client binaries, modifies the library, offers network-accessible functionality, links/combines components, or distributes corresponding source.
- Do not assume that keeping the library in a native module avoids AGPL obligations.
- Do not assume that AGPL automatically requires publication of every unrelated application component; this is a legal interpretation question.
- Obtain a written opinion from counsel familiar with AGPL, mobile distribution and hosted services before integrating or shipping.

**License status: BLOCKER until legal approval.**

### 3.3 Android compatibility

Documented upstream support includes:
- Java API;
- published Signal Maven artifacts including `org.signal:libsignal-android`;
- Android native libraries for `armeabi-v7a`, `arm64-v8a`, `x86` and `x86_64`;
- Android build prerequisites include JDK 21, Android SDK/NDK and Rust targets.

Advantages:
- Best fit with the Phase 0.4 Android-first plan.
- Java/Kotlin native integration is more direct than introducing a custom Rust FFI.
- Signal's own Android client provides a reference consumer, but copying application internals is not a supported SDK contract.

Constraints:
- External use is explicitly unsupported.
- Maven repository and artifact provenance must be checked independently before use.
- Pin immutable version and artifact hashes; do not use dynamic versions.
- Verify minSdk/ABI coverage and native packaging against FLY's actual device matrix.
- Confirm JNI behavior, threading, lifecycle, memory ownership and exception/error mapping.
- The published Android artifacts do not remove the need to protect protocol state and key custody in the FLY native layer.

### 3.4 iOS compatibility

Documented upstream support includes:
- Swift binding over Rust C FFI;
- CocoaPods integration and Swift Package local development;
- prebuilt FFI checksum use and Rust build prerequisites.

Advantages:
- A first-party Swift surface exists.
- The underlying Rust implementation can reduce divergence between Android and iOS protocol engines.

Constraints:
- The README's support warning still applies.
- Swift Package / CocoaPods integration and prebuilt checksum lifecycle need validation for the exact release.
- Confirm supported iOS deployment targets, simulator/device slices, Xcode versions and Apple Silicon build reproducibility.
- Confirm secure persistence semantics and whether FLY can safely use the exposed API without importing Signal-specific account/server assumptions.
- RN will require a narrow Swift native module. Do not pass secret byte arrays through JS.

### 3.5 Maintenance and release activity

At the evaluation snapshot, the official release page showed `v0.106.2` as the latest release, published 2026-11-05. The repository's recent release stream indicates active maintenance. The release number is not itself a quality or security guarantee.

Observed repository metadata also indicates a current Rust toolchain requirement; the workspace manifest reported Rust 1.93.1 at the time reviewed. Recheck the exact tag's toolchain and build requirements before pinning.

Maintenance strengths:
- active upstream release cadence;
- same repository is consumed by Signal clients;
- Java and Swift build instructions and release artifacts are documented.

Maintenance risks:
- API/bridge compatibility is explicitly not guaranteed for third-party use;
- upstream priorities are Signal product needs;
- FLY may need to carry compatibility patches or pin an older version;
- a fork creates a long-term cryptographic maintenance and audit burden.

### 3.6 Security evidence and limitations

The official repository publishes a security policy and receives ongoing development. This dossier did not identify a public repository security advisory listing in the reviewed page; absence of published advisories is not evidence of absence of vulnerabilities.

No FLY-specific integration audit was performed. Do not claim that the whole FLY system is audited because it uses libsignal.

### 3.7 Candidate assessment

**Strengths**
- Closest protocol fit to Phase 0.4's Signal-family direction.
- Java and Swift interfaces are documented.
- Official Android and iOS clients use it.
- Active release cadence.

**Blockers / risks**
- AGPL-3.0-only license.
- Explicitly unsupported third-party use.
- Unstable API and bridge layers.
- FLY-specific identity, directory, transparency, device lifecycle, recovery and relay integration remain unreviewed.

**Disposition:** Primary candidate for a legal and technical feasibility spike only. Not approved for production.

Sources:
- https://github.com/signalapp/libsignal
- https://github.com/signalapp/libsignal/blob/main/README.md
- https://github.com/signalapp/libsignal/blob/main/Cargo.toml
- https://github.com/signalapp/libsignal/releases
- https://github.com/signalapp/libsignal/blob/main/swift/README.md

---

## 4. Candidate B — Matrix `vodozemac`

### 4.1 Protocol and API surface

`vodozemac` is a pure Rust implementation of Matrix Olm (1:1 Double Ratchet) and Megolm (group ratchet), designed as a modern alternative to libolm. It is used in Matrix E2EE.

It is not Signal Protocol and must not be described as an interchangeable implementation of Signal's PQXDH + Double Ratchet stack. Olm has its own handshake, account/session model, serialization, identity semantics and Matrix ecosystem expectations.

### 4.2 Licensing

The crate declares Apache-2.0. This is substantially more permissive than AGPL-3.0 for typical proprietary/commercial distribution, subject to preserving notices and reviewing transitive dependency licenses.

### 4.3 Android and iOS compatibility

The core is Rust. The standalone `vodozemac` crate does not document first-party Java and Swift SDKs comparable to libsignal's surfaces.

The separate `matrix-org/vodozemac-bindings` repository includes language bindings, but its README marks the project **Unmaintained** and says it is no longer actively maintained. Do not base a new mobile product on those bindings without adopting and maintaining them internally.

The Matrix Rust SDK is a more complete alternative:
- its SDK exposes a crypto state machine for Matrix clients;
- its FFI layer has Android `cdylib` and iOS `staticlib` build outputs;
- Apple documentation describes generating an XCFramework for the full SDK or a smaller crypto module.

However, this route brings Matrix-specific API, state and protocol assumptions. It is not merely a thin binding around a generic Signal-compatible ratchet. Adopting it would require revisiting FLY's backend protocol, identity mapping, device model, event semantics and architecture ADRs.

### 4.4 Maintenance and toolchain

The reviewed `vodozemac` changelog showed:
- version `0.11.1` dated 2026-09-30;
- version `0.11.0` dated 2026-09-11;
- the current crate manifest reports Rust 1.96.0 as its minimum Rust version.

This indicates active crate maintenance at the snapshot, but the separate binding repository is unmaintained. The FLY team would need to own and test its native binding layer if using the standalone crate.

### 4.5 Audit history — important qualification

A Least Authority audit report exists, dated 2022-03-30. It reviewed specific vodozemac commits from 2022, not the current 0.11.1 revision.

The report recorded:
- issues A–H as resolved at the audit verification;
- issue I (keys in memory / swap / side-channel exposure) as unresolved;
- issue J (MAC tag length) as unresolved;
- suggestion 8 (configurable retained chain/message keys) as unresolved.

The report explicitly excluded third-party bindings/dependencies unless listed in scope, and excluded high-level Matrix application integration and the cryptographic design of Olm/Megolm. Therefore:
- this audit is useful evidence, not a current certification;
- it does not prove the current version has the unresolved conditions unchanged;
- it does not prove those conditions have been fixed in the current version;
- the current revision needs a remediation mapping and fresh independent review;
- FLY integration and native FFI need separate review.

**Audit status for current v0.11.1: UNKNOWN until revision-by-revision remediation evidence is obtained.**

### 4.6 Candidate assessment

**Strengths**
- Apache-2.0 license.
- Rust implementation.
- Published audit with remediation verification for many findings.
- Active crate releases and strict Rust lint configuration.

**Blockers / risks**
- Matrix Olm/Megolm, not Signal/PQXDH.
- Standalone Android/iOS bindings are not maintained as a supported product interface.
- Matrix Rust SDK path implies a broader protocol/platform architecture adoption.
- Historical audit has unresolved items and narrow scope; current-version mapping required.
- Rust 1.96 minimum toolchain raises build and reproducibility requirements.

**Disposition:** Secondary comparator only. Do not adopt as a drop-in replacement. A protocol change to Matrix Olm would require explicit re-approval of D-01, D-03, D-05, D-07 and related ADRs.

Sources:
- https://github.com/matrix-org/vodozemac
- https://github.com/matrix-org/vodozemac/blob/main/Cargo.toml
- https://github.com/matrix-org/vodozemac/blob/main/CHANGELOG.md
- https://github.com/matrix-org/vodozemac-bindings
- https://github.com/matrix-org/matrix-rust-sdk
- https://github.com/matrix-org/matrix-rust-sdk/blob/main/bindings/apple/README.md
- https://matrix.org/media/Least%20Authority%20-%20Matrix%20vodozemac%20Final%20Audit%20Report.pdf

---

## 5. Candidate C — Matrix Rust SDK / `matrix-sdk-crypto`

### 5.1 What it offers

The Matrix Rust SDK describes `matrix-sdk-crypto` as a standalone encryption state machine with no network I/O, intended for Matrix clients. The SDK is Apache-2.0 and is used by Element X iOS and Android.

Its FFI includes mobile-oriented build outputs:
- Android `cdylib`;
- iOS `staticlib`;
- UniFFI-generated language interfaces.

This is the strongest alternative if the product is willing to adopt Matrix protocol and client semantics, not simply if FLY wants a cryptographic engine.

### 5.2 Fit and incompatibilities

Potential benefits:
- maintained Rust SDK;
- existing mobile binding/build model;
- higher-level encryption state machine rather than a raw ratchet only;
- Apache-2.0 licensing.

Risks:
- Matrix account/device/event/room semantics may conflict with FLY ID, custom message envelope, relay and directory decisions;
- using only the crypto crate may still require Matrix protocol types and state assumptions;
- a full SDK may bring networking, storage, event and server compatibility commitments outside the Phase 0.4 design;
- migration away later would be a protocol migration, not a routine dependency replacement.

### 5.3 Disposition

**Not recommended for the current FLY protocol baseline.** Keep as a strategic alternative only if the product owner is willing to reconsider the FLY backend and protocol around Matrix. No implementation is authorized.

Sources:
- https://github.com/matrix-org/matrix-rust-sdk
- https://github.com/matrix-org/matrix-rust-sdk/blob/main/bindings/matrix-sdk-ffi/Cargo.toml
- https://github.com/matrix-org/matrix-rust-sdk/blob/main/bindings/apple/README.md

---

## 6. Rejected / non-equivalent options

### 6.1 Legacy libolm

Matrix announced libolm deprecation in July 2024 and recommends migration to vodozemac / Matrix Rust SDK. Do not select libolm for a new product.

Source: https://gitlab.matrix.org/matrix-org/olm

### 6.2 libsodium, Noise and primitive libraries

These may provide useful primitives or secure channel constructions, but they are not an approved end-to-end asynchronous messaging product protocol by themselves. Choosing primitives and composing a bespoke identity/prekey/ratchet/device/recovery protocol would move protocol correctness and audit burden onto FLY SECRET.

They may be used only behind a reviewed protocol implementation or in a separately reviewed, narrowly scoped non-protocol utility.

### 6.3 OpenMLS

MLS is a group messaging protocol, not the current 1:1 protocol candidate. It remains a separate future evaluation for groups and must not be used to close D-01.

---

## 7. Comparison matrix

Qualitative ratings are relative to FLY SECRET's current Android-first custom-service architecture. They are not cryptographic strength scores.

| Criterion | libsignal | vodozemac standalone | Matrix Rust SDK crypto |
|---|---|---|---|
| 1:1 protocol fit | **High** — Signal-family | **Medium/Low** — Olm, different protocol | **Low/Medium** — Matrix-specific state machine |
| Android API | **High documented surface** — Java/AAR | **Low** — custom Rust FFI required; separate bindings unmaintained | **High documented FFI path** |
| iOS API | **High documented surface** — Swift | **Low** — custom Rust FFI required | **High documented XCFramework/Swift path** |
| License | **AGPL-3.0-only** | **Apache-2.0** | **Apache-2.0** |
| Third-party support promise | **Explicitly unsupported** | No turnkey mobile SDK support promise | SDK designed for Matrix client integrations |
| API stability | **Low/conditional** — upstream says APIs may change | Rust crate API evolves; wrapper is FLY-owned | SDK versioned; breaking changes still require upgrade discipline |
| Maintenance | Active upstream | Active core crate; standalone bindings unmaintained | Active SDK |
| Security evidence | No FLY integration audit; upstream security process | 2022 external audit, limited scope; current mapping required | SDK security history must be assessed separately; no FLY integration audit |
| Protocol migration cost | Medium if chosen before wire format freezes | High if replacing Signal direction with Olm | Very high if adopting Matrix architecture |
| Current recommendation | Primary feasibility candidate, blocked | Comparator only | Strategic alternative only |

### 7.1 Why libsignal is not an automatic winner

It wins on protocol fit and direct Android/iOS API availability. It loses on a potentially decisive licensing and support constraint. Those are not secondary paperwork issues; they can make the candidate unsuitable for the product's business and maintenance model.

### 7.2 Why vodozemac is not the automatic permissive substitute

Apache-2.0 does not make two different protocols interchangeable. Olm and Signal-family handshakes, identity binding, key distribution, state formats, server semantics and interop are different. Choosing vodozemac would be a protocol architecture decision, not a library swap.

---

## 8. Proposed dependency selection criteria

The following gates are mandatory for any candidate. No weighted score may override a failed hard gate.

### Gate 1 — Legal and provenance (hard gate)

- [ ] SPDX license for root package and every transitive dependency recorded.
- [ ] Written legal approval for distribution, hosted service and modification model.
- [ ] Source repository, immutable tag and commit SHA verified.
- [ ] Release artifact provenance and checksums verified.
- [ ] Export-control / jurisdiction review assigned where applicable.
- [ ] Maintainer support policy and third-party use position recorded.

### Gate 2 — Protocol suitability (hard gate)

- [ ] Protocol specification and version identified.
- [ ] Authenticated asynchronous session setup documented.
- [ ] Identity binding and prekey lifecycle understood.
- [ ] Forward secrecy and post-compromise recovery properties documented.
- [ ] PQ claims tied to a specific implemented version and test evidence; no marketing inference.
- [ ] Multi-device design fits FLY per-device identity/session model.
- [ ] Replay, duplicate, reordering, skipped-key limits and downgrade behavior are explicit.
- [ ] Interoperability vectors and protocol state serialization documented.
- [ ] No custom protocol composition is required to make the library fit.

### Gate 3 — Android platform compatibility

- [ ] Supported ABI list verified against target device matrix.
- [ ] Minimum Android API level and NDK/JDK/Rust toolchain pinned.
- [ ] Release and debug native artifacts reproducibly built.
- [ ] JNI/native bridge lifecycle, threading, memory ownership and error mapping reviewed.
- [ ] Key custody remains inside native boundary; no private-key/ratchet export to JS.
- [ ] Crash, process death, backup/restore and DB migration semantics verified.
- [ ] Low-memory and low-end device behavior measured.

### Gate 4 — iOS platform compatibility

- [ ] Minimum iOS version and supported device architectures verified.
- [ ] Device and simulator slices built from pinned sources.
- [ ] Xcode/Swift/Rust versions pinned.
- [ ] Swift/FFI lifecycle and memory ownership reviewed.
- [ ] Keychain/Secure Enclave interaction and background/lock behavior specified.
- [ ] State persistence and app upgrade migration tested.
- [ ] No secret material is passed through React Native JS.

### Gate 5 — Security assurance

- [ ] All published audits obtained, with exact audited commit/revision.
- [ ] Findings mapped to current selected commit and independently verified as fixed, mitigated or accepted.
- [ ] Audit scope includes protocol implementation, serialization, memory handling and FFI relevant to use.
- [ ] FLY-specific integration threat model independently reviewed.
- [ ] Fuzzing, property tests, protocol vectors and negative tests are available.
- [ ] Security policy, vulnerability disclosure and patch cadence reviewed.
- [ ] No unresolved Critical/High issue without written security-owner disposition.

### Gate 6 — Maintenance and supply chain

- [ ] Release cadence and maintenance activity reviewed over a defined period.
- [ ] Supported Rust/Java/Kotlin/Swift toolchains documented.
- [ ] Dependency graph and SBOM generated from exact build.
- [ ] Transitive dependency license/security scanning automated.
- [ ] Reproducible build and artifact hash verification demonstrated.
- [ ] Upgrade policy, compatibility window and emergency patch process assigned.
- [ ] Fork policy and exit/migration plan documented.

### Gate 7 — Performance and operational behavior

Measure on representative low, mid and high Android devices and supported iPhones:
- cold-start initialization time;
- session establishment latency;
- encrypt/decrypt latency at p50/p95/p99;
- CPU and battery cost under sustained messaging;
- peak and steady-state memory;
- native binary size and app size impact;
- queue/out-of-order handling cost;
- crash recovery and state persistence overhead.

No numeric threshold is invented here. Product and engineering owners must approve thresholds before the spike, and results must include device models, OS versions, build hashes and test conditions.

---

## 9. Required technical spike plan — not yet authorized or executed

A technical spike may be started only after legal and security owners approve the candidate and scope. It must be isolated from production application code.

### Spike A — libsignal feasibility
1. Pin an immutable release/tag and artifact checksums.
2. Build Android AAR and iOS framework from verified upstream sources.
3. Confirm exact protocol APIs needed for 1:1 session setup and message encryption/decryption.
4. Validate native storage adapter boundary without exporting secrets to JS.
5. Test state serialization, process death, rollback attempt and version migration.
6. Record dependency/license inventory and binary provenance.
7. Run protocol vectors and negative tests supplied by upstream; add FLY-specific integration tests only after design review.
8. Produce a written API gap list and upstream support assessment.

### Spike B — vodozemac comparator
1. Pin exact crate revision and Rust toolchain.
2. Build minimal Rust static/dynamic libraries for Android and iOS.
3. Prototype a narrow UniFFI or equivalent native boundary in a throwaway workspace only.
4. Determine whether Olm handshake and state model can meet FLY requirements without protocol modifications.
5. Map identity, prekey, device and server assumptions against Phase 0.4 architecture.
6. Map 2022 audit findings to the exact current revision; verify issue I, issue J and suggestion 8 status.
7. Obtain a new independent review scope before any production consideration.

### Spike C — Matrix SDK alternative
1. Evaluate whether FLY is willing to adopt Matrix client-server protocol semantics.
2. Map FLY ID to Matrix user/device identity without weakening key transparency.
3. Map relay, directory, recovery and multi-device flows to Matrix SDK behavior.
4. Estimate backend and product architecture changes.
5. Reject if adoption requires unsafe forked crypto or extensive protocol surgery.

**No spike was run in Phase 0.5.**

---

## 10. Decision log update

| Item | Phase 0.4 position | Phase 0.5 outcome |
|---|---|---|
| D-01 — exact 1:1 implementation | Signal-family recommended, exact library open | Still OPEN; libsignal primary conditional candidate, AGPL/support blocker |
| Crypto license | Legal review required | AGPL-3.0-only identified for libsignal; written legal opinion required |
| Android integration | RN + Kotlin native adapter | libsignal Java/AAR documented; not tested in FLY |
| iOS integration | Swift native adapter | libsignal Swift binding documented; not tested in FLY |
| Alternative protocol | Not selected | vodozemac evaluated as non-equivalent Olm option; Matrix SDK as broader alternative |
| Current production dependency | None | None |
| Exact version pin | None | None |
| Security audit of FLY integration | Not performed | Not performed |
| Technical spike | Not authorized | Not run |

### Recommended status changes

- D-01: **OPEN / BLOCKED** — no final library selection.
- D-02: unchanged — groups deferred; OpenMLS not evaluated as 1:1 candidate.
- D-11: unchanged — narrow native adapters remain the target; actual crypto API boundary depends on D-01.
- D-13: unchanged — no dependency pipeline or signing implementation created.

### Explicit non-decisions

- No approval to use libsignal in production.
- No approval to use vodozemac in production.
- No approval to use Matrix Rust SDK in production.
- No approval to fork any crypto library.
- No cryptographic audit or FLY integration test has been completed.
- No Red-Team test status changes.

---

## 11. Phase 0.5 exit criteria

Phase 0.5 documentation exit is achieved when:
- [x] Candidates and non-equivalent alternatives are identified.
- [x] License constraints are recorded.
- [x] Android/iOS API and integration surfaces are compared.
- [x] Maintenance and known support limitations are recorded.
- [x] Published audit scope and limitations are documented.
- [x] Dependency selection gates and spike plan are documented.
- [x] D-01 status is updated without inventing approval or test results.

Implementation admission is **not** achieved. It remains blocked by:
- legal approval for any selected library;
- final protocol/library decision;
- exact immutable version and artifact provenance;
- Android/iOS compatibility spike;
- security review of current upstream revision and FLY integration;
- approved persistence, device and recovery API contracts.

---

## 12. Verification record

- Base used: `security/fly-secret-decision-closure-0.4`.
- Working branch: `security/fly-secret-crypto-dependency-evaluation-0.5`.
- Dependency dossier written: yes.
- Application code written: no.
- Technical spike: not run.
- Build or test suite: not run.
- APK: not built.
- FLY CI: not implemented or run.
- Production dependency selected: no.
- PR created: no.
- `main` changed: no.
- Red-Team scenarios: 27 × `NOT_RUN`.
- TOR MODE: excluded.
- Decentralization: future-only.

**Phase 0.5 documentation status:** complete, pending independent review of the dossier.  
**Crypto dependency status:** OPEN / BLOCKED.  
**Implementation readiness:** CONDITIONAL NO-GO.

---

## 13. Primary sources

### Signal
- Repository / README / API support statement / license: https://github.com/signalapp/libsignal
- Cargo workspace manifest: https://github.com/signalapp/libsignal/blob/main/Cargo.toml
- Releases: https://github.com/signalapp/libsignal/releases
- Swift integration: https://github.com/signalapp/libsignal/blob/main/swift/README.md
- Signal Double Ratchet specification: https://signal.org/docs/specifications/doubleratchet/
- Signal PQXDH specification: https://signal.org/docs/specifications/pqxdh/

### Matrix
- vodozemac repository: https://github.com/matrix-org/vodozemac
- vodozemac Cargo manifest: https://github.com/matrix-org/vodozemac/blob/main/Cargo.toml
- vodozemac changelog: https://github.com/matrix-org/vodozemac/blob/main/CHANGELOG.md
- vodozemac bindings (marked unmaintained): https://github.com/matrix-org/vodozemac-bindings
- Matrix Rust SDK: https://github.com/matrix-org/matrix-rust-sdk
- Matrix Rust SDK FFI manifest: https://github.com/matrix-org/matrix-rust-sdk/blob/main/bindings/matrix-sdk-ffi/Cargo.toml
- Apple FFI / XCFramework instructions: https://github.com/matrix-org/matrix-rust-sdk/blob/main/bindings/apple/README.md
- Least Authority vodozemac audit (2022-03-30): https://matrix.org/media/Least%20Authority%20-%20Matrix%20vodozemac%20Final%20Audit%20Report.pdf
- Matrix libolm deprecation notice: https://gitlab.matrix.org/matrix-org/olm

All claims above are limited to these public materials and the repository snapshot reviewed on 2026-11-14. The dossier does not substitute for current source checkout, license counsel, reproducible builds or an independent security review.
