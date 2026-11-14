# FLY SECRET — Phase 1.1
## Security Architecture Preservation & Non-Crypto MVP Planning

**Status:** PLANNING DOCUMENT COMPLETE — NO IMPLEMENTATION AUTHORIZED  
**Baseline:** Phase 1.0 — Crypto Architecture Decision Gate  
**Base branch:** `security/fly-secret-crypto-architecture-decision-gate-1.0`  
**Working branch:** `security/fly-secret-non-crypto-mvp-planning-1.1`  
**Repository:** `luna7code/luna-apps`  
**Review date:** 2026-11-14

> This phase identifies product and platform components that can be designed before D-01 is resolved. It does not authorize code, a prototype, a technical spike, a backend deployment, user-data processing, cryptographic integration or a production release.

---

## 1. Executive disposition

Phase 1.0 selected deferral of production E2EE as the current execution posture. D-01 remains OPEN. Phase 1.1 preserves that decision and defines a bounded, non-cryptographic MVP planning surface.

### 1.1 Approved planning posture

The project may prepare specifications for:
- application shell, navigation, accessibility and localization;
- onboarding and local profile presentation;
- FLY ID as a non-secret public identifier, with no authentication or cryptographic meaning implied;
- device inventory UX using explicitly non-authenticating installation records;
- contacts UX and local address-book-like organization, without claiming verified identities or implementing a privacy-invasive discovery service;
- settings, privacy explanations, session/device management UX shells;
- notification preferences and opaque wake-up contract design, without message content in push payloads;
- local data lifecycle, deletion UX, export/import format requirements and backup policy;
- service/API contracts at the level of schemas and threat-model requirements, without production endpoints or security claims;
- security center UX with clearly marked unavailable/not-yet-implemented states;
- design system, telemetry policy, error taxonomy and release process documentation.

### 1.2 Not approved

No code, build, technical spike, backend integration, crypto engine, message encryption, secure key storage implementation, calls, file encryption/transfer, contact discovery service, production push pipeline, account recovery implementation or release is authorized by this phase.

**The non-crypto MVP must not be marketed or represented as a secure messenger or E2EE product.** If any interactive messaging mock is later separately authorized, it must use synthetic data and be visibly labeled as a demo/non-secure environment. It must not accept real sensitive content.

---

## 2. Identity and identifier separation

The following separation is mandatory. Similar names or shared database columns must not collapse these concepts.

### 2.1 Identifier taxonomy

| Concept | Meaning | May be public? | Secret/key? | Phase 1.1 status |
|---|---|---:|---:|---|
| FLY ID | Random opaque user-facing identifier | Yes, subject to privacy policy | No | Design allowed |
| Device record ID | Opaque identifier for an installation record | No public exposure by default | No | Design allowed |
| Device identity key | Future cryptographic identity key bound to a device | Public key may be published under approved protocol; private key never leaves device | Yes (private half) | Not generated |
| Transport ID | Delivery/routing locator or transient endpoint identifier | No; disclose only as required | No | Contract design only |
| Session ID | Identifier for a logical conversation/session | Minimize exposure | No | Schema design only |
| Session keys | Future protocol-derived encryption/authentication secrets | No | Yes | Must not exist yet |
| Recovery material | Future client-controlled recovery secret/material | Never send to server | Yes | Design requirements only |
| Push token | Platform routing token | Restricted operational metadata | Not a cryptographic key | Defer collection; minimize |

### 2.2 FLY ID

FLY ID is a random opaque identifier, not a password, private key, device key, bearer credential or proof of identity.

Planning requirements:
- generate only with operating-system cryptographically secure random number generation if/when implementation is separately authorized;
- never derive it from phone number, email, name, device serial, advertising ID, IP address or other stable personal/device data;
- do not encode timestamps, location, platform or sequential database IDs;
- do not use FLY ID as an API bearer token or authentication secret;
- allow rotation/replacement policy to be specified separately, including contact re-link implications;
- do not expose a directory lookup endpoint until enumeration, correlation, scraping, abuse and metadata threats have been reviewed.

The term “cryptographically secure random number generation” describes use of a platform primitive for an identifier; it does not constitute custom cryptography or create an encryption protocol.

### 2.3 Device identity

Separate two meanings that must not be conflated:
1. **Device record / installation identity:** a non-secret random local identifier used to label an installation record in UX and planning schemas.
2. **Cryptographic device identity:** future public/private key material and protocol enrollment proof.

Phase 1.1 may design a device list with fields such as user label, platform family, last-seen bucket and local status, provided these fields are synthetic or explicitly non-production. It must not imply that a server-side record proves device possession, authenticity or authorization.

Do not use hardware serials, IMEI, MAC addresses, Android ID, IDFV, advertising IDs or fingerprinting as a substitute for device identity. Device record IDs must not be treated as security credentials.

### 2.4 Transport identifiers

Transport identifiers are routing metadata, not FLY IDs and not cryptographic identities.

Requirements for a future transport design:
- use scoped, opaque identifiers;
- avoid globally stable transport IDs where rotation or short-lived aliases are feasible;
- do not reuse push tokens, WebSocket connection IDs or relay routing IDs as account identity;
- keep transport IDs out of user-facing profiles and contact QR codes;
- define retention and deletion windows before collecting them;
- prohibit plaintext content, key material and recovery secrets in routing headers, logs and analytics.

Phase 1.1 defines no live transport service or identifier issuance.

### 2.5 Future session keys

Session keys are reserved concepts only. No placeholder key, fake key exchange, deterministic derivation, local “encrypted” flag, static shared secret or home-grown ratchet may be implemented.

When D-01 is eventually approved:
- protocol library and immutable version must be pinned;
- session keys must be derived and managed only by the approved protocol implementation within the approved native boundary;
- private key material and ratchet state must not cross into React Native/JavaScript, analytics, crash reports or backend services;
- persistence, backup, rotation, replay handling and rollback protection require their own reviewed design;
- test fixtures must use test-only keys and synthetic data, never production secrets.

---

## 3. Non-crypto MVP capability classification

### 3.1 Green — safe to design now

“Green” means documentation, UX and non-sensitive local-only design can proceed. It does not mean production security is approved.

| Component | Planning scope | Boundary |
|---|---|---|
| App shell/navigation | Chats, Calls, Contacts, Settings navigation shell | Empty/disabled states; no real secure chat |
| Onboarding | Welcome, language, accessibility, local preferences | No mandatory phone/email; no claim of anonymous authentication |
| FLY ID profile | Display/copy/share concept for opaque identifier | No auth credential or key semantics |
| Local device list UX | Device labels, platform label, local remove action concept | No cryptographic enrollment/revocation claim |
| Settings | Appearance, language, notification preference UX | Preferences only; no hidden collection |
| Security Center shell | Explain future controls and current availability | Explicit “not implemented” states |
| Privacy Center | Data categories, retention explanations, deletion guidance | Claims must match actual behavior |
| Local-only contact organization | Manual aliases, favorites, notes using synthetic/local data | No server-side discovery or verified identity |
| Help and safety education | Explain limits, verification concepts, recovery warnings | No unsupported guarantees |
| Design system | Components, typography, contrast, focus, motion, responsive behavior | No security status inferred from color/icon |
| Error handling UX | Offline, retry, permission and unsupported states | No secrets or content in error details |
| Localization | Russian/English string catalog and terminology | Security terms reviewed for accuracy |
| Data lifecycle specification | Local cache categories, deletion and export requirements | No implementation or backup claim |
| API schema documentation | Future boundary contracts and data minimization | No live service or production credentials |
| Release governance | Feature flags, environment separation, security review checklist | No cryptographic release gate bypass |

### 3.2 Amber — design only with strict constraints

| Component | Allowed now | Not allowed now |
|---|---|---|
| Chat list and chat screen | UI wireframes, synthetic demo messages, disabled composer | Real user messaging or E2EE label |
| Contacts | Manual local entries and UX | Contact upload, address-book hashing, discoverability API |
| Calls | Navigation shell and explanatory disabled state | WebRTC session, media transport or call privacy claim |
| File sharing | UX requirements and local mock screen | Uploading real files, encryption claim, sharing backend |
| Notifications | Preference UI and payload minimization spec | Content-bearing push or production token collection |
| Multi-device | Device list and lifecycle diagrams | Device linking, trust establishment or key transfer |
| Recovery | Warning UX and requirements | Recovery Kit generation, server escrow or recovery protocol |
| Security indicators | Neutral status labels (“not available”, “not verified”) | Green lock/check icon implying E2EE or verified identity |
| Search | Local synthetic/demo search UX | Server-side content indexing or contact enumeration |
| Analytics | Privacy-preserving event taxonomy design | Message text, contact graph, FLY ID or stable transport IDs in analytics |

### 3.3 Red — prohibited until later authorization and gates

- production message exchange involving real user content;
- any claim or UI state indicating E2EE, encrypted by default, verified, anonymous or untraceable;
- custom cryptography, custom key exchange, DIY ratchet or protocol adaptation;
- key generation/storage/session establishment for production identities;
- server-side plaintext escrow, message content inspection or server-held decryption keys;
- production contact discovery or bulk address-book upload;
- real file transfer, media upload or call service;
- WebRTC calls represented as E2EE without approved application-level media E2EE;
- production recovery or multi-device key transfer;
- push payloads containing message text, names, FLY IDs, file names or call content;
- collecting device fingerprint identifiers for identity/authentication;
- shipping a non-E2EE prototype with real sensitive user data.

---

## 4. Proposed non-crypto MVP scope

### 4.1 MVP-0 — Product shell

Can be specified without a crypto engine:
- app launch and onboarding;
- language selection (Russian/English);
- four primary navigation areas: Chats, Calls, Contacts, Settings;
- responsive layouts and accessibility;
- empty, loading, offline and disabled states;
- local appearance and preference settings;
- privacy/help content.

### 4.2 MVP-1 — Local profile and identifier presentation

Design:
- create/display an opaque FLY ID using a future OS CSPRNG requirement;
- copy/share interaction, with a warning that sharing an ID can enable correlation;
- profile alias and avatar stored locally;
- no phone/email requirement in the product UX;
- no directory, login or account recovery claim until a separate identity service threat model is approved.

The identifier may be displayed in a prototype only if it is synthetic or generated in a separately authorized local-only implementation. It must never be used as a secret credential.

### 4.3 MVP-2 — Device inventory UX

Design:
- list installations with user-defined labels;
- display coarse platform family and last-seen status only where actually available and approved;
- local “remove this installation” interaction in the design prototype;
- clearly distinguish “remove local app data” from “revoke cryptographic device authorization”.

Until cryptographic enrollment exists, there is no cryptographic device trust state. A UI cannot mark a device “trusted” merely because a record exists.

### 4.4 MVP-3 — Privacy and security education

Design:
- Security Center overview with accurate capability availability;
- Privacy Center data inventory;
- recovery-loss warning: loss of all devices and approved recovery material may permanently lose identity/data;
- screenshot-protection limitations;
- device compromise and rooted-device limitations;
- clear separation of transport connectivity and E2EE status.

Do not display a green lock or “secure” badge for transport TLS, an authenticated API connection, local app PIN screen or a device record.

### 4.5 Explicitly deferred product capabilities

- E2EE chat send/receive;
- key verification and safety numbers;
- key transparency;
- multi-device cryptographic linking;
- Recovery Kit and encrypted transfer;
- encrypted file transfer;
- E2EE voice/video calls;
- disappearing-message security guarantees;
- Web crypto parity;
- production push wake-up pipeline until data-flow and token handling are approved.

---

## 5. Data classification and handling policy

### 5.1 Proposed data classes

| Class | Examples | Default handling |
|---|---|---|
| Public | Public help pages, product version | May be public |
| User-local | Alias, avatar preference, UI settings | Local-only by default |
| Identifier metadata | FLY ID, device record ID | Restricted; minimize sharing and logs |
| Transport metadata | Relay route, connection ID, push token | Restricted; short retention; no analytics reuse |
| Sensitive content | Messages, files, voice/video media | Not collected/processed in non-crypto MVP |
| Cryptographic secrets | Private keys, session keys, recovery secrets | Must not exist in Phase 1.1 implementation |
| Security telemetry | Coarse app health/error categories | Minimize; no content, keys or stable identity correlation |

### 5.2 Data minimization requirements

Before any future service accepts data, its schema must specify:
- purpose and owner;
- whether the field is required;
- whether it can be derived locally instead;
- retention and deletion deadline;
- access-control policy;
- log/trace/analytics exclusion;
- user visibility and export/delete behavior;
- correlation/linkability risk;
- breach impact.

Do not collect a field “for future use.” A field without a current documented purpose is excluded.

### 5.3 Logs and crash reports

Never place the following in logs, traces, analytics or crash reports:
- message content or previews;
- contact names/graphs;
- private keys, session keys, ratchet state or Recovery Kit material;
- FLY ID unless a narrowly justified, reviewed operational need exists (default: exclude);
- full transport identifiers, push tokens or invite secrets;
- file names, local file paths or media metadata.

Use bounded error codes and redacted diagnostics. Debug builds must not silently relax these restrictions.

### 5.4 Backups and deletion

Phase 1.1 may specify:
- which local preferences are exportable;
- what local-only profile fields are deleted on uninstall/reset;
- deletion confirmation and consequences;
- backup exclusions for any future secret-bearing storage.

It must not claim cryptographic secure deletion. Flash storage, OS backups, snapshots and forensic recovery create limitations. A future implementation must define its threat model and verify platform-specific behavior.

---

## 6. Trust boundaries and future service contracts

### 6.1 Boundary model

| Boundary | May handle | Must never handle |
|---|---|---|
| UI / React Native | Displayed local profile/preferences; later plaintext only within reviewed UX boundary | Private keys, ratchet state, Recovery Kit secrets |
| Native security module (future) | Approved protocol keys/state and cryptographic operations | Unreviewed analytics/export of secrets |
| Identity/directory service (future) | Minimal public identifiers and approved public-key records | Private keys, session secrets, plaintext escrow |
| Relay service (future) | Opaque routing metadata and ciphertext only after protocol approval | Plaintext, decryption keys, recovery secrets |
| Object storage (future) | Ciphertext blobs only after file protocol approval | Plaintext media or media decryption keys |
| Push provider | Opaque wake-up payload with minimum routing data | Message text, contact names, file names, cryptographic secrets |
| Analytics/crash service | Minimized non-sensitive operational events | Content, identifiers enabling broad correlation, key material |

### 6.2 Service contract principles

Any future API schema must:
- use explicit versioning and strict input validation;
- separate public identifiers from authentication credentials;
- avoid accepting a FLY ID as proof of account/device control;
- avoid returning internal database IDs as public identity;
- avoid storing content in request logs;
- set bounded payload size and rate limits;
- define replay/idempotency behavior where relevant;
- use least-privilege service credentials and narrowly scoped authorization;
- define retention, deletion and abuse controls;
- be threat-modeled before deployment.

No endpoint is implemented or approved by this document.

### 6.3 Transport metadata policy

Transport ID issuance and rotation require a later ADR. The design should consider:
- per-connection or short-lived scoped aliases;
- separation from FLY ID and device record ID;
- bounded retention;
- limited relay visibility;
- no reuse as analytics identity;
- abuse/rate-limit controls that do not create a permanent global user tracking ID.

This document does not prescribe a specific transport protocol or claim anonymity.

---

## 7. UX security semantics

### 7.1 Status vocabulary

Permitted labels in the pre-E2EE MVP:
- “Not available yet”
- “Not configured”
- “Not verified”
- “Demo data”
- “Local only”
- “Connection status”
- “Device record” (not “trusted device”)

Prohibited until verified:
- “End-to-end encrypted”
- “Encrypted by default”
- “Verified contact”
- “Secure call”
- “Anonymous”
- “Untraceable”
- “Protected from screenshots”
- “Recovery guaranteed”

### 7.2 Visual design requirements

- Do not use a closed padlock, green shield or checkmark as a generic success indicator for a non-crypto state.
- Separate network connectivity from identity verification and message encryption.
- Disabled security features must be visibly unavailable, not presented as completed.
- Do not create fake encryption animations, key exchange progress or fabricated fingerprint values.
- Accessibility labels must describe actual behavior, not imply security.
- Any future “verified” status must be tied to a protocol-backed verification state and invalidated on key change.

---

## 8. Threat model for non-crypto MVP

| Threat | Non-crypto mitigation | Residual risk |
|---|---|---|
| FLY ID enumeration | Random opaque ID; no public lookup endpoint in MVP | Shared IDs can be correlated |
| Device fingerprinting | No hardware identifiers or fingerprint collection | OS/app metadata may still leak |
| Identifier correlation | Separate FLY ID, device record ID and transport ID | Operational metadata can correlate events |
| Plaintext exposure | Do not process real message content in MVP | Synthetic demos still require clear labeling |
| Log leakage | Redaction policy and event allowlist | Implementation needs later verification |
| Push metadata leakage | No production push; payload contract excludes content | Provider sees token/connection metadata if later enabled |
| False security claims | Controlled vocabulary and UX review | Copy drift without release review |
| Prototype data misuse | Synthetic data and demo labeling | Users may still enter real content unless UI prevents it |
| Local data recovery | Honest deletion/backup limitations | OS backups and flash remnants |
| API abuse | Design rate limits and least privilege | No live abuse controls implemented |

This table is a planning baseline, not a completed security assessment.

---

## 9. MVP release gates

A non-crypto prototype may be considered for a later, separately authorized demo release only if all applicable gates pass:

### G-UX — Product clarity
- all non-implemented security capabilities clearly marked;
- no misleading lock/verified/encrypted labels;
- Russian and English security terminology reviewed;
- accessibility labels reflect real behavior.

### G-DATA — Data minimization
- data inventory approved;
- no unnecessary personal/device identifiers;
- logs and analytics event allowlist documented;
- deletion and retention behavior specified.

### G-DEMO — Synthetic data
- demo environment visibly separated from production;
- synthetic users/messages/files only;
- no real sensitive content accepted or transmitted;
- demo data reset behavior documented.

### G-API — Backend boundary
- no production endpoint or service introduced without separate authorization;
- API schemas distinguish IDs and credentials;
- no plaintext escrow or secret-bearing fields;
- authentication/authorization and abuse controls reviewed before deployment.

### G-PRIV — Privacy review
- collection purposes and retention documented;
- privacy notice matches actual behavior;
- contact discovery and push token collection remain disabled unless separately reviewed.

### G-REL — Release review
- security owner and product owner sign off;
- feature flags cannot accidentally enable deferred functions;
- no E2EE marketing claim;
- rollback and incident response owner identified.

Passing these gates does not close D-01 or authorize E2EE.

---

## 10. Feature dependency and sequencing plan

| Sequence | Work package | Dependency | Phase 1.1 status |
|---|---|---|---|
| 1 | Product terminology and security copy | Product/security review | SPECIFIABLE |
| 2 | App shell and navigation | UX/design approval | SPECIFIABLE |
| 3 | Local preferences and accessibility | Data inventory | SPECIFIABLE |
| 4 | FLY ID semantics and UX | Identity threat model | DESIGN ONLY |
| 5 | Device inventory UX | Device lifecycle ADR | DESIGN ONLY |
| 6 | Privacy/Security Center shell | Capability truth table | SPECIFIABLE |
| 7 | Contact organization UX | Local-only data boundary | DESIGN ONLY |
| 8 | API/data contracts | Service threat model | DOCUMENT ONLY |
| 9 | Demo environment | Synthetic data and separate authorization | NOT AUTHORIZED |
| 10 | Production identity/directory | D-02/D-03 and security review | DEFERRED |
| 11 | Messaging and session lifecycle | D-01 and later implementation authorization | DEFERRED |
| 12 | Files, calls, push, recovery | Separate protocol and privacy gates | DEFERRED |

---

## 11. Future architecture decision dependencies

Phase 1.1 preserves, but does not resolve, the following decisions:

- **D-01:** crypto protocol/library — OPEN.
- **D-02:** FLY ID and identity binding — must not conflate ID with authentication.
- **D-03:** key transparency and trust directory — no verified-contact state before this is approved.
- **D-04:** multi-device lifecycle — device record is not a cryptographically trusted device.
- **D-05:** Recovery Kit — no server escrow; no recovery protocol implemented.
- **D-06:** 1:1/group messaging — no real messaging protocol.
- **D-08:** metadata privacy — FLY ID/transport separation and retention require review.
- **D-09:** native boundary — future secrets stay inside reviewed native crypto boundary.
- **D-10:** Web security — separate threat model; no parity assumptions.
- **D-11:** supply chain — exact dependency, signing and provenance policy before crypto integration.

---

## 12. Red-Team preservation and project constraints

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

No Red-Team scenario was executed, passed, failed or reclassified in Phase 1.1. `NOT_RUN` is not PASS.

TOR MODE remains excluded from product, architecture, UI, configuration, tests and roadmap. Decentralization remains future-only and outside MVP.

---

## 13. Phase 1.1 execution record

**Performed:**
- fetched Phase 1.0 decision gate and existing Red-Team catalog from the approved base branch;
- confirmed the Phase 1.0 baseline has D-01 OPEN and current execution posture Option C;
- confirmed 27 unique Red-Team IDs and 32 `NOT_RUN` mentions before authoring;
- created a dedicated Phase 1.1 branch from Phase 1.0;
- prepared non-crypto capability classification, identifier taxonomy, data handling rules, UX semantics, trust boundaries, MVP sequencing and release gates.

**Not performed:**
- no code or application implementation;
- no technical spike, build, benchmark or test;
- no backend endpoint, database migration or service deployment;
- no crypto engine, key generation or session establishment;
- no real message, file, call or push flow;
- no Red-Team execution;
- no main branch change;
- no Pull Request creation;
- no production release or security claim.

---

## 14. Formal Phase 1.1 disposition

| Item | Result |
|---|---|
| Non-crypto MVP planning | COMPLETE |
| Safe-to-design components classified | YES |
| Restricted components identified | YES |
| Prohibited pre-D-01 functions identified | YES |
| FLY ID / device identity / transport ID separated | YES |
| Future session keys explicitly deferred | YES |
| Plaintext escrow prohibited | YES |
| Custom cryptography prohibited | YES |
| False E2EE claims prohibited | YES |
| Code / spike / build / tests | NOT PERFORMED |
| D-01 | OPEN |
| Red-Team | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: NON-CRYPTO MVP MAY BE SPECIFIED; IMPLEMENTATION REQUIRES SEPARATE AUTHORIZATION; PRODUCTION E2EE REMAINS DEFERRED; D-01 OPEN.**

The safe scope is product shell, local preferences, profile/identifier UX, device inventory presentation, privacy/security education, localization and documentation. Real messaging, contact discovery, calls, file transfer, recovery, production push and cryptographic identity remain deferred.
