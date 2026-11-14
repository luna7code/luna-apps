# FLY SECRET — Architecture Decision Records (Phase 0.2)

- Version: 0.2
- Base: `security/fly-secret-architecture-review-0.1`
- Branch: `security/fly-secret-architecture-adrs-0.2`
- Status: Proposed; decisions require explicit architecture approval
- Application implementation: not started
- Red-Team tests: 27 × `NOT_RUN`
- TOR MODE: excluded from requirements, architecture, UI, configuration and tests
- Decentralization: future-only; excluded from MVP and current release scope

> These ADRs are design decisions and recommendations, not evidence of a working or audited product. No library/version is considered finally selected until the due-diligence and approval criteria in ADR-001 are met.

## ADR index

| ADR | Topic | Recommendation | Status |
|---|---|---|---|
| 001 | Cryptographic stack | Reviewed protocol implementation; no custom cryptography | Proposed |
| 002 | FLY ID and identity lifecycle | Random opaque ID, distinct from identity/device/session keys | Proposed |
| 003 | Key transparency | Append-only Merkle log, independent monitors and gossip | Proposed |
| 004 | Multi-device | Per-device keys, explicit linking and revocation | Proposed |
| 005 | Recovery Kit | Client-side encrypted recovery; no server-side plaintext escrow | Proposed |
| 006 | 1:1 and group messaging | Signal-family for 1:1; MLS candidate for groups after review | Proposed |
| 007 | E2EE calls | Application-level media E2EE; WebRTC transport alone is insufficient | Proposed |
| 008 | Metadata privacy | Minimize collection/retention; opaque push; disclose residual metadata | Proposed |
| 009 | Mobile architecture | React Native + TypeScript with narrow Kotlin/Swift security adapters | Proposed |
| 010 | Web security | Separate web threat model; companion-client model initially | Proposed |
| 011 | Android CI/release | Least-privilege GitHub Actions; isolated protected signing | Proposed |

---

# ADR-001 — Cryptographic Stack

- **Status:** Proposed
- **Priority:** Critical
- **Threat references:** ID-01…03, CRYPTO-01…03, REC-01…03, CALL-01…02, SUPPLY-01

### Context

FLY SECRET needs E2EE, forward secrecy, post-compromise recovery, multi-device support and a credible post-quantum migration path. There is no application implementation or selected crypto library yet. A flawed protocol composition or unsafe persistence can compromise every conversation.

### Options

**A. Build a proprietary protocol and primitives.** Advantages: full API and roadmap control. Disadvantages: extreme design, implementation and audit burden. Threats: ratchet rollback, nonce/key reuse, downgrade, side-channel and state-machine errors. **Reject.**

**B. Compose a protocol from unrelated primitive libraries.** Advantages: flexibility and component choice. Disadvantages: project owns correctness of the composition; incompatible APIs and review quality. Threats: domain-separation mistakes, key confusion, downgrade and serialization errors. **Reject as the default.**

**C. Adopt a mature, independently reviewed protocol implementation.** Advantages: reviewed state machines, known protocol semantics and reduced custom crypto surface. Disadvantages: dependency on library maintenance, licensing, API and platform support. Threats: supply-chain compromise, stale versions, integration misuse and vulnerable persistence. **Recommend.**

**D. Use distinct reviewed stacks for distinct domains.** Advantages: specialized protocols for 1:1, groups, calls and storage. Disadvantages: more trust boundaries and integration points. Threats: cross-protocol confusion and inconsistent identity semantics. **Allow only with explicit interfaces and separate reviews.**

### Selection criteria

- Public specification and independent review of the exact implementation/version.
- Active maintenance, security response process, licensing and provenance.
- Android/iOS/Web/Desktop support and multi-device semantics.
- Correct replay, out-of-order, skipped-key, rollback and crash-consistency behavior.
- Interoperability vectors, upgrade/migration policy and post-quantum roadmap.
- No custom ratchet, KEM, KDF, handshake or media cipher.

### Recommendation

Choose **C** as the mandatory strategy. Evaluate the current Signal Protocol family and supported implementations for 1:1, including PQXDH/Double Ratchet and subsequent official protocol updates where applicable. These are evaluation references, not a final library/version selection. Evaluate MLS separately for groups under ADR-006. Calls require a separate reviewed media design under ADR-007.

Before acceptance, produce a candidate matrix with exact versions, audit scope, license, platform support, maintenance history, API gaps and migration plan. Independent review is required. A language choice (including Rust) is not a substitute for selecting a vetted protocol implementation.

### Consequences / acceptance

- Crypto is isolated behind a narrow interface; protocol state persistence is atomic and protected.
- Exact versions are pinned and recorded in the SBOM.
- Protocol/library upgrades require migration, interoperability and downgrade testing.
- **Unresolved:** shortlist, exact library/version, license approval, PQ policy and reviewer sign-off.
- No crypto test is PASS until actually executed.

---

# ADR-002 — FLY ID and Identity Lifecycle

- **Status:** Proposed
- **Priority:** Critical
- **Threat references:** ID-01…04, PRIV-02, REC-03

### Context

Users should not need a phone number or email. A public identifier must not become an authentication secret or be conflated with cryptographic keys.

### Options

**A. Sequential database ID or guessable username.** Advantages: easy lookup and support. Disadvantages: enumeration and scraping. Threats: PRIV-02 and account discovery. **Reject.**

**B. Random opaque FLY ID.** Advantages: unpredictable and independent of keys; allows technical identifiers to rotate. Disadvantages: less memorable; does not hide an ID from someone who already knows it. Threats: directory leakage and correlation if reused as a transport ID. **Recommend.**

**C. FLY ID equals public key/fingerprint.** Advantages: direct cryptographic binding. Disadvantages: couples user addressing to key rotation and algorithm changes. Threats: confusing key-change semantics. **Reject.**

### Selection criteria

Unpredictability, no embedded permissions/secrets, independent identity/device/session/transport identifiers, key rotation, recovery, revocation and anti-enumeration controls.

### Recommendation

Use a CSPRNG-generated opaque FLY ID with a target minimum of 128 bits of entropy; finalize encoding and format in a technical spec. FLY ID is an address, not a bearer credential, password or key.

Maintain separate entities: identity keypair, per-device keypair, session/ratchet state and rotatable transport/mailbox identifiers. Registration and device linking require proof-of-possession, a single-use expiring challenge and transcript/action binding. Do not require phone/email. Recovery that changes identity keys must be visible and must not silently inherit Verified status.

### Consequences / acceptance

- Directory lookup needs rate limits, uniform responses and privacy review.
- QR profiles contain only necessary bootstrap data; pairing credentials are short-lived.
- **Unresolved:** ID format, registration proof format, key rotation and re-establishment UX.
- Tests for replay, enumeration and key change remain NOT_RUN.

---

# ADR-003 — Key Transparency

- **Status:** Proposed
- **Priority:** Critical
- **Threat references:** ID-01, ID-04, PRIV-04

### Context

TLS and signed directory responses do not stop a malicious directory from presenting different valid-looking keys to different users (split-view/equivocation).

### Options

**A. Trust the server directory.** Advantages: simple and cheap. Disadvantages: single trust authority. Threats: targeted key substitution and split-view. **Reject.**

**B. Manual QR/safety-number comparison only.** Advantages: end-to-end out-of-band verification. Disadvantages: user-dependent, not scalable and no global consistency guarantee. Threats: social engineering and ignored warnings. **Keep as an additional control.**

**C. Append-only Merkle transparency log with signed tree heads, inclusion/consistency proofs, independent monitors and gossip/witness comparison.** Advantages: makes key history auditable and split-view detectable. Disadvantages: operational complexity and monitor/witness governance. Threats: equivocation, stale heads, monitor collusion and rollback. **Recommend.**

**D. Decentralized consensus directory.** Advantages: potential reduction of single-operator dependence. Disadvantages: Sybil, Eclipse and governance complexity. **Future-only; not current scope.**

### Selection criteria

Append-only verifiability, freshness, consistency proofs, independent witnesses, rollback detection, identity-key binding and safe outage behavior.

### Recommendation

Choose **C**. Specify signed tree heads, inclusion and consistency proofs, independent monitors and gossip/witness comparison. A Merkle log alone does not prevent split-view unless independent parties compare views. Client retains the last verified tree head and rejects silent rollback.

Verified is bound to a specific fingerprint/key version, not a permanent contact flag. Any key change or failed consistency check clears Verified across UI surfaces and triggers a prominent warning plus QR/safety-number re-verification. If transparency is unavailable, do not mark a new or changed key as Verified.

### Consequences / acceptance

- **Unresolved:** log format, trust roots, monitor diversity/quorum, freshness and outage policy.
- Required tests: split-view, equivocation, stale tree head and rollback. All remain NOT_RUN.

---

# ADR-004 — Multi-device Lifecycle

- **Status:** Proposed
- **Priority:** Critical
- **Threat references:** ID-03, ID-04, CRYPTO-01, REC-03

### Context

A FLY identity can have multiple devices. Sharing one private key across devices prevents reliable per-device revocation and increases compromise impact.

### Options

**A. One private key shared by all devices.** Advantages: simple synchronization. Disadvantages: one compromise affects all devices; no granular revoke. Threats: cloning and lateral movement. **Reject.**

**B. Independent device keypairs and per-device sessions.** Advantages: isolation, explicit trust and granular revocation. Disadvantages: more complex fan-out, session state and device UX. Threats: unauthorized linking, stale sessions and partial revoke. **Recommend.**

**C. Server stores shared plaintext key/session state.** Advantages: easy recovery. Disadvantages: breaks E2EE and makes server a trust authority. Threats: server breach and insider access. **Reject.**

### Selection criteria

Per-device identity, explicit approval, proof-of-possession, session lifecycle, revocation/rotation, replay/race safety and cross-device notifications.

### Recommendation

Choose **B**. Device states: `PENDING → ACTIVE → REVOKED`. New device initiates a short-lived pairing flow; an existing trusted device approves it after transcript/fingerprint verification. If no trusted device exists, use the separate Recovery/Identity Re-establishment flow. Do not copy raw private device keys between devices.

Each device has its own keys and sessions. Revocation blocks new operations, invalidates associated server credentials and triggers protocol-defined session closure/rotation. Server stores public device records and opaque routing state only, never plaintext or session secrets.

### Consequences / acceptance

- UI shows devices, trust state, last activity and revoke action.
- Revoke cannot erase plaintext already received by another participant.
- **Unresolved:** pairing protocol, device record schema, revoke propagation and fan-out semantics.
- Multi-device tests remain NOT_RUN.

---

# ADR-005 — Recovery Kit, Device Transfer and Identity Re-establishment

- **Status:** Proposed
- **Priority:** Critical
- **Threat references:** REC-01…03, ID-03, ID-04

### Context

An anonymous identity has no external phone/email recovery authority. Device transfer, recovery backup and creation of a new identity after total key loss are different operations.

### Options

**A. Server-side plaintext escrow/account reset.** Advantages: convenient. Disadvantages: server can restore identity or decrypt data, contradicting E2EE. Threats: server breach, insider abuse and coercion. **Reject.**

**B. Device-to-device transfer approved by an existing trusted device.** Advantages: preserves trust continuity. Disadvantages: old device must be available; pairing channel can be attacked. Threats: rogue pairing and replay. **Recommend when an active trusted device exists.**

**C. User-controlled client-encrypted Recovery Kit.** Advantages: recovery without server escrow. Disadvantages: lost secret can mean irreversible loss; phishing and weak passphrases are risks. Threats: theft, replay and backup substitution. **Recommend as an independent path.**

**D. Support-mediated identity reset.** Advantages: familiar support flow. Disadvantages: support becomes identity authority. Threats: social engineering and account takeover. **Reject.**

### Selection criteria

No server plaintext keys/secrets, high-entropy recovery material, vetted KDF/AEAD, atomic one-time recovery, explicit old-device revocation and honest loss warnings.

### Recommendation

Combine **B + C**, with **D** only as a clearly labelled identity re-establishment process:

1. **Device Transfer:** short-lived, one-time pairing approved by an existing trusted device and bound to a verified transcript.
2. **Recovery Kit:** generated and encrypted on the client. Server may store ciphertext and minimal opaque metadata only. Secret never enters logs, analytics, support or chat.
3. **Identity Re-establishment:** if original keys are lost, create a new identity/key lineage or an explicitly reviewed recovery lineage. Contacts see a trust change; Verified is not silently inherited.

Choose container format, KDF and AEAD only from vetted implementations after ADR-001. Human passphrases require a reviewed memory-hard KDF and parameters. Do not invent a recovery cipher.

### Consequences / acceptance

- Recovery Kit is not a magic account reset. If all required recovery material is lost, access may be permanently lost.
- Message history is recoverable only if its required keys/backup survive.
- **Unresolved:** container format, key derivation, recovery transaction semantics and contact notifications.
- REC-01…03 remain NOT_RUN.

---

# ADR-006 — 1:1 and Group Messaging

- **Status:** Proposed
- **Priority:** Critical
- **Threat references:** CRYPTO-01…03, RELAY-01…02, ID-03, LOG-01

### Context

1:1 ratcheting and group membership/epoch changes have different security semantics. One protocol should not be assumed to fit both without evidence.

### Options

**A. Custom unified protocol.** Advantages: one API. Disadvantages: project owns complex crypto state machines. Threats: key reuse, membership desync, rollback and downgrade. **Reject.**

**B. Signal-family implementation for 1:1 and MLS for groups after review.** Advantages: purpose-built reviewed approaches. Disadvantages: two stacks and integration boundaries. Threats: cross-protocol confusion, stale group epochs and device membership mismatch. **Recommend.**

**C. MLS for every conversation.** Advantages: one group-oriented model. Disadvantages: not automatically a drop-in replacement for every 1:1/session semantic. Threats: wrong protocol fit and recovery mismatch. **Do not select without evidence.**

**D. Ship 1:1 only; defer groups.** Advantages: smaller MVP attack surface. Disadvantages: no groups in first release. Threats: users may rely on insecure external workarounds if scope is unclear. **Acceptable fallback.**

### Selection criteria

Forward secrecy, post-compromise recovery, authenticated membership changes, multi-device support, replay/reorder handling, mature implementations, license/platform support and interoperability.

### Recommendation

For 1:1, use the reviewed Signal-family implementation selected in ADR-001. For groups, evaluate MLS (RFC 9420 and architecture RFC 9750) and adopt only if a mature maintained implementation, license, platform coverage and integration with device identity are verified. Membership changes must cause protocol-correct epoch/key updates. If MLS does not meet criteria for MVP, ship 1:1 only and defer groups behind a feature boundary. Do not invent group encryption.

### Consequences / acceptance

- Backend is a ciphertext relay/store-and-forward service, not a decryption participant.
- **Unresolved:** exact 1:1 library/version, MLS candidate, group MVP scope and epoch lifecycle.
- Group/membership tests remain NOT_RUN.

---

# ADR-007 — E2EE Voice and Video Calls

- **Status:** Proposed
- **Priority:** Critical
- **Threat references:** CALL-01…03, PRIV-01, ID-04

### Context

WebRTC DTLS-SRTP protects transport hops. With an SFU, transport encryption alone does not prove that media is end-to-end encrypted between participants.

### Options

**A. DTLS-SRTP only.** Advantages: standard transport security and broad support. Disadvantages: intermediary may access media depending on topology. Threats: SFU plaintext access. **Reject as an E2EE claim.**

**B. Peer-to-peer WebRTC with authenticated application-level E2EE.** Advantages: fewer media intermediaries. Disadvantages: NAT/connectivity limitations and potential IP disclosure. Threats: signalling substitution and ICE leakage. **Suitable for reviewed 1:1 mode.**

**C. WebRTC + SFU + application-level frame E2EE.** Advantages: scalable routing while SFU handles ciphertext. Disadvantages: key distribution, frame processing, rotation and platform integration complexity. Threats: media-key leakage, incorrect integration and metadata exposure. **Recommend as target architecture.**

**D. Proprietary media cipher.** Advantages: full control. Disadvantages: extreme risk and no mature review. **Reject.**

### Selection criteria

Endpoint-only media keys, authenticated signalling, participant/device binding, key rotation, multi-platform support, ICE/IP disclosure policy and independent cryptographic review.

### Recommendation

Use **C** as the target for scalable/group calls; consider **B** for 1:1 after IP/privacy trade-off review. WebRTC is transport, not the E2EE proof. Evaluate SFrame (RFC 9605) for encoded media frame encryption in SFU deployments. SFrame does not by itself solve participant authentication or key management; bind media keys to verified identity/device sessions and a reviewed key-management protocol. Never write a custom media cipher.

Do not claim SFU cannot decrypt media until a cryptographic review and black-box validation prove that the SFU never receives decryption keys.

### Consequences / acceptance

- **Unresolved:** concrete SFrame implementation/version, key management, signalling transcript, group membership rotation and ICE policy.
- Required tests: SFU key isolation, signalling substitution/replay, key rotation and STUN/TURN/IP disclosure. All NOT_RUN.

---

# ADR-008 — Metadata Privacy

- **Status:** Proposed
- **Priority:** High; Critical if plaintext/keys leak
- **Threat references:** PRIV-01…04, CALL-03, LOG-01, RELAY-02

### Context

E2EE protects content, not automatically sender/recipient, timing, message sizes, IP addresses, push tokens, group membership or device graphs.

### Options

**A. Full telemetry and detailed event history.** Advantages: easier analytics and abuse investigations. Disadvantages: creates social graph and breach impact. Threats: correlation, insider abuse and data breach. **Reject as default.**

**B. Minimize collection and retention; use opaque push.** Advantages: smaller data footprint and blast radius. Disadvantages: reduced diagnostic visibility. Threats: residual traffic analysis. **Recommend.**

**C. Promise zero metadata.** Advantages: strong-sounding privacy claim. Disadvantages: incompatible with practical relay, delivery and operations; misleading. Threats: false assurance. **Reject.**

### Selection criteria

Data inventory, purpose/access/retention for each field, opaque push, identifier separation, contact-discovery privacy, secret-free logs and measurable residual correlation risk.

### Recommendation

Choose **B**. Collect only what is necessary for delivery, abuse prevention and operations; document retention and enforce deletion. Push payload contains only an opaque wake-up signal—no message text, FLY ID, sender identity or preview. Keep push token mapping separate from identity and restrict access/retention. Logs, metrics and crash reports use allowlists; redaction and secret scanning are defense-in-depth, not permission to log secrets.

Do not use stable hashes of address books as contact discovery. Evaluate PSI or alternatives only after a separate cryptographic/privacy review. OHTTP (RFC 9458) may be appropriate for selected stateless requests with a relay/gateway design; it is not universal anonymity, does not hide every metadata field and is not TOR. TOR MODE remains excluded.

### Consequences / acceptance

- Do not promise absolute anonymity. Network operators, endpoints and some infrastructure may observe residual metadata.
- **Unresolved:** field-level data inventory, retention periods, push schema, contact discovery and analytics policy.
- Correlation and logging tests remain NOT_RUN.

---

# ADR-009 — React Native and Native Security Modules

- **Status:** Proposed
- **Priority:** High
- **Threat references:** CRYPTO-01…03, REC-01, LOG-01, SUPPLY-01

### Context

The mobile app targets Android and iOS. React Native can share UX/business orchestration, but private keys should not live in ordinary JS storage or be exposed through broad bridge APIs.

### Options

**A. Entirely JavaScript crypto and storage.** Advantages: one codebase. Disadvantages: key material exposed to JS runtime/heap and weaker access to platform secure hardware. Threats: instrumentation, storage leakage and bridge misuse. **Reject for key custody.**

**B. React Native + narrow native security adapters.** Advantages: shared UI with Android Keystore/iOS Keychain/Secure Enclave where available. Disadvantages: Kotlin/Swift modules and platform-specific testing. Threats: bridge contract bugs and dependency drift. **Recommend.**

**C. Fully native apps.** Advantages: maximum platform control. Disadvantages: duplicated UI/business logic and higher maintenance. Threats: divergent security behavior. **Fallback if RN cannot meet gates.**

**D. Shared Rust crypto core through FFI.** Advantages: common core and fewer platform divergences. Disadvantages: ABI, memory ownership and build complexity. Threats: FFI misuse and panic handling. **Optional only if vetted library supports it.**

### Selection criteria

Private keys inaccessible to ordinary JS storage, platform secure storage, narrow versioned interfaces, reviewed crypto library, backup/local DB policy and separate platform test matrices.

### Recommendation

Choose **B**. React Native + TypeScript handles presentation/orchestration. Kotlin/Swift adapters expose only minimal secure storage, biometric gating and crypto operations. Consider Rust only as a wrapper/integration option for an already vetted implementation; language choice is not a security guarantee.

Android: Android Keystore, BiometricPrompt, encrypted database, backup exclusions and FLAG_SECURE on appropriate screens. iOS: Keychain/Secure Enclave where supported and appropriate accessibility/backup controls. These reduce risk but do not protect against every compromised OS or external camera.

### Consequences / acceptance

- Native security surface stays small and independently tested.
- Hardware-backed guarantees differ by device; never claim parity across all devices.
- **Unresolved:** exact RN version, native API contracts, storage implementation and FFI decision.
- Platform tests remain NOT_RUN.

---

# ADR-010 — Web Security Boundary

- **Status:** Proposed
- **Priority:** High; Critical for key/plaintext compromise
- **Threat references:** CRYPTO-01…03, REC-01, LOG-01, SUPPLY-01

### Context

Browser runtime is exposed to XSS, malicious extensions, compromised dependencies/origin/service workers and browser storage access. Native mobile security assumptions do not transfer to Web.

### Options

**A. Full mobile parity with long-lived keys in browser storage.** Advantages: convenient offline/multi-device UX. Disadvantages: XSS/runtime compromise can steal accessible keys. Threats: persistent key theft and malicious extensions. **Reject as default.**

**B. Limited companion client with minimal persistence and trusted-device pairing.** Advantages: smaller long-lived key exposure and easier revocation. Disadvantages: reduced offline/recovery functionality. Threats: session hijack, XSS and compromised browser runtime. **Recommend for first Web release.**

**C. Exclude Web until it passes security gates.** Advantages: smaller attack surface. Disadvantages: no Web access. **Acceptable fallback.**

### Selection criteria

Explicit browser trust model, strict CSP, Trusted Types where supported, no third-party scripts on sensitive origins, dependency review, minimal key persistence, session revocation and independent Web tests.

### Recommendation

Choose **B**. Initial Web client is a companion activated through explicit trusted-device pairing. Do not claim equivalence to Android Keystore or iOS Secure Enclave. Minimize long-lived private key material in browser JS and persistent storage; any unavoidable browser cryptography requires a separately reviewed design.

Baseline: strict CSP without unsafe-inline/eval, Trusted Types where available, no third-party scripts on sensitive origins, dependency pinning/review, secure session/CSRF controls, controlled service-worker updates, no secrets in localStorage, explicit logout/revoke and cache cleanup. If browser controls fail release gates, disable sensitive Web messaging/calls rather than silently weakening protection.

### Consequences / acceptance

- Web feature parity is not mandatory for MVP.
- **Unresolved:** browser key model, CSP policy, service-worker lifecycle, session storage and supported browser matrix.
- Web tests remain NOT_RUN and cannot inherit mobile PASS.

---

# ADR-011 — GitHub Actions Android APK Pipeline

- **Status:** Proposed
- **Priority:** Critical
- **Threat references:** SUPPLY-01, LOG-01

### Context

APK build/release is part of the security boundary. Workflow, dependency, runner or signing compromise can distribute a malicious client. Keystore/passwords must never be committed or requested in chat.

### Options

**A. Manual local build and APK transfer.** Advantages: simple prototype workflow. Disadvantages: weak provenance and reproducibility. Threats: artifact substitution. **Not suitable for release.**

**B. GitHub Actions CI for build/test; signing in a protected release environment.** Advantages: traceability and separation of CI from signing trust. Disadvantages: more setup and approval controls. Threats: workflow/dependency compromise remain possible but can be constrained. **Recommend.**

**C. Sign production APKs in every branch/PR workflow.** Advantages: convenient installable artifacts. Disadvantages: broad signing-secret exposure. Threats: malicious PR and signing-key exfiltration. **Reject.**

### Selection criteria

Least-privilege token permissions, immutable action pins, locked dependencies, no production secrets in PR workflows, protected environment approval, short-lived/OIDC or managed signing where possible, SBOM, provenance, checksum and artifact-to-commit binding.

### Recommendation

Choose **B**.

1. PR CI runs static checks, unit tests, lint and unsigned debug/test APK. It has minimum permissions and no production signing secrets, including for fork PRs.
2. Production signing runs only for a protected release tag/approved release in a protected environment with required approval. Prefer managed signing/HSM or short-lived credentials via OIDC when available.
3. Pin third-party Actions to immutable full commit SHAs; update pins through reviewed changes.
4. Lock dependencies, run dependency review, produce and archive SBOM and vulnerability results.
5. Attach SHA-256 checksum, commit SHA, workflow run, toolchain versions and provenance/attestation to each release artifact. Use SLSA-aligned provenance where feasible.
6. Keep keystore/signing credentials outside Git and chat. Do not expose secrets through logs, artifacts, caches or debug symbols.

The repository's existing Android CI workflow was previously identified as targeting another app path. Before FLY builds, independently verify workflow triggers, project path, Gradle tasks, permissions and artifact naming. This ADR does not claim that a FLY-specific workflow exists or has run.

### Consequences / acceptance

- Debug APK can be built without production signing credentials.
- Production signing is a separate controlled release event.
- Signing-key rotation/revocation and incident response must be documented.
- **Unresolved:** FLY workflow file, action pins, runner/toolchain, protected environment, signing custody and attestation implementation.
- No APK build or CI execution is claimed by this ADR.

---

## Cross-ADR dependencies and phase exit criteria

- ADR-001 is foundational to ADR-004, ADR-005, ADR-006 and ADR-007.
- ADR-002 and ADR-003 jointly define identity and verifiable trust.
- ADR-004 affects messaging fan-out, group membership, calls and recovery.
- ADR-008 constrains identity discovery, push, relay, calls and telemetry.
- ADR-009 and ADR-010 define platform-specific trust boundaries.
- ADR-011 protects the supply chain for every client artifact.

Phase 0.2 documentation is prepared when all eleven ADRs are recorded and cross-references are consistent. Implementation must not start until the blocking decisions have been explicitly accepted, especially the concrete crypto implementation, transparency operation, recovery format, group scope and call media E2EE design.

## Current status

- ADR count: 11
- Status: Proposed; no ADR marked Accepted
- Application code: not written
- Tests: 27 × `NOT_RUN`
- APK build: not run
- GitHub Actions FLY pipeline: not implemented by this phase
- `main`: must remain unchanged
- TOR MODE: excluded
- Decentralization: future-only
