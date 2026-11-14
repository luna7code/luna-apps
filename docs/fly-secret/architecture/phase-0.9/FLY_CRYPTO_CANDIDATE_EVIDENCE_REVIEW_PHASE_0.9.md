# FLY SECRET — Phase 0.9
## Crypto Candidate Evidence Review

**Статус:** EVIDENCE REVIEW COMPLETE — NO CANDIDATE SELECTED — NO-GO FOR SPIKE / PRODUCTION INTEGRATION  
**База:** Phase 0.8 — Crypto Candidate Decision & Legal Contingency  
**Базовая ветка:** `security/fly-secret-crypto-candidate-contingency-0.8`  
**Рабочая ветка:** `security/fly-secret-crypto-evidence-review-0.9`  
**Репозиторий:** `luna7code/luna-apps`  
**Дата проверки публичных источников:** 2026-11-14

> Это документальная проверка опубликованных источников и репозиториев. Она не является юридическим заключением, аудитом FLY SECRET, повторным независимым аудитом библиотек или подтверждением того, что конкретные binaries были собраны и протестированы в этой фазе. Неизвестные факты отмечены как UNKNOWN/BLOCKED, а не PASS.

---

## 1. Executive decision

Phase 0.9 обновляет доказательную базу по трём рассматриваемым направлениям:

1. Signal `libsignal`.
2. Matrix `vodozemac` как отдельная криптографическая реализация Olm/Megolm.
3. Matrix Rust SDK / `matrix-sdk-crypto` как более высокий уровень Matrix crypto state machine и платформенных bindings.

**Итог: ни один кандидат не выбран.** У каждого есть значимые подтверждённые свойства, но ни один не прошёл все hard gates FLY SECRET.

### 1.1 Основные новые сведения

- Signal публикует `libsignal v0.106.2` (release от 2026-11-05); upstream README прямо говорит, что использование вне Signal не поддерживается, а API и bridge layers могут меняться без уведомления. Лицензия репозитория — AGPL-3.0-only. Это остаётся блокирующим юридическим и operational фактором для закрытого коммерческого продукта. [S1][S2][S3]
- 2026-11-10 Google DeepMind опубликовала формальную проверку части `libsignal` на уровне AArch64 machine code. Но доказательства относятся к `libsignal v0.105.4` / SPQR v1.6.0 и ограниченному one-to-one CLI harness; они не доказывают автоматически свойства v0.106.2, мобильных приложений, multi-device lifecycle или FLY SECRET service architecture. [S4]
- `vodozemac v0.11.1` имеет Apache-2.0 license и подписанный Git tag от 2026-09-30. Но публичный Least Authority audit датирован 2022-03-30 и исследовал старые revisions; в финальном отчёте Issues I/J и Suggestion 8 остались unresolved. Нет найденного публичного audit-to-current-commit mapping, доказывающего, что все эти вопросы закрыты в v0.11.1. [S5][S6][S7]
- Matrix Rust SDK `0.19.1` (2026-09-18) — Apache-2.0, официально позиционируется как production-ready, используется Element X на iOS/Android; имеет Swift/Kotlin FFI bindings. Но эти bindings являются Matrix SDK surface, а не доказательством наличия готового standalone FLY-compatible vodozemac binding. [S8][S9]
- Matrix SDK security/release history показывает опубликованные security fixes, включая CVE-2026-45056 и CVE-2026-45057 в release 0.17.0. Это свидетельство работающего patch/release процесса, но не независимый аудит всей криптографической реализации. [S10]
- Matrix SDK `.deny.toml` включает license/advisory/source checks, но также разрешает multiple dependency versions и wildcard dependencies; есть allowlist для Git dependencies. Это полезные supply-chain controls, но не заменяет независимую проверку фактически resolved graph и release artifacts. [S11]

### 1.2 Formal disposition

| Decision item | Status |
|---|---|
| libsignal license compatibility for FLY SECRET | BLOCKED — AGPL-3.0-only counsel review not obtained |
| libsignal upstream support | BLOCKED — third-party use explicitly unsupported |
| libsignal formal assurance | STRONG NEW EVIDENCE, but scope/version mismatch remains |
| vodozemac license | SOURCE CONFIRMED — Apache-2.0; full dependency/legal review open |
| vodozemac audit | HISTORICAL AUDIT FOUND; current-version mapping incomplete |
| vodozemac direct Android/iOS bindings | NOT ESTABLISHED |
| Matrix SDK license | SOURCE CONFIRMED — Apache-2.0; full dependency/legal review open |
| Matrix SDK mobile bindings | SOURCE CONFIRMED — Kotlin/Swift FFI exists; FLY fit unproven |
| Matrix SDK independent crypto audit | NOT ESTABLISHED by sources reviewed |
| Supply-chain reproducibility for FLY build | NOT TESTED |
| D-01 crypto candidate decision | OPEN |
| Technical spike | NOT AUTHORIZED |
| Production integration | NO-GO |

## 2. Evidence methodology and confidence labels

### 2.1 Evidence labels

- **CONFIRMED:** explicitly stated in an official repository, release, license, audit report or primary research publication.
- **PARTIALLY CONFIRMED:** the source supports only part of the claim or only a specific version/surface.
- **UNKNOWN:** no adequate primary evidence was located in this review.
- **BLOCKED:** a required decision/evidence item remains unresolved.
- **NOT TESTED:** no build, binary verification, test suite, benchmark or integration was executed in Phase 0.9.

### 2.2 Source hierarchy

1. Exact repository files and release tags.
2. Published independent audit reports and formal verification papers.
3. Official project documentation and security policies.
4. Secondary documentation only as discovery aid; not treated as primary proof where an upstream source exists.

### 2.3 Important limitation

A repository's current `main` branch can move after this review. A release tag is more useful for reproducibility, but must still be checked against immutable commit SHA, source archive, release artifact digest, dependency lockfile and build provenance at the time a real integration is authorized.

No live checkout, cargo build, Android/iOS build, dependency audit, SBOM generation or signature verification was performed by this documentation-only phase.

---

## 3. Candidate A — Signal libsignal

### 3.1 Identity, version and license

| Item | Evidence | Assessment |
|---|---|---|
| Repository | `signalapp/libsignal` | CONFIRMED |
| Latest release observed | `v0.106.2`, 2026-11-05 | CONFIRMED |
| Release tag | GitHub reports verified signature; abbreviated commit `8509425` | PARTIALLY CONFIRMED; immutable full SHA and local signature verification not performed |
| License | GNU AGPLv3; package metadata uses `AGPL-3.0-only` | CONFIRMED |
| External use | README: use outside Signal is unsupported | CONFIRMED |
| API stability | README: APIs/implementations and JNI/C/Node bridge layers may change without notice; backward-incompatible changes reflected on best-effort basis | CONFIRMED |
| Security contact | `security@signal.org` for vulnerabilities in Signal/repository | CONFIRMED |
| Commercial/legal suitability | No counsel opinion for FLY SECRET | BLOCKED |

Sources: [S1][S2][S3][S12]

**Interpretation:** the AGPL designation cannot be resolved by placing a Kotlin/Swift wrapper around the library or moving calls across FFI. Whether a specific distribution/linking/deployment arrangement creates obligations is a legal question for counsel. No conclusion is made here about the exact legal effect on FLY SECRET.

### 3.2 Upstream support posture

The official README explicitly says:
- the repository is used by Signal's own clients and servers;
- use outside Signal is unsupported;
- APIs and implementations are subject to change without notice;
- compatibility guarantees for non-bridge APIs are only best-effort.

The security policy invites vulnerability reports for Signal/repository vulnerabilities. It does not create a third-party support commitment, SLA, compatibility promise, integration support or roadmap obligation.

**Assessment:**
- Maintained for Signal's own product: CONFIRMED.
- Supported as a third-party FLY SECRET dependency: NOT CONFIRMED; upstream wording says unsupported.
- Security vulnerability intake exists: CONFIRMED.
- Contractual response time, long-term API stability, third-party integration support: UNKNOWN.

**Gate:** SEL-06 / upstream support remains BLOCKED unless product/security owners explicitly accept unsupported integration risk in writing. Silence or a security email address is not support endorsement.

### 3.3 Android binding evidence

Official README documents Java APIs and the Android package `org.signal:libsignal-android`, published from Signal's Maven repository. It lists native ABIs:
- `armeabi-v7a`
- `arm64-v8a`
- `x86`
- `x86_64`

The build instructions identify JDK 21 and Android SDK/NDK/Rust targets. The README warns that the Android integration needs both `libsignal-android` and `libsignal-client`, with packaging exclusions for non-Android native libraries.

**Assessment:** a concrete Android integration surface exists for Signal's own app ecosystem. This does not establish a supported third-party contract, FLY API suitability, reproducible FLY build, or FLY's desired React Native/native boundary.

### 3.4 iOS binding evidence

Signal exposes Swift APIs over Rust through C FFI. The repository release history reports that `v0.106.1` moved Swift distribution to Swift Package Manager instead of CocoaPods. The current Swift README still contains historical CocoaPods instructions and describes CocoaPods as canonical for Signal's own client build; therefore the documentation surfaces are not fully aligned and the exact current external-consumption procedure must be confirmed against the exact release.

The Swift README documents a prebuilt FFI checksum workflow for release integration. That is a positive integrity control, but a FLY consumer must independently verify the checksum source, artifact provenance, package manifest and transitive licenses.

**Assessment:** Swift/Rust FFI exists. Current package integration and release artifact handling need exact version-specific confirmation. No iOS build was run.

### 3.5 Independent assurance — AlphaProof Code (2026-11-10)

Google DeepMind and Google published *AlphaProof Code: Verifying Signal’s Security at the Machine-Code Level* on 2026-11-10.

**Verified scope described by the paper:**
- unmodified `libsignal v0.105.4` with SPQR v1.6.0;
- one-to-one PQXDH handshake and Triple Ratchet;
- a small CLI harness compiled as an AArch64 executable;
- machine-checked proofs over compiled machine code;
- formal properties include handshake authentication/secrecy, forward secrecy and post-compromise security under explicitly stated models and assumptions;
- two previously discovered bugs were reintroduced and the formal specification rejected the mutated binaries.

**Critical boundaries explicitly stated in the paper:**
- not the current `v0.106.2` release;
- not Signal's actual mobile applications;
- no multi-device state in the verified setting;
- group messaging left as future work in the paper's model;
- the computational proof does not guarantee against a quantum adversary;
- some responder/first-message cases are outside the theorem;
- identity keys are trusted on first use in the simplified setting;
- the symbolic proof does not establish computational secrecy or SPQR post-quantum healing;
- cryptographic primitives and selected routines are trusted through contracts;
- the proof depends on a trusted specification, Lean kernel, AArch64/Linux semantics and build/extraction tooling.

**Assessment:** this is unusually strong, relevant independent assurance for the specific 1:1 implementation revision studied. It materially improves the evidence quality for libsignal but does not prove the current release or FLY SECRET's complete identity, multi-device, recovery, platform and service architecture.

**Required follow-up before using this evidence as a gate:**
1. Map v0.105.4 verified source and binary to v0.106.2 and later intended pin.
2. Review all relevant changes after v0.105.4, especially session management changes introduced in v0.106.0.
3. Confirm whether the proof project will be maintained/re-run for subsequent versions.
4. Extend review to Android/iOS bindings, persistent state, platform lifecycle and multi-device logic.
5. Independently assess assumptions and proof scope with cryptography/formal-methods reviewers.

Sources: [S4][S13]

### 3.6 Supply chain

Positive evidence:
- release tags are shown by GitHub as verified signatures;
- Android packages are distributed through an official Signal Maven repository;
- Swift release integration documents an FFI prebuild checksum;
- repository contains Cargo lock/toolchain files and build instructions.

Open items:
- full tag-signature verification by FLY CI not performed;
- artifact checksums/signatures and provenance not independently verified;
- SBOM and complete transitive license inventory for exact Android/iOS packages not generated;
- no FLY reproducible-build comparison;
- no pinning decision or update policy;
- no third-party support commitment for vulnerability response.

**Status:** PARTIALLY CONFIRMED / NOT TESTED. No supply-chain PASS.

### 3.7 FLY SECRET compatibility

| Requirement | Assessment |
|---|---|
| Signal-family 1:1 session semantics | Strong protocol fit; exact FLY identity/service mapping still requires review |
| FLY ID distinct from cryptographic identity | Not proven by library choice; FLY architecture must own binding |
| Custom ciphertext relay | Protocol design may support untrusted relay conceptually; actual FLY integration contract not validated |
| Key transparency | Must be supplied by FLY directory/transparency architecture; library choice does not close it |
| Per-device sessions/revocation | Signal has device-oriented concepts, but DeepMind formal scope explicitly excludes multi-device state |
| Recovery Kit without server escrow | Not established by the 1:1 protocol library alone |
| Atomic state persistence/rollback | FLY storage adapter and lifecycle need separate proof/testing |
| Android/iOS parity | Bindings exist; FLY parity not tested |
| Legal fit | BLOCKED |
| Third-party upstream support | BLOCKED |

**Candidate disposition:** retain as a technically relevant conditional candidate, but not selectable under current legal/upstream blockers.

---

## 4. Candidate B — Matrix vodozemac

### 4.1 Identity, version and license

| Item | Evidence | Assessment |
|---|---|---|
| Repository | `matrix-org/vodozemac` | CONFIRMED |
| Latest release observed | `0.11.1`, 2026-09-30 | CONFIRMED |
| Release tag | GitHub reports verified committer signature; abbreviated commit `3e11ae8` | PARTIALLY CONFIRMED; no local verification |
| License | Cargo manifest declares Apache-2.0 | CONFIRMED for crate manifest |
| Language | Pure Rust | CONFIRMED |
| Protocols | Olm and Megolm ratchets; Matrix-oriented SAS and related functionality | CONFIRMED |
| Rust version | Cargo manifest states Rust 1.96 | CONFIRMED for inspected manifest |
| Unsafe code | Cargo lint configuration denies unsafe code | CONFIRMED for inspected manifest |

Sources: [S5][S6][S7]

### 4.2 Protocol suitability

vodozemac implements Matrix Olm and Megolm. Olm is a 1:1 ratchet protocol; Megolm is a group ratchet. It is not a Signal Protocol implementation and is not wire-compatible with libsignal.

A FLY SECRET adoption would therefore require a new protocol decision, including:
- handshake and identity semantics;
- device key publication and trust;
- session serialization and migrations;
- multi-device behavior;
- group semantics if Megolm is used;
- server-visible metadata and Matrix-specific protocol expectations;
- Recovery Kit and key backup behavior.

**Assessment:** technically relevant as a distinct protocol family, not a drop-in replacement. Matrix protocol semantics would have to be adopted or deliberately adapted under a new ADR. No custom protocol fork is approved.

### 4.3 Independent audit — Least Authority (2022-03-30)

The public final report is dated 2022-03-30. The report states:
- initial review revision: `7c11a501bc316a0bf92a5fe06fee8582aad24897`;
- verification revision: `57d8d87a747653d6d7b7a53acb9a8d8f8de48285`.

The report lists 10 issues (A–J) and 8 suggestions. At the report's verification stage:
- Issues A–H: Resolved.
- Issue I — keys in memory not secure against swap access and side-channel attacks: Unresolved.
- Issue J — HMAC tag truncated to 8 bytes / 64 bits: Unresolved.
- Suggestion 8 — making the number of chain/message keys configurable: Unresolved.
- Suggestion 1 on outdated dependencies: Resolved in the audited revision.

The report notes that Issue I's mitigation was acknowledged but deferred. For Issue J, the report says remediation would require a Matrix protocol-level coordinated change because of interoperability constraints. Suggestion 8 was not implemented because maintainers considered configurability likely to increase misuse.

**Important:** the project's current README summarizes the audit as having “no significant findings”. The detailed final report is more nuanced and must be preserved in this candidate review. The report is historical and does not, by itself, prove whether I/J/Suggestion 8 remain unresolved in v0.11.1 or whether their status changed after the audited revision.

**Required evidence before candidate promotion:**
1. Exact source diff from both audited revisions to v0.11.1.
2. Maintainer evidence or independent reviewer mapping for each I/J/Suggestion 8 item.
3. Current cryptographic rationale for 64-bit MAC truncation and interoperability implications.
4. Current memory zeroization, swap and side-channel threat treatment on Android/iOS.
5. A new audit or scoped re-review against the exact intended immutable commit.

**Assessment:** historical independent audit exists; current version assurance is BLOCKED pending version mapping and review.

Sources: [S6][S7]

### 4.4 Android/iOS binding evidence

The vodozemac repository describes a pure Rust crate and provides Cargo installation. The reviewed public materials do not establish a first-party, stable Android Java/Kotlin or iOS Swift binding surface for the standalone crate.

Matrix Rust SDK, by contrast, has a separate UniFFI binding layer that can be embedded in Kotlin and Swift. That is an SDK integration surface, not proof that vodozemac's internal API is independently exposed as a supported FLY crypto module.

**Assessment:**
- standalone vodozemac Android binding: UNKNOWN / NOT ESTABLISHED;
- standalone vodozemac iOS binding: UNKNOWN / NOT ESTABLISHED;
- Rust compilation to Android/iOS targets: technically plausible but not validated here;
- secure FLY native boundary and platform parity: NOT TESTED.

A FLY-owned UniFFI or C FFI wrapper would be new integration code and would require its own API design, audit, memory/lifecycle review and platform validation. It cannot be treated as pre-existing upstream support.

### 4.5 Supply chain

Positive evidence:
- signed release tag is reported for v0.11.1;
- Cargo manifest pins package version and declares Apache-2.0;
- strict Rust lints include `unsafe_code = "deny"`;
- project has a published release history.

Open items:
- exact v0.11.1 dependency graph not independently resolved and audited in this phase;
- current dependency advisories and license inventory not generated;
- signed tag not verified locally;
- crate registry package checksum not captured;
- no FLY reproducible build or Android/iOS artifact provenance;
- no current independent audit mapping to v0.11.1.

**Status:** PARTIALLY CONFIRMED / NOT TESTED.

### 4.6 FLY SECRET compatibility

| Requirement | Assessment |
|---|---|
| 1:1 protocol | Olm is 1:1 but not Signal-compatible |
| Group messaging | Megolm exists, but separate group security review required |
| FLY ID separation | Requires new identity binding ADR |
| Key transparency | Matrix trust model cannot be assumed to satisfy FLY's proposed independent monitor/witness design |
| Multi-device lifecycle | Matrix semantics exist at SDK level; FLY lifecycle compatibility unproven |
| Recovery without server escrow | Matrix key backup/dehydrated-device flows need explicit redesign/review; do not assume FLY Recovery Kit compatibility |
| Custom relay / minimal metadata | Requires protocol/service redesign and metadata review |
| Android/iOS bindings | Not established for standalone crate |
| Current-version audit coverage | BLOCKED |
| Legal fit | Apache-2.0 crate license is promising; transitive and distribution review remains open |

**Candidate disposition:** not a drop-in alternative. Keep as an unselected protocol-family option only if FLY is willing to reopen identity, messaging, device, recovery and service ADRs.

---

## 5. Candidate C — Matrix Rust SDK / matrix-sdk-crypto

### 5.1 Identity, version and license

| Item | Evidence | Assessment |
|---|---|---|
| Repository | `matrix-org/matrix-rust-sdk` | CONFIRMED |
| Latest release observed | `0.19.1`, 2026-09-18 | CONFIRMED |
| Release tag | GitHub reports verified committer signature | PARTIALLY CONFIRMED; no local verification |
| License | Apache-2.0 | CONFIRMED for SDK repository |
| Crypto layer | `matrix-sdk-crypto` described as standalone encryption state machine with no network I/O | CONFIRMED |
| Project status | README describes SDK as production-ready and used by Element X | Upstream project claim, CONFIRMED as claim |
| Mobile use | Element X uses SDK on iOS and Android | CONFIRMED |
| Bindings | Swift/Kotlin/JavaScript/Node supported via bindings directory | CONFIRMED at project level |

Sources: [S8][S9][S10]

### 5.2 Critical architectural distinction

`matrix-sdk-crypto` is not merely a thin wrapper around vodozemac. It is the higher-level Matrix encryption state machine. The broader SDK includes client-server synchronization, room/event state, storage and UI-level facilities.

The fact that the crypto crate itself has no network I/O does not mean the overall SDK is protocol-neutral or independent of Matrix:
- the crypto state machine models Matrix identity/device/key and room semantics;
- Matrix SDK's higher layers are designed around Matrix homeservers and sync;
- using the SDK FFI can bring a larger API, storage and lifecycle surface than FLY's desired narrow crypto adapter.

**Assessment:** the most complete documented mobile integration path among the Matrix options, but only if FLY is prepared to adopt or adapt Matrix semantics. This is an architecture choice, not a library substitution.

### 5.3 Android/iOS bindings

The SDK repository documents Kotlin and Swift bindings generated through UniFFI. The binding crate builds:
- `cdylib` for Android;
- `staticlib` for iOS;
- a regular Rust library for JS/Wasm-related use.

The repository is used by Element X on both mobile platforms. The binding layer has its own changelog and release train.

**Stability caveat:** the SDK's Swift/Kotlin bindings are generated to meet Element X needs. The Matrix SDK documentation says to use Element X as the working reference rather than assume old snippets or a frozen API. API stability and compatibility must be evaluated for the exact release.

**Assessment:** mobile bindings exist and have real production consumer evidence. However:
- no FLY build was performed;
- no proof was established that every required crypto operation is exposed through the current FFI;
- no proof was established that the FFI can enforce FLY's strict secret custody boundary;
- no proof was established that Matrix-specific persistence can be replaced without altering security semantics;
- no RN bridge integration or JS plaintext exposure review was performed.

Sources: [S8][S9][S14]

### 5.4 Audit and vulnerability-response evidence

The official repository describes the SDK as production-ready and provides a security policy. Public release notes show security fixes in the 0.17.0 release:
- CVE-2026-45056 / GHSA-wfq4-36m3-9g42;
- CVE-2026-45057 / GHSA-h97m-27fx-42rx.

This demonstrates that security fixes are published through the release process. It does not establish that the entire SDK or its crypto state machine has a current independent cryptographic audit.

No current independent audit report specifically covering the exact `matrix-sdk-crypto 0.19.1` revision was established from the reviewed primary sources. This is an evidence gap, not proof that no private or separately published review exists.

**Required evidence:**
1. Request/locate audit reports for exact `matrix-sdk-crypto` and `vodozemac` revisions used by 0.19.1.
2. Map published CVEs/advisories to affected components and fixed commits.
3. Review security policy, disclosure channel, patch cadence and support expectations.
4. Confirm whether security fixes in internal crates are released independently or only with SDK trains.

Sources: [S10][S15]

### 5.5 Supply-chain controls

The repository includes:
- Cargo.lock;
- `.deny.toml` with license allowlist;
- advisory checking configuration;
- source policy denying unknown registries and unknown Git sources;
- allowlisted Git dependencies;
- release tags reported as signed.

The same `.deny.toml` also:
- allows multiple versions of dependencies;
- allows wildcard dependencies;
- includes specific advisory ignore entries with rationale.

These are not necessarily defects, but they create explicit review obligations. A FLY consumer must inspect the exact resolved dependency graph and validate each exception. The existence of a policy file is not proof that a release passed it or that FLY's own build is reproducible.

**Required evidence:** exact lockfile and release source archive, cargo-deny output, cargo-audit output, SPDX/CycloneDX SBOM, license notice package, source provenance, signature verification, build attestation, artifact hashes and reproducibility report.

Source: [S11]

### 5.6 FLY SECRET compatibility

| Requirement | Assessment |
|---|---|
| 1:1 and group protocol semantics | Matrix-specific; requires formal adoption decision |
| FLY ID separate from Matrix user/device identity | Requires identity and directory ADR redesign |
| Custom ciphertext relay | Matrix client-server assumptions likely need adaptation; full data-flow review required |
| Key transparency | Matrix cross-signing/key verification is not automatically the proposed FLY Merkle monitor/witness architecture |
| Per-device lifecycle | SDK supports Matrix device model; FLY revocation/recovery mapping unproven |
| Recovery Kit | Matrix key backup/dehydrated-device semantics require separate threat-model decision |
| Minimal metadata | Room, sync, event, device and homeserver metadata require privacy review |
| Native Android/iOS | Strongest documented Matrix mobile path; FLY-specific custody/parity untested |
| Audit coverage | Current exact crypto revision not established |
| Legal fit | Apache-2.0 main repository; transitive license and artifact review still required |

**Candidate disposition:** technically the most complete Matrix mobile SDK route, but high architectural coupling. It is not recommended as a library-only replacement for Signal. No selection.

---

## 6. Comparative evidence matrix

Legend: CONFIRMED / PARTIAL / UNKNOWN / BLOCKED / NOT TESTED.

| Criterion | libsignal | vodozemac | Matrix Rust SDK |
|---|---|---|---|
| License at main repository/package | AGPL-3.0-only — CONFIRMED | Apache-2.0 — CONFIRMED | Apache-2.0 — CONFIRMED |
| Legal approval for FLY distribution | BLOCKED | BLOCKED pending full graph review | BLOCKED pending full graph review |
| Third-party support posture | Explicitly unsupported | No FLY support commitment established | Maintained SDK, but no FLY support commitment |
| Protocol match for Signal-like 1:1 | Strong | Different protocol | Different protocol / Matrix state machine |
| Current release evidence | v0.106.2, signed tag reported | 0.11.1, signed tag reported | 0.19.1, signed tag reported |
| Independent assurance | DeepMind formal verification for v0.105.4 scope | Least Authority 2022 audit; current mapping open | Exact current independent crypto audit not established |
| Android surface | Java + Android artifacts | Standalone binding not established | Kotlin UniFFI bindings |
| iOS surface | Swift/Rust FFI; release packaging transition needs confirmation | Standalone binding not established | Swift UniFFI bindings |
| Multi-device FLY proof | Not covered by DeepMind proof | Not established | Matrix SDK support exists; FLY semantics unproven |
| Recovery Kit fit | Not established by library alone | Not established | Matrix backup/dehydrated device requires redesign |
| FLY custom relay fit | Requires adapter and protocol review | Matrix semantics require adaptation | Strong Matrix coupling |
| Supply-chain controls | Signed releases/checksum process; FLY verification absent | Signed tag; full graph verification absent | cargo-deny/lockfile/signing; FLY verification absent |
| FLY reproducible build | NOT TESTED | NOT TESTED | NOT TESTED |
| Candidate selection | NO | NO | NO |

### 6.1 What the evidence supports

- **libsignal** has the strongest newly discovered formal assurance for the Signal-family 1:1 protocol implementation, but its AGPL-3.0-only license and unsupported third-party use remain serious blockers.
- **vodozemac** has a permissive crate license and a public historical independent audit, but the unresolved historical findings and lack of current-version audit mapping prevent a clean assurance decision. Its standalone mobile binding story is not established.
- **Matrix Rust SDK** has the clearest documented Android/iOS integration and an Apache-2.0 repository license, but it entails Matrix protocol/service semantics and the exact current crypto audit evidence remains incomplete.

No weighted score is assigned because the candidates have not passed all mandatory hard gates. Scoring blocked candidates would create false precision.

---

## 7. Supply-chain evidence requirements — common baseline

Before any candidate is eligible for a technical spike, the evidence package must include:

1. Immutable source commit and signed release tag verification.
2. Exact crate/Maven/Swift package coordinates and version.
3. Source archive digest and artifact digest.
4. SBOM in SPDX or CycloneDX format.
5. Full direct and transitive license inventory, including generated/bundled native artifacts.
6. Vulnerability/advisory report for the resolved dependency graph.
7. Explicit review of ignored advisories, Git dependencies, multiple versions and wildcard constraints.
8. Build toolchain and target ABI matrix.
9. Build provenance and CI identity.
10. Reproducible build comparison or documented reason reproducibility is unavailable.
11. Artifact retention and rollback procedure.
12. Vulnerability intake, patch SLAs/expectations and emergency upgrade path.

### 7.1 Candidate-specific supply-chain gaps

**libsignal**
- Verify the exact Maven artifacts for both Android packages and the Swift package release artifacts.
- Verify checksums/signatures from the official release for the exact pin.
- Map AGPL notices and every transitive/native artifact.
- Establish whether the official build artifacts can be consumed and redistributed under the counsel-approved model.

**vodozemac**
- Verify the exact crates.io package checksum and signed Git tag.
- Resolve and inspect the complete Cargo.lock at the selected tag.
- Review the pinned `olm-rs` Git patch and all transitive Git dependencies.
- Reconcile audit revisions with the exact source revision.

**Matrix Rust SDK**
- Review Cargo.lock, .deny.toml exceptions, all Git overrides and multiple-version graph.
- Verify UniFFI-generated Kotlin/Swift outputs and native binaries against the release source.
- Establish which SDK components are required and remove unused surface only if this does not invalidate upstream support/security assumptions.
- Verify release assets and platform build provenance.

---

## 8. Architecture compatibility review against FLY invariants

The following FLY invariants remain mandatory regardless of candidate:

- FLY ID is a random opaque identifier, not a public/private key.
- Identity keys, device keys, session keys and transport identifiers remain separate.
- Private keys and ratchet/session secrets remain on device.
- Backend stores ciphertext and minimal routing metadata only.
- No server-side plaintext escrow of Recovery Kit, identity private keys or ratchet state.
- Key transparency and contact verification remain independently designed.
- Every device has explicit lifecycle, revocation and rotation.
- Ratchet/session persistence is atomic and rollback-resistant.
- Web remains a separate threat model.
- App-level E2EE for calls is not inferred from transport encryption.
- No custom cryptographic protocol.

### 8.1 Candidate impact summary

| FLY subsystem | libsignal | vodozemac | Matrix SDK |
|---|---|---|---|
| FLY ID / directory | FLY-owned adapter required | Reopen identity model | Reopen Matrix identity mapping |
| Device lifecycle | Verify Signal state semantics and FLY revocation | Matrix Olm account/device model | Matrix device/cross-signing lifecycle |
| Session persistence | Native store adapter and rollback design | Rust serialization and persistence wrapper | SDK crypto store integration and migration |
| Recovery Kit | Separate FLY design; not supplied by protocol primitive | Must reconcile Olm account/key backup semantics | Matrix backup/dehydrated-device model conflicts require explicit analysis |
| Ciphertext relay | FLY protocol envelope and directory contract | Matrix protocol/service compatibility decision | Matrix homeserver/sync/event model coupling |
| Transparency | External FLY transparency service | Matrix trust model review | Matrix cross-signing not equivalent to FLY witness design |
| Native boundary | Java/Swift APIs exist; secret custody audit required | FLY wrapper likely required | UniFFI SDK surface; narrow-boundary audit required |
| Web | TypeScript API exists, but Web threat model remains separate | Rust/WASM option not approved | SDK crypto WASM exists; Web security review required |

---

## 9. Required new ADR and gate decisions

D-01 remains OPEN. The following ADRs must be reopened if a candidate path is chosen:

- **D-01:** protocol and implementation — always reopen.
- **D-02:** FLY ID and identity binding — mandatory for vodozemac/Matrix; confirm for libsignal.
- **D-03:** key transparency and trust directory — mandatory if candidate changes trust/key publication semantics.
- **D-04:** multi-device lifecycle — mandatory for all candidates.
- **D-05:** Recovery Kit and re-establishment — mandatory for all candidates.
- **D-06:** 1:1/group messaging and wire format — mandatory for vodozemac/Matrix; update for libsignal version/API changes.
- **D-08:** metadata/service boundary — mandatory for Matrix SDK adoption and any relay/server coupling.
- **D-09:** native boundary — mandatory for all platform binding choices.
- **D-10:** Web security — mandatory if TS/WASM/JS crypto APIs are used.
- **D-11:** release/supply chain — mandatory for every exact package and native artifact.

The ADR must include the exact immutable version, legal memo, audit scope, platform proof, dependency graph, FLY threat-model mapping, unresolved findings, residual risk owners and formal approval.

### 9.1 Evidence gate table

| Gate | Current result | Required closure |
|---|---|---|
| Legal / license | libsignal BLOCKED; Matrix candidates PARTIAL | Counsel memo and complete transitive inventory |
| Upstream support | libsignal BLOCKED; Matrix support posture only partial | Written position or formal risk acceptance |
| Independent assurance | Candidate-specific scope gaps | Current revision audit/formal review mapping |
| Android | libsignal surface confirmed; Matrix SDK binding confirmed; vodozemac standalone unknown | Exact FLY build and API boundary review |
| iOS | libsignal surface confirmed with packaging caveat; Matrix SDK binding confirmed; vodozemac standalone unknown | Exact FLY build and API boundary review |
| Supply chain | PARTIAL for all | Exact artifact and reproducibility package |
| Identity/device/recovery | OPEN for all | Updated ADR and architecture proofs |
| Privacy/relay | OPEN for all | Data-flow and metadata analysis |
| Performance | NOT TESTED | Authorized benchmark plan and execution |
| Candidate decision | OPEN | All hard gates + formal owner approval |

No gate is closed by this phase.

---

## 10. Recommendation and next decision

### 10.1 Evidence-based recommendation

This phase does **not** recommend selecting any library.

The rational shortlist remains:
- **libsignal:** preserve as conditional Signal-family candidate because of protocol fit and newly available formal assurance; do not proceed until legal and unsupported-upstream risk are explicitly resolved.
- **Matrix Rust SDK:** retain as a distinct architecture option if product owners are willing to reconsider Matrix protocol/service semantics; obtain exact audit evidence and perform full FLY compatibility review.
- **vodozemac alone:** do not treat as a complete mobile integration option until standalone bindings, persistence, device lifecycle and current-version audit mapping are established.

If AGPL legal review or upstream support is unacceptable, the next decision should compare a Matrix architecture against deferring production E2EE implementation. It must not silently replace libsignal with vodozemac and preserve old ADR assumptions.

### 10.2 No candidate score

No numerical ranking is published. A weighted score is valid only after hard gates pass. A candidate with an unresolved legal blocker, current-version assurance gap or unverified platform integration must remain BLOCKED regardless of other strengths.

---

## 11. Red-Team and release-gate preservation

All 27 unique Red-Team scenario IDs remain `NOT_RUN`:

- PRIV-01..04
- ID-01..04
- RELAY-01..02
- CRYPTO-01..03
- REC-01..03
- FILE-01..03
- CALL-01..03
- DEC-01..03
- SUPPLY-01
- LOG-01

No Red-Team scenario was executed in Phase 0.9. The source test-case document is unchanged from Phase 0.8. `NOT_RUN` is not PASS.

TOR MODE remains excluded from product, architecture, UI, configuration, tests and roadmap. Decentralization remains future-only and is not part of MVP or this review.

---

## 12. Phase 0.9 execution record

**Performed:**
- reviewed Phase 0.8 baseline and its decision constraints;
- reviewed official repositories, release pages, licenses, binding documentation and security policy material for all three candidate paths;
- reviewed Least Authority's original vodozemac audit report and its explicit issue status;
- reviewed Google DeepMind's 2026-11-10 formal verification paper and its version/scope limitations;
- documented evidence, confidence, unknowns, supply-chain gaps and FLY architecture implications;
- created this documentation on a dedicated branch.

**Not performed:**
- no legal opinion or counsel consultation;
- no upstream inquiry sent and no upstream response received;
- no library selected;
- no application or wrapper code;
- no technical spike, prototype, build, benchmark or test suite;
- no cargo audit, SBOM generation, artifact signature verification or reproducible build;
- no main branch changes;
- no Pull Request created;
- no Red-Team status changes.

## 13. Source register

All sources below were accessed/reviewed on 2026-11-14. Release tags and branch contents may change; immutable commit and artifact verification is still required before adoption.

### Signal libsignal
- [S1] Official repository README — scope, unsupported third-party use, API stability, Java/Android packages and ABI details: https://github.com/signalapp/libsignal
- [S2] Official LICENSE — GNU AGPLv3: https://github.com/signalapp/libsignal/blob/main/LICENSE
- [S3] Official releases — v0.106.2 and recent version history: https://github.com/signalapp/libsignal/releases
- [S12] Official security policy: https://github.com/signalapp/libsignal/security
- [S13] Swift binding documentation and FFI checksum/build instructions: https://github.com/signalapp/libsignal/blob/main/swift/README.md

### Independent libsignal formal verification
- [S4] Google DeepMind / Google, *AlphaProof Code: Verifying Signal’s Security at the Machine-Code Level*, published 2026-11-10: https://google-deepmind.github.io/code-verification/
- PDF: https://storage.googleapis.com/gdm-deepmind-com-prod-public/media/documents/Formal_Verification_of_Signal_4.pdf
- Proof repository: https://github.com/google-deepmind/code-verification

### vodozemac
- [S5] Official repository README: https://github.com/matrix-org/vodozemac
- [S6] Official Cargo manifest — version, Apache-2.0, Rust version and lints: https://github.com/matrix-org/vodozemac/blob/main/Cargo.toml
- [S7] Least Authority, *Matrix vodozemac Final Audit Report*, 2022-03-30: https://matrix.org/media/Least%20Authority%20-%20Matrix%20vodozemac%20Final%20Audit%20Report.pdf
- Releases: https://github.com/matrix-org/vodozemac/releases

### Matrix Rust SDK
- [S8] Official Matrix Rust SDK repository — production status, crypto crate, mobile use, bindings and Apache-2.0: https://github.com/matrix-org/matrix-rust-sdk
- [S9] Official Android/iOS FFI crate manifest: https://github.com/matrix-org/matrix-rust-sdk/blob/main/bindings/matrix-sdk-ffi/Cargo.toml
- [S10] Official release history and security fixes: https://github.com/matrix-org/matrix-rust-sdk/releases
- [S11] Official Cargo dependency/license/advisory/source policy: https://github.com/matrix-org/matrix-rust-sdk/blob/main/.deny.toml
- [S14] Official Apple binding build instructions: https://github.com/matrix-org/matrix-rust-sdk/blob/main/bindings/apple/README.md
- [S15] Official security policy: https://github.com/matrix-org/matrix-rust-sdk/security
- [S16] Matrix Rust SDK platform/binding notes (supplementary documentation; verify against upstream release sources): https://matrixdocs.github.io/docs/sdks/matrix-rust-sdk

---

## 14. Formal Phase 0.9 disposition

| Decision | Result |
|---|---|
| Evidence review of libsignal | COMPLETE, conditional candidate only |
| Evidence review of vodozemac | COMPLETE, current audit/bindings gaps remain |
| Evidence review of Matrix Rust SDK | COMPLETE, architecture/audit gaps remain |
| Library selected | NO |
| Weighted candidate ranking | NOT ISSUED — hard gates not passed |
| D-01 closed | NO — OPEN |
| Legal blocker resolved | NO |
| Upstream support blocker resolved | NO |
| Android/iOS FLY integration validated | NO |
| Supply-chain verification executed | NO |
| Technical spike authorized | NO |
| Production integration approved | NO |
| Red-Team scenarios | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: EVIDENCE REVIEW COMPLETE / NO-GO FOR IMPLEMENTATION.**  
The new DeepMind result materially strengthens the libsignal evidence, but it does not supersede legal review or prove the current release and FLY-specific architecture. Matrix options remain separate protocol architecture paths. The next step requires owner decisions on legal risk and willingness to adopt Matrix semantics, followed by a formal ADR. No implementation authorization is implied.
