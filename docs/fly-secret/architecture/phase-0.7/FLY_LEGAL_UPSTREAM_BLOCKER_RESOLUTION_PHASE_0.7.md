# FLY SECRET — Phase 0.7
## Legal & Upstream Blocker Resolution

**Status:** DOCUMENTATION COMPLETE — BLOCKERS UNRESOLVED — NO-GO FOR SPIKE  
**Baseline:** `security/fly-secret-crypto-integration-feasibility-0.6`  
**Branch:** `security/fly-secret-legal-upstream-resolution-0.7`  
**Candidate under review:** Signal `libsignal` (conditional only)  
**Decision owner:** FLY SECRET product/security owners, with qualified external legal counsel for licensing questions.

> This is a decision-preparation dossier. It is not a legal opinion, does not grant a license, does not represent an upstream response, and does not establish technical compatibility. No request was sent to Signal maintainers from this phase. No technical spike, dependency addition, application code, build, or test was run.

---

## 1. Executive disposition

**Current decision: NO-GO for technical spike and NO-GO for production integration.**

The blockers identified in Phase 0.6 remain open:

- **L-01 / AGPL legal review:** no written counsel opinion is present in the project evidence.
- **U-01 / upstream support and API position:** no written upstream clarification is present. The Phase 0.5 dossier records that the project describes third-party use as unsupported and warns that APIs/implementations may change without notice.
- **A-01 / FLY architecture compatibility:** no signed compatibility assessment against a pinned immutable revision exists.
- **S-01 / supply-chain baseline:** no immutable candidate artifact set has been approved for an isolated spike.

This phase prepares the legal checklist, an upstream inquiry package ready for human review and sending, architecture acceptance criteria, and a formal decision matrix. It does not resolve blockers by assertion.

### What this decision means

| Activity | Current status |
|---|---|
| Continue documentation and evidence collection | GO |
| Obtain external AGPL legal opinion | GO |
| Send the prepared upstream inquiry through an authorized project contact | GO |
| Perform architecture review on public documentation/source without integrating artifacts | GO, subject to normal review controls |
| Create isolated Android/Kotlin or iOS/Swift spike | **NO-GO** |
| Add libsignal to app or native build | **NO-GO** |
| Write production crypto/native integration code | **NO-GO** |
| Select libsignal for production | **NO-GO** |
| Replace it silently with Matrix or custom crypto | **NO-GO** |

## 2. Evidence baseline and limitations

Phase 0.5 recorded these findings for `libsignal`:

- Signal Protocol implementation including Double Ratchet, with Rust internals and Java, Swift and TypeScript interfaces described by upstream.
- Repository license recorded as `AGPL-3.0-only`.
- Upstream README records that use outside Signal is unsupported and APIs/implementations may change without notice.
- Android Java and iOS Swift surfaces exist in the upstream project.

Phase 0.6 translated those findings into legal, upstream, architecture and supply-chain blockers and proposed a native-owned secret boundary.

**Evidence limitation:** the statements above are inherited from the Phase 0.5 research record. This Phase 0.7 document does not claim a fresh verification of the current upstream head, a current release, or the legal effect of any particular linking/distribution model. Before a decision, reviewers must inspect the exact candidate tag/commit and its complete dependency/artifact set.

## 3. Legal review checklist — AGPL-3.0-only

### 3.1 Counsel mandate

Engage qualified counsel with demonstrated experience in:
- AGPL-3.0 and copyleft software licensing.
- Mobile binary distribution (Android APK/AAB and iOS application packages).
- Native libraries, Rust FFI, Java/Kotlin and Swift bindings.
- Proprietary/commercial software and mixed-license products.
- Hosted services and network interaction obligations.
- Open-source compliance programs, notices and corresponding-source delivery.

The review must be based on the actual intended product and distribution model, not a hypothetical generic app.

### 3.2 Product facts counsel must receive

Provide counsel with a factual architecture and distribution pack:

- FLY SECRET is intended as a cross-platform secure messenger.
- Mobile clients use React Native/TypeScript with native Kotlin/Swift security adapters.
- The proposed crypto library would be linked/embedded through platform-specific native artifacts; exact mechanism must be confirmed against the pinned revision.
- The application may be proprietary and commercially distributed; product owner must confirm intended license and monetization model.
- Backend services relay ciphertext and limited metadata; they are not intended to receive plaintext or client private/ratchet secrets.
- The app may use a custom relay and FLY ID/device directory rather than Signal-operated services.
- The exact native bridge, generated bindings, build scripts, modifications and transitive dependencies must be disclosed.
- No production integration has occurred as of this phase.

### 3.3 Required legal questions

Counsel must answer each question with a reasoned conclusion, relevant license clauses, assumptions, and unresolved risks.

| ID | Question for counsel | Required disposition |
|---|---|---|
| LEG-01 | What obligations arise from embedding/linking the exact `libsignal` artifacts in Android and iOS binaries? | Written analysis for each platform and linkage method |
| LEG-02 | Does static vs dynamic linking, FFI, JNI, Swift wrapper, or generated binding materially change the analysis? | Platform-specific conclusions; no unsupported assumptions |
| LEG-03 | What source-code obligations apply to the combined distributed client, native adapter, bridge and modifications? | Define the scope of corresponding source, if any |
| LEG-04 | Can the React Native application and unrelated modules remain under their intended license? | Explicit conclusion and conditions |
| LEG-05 | What obligations arise for modifications, patches, generated code, build scripts and interface wrappers? | Scope and publication/delivery process |
| LEG-06 | Do hosted-service/network features trigger additional AGPL obligations for the planned architecture? | Analyze actual client/server boundaries and network use |
| LEG-07 | What notices, license texts, attribution and source-access mechanisms must ship? | Concrete compliance checklist |
| LEG-08 | What obligations apply to transitive dependencies and bundled native artifacts? | Complete dependency/license inventory requirements |
| LEG-09 | Are there patent, trademark, export-control or other license interactions requiring separate review? | Identify issues and responsible reviewer |
| LEG-10 | Can FLY distribute through Google Play, Apple App Store, enterprise channels and direct download under the proposed model? | Channel-specific conclusion and restrictions |
| LEG-11 | What ongoing obligations apply to updates, forks, security patches and end-of-life? | Operational compliance plan |
| LEG-12 | Is the intended commercial/proprietary model acceptable, and what changes would be required if not? | Explicit GO / conditional GO / NO-GO recommendation |

### 3.4 Evidence checklist

- [ ] Counsel engagement and scope documented.
- [ ] Exact candidate release/tag/commit identified for review.
- [ ] Full upstream LICENSE and relevant notices archived.
- [ ] Android and iOS artifact coordinates and checksums documented.
- [ ] Complete direct/transitive dependency inventory and SPDX report produced.
- [ ] Native linkage, FFI, wrapper and bridge architecture supplied to counsel.
- [ ] Product distribution, licensing and monetization assumptions confirmed by product owner.
- [ ] Written counsel memo addresses LEG-01 through LEG-12.
- [ ] Required notices/source publication process estimated and assigned.
- [ ] Product owner formally accepts the obligations and residual risks.
- [ ] Security owner confirms legal constraints do not force unsafe architecture or unmaintainable fork.

**Legal gate pass rule:** all material questions must have a written answer; any conditional approval must list enforceable conditions, owners and release gates. A verbal opinion, informal chat, generic FAQ, or assumption that native isolation avoids copyleft is not sufficient.

### 3.5 Legal NO-GO triggers

Reject `libsignal` for this product path if counsel concludes that:
- required source disclosure is incompatible with the approved product license/business model;
- distribution channels or intended deployment model cannot meet the license conditions;
- compliance scope is materially ambiguous and cannot be resolved before integration;
- required modifications or fork maintenance create unacceptable security/supply-chain risk;
- product owners decline the obligations or cannot operationally satisfy them.

Do not reinterpret or work around license obligations by moving code behind a native boundary.

## 4. Upstream inquiry package — ready for review and sending

**Recipient:** maintainers/project contact designated by the official Signal `libsignal` repository.  
**Sending status:** NOT SENT by this phase.  
**Purpose:** clarify the practical third-party integration/support position before any isolated feasibility work.

### 4.1 Draft inquiry

Subject: Third-party integration feasibility and supported API surface for libsignal

Hello libsignal maintainers,

We are evaluating whether libsignal could be a suitable cryptographic protocol implementation for a separate secure-messaging product. We have read the repository documentation, including the statement that use outside Signal is unsupported and that APIs and implementations may change without notice. We do not want to infer support or compatibility that the project does not provide.

Before deciding whether to proceed with a strictly isolated feasibility evaluation, could you clarify the following points?

1. Is third-party use of libsignal permitted under the published license, subject to compliance with its terms, even though it is unsupported? We understand this is not a request for legal advice.
2. Are the Java/Android and Swift/iOS interfaces intended to be consumed by independent applications, or are they internal implementation surfaces for Signal clients?
3. Is there a documented compatibility or versioning policy for those interfaces, generated bindings, native artifacts and serialized protocol state?
4. Are there known constraints on embedding the Android and iOS artifacts in an application with its own identity service, device directory, key-transparency system and ciphertext relay?
5. Does the protocol implementation assume any Signal-operated service behavior that an independent service would need to reproduce? If so, is there public documentation describing those assumptions?
6. Are there restrictions or recommended practices for independent applications implementing their own account/device lifecycle, pre-key publication, device revocation and session recovery?
7. What is the recommended process for downstream integrators to receive security advisories, report vulnerabilities and learn about breaking API or state-format changes?
8. Are there any published integration examples or tests that demonstrate interoperability across Android and iOS clients using the same protocol revision without Signal service infrastructure?
9. Are there known limitations around concurrent session operations, persistence, process restart, state migration or rollback that downstream integrators should account for?
10. If no third-party support is offered, can you confirm that an integrator should treat the APIs and artifacts as unsupported and independently own compatibility, security maintenance and migration risk?

We are not asking for a support commitment if the project does not offer one. A pointer to authoritative documentation or a clarification of the project's intended boundaries would be helpful.

Thank you.

### 4.2 Inquiry handling requirements

- Send only through an authorized FLY SECRET project account/contact.
- Do not include private keys, user data, unpublished security details or proprietary credentials.
- Preserve the exact sent text, date, sender role and public/private channel used.
- Archive the response verbatim with a timestamp and source link, then summarize it separately.
- If no response arrives, record **NO RESPONSE** after a product-owner-defined waiting period; do not interpret silence as permission or support.
- If maintainers decline support, record that fact and assess whether unsupported downstream maintenance is acceptable.
- Do not state that upstream has approved FLY SECRET or endorsed the integration unless an explicit, attributable statement supports that claim.

### 4.3 Upstream gate pass rule

U-01 can be considered resolved only when:
- the published unsupported-use status is acknowledged;
- intended API surfaces and versioning limits are documented from authoritative evidence;
- security advisory/vulnerability handling is understood;
- all material service/protocol assumptions are mapped;
- FLY owners explicitly accept the residual risk or reject the candidate.

A positive answer does not override the license gate or prove technical compatibility.

## 5. Architecture compatibility review

The review is about fitting the candidate protocol implementation into FLY's security architecture without modifying cryptographic protocol logic.

### 5.1 Mandatory compatibility criteria

| ID | Criterion | Required evidence | Failure disposition |
|---|---|---|---|
| ARC-01 | FLY ID remains an opaque identifier separate from cryptographic identity keys | Identity data-flow and key ownership diagram | NO-GO |
| ARC-02 | No dependency on Signal account/directory/relay services is required for the intended deployment | Service assumption inventory mapped to FLY components | NO-GO or reopen architecture |
| ARC-03 | Native layer owns private identity/device keys and all ratchet secrets | API/data-flow review and negative bridge tests in a later authorized spike | NO-GO |
| ARC-04 | Android and iOS expose equivalent security-relevant operations | Exact pinned API comparison and cross-platform contract | NO-GO |
| ARC-05 | Per-device sessions, pairing, revocation and re-establishment fit FLY lifecycle | Device state machine and lifecycle test plan | NO-GO |
| ARC-06 | Session persistence is atomic and crash-consistent | Persistence design and failure semantics from exact implementation | NO-GO |
| ARC-07 | Replay, reorder, duplicate delivery and concurrent operations are safely handled | Protocol behavior mapping and deterministic test plan | NO-GO |
| ARC-08 | Key changes invalidate stale Verified status and integrate with transparency proofs | Trust-state transition model | NO-GO |
| ARC-09 | No custom protocol or cryptographic modifications are required | Independent security architecture review | Reject candidate |
| ARC-10 | Recovery Kit remains client-controlled; no server-side secret escrow | Recovery data-flow and storage boundary review | NO-GO |
| ARC-11 | Native state migration/rollback cannot silently restore stale ratchet state | Versioning and rollback threat analysis | NO-GO |
| ARC-12 | Relay receives ciphertext and minimum routing metadata only | Backend data-flow and envelope contract | NO-GO |
| ARC-13 | Errors, telemetry, crash reporting and diagnostics cannot expose secret state | Logging/observability data inventory | NO-GO |
| ARC-14 | Dependency update, security patch and artifact provenance process is sustainable | SBOM, immutable pin, advisory and release process | NO-GO |

### 5.2 Explicitly out of scope for this candidate decision

- Group messaging / MLS.
- Voice/video calls and media E2EE.
- Web/Desktop crypto implementation.
- Decentralized identity, decentralized relays or consensus.
- TOR MODE.

These remain separate future decisions. This review must not accidentally expand MVP scope.

### 5.3 Compatibility review output

The reviewer must produce:
1. Exact library tag/commit and artifact manifest.
2. FLY-to-library protocol operation mapping.
3. Android/iOS API parity table.
4. FLY ID, device lifecycle, transparency and recovery data-flow diagrams.
5. Session persistence and crash consistency analysis.
6. Threat analysis of bridge, native storage, logging and state migration.
7. List of upstream assumptions and unsupported dependencies.
8. Signed recommendation: PASS, CONDITIONAL PASS or FAIL for each ARC criterion.

No criterion may be marked PASS solely because a method or binding exists in source code.

## 6. Formal GO / NO-GO decision framework

### 6.1 Decision gates

| Gate | Owner | GO evidence | Current state |
|---|---|---|---|
| L-01 Legal | External counsel + product owner | Written LEG-01..12 opinion and accepted obligations | **BLOCKED** |
| U-01 Upstream | Crypto dependency owner | Archived authoritative clarification and residual risk acceptance | **BLOCKED** |
| A-01 Architecture | Security architect | ARC-01..14 review with no unresolved critical incompatibility | **BLOCKED** |
| S-01 Supply chain | Build/security owner | Immutable revision, checksums, SBOM, license and provenance inventory | **BLOCKED** |
| B-01 Boundary | Mobile/security owners | Approved native API contract and secret-flow review | **DESIGNED; NOT VALIDATED** |
| P-01 Spike plan | Engineering owner | Timebox, isolated workspaces, synthetic data, stop conditions and approval | **PREPARED; NOT AUTHORIZED** |

### 6.2 GO criteria for an isolated technical spike

A spike may begin only when every condition below is true:

- L-01 is PASS or a formally accepted conditional PASS with no unresolved legal prohibition.
- U-01 is PASS or residual unsupported-use risk is explicitly accepted by the accountable owners.
- A-01 is PASS for all critical architecture criteria; no custom crypto or protocol fork is required.
- S-01 identifies immutable artifacts and a reviewable dependency chain.
- B-01 is approved by mobile and security owners.
- P-01 has a named owner, timebox, isolated workspace, synthetic fixtures, evidence requirements and stop conditions.
- A separate explicit authorization to start the spike is recorded.

**A GO for a spike is not a GO for production adoption.** Production selection requires spike evidence, independent review, security testing, legal compliance implementation and a separate release decision.

### 6.3 Immediate NO-GO criteria

Any one of the following keeps the decision at NO-GO:

- No written legal review or counsel says intended distribution is incompatible.
- Upstream API/support status is materially unclear and owners have not accepted the risk.
- FLY service architecture depends on unsupported or unavailable Signal-operated infrastructure.
- Android/iOS APIs cannot implement equivalent required security behavior.
- Integration requires a custom cryptographic protocol, protocol fork or secret export to JavaScript.
- State persistence, crash recovery, device revocation or key verification cannot be made safe.
- Dependency provenance, licensing or security maintenance cannot be established.
- The proposed spike cannot be isolated from production app, signing credentials and user data.

### 6.4 Decision states

- **GO:** all gate evidence reviewed and approved; only the explicitly named next activity is authorized.
- **CONDITIONAL GO:** narrow activity authorized with documented conditions, owners and expiry; no implicit scope expansion.
- **NO-GO:** do not perform the activity. Continue only non-invasive documentation/evidence collection.
- **NOT ASSESSED:** insufficient evidence. For admission decisions, treat as NO-GO.

### 6.5 Current formal decision

**Decision: NO-GO for technical spike; NO-GO for production integration; GO for external legal review, upstream inquiry and non-invasive architecture evidence collection.**

Rationale:
- No legal opinion is recorded.
- No upstream response is recorded.
- No signed architecture compatibility review exists.
- No immutable artifact/SBOM package has been approved.
- Native boundary is designed on paper only and has not been validated.

Decision owner sign-off is pending. This document records the engineering disposition, not an external legal or maintainer decision.

## 7. Action register

| Action | Owner role | Output | Status |
|---|---|---|---|
| LEG-A — commission AGPL review | Product/legal owner | Written counsel memo | OPEN |
| LEG-B — provide factual product architecture pack | Security architect | Counsel briefing pack | PREPARED |
| UP-A — review and send upstream inquiry | Crypto dependency owner | Sent inquiry + archive | DRAFT READY / NOT SENT |
| UP-B — track response or non-response | Crypto dependency owner | Evidence record | NOT STARTED |
| ARC-A — complete pinned-revision compatibility review | Security architect | ARC-01..14 signed matrix | BLOCKED pending immutable candidate review |
| SC-A — produce dependency provenance/SBOM | Build/security owner | Artifact and license inventory | NOT STARTED |
| BND-A — approve native API contract | Mobile + security owners | Signed boundary contract | PROPOSED |
| DEC-A — convene GO/NO-GO review | Product/security owners | Signed decision record | NOT STARTED |

No named individual or external organization is represented as having completed an action.

## 8. Red-Team and project guardrails

- The catalog contains **27 unique Red-Team scenario IDs**: PRIV-01..04, ID-01..04, RELAY-01..02, CRYPTO-01..03, REC-01..03, FILE-01..03, CALL-01..03, DEC-01..03, SUPPLY-01 and LOG-01.
- All 27 remain **NOT_RUN**. No Red-Team test was executed or passed in Phase 0.7.
- Release gates remain unchanged; NOT_RUN/BLOCKED never count as PASS.
- TOR MODE remains excluded from product, architecture, UI, configuration, tests and roadmap.
- Decentralization remains future-only.
- No application code, native integration, dependency change, build, benchmark or test was performed.
- No PR was created; `main` was not modified.
- No private signing key, keystore or Recovery Kit secret was requested.

## 9. Next permissible step

The next permissible work is external evidence collection:
1. Product owner confirms the intended licensing/distribution model and commissions counsel.
2. Authorized project contact reviews and sends the upstream inquiry.
3. Security architect assembles a pinned-revision compatibility evidence pack once a candidate revision is selected for review.
4. Owners reconvene to decide whether a narrowly scoped spike can be authorized.

If legal review rejects AGPL-3.0-only, or the unsupported upstream position is unacceptable, stop the libsignal path and reopen the protocol/dependency decision. Do not quietly replace it with `vodozemac`, Matrix Rust SDK, primitives, or custom cryptography.

---

## References

- Phase 0.5 dependency evaluation: `docs/fly-secret/architecture/phase-0.5/FLY_CRYPTOGRAPHIC_DEPENDENCY_EVALUATION_PHASE_0.5.md`
- Phase 0.6 native boundary design: `docs/fly-secret/architecture/phase-0.6/FLY_CRYPTO_INTEGRATION_FEASIBILITY_NATIVE_BOUNDARY_PHASE_0.6.md`
- Phase 0.2 ADRs: `docs/fly-secret/architecture/phase-0.2/FLY_ARCHITECTURE_DECISION_RECORDS_PHASE_0.2.md`
- Phase 0.3 security blueprint: `docs/fly-secret/architecture/phase-0.3/FLY_ARCHITECTURE_CONSOLIDATION_SECURITY_BLUEPRINT_PHASE_0.3.md`
- Red-Team catalog: `docs/fly-secret/red-team/v1.0/FLY_RED_TEAM_TEST_CASES.md`
- Security release gates: `docs/fly-secret/red-team/v1.0/FLY_SECURITY_RELEASE_GATES.md`
- Upstream repository: https://github.com/signalapp/libsignal
- Upstream README: https://github.com/signalapp/libsignal/blob/main/README.md
- AGPL-3.0 text: https://www.gnu.org/licenses/agpl-3.0.html
