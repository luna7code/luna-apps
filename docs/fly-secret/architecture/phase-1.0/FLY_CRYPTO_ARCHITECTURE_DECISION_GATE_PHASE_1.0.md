# FLY SECRET — Phase 1.0
## Crypto Architecture Decision Gate

**Status:** DECISION GATE PREPARED — D-01 OPEN — NO-GO FOR CRYPTO IMPLEMENTATION  
**Evidence baseline:** Phase 0.9 — Crypto Candidate Evidence Review  
**Previous decision baseline:** Phase 0.8 — Crypto Candidate Decision & Legal Contingency  
**Base branch:** `security/fly-secret-crypto-evidence-review-0.9`  
**Working branch:** `security/fly-secret-crypto-architecture-decision-gate-1.0`  
**Repository:** `luna7code/luna-apps`  
**Review date:** 2026-11-14

> This document formalizes the decision among (A) conditional continuation with Signal libsignal, (B) migration to Matrix Rust SDK, or (C) deferral of production E2EE implementation. It is not a legal opinion, product-owner approval, cryptographic audit, or authorization to implement. No candidate is selected and D-01 is intentionally left OPEN.

---

## 1. Executive decision

### 1.1 Current decision

**Select Option C as the current project execution posture: defer production E2EE implementation and keep the crypto architecture decision open.**

This is a reversible project-control decision, not a permanent rejection of E2EE, libsignal, or Matrix. It means:
- do not integrate a cryptographic engine;
- do not create custom cryptography or a protocol fork;
- do not ship a product that represents messages as end-to-end encrypted before the full protocol and platform gates pass;
- preserve the ability to revisit Option A or Option B after the evidence and approval conditions in this document are satisfied;
- separately scoped non-cryptographic product work may proceed only under a distinct explicit authorization and must not imply E2EE security guarantees.

**D-01 remains OPEN.** Option C is the only currently admissible execution posture because Options A and B each have unresolved hard blockers. It is not a final crypto candidate selection.

### 1.2 Why this is the defensible gate outcome

- **Option A — libsignal:** strongest protocol fit to the Signal-family 1:1 direction and a significant new formal-verification result, but AGPL-3.0-only legal compatibility and explicit unsupported third-party use remain unresolved. The DeepMind evidence is for v0.105.4 and a constrained AArch64 CLI model, not the current v0.106.2 release or FLY's complete multi-device/mobile architecture.
- **Option B — Matrix Rust SDK:** Apache-2.0 repository license and real Kotlin/Swift UniFFI mobile bindings provide a credible integration path. However, it is a Matrix protocol/client architecture, not a drop-in Signal crypto library. Adopting it changes identity, device, room/event, sync, storage, recovery, metadata and service assumptions. Exact current crypto audit coverage and FLY-specific security boundary remain unproven.
- **Option C — defer:** avoids an irreversible protocol/library choice while legal, assurance, architecture and platform evidence is incomplete. It preserves product integrity and prevents unsupported security claims.

### 1.3 Formal gate state

| Decision | Current state |
|---|---|
| libsignal selected | NO |
| Matrix Rust SDK selected | NO |
| E2EE production implementation authorized | NO |
| Current execution posture | OPTION C — DEFER |
| D-01 | OPEN |
| Legal approval for libsignal | BLOCKED |
| Matrix architecture adoption approval | NOT GRANTED |
| Current-version assurance closure | BLOCKED |
| FLY Android/iOS integration validation | NOT TESTED |
| Supply-chain verification | NOT TESTED |
| Technical spike | NOT AUTHORIZED |
| Production release | NO-GO |

---

## 2. Decision scope and non-negotiable invariants

This gate decides only the crypto architecture path for production E2EE. It does not approve a UI implementation, server deployment, product launch or platform release.

All paths must preserve these invariants:

1. FLY ID is a random opaque identifier and is not a cryptographic key.
2. Identity keys, device keys, session keys and transport identifiers remain separate.
3. Private identity/device keys, ratchet secrets and Recovery Kit secrets remain client-controlled and are never sent to the backend.
4. Backend relay/storage receives ciphertext and strictly minimized routing metadata, not plaintext or decryption keys.
5. No server-side plaintext escrow or server-held recovery secret.
6. Contact verification and key transparency are independently designed; a library's default trust model is not automatically FLY's trust model.
7. Every device has explicit enrollment, authorization, revocation and key-rotation lifecycle.
8. Session/ratchet persistence is atomic, corruption-aware and rollback-resistant.
9. Multi-device concurrency, duplicate/reordered/replayed messages and state migration are part of the security model.
10. File/media encryption, metadata stripping, temporary-file handling and object-level authorization are separate gates.
11. Calls require application-level media E2EE; WebRTC DTLS-SRTP alone is not proof of end-to-end confidentiality.
12. Web has a separate threat model; browser runtime compromise/XSS cannot be treated as native hardware-backed parity.
13. No custom cryptographic protocol, home-grown ratchet, or primitive assembly.
14. No product copy may claim screenshot prevention, anonymity or compromise resistance beyond tested platform guarantees.
15. TOR MODE remains excluded.
16. Decentralization remains future-only and is not part of MVP.

---

## 3. Candidate decision matrix

### 3.1 Hard-gate comparison

Legend:
- **BLOCKED:** mandatory condition not met.
- **PARTIAL:** some primary evidence exists, but it does not close the requirement.
- **UNKNOWN:** sufficient evidence not established.
- **NOT TESTED:** no FLY build, integration or validation occurred.
- **CONDITIONAL:** candidate may be reconsidered only after listed conditions pass.

| Decision dimension | A — libsignal | B — Matrix Rust SDK | C — Defer E2EE |
|---|---|---|---|
| Protocol fit to current Signal-family 1:1 intent | Strong | Different protocol / Matrix semantics | No protocol commitment |
| Legal status | AGPL-3.0-only confirmed; counsel approval BLOCKED | Apache-2.0 repo confirmed; full dependency/legal review PARTIAL | No crypto dependency introduced |
| Upstream posture | Explicitly unsupported for third-party use | Maintained SDK; FLY-specific support/commitment not established | No dependency support assumption |
| Independent assurance | DeepMind formal proof for libsignal v0.105.4 scope; not current release or FLY system | Historical vodozemac audit exists; current SDK crypto revision mapping incomplete | No cryptographic assurance claim |
| Android binding | Java API and Android artifacts documented | Kotlin UniFFI binding documented | Not applicable yet |
| iOS binding | Swift/Rust FFI documented; exact current packaging must be confirmed | Swift UniFFI binding documented | Not applicable yet |
| FLY-specific native custody boundary | NOT TESTED | NOT TESTED; broader SDK surface requires analysis | No engine boundary yet |
| FLY ID and directory compatibility | Adapter/design review required | Identity model and directory ADR likely reopen | Remains uncommitted |
| Multi-device and revocation | DeepMind proof scope excludes multi-device state; FLY mapping open | Matrix device semantics exist; FLY lifecycle mapping open | No production multi-device crypto claim |
| Recovery Kit compatibility | Not supplied by library alone | Matrix backup/dehydrated-device semantics require redesign | Preserve current no-escrow requirement |
| Custom relay / metadata minimization | FLY envelope/service integration review required | Matrix homeserver/sync/event coupling is material | No protocol service coupling introduced |
| Current exact-version audit coverage | PARTIAL | UNKNOWN / PARTIAL | No candidate audit claim |
| Supply-chain reproducibility | NOT TESTED | NOT TESTED | No new crypto supply chain |
| Migration cost | Moderate-to-high; legal and support blockers | High; protocol and service redesign | Delays E2EE but preserves optionality |
| Current eligibility | BLOCKED | NOT READY | ADMISSIBLE CURRENT POSTURE |
| Final candidate selected | NO | NO | Execution posture only; D-01 OPEN |

### 3.2 Weighted scoring is intentionally withheld

No numeric weighted score is published. A weighted average can hide a fatal legal or security blocker behind strong performance or platform scores. A candidate must pass every mandatory gate before secondary scoring (performance, developer experience, binary size, operational cost) is meaningful.

The selection rule is lexicographic:
1. legal and licensing viability;
2. protocol/security assurance;
3. identity/device/recovery compatibility;
4. Android/iOS security boundary and parity;
5. supply-chain provenance and maintenance posture;
6. operational support and upgrade path;
7. only then performance, binary size and engineering cost.

Any BLOCKED item in gates 1–5 means NO-GO regardless of aggregate score.

---

## 4. Option A — Conditional continuation with libsignal

### 4.1 Evidence supporting reconsideration

- Signal's repository provides a Signal-family implementation and documented Java/Android and Swift/Rust FFI surfaces.
- The repository's v0.106.2 release was observed in Phase 0.9.
- Google DeepMind's 2026-11-10 paper reports machine-checked verification at machine-code level for unmodified libsignal v0.105.4 with SPQR v1.6.0, covering a constrained one-to-one PQXDH and Triple Ratchet model.
- This is materially stronger evidence than a protocol description or unit-test claim.

### 4.2 Evidence limitations that remain decisive

1. **License:** AGPL-3.0-only is confirmed. No qualified counsel memo establishes compatibility with FLY SECRET's intended mobile distribution, commercial model, native linking/FFI, server-side components and proprietary application code.
2. **Upstream support:** official README says third-party use outside Signal is unsupported. A public vulnerability reporting channel is not an integration support SLA.
3. **Version gap:** formal verification concerns v0.105.4, not the v0.106.2 release observed in Phase 0.9.
4. **Scope gap:** the paper's proof harness does not establish the security of Signal's full mobile applications, FLY's native adapters, persistence, recovery, key transparency or multi-device lifecycle.
5. **Platform gap:** documented bindings are evidence of API availability, not proof of FLY's exact build, stable third-party contract, secret custody, platform parity or artifact provenance.
6. **Supply-chain gap:** no FLY verification of signatures, checksums, SBOM, transitive license graph or reproducible native artifacts has occurred.

### 4.3 Conditions to reopen Option A

All conditions are mandatory:

- **A-L1 Legal:** written counsel opinion addressing AGPL-3.0-only for exact Android/iOS packaging, FFI/linking, distribution, server use, notices, source obligations and commercial model.
- **A-U1 Upstream:** written upstream clarification if obtainable; if no support is offered, product owner and security owner must explicitly accept the unsupported dependency risk. Silence is not acceptance.
- **A-F1 Formal evidence:** version-by-version mapping from verified v0.105.4 source to the exact proposed immutable commit; review all changes affecting handshake, ratchet, state and platform bindings.
- **A-S1 Scope review:** independent cryptographic review of FLY-specific multi-device, persistence, recovery, key transparency, relay envelope and platform lifecycle.
- **A-P1 Platforms:** Android and iOS proof plan with exact artifacts, API surface, key custody, error handling and parity requirements.
- **A-C1 Supply chain:** verified tag/signatures, artifact digests, SBOM, full license/advisory graph, provenance and reproducible-build assessment.
- **A-R1 Residual risk:** written risk acceptance for unsupported upstream status, with named owner, monitoring, fork contingency and emergency patch plan.

If counsel finds the license incompatible or ambiguous, Option A remains NO-GO. A native wrapper does not automatically avoid license obligations.

---

## 5. Option B — Matrix Rust SDK architecture

### 5.1 Evidence supporting reconsideration

- Matrix Rust SDK repository is Apache-2.0.
- SDK has documented Kotlin and Swift UniFFI bindings and production use in Element X.
- `matrix-sdk-crypto` provides a higher-level Matrix encryption state machine; it is not merely a thin vodozemac wrapper.
- vodozemac is a distinct Rust implementation of Matrix Olm/Megolm. Its historical Least Authority audit is evidence of an external review, but not a current audit of every crate/revision shipped in Matrix Rust SDK 0.19.1.
- SDK release/security history demonstrates that security fixes are published, but patch history does not equal a current independent crypto audit.

### 5.2 Architectural cost and non-equivalence

Option B is not “replace one crypto library with another.” It potentially changes:
- user/device identity semantics;
- key publication, cross-signing and trust relationships;
- 1:1 and group protocol semantics;
- room/event/sync and server interactions;
- local crypto state persistence and migrations;
- multi-device verification and revocation;
- key backup and recovery flows;
- metadata exposed to homeserver/service infrastructure;
- native binding surface and SDK storage assumptions.

FLY's random opaque FLY ID, custom relay, independent transparency monitors/witnesses, no-server-escrow Recovery Kit and strict secret custody boundary must be mapped explicitly. Matrix's defaults cannot be assumed to meet these requirements.

### 5.3 Conditions to reopen Option B

All conditions are mandatory:

- **B-L1 Legal:** counsel review of Apache-2.0 SDK and every transitive/native dependency, notices, bundled assets and generated bindings.
- **B-A1 Audit:** obtain and map exact independent audit reports to the exact `matrix-sdk-crypto`, vodozemac and binding revisions intended for use. Reconcile historical Least Authority findings I/J and Suggestion 8 with current code and protocol constraints.
- **B-P1 Platforms:** demonstrate that the current Kotlin/Swift FFI exposes the required functions without leaking private keys or ratchet state into JS or application logs.
- **B-I1 Identity:** new ADR for FLY ID to Matrix user/device identity mapping, directory and trust-on-first-use/cross-signing implications.
- **B-M1 Messaging:** new ADR for Olm/Megolm semantics, wire format, group scope, event metadata and relay/homeserver boundary.
- **B-D1 Devices:** formal per-device enrollment, revocation, rotation, concurrency and state migration mapping.
- **B-R1 Recovery:** prove that the selected recovery design does not create server-side plaintext escrow or weaken device trust.
- **B-T1 Transparency:** explain how Matrix verification/cross-signing relates to FLY's independent key-transparency log and monitor/witness model; do not claim equivalence without proof.
- **B-S1 Supply chain:** exact release pin, signed source, dependency graph, SBOM, advisory report, FFI artifacts, provenance and reproducibility evidence.
- **B-O1 Operations:** document homeserver/service dependencies, metadata, upgrades, migration and incident response.

If FLY does not intend to adopt Matrix protocol and service semantics, Option B should be rejected rather than partially embedded as a supposedly protocol-neutral crypto library.

---

## 6. Option C — Defer production E2EE implementation

### 6.1 What deferral means

- D-01 remains OPEN; no protocol or crypto library is selected.
- No production E2EE implementation, encrypted-message claim or security badge may be shipped.
- No plaintext-to-ciphertext relay contract is represented as complete.
- No custom cryptographic substitute is permitted.
- Architecture documentation, threat-model refinement and product work that does not make cryptographic security claims may proceed only with separate scope authorization.
- Any data handling during a non-E2EE prototype must be explicit, minimized, access-controlled and never mislabeled as end-to-end encrypted.
- Before real users or sensitive data are involved, a separate privacy/security gate is required.

### 6.2 Benefits

- avoids accepting AGPL obligations without counsel;
- avoids building on an explicitly unsupported third-party dependency without formal risk acceptance;
- avoids silently adopting Matrix protocol and infrastructure assumptions;
- prevents false assurance based on a historical audit or a version-mismatched formal proof;
- preserves ability to select a maintained and legally viable implementation later;
- prevents product architecture from accumulating irreversible protocol-specific coupling.

### 6.3 Costs and risks

- delays E2EE product delivery;
- may require re-planning feature sequencing and launch commitments;
- increases risk that non-E2EE prototype interfaces are mistaken for production security unless clearly labeled;
- requires disciplined separation between future E2EE architecture and any temporary data flow.

### 6.4 Deferral exit criteria

Deferral ends only when one candidate passes all mandatory hard gates, a new ADR is approved, and a separate implementation authorization is given. The end of this phase does not automatically authorize a spike.

---

## 7. Formal decision rule

The following decision tree governs any future reconsideration:

1. Is a candidate legally compatible with the intended product and distribution model, documented by counsel?
   - No / unclear → Option C.
2. Is there adequate current-version independent assurance and a documented upstream maintenance/security response path?
   - No / unclear → Option C.
3. Does the candidate preserve FLY identity, device, recovery, transparency and relay invariants without custom cryptography?
   - No / unclear → Option C or reject that candidate.
4. Are Android and iOS bindings and native secret custody boundaries proven for the exact version?
   - No / unclear → Option C.
5. Is the exact dependency graph, artifact provenance and upgrade process acceptable?
   - No / unclear → Option C.
6. Have product owner, security owner and architecture owner approved the candidate and its residual risks?
   - No → D-01 remains OPEN; Option C.
7. Only after all above: prepare a separate, explicitly authorized technical spike plan with measurable acceptance criteria.

A candidate cannot self-promote from PARTIAL/UNKNOWN to PASS based on project marketing, API availability, a repository license badge, an old audit, or a proof for a different version.

---

## 8. ADR reopening requirements

### 8.1 Required ADRs for libsignal path

- D-01 protocol/library: exact libsignal version, commit, license and assurance scope.
- D-04 multi-device lifecycle: per-device sessions, revocation, rotation, concurrency and state rollback.
- D-05 recovery: client-only Recovery Kit, device transfer and identity re-establishment.
- D-06 1:1/group messaging: protocol envelope, handshake, serialization and group deferral.
- D-09 native boundary: Android Java and iOS Swift/Rust FFI, secret custody, persistence and errors.
- D-11 supply chain: Maven/Swift/Rust artifacts, signatures, SBOM, licensing, provenance and patch policy.
- D-03 key transparency and D-08 metadata/privacy must be reopened if the chosen integration changes key publication or relay/service assumptions.

### 8.2 Required ADRs for Matrix SDK path

All relevant ADRs must be reopened:
- D-01 protocol/library;
- D-02 FLY ID and identity binding;
- D-03 key transparency and trust directory;
- D-04 multi-device lifecycle;
- D-05 Recovery Kit and re-establishment;
- D-06 1:1/group protocol and event/wire format;
- D-08 metadata privacy, homeserver/relay and sync boundary;
- D-09 native boundary and UniFFI exposure;
- D-10 Web security if SDK crypto/WASM is considered;
- D-11 supply chain and release pipeline.

### 8.3 Required ADR content

Every reopened ADR must include:
1. exact candidate, immutable commit and release artifact identifiers;
2. legal memo and full license inventory;
3. audit report identifiers, exact audited revisions and finding-by-finding mapping;
4. upstream support and vulnerability response evidence;
5. protocol and wire compatibility statement;
6. identity/device/recovery/transparency data-flow diagrams;
7. Android/iOS API and secret custody boundary;
8. dependency graph, SBOM, signature and provenance evidence;
9. unresolved findings and explicit residual risk owners;
10. rollback, migration and emergency upgrade plan;
11. product owner, security owner and architecture owner sign-off;
12. clear GO/NO-GO and expiry/review trigger.

---

## 9. Required owners and approval record

No approval is implied by this document. Before D-01 can close, record explicit approval from:

| Role | Required decision |
|---|---|
| Product owner | Accept product/protocol direction, scope and delivery consequences |
| Qualified legal counsel | Written licensing/distribution opinion for exact dependency graph |
| Security owner | Accept assurance scope and residual security risks |
| Architecture owner | Approve identity/device/recovery/relay/native-boundary mapping |
| Mobile platform owners | Confirm Android/iOS build, lifecycle and secret custody feasibility |
| Release/supply-chain owner | Approve artifact provenance, dependency policy and update process |

Approval record currently:
- Product owner: NOT RECORDED.
- Legal counsel: NOT OBTAINED.
- Security owner: NOT RECORDED.
- Architecture owner: NOT RECORDED.
- Android/iOS platform sign-off: NOT RECORDED.
- Supply-chain sign-off: NOT RECORDED.

---

## 10. Risks and release implications

### 10.1 Critical risks if implementation starts prematurely

- AGPL compliance or distribution obligations discovered after app integration.
- Unmaintainable fork/patch burden because third-party upstream use is unsupported.
- Formal proof scope incorrectly represented as covering current release or mobile/multi-device implementation.
- Matrix adoption introduces identity, metadata and server dependencies not reflected in threat model.
- Recovery flow accidentally introduces server escrow or weakens device trust.
- Native bindings expose secret state or permit rollback/replay through persistence.
- Security claims are made before independent validation.

### 10.2 Release policy

Until D-01 is closed and later release gates pass:
- no public E2EE claim;
- no production cryptographic identity or secure messaging release;
- no use of NOT_RUN test scenarios as evidence of security;
- no security marketing language that overstates screenshot protection, anonymity or compromise resistance;
- no production sensitive-data handling in a prototype without a separately approved privacy/security model.

---

## 11. Red-Team and project constraints

The 27 unique Red-Team scenario IDs remain `NOT_RUN`:
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

No Red-Team scenario was executed or reclassified in Phase 1.0. `NOT_RUN` is not PASS.

TOR MODE remains excluded from product, architecture, UI, configuration, tests and roadmap. Decentralization remains future-only and is not part of MVP or this decision gate.

---

## 12. Phase 1.0 execution record

**Performed:**
- fetched Phase 0.9 evidence review and Phase 0.8 decision baseline from the approved base branch;
- fetched the existing Red-Team test catalog and confirmed 27 unique scenario IDs and 32 `NOT_RUN` mentions before authoring;
- created a dedicated branch from Phase 0.9;
- prepared this formal three-path decision gate and explicit reopening criteria.

**Not performed:**
- no new legal research or legal opinion;
- no counsel engagement;
- no upstream inquiry or response;
- no library selection;
- no code, wrapper, prototype or technical spike;
- no build, benchmark, unit/integration/security test;
- no artifact signature verification, SBOM generation or reproducible build;
- no Red-Team execution;
- no main branch change;
- no Pull Request creation.

---

## 13. Source baseline

This decision relies on the detailed primary-source register in Phase 0.9:
`docs/fly-secret/architecture/phase-0.9/FLY_CRYPTO_CANDIDATE_EVIDENCE_REVIEW_PHASE_0.9.md`

Key evidence to preserve:
- Signal libsignal repository, license, release history, README support disclaimer, Java/Android and Swift/FFI documentation.
- Google DeepMind, *AlphaProof Code: Verifying Signal’s Security at the Machine-Code Level* (2026-11-10), including version and model limitations.
- vodozemac repository, Cargo manifest and Least Authority *Matrix vodozemac Final Audit Report* (2022-03-30), including unresolved historical findings I/J and Suggestion 8.
- Matrix Rust SDK repository, release history, Kotlin/Swift FFI manifests and Cargo dependency policy.

No evidence in Phase 0.9 or this gate should be interpreted as proof of FLY integration, current-version full-system security, legal approval or product-owner sign-off.

---

## 14. Formal Phase 1.0 disposition

| Item | Result |
|---|---|
| Option A — libsignal | CONDITIONAL CANDIDATE / BLOCKED |
| Option B — Matrix Rust SDK | ARCHITECTURAL ALTERNATIVE / NOT APPROVED |
| Option C — defer production E2EE | CURRENT EXECUTION POSTURE |
| Permanent crypto architecture selected | NO |
| D-01 | OPEN |
| Legal blockers resolved | NO |
| Upstream support blockers resolved | NO |
| Current-version assurance gates closed | NO |
| Android/iOS FLY integration validated | NO |
| Supply-chain verification executed | NO |
| Technical spike authorized | NO |
| Production integration approved | NO |
| Red-Team scenarios | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: OPTION C — DEFER PRODUCTION E2EE; D-01 OPEN; NO-GO FOR IMPLEMENTATION.**

This is the only defensible current execution posture based on evidence available in Phase 0.9. It does not permanently select or reject libsignal or Matrix. Reconsideration requires closure of all candidate-specific hard gates, explicit owner approvals and a new ADR decision. A later technical spike requires separate authorization.
