# FLY SECRET — Architecture Decision Closure & MVP Scope
- Phase: 0.4
- Version: 0.4
- Base branch: `security/fly-secret-architecture-blueprint-0.3`
- Working branch: `security/fly-secret-decision-closure-0.4`
- Status: Decision baseline prepared; formal product/security sign-off still required
- Scope: Architecture decisions, MVP boundary, implementation admission criteria; documentation only
- Application code: not written
- Red-Team scenarios: 27 × `NOT_RUN`
- TOR MODE: excluded from product, architecture, UI, configuration, tests and roadmap
- Decentralization: future-only; excluded from MVP

> This document closes the Phase 0.3 decision register as a recommended baseline. It is not a cryptographic audit, legal opinion, implementation result, test report, or automatic product-owner approval. No decision below should be represented as formally accepted by the product owner until the approval record is signed.

---

## 1. Executive decision

### 1.1 Recommended product boundary

Approve an **Android-first, client-controlled E2EE messenger MVP** with:
1. FLY ID registration without mandatory phone number or email.
2. 1:1 end-to-end encrypted text messaging.
3. Manual contact discovery through FLY ID / QR; no address-book upload in MVP.
4. Key fingerprint verification and transparency status, with key changes invalidating prior Verified state.
5. Explicit trusted-device pairing and revocation.
6. User-controlled Recovery Kit and identity re-establishment flow; no server-side plaintext escrow.
7. Encrypted file/photo transfer only after the file authorization, metadata, parser and temporary-file gates are closed.
8. Android local key custody and encrypted local message storage.
9. Minimal ciphertext relay and opaque push wake-up.
10. A FLY-specific protected CI pipeline producing unsigned test artifacts; production signing remains a separate protected release operation.

**iOS is the next platform after Android security and feature parity review.** It is not silently included in the first public MVP. Groups, voice/video calls, Web, Desktop and decentralization are outside the first MVP.

### 1.2 MVP exclusions

| Capability | Decision | Reason |
|---|---|---|
| Group messaging | Defer | MLS library integration, membership/epoch lifecycle, multi-device semantics and group removal/recovery require a separate decision and test matrix |
| Voice/video calls | Defer | Application media E2EE, key distribution/rotation, authenticated signalling, SFU behavior and ICE/IP disclosure are not closed |
| Web client | Defer | Browser runtime/XSS/extensions/service-worker threat model is independent; native key-custody guarantees do not transfer |
| Desktop client | Defer | Separate platform and update/supply-chain surface; no shortcut around Web/native gates |
| Voice messages | Defer from first MVP unless file/media gates and UX semantics are closed as a separately approved scope increment | Audio files introduce media parsing, metadata, cache and temporary-file risks |
| Contact discovery by address book | Exclude | Privacy-preserving discovery protocol and metadata leakage have not been approved |
| Decentralized network | Future-only | Not an MVP dependency; no node, consensus, peer-routing or Sybil-resistance implementation |
| TOR MODE | Explicitly excluded | Must not appear in requirements, code, UI, configuration, tests or roadmap |

### 1.3 Release posture

The MVP is **not authorized to begin implementation merely because this document exists**. Implementation admission requires:
- formal approval of the blocking decision baseline;
- exact crypto dependency and license decision;
- approved identity/device/recovery/message specifications;
- approved data inventory and retention policy;
- threat model and platform matrix sign-off;
- test plan and release evidence ownership;
- closure of all implementation-blocking Critical/High design questions.

Public release remains NO-GO until applicable release gates have executed and produced reproducible evidence. `NOT_RUN` and `BLOCKED` are never PASS.

---

## 2. Decision status model

- **Recommended baseline** — architecture direction is sufficiently clear to propose for approval; still requires product/security owner sign-off.
- **Conditional** — direction is recommended, but implementation cannot start until specified evidence or a lower-level specification exists.
- **Open** — no safe final selection is possible from current evidence; the decision remains blocked.
- **Deferred** — deliberately excluded from MVP; this is a scope decision, not a claim that the feature is technically solved.

No ADR is silently marked Accepted. The approval record in Section 5 must be completed by authorized owners.

---

## 3. Fourteen decision records

### D-01 — 1:1 cryptographic protocol implementation and license

**Options**
- A. Adopt a maintained Signal-family implementation after dependency, license, API and platform review.
- B. Adopt a different independently reviewed 1:1 protocol implementation with equivalent documented security goals.
- C. Build or combine a custom protocol/ratchet.

**Recommendation: A, conditional; C rejected.**

Rationale:
- The Phase 0.3 direction favors a Signal-family protocol for asynchronous 1:1 sessions, forward secrecy and post-compromise recovery.
- The official `signalapp/libsignal` repository describes platform-agnostic Java, Swift and TypeScript interfaces over Rust, but explicitly says use outside Signal is unsupported and APIs may change. Its repository metadata identifies AGPL-3.0. These are material integration and licensing constraints, not details to ignore.
- Before selection, conduct a documented legal review of AGPL obligations and product distribution model; API/ABI compatibility spike; Android/iOS build matrix; storage/state persistence review; maintenance/security-policy review; pinned-version reproducibility review; and independent cryptographic review scoped to the exact version and integration.
- If licensing or unsupported-use constraints are unacceptable, evaluate option B. Do not fork or adapt a library without an explicit maintenance and audit plan.
- No custom cryptographic primitives, protocol composition, ratchet or handshake.

**Status:** Conditional / implementation blocker. Exact library, version, license acceptance and reviewer sign-off remain OPEN.

**Required evidence:** dependency dossier, license opinion, version/hash pin, platform build proof, API lifecycle plan, threat-model mapping, independent review scope and migration/rollback plan.

References:
- https://github.com/signalapp/libsignal
- https://github.com/signalapp/libsignal/blob/main/README.md

### D-02 — Group messaging / MLS

**Options**
- A. Include MLS in the initial release.
- B. Evaluate MLS as a separate group protocol and defer groups from MVP.
- C. Design a custom group ratchet.

**Recommendation: B; groups deferred.**

Rationale:
- MLS is standardized in RFC 9420 and its architecture is described in RFC 9750, but neither standard supplies the application's membership authorization, identity binding, device lifecycle, delivery service, transparency, recovery or UI policy.
- OpenMLS is a plausible Rust implementation candidate, but its published platform matrix distinguishes targets built in CI from targets built and tested. Android/iOS/wasm targets must not be described as supported-and-tested merely because they compile in CI.
- A group protocol decision requires a separate implementation/version/license review, group membership and epoch lifecycle, device add/remove semantics, concurrent commits, delivery service integration, recovery and interop tests.
- No custom group ratchet.

**Status:** Deferred for MVP. MLS evaluation may occur after 1:1 foundation; no group code or dependency is authorized by this decision.

References:
- https://www.rfc-editor.org/rfc/rfc9420
- https://www.rfc-editor.org/rfc/rfc9750
- https://github.com/openmls/openmls

### D-03 — FLY ID format and registration proof

**Options**
- A. Random opaque public identifier generated from a cryptographically secure random source; public ID is distinct from all key material.
- B. User-chosen username as the primary cryptographic identity.
- C. Phone/email-derived identifier.

**Recommendation: A.**

Decision baseline:
- FLY ID is a public lookup identifier, not a password, bearer token, secret, private key, device key, session key or transport credential.
- Generate at least 128 bits of unpredictable identifier entropy using an OS-approved CSPRNG; encode canonically with a version prefix and checksum/error-detection suitable for human transcription. Exact encoding and collision handling must be specified before implementation.
- Registration uses a short-lived, single-use server challenge bound to the requested FLY ID, public identity-key fingerprint, device public key and protocol version. Client proves possession of the corresponding private key. Challenge consumption must be atomic and replay-safe.
- No mandatory phone/email. Do not expose a public enumeration endpoint; rate-limit lookup and registration. Manual FLY ID / QR contact addition is the MVP discovery path.
- Usernames, if later introduced, are aliases and must not replace cryptographic identity binding.

**Status:** Recommended baseline; exact wire format, challenge transcript, expiry, rate limits and collision policy are conditional/open.

### D-04 — Key transparency log and witness model

**Options**
- A. Central directory only.
- B. Append-only Merkle transparency log with signed tree heads, inclusion/consistency proofs, independent monitors/witnesses and client gossip.
- C. Fully decentralized consensus.

**Recommendation: B; C future-only.**

Decision baseline:
- Public identity-to-key/device bindings are versioned and auditable.
- Clients verify inclusion and consistency proofs and pin the latest accepted tree state.
- At least two operationally independent monitors/witnesses are required before production; the independence criteria, witness quorum, freshness threshold, split-view response and outage policy must be specified by security owners.
- Client-to-client gossip or equivalent cross-client comparison is required to reduce split-view risk.
- A key change, recovery lineage change, device-key replacement or failed proof invalidates previous Verified state and requires explicit re-verification.
- A Merkle database without independent witnesses/gossip is insufficient.

**Status:** Recommended baseline; witness operators, quorum, freshness SLO and incident policy remain conditional/open.

### D-05 — Device pairing, lifecycle and revocation

**Options**
- A. Server-authorized device linking.
- B. Existing trusted device explicitly approves a new device using an authenticated, transcript-bound pairing ceremony; Recovery Kit is a distinct fallback.
- C. Any device with FLY ID and password may join.

**Recommendation: B.**

Decision baseline:
- Every device has its own device key pair, device identifier, registration record and protocol sessions. Do not clone private device keys between devices.
- Pairing must bind a one-time challenge, source and target device identities/keys, account identity, protocol version and user-visible confirmation. The transcript must be authenticated and replay-resistant.
- New device is visibly pending until approval completes. A QR/manual verification step is acceptable as a user-facing channel, but the security proof must be cryptographic, not merely visual.
- Revocation invalidates future sessions and credentials as soon as the protocol and server controls permit; it cannot erase plaintext or files already received by a revoked device.
- Define revocation propagation, offline device behavior, concurrent pairing races, device cap, lost-device workflow and key rotation before implementation.
- Recovery Kit restoration is not the same as trusted-device transfer.

**Status:** Recommended baseline; protocol transcript, device cap, revocation timing and race semantics remain conditional.

### D-06 — Recovery Kit format, KDF, AEAD and identity lineage

**Options**
- A. Server stores encrypted backup and controls decryption.
- B. Client-generated Recovery Kit, client-side encryption, no server-side plaintext escrow; trusted-device transfer is a separate flow.
- C. Server-held recovery secret or support-assisted reset.

**Recommendation: B; A and C rejected.**

Decision baseline:
- Recovery Kit secret is generated and controlled by the user. Backend must not receive it or a plaintext private key.
- Recovery payload includes only the minimum encrypted identity/recovery state required by an explicitly defined recovery model. It must not silently restore revoked devices or claim to restore message history unless that is separately designed and tested.
- KDF and AEAD must come from maintained reviewed implementations. Argon2id is a candidate KDF; AES-GCM or XChaCha20-Poly1305 are candidates for AEAD, not approved parameters or final selections.
- KDF parameters must be selected after security review and device-class performance/memory benchmarking. Nonce uniqueness, associated data, versioning, salt, tamper detection, format migration and downgrade resistance must be specified.
- Recovery is an atomic, single-use or explicitly versioned operation with replay/race defenses, rate limits and recovery lineage. Recovery must trigger key rotation/rebinding and invalidate stale Verified state.
- UX must explain that loss of all devices and Recovery Kit may mean permanent loss of identity/access. Support cannot bypass this with server-side decryption.

**Status:** Conditional / implementation blocker. Exact container, KDF parameters, AEAD suite, lineage, single-use semantics and recovery abuse controls remain OPEN.

### D-07 — Message envelope, delivery, TTL and retention

**Options**
- A. Server-visible message bodies and server-side delivery transformation.
- B. Versioned ciphertext-only envelope with client-side encryption, opaque routing, idempotency and bounded metadata.
- C. Unstructured opaque blob with no protocol/version or lifecycle fields.

**Recommendation: B.**

Decision baseline:
- Envelope is versioned and bounded; it carries only the fields needed for routing, protocol selection, expiry and idempotent delivery. It must not contain plaintext, private keys, ratchet secrets or sensitive identity labels.
- Per-device protocol sessions are explicit. Sender encrypts separately for each authorized recipient device unless the selected reviewed protocol provides a vetted multi-device construction.
- Relay may store/forward ciphertext, reject malformed/oversized envelopes and enforce quotas; it cannot transform plaintext or infer protocol secrets.
- Define duplicate, replay, reorder, delayed, corrupt, expired, unknown-version and skipped-key exhaustion behavior.
- Delivery/read receipts are minimized and must not imply proof of human reading.
- TTL values and retention periods are not guessed here; privacy owner must approve a data-class schedule and deletion evidence. Server expiry and client deletion are separate controls.
- Push payload contains opaque wake-up only.

**Status:** Recommended baseline; canonical schema, TTL, receipts, retention schedule, queue limits and idempotency key lifecycle remain conditional.

### D-08 — Encrypted files, photos and object authorization

**Options**
- A. Server-side encryption with server-managed content keys.
- B. Client-side content encryption with per-recipient/device key wrapping and object-level authorization.
- C. Public/unlisted object URLs as access control.

**Recommendation: B; C rejected.**

Decision baseline:
- Client generates a unique random content key per object and encrypts before upload. Content keys are delivered only through authenticated E2EE sessions to authorized recipient devices.
- Object IDs are locators, never authorization. Every upload, download, preview, thumbnail, range request, share and delete operation checks identity/device/object authorization.
- Server stores ciphertext and minimum routing/expiry metadata only.
- Strip EXIF/GPS metadata where appropriate before encryption; validate MIME/type by content, impose size/time/resource limits and isolate parsers/previews.
- Define encrypted thumbnail behavior, temporary-file lifecycle, cache cleanup, download-to-gallery semantics, backup policy and deletion limitations.
- File transfer enters MVP only after object authorization, parser, metadata and temporary-file gates have passed.

**Status:** Conditional. Key wrapping, object ACL model, limits, metadata policy, parser isolation and deletion semantics remain OPEN.

### D-09 — Voice/video call media E2EE, signalling and ICE

**Options**
- A. WebRTC DTLS-SRTP only.
- B. WebRTC transport plus application-level E2EE media framing, authenticated signalling, independent key management and documented ICE/IP exposure.
- C. Custom media cryptography.

**Recommendation: B as future architecture; calls deferred. A is not E2EE; C rejected.**

Rationale:
- DTLS-SRTP protects transport hops but does not prove that an SFU cannot decrypt media.
- RFC 9605 SFrame is a relevant standards-track media-framing mechanism for E2EE media with SFUs, but it does not define application key provisioning. The application must guarantee sender/key separation and nonce/counter uniqueness and handle participant changes/rekeying.
- Before calls can be considered, define signalling authentication, call identity binding, per-sender media keys, join/leave rekey, replay handling, SFU trust assumptions, device revocation, key confirmation, ICE candidate/IP disclosure, TURN behavior, abuse controls and independent media E2EE review.
- No calls in MVP. No claim that SFrame has been integrated or validated.

**Status:** Deferred for MVP; detailed call ADR remains open.

Reference: https://www.rfc-editor.org/rfc/rfc9605

### D-10 — Metadata inventory, retention and contact discovery

**Options**
- A. Collect broad analytics/address books for convenience.
- B. Field-level inventory, minimization, explicit retention and manual FLY ID/QR discovery.
- C. Claim zero metadata.

**Recommendation: B; C rejected as an inaccurate claim.**

Decision baseline:
- Create a field-level inventory for identity, device, relay, push, files, telemetry, abuse controls, network and recovery.
- For every field define purpose, source, recipient/service, access roles, retention, deletion method, legal/abuse exception and user disclosure.
- No address-book upload or server-side contact matching in MVP. Manual FLY ID/QR discovery is the baseline.
- Push tokens are sensitive metadata, access-controlled and rotatable; push content has no message preview, sender name or FLY ID.
- IP, timing, packet size, delivery pattern and provider metadata remain residual risks. Product language must not claim absolute anonymity or “no metadata.”
- Logs, crash reports and analytics must be allowlisted and tested for secret/plaintext leakage.

**Status:** Recommended baseline; retention periods, operational access matrix, abuse/legal exceptions and final privacy notice remain conditional.

### D-11 — React Native bridge, native storage and platform boundary

**Options**
- A. All key handling and long-term secret storage in JavaScript.
- B. React Native + TypeScript for UI/orchestration, narrow Kotlin/Swift native adapters for platform key custody and sensitive operations.
- C. Rewrite all clients natively before validating the product.

**Recommendation: B.**

Decision baseline:
- JavaScript must not be the long-term custodian of private identity/device keys, ratchet secrets or Recovery Kit secrets.
- Native adapters expose narrow typed operations; no raw private-key export to JS. Bridge contracts define input validation, error handling, memory ownership, concurrency, cancellation and redacted diagnostics.
- Android: Keystore, BiometricPrompt, encrypted local database, backup exclusions and appropriate FLAG_SECURE use. These controls reduce risk but do not defeat a compromised OS or external camera.
- iOS: Keychain/Secure Enclave where supported and appropriate accessibility/backup controls; hardware-backed guarantees vary by device.
- Root/jailbreak detection is risk telemetry/warning, not a security guarantee.
- Android and iOS require independent platform threat models and test matrices.

**Status:** Recommended baseline; exact library APIs, minimum OS/device matrix, database implementation and bridge contract remain conditional.

### D-12 — Web companion and browser support

**Options**
- A. Full parity with long-lived secrets in browser storage.
- B. Limited companion mode paired from a trusted device, minimal persistence and separate browser security gates.
- C. Exclude Web until a later release.

**Recommendation: C for MVP; B may be evaluated after native MVP.**

Rationale:
- Browser XSS, extensions, compromised origin/CDN, service-worker takeover and storage extraction create a distinct threat model.
- A trusted-device pairing flow does not by itself make browser storage safe.
- If Web is later approved, require strict CSP, Trusted Types where supported, no third-party scripts on sensitive origin, controlled service-worker lifecycle, dependency review, session revocation, minimal persistence and independent browser testing.
- Do not claim Android Keystore/Secure Enclave parity.
- If Web gates fail, sensitive messaging must be disabled rather than silently downgraded.

**Status:** Deferred for MVP. Browser support matrix and companion key/session design remain open.

### D-13 — CI, runner trust, APK artifacts and signing custody

**Options**
- A. Manual builds and signing from developer machines.
- B. Unprivileged PR CI, unsigned test artifacts, protected release workflow and isolated production signing.
- C. Production signing credentials available to every branch/PR workflow.

**Recommendation: B; C rejected.**

Decision baseline:
- PR CI has least-privilege permissions, immutable full-SHA pins for third-party Actions, locked dependencies and no production signing secrets.
- PR/fork workflows produce unsigned debug/test artifacts only.
- Production signing occurs only from a protected release tag/environment with required approval; use managed signing/HSM or short-lived OIDC credentials where feasible.
- Produce SBOM, dependency review output, checksum and provenance bound to commit SHA, workflow run, toolchain and artifact.
- Protect workflow changes and release environments; document signing key rotation, revocation and compromise response.
- Never place keystore/private signing material in Git, logs, artifacts, caches or chat.
- Existing repository workflows are not evidence of a FLY-specific pipeline. Verify the actual workflow path, triggers, permissions, build tasks and artifacts before claiming readiness.

**Status:** Recommended baseline; exact runner, action pins, environment protection, signing service and provenance implementation remain conditional.

### D-14 — MVP platform and feature cut

**Options**
- A. Android + iOS + Web + Desktop simultaneously.
- B. Android-first secure core, then iOS after parity review; Web/Desktop later.
- C. Web-first.

**Recommendation: B.**

Approved scope proposal:
- MVP platform: Android.
- Next platform: iOS after Android security gates and native adapter parity review.
- Web and Desktop: post-MVP, independent security approval required.
- MVP capabilities: FLY ID, 1:1 E2EE text, manual FLY ID/QR contact add, transparency-backed key verification, device pairing/revocation, Recovery Kit, ciphertext relay, opaque push, local encrypted storage.
- Encrypted files/photos: conditional MVP increment only after D-08 gates close.
- Voice notes, groups, calls, Web and Desktop are excluded from the initial MVP baseline.
- Premium/payments are not part of this security MVP.
- Decentralization is future-only; TOR MODE excluded.

**Status:** Recommended product baseline; requires explicit product owner sign-off.

---

## 4. Final MVP definition

### 4.1 MVP-0: secure foundation (first implementation increment)

- Android application shell and security-first navigation.
- FLY ID creation and registration challenge.
- Identity key and device key generation/storage through native boundary.
- Public identity/device records and transparency proof verification.
- Manual contact add by FLY ID / QR and explicit fingerprint verification.
- 1:1 E2EE text session using the exact reviewed and approved library/version.
- Ciphertext-only relay with bounded envelope, idempotency, expiry and abuse controls.
- Opaque push wake-up.
- Local encrypted database and secret-free diagnostics.
- Device list, explicit pairing and revocation.
- Recovery Kit creation/restore and identity re-establishment UX.
- Security Center: key status, verified contacts, device status, recovery readiness and warnings.

### 4.2 MVP-1: encrypted attachments (conditional increment)

Allowed only after D-08 specification and FILE-01…03 test plans are approved:
- encrypted photos and files;
- per-object content key and recipient/device key delivery;
- object-level authorization on every endpoint;
- metadata stripping, parser/resource limits, expiry and temporary-file controls;
- explicit disclosure of recipient copies and external exports.

### 4.3 Not in MVP

- Group messaging and MLS integration.
- Voice/video calls, SFrame integration or SFU deployment.
- Voice messages unless separately approved after attachment/media gates.
- Web and Desktop.
- Address-book upload/contact matching.
- Cloud plaintext backup or server recovery escrow.
- Decentralized network, peer consensus, node discovery or Sybil defenses.
- TOR MODE.
- Payments, monetization and unrelated feature expansion.

### 4.4 Non-negotiable security properties

1. Backend never receives message plaintext, private identity/device keys, ratchet secrets, Recovery Kit secrets or media decryption keys.
2. FLY ID is public lookup data, never a credential or secret.
3. Identity, device, session and transport identifiers remain separate.
4. No custom cryptography.
5. No silent key substitution, downgrade, rollback or replay acceptance.
6. Key change invalidates previous Verified state.
7. Device pairing requires explicit cryptographic authorization.
8. Revocation does not promise deletion of already received data.
9. Push contains no content or identifying preview.
10. File object identifiers are never treated as authorization.
11. Web does not inherit native security claims.
12. DTLS-SRTP alone is never called E2EE.
13. Production signing secrets are isolated from PR CI and chat.
14. Disappearing/deletion features do not promise erasure of recipient-controlled copies.
15. TOR MODE absent.
16. Decentralization future-only.

---

## 5. Approval and ownership matrix

The following is a proposed ownership model. Names are intentionally not invented.

| Decision | Required approver | Approval state |
|---|---|---|
| D-01 crypto implementation/license | Security/crypto reviewer + legal reviewer + product owner | PENDING |
| D-02 group deferral | Product owner + security architect | PENDING |
| D-03 FLY ID/registration | Identity architect + security reviewer | PENDING |
| D-04 transparency | Security architect + independent monitor operator | PENDING |
| D-05 device lifecycle | Mobile/security leads | PENDING |
| D-06 recovery | Crypto reviewer + product owner | PENDING |
| D-07 message envelope/retention | Messaging/backend lead + privacy owner | PENDING |
| D-08 files | Security/backend lead + privacy owner | PENDING |
| D-09 call deferral | Product owner + media/security reviewer | PENDING |
| D-10 metadata/privacy | Privacy owner + security owner | PENDING |
| D-11 native boundary | Android/iOS leads + security reviewer | PENDING |
| D-12 Web deferral | Product owner + security architect | PENDING |
| D-13 CI/signing | Release engineering + security owner | PENDING |
| D-14 MVP cut | Product owner | PENDING |

**Formal approval record**
- Product owner: PENDING
- Security owner: PENDING
- Crypto reviewer: PENDING
- Privacy/legal reviewer: PENDING
- Release engineering owner: PENDING
- Approval date: PENDING
- Approved ADR revision/hash: PENDING

Until these fields are completed, this Phase 0.4 document is a recommended decision baseline, not a signed approval.

---

## 6. Implementation admission criteria

Implementation may start only when all applicable criteria below are satisfied.

### Gate A — Product and scope
- [ ] Product owner signs D-14 and the MVP exclusion list.
- [ ] Security owner signs threat model and trust boundaries.
- [ ] Android-first platform matrix and minimum OS/device support are documented.
- [ ] No TOR MODE or decentralization dependency appears in MVP artifacts.

### Gate B — Cryptography and licensing
- [ ] Exact 1:1 library, version, immutable source reference and license are selected.
- [ ] Legal review approves license and distribution obligations.
- [ ] Maintenance, security policy, vulnerability response and API compatibility are reviewed.
- [ ] Android integration and persistence spike succeeds on supported devices.
- [ ] Crypto reviewer approves protocol usage, state persistence and integration threat model.
- [ ] No custom protocol or primitive is introduced.

### Gate C — Identity and transparency
- [ ] FLY ID canonical format and entropy source are specified.
- [ ] Registration challenge transcript, expiry, one-time consumption and replay controls are specified.
- [ ] Identity/device/session/transport schemas are versioned and separate.
- [ ] Transparency proof, monitor/witness quorum, freshness and outage behavior are specified.
- [ ] Key change and Verified invalidation behavior is specified.

### Gate D — Device and recovery
- [ ] Device pairing transcript and user confirmation are specified.
- [ ] Device revocation propagation and offline-device semantics are specified.
- [ ] Recovery Kit format, KDF/AEAD and parameters are independently reviewed.
- [ ] Recovery replay/race, rate limits and lineage are specified.
- [ ] Loss-of-all-devices warning and irreversible-loss UX are approved.

### Gate E — Messaging and backend
- [ ] Message envelope schema, version negotiation, idempotency, replay/reorder and TTL are specified.
- [ ] Backend API schema has no plaintext or secret fields.
- [ ] Relay queue quotas, abuse controls, retention and deletion are specified.
- [ ] Push payload is opaque and validated by tests.
- [ ] Logging/telemetry allowlist is approved.

### Gate F — Local storage and files
- [ ] Android key custody, encrypted DB, backup exclusions and temporary-file handling are specified.
- [ ] FILE-01…03 threat-driven test cases and object authorization model are ready before attachments.
- [ ] MIME/parser/resource limits, metadata stripping and deletion semantics are specified.

### Gate G — CI and release engineering
- [ ] FLY-specific workflow design is reviewed.
- [ ] PR CI has no production signing secrets.
- [ ] Third-party Actions are pinned to immutable commit SHAs.
- [ ] Dependency lockfiles, SBOM and vulnerability policy are defined.
- [ ] Protected release environment and signing custody are approved.
- [ ] Artifact checksum/provenance and signing-key incident response are specified.

### Gate H — Test readiness
- [ ] Every mandatory test has a test owner, environment, preconditions and expected result.
- [ ] Test data are synthetic and secrets are never included in evidence.
- [ ] Test execution records commit/build hash and reproducible evidence.
- [ ] The 27 existing Red-Team scenarios retain their actual status; no status is changed without execution.
- [ ] Critical/High design questions are closed before implementation of affected components.

**Admission rule:** no implementation of a component while its relevant blocking gate remains unresolved. A scoped prototype may be separately authorized for a technical spike, but it must be isolated, non-production and explicitly labeled non-audited.

---

## 7. Delivery sequence after approval

| Step | Work package | Required exit |
|---|---|---|
| 1 | Crypto dependency/legal/platform spike | Exact library/version/license approved |
| 2 | FLY ID and device schema | Registration, key binding and replay model approved |
| 3 | Transparency specification | Witness/freshness/split-view behavior approved |
| 4 | Android native security boundary | Key custody, DB, backup and bridge tests pass |
| 5 | 1:1 messaging | Protocol integration and state/replay tests pass |
| 6 | Device lifecycle | Pairing/revocation/race tests pass |
| 7 | Recovery | Recovery theft/replay/race tests pass |
| 8 | Relay/privacy | Metadata, queue, abuse and retention tests pass |
| 9 | Attachments (conditional) | File authorization/parser/metadata tests pass |
| 10 | iOS platform increment | Native parity review and platform gates pass |
| 11 | Release engineering | Protected CI, SBOM, provenance and signing checks pass |
| 12 | Release candidate | Applicable release gates and independent review complete |

Groups, calls, Web and Desktop require separate scope/architecture approvals; they are not implied by completion of this sequence.

---

## 8. Risk register carried forward

| Risk | Decision treatment | Residual condition |
|---|---|---|
| libsignal license/API/support constraints | D-01 conditional | Legal review and integration spike |
| Crypto library compromise or defect | D-01 blocker | Exact version review and independent assessment |
| Transparency split-view | D-04 blocker | Independent witnesses, gossip and freshness |
| Device cloning/pairing race | D-05 blocker | Transcript binding, atomic replay control, revocation tests |
| Recovery theft/replay | D-06 blocker | KDF/AEAD review, rate limits, lineage and race tests |
| Relay replay/reorder/metadata | D-07 blocker | Envelope spec, bounded queues and fault matrix |
| File IDOR/parser/EXIF leakage | D-08 conditional | Attachment gate and FILE test execution |
| Call/SFU media exposure | D-09 deferred | Separate media E2EE architecture and review |
| Contact graph leakage | D-10 reduced | No address-book upload; metadata remains |
| JS runtime exposure | D-11 reduced, not eliminated | Narrow bridge; endpoint compromise remains |
| Browser XSS/extensions | D-12 deferred | Separate Web gates |
| CI/signing compromise | D-13 blocker | Protected workflow, isolated signing, provenance |
| Decentralization/Sybil | Future-only | Not in current scope |

---

## 9. Phase 0.4 verification record

- Phase 0.3 blueprint used as base: yes.
- Fourteen decision records prepared: yes.
- Options and recommendations recorded: yes.
- Recommended / conditional / open / deferred distinctions recorded: yes.
- MVP scope and exclusions documented: yes.
- Implementation admission criteria documented: yes.
- Product-owner approval: pending.
- Security-owner approval: pending.
- Legal/license approval: pending.
- Application code written: no.
- Tests executed: no.
- Existing Red-Team scenarios: 27 × `NOT_RUN`.
- APK built: no.
- FLY-specific CI workflow implemented or run: no.
- Pull request created: no.
- `main` modified: no.
- TOR MODE: excluded.
- Decentralization: future-only.

**Phase 0.4 documentation status:** decision baseline prepared.  
**Implementation readiness:** CONDITIONAL NO-GO pending the explicit approvals and Gate A–H evidence above.

---

## 10. Source references reviewed

- Signal libsignal repository and README: https://github.com/signalapp/libsignal
- MLS protocol, RFC 9420: https://www.rfc-editor.org/rfc/rfc9420
- MLS architecture, RFC 9750: https://www.rfc-editor.org/rfc/rfc9750
- OpenMLS implementation and platform notes: https://github.com/openmls/openmls
- SFrame, RFC 9605: https://www.rfc-editor.org/rfc/rfc9605

These references support standards/library-surface observations only. They do not constitute a security audit of any dependency or FLY SECRET integration.
