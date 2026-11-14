# FLY SECRET — Phase 1.2
## Non-Crypto MVP Product Architecture

**Status:** PRODUCT ARCHITECTURE SPECIFICATION COMPLETE — IMPLEMENTATION NOT AUTHORIZED  
**Baseline:** Phase 1.1 — Security Architecture Preservation & Non-Crypto MVP Planning  
**Base branch:** `security/fly-secret-non-crypto-mvp-planning-1.1`  
**Working branch:** `security/fly-secret-non-crypto-mvp-product-architecture-1.2`  
**Repository:** `luna7code/luna-apps`  
**Date:** 2026-11-14

> This document specifies product structure, UX, local data boundaries, future API contracts and platform requirements. It does not authorize code, a prototype, production services, data collection, messaging, calls, file transfer, push, recovery or cryptographic implementation.

---

# 1. Product posture and design principles

## 1.1 Product objective

Define a cross-platform FLY SECRET application shell that can be built in independently reviewable stages without prematurely selecting or simulating an E2EE engine.

The pre-crypto MVP is a **local-first product shell and UX demonstrator**, not a secure messaging service. It may provide onboarding, local profile preferences, FLY ID presentation rules, a local device inventory view, settings, privacy education and disabled feature surfaces.

It must not accept real sensitive messages, contacts, media or call content.

## 1.2 Non-negotiable principles

1. Security capability is never inferred from a screen, icon, transport connection or local PIN.
2. FLY ID, Device Record ID, cryptographic device identity, Transport ID, Session ID and Session Keys are separate concepts.
3. No production cryptographic secret exists in this phase.
4. No server-side plaintext escrow or server-held decryption key.
5. No custom cryptography or mock encryption presented as real.
6. UI must truthfully distinguish available, unavailable, demo and local-only capabilities.
7. Data collection is opt-in only where necessary, purpose-bound and minimized; the current phase has no production collection.
8. Platform-specific controls are documented separately; Web is not assumed to have native security parity.
9. TOR MODE is excluded. Decentralization is future-only.
10. All 27 Red-Team scenarios remain NOT_RUN.

## 1.3 Product capability labels

Every feature definition must carry one of these product states:

- `SPECIFIED`: requirements documented; no implementation implied.
- `LOCAL_ONLY`: future implementation may operate only on device and local preferences.
- `DEMO_ONLY`: synthetic data in an isolated demonstrator; not production.
- `DISABLED`: visible explanatory surface, no active workflow.
- `DEFERRED_SECURITY_GATE`: blocked on later architecture/security decision.
- `NOT_IN_SCOPE`: excluded from this phase.

No UI may use “secure”, “encrypted”, “verified”, “anonymous” or “protected” as a generic product state.

---

# 2. Information architecture and navigation

## 2.1 Primary navigation

Four primary destinations, consistent across supported mobile form factors:

| Tab | Purpose | Phase 1.2 availability |
|---|---|---|
| Chats | Future conversation list and chat entry point | Disabled / demo-only shell |
| Calls | Future call history and call entry point | Disabled shell |
| Contacts | Local contact organization and future verified contact list | Local-only design; no discovery |
| Settings | Profile, preferences, privacy, security education, app information | Available as specification |

The primary navigation is a product information architecture decision, not a claim that the underlying features work.

## 2.2 Navigation behavior

- Android/iOS: four-item bottom navigation for primary destinations.
- Compact widths: labels remain available to assist accessibility; avoid icon-only navigation unless screen-reader labels and clear selection state are provided.
- Large screens: navigation rail or persistent side navigation may replace the bottom bar while preserving the same route hierarchy.
- Web: persistent sidebar or responsive navigation shell; browser history must work with direct links and back/forward.
- Deep links: Phase 1.2 documents route naming only. No invitation, message or call deep link is activated.
- Modal routes: use for short tasks such as editing a local alias or viewing an explanation; long settings pages use full screens.
- Navigation state must not contain secrets, message content or recovery material.

## 2.3 Route inventory

| Route ID | Route concept | Parent |
|---|---|---|
| APP-ROOT | App shell | — |
| TAB-CHATS | Chats | APP-ROOT |
| CHAT-DETAIL-DEMO | Demo conversation | TAB-CHATS |
| TAB-CALLS | Calls | APP-ROOT |
| CALL-DETAIL-DISABLED | Call explanation | TAB-CALLS |
| TAB-CONTACTS | Contacts | APP-ROOT |
| CONTACT-LOCAL-DETAIL | Local contact detail | TAB-CONTACTS |
| TAB-SETTINGS | Settings | APP-ROOT |
| SETTINGS-PROFILE | Local profile | TAB-SETTINGS |
| SETTINGS-DEVICES | Device inventory concept | TAB-SETTINGS |
| SETTINGS-PRIVACY | Privacy Center | TAB-SETTINGS |
| SETTINGS-SECURITY | Security Center | TAB-SETTINGS |
| SETTINGS-NOTIFICATIONS | Notification preferences | TAB-SETTINGS |
| SETTINGS-APPEARANCE | Appearance and accessibility | TAB-SETTINGS |
| SETTINGS-LANGUAGE | Language | TAB-SETTINGS |
| SETTINGS-ABOUT | Version, licenses, support | TAB-SETTINGS |
| ONBOARDING-* | First-run setup | APP-ROOT |

These are route identifiers for design and planning only.

---

# 3. Screen architecture

## 3.1 Onboarding and first launch

### ONB-01 — Welcome
Purpose: explain what FLY SECRET is intended to become and accurately describe the current non-crypto state.

Elements:
- product name and concise purpose;
- “Continue” action;
- language selection entry;
- visible note that secure messaging is not yet available in this build, if a demonstrator is later authorized.

States: first launch, returning user, unsupported version.

### ONB-02 — Language
- Russian and English.
- Language selection persisted locally.
- No locale-based identity inference.

### ONB-03 — Setup choice
- “Настроить приложение” / “Быстрый просмотр” (or equivalent localized copy).
- Both paths remain local-only.
- No account registration, phone/email request or backend identity enrollment.

### ONB-04 — Local profile setup
- optional display alias;
- optional local avatar selection, only if separately implemented;
- FLY ID shown only as a synthetic/demo placeholder until actual local generation is separately authorized;
- clear explanation that a displayed identifier is not a password or proof of identity.

### ONB-05 — Privacy briefing
- what the current build stores locally;
- what is not implemented;
- no contact upload or message processing;
- deletion limitations and OS backup caveats.

### ONB-06 — Completion
- enter app shell;
- no fake “account secured” or “encryption enabled” confirmation.

## 3.2 Chats

### CHAT-01 — Conversation list shell
May display empty state and synthetic demo entries only.

Empty state:
- explain that real messaging is not available;
- no fake online status, delivery ticks, read receipts or E2EE badges;
- no real contacts imported.

### CHAT-02 — Demo conversation
If a separate demo is later authorized:
- visibly marked DEMO;
- synthetic text only;
- no free-form composer for real content unless a local-only test mode is specifically authorized;
- no send, delivery or read state that resembles a real network exchange.

### CHAT-03 — Conversation details
Design-only fields: local display name, local note, demo state. Do not show verified key fingerprint, session state or device trust.

### CHAT-04 — Search
Local synthetic demo search only. No server-side content search or contact enumeration.

## 3.3 Calls

### CALL-01 — Calls landing
- explanatory disabled state;
- no microphone/camera permission prompt in the non-crypto shell;
- no call history generated from real communication.

### CALL-02 — Call details
Design-only state. No IP address, ICE candidate, relay or peer diagnostic collection.

### CALL-03 — Call readiness explanation
Explain that real calls are deferred pending a separately reviewed media security design. Do not describe WebRTC transport encryption as proof of app-level E2EE.

## 3.4 Contacts

### CONTACT-01 — Contacts list
- local manual entries only, if implementation later authorized;
- local aliases and favorites;
- synthetic examples in demo;
- no system address-book permission request by default.

### CONTACT-02 — Local contact detail
- alias, optional local note and favorite flag;
- no “verified” status;
- no public-key fingerprint placeholder that looks authentic;
- no FLY ID resolution service.

### CONTACT-03 — Add local contact
- manual local entry UX;
- any FLY ID field must be labeled as a local note/unverified identifier until a reviewed directory and verification protocol exist;
- no invitation delivery.

### CONTACT-04 — Verification explanation
Explain future key verification conceptually. State “Not available yet”; no QR key exchange or safety number is generated.

## 3.5 Settings and account presentation

### SET-01 — Settings home
Sections:
- Profile
- Devices
- Privacy Center
- Security Center
- Notifications
- Appearance and accessibility
- Language
- About and support

### SET-02 — Local profile
- display alias;
- local avatar preference (future local-only);
- FLY ID explanation and copy/share design only;
- warning that sharing an identifier can permit correlation.

### SET-03 — Device inventory
- device records represented as local labels or synthetic entries;
- no cryptographic “trusted” badge;
- local remove/reset explanation;
- distinguish “remove this local record” from “revoke cryptographic device authorization”.

### SET-04 — Privacy Center
- current data inventory;
- local storage categories;
- permission status only where the platform actually exposes it;
- retention/deletion explanation;
- no privacy score implying a completed audit.

### SET-05 — Security Center
Groups:
- Identity and verification — not available;
- Encrypted messaging — not available;
- Device trust — not available;
- Recovery — not available;
- App lock/biometrics — design requirements only, not implemented in this phase;
- Security notices — static educational content.

### SET-06 — Notifications
- local preference toggles may be specified;
- no push token collection or permission prompt in this phase;
- no preview-content preference until a real notification system is separately reviewed.

### SET-07 — Appearance and accessibility
- theme preference only if selected for implementation;
- text scaling, contrast, reduced motion;
- keyboard and screen-reader navigation.

### SET-08 — Language
- Russian/English;
- user-controlled, stored locally.

### SET-09 — About
- app version/build environment in a future implementation;
- open-source licenses and legal notices;
- support link only when an official destination exists;
- no fabricated security certification.

---

# 4. User journeys and acceptance behavior

## 4.1 First launch

1. Launch app.
2. Select Russian or English.
3. Choose setup or quick preview.
4. Read privacy briefing.
5. Optionally enter local display alias.
6. Enter app shell.
7. Land on Chats empty state.

Acceptance:
- no phone/email is requested;
- no contacts are uploaded;
- no network account is created;
- no E2EE or secure identity claim appears;
- user can reach Settings and Privacy Center.

## 4.2 View FLY ID concept

1. Open Settings → Profile.
2. Read explanation.
3. View a synthetic ID in a demo, or a locally generated ID only after separate implementation authorization.
4. Copy/share action is disabled in specification-only build.
5. Read correlation warning.

Acceptance:
- FLY ID is never described as a password, token or private key;
- no ID is derived from personal/device information;
- no directory lookup occurs.

## 4.3 Review devices

1. Open Settings → Devices.
2. View local/synthetic device records.
3. Open a record.
4. Read difference between local record removal and future cryptographic revocation.

Acceptance:
- no record is called trusted;
- no hardware identifier is shown;
- no remote revoke action is claimed.

## 4.4 Review privacy

1. Open Settings → Privacy Center.
2. View data categories.
3. Inspect local storage and permission explanations.
4. Read deletion limitations.

Acceptance:
- claims match actual build behavior;
- no “secure delete” guarantee;
- no hidden data collection.

## 4.5 Open deferred features

1. Open Calls or a chat demo route.
2. See an explicit unavailable/demo state.
3. Read what is missing.
4. Return to navigation.

Acceptance:
- no network permission is requested;
- no real message or call is started;
- no fake sent/delivered/read state;
- no fake encryption animation.

## 4.6 Reset local preferences

1. Open Settings → Privacy Center or local data controls.
2. Review what will be reset.
3. Confirm.
4. Clear only the defined local preference/demo namespace.

Acceptance:
- reset scope is explicit;
- no claim that OS backups, logs or flash remnants are forensically erased;
- no server-side account deletion is implied.

---

# 5. State model

## 5.1 Application lifecycle

| State | Meaning | Allowed transition |
|---|---|---|
| COLD_START | App process starts | BOOTSTRAPPING |
| BOOTSTRAPPING | Load app version and local preferences | FIRST_RUN / READY / RECOVERY_REQUIRED |
| FIRST_RUN | No local setup preference exists | ONBOARDING / QUICK_PREVIEW |
| ONBOARDING | Local setup screens | READY |
| QUICK_PREVIEW | Synthetic/local preview | READY |
| READY | App shell available | BACKGROUND / ERROR |
| BACKGROUND | App not foregrounded | READY / TERMINATED |
| ERROR | Recoverable local UI error | READY / BOOTSTRAPPING |
| RECOVERY_REQUIRED | Local preferences invalid/corrupt | RESET_LOCAL / SUPPORT_INFO |

No cryptographic recovery state is represented.

## 5.2 Feature availability state

Each feature must expose a single explicit availability state:
- AVAILABLE_LOCAL
- DEMO_ONLY
- DISABLED
- DEFERRED_SECURITY_GATE
- NOT_IN_SCOPE

Do not overload feature availability with network connectivity or encryption state.

## 5.3 Network state

The shell may later display:
- OFFLINE
- ONLINE
- DEGRADED
- UNKNOWN

In Phase 1.2, no service connection is required. A network indicator must not be presented as a security indicator.

## 5.4 Identity and device state

Pre-crypto identity display states:
- NO_LOCAL_PROFILE
- LOCAL_PROFILE_PRESENT
- SYNTHETIC_FLY_ID
- LOCAL_FLY_ID (only after separately authorized local implementation)

Device record states:
- LOCAL_RECORD
- DEMO_RECORD
- REMOVED_LOCAL

There is no TRUSTED_DEVICE, VERIFIED_DEVICE or REVOKED_CRYPTO_DEVICE state in this phase.

## 5.5 UI state invariants

- Loading state does not imply cryptographic negotiation.
- Success state does not imply secure identity or E2EE.
- Offline state does not imply private mode.
- App lock state does not imply message encryption.
- “Local only” must mean no network transmission for that feature, not merely that the UI is rendered locally.

---

# 6. Local data model

All models below are logical schema proposals. No database, migration or serialization code is created by Phase 1.2.

## 6.1 Data domains

| Domain | Storage posture | Phase 1.2 |
|---|---|---|
| App preferences | Local, non-sensitive | Schema proposal |
| Local profile | Local, user-editable | Schema proposal |
| FLY ID | Opaque identifier, restricted | Semantics only |
| Device record | Local/synthetic inventory | Schema proposal |
| Local contacts | Local-only, optional | Schema proposal |
| Demo data | Synthetic and segregated | Schema proposal |
| Security education content | Bundled/static | Content model |
| Transport metadata | Not stored | Prohibited in this phase |
| Session keys / crypto state | Must not exist | Prohibited |
| Message/file/call content | Must not be collected | Prohibited |

## 6.2 Proposed logical entities

### AppPreferences
- schema_version
- locale: `ru | en`
- appearance_preference
- reduced_motion
- text_scale
- accessibility_preferences
- onboarding_completed
- updated_at

No advertising ID, tracking ID or fingerprint.

### LocalProfile
- local_profile_id: random local record identifier, not public identity
- display_alias
- avatar_local_reference (optional; no remote URL by default)
- created_at
- updated_at

Do not store email/phone by default.

### FlyIdentifier
- value: opaque FLY ID
- lifecycle: `SYNTHETIC | LOCAL_ONLY`
- created_at
- disclosure_warning_acknowledged

No private key, password, bearer token or secret field.

### DeviceRecord
- device_record_id: opaque local record identifier
- display_label
- platform_family: `ANDROID | IOS | WEB | DESKTOP | UNKNOWN`
- record_origin: `LOCAL | DEMO`
- last_seen_bucket: optional coarse display value only
- created_at
- updated_at

Must not contain IMEI, serial, MAC, advertising ID, Android ID, IDFV, hardware fingerprint, device private key or attestation blob.

### LocalContact
- local_contact_id
- display_alias
- optional_fly_id_note (explicitly unverified text)
- local_note
- is_favorite
- created_at
- updated_at

No imported address-book data or public-key verification state.

### DemoFixture
- fixture_id
- fixture_type
- synthetic_payload
- created_at
- demo_label

Must be stored in a namespace separate from future production data and easy to reset.

### FeatureAvailability
- feature_key
- availability_state
- explanatory_copy_key
- minimum_app_version (optional)
- updated_at

Must not include a boolean called `is_encrypted` or a fake trust flag.

## 6.3 Explicitly absent entities

The Phase 1.2 schema must not define production records for:
- Message, MessageBody, Attachment or MediaBlob;
- SessionKey, RatchetState, PreKey, IdentityPrivateKey;
- RecoveryKitSecret or KeyBackup;
- VerifiedContact or TrustedDevice;
- PushToken or production TransportEndpoint;
- CallSession, ICECandidate or media encryption context.

Future phases may define these only after the corresponding security decisions and separate authorization.

## 6.4 Local storage rules

- Prefer a small, versioned local preference store for non-sensitive settings.
- Do not place secrets in AsyncStorage, browser localStorage, logs, crash reports or URL state.
- Web localStorage is not a secure vault and must not store secrets.
- Demo fixture namespace must be separate from local profile/preferences.
- Define reset behavior by data domain; no broad destructive wipe without confirmation.
- Any future sensitive local storage requires a platform-specific threat model and approved storage design.

---

# 7. Future API contracts — documentation only

No endpoints are implemented or approved for deployment. The contracts below are interface constraints to prevent future layers from collapsing identity, device and transport concepts.

## 7.1 API design principles

- HTTPS transport is necessary but is not E2EE.
- FLY ID is not an authentication credential.
- No endpoint accepts private keys, session keys, ratchet state or Recovery Kit secrets.
- No plaintext message/file/call payloads are accepted by the non-crypto MVP.
- Every endpoint requires purpose, authorization, rate-limit, retention and logging policy.
- Use opaque internal identifiers; do not expose sequential database IDs.
- Strict schema validation, bounded payload sizes, versioning and generic error responses.
- No public user lookup or contact discovery endpoint in this phase.

## 7.2 Proposed future contract inventory

| Contract ID | Method/path concept | Intended purpose | Phase status |
|---|---|---|---|
| API-IDENT-01 | POST /v1/identity/registration-challenge | Future registration challenge | NOT IMPLEMENTED |
| API-IDENT-02 | POST /v1/identity/complete | Future identity enrollment | BLOCKED by identity/crypto decisions |
| API-IDENT-03 | GET /v1/identity/{opaque_id}/public-record | Future minimal public record | BLOCKED by directory/key-transparency decisions |
| API-DEVICE-01 | GET /v1/devices | Future authorized device inventory | BLOCKED by device lifecycle |
| API-DEVICE-02 | POST /v1/devices/link-challenge | Future device linking | BLOCKED by crypto and device lifecycle |
| API-DEVICE-03 | DELETE /v1/devices/{device_record_id} | Future revoke workflow | BLOCKED; deletion is not revocation until protocol-backed |
| API-TRANSPORT-01 | POST /v1/transport/lease | Future short-lived routing lease | NOT IMPLEMENTED; metadata review required |
| API-PREF-01 | GET /v1/preferences | Optional future sync of non-secret preferences | OUTSIDE current local-first MVP |
| API-PREF-02 | PUT /v1/preferences | Optional future sync of non-secret preferences | OUTSIDE current local-first MVP |

These are conceptual names, not committed API routes. No route should be exposed until a separate service design is approved.

## 7.3 Example schema constraints

A future identity registration request may contain:
- protocol_version;
- client_build;
- public enrollment material only, after protocol approval;
- challenge response bound to the selected protocol.

It must not contain:
- private key;
- session key;
- plaintext recovery secret;
- raw device fingerprint;
- phone/email unless a separate approved product requirement exists.

A future device inventory response may contain:
- opaque device record reference;
- user-defined label;
- coarse platform family;
- last-seen bucket, if justified.

It must not imply cryptographic trust unless a protocol-backed enrollment and verification state exists.

A future transport lease may contain:
- short-lived opaque routing alias;
- expiry;
- limited delivery parameters.

It must not use FLY ID as the routing identifier and must not contain plaintext, decryption keys or stable cross-service tracking IDs.

## 7.4 Error contract

Future APIs should use stable generic error classes:
- INVALID_REQUEST
- UNAUTHORIZED
- FORBIDDEN
- NOT_FOUND
- RATE_LIMITED
- CONFLICT
- TEMPORARILY_UNAVAILABLE
- INTERNAL_ERROR

Do not return whether a particular FLY ID exists when that enables enumeration. Do not include secrets, submitted content or internal stack traces in client-visible errors.

## 7.5 Logging contract

Default-deny logging for:
- request bodies containing identifiers beyond approved operational minimum;
- contact data;
- message/file/call content;
- private or session keys;
- recovery material;
- full transport IDs and push tokens.

Operational metrics should be aggregate, bounded and reviewed for linkability.

---

# 8. Platform requirements

## 8.1 Android

Product-shell requirements:
- support current project-approved Android SDK baseline (exact min/target SDK to be decided in a platform ADR);
- responsive layouts for compact and large screens;
- system back behavior and predictable navigation restoration;
- edge-to-edge and keyboard/IME behavior reviewed;
- TalkBack labels, focus order, text scaling and contrast;
- runtime permissions requested only at point of a separately approved feature need;
- no contacts, microphone, camera, nearby-device or notification permission prompts in the Phase 1.2 shell;
- app data reset and backup behavior documented.

Security constraints:
- do not claim FLAG_SECURE prevents all capture; it cannot prevent external cameras or all privileged capture;
- no device serial/IMEI/MAC/Android ID collection;
- no secrets in preferences, logs or clipboard;
- no private key store or crypto adapter is implemented in this phase;
- Android Keystore integration is deferred until an approved cryptographic design exists.

## 8.2 iOS

Product-shell requirements:
- support project-approved iOS deployment target (exact version to be decided separately);
- safe-area, Dynamic Type, VoiceOver and keyboard behavior;
- standard navigation and modal dismissal;
- permission prompts absent unless a later authorized feature requires them;
- clear local data reset semantics.

Security constraints:
- do not treat IDFV or device model as a cryptographic identity;
- no secrets in UserDefaults, logs, analytics or pasteboard;
- no Keychain crypto integration in this phase;
- no claim that app switcher snapshot suppression or screen-capture notifications prevent all capture;
- Secure Enclave/Keychain design is deferred to the approved crypto/native boundary.

## 8.3 Web

Product-shell requirements:
- responsive layout and browser back/forward support;
- direct route loading and refresh behavior;
- keyboard navigation and screen-reader support;
- clear unsupported-feature states;
- no automatic contact import or permission requests.

Security constraints:
- Web is a separate threat model, not equivalent to native apps;
- browser localStorage is not a secure secret store;
- XSS, malicious extensions, compromised dependencies, browser runtime and shared-device exposure must be considered;
- no production secrets or cryptographic session state in JavaScript storage;
- no Web E2EE parity claim;
- content security policy, dependency controls, secure headers and origin isolation require a separate Web release gate.

## 8.4 Desktop (future product surface)

Desktop is not a Phase 1.2 implementation target. If later included:
- use a separately approved desktop shell and update model;
- protect local preferences and temporary files;
- define OS credential-store boundary;
- consider multi-user/shared-device and clipboard risks;
- no desktop/native crypto parity claim without independent review.

## 8.5 Cross-platform consistency

Shared product behavior:
- same navigation destinations and capability vocabulary;
- same feature availability state model;
- same identity terminology;
- same privacy copy and data categories;
- platform-native accessibility and permission behavior;
- no assumption that identical UI means identical security properties.

---

# 9. Design system and interaction contracts

## 9.1 Component families

- AppShell
- PrimaryNavigation
- ScreenHeader
- EmptyState
- DisabledFeaturePanel
- DemoDataBanner
- LocalProfileCard
- IdentifierDisclosurePanel
- DeviceRecordListItem
- PrivacyDataCategory
- SecurityCapabilityRow
- PreferenceRow
- ConfirmationDialog
- InlineError
- OfflineNotice
- AccessibilitySettingsGroup

These are component specifications only; no source components are created.

## 9.2 Component invariants

- DisabledFeaturePanel must state why a feature is unavailable.
- DemoDataBanner must remain visible throughout demo routes.
- IdentifierDisclosurePanel must explain correlation risk.
- DeviceRecordListItem must not imply trust.
- SecurityCapabilityRow must distinguish current state from planned state.
- ConfirmationDialog must name exact data scope affected.
- EmptyState must not promise a launch date or imply a live service.

## 9.3 Interaction states

Every interactive component documents:
- default;
- pressed/active;
- focus-visible;
- disabled;
- loading (only when an actual local operation exists);
- error;
- accessibility announcement.

No loading spinner may imitate key exchange, message encryption or secure connection establishment.

---

# 10. Non-functional requirements

## 10.1 Performance

Targets to be established by implementation planning, not claimed as measured:
- cold start and navigation responsiveness measured on a declared device matrix;
- no blocking synchronous work on UI thread;
- bounded memory use for shell and local preferences;
- no background network polling in non-crypto MVP;
- reduced-motion support.

No performance benchmark was run in Phase 1.2.

## 10.2 Reliability

- versioned local preference schema;
- defensive handling of malformed/corrupt local preferences;
- recoverable onboarding interruption;
- no silent loss of user-entered local profile preferences;
- explicit reset confirmation.

## 10.3 Accessibility

- semantic roles and labels;
- keyboard navigation on Web/Desktop;
- TalkBack and VoiceOver support;
- Dynamic Type/text scaling;
- minimum contrast compliance target to be measured in implementation QA;
- reduced motion and no color-only status distinction.

## 10.4 Localization

- Russian and English as initial locales;
- all user-visible strings externalized;
- no concatenated sentence fragments;
- security terminology reviewed by product/security owner;
- dates, numbers and plural forms localized;
- route IDs and API field names remain locale-neutral.

---

# 11. Analytics and observability policy

No analytics SDK or crash reporting integration is authorized.

If a later phase proposes telemetry:
- define event allowlist before implementation;
- collect only aggregate app health and feature availability events;
- exclude FLY ID, device record IDs, contact graph, content, transport IDs and secrets;
- no session replay or screen recording;
- no user-level cross-device analytics identity;
- define retention and deletion;
- document third-party processor and data residency;
- review privacy notice and consent.

Permitted future aggregate event examples: app_started, onboarding_completed, local_reset_completed, non_sensitive_error_category. These are examples for review, not authorized event emission.

---

# 12. QA and acceptance matrix for a future implementation

No tests are run by Phase 1.2. The following are future acceptance criteria.

| Area | Acceptance criterion | Current status |
|---|---|---|
| Navigation | All four primary destinations reachable; back behavior consistent | NOT_RUN |
| Onboarding | First-run and quick-preview paths remain local-only | NOT_RUN |
| FLY ID | Never treated as password/token/private key | NOT_RUN |
| Device inventory | No hardware fingerprint or false trust state | NOT_RUN |
| Contacts | No system address-book access or upload | NOT_RUN |
| Chats | Demo-only state cannot send real content | NOT_RUN |
| Calls | No microphone/camera/network session in shell | NOT_RUN |
| Privacy | Data inventory matches actual storage | NOT_RUN |
| Reset | Only declared local data namespaces cleared | NOT_RUN |
| Android | TalkBack, back stack, IME, permissions checked | NOT_RUN |
| iOS | VoiceOver, safe area, Dynamic Type, permissions checked | NOT_RUN |
| Web | Direct routes, CSP, XSS threat model, storage checked | NOT_RUN |
| Security semantics | No false E2EE/verified/anonymous claims | NOT_RUN |
| Logs | No content, secrets or stable identifiers | NOT_RUN |
| Supply chain | Dependency and build provenance gates reviewed | NOT_RUN |

The 27 Red-Team scenarios are a separate security catalog and remain NOT_RUN. These product acceptance rows do not replace or reclassify them.

---

# 13. Architecture decisions and open questions

Phase 1.2 makes product-level proposals only. It does not close architecture ADRs.

| Decision | Phase 1.2 position | Status |
|---|---|---|
| Primary navigation | Chats, Calls, Contacts, Settings | Proposed product baseline |
| First-run setup | Setup or quick preview | Proposed |
| Local profile | Alias and optional local avatar | Proposed |
| FLY ID | Opaque, non-secret, no authentication semantics | Preserved |
| Device identity | Separate local record and future cryptographic identity | Preserved |
| Transport ID | Scoped routing metadata, separate from identity | Preserved |
| Session keys | Future protocol-only secrets | Deferred |
| Contacts | Local-first manual organization | Proposed |
| Chat/Calls/Files | Disabled/demo shells only | Deferred |
| Local storage | Preferences/profile/demo namespace only | Proposed |
| API | Contracts only, no endpoints | Documentation only |
| Android/iOS/Web | Distinct platform controls and threat models | Required |
| Desktop | Future product surface | Deferred |
| D-01 crypto engine | No selection | OPEN |

### 13.1 Product questions for future sign-off

1. Confirm primary navigation labels and ordering.
2. Confirm whether the first authorized demonstrator should be mobile-only or include Web shell.
3. Confirm whether FLY ID should be visible during onboarding or only after local profile setup.
4. Confirm whether local contacts belong in the first demonstrator or should remain a later shell.
5. Confirm visual design system and accessibility minimums before implementation.

These questions do not block completion of this planning document and do not authorize implementation.

---

# 14. Phase 1.2 scope boundaries

Explicitly not delivered:
- real-time messaging or message storage;
- E2EE or crypto engine selection;
- private/public key generation or session establishment;
- device linking, cryptographic trust or revocation;
- Recovery Kit, backup or key transfer;
- contact discovery or address-book upload;
- file transfer or media storage;
- voice/video calls or WebRTC;
- production push notification pipeline;
- analytics SDK or crash-reporting integration;
- backend deployment, database migration or live API;
- Android/iOS/Web/Desktop app code;
- test execution, performance benchmark or security audit.

TOR MODE remains excluded from product, architecture, UI, configuration, tests and roadmap. Decentralization remains future-only.

---

# 15. Phase 1.2 execution record

**Performed:**
- fetched the Phase 1.1 baseline from the approved branch;
- confirmed its planning-only posture and preserved project constraints;
- created a dedicated Phase 1.2 branch from Phase 1.1;
- prepared the product information architecture, route inventory, screen specifications, user journeys, state model, logical local data schema, future API contract constraints, platform requirements, design-system contracts and future QA acceptance matrix.

**Not performed:**
- no code or application implementation;
- no prototype, technical spike, build or benchmark;
- no test or Red-Team execution;
- no backend, database or API deployment;
- no messaging, calls, files, push, recovery or E2EE;
- no crypto engine selection or key generation;
- no main branch modification;
- no Pull Request creation.

---

# 16. Formal Phase 1.2 disposition

| Item | Result |
|---|---|
| Product information architecture | COMPLETE |
| Navigation and route inventory | COMPLETE |
| Screen specifications | COMPLETE |
| User journeys | COMPLETE |
| UI/application state model | COMPLETE |
| Logical local data model | COMPLETE |
| Future API contract constraints | COMPLETE |
| Android requirements | COMPLETE |
| iOS requirements | COMPLETE |
| Web requirements | COMPLETE |
| Desktop posture | DEFERRED |
| Implementation | NOT AUTHORIZED |
| D-01 | OPEN |
| Red-Team | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: NON-CRYPTO MVP PRODUCT ARCHITECTURE SPECIFIED. IMPLEMENTATION REQUIRES SEPARATE AUTHORIZATION. NO PRODUCTION MESSAGING OR SECURITY CLAIMS. D-01 REMAINS OPEN.**
