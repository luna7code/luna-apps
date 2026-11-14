# FLY SECRET — Phase 1.4
## UX Security Review & Design Approval

**Status:** DOCUMENTARY REVIEW COMPLETE — DESIGN APPROVAL PENDING  
**Baseline:** Phase 1.3 — UX Architecture & Design System Specification  
**Product architecture baseline:** Phase 1.2 — Non-Crypto MVP Product Architecture  
**Working branch:** `security/fly-secret-ux-security-review-design-approval-1.4`  
**Repository:** `luna7code/luna-apps`  
**Date:** 2026-11-14

> Scope: static/documentary review of UX specifications. This document does not claim runtime behavior, visual implementation, accessibility conformance, penetration testing or security certification. No code, prototype, spike, build, test execution or PR was authorized or performed.

---

# 1. Executive decision

## 1.1 Review result

**Decision: CONDITIONAL DESIGN NO-GO for implementation approval.**

Phase 1.3 establishes a strong baseline: it prohibits false E2EE claims, distinguishes identity and transport concepts, minimizes permissions, defines demo/unavailable states, and specifies Android/iOS/Web accessibility expectations.

However, the review identified defects and unresolved design controls that should be closed before the visual system is approved for implementation.

- **Critical:** 0 confirmed documentary defects.
- **High:** 5 findings.
- **Medium:** 7 findings.
- **Low:** 3 findings.
- **Total:** 15 findings.

Severity here measures the potential user/privacy/security impact if the specification were implemented as written. It is not a statement that a production vulnerability was observed.

## 1.2 What was reviewed

- All onboarding screens ONB-01..06.
- Chats CHAT-01..04.
- Calls CALL-01..03.
- Contacts CONTACT-01..04.
- Settings SET-01..09.
- AppShell, PrimaryNavigation, ScreenHeader, buttons, inputs, cards, StatusBadge, EmptyState, DisabledFeaturePanel, DemoDataBanner, DeviceRecordListItem, IdentifierDisclosurePanel, dialogs, sheets, toasts and notices.
- Feature availability and navigation states.
- Identity/transport terminology and user-facing copy.
- Permission, clipboard, local storage, notifications, error and screen-capture rules.
- Android, iOS, Web and deferred Desktop adaptation.
- Color, typography, responsive layout, motion and accessibility targets.

## 1.3 Review limitations

The review is limited to written artifacts. It does not prove:
- actual color rendering or contrast in a built app;
- actual screen reader announcements;
- correct focus trapping or restoration;
- platform permission behavior;
- storage isolation or deletion;
- network behavior;
- absence of analytics or telemetry;
- implementation conformance to the specification.

No live UI, browser, native device, source code or runtime was tested.

---

# 2. Severity model

| Severity | Meaning | Required disposition |
|---|---|---|
| Critical | Could directly enable severe compromise, irreversible sensitive disclosure or a fundamentally false security guarantee | Block release/design approval; immediate owner/security review |
| High | Material privacy/security deception, identity confusion, or failure of a core accessibility requirement | Must be resolved before implementation approval |
| Medium | Meaningful ambiguity, privacy exposure, inconsistent state or accessibility risk | Resolve before affected screen implementation; documented exception requires owner |
| Low | Clarity, consistency or maintainability issue with limited direct security impact | Resolve in design handoff or track explicitly |

Finding IDs are stable for later review. These are UX/documentation findings, not the 27 Red-Team scenario IDs.

---

# 3. Findings register

## 3.1 High severity

### UX-SEC-01 — Insufficient contrast for light-theme tertiary text

**Severity:** HIGH  
**Affected:** color tokens, metadata, captions, helper text, disabled explanations  
**Evidence:** Phase 1.3 light theme defines `color.text.tertiary = #777E7A` on `color.canvas = #F7F7F5`.

**Finding:** Calculated contrast is approximately **3.88:1**. This does not meet the 4.5:1 WCAG AA threshold for normal-size text. The Phase 1.3 document already cautions against using this token for essential content until verified, but the token remains available to components and metadata.

**Risk:** Users with low vision may miss device state, local-only explanations, error context or privacy disclosures if a component uses the tertiary token for meaningful text.

**Required correction:**
- Replace the token with a darker neutral that reaches at least 4.5:1 against every approved light surface on which it is used.
- Alternatively, reserve it strictly for non-text decoration and prohibit its use for text through token/component governance.
- Test all semantic text/surface combinations, not only canvas.
- Document permitted foreground/background pairings.

**Acceptance criteria:**
- Every normal text pairing reaches at least 4.5:1.
- Large text reaches at least 3:1.
- Essential non-text component boundaries and focus indicators meet applicable 3:1 requirements.
- No essential copy uses a token marked “contrast not verified”.

**Status:** OPEN — owner/design correction required.

### UX-SEC-02 — Free-form unverified FLY ID entry can create identity confusion

**Severity:** HIGH  
**Affected:** CONTACT-03 Add local contact; CONTACT-02 Contact detail; IdentifierDisclosurePanel; identity terminology  
**Evidence:** Phase 1.3 allows an “optional unverified FLY ID note” in a local contact record.

**Finding:** A user-entered string that resembles a FLY ID may be interpreted as a real, valid or resolved identity even though the non-crypto shell does not issue, resolve or verify FLY IDs. Calling the field “FLY ID” can lend authority to arbitrary input.

**Risk:** Impersonation, mistaken contact association, user-generated identifier collision, social engineering and accidental sharing of a string believed to be verified.

**Required correction:**
- Remove the FLY ID input from the current Non-Crypto MVP contact creation flow.
- If a research/demo fixture needs it, label it “Unverified sample identifier (demo only)” and accept only synthetic fixtures.
- Do not permit lookup, resolution, contact matching, copy/share or trust actions.
- Future real FLY ID entry requires an approved issuance/format/namespace and verification lifecycle.

**Acceptance criteria:**
- Production/local contact forms cannot create a field that looks like an issued FLY ID.
- Demo identifiers are synthetic, visibly marked and never resolved.
- No Verified/Trusted state can be inferred from presence or syntax.

**Status:** OPEN — remove or constrain field.

### UX-SEC-03 — “LOCAL ONLY” can overstate data locality without per-operation proof

**Severity:** HIGH  
**Affected:** StatusBadge; local profile; contacts; preferences; demo data; privacy disclosures  
**Evidence:** Phase 1.3 permits a “LOCAL ONLY” badge and copy such as “Your local display preferences are stored on this device.”

**Finding:** “Local-only” is a strong data-flow assertion. The Phase 1.3 artifact is a design specification, not evidence that a particular future implementation has no telemetry, sync, backup, crash upload, browser extension access or other remote processing.

**Risk:** Users may disclose data under an incorrect belief that it never leaves the device.

**Required correction:**
- Treat LOCAL ONLY as a runtime capability claim, not a decorative status token.
- Bind it to an approved data inventory and verified implementation behavior for that specific data class and operation.
- Before implementation, define whether OS backup, browser profile sync, crash reporting, analytics, logs or support exports are in scope.
- Where behavior is not verified, use “Designed for local storage; implementation not yet verified” or omit the claim.

**Acceptance criteria:**
- Every LOCAL ONLY claim maps to a documented data-flow statement and testable implementation requirement.
- The badge is not shown in an unimplemented prototype or shell as a factual guarantee.
- No blanket “all data stays on this device” statement.

**Status:** OPEN — terminology and runtime dependency must be tightened.

### UX-SEC-04 — Device inventory wording risks conflating a local record with device trust

**Severity:** HIGH  
**Affected:** SET-03 Devices; DeviceRecordListItem; Security Center  
**Evidence:** The screen is titled “Devices”, rows show a local/demo record state, and the terminology distinguishes Device Record ID from cryptographic device identity.

**Finding:** Even with a textual disclaimer, a screen titled “Devices” inside a security-oriented messenger can imply enrolled, trusted or cryptographically bound devices. A “record state” can be mistaken for session authorization or revocation status.

**Risk:** Users may believe an installation is trusted, linked, revoked or safe based only on a local inventory row.

**Required correction:**
- Rename the current screen to “Local app installations” or “This device and local records”.
- Add a persistent explanatory sentence: “These are local records only. They do not prove device identity, trust or access to an account.”
- Use only states such as “Local record”, “Demo record” and “Not connected”.
- Do not show “active”, “trusted”, “linked”, “revoked” or “last secure activity” until a real device lifecycle exists.

**Acceptance criteria:**
- A user can distinguish local inventory from cryptographic device identity without opening help.
- No device row visually resembles a trusted-device management control.
- Future revocation is not represented as deleting a local record.

**Status:** OPEN — screen naming and status vocabulary revision required.

### UX-SEC-05 — Security Center grouping may imply implemented protections

**Severity:** HIGH  
**Affected:** SET-05 Security Center; navigation labels; empty/unavailable states  
**Evidence:** Security Center lists Identity verification, Encrypted messaging, Device trust and Recovery as unavailable, while the product brand itself is a secure messenger.

**Finding:** A dedicated Security Center and list of security capabilities can imply the app has an operational security subsystem even if each row says unavailable. A polished security dashboard can produce trust transfer from visual design to nonexistent controls.

**Risk:** Users overestimate protection, assume the app is safer than it is, or interpret the screen as a checklist of protections already active.

**Required correction:**
- Use an explicit title such as “Security capabilities” or “Security roadmap” for the non-crypto shell.
- Place a clear top-level banner: “Security features are not implemented in this build. Do not use this build for private communications.”
- Do not display a score, shield meter, completion ring or green status.
- Keep all feature rows informational, with no misleading toggles.

**Acceptance criteria:**
- The first viewport communicates the absence of real secure messaging.
- No security posture score or positive security status is shown.
- No control implies activation of encryption, identity verification or recovery.

**Status:** OPEN — global disclosure and screen hierarchy required.

## 3.2 Medium severity

### UX-SEC-06 — Status vocabulary is inconsistent across principles and components

**Severity:** MEDIUM  
**Affected:** StatusBadge; feature state transitions; all screens  
**Evidence:** Phase 1.3 principles list Available, Local-only, Demo, Disabled and Deferred; StatusBadge enumerates LOCAL ONLY, DEMO, UNAVAILABLE, DEFERRED and INFORMATION.

**Finding:** “Available” and “Disabled” are not represented consistently. “Unavailable” and “Deferred” may be used interchangeably despite different meanings. “Local-only” is both a capability claim and a display label.

**Risk:** Product teams may create inconsistent labels or treat a security-gated feature as merely disabled by configuration.

**Required correction:**
Adopt one canonical state taxonomy:
- **Available:** implemented and approved for the stated scope.
- **Local-only:** implemented, with verified local-only data flow.
- **Demo:** synthetic fixture only.
- **Unavailable:** not implemented in this build.
- **Deferred:** intentionally excluded pending a named gate/decision.
- **Disabled by user:** an implemented feature explicitly turned off by the user.

Keep “Information” as a content type, not a capability state.

**Acceptance criteria:** One canonical enum/term list is used across navigation, badges, screen specs, copy and QA.

**Status:** OPEN.

### UX-SEC-07 — Demo banner coverage and screen-reader persistence are underspecified

**Severity:** MEDIUM  
**Affected:** DemoDataBanner; CHAT-01..04; CALL-02; CONTACT screens; Settings fixtures  
**Evidence:** Banner is persistent on demo screens, but the specification explicitly calls out non-dismissability only on routes containing synthetic conversation/contact data.

**Finding:** The scope of “demo screen” is not enumerated across all routes. Screen-reader announcement timing and repeated announcement behavior are not defined.

**Risk:** Synthetic records may be mistaken for real contacts/history, or assistive technology users may not receive the demo warning.

**Required correction:**
- Enumerate every route that can display synthetic data.
- Show a persistent visible DEMO label on each such route.
- Announce demo status once on route entry and expose it in screen context without repeated noisy announcements.
- Prevent demo fixtures from being mixed with user-created local records.

**Acceptance criteria:** Route-by-route demo banner matrix exists; visual and assistive state are equivalent.

**Status:** OPEN.

### UX-SEC-08 — Offline/unknown status and AppShell network state lack source semantics

**Severity:** MEDIUM  
**Affected:** AppShell; global notice; offline state  
**Evidence:** AppShell lists offline/unknown as a state; global offline screen is allowed if a real connectivity signal exists.

**Finding:** The design does not distinguish OS network connectivity, internet reachability, service availability and application session connectivity. In a non-crypto shell with no messaging backend, a generic “offline” state could imply the app attempted or maintains a service connection.

**Risk:** Users infer that an online state proves relay availability, identity resolution or privacy.

**Required correction:**
- Do not show network state unless a defined, real network-dependent operation exists.
- If shown in a future build, label the exact signal: “Internet connection”, “Service unavailable” or “Not connected to service”.
- Explicitly state that connectivity does not establish identity or E2EE.

**Acceptance criteria:** Every network status has a named source, meaning, freshness and failure behavior.

**Status:** OPEN.

### UX-SEC-09 — FLY ID copy/share warning is not connected to a future disclosure flow

**Severity:** MEDIUM  
**Affected:** SET-02 Profile; IdentifierDisclosurePanel; clipboard/share  
**Evidence:** Copy/share is disabled until identity lifecycle approval, but no complete confirmation, destination, expiration or post-share state is specified.

**Finding:** The document correctly blocks copy/share now, but does not define the complete future disclosure UX. A simple warning may be insufficient if the identifier can enable correlation.

**Risk:** Users share a stable identifier broadly and unintentionally link activity across contexts.

**Required correction:**
- Keep controls absent in the current non-crypto shell.
- Future flow must explain scope, correlation risk, destination and reversibility limitations before sharing.
- Use explicit user gesture; never auto-copy.
- Do not include identifier in analytics, crash logs or notification payloads.

**Acceptance criteria:** Sharing requires explicit confirmation and has a reviewed data-flow/retention statement.

**Status:** OPEN — future gate; no current feature.

### UX-SEC-10 — Accessibility acceptance criteria are targets, not a complete component-level audit plan

**Severity:** MEDIUM  
**Affected:** All components and screens; WCAG 2.2 AA target  
**Evidence:** Phase 1.3 specifies WCAG 2.2 AA, screen reader, keyboard, 200% zoom and contrast targets, while its acceptance matrix leaves all checks NOT_RUN.

**Finding:** The target is sound, but acceptance criteria do not yet define exact test coverage for every component state, modal sequence, validation error, responsive breakpoint and localization overflow.

**Risk:** Core privacy disclosures or destructive confirmation may become inaccessible even if static color and font targets pass.

**Required correction:**
- Create a component × state × platform accessibility matrix.
- Include keyboard-only, TalkBack, VoiceOver, zoom/reflow, dynamic type, focus restoration, error announcement and reduced motion.
- Include Russian/English long strings and pluralization.
- Test at the documented mobile and desktop breakpoints.

**Acceptance criteria:** Every interactive component and high-risk screen has an accessibility test case and explicit pass/fail criteria.

**Status:** OPEN — QA plan required before implementation QA.

### UX-SEC-11 — “Approved terminology” heading overstates approval status

**Severity:** MEDIUM  
**Affected:** Content design and terminology  
**Evidence:** Section 13.1 is titled “Approved terminology”, while Section 16 lists product-owner review decisions still pending.

**Finding:** Editorial status is internally inconsistent: terms are presented as approved although the design direction and vocabulary await owner approval.

**Risk:** Engineering and marketing may treat proposed wording as final.

**Required correction:** Rename section to “Proposed terminology baseline” until signed off; record owner, date and version after approval.

**Acceptance criteria:** No proposed design decision is described as approved before explicit sign-off.

**Status:** OPEN.

### UX-SEC-12 — Local reset scope and irreversibility are not sufficiently explicit per data domain

**Severity:** MEDIUM  
**Affected:** SET-04 Privacy Center; local preference corruption; destructive dialog  
**Evidence:** Reset must name exact domains and state what remains, but the screen-level data inventory and reset matrix are not included in Phase 1.3.

**Finding:** “Local reset” can be interpreted as app reset, profile deletion, identity deletion or remote account deletion. The non-crypto shell must not imply that deleting a local record revokes a future cryptographic device.

**Risk:** Accidental data loss or false belief that remote identity/session access has been revoked.

**Required correction:**
- List each deletable local data namespace and its contents.
- Distinguish reset preferences, delete local contacts, clear demo fixtures and clear app-local cache.
- State explicitly what is not affected (remote account, other devices, future sessions).
- No “secure erase” claim.

**Acceptance criteria:** Each destructive action has a data-domain-specific confirmation and post-action result.

**Status:** OPEN.

## 3.3 Low severity

### UX-SEC-13 — “Calls” and “Chats” navigation may look operational despite disabled capability

**Severity:** LOW  
**Affected:** PrimaryNavigation; CHAT-01; CALL-01  
**Evidence:** Four-tab order is retained, while messaging and calling are unavailable.

**Finding:** Retaining the product's intended navigation is acceptable, but active-looking icons and selected tabs may suggest working functionality.

**Required correction:** On unavailable routes, show an explicit “Not available in this build” state immediately. Do not show fake activity badges, call buttons or message composers.

**Acceptance criteria:** No actionable control implies live send/call capability.

**Status:** OPEN.

### UX-SEC-14 — Disabled action reasons are not consistently placed in the screen layout

**Severity:** LOW  
**Affected:** Buttons, disabled feature panels, forms  
**Evidence:** The component spec requires a nearby or accessible reason, but screen layouts do not specify exact placement for all disabled states.

**Finding:** Reason text may be omitted or separated from the control.

**Required correction:** Define a shared disabled-action pattern: visible reason adjacent to the control, programmatic description and no hover-only explanation.

**Acceptance criteria:** Keyboard and screen-reader users can discover why an action is unavailable.

**Status:** OPEN.

### UX-SEC-15 — Localization and identifier typography need a stronger anti-confusion rule

**Severity:** LOW  
**Affected:** FLY ID, Device Record ID, Transport ID; Russian/English typography  
**Evidence:** Monospaced display is suggested “when it improves character distinction”, but no homoglyph policy is defined.

**Finding:** If opaque identifiers are later displayed, Latin/Cyrillic homoglyphs, truncation and line wrapping may cause confusion.

**Required correction:** Future identifier format must define an alphabet, normalization, bidi isolation, copy-safe representation and full-value disclosure. Never visually normalize or truncate without a reveal mechanism.

**Acceptance criteria:** Identifier rendering is specified with format-level rules before any real identifier appears.

**Status:** OPEN — future identity design gate.

---

# 4. Screen-by-screen audit matrix

| Screen | Review outcome | Main finding(s) | Required gate |
|---|---|---|---|
| ONB-01 Welcome | Conditional | Must not imply service or security readiness | UX-SEC-05 |
| ONB-02 Language | Pass with QA pending | Long labels and selected semantics | UX-SEC-10 |
| ONB-03 Setup / quick preview | Conditional | Preview must not be mistaken for account creation | UX-SEC-07 |
| ONB-04 Local profile | Conditional | Do not generate/display production FLY ID | UX-SEC-02, 09 |
| ONB-05 Privacy briefing | Conditional | Locality wording must be evidence-based | UX-SEC-03 |
| ONB-06 Completion | Conditional | No “identity secured” or setup-as-security claim | UX-SEC-05 |
| CHAT-01 List | Conditional | Demo provenance and no fake unread state | UX-SEC-07, 13 |
| CHAT-02 Demo conversation | Conditional | Persistent DEMO; no send/delivery/read/E2EE implication | UX-SEC-07, 13 |
| CHAT-03 Information | Conditional | No verification or trust implication | UX-SEC-02 |
| CHAT-04 Search | Conditional | Search synthetic fixtures only | UX-SEC-07 |
| CALL-01 Landing | Conditional | No call-ready affordance or permission request | UX-SEC-13 |
| CALL-02 Demo detail | Conditional | Clearly synthetic; no transport-security claims | UX-SEC-07, 08 |
| CALL-03 Readiness | Conditional | No “ready” status; distinguish transport from E2EE | UX-SEC-05 |
| CONTACT-01 List | Conditional | Local data claim requires verified locality | UX-SEC-03 |
| CONTACT-02 Detail | Conditional | No real FLY ID or verified state | UX-SEC-02 |
| CONTACT-03 Add | **NO-GO** | Remove free-form FLY ID field | UX-SEC-02 |
| CONTACT-04 Verification | Conditional | Explanation only; no fake fingerprint/QR | UX-SEC-05 |
| SET-01 Settings | Conditional | Grouping must not imply operational security controls | UX-SEC-05 |
| SET-02 Profile | Conditional | FLY ID issuance/share remains unavailable | UX-SEC-02, 09 |
| SET-03 Devices | **NO-GO** | Rename and clarify local records vs trust | UX-SEC-04 |
| SET-04 Privacy Center | **NO-GO** | Need precise reset/data inventory and locality evidence | UX-SEC-03, 12 |
| SET-05 Security Center | **NO-GO** | Needs prominent non-crypto build disclosure | UX-SEC-05 |
| SET-06 Notifications | Conditional | No push prompt or previews; future data-flow review | UX-SEC-09 |
| SET-07 Appearance/accessibility | Conditional | Component-level accessibility test matrix | UX-SEC-01, 10 |
| SET-08 Language | Pass with QA pending | Localization overflow and screen-reader review | UX-SEC-10 |
| SET-09 About | Conditional | Version/support/legal content only when verified | UX-SEC-11 |
| Global error | Conditional | No sensitive values; retry only real operations | UX-SEC-10 |
| Local preference corruption | Conditional | Exact affected namespace and reset scope | UX-SEC-12 |
| Offline state | **NO-GO until defined** | Connectivity source and semantics missing | UX-SEC-08 |

“Pass with QA pending” means no material documentary contradiction was identified in this static review; it does not mean the screen is implemented or tested.

---

# 5. Component-level audit matrix

| Component | Review | Main correction |
|---|---|---|
| AppShell | Conditional | Remove generic network state unless sourced and defined |
| PrimaryNavigation | Conditional | Make unavailable tabs unmistakably informational |
| ScreenHeader | Pass with QA pending | Ensure labels/actions have semantics |
| Buttons | Conditional | Adjacent disabled reason and no fake loading |
| Inputs | Conditional | No FLY ID field in current contact form |
| Cards/grouped rows | Pass with QA pending | Avoid security-dashboard visual theater |
| StatusBadge | **NO-GO** | Canonical status taxonomy and evidence-backed LOCAL ONLY |
| EmptyState | Pass with QA pending | No false invitations or availability claims |
| DisabledFeaturePanel | Conditional | Explain current-build limitation and no hidden activation |
| DemoDataBanner | **NO-GO** | Enumerate route coverage and assistive announcement |
| DeviceRecordListItem | **NO-GO** | No trust-like states; local record terminology |
| IdentifierDisclosurePanel | Conditional | Keep FLY ID copy/share absent until lifecycle approval |
| Dialog/bottom sheet | Conditional | Safe default, focus management and exact reset scope |
| Toast/inline notice | Conditional | Never use toast for critical security warning |

---

# 6. Cross-cutting security and privacy review

## 6.1 E2EE and security claims

**Documentary result: STRONG BASELINE, IMPLEMENTATION NOT VERIFIED.**

Phase 1.3 explicitly prohibits:
- E2EE lock icon;
- encrypted message/call status;
- verified contact checkmark;
- security score;
- simulated key exchange or encryption animation;
- “100% anonymous”, “unhackable”, “military-grade” and “fully secure” claims.

Additional requirement:
- Put a non-dismissible, plain-language disclosure at onboarding and in the app shell: “This build does not provide private messaging or end-to-end encryption. Do not use it to exchange sensitive information.”
- Marketing/site copy must not imply the non-crypto shell is a secure messenger ready for sensitive use.
- The disclaimer must remain visible in all demo modes.

## 6.2 Identity separation

Required model remains:

| Entity | Allowed UX representation | Forbidden implication |
|---|---|---|
| FLY ID | Future opaque user-facing identifier | Password, secret, bearer token, public key, proof of identity |
| Device Record ID | Local/administrative record reference | Cryptographic device identity or trust |
| Cryptographic device identity | Future protocol-bound entity | Device label, serial or inventory row |
| Transport ID | Scoped routing identifier | Stable identity, anonymity proof |
| Session ID | Future protocol session reference | Session key or user identity |
| Session Keys | Future secret material | UI identifier, API token, preference |

No current UI should generate, display, copy, resolve or verify a production FLY ID, Device Identity, Transport ID, Session ID or Session Keys.

## 6.3 Permission abuse

**Documentary result: GOOD RESTRICTION; runtime NOT_TESTED.**

No permission prompts should appear in this phase for:
- Contacts;
- Microphone;
- Camera;
- Photo library;
- Notifications;
- Nearby devices;
- Location.

Future permission UX must include purpose, timing, least privilege, denial behavior and settings recovery. No unrelated local workflow may be blocked by denial.

## 6.4 Privacy leakage

Controls to preserve and strengthen:
- no secrets in localStorage/sessionStorage/URL;
- no identifiers or message content in logs, errors or push payloads;
- no automatic clipboard writes;
- no stable transport IDs in user-visible diagnostics;
- no permission prompts at onboarding;
- no sensitive content in app-switcher snapshots in future sensitive screens;
- no blanket local-only or anonymity claims without a verified data-flow.

Additional requirement: define crash-reporting, analytics, telemetry, OS backup and browser profile sync explicitly before any privacy claim is approved.

## 6.5 Accessibility

Color calculations performed against the Phase 1.3 token values:

| Pair | Calculated ratio | WCAG text implication |
|---|---:|---|
| Light tertiary #777E7A / canvas #F7F7F5 | **3.88:1** | Fails normal-text AA |
| Light secondary #5E6461 / canvas #F7F7F5 | 5.64:1 | Meets normal-text AA |
| Light accent #176B63 / white | 6.33:1 | Meets normal-text AA |
| Light warning #8A5B13 / #FFF4DE | 5.37:1 | Meets normal-text AA |
| Light danger #B3261E / #FCEBE9 | 5.66:1 | Meets normal-text AA |
| Dark tertiary #929B95 / #191C1A | 6.01:1 | Meets normal-text AA |
| Dark secondary #B7BFBA / #191C1A | 9.14:1 | Meets normal-text AA |
| Dark accent #79BDB2 / #191C1A | 7.96:1 | Meets normal-text AA |
| Dark warning #E9B85F / #382B18 | 7.52:1 | Meets normal-text AA |
| Dark danger #F28B82 / #3A2220 | 6.15:1 | Meets normal-text AA |

These are token-pair calculations, not a complete contrast audit. Focus rings, borders, selected/unselected states, disabled controls, overlays, surfaces and component boundaries still require a full matrix. Disabled content is not exempt from the need to remain understandable.

---

# 7. Required design corrections before approval

The following order is recommended:

1. **P0 — Security disclosure:** add persistent non-crypto/no-private-messaging disclosure to onboarding and app shell.
2. **P0 — Identity:** remove free-form FLY ID entry from CONTACT-03; prohibit production ID generation/display in this phase.
3. **P0 — Device semantics:** rename SET-03 and remove any implication of device trust, pairing or revocation.
4. **P0 — Security Center:** rename or reframe SET-05 and remove score/positive security signals.
5. **P0 — Locality:** constrain LOCAL ONLY to evidence-backed, per-data-class claims.
6. **P1 — Contrast:** revise light tertiary token and complete semantic contrast matrix.
7. **P1 — State taxonomy:** publish one canonical availability/status model.
8. **P1 — Demo provenance:** define route-by-route banner and screen-reader behavior.
9. **P1 — Network status:** remove generic offline/online state or define exact source and meaning.
10. **P1 — Reset semantics:** create local data inventory and per-action deletion matrix.
11. **P1 — Accessibility QA plan:** component/state/platform test matrix.
12. **P2 — Terminology governance:** change “Approved terminology” to “Proposed terminology baseline” until sign-off.
13. **P2 — Disabled control pattern:** standardize visible and programmatic reason.
14. **P2 — Identifier rendering:** defer homoglyph/copy rules to future real identifier format specification.
15. **P2 — Navigation polish:** ensure unavailable Chats/Calls cannot be mistaken for operational services.

Priority labels P0/P1/P2 indicate ordering for design correction; they do not replace severity classification.

---

# 8. Product-owner design approval register

No product-owner approval was inferred from prior design proposals. The following decisions remain explicitly pending.

| Decision | Proposed baseline | Approval |
|---|---|---|
| DA-01 Visual direction | Obsidian / Porcelain / Graphite | PENDING |
| DA-02 Accent | Restrained teal #176B63 light / #79BDB2 dark, subject to contrast | PENDING |
| DA-03 Theme | System default; light and dark available | PENDING |
| DA-04 Typography | Native system font stacks | PENDING |
| DA-05 Navigation | Chats, Calls, Contacts, Settings | PENDING |
| DA-06 Responsive behavior | Bottom tabs on compact; rail/sidebar on wider screens | PENDING |
| DA-07 Accessibility target | WCAG 2.2 AA Web; equivalent native outcomes | PENDING |
| DA-08 Non-crypto disclosure | Persistent explicit notice in onboarding and app shell | PENDING |
| DA-09 Security Center | Reframe as Security capabilities/roadmap; no score | PENDING |
| DA-10 Devices screen | Rename to Local app installations/local records | PENDING |
| DA-11 Contact form | Remove FLY ID field from current local contact flow | PENDING |
| DA-12 Status taxonomy | Canonical six-state model in Section 3.2 | PENDING |
| DA-13 Local-only wording | Evidence-backed per operation/data class only | PENDING |
| DA-14 Demo state | Persistent visual and accessible provenance on every demo route | PENDING |
| DA-15 Network state | No generic online/offline claim without a real named signal | PENDING |
| DA-16 Local reset | Per-domain deletion matrix and precise confirmation | PENDING |

**Approval mechanism:** product owner records each decision as APPROVED, REJECTED or REVISE with date and rationale. Silence or continued development is not approval.

---

# 9. UX security acceptance gates

The design may proceed to implementation planning only after the following gates close:

| Gate | Requirement | Current status |
|---|---|---|
| UXG-01 | Persistent non-crypto disclosure approved | BLOCKED |
| UXG-02 | No production FLY ID entry/display in shell | BLOCKED |
| UXG-03 | Device record/trust distinction explicit | BLOCKED |
| UXG-04 | Security Center no longer implies active controls | BLOCKED |
| UXG-05 | LOCAL ONLY claim tied to verified data flow | BLOCKED |
| UXG-06 | Light/dark contrast matrix meets target | BLOCKED |
| UXG-07 | Canonical status taxonomy approved | BLOCKED |
| UXG-08 | Demo routes and announcements enumerated | BLOCKED |
| UXG-09 | Network status semantics defined or removed | BLOCKED |
| UXG-10 | Local deletion inventory and confirmation matrix | BLOCKED |
| UXG-11 | Component/state/platform accessibility plan | BLOCKED |
| UXG-12 | Owner sign-off for DA-01..DA-16 | BLOCKED |

A BLOCKED gate is not a test failure. It means evidence/approval is not yet available.

---

# 10. Relationship to security architecture and Red-Team

- Phase 1.0 D-01 remains OPEN.
- No crypto engine is selected or integrated.
- No E2EE claim is permitted.
- No plaintext escrow is permitted.
- No custom cryptography is permitted.
- FLY ID, Device Record ID, Transport ID and future Session Keys remain separate.
- All 27 Red-Team scenario IDs remain NOT_RUN:
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
- The Red-Team catalog is not modified by this UX review.
- TOR MODE remains excluded from product, architecture, UI, configuration, tests and roadmap.
- Decentralization remains future-only.

UX findings do not replace or close any cryptographic, privacy, backend, platform, supply-chain or Red-Team release gate.

---

# 11. Phase 1.4 execution record

## Performed
- Retrieved and reviewed the complete Phase 1.3 UX specification.
- Retrieved and reviewed the Phase 1.2 product architecture baseline.
- Retrieved the Red-Team catalog and confirmed 27 unique scenario IDs and 32 NOT_RUN mentions.
- Reviewed screen routes, component inventory, state vocabulary, identity semantics, copy, permission rules, platform adaptations and accessibility targets.
- Calculated selected color-token contrast ratios from specified hex values.
- Created this findings register, screen/component audit matrices, remediation criteria, approval register and UX acceptance gates.
- Created a dedicated branch from the Phase 1.3 branch.

## Not performed
- No source code or application implementation.
- No Figma, visual prototype or generated assets.
- No technical spike, build or dependency change.
- No browser, Playwright, Android, iOS or desktop runtime test.
- No accessibility conformance audit or assistive technology test.
- No penetration test or Red-Team scenario execution.
- No API, backend, storage or data-flow implementation.
- No E2EE, messaging, calls, files, push or recovery implementation.
- No change to D-01.
- No change to `main`.
- No Pull Request.

---

# 12. Git and document verification expectations

This document is committed only to the dedicated Phase 1.4 branch. Before declaring the phase complete, verify:
- fetched file content and blob SHA;
- branch comparison against Phase 1.3 is one new file, zero deletions;
- comparison against `main` contains only additions;
- Red-Team catalog blob SHA matches the Phase 1.3 baseline;
- 27 unique Red-Team IDs remain and every scenario remains NOT_RUN;
- no PR was created and no main branch mutation occurred.

---

# 13. Formal Phase 1.4 disposition

| Item | Result |
|---|---|
| Static documentary UX review | COMPLETE |
| Screen-by-screen audit | COMPLETE |
| Component-level audit | COMPLETE |
| E2EE/deceptive claim review | COMPLETE |
| Identity semantics review | COMPLETE |
| Permission review | COMPLETE |
| Privacy disclosure review | COMPLETE |
| Accessibility target review | COMPLETE — runtime NOT_RUN |
| Contrast spot calculations | COMPLETE — one normal-text failure confirmed |
| Findings | 15 total: 5 HIGH, 7 MEDIUM, 3 LOW |
| Design-owner decisions | 16 PENDING |
| UX security acceptance gates | 12 BLOCKED |
| Design approval | NOT GRANTED |
| Implementation authorization | NOT GRANTED |
| D-01 | OPEN |
| Red-Team | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: PHASE 1.4 DOCUMENTARY REVIEW COMPLETE. DESIGN APPROVAL IS WITHHELD PENDING CLOSURE OF HIGH-SEVERITY FINDINGS, REQUIRED UX GATES AND PRODUCT-OWNER DECISIONS. NO CODE, PROTOTYPE, SPIKE OR RUNTIME QA WAS PERFORMED.**
