# FLY SECRET — Phase 1.4.1
## UX Security Remediation Specification

**Status:** REMEDIATION SPECIFICATION COMPLETE — OWNER APPROVAL AND IMPLEMENTATION NOT AUTHORIZED  
**Baseline review:** Phase 1.4 — UX Security Review & Design Approval  
**Design baseline:** Phase 1.3 — UX Architecture & Design System Specification  
**Product baseline:** Phase 1.2 — Non-Crypto MVP Product Architecture  
**Branch:** `security/fly-secret-ux-security-remediation-spec-1.4.1`  
**Date:** 2026-11-14

> This is a normative text/design remediation specification. It defines required copy, component behavior, screen changes, status semantics, contrast tokens and acceptance evidence. It does not implement them. No source code, prototype, Figma artifact, spike, build, runtime test, accessibility audit or PR was created.

---

# 1. Purpose and disposition

Phase 1.4 identified 15 UX security findings: 5 High, 7 Medium and 3 Low. This phase translates every finding into a precise before/after requirement and a closure checklist.

**Disposition:** The remediation specification is complete. Findings are not marked CLOSED merely because a proposed correction is documented. Closure requires:
1. owner approval of the wording and design change;
2. corresponding update to the canonical UX specification;
3. implementation evidence when implementation is later authorized;
4. relevant test evidence;
5. review sign-off.

No implementation authorization is granted by this document.

## 1.1 Finding inventory

| ID | Severity | Area | Remediation state |
|---|---|---|---|
| UX-SEC-01 | HIGH | Contrast | SPECIFIED — evidence required |
| UX-SEC-02 | HIGH | FLY ID input / identity confusion | SPECIFIED — owner approval required |
| UX-SEC-03 | HIGH | LOCAL ONLY claim | SPECIFIED — data-flow evidence required |
| UX-SEC-04 | HIGH | Device record vs trust | SPECIFIED — owner approval required |
| UX-SEC-05 | HIGH | Security Center / non-crypto disclosure | SPECIFIED — owner approval required |
| UX-SEC-06 | MEDIUM | Status taxonomy | SPECIFIED — owner approval required |
| UX-SEC-07 | MEDIUM | Demo provenance | SPECIFIED — route and accessibility evidence required |
| UX-SEC-08 | MEDIUM | Network status semantics | SPECIFIED — signal definition required |
| UX-SEC-09 | MEDIUM | FLY ID disclosure/share | SPECIFIED — future gate |
| UX-SEC-10 | MEDIUM | Accessibility acceptance | SPECIFIED — QA evidence required |
| UX-SEC-11 | MEDIUM | Terminology governance | SPECIFIED — document update required |
| UX-SEC-12 | MEDIUM | Local reset semantics | SPECIFIED — data inventory required |
| UX-SEC-13 | LOW | Chats/Calls affordances | SPECIFIED — owner approval required |
| UX-SEC-14 | LOW | Disabled-action reason | SPECIFIED — component acceptance required |
| UX-SEC-15 | LOW | Identifier rendering | SPECIFIED — future identity gate |

No finding is represented as runtime-verified or closed.

---

# 2. Normative product-wide security disclosure

## 2.1 Required disclosure — exact English copy

**Title:** Security features are not available in this build

**Body:** This build does not provide private messaging, end-to-end encryption, identity verification, secure calls or account recovery. Do not use it to exchange sensitive information. Demo content is synthetic.

**Action:** Got it

The disclosure is informational and must not be styled as a successful security state. The action only acknowledges the notice; it does not enable a capability.

## 2.2 Required disclosure — Russian copy

**Заголовок:** Функции безопасности недоступны в этой сборке

**Текст:** Эта сборка не поддерживает приватные сообщения, сквозное шифрование, проверку личности, защищённые звонки или восстановление аккаунта. Не используйте её для передачи конфиденциальной информации. Демонстрационные данные являются искусственными.

**Кнопка:** Понятно

Translations must preserve the same scope and warning strength. A shorter translation may not remove any unavailable capability or the instruction not to exchange sensitive information.

## 2.3 Placement and behavior

- Show during onboarding before any demo content is presented.
- Show a persistent, compact disclosure in the app shell on all routes in the non-crypto build.
- On small screens, the compact disclosure may collapse to a clearly labelled banner, but the full text must be available with one action and be announced to assistive technology.
- It cannot be dismissed permanently for the lifetime of the build.
- Do not use a green success color, shield-with-check icon, lock icon, encryption animation or “protected” visual.
- Do not imply that acknowledging the notice activates or configures security.
- Do not use a toast as the only disclosure channel.

## 2.4 Before / after

| Before | After |
|---|---|
| Individual unavailable labels within security feature rows | One clear app-wide statement plus specific row-level unavailability |
| Security Center may appear as a normal security dashboard | Security capabilities information page with explicit non-crypto notice |
| Demo context may be discovered after navigation | Disclosure appears before demo use and persists in shell |

**Closure evidence:** screenshot/spec review for onboarding and all top-level routes; English/Russian copy review; screen-reader announcement plan; confirmation no E2EE/security success affordance remains.

---

# 3. Detailed remediation — five HIGH findings

## UX-SEC-01 — Contrast token remediation

### Before
Light tertiary text token `#777E7A` on `#F7F7F5`, calculated at approximately 3.88:1.

### After — normative token proposal
- Replace `color.text.tertiary` with **`#626A65`** for light theme.
- Keep `color.canvas` at `#F7F7F5`.
- Calculated contrast for `#626A65` on `#F7F7F5`: approximately **5.19:1**.
- Keep `#777E7A` unavailable for normal text on light canvas; it may only be considered for non-text decoration after separate review.
- Do not infer contrast validity on other surfaces from this one calculation.
- Every semantic token pair must be explicitly listed in a contrast matrix.

### Required pairing matrix

| Foreground token | Background | Required threshold |
|---|---|---:|
| Normal text | Each approved surface | ≥4.5:1 |
| Large text | Each approved surface | ≥3:1 |
| Essential icons/control boundaries | Adjacent surface | ≥3:1 |
| Focus indicator | Adjacent colors | ≥3:1 |
| Selected/unselected state | Adjacent state colors | ≥3:1 where non-text contrast applies |
| Placeholder/helper/metadata text | Each approved surface | ≥4.5:1 |
| Error/warning/success text | Each approved surface | ≥4.5:1 |

Large text means at least 18pt regular or 14pt bold (or equivalent CSS sizing). Do not use this exception for ordinary metadata.

### Component requirements
- No component may select text color independently of semantic tokens.
- Cards, dialogs, sheets, navigation, badges, form controls, toasts and banners must declare approved foreground/background pairings.
- Disabled state must remain understandable. Do not reduce disabled text opacity until it fails contrast; explain unavailability in readable text.
- Focus indicator cannot rely on color alone; provide visible shape/outline and state semantics.

### Closure criteria
1. Full light/dark token-pair matrix exists.
2. Every used pair meets the applicable threshold.
3. Contrast calculations are recorded with exact hex values and surface context.
4. No essential text uses an unverified token.
5. Manual rendered-state review is completed after implementation authorization.
6. Accessibility reviewer signs off.

**Status:** SPECIFIED; not tested in a rendered app.

## UX-SEC-02 — Remove free-form FLY ID from current contact flow

### Before
CONTACT-03 permits an optional unverified FLY ID note, which can resemble a valid identifier.

### After — current non-crypto MVP
- Remove the FLY ID field from CONTACT-03.
- Do not offer a field named FLY ID, user ID, account ID or verified identifier.
- Local contacts may contain only user-authored display name and explicitly approved local notes, without identifier semantics.
- Do not provide FLY ID lookup, resolution, matching, validation, QR scanning, copy/share or trust state.
- Do not generate a plausible-looking production identifier.
- Synthetic identifiers may appear only in a dedicated demo fixture, must be visibly marked “SAMPLE — NOT A REAL FLY ID”, and cannot be copied or resolved.

### Future-only identity flow
A real FLY ID input cannot be designed until the product approves:
- issuance and uniqueness rules;
- canonical syntax and normalization;
- namespace and collision handling;
- proof-of-control / resolution protocol;
- anti-impersonation and key-change behavior;
- privacy/correlation disclosure;
- recovery and account lifecycle.

A syntactically valid string is never proof of identity.

### Exact UI copy for current CONTACT-03
**Title:** Add local contact  
**Description:** This creates a local contact note on this device. It does not find, verify or connect to a FLY SECRET account.  
**Fields:** Display name; optional note  
**Primary action:** Save local contact

### Closure criteria
1. FLY ID input is absent from current CONTACT-03.
2. No production FLY ID appears in onboarding, profile, contacts or diagnostics.
3. Demo fixture identifiers are clearly synthetic and non-interactive.
4. No contact is labelled verified/trusted based on a string.
5. Copy/share/resolve actions are absent.
6. Future identity flow remains separately gated.

**Status:** SPECIFIED; owner approval required.

## UX-SEC-03 — LOCAL ONLY becomes evidence-backed, scoped language

### Before
A LOCAL ONLY badge and general local-storage copy may imply that data never leaves the device.

### After — claim governance
LOCAL ONLY is not a design decoration. It is a runtime claim that may appear only when the exact data class and operation have been verified.

A claim record must specify:
- data class;
- operation;
- device/platform;
- storage location;
- network transmission behavior;
- analytics/telemetry behavior;
- crash reporting behavior;
- OS backup / cloud backup behavior;
- browser profile sync behavior where relevant;
- export/share behavior;
- deletion semantics;
- evidence and reviewer.

### Approved language by evidence state

| Evidence state | Allowed copy |
|---|---|
| Implementation not yet built | “Designed for local storage; behavior not yet verified.” |
| Local persistence verified, backup behavior unknown | “Stored on this device by the app. Backup behavior has not been verified.” |
| Local storage and all declared transfer paths verified | “This item is stored locally and is not transmitted by this feature.” |
| Unknown or conflicting evidence | Do not display LOCAL ONLY |

Never use “all your data stays on your device” unless every data path and platform behavior is in scope and verified.

### Component behavior
- StatusBadge must not render LOCAL ONLY from a static design token.
- It requires an approved capability/evidence state from the actual implementation.
- If the evidence is missing or stale, hide the badge and use neutral explanatory text.
- A badge applies only to the specific item/operation, never to the whole app by implication.

### Closure criteria
1. Data-flow inventory exists for each claim.
2. Backup, telemetry, crash reporting, sync, export and support paths are addressed.
3. Evidence is platform-specific.
4. Badge is conditionally shown only when implementation state supports it.
5. Copy review confirms no app-wide locality overclaim.

**Status:** SPECIFIED; data-flow evidence required before use.

## UX-SEC-04 — Rename device screen and separate record from trust

### Before
SET-03 “Devices” and DeviceRecordListItem may imply trusted/enrolled devices.

### After
**Screen title:** Local app installations  
**Introductory disclosure:** These are local records shown by this app. They do not prove device identity, account access, cryptographic trust or secure session status.

### Permitted row labels
- This app installation
- Local record
- Demo record
- Not connected
- Details unavailable

### Forbidden labels until real lifecycle exists
- Trusted
- Verified
- Paired
- Linked
- Active session
- Secure device
- Revoked
- Protected
- Last secure activity

“Active” may only describe a verified local process/session with a precise meaning; it must not mean account authorization or cryptographic trust.

### DeviceRecordListItem specification
- Show a neutral record icon, not a shield/check.
- Display a human-readable local label and optional local creation date only if real.
- Never expose Device Record ID as a credential.
- No revoke/remove-security action.
- If a local record can be deleted, label action “Remove local record” and explain it does not revoke any remote access.
- No fake device fingerprint or key status.

### Exact screen copy
“These entries describe local app records only. Device identity, trusted-device linking and remote revocation are not available in this build.”

### Closure criteria
1. SET-03 renamed.
2. Disclosure appears before list content.
3. All row states use approved local-record vocabulary.
4. No trust-like iconography or security status.
5. Any local delete action explicitly distinguishes local removal from remote revocation.
6. Screen-reader reading order announces the disclosure before rows.

**Status:** SPECIFIED; owner approval required.

## UX-SEC-05 — Reframe Security Center as capability information

### Before
A Security Center dashboard lists unavailable security capabilities inside a product that may visually suggest operational security.

### After
**Screen title:** Security capabilities  
**Subtitle:** Availability in this build

At the top of the screen, show the full non-crypto disclosure from Section 2. No score, gauge, completion ring, shield status or green check.

### Row format
Each row contains:
- feature name;
- state “Not available in this build”;
- one sentence explaining that it is not implemented;
- no activation toggle;
- no progress bar;
- no implied completion.

Rows:
- Private messaging — Not available in this build.
- End-to-end encryption — Not available in this build.
- Identity verification — Not available in this build.
- Device trust — Not available in this build.
- Account recovery — Not available in this build.
- Secure calls — Not available in this build.

### Exact screen copy
“Security capabilities are not active in this build. This page describes product scope; it is not a security status report.”

### Visual rules
- Neutral text/icon treatment only.
- No lock/check/shield badge that communicates success.
- No “security level”, “protection score”, “risk-free” or “all clear”.
- No switch that toggles a deferred capability.
- No fake progress or scanning animation.

### Closure criteria
1. Screen title and subtitle updated.
2. Top-level warning is visible without scrolling.
3. All rows are informational and consistently unavailable.
4. No positive security score or active status exists.
5. Onboarding and app shell carry the same warning.
6. Copy has been reviewed against actual product scope.

**Status:** SPECIFIED; owner approval required.

---

# 4. Detailed remediation — seven MEDIUM findings

## UX-SEC-06 — Canonical capability state taxonomy

### Before
Available, Disabled, Local-only, Demo, Deferred and Unavailable are inconsistently defined or used.

### After — normative definitions

| State | Definition | Display label | Permitted use |
|---|---|---|---|
| AVAILABLE | Implemented, enabled, tested for the stated scope and approved | Available | Real supported operation only |
| LOCAL_ONLY | Implemented operation with verified scoped local-only data flow | Stored locally | Only for specific evidence-backed operation |
| DEMO | Synthetic fixture or simulated interaction, not a real service operation | Demo | Explicitly marked demo route/data |
| UNAVAILABLE | Not implemented or not supported in this build | Not available in this build | No action affordance |
| DEFERRED | Deliberately excluded pending a named architecture/security gate | Deferred | Informational explanation only |
| DISABLED_BY_USER | Implemented feature explicitly turned off by user | Off | Only where a real user setting exists |

Rules:
- “Information” is content type, not a capability state.
- “Loading” is a transient operation state, not a capability state.
- “Error” is an operation outcome, not a capability state.
- A feature cannot transition from DEFERRED or UNAVAILABLE to AVAILABLE because of a local configuration flag alone.
- A feature cannot be called AVAILABLE without implementation and acceptance evidence.
- UNKNOWN is handled fail-closed: show “Status unavailable”, never infer AVAILABLE.

### Closure criteria
One canonical state glossary is reflected in all screen specs, component specs, navigation, copy and QA. No synonyms create additional states.

## UX-SEC-07 — Demo provenance and assistive announcement

### Before
Persistent demo banner behavior is described but not exhaustively mapped to every route and assistive technology.

### After — route coverage

| Route | Synthetic content possible? | Required provenance |
|---|---|---|
| ONB-03 Quick preview | Yes | Demo disclosure before preview |
| CHAT-01 Conversation list | Yes | Persistent Demo banner |
| CHAT-02 Demo conversation | Yes | Persistent Demo banner |
| CHAT-03 Conversation information | Yes | Demo label attached to record |
| CHAT-04 Search | Yes | Search results labelled Demo |
| CALL-02 Call details concept | Yes | Persistent Demo banner |
| CONTACT-01 Contacts list | Yes | Demo row label and banner if fixture shown |
| CONTACT-02 Contact detail | Yes | Demo label attached to record |
| CONTACT-04 Verification explanation | Example only | “Illustrative example — not a real verification” |
| Settings fixture views | Only if fixtures are present | Same persistent Demo banner |

No other route may silently display synthetic message/contact/device activity.

### Announcement behavior
- On route entry, announce “Demo content. Synthetic data only.” once through the screen reader.
- Keep a visible persistent label in the screen context.
- Do not re-announce on every focus movement.
- If route content changes from real local data to demo data, announce the change.
- Demo and user-created local records must not be mixed in one list without unmistakable grouping and labels.
- Demo send/call/verify controls must be absent or inert with an adjacent explanation.

### Closure criteria
Route inventory is complete; every demo route has visual and accessible provenance; synthetic fixtures cannot be confused with real activity.

## UX-SEC-08 — Network state semantics

### Before
Generic online/offline/unknown state may be shown in AppShell.

### After
No generic network badge is shown in the non-crypto shell unless a real network-dependent operation exists and its signal is defined.

Permitted future labels:
- “Internet connection available” — OS/network reachability signal only.
- “Service unavailable” — named service health result.
- “Not connected to service” — application connection state.
- “Connection status unknown” — no reliable signal.

Forbidden:
- “Secure connection” without exact security property and evidence.
- “Relay connected” when no relay connection is actually established.
- “Anonymous” or “private” based on connectivity.
- “Online” as a proxy for identity verification or E2EE.

Every future network indicator must document source, polling/event model, timestamp/freshness, stale behavior, offline behavior and what the signal does not prove.

### Closure criteria
Either remove the generic network status from current screens, or document the exact source and semantics for each displayed state.

## UX-SEC-09 — FLY ID disclosure and sharing lifecycle

### Before
Copy/share is disabled until identity lifecycle approval, but future disclosure requirements are incomplete.

### After — current build
- No FLY ID copy/share control.
- No QR code containing a production identifier.
- No automatic clipboard write.
- No FLY ID in notifications, URLs, logs, analytics or support exports.

### Future-only sharing flow requirements
Before sharing a real FLY ID, the user must see:
- what the identifier identifies;
- who can see it after sharing;
- whether it can be correlated across contexts;
- whether it can be changed/revoked;
- what the receiving app/site will receive;
- explicit destination selection;
- explicit user confirmation.

Copy action requires a direct gesture and a clear post-action notice. Clipboard content must not include secrets or session keys. Any expiration/clipboard clearing behavior must be described honestly and cannot be promised if the platform does not guarantee it.

### Closure criteria
Current controls remain absent; future flow remains a gated design item with privacy review and an approved identity lifecycle.

## UX-SEC-10 — Accessibility acceptance matrix

### Before
WCAG 2.2 AA and equivalent native outcomes are targets without a complete component × state × platform test plan.

### After — mandatory matrix

| Area | Web | Android | iOS | Required evidence |
|---|---|---|---|---|
| Keyboard | All actions reachable; logical order; visible focus | External keyboard where supported | Hardware keyboard where supported | Manual test record |
| Screen reader | NVDA/VoiceOver supported combinations | TalkBack | VoiceOver | Announcement/reading-order record |
| Dynamic text | 200% zoom/reflow | System font scaling | Dynamic Type | No clipping/overlap |
| Focus | Dialog trap/restore; route focus | Modal focus semantics | Sheet/dialog focus semantics | Interaction test |
| Errors | Programmatic association and announcement | TalkBack error announcement | VoiceOver error announcement | Validation test |
| Status changes | Live region only when useful | Accessible event | Accessible announcement | State-change test |
| Reduced motion | prefers-reduced-motion | Animator scale/reduced motion | Reduce Motion | Motion review |
| Touch | Pointer and keyboard equivalence | Target ≥48dp where practical | Target ≥44pt | Target-size audit |
| Contrast | Full semantic matrix | Native rendered matrix | Native rendered matrix | Recorded ratios |
| Localization | Russian/English; zoom | Russian/English; font scale | Russian/English; Dynamic Type | Long-string test |

### High-risk screens requiring full accessibility test cases
- onboarding disclosure;
- Security capabilities;
- local contacts create/edit;
- local installations;
- Privacy Center destructive actions;
- dialogs and bottom sheets;
- all demo routes;
- all error/unavailable states.

### Acceptance criteria
- Every interactive component has keyboard/focus/reader behavior.
- Every high-risk disclosure is announced and remains discoverable.
- No information relies on color alone.
- No focus trap or focus loss.
- No clipping at 200% Web zoom or platform text scaling.
- Reduced motion is respected.
- Russian and English labels fit without hiding actions.
- Test evidence is recorded by platform and assistive technology.

All checks remain NOT_RUN until actual testing is authorized and performed.

## UX-SEC-11 — Terminology approval governance

### Before
Section 13.1 says “Approved terminology” while product-owner design decisions are pending.

### After
Rename to **“Proposed terminology baseline — pending product-owner approval.”**

Every term has:
- term;
- definition;
- allowed context;
- prohibited synonym/implication;
- owner;
- approval state;
- version/date.

A proposed term cannot be used in marketing or shipped copy as approved. After sign-off, record approver, date and specification version. Changes require review.

### Closure criteria
Canonical UX document title updated; terminology register has explicit proposal/approval state; no unapproved term is described as final.

## UX-SEC-12 — Local reset and deletion semantics

### Before
Reset guidance requires naming exact domains, but the full domain inventory and per-action matrix are missing.

### After — current non-crypto local data domains

The design must distinguish these domains. This list is a design inventory to validate against implementation, not proof that each store exists.

| Action | Scope | Explicitly does not mean |
|---|---|---|
| Reset appearance | Theme, density and visual preferences | Delete contacts or remote account |
| Reset app preferences | Named app settings only | Delete account or revoke devices |
| Delete local contact | Selected local contact record | Revoke a remote identity/session |
| Clear demo data | Synthetic fixtures only | Delete user-created local data |
| Clear local cache | Named cache/temp data | Secure erase or remote deletion |
| Reset onboarding | Local onboarding completion state | Re-register or reset identity |

### Confirmation copy template
**Title:** Delete [exact data domain]?  
**Body:** This removes [specific items] from this app on this device. It does not delete a remote account, revoke access on other devices, or erase backups unless explicitly stated here.  
**Actions:** Cancel / Delete [domain]

Do not use “secure erase”, “permanently erased everywhere” or “account deleted” unless the operation demonstrably does that.

### Additional requirements
- List affected data before confirmation.
- Safe default is Cancel.
- No destructive action on a single accidental tap.
- Provide a clear completion result.
- If deletion fails or is partial, state exactly what remains.
- Do not expose sensitive data in confirmation text or logs.

### Closure criteria
Data inventory maps every UI action to actual stores and retention paths; each action has domain-specific copy and failure handling; no remote revocation/deletion implication.

---

# 5. Detailed remediation — three LOW findings

## UX-SEC-13 — Chats and Calls unavailable affordances

### Before
Four-tab navigation is retained, but active-looking controls may imply live messaging or calling.

### After
- Keep navigation order Chats, Calls, Contacts, Settings.
- Chats and Calls remain navigable as product-scope information/demo surfaces only.
- On entry, display “Messaging is not available in this build” or “Calls are not available in this build”.
- Do not display message composer, send, attach, dial, answer, hang up, call-ready or fake activity badge.
- If a visual concept is shown, mark it Demo and remove any control that could be mistaken for a real operation.

### Closure criteria
Route review confirms no live send/call affordance; all demo concepts carry provenance; keyboard and screen reader discover unavailable state.

## UX-SEC-14 — Disabled action reason pattern

### Before
Disabled controls require nearby or accessible reasons, but exact component layout is not standardized.

### After — shared pattern
1. Do not render a disabled action if it creates false capability expectations; prefer explanatory text.
2. If a disabled control is necessary for layout continuity, place a visible reason immediately adjacent.
3. Programmatically associate reason text with control.
4. Never rely on hover tooltip only.
5. Disabled state must not be indicated only by opacity/color.
6. No disabled action may look like a progress or success state.

Example:
- Control: “Start secure call” (not shown in current build)
- Explanation: “Secure calls are not implemented in this build.”

Prefer the explanatory sentence alone over a greyed-out fake action.

### Closure criteria
Every disabled control has adjacent visible and programmatic reason; reason is discoverable by touch, keyboard and screen reader.

## UX-SEC-15 — Future identifier typography and anti-confusion

### Before
Monospaced rendering is suggested, but homoglyph, truncation, normalization and bidirectional rules are undefined.

### After — future-only specification
Before any real opaque identifier is displayed, define:
- allowed alphabet and canonical normalization;
- rejection/handling of mixed scripts and confusables;
- bidi isolation for RTL contexts;
- full-value view;
- safe copy representation;
- truncation with explicit reveal;
- grouping/spacing that does not alter the canonical value;
- accessible character-by-character reading where necessary;
- comparison and fingerprint rules only if backed by real protocol.

Do not silently normalize, translate, localize or substitute characters. Do not treat visual similarity as equality.

### Current build
No production FLY ID, Device Identity, Transport ID, Session ID or Session Keys are displayed. Synthetic examples must be marked and non-copyable.

### Closure criteria
A future identifier format spec is approved before production identifiers are rendered; test cases cover mixed scripts, homoglyphs, bidi and truncation.

---

# 6. Screen-by-screen replacement requirements

| Screen | Required replacement/update |
|---|---|
| ONB-01 Welcome | Show non-crypto disclosure before any “get started” preview |
| ONB-02 Language | Use approved translated warning copy; accessible language selection |
| ONB-03 Setup/quick preview | Mark all synthetic preview content Demo before display |
| ONB-04 Local profile | No production FLY ID generation/display; profile is local display data only |
| ONB-05 Privacy briefing | Explain only verified data flows; no blanket local-only claim |
| ONB-06 Completion | Completion means onboarding complete, not security configured |
| CHAT-01 List | Demo provenance; no fake unread/delivery/security status |
| CHAT-02 Conversation | No composer/send/read/delivery/E2EE; persistent Demo banner |
| CHAT-03 Information | No verified/trusted identity state |
| CHAT-04 Search | Synthetic-only demo results clearly marked |
| CALL-01 Landing | No call-ready state, dial action or permission prompt |
| CALL-02 Details | Demo label; no transport or media security claim |
| CALL-03 Readiness | Rename to call capability explanation; no “ready” state |
| CONTACT-01 List | Distinguish local contacts from service directory |
| CONTACT-02 Detail | No FLY ID or trust badge |
| CONTACT-03 Add | Fields only Display name and optional local note |
| CONTACT-04 Verification | Explain future concept; no real QR/fingerprint/checkmark |
| SET-01 Settings | Security disclosure visible; clear separation of preferences and unavailable capabilities |
| SET-02 Profile | No production FLY ID/copy/share |
| SET-03 Devices | Rename Local app installations; record-vs-trust disclosure |
| SET-04 Privacy Center | Data-domain inventory and precise reset actions |
| SET-05 Security Center | Rename Security capabilities; top-level non-crypto disclosure |
| SET-06 Notifications | No push permission prompt; no sensitive previews |
| SET-07 Appearance/accessibility | Contrast tokens and accessibility controls documented |
| SET-08 Language | Full warning copy in both supported languages |
| SET-09 About | Do not claim certification, encryption or verified security |
| Global error | No secrets/identifiers in error content; retry only real operation |
| Preference corruption | Name exact affected local setting and safe recovery |
| Offline/unknown | Remove generic status until signal semantics are defined |

---

# 7. Component specification deltas

| Component | Required change |
|---|---|
| AppShell | Persistent non-crypto disclosure; no unsupported generic network state |
| PrimaryNavigation | Unavailable Chats/Calls remain informational, not operational |
| ScreenHeader | Screen title must not imply trust/security not implemented |
| Buttons | Avoid fake send/call/verify/recovery actions; disabled reason pattern |
| Input fields | Remove FLY ID field from current contact form |
| Cards/grouped rows | Neutral treatment; no false security dashboard |
| StatusBadge | Canonical state taxonomy; LOCAL ONLY requires evidence |
| EmptyState | No claim that service is ready or contacts can be reached |
| DisabledFeaturePanel | State “not available in this build”; no activation toggle |
| DemoDataBanner | Route coverage and accessible one-time announcement |
| DeviceRecordListItem | Local record vocabulary only; no trusted/verified state |
| IdentifierDisclosurePanel | No current production identifier; future sharing risk disclosure |
| Dialog/bottom sheet | Domain-specific destructive copy; safe default; focus behavior |
| Toast/inline notice | Never sole channel for critical privacy/security disclosure |

---

# 8. Canonical copy catalogue

The following strings are normative proposals for owner review.

| Context | English | Russian |
|---|---|---|
| App-wide security notice | This build does not provide private messaging or end-to-end encryption. Do not use it to exchange sensitive information. | Эта сборка не поддерживает приватные сообщения и сквозное шифрование. Не используйте её для передачи конфиденциальной информации. |
| Demo provenance | Demo content. Synthetic data only. | Демонстрационный режим. Только искусственные данные. |
| Local contact | This creates a local contact note on this device. It does not find or verify an account. | Это создаёт локальную запись контакта на устройстве. Поиск и проверка аккаунта не выполняются. |
| Device records | These are local app records. They do not prove device identity or trust. | Это локальные записи приложения. Они не подтверждают личность устройства или доверие к нему. |
| Security capabilities | Security capabilities are not active in this build. This page describes product scope, not security status. | Функции безопасности не активны в этой сборке. Эта страница описывает состав продукта, а не состояние защиты. |
| Not implemented | Not available in this build. | Недоступно в этой сборке. |
| Deferred | Deferred pending architecture and security approval. | Отложено до архитектурного и security-согласования. |
| Unknown status | Status unavailable. | Статус недоступен. |
| Locality unverified | Designed for local storage; behavior not yet verified. | Предусмотрено локальное хранение; поведение ещё не проверено. |
| Local removal | Remove local record. This does not revoke remote access. | Удалить локальную запись. Это не отзывает удалённый доступ. |

Translation review must preserve semantics; these strings are not marketing copy and may not be softened into a positive security promise.

---

# 9. Consolidated before / after register and closure criteria

| Finding | Before | After | Minimum closure evidence |
|---|---|---|---|
| UX-SEC-01 | Tertiary 3.88:1 | Light tertiary #626A65 proposed; full matrix | Exact token matrix + rendered audit |
| UX-SEC-02 | Free-form unverified FLY ID | Remove current field; demo-only synthetic fixture | CONTACT-03 spec and implementation review |
| UX-SEC-03 | Generic LOCAL ONLY | Per-operation evidence-backed claim | Data-flow inventory and platform evidence |
| UX-SEC-04 | “Devices” with ambiguous records | “Local app installations”; no trust labels | Screen/copy review and reader order |
| UX-SEC-05 | Security Center dashboard | Security capabilities + persistent warning | All routes show disclosure; no positive status |
| UX-SEC-06 | Mixed state terms | Canonical six-state taxonomy | Glossary consistency audit |
| UX-SEC-07 | Partial demo banner rules | Complete route matrix and accessible announcement | Route coverage and assistive test evidence |
| UX-SEC-08 | Generic online/offline | Remove or define named signal | Signal source/freshness semantics |
| UX-SEC-09 | Copy/share blocked but future flow vague | No current controls; future risk disclosure spec | UI absence now; future design gate |
| UX-SEC-10 | Broad accessibility target | Component × state × platform matrix | Recorded platform and AT results |
| UX-SEC-11 | “Approved terminology” | “Proposed terminology baseline” | Updated canonical heading and approval register |
| UX-SEC-12 | Generic reset guidance | Data-domain-specific deletion model | Data inventory, confirmations and failure cases |
| UX-SEC-13 | Active-looking Chats/Calls | Explicit unavailable/demo-only surfaces | No operational affordances in route review |
| UX-SEC-14 | Inconsistent disabled reasons | Shared adjacent/programmatic reason pattern | Component tests and reader checks |
| UX-SEC-15 | Basic monospaced guidance | Future format anti-confusion rules | Approved identifier format and test vectors |

A finding moves through:
- SPECIFIED — proposed correction documented;
- OWNER_APPROVED — product owner accepted wording/design;
- IMPLEMENTED — authorized implementation exists;
- VERIFIED — required test evidence passed;
- CLOSED — reviewer confirms all closure criteria.

No finding may skip stages. SPECIFIED is the only state reached by this phase.

---

# 10. Remediation release gates

| Gate | Closure requirement | Phase 1.4.1 result |
|---|---|---|
| RMG-01 | UX-SEC-01 contrast token and complete matrix | SPECIFIED |
| RMG-02 | UX-SEC-02 remove FLY ID field | SPECIFIED |
| RMG-03 | UX-SEC-03 data-flow proof for LOCAL ONLY | BLOCKED — evidence absent |
| RMG-04 | UX-SEC-04 local record terminology | SPECIFIED |
| RMG-05 | UX-SEC-05 non-crypto disclosure and Security capabilities | SPECIFIED |
| RMG-06 | UX-SEC-06 canonical state taxonomy | SPECIFIED |
| RMG-07 | UX-SEC-07 demo route and announcement matrix | SPECIFIED |
| RMG-08 | UX-SEC-08 network signal semantics | SPECIFIED — current generic status to be removed |
| RMG-09 | UX-SEC-09 no current FLY ID sharing | SPECIFIED |
| RMG-10 | UX-SEC-10 accessibility test plan | SPECIFIED — all execution NOT_RUN |
| RMG-11 | UX-SEC-11 terminology governance | SPECIFIED |
| RMG-12 | UX-SEC-12 data deletion inventory | BLOCKED — implementation inventory absent |
| RMG-13 | UX-SEC-13 Chats/Calls unavailable behavior | SPECIFIED |
| RMG-14 | UX-SEC-14 disabled action pattern | SPECIFIED |
| RMG-15 | UX-SEC-15 future identifier format | FUTURE GATE |

These are documentation readiness states, not security test passes.

---

# 11. Owner approval register

The remediation proposals require explicit product-owner review. They are not silently approved by this commit.

| Decision | Proposal | State |
|---|---|---|
| RA-01 | Light tertiary text #626A65 | PENDING |
| RA-02 | Remove FLY ID from current contact form | PENDING |
| RA-03 | Evidence-backed LOCAL ONLY policy | PENDING |
| RA-04 | Rename Devices to Local app installations | PENDING |
| RA-05 | Reframe Security Center as Security capabilities | PENDING |
| RA-06 | Persistent non-crypto disclosure | PENDING |
| RA-07 | Canonical six-state taxonomy | PENDING |
| RA-08 | Demo route/announcement policy | PENDING |
| RA-09 | Remove generic network status without signal | PENDING |
| RA-10 | Future FLY ID share-risk requirements | PENDING |
| RA-11 | Component/state/platform accessibility matrix | PENDING |
| RA-12 | Rename approved terminology section | PENDING |
| RA-13 | Per-domain local deletion model | PENDING |
| RA-14 | Chats/Calls informational-only surfaces | PENDING |
| RA-15 | Shared disabled-action pattern | PENDING |
| RA-16 | Future identifier anti-confusion requirements | PENDING |

Approval must be recorded with decision, approver, date, rationale and version. This document does not itself constitute owner sign-off.

---

# 12. Security architecture and non-negotiable constraints

- No E2EE, private messaging, secure calls, verified identity, device trust or recovery claims until implemented and independently reviewed.
- No custom cryptography.
- No plaintext, private keys, ratchet secrets, Recovery Kit secrets or media decryption keys on backend.
- FLY ID, Device Record ID, cryptographic Device Identity, Transport ID, Session ID and Session Keys remain distinct.
- FLY ID is not a password, private key or bearer token.
- Device Record ID is not cryptographic identity or trust proof.
- Transport ID is not stable identity or anonymity proof.
- Session Keys are not UI IDs or API tokens.
- No secrets in clipboard, logs, URL, localStorage/sessionStorage or push payloads.
- No automatic clipboard writes.
- Permission requests only at the moment a separately approved feature needs them.
- Push is opaque wake-up only, if introduced in a future authorized scope.
- Web security posture remains distinct from native hardware-backed assumptions.
- FLAG_SECURE and anti-capture mechanisms are not universal guarantees.
- TOR MODE remains excluded from product, architecture, UI, configuration, tests and roadmap.
- Decentralization remains future-only.

---

# 13. Red-Team and QA status

The Red-Team test catalog is not changed by this phase.

All 27 unique scenario IDs remain **NOT_RUN**:
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

No scenario has been executed, passed, failed or waived in this phase.

Accessibility matrix is specified but all runtime/platform checks remain NOT_RUN. Color ratios in this document are calculations on declared hex tokens, not rendered UI tests.

---

# 14. Execution record

## Performed
- Retrieved Phase 1.4 review and Phase 1.3 UX baseline.
- Retrieved the current Red-Team catalog.
- Confirmed 27 unique Red-Team IDs and 32 NOT_RUN mentions before work.
- Created a dedicated remediation branch from Phase 1.4.
- Authored exact remediation proposals for all 15 findings.
- Added before/after register, screen deltas, component deltas, copy catalogue, contrast proposal, accessibility matrix, closure criteria and owner approval register.

## Not performed
- No code or app changes.
- No prototype, Figma or generated UI.
- No spike, build, dependency integration or runtime.
- No browser/Playwright, Android, iOS, Web or Desktop testing.
- No accessibility execution.
- No data-flow inspection of a running implementation.
- No penetration test or Red-Team test.
- No implementation of cryptography, messaging, calls, files, push or recovery.
- No change to D-01.
- No modification of `main`.
- No Pull Request.

---

# 15. Git verification checklist

Before declaring document delivery complete:
- fetch this file back from the new branch and record its blob SHA;
- compare the new branch with Phase 1.4 and confirm one new file, zero deletions;
- compare with `main` and confirm all changes are additions only;
- fetch Red-Team catalog from this branch and Phase 1.4 branch and confirm identical blob SHA;
- confirm 27 unique IDs and all scenarios remain NOT_RUN;
- do not create a PR.

---

# 16. Formal Phase 1.4.1 disposition

| Item | Result |
|---|---|
| Phase 1.4 baseline reviewed | COMPLETE |
| 5 High remediation specs | COMPLETE |
| 7 Medium remediation specs | COMPLETE |
| 3 Low remediation specs | COMPLETE |
| Before/after table | COMPLETE |
| Screen specifications | COMPLETE |
| Component specifications | COMPLETE |
| Status taxonomy | COMPLETE |
| Contrast proposal | COMPLETE — full matrix pending |
| Identity and privacy copy | COMPLETE |
| Accessibility acceptance matrix | COMPLETE — execution NOT_RUN |
| Closure criteria | COMPLETE |
| Owner approvals | 16 PENDING |
| Implementation | NOT AUTHORIZED |
| Runtime verification | NOT RUN |
| Red-Team | 27 / 27 NOT_RUN |
| D-01 | OPEN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: PHASE 1.4.1 REMEDIATION SPECIFICATION COMPLETE. All 15 findings have exact proposed corrections and closure criteria. No finding is represented as implemented, verified or closed. Product-owner approval and later authorized implementation/testing remain separate gates.**
