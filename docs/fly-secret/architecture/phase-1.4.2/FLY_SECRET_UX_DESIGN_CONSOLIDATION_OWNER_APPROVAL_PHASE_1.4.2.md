# FLY SECRET — Phase 1.4.2
## UX Design Consolidation & Product-Owner Approval Package

**Status:** CONSOLIDATED SPECIFICATION COMPLETE — OWNER APPROVAL PENDING  
**Repository:** luna7code/luna-apps  
**Base branch:** security/fly-secret-ux-security-remediation-spec-1.4.1  
**Working branch:** security/fly-secret-ux-design-consolidation-1.4.2  
**Base commit:** 842de25ed45e4ae82d9e1c709e2846a0ff889cba  
**MagicPath project:** FLY SECRET Phase 1.4.2 (private design-specification board)  
**Document path:** docs/fly-secret/architecture/phase-1.4.2/FLY_SECRET_UX_DESIGN_CONSOLIDATION_OWNER_APPROVAL_PHASE_1.4.2.md  
**Date:** 2026-11-14

> This phase consolidates the existing product, UX and remediation specifications into one reviewable baseline and an explicit owner-decision package. It does not approve the proposed design, authorize application implementation, create an application prototype, select a cryptographic protocol or close security gates.

---

# 1. Executive disposition

Phase 1.4.2 consolidates four inputs:

1. Phase 1.2 — Non-Crypto MVP Product Architecture.
2. Phase 1.3 — UX Architecture & Design System Specification.
3. Phase 1.4 — UX Security Review & Design Approval.
4. Phase 1.4.1 — UX Security Remediation Specification.

The consolidated baseline preserves the intended four-destination information architecture and the native-first, responsive visual system proposal while applying all 15 UX security remediations.

## 1.1 Outcome

| Area | Phase 1.4.2 result |
|---|---|
| Product scope and route model | Consolidated |
| Screen and component terminology | Consolidated |
| Capability-state semantics | Consolidated into one UI taxonomy |
| Planning taxonomy vs UI state | Explicitly separated |
| Identity and device semantics | Security corrections take precedence |
| Non-crypto disclosure | Exact EN/RU copy consolidated |
| LOCAL ONLY claims | Evidence-gated; not currently claimable without evidence |
| Theme, color and typography | Proposed baseline consolidated; owner approval pending |
| Responsive model | Proposed baseline consolidated; owner approval pending |
| Accessibility | Acceptance requirements consolidated; execution NOT_RUN |
| 15 UX-SEC findings | Corrections specified; none implemented or closed |
| DA-01…DA-16 | 16 decisions PENDING |
| RA-01…RA-16 | 16 decisions PENDING |
| UX security gates | Remain BLOCKED pending evidence and approval |
| Application code or prototype | Not created |
| Cryptographic implementation | Not authorized; D-01 remains OPEN |
| Red-Team | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main / Pull Request | main unchanged; no PR |

## 1.2 Normative precedence

When source documents differ, apply this order:

1. Security invariants and explicit product-owner constraints.
2. Phase 1.4.1 security remediation requirements.
3. Phase 1.4 documentary findings and security gates.
4. Phase 1.3 design-system proposals, subject to the corrections in this document.
5. Phase 1.2 product architecture, subject to later identity, device, state and security corrections.

This precedence resolves documentation conflicts only. It does not convert a proposed design into an approved design.

## 1.3 Terms used in this document

- **Normative requirement:** must be followed by later design and implementation work.
- **Proposed baseline:** recommended direction awaiting product-owner approval.
- **PENDING:** no approval has been recorded.
- **SPECIFIED:** documented; not implemented.
- **BLOCKED:** required evidence or approval is absent.
- **NOT_RUN:** a test or runtime check has not been executed.
- **Future-only:** excluded from the current MVP and not a current implementation commitment.

---

# 2. Product posture and security boundary

## 2.1 Current product scope

FLY SECRET is specified as a cross-platform protected-messaging product direction for Android, iOS, Desktop and Web. The current execution scope remains a non-crypto MVP shell and UX specification.

The present build must not be represented as a functioning private messenger or as a secure communication service.

The current phase does not provide:

- private messaging or real message delivery;
- end-to-end encryption;
- verified identity or cryptographic contact verification;
- trusted-device lifecycle or cryptographic device revocation;
- secure voice or video calls;
- private file transfer or media decryption;
- production push notifications;
- Recovery Kit or account recovery;
- a selected or integrated production cryptographic engine.

No screen, status, icon, animation, badge, illustration or copy may imply that one of these capabilities is active.

## 2.2 Mandatory persistent non-crypto disclosure

The disclosure below is normative for the current non-crypto build. It must appear during onboarding and remain available in the application shell. It must not be hidden behind a secondary information route or reduced to a decorative badge.

**English**

> This build does not provide private messaging, end-to-end encryption, identity verification, secure calls or account recovery. Do not use it to exchange sensitive information. Demo content is synthetic.

**Russian**

> Эта сборка не поддерживает приватные сообщения, сквозное шифрование, проверку личности, защищённые звонки или восстановление аккаунта. Не используйте её для передачи конфиденциальной информации. Демонстрационные данные являются искусственными.

Presentation requirements:

- Use normal readable body text, not tiny metadata or a transient toast.
- Preserve the complete meaning at 200% text scaling and browser zoom.
- Announce the disclosure to assistive technology when it is first presented in onboarding.
- Keep the notice visible or directly discoverable in the persistent app shell without requiring a search.
- Do not present an acknowledgement checkbox as proof that the product is secure or that legal consent has been obtained.
- Localize RU and EN from the same controlled copy source.
- Do not soften this notice into promotional wording.

## 2.3 Security language that is prohibited in the current shell

Do not describe the current build as secure, encrypted, E2EE-protected, verified, trusted, anonymous, untraceable, private-by-default or protected.

Do not use:

- a green shield, lock or checkmark to imply security activation;
- security scores, readiness percentages or fake protection meters;
- animated key exchange, encryption or identity-verification progress;
- sent, delivered, read, online or ringing indicators for synthetic content;
- “secure connection” based only on Internet reachability or transport connectivity;
- claims that local record deletion revokes remote access;
- “secure erase” or “deleted everywhere” without verified platform-specific evidence.

A future implementation may make narrowly scoped claims only after the capability exists, its data flow is evidenced, the claim is reviewed and the associated release gates are closed.

---

# 3. Consolidated identity, device and privacy model

## 3.1 Identity object separation

These concepts must remain distinct in architecture, API contracts, UI copy and future data models.

| Concept | Meaning | Must not be presented as |
|---|---|---|
| FLY ID | Future user-facing pseudonymous identifier | Password, private key, bearer token or proof of identity |
| Device Record ID | Identifier for a local/application device record | Cryptographic identity, trusted-device proof or authorization |
| Cryptographic Device Identity | Future protocol-bound device identity | Device name, hardware serial or inventory row |
| Transport ID | Scoped routing identifier | Stable personal identity or anonymity proof |
| Session ID | Future protocol session reference | Session key or user identity |
| Session Keys | Future cryptographic secret material | UI identifier, API token or preference |

No production values for these identifiers or secrets may be generated or exposed in this phase.

## 3.2 FLY ID — current and future behavior

Current non-crypto shell:

- Do not generate or display a production FLY ID.
- Do not expose an FLY ID input in the local contact form.
- Do not offer FLY ID lookup, directory resolution, QR exchange, copy, share, invite or verification.
- Do not place a synthetic identifier in a field that looks like a real account identifier.
- A static educational explanation may describe the future concept, provided it clearly states that the capability is not available in this build.
- A demo may use a visibly fictional example only where it cannot be mistaken for a real identifier; prefer omitting the identifier entirely.

Future-only requirements:

- Define the identifier lifecycle, entropy, collision handling, rotation and revocation before UI design.
- Define correlation and linkability risk before copy/share flows.
- Separate FLY ID from cryptographic identity and transport routing.
- Require explicit disclosure before a user shares an identifier.
- Do not infer identity from phone, email, device fingerprint or other personal data.
- Do not create an identity lookup service until directory privacy and key-transparency models are separately approved.

**Conflict resolution:** Phase 1.2 allowed a synthetic placeholder and discussed an unverified local identifier note. Phase 1.4.1 removes the current FLY ID field and production-looking identifier surfaces. Phase 1.4.1 and this consolidated document supersede the earlier field proposal.

## 3.3 Contacts

Current local contact data, if a later implementation is separately authorized, is limited to:

- display name / alias;
- optional local note;
- optional local favorite flag.

The current contact form must not contain FLY ID, phone, email, identity lookup, key fingerprint, QR, verification or invitation controls.

No system address-book permission is requested by default. No contact discovery or upload is implied.

## 3.4 Local app installations

The former “Devices” concept is renamed:

- English: **Local app installations**
- Russian: **Локальные установки приложения**

Permitted content:

- local app-installation records, only if such records are actually implemented;
- a user-defined local label;
- platform family, only where the app can reliably identify it and the display is necessary;
- a precise local record removal action, if implemented.

Forbidden in the current shell:

- Trusted, Verified, Paired, Active, Revoked or Secure device labels;
- remote device revocation controls;
- fake device enrollment or approval;
- hardware serials, persistent hardware fingerprints or unnecessary device diagnostics;
- claims that deleting a local row revokes cryptographic authorization.

Required explanation:

> Removing a local app-installation record removes that record from this app. It does not revoke a remote session or cryptographic device authorization.

Russian equivalent:

> Удаление локальной записи установки удаляет эту запись из приложения. Оно не отзывает удалённую сессию или криптографическую авторизацию устройства.

## 3.5 LOCAL ONLY / “Stored locally” claim policy

The UI may display “Stored locally” only for a specific implemented operation and data class after evidence confirms the stated behavior on the relevant platform.

Required evidence inventory must cover:

1. Primary database and local files.
2. OS backup and device migration behavior.
3. Cloud sync and browser profile sync.
4. Analytics and telemetry.
5. Crash reporting and diagnostic attachments.
6. Logs and error reporting.
7. Export, share sheet and clipboard paths.
8. Temporary files, caches and thumbnails.
9. Reset/deletion behavior and residual copies.
10. Platform-specific storage and permission behavior.

Until the complete relevant scope is verified, use:

- English: “Designed for local storage; behavior not yet verified.”
- Russian: “Предусмотрено локальное хранение; поведение ещё не проверено.”

A local-first architectural intention is not proof of locality. A local UI label is not proof that backups, telemetry, crash reports, exports or operating-system services cannot copy data.

---

# 4. Information architecture and route baseline

## 4.1 Primary navigation

The proposed and retained primary navigation contains exactly four destinations in this order:

1. Chats
2. Calls
3. Contacts
4. Settings

The four destinations are an information-architecture structure. They do not claim that messaging, calling, identity or contact discovery is operational.

## 4.2 Navigation behavior

| Platform / width | Proposed navigation |
|---|---|
| Compact Android/iOS | Four-item bottom navigation with visible text labels |
| Large mobile / small tablet | Bottom navigation or adaptive rail when layout genuinely benefits |
| Tablet / compact desktop | Navigation rail plus content |
| Desktop / Web | Persistent sidebar plus constrained content area |

Rules:

- Keep route hierarchy and destination order consistent.
- Do not use icon-only navigation as the sole label.
- Provide accessible names and an unmistakable selected state.
- Preserve browser history, deep-link fallback and back/forward semantics on Web.
- Never put message content, secrets, Recovery Kit material or cryptographic values in URLs or navigation state.
- Do not use notification badges to imply incoming real messages or calls.
- Do not request permissions merely because a user opens Chats, Calls or Contacts.

## 4.3 Route inventory

The stable route inventory from Phase 1.2 is preserved. This table combines route-family identifiers with screen-specification IDs for readability. Identifiers such as ONB-01, CHAT-01, CALL-01, CONTACT-01 and SET-01 are screen-specification references, not new router paths or additional stable route IDs. The underlying route families remain APP-ROOT, TAB-CHATS, CHAT-DETAIL-DEMO, TAB-CALLS, CALL-DETAIL-DISABLED, TAB-CONTACTS, CONTACT-LOCAL-DETAIL, TAB-SETTINGS, SETTINGS-PROFILE, SETTINGS-DEVICES, SETTINGS-PRIVACY, SETTINGS-SECURITY, SETTINGS-NOTIFICATIONS, SETTINGS-APPEARANCE, SETTINGS-LANGUAGE, SETTINGS-ABOUT and ONBOARDING-*.

| Route family / screen reference | Consolidated route meaning | Current capability posture |
|---|---|---|
| APP-ROOT | Application shell and persistent disclosure | Specification only |
| ONB-01 | Welcome | Specification only |
| ONB-02 | Language selection | Specification only |
| ONB-03 | Setup choice / quick preview | No account creation; demo disclosure required for preview |
| ONB-04 | Local profile setup | Alias only; no FLY ID display |
| ONB-05 | Privacy briefing | Mandatory non-crypto disclosure and data limitations |
| ONB-06 | Setup completion | No “secured” or “encrypted” success state |
| TAB-CHATS / CHAT-01 | Chats list | Unavailable or clearly synthetic demo only |
| CHAT-02 | Demo conversation | Synthetic content only; no composer or delivery/read states |
| CHAT-03 | Conversation information | Demo/local metadata only; no verified identity or keys |
| CHAT-04 | Search | Synthetic fixture search only; no server discovery |
| TAB-CALLS / CALL-01 | Calls landing | Unavailable explanation; no dial or permission request |
| CALL-02 | Call detail concept | Synthetic record only, if a fixture exists |
| CALL-03 | Call readiness explanation | Deferred; no readiness claim |
| TAB-CONTACTS / CONTACT-01 | Contacts list | Local records or marked synthetic fixtures only |
| CONTACT-02 | Local contact detail | Alias, local note, optional favorite |
| CONTACT-03 | Add local contact | Alias + optional local note; no FLY ID |
| CONTACT-04 | Verification explanation | “Not available yet”; no real fingerprint or QR |
| TAB-SETTINGS / SET-01 | Settings home | Specification only |
| SET-02 | Local profile | Alias and local preferences only |
| SET-03 | Local app installations | No trust, pairing or revocation semantics |
| SET-04 | Privacy Center | Evidence-backed data inventory only |
| SET-05 | Security capabilities | Availability information, not an operational security dashboard |
| SET-06 | Notifications | Local preferences only, if implemented; no push token collection |
| SET-07 | Appearance and accessibility | Theme and accessibility preferences, if implemented |
| SET-08 | Language | Russian / English preference |
| SET-09 | About | Accurate build and license information only |

Phase 1.2 route identifiers may be retained internally for continuity, but their labels, content and behavior must follow this consolidated baseline.

---

# 5. Canonical capability state model

## 5.1 One UI capability taxonomy

The six-state taxonomy from Phase 1.4.1 is the canonical state vocabulary for UI capability availability.

| State | Exact meaning | User-facing label EN | User-facing label RU |
|---|---|---|---|
| AVAILABLE | Implemented, enabled, tested for the stated scope and approved | Available | Доступно |
| LOCAL_ONLY | Implemented operation with verified, scoped local-only data flow | Stored locally | Хранится локально |
| DEMO | Synthetic fixture or simulated interaction, not a real service operation | Demo | Демонстрация |
| UNAVAILABLE | Not implemented or not supported in this build | Not available in this build | Недоступно в этой сборке |
| DEFERRED | Deliberately excluded pending a named architecture or security gate | Deferred | Отложено |
| DISABLED_BY_USER | Implemented feature explicitly turned off by the user | Off | Выключено |

Rules:

- Unknown state fails closed and is displayed as “Status unavailable” / “Статус недоступен”.
- Unknown must never be inferred as AVAILABLE.
- LOCAL_ONLY requires data-flow evidence for the exact operation, data class and platform.
- DEMO requires synthetic provenance at route and record level.
- DEFERRED requires a named dependency, decision or security gate.
- DISABLED_BY_USER may be used only when a real implemented user setting exists.
- A feature that does not exist cannot be described as DISABLED_BY_USER.
- A feature cannot transition from DEFERRED or UNAVAILABLE to AVAILABLE through a configuration flag alone.
- “Information” is a content type, not a capability state.
- Loading and Error are operation states, not capability states.

## 5.2 Planning classification is not a UI state

Phase 1.2 used the planning labels SPECIFIED, LOCAL_ONLY, DEMO_ONLY, DISABLED, DEFERRED_SECURITY_GATE and NOT_IN_SCOPE.

Those labels may remain in legacy planning artifacts and backlog metadata, but they must not be rendered as a second UI state system.

Mapping:

| Phase 1.2 planning label | Consolidated interpretation | UI consequence |
|---|---|---|
| SPECIFIED | Requirements documented; no implementation implied | UNAVAILABLE or no exposed action |
| LOCAL_ONLY | Local behavior is an architectural intention, not evidence | Do not show LOCAL_ONLY until implementation and data-flow proof exist |
| DEMO_ONLY | Synthetic demonstrator scope | DEMO with explicit persistent provenance |
| DISABLED | Explanatory surface without active operation | UNAVAILABLE or DEFERRED according to reason |
| DEFERRED_SECURITY_GATE | Blocked on named security/architecture decision | DEFERRED |
| NOT_IN_SCOPE | Excluded from current scope | Omit from UI unless an explanation is necessary; never imply availability |

Additional Phase 1.2 state models are domain-specific and do not override the UI capability taxonomy:

| Legacy Phase 1.2 model | Consolidated interpretation | Current UI rule |
|---|---|---|
| AVAILABLE_LOCAL | A local implementation candidate, not proof of a verified local-only data flow | Do not display AVAILABLE or LOCAL_ONLY without implementation and evidence; otherwise UNAVAILABLE or DEFERRED |
| OFFLINE / ONLINE / DEGRADED / UNKNOWN | Network reachability or service state | No generic shell badge; show only a named, source-backed signal under Section 9.2 |
| NO_LOCAL_PROFILE / LOCAL_PROFILE_PRESENT | Local profile lifecycle | Internal domain state only; never imply identity verification or account enrollment |
| SYNTHETIC_FLY_ID | Synthetic identifier fixture | DEMO only if a clearly fictional example is explicitly approved; default is to omit |
| LOCAL_FLY_ID | Previously proposed local identifier state | Removed from the current shell; no production ID generation or display |
| LOCAL_RECORD / DEMO_RECORD / REMOVED_LOCAL | Local record origin/removal lifecycle | Record provenance only; never map to trusted, paired, active or revoked device states |

This mapping is deliberate: the same string “LOCAL_ONLY” has different meanings in legacy planning metadata and the runtime capability state. Do not interchange those meanings.

## 5.3 Application and operation state are separate

The application lifecycle model from Phase 1.2 remains separate from capability availability. COLD_START, BOOTSTRAPPING, FIRST_RUN, ONBOARDING, QUICK_PREVIEW, READY, BACKGROUND, ERROR and RECOVERY_REQUIRED are application lifecycle concepts, not capability states.

A control operation may separately report loading, success or error. These operation states do not change the capability state unless an authorized, evidence-backed product transition occurs.

---

# 6. Consolidated visual design system proposal

All values in this section are proposed design tokens. DA-01…DA-07 remain PENDING. The MagicPath board visualizes this proposal and is not a product-owner approval.

## 6.1 Visual direction

**Proposed name:** Obsidian / Porcelain / Graphite.

Design intent:

- restrained, calm, high-clarity interface;
- neutral surfaces with strong readable text;
- limited accent usage for navigation and primary actions;
- no neon, glow, decorative circuit patterns or encryption-themed gradients;
- no security status conveyed through color alone;
- no ornamental shield or padlock motif;
- consistent native-feeling controls across platforms.

## 6.2 Light theme tokens

| Token | Proposed value | Purpose |
|---|---|---|
| color.canvas | #F7F7F5 | App background |
| color.surface | #FFFFFF | Primary cards and sheets |
| color.surface.subtle | #F0F1EF | Secondary grouped surface |
| color.surface.raised | #FFFFFF | Dialogs and popovers |
| color.text.primary | #171918 | Primary text |
| color.text.secondary | #5E6461 | Secondary text |
| color.text.tertiary | #626A65 | Metadata only; corrected proposal |
| color.border | #DDE1DE | Dividers and outlines |
| color.border.strong | #B8BFBA | Strong boundaries |
| color.accent | #176B63 | Primary action and selected control |
| color.accent.hover | #125A54 | Hover/pressed progression |
| color.accent.subtle | #E7F1EF | Subtle accent surface |
| color.warning | #8A5B13 | Warning semantics |
| color.warning.surface | #FFF4DE | Warning background |
| color.danger | #B3261E | Destructive/error semantics |
| color.danger.surface | #FCEBE9 | Destructive/error background |
| color.success | #246B45 | Completed operation only |
| color.focus | #175FC4 | Focus indicator; not brand accent |

The proposed light tertiary correction is #626A65 on #F7F7F5, approximately 5.19:1 by token calculation. This improves the previously identified 3.88:1 pair. It is not a substitute for checking every rendered pairing.

## 6.3 Dark theme tokens

| Token | Proposed value | Purpose |
|---|---|---|
| color.canvas | #111312 | App background |
| color.surface | #191C1A | Primary cards and sheets |
| color.surface.subtle | #202522 | Secondary grouped surface |
| color.surface.raised | #252A27 | Dialogs and popovers |
| color.text.primary | #F2F4F1 | Primary text |
| color.text.secondary | #B7BFBA | Secondary text |
| color.text.tertiary | #929B95 | Metadata |
| color.border | #343B36 | Dividers and outlines |
| color.border.strong | #4B554E | Strong boundaries |
| color.accent | #79BDB2 | Primary action and selected control |
| color.accent.hover | #91C9BF | Hover/pressed progression |
| color.accent.subtle | #20332F | Subtle accent surface |
| color.warning | #E9B85F | Warning semantics |
| color.warning.surface | #382B18 | Warning background |
| color.danger | #F28B82 | Destructive/error semantics |
| color.danger.surface | #3A2220 | Destructive/error background |
| color.success | #8BC9A2 | Completed operation only |
| color.focus | #8AB4F8 | Focus indicator |

## 6.4 Color semantics

- Accent means primary interactive emphasis, never encrypted or verified.
- Success means a specific operation completed, never secure.
- Warning means user attention is required.
- Danger means destructive action or error.
- Neutral is the default for unknown, unavailable, demo and deferred information unless the state itself requires a semantic warning.
- No cryptographic trust color is defined in the current build.
- Color must never be the only channel conveying a state.
- Do not use red/green as a substitute for accessible text labels.
- The focus color is a utility token, not a secondary brand accent.
- A full foreground/background/state matrix is required before implementation.

## 6.5 Typography

Proposed system-native stacks:

- Android: Roboto / system sans-serif.
- iOS: SF Pro / system sans-serif.
- Web: system UI stack with Cyrillic-compatible fallback.
- Desktop: native system sans-serif.

Proposed type scale:

| Token | Size / line height | Weight | Use |
|---|---:|---|---|
| display | 32 / 40 | Semibold | Onboarding hero, sparingly |
| h1 | 28 / 36 | Semibold | Main screen title |
| h2 | 22 / 30 | Semibold | Section title |
| h3 | 18 / 26 | Semibold | Card or group title |
| body.large | 17 / 25 | Regular | Introductory content |
| body | 16 / 24 | Regular | Main reading text |
| body.small | 14 / 20 | Regular | Supporting text |
| label | 14 / 20 | Medium | Buttons and labels |
| caption | 12 / 18 | Regular | Metadata only |
| numeric | 14–16 / 20–24 | Medium, tabular | Counts and dates |

Rules:

- Do not use caption size for instructions, warnings or security disclosures.
- Avoid long all-caps copy.
- Use sentence case.
- Do not rely on thin weights on dark backgrounds.
- Respect user text scaling and browser zoom.
- No critical identity or safety explanation may be silently truncated.

## 6.6 Spacing, grid, shape and motion

Spacing scale uses a 4 pt base:

| Token | Value |
|---|---:|
| space.1 | 4 |
| space.2 | 8 |
| space.3 | 12 |
| space.4 | 16 |
| space.5 | 20 |
| space.6 | 24 |
| space.8 | 32 |
| space.10 | 40 |
| space.12 | 48 |
| space.16 | 64 |

Responsive breakpoints:

| Width | Layout |
|---|---|
| 320–359 px | Compact mobile; single column; 16 px side inset |
| 360–599 px | Mobile; single column; 16–20 px inset |
| 600–839 px | Large mobile / small tablet; single or two-column settings |
| 840–1199 px | Tablet / compact desktop; rail plus content |
| 1200–1599 px | Desktop; sidebar and centered content, max 1120 px |
| 1600 px and above | Wide desktop; constrained content, max 1200 px |

Touch and pointer targets:

- Cross-platform design target: at least 44 × 44 CSS px/pt.
- Android controls should aim for 48 dp where practical.
- Do not put adjacent destructive and non-destructive controls close together.
- Hover is supplementary; keyboard and touch users need equivalent access.
- Focus-visible must be independently visible from hover and selection.

Motion:

- Respect reduced-motion preferences.
- Use motion only to explain state change or spatial continuity.
- Avoid fake cryptographic, connection or device-trust animations.
- Do not use animation as the only status cue.
- No forced theme-transition animation.
- Motion timing values remain proposals and require device-level review.

---

# 7. Consolidated component contract

Each component must preserve its semantics and accessibility across light/dark theme, mobile/desktop layout and all capability states.

| Component | Consolidated requirement |
|---|---|
| AppShell | Persistent non-crypto disclosure; no generic network/security badge; predictable content hierarchy |
| PrimaryNavigation | Four destinations in fixed order; labels, accessible names and visible selected state |
| ScreenHeader | One clear title; optional subtitle; no security claim in decorative icon |
| PrimaryButton | One primary action per decision context; no fake operational action |
| SecondaryButton | Visually subordinate but clearly actionable |
| DestructiveButton | Explicit action language and consequence; separated from safe action |
| InputField | Explicit label, help text, error association and no identity implication |
| Card / GroupedRow | Clear hierarchy; avoid status-color decoration |
| StatusBadge | Uses canonical capability state; text plus non-color cue |
| EmptyState | Accurate explanation and a safe next step; no fabricated activity |
| DisabledFeaturePanel | No misleading active control; adjacent reason and accessible explanation |
| DemoDataBanner | Persistent, route-aware synthetic provenance |
| LocalInstallationRecord | Local record only; no trusted, paired, active or revoked semantics |
| IdentifierDisclosurePanel | Educational copy only in current build; no live ID or share control |
| Dialog / BottomSheet | Focus management, clear title, explicit actions and reversible dismissal where safe |
| Toast / InlineNotice | Toast for transient local result; inline notice for persistent explanation; never use toast for critical security warning |
| DisclosureNotice | Full non-crypto statement; readable, persistent and announced where appropriate |

## 7.1 Interaction state matrix

Components must explicitly define:

- default;
- hover (where applicable);
- pressed;
- focus-visible;
- selected;
- disabled;
- loading (only for an actual operation);
- success (only after an actual successful operation);
- validation error;
- unavailable;
- demo;
- deferred;
- unknown / status unavailable.

Do not make a disabled state visually identical to an active one. Do not remove accessibility names when a control is disabled. Do not expose a loading state without a real operation.

## 7.2 Disabled action reason pattern

A disabled control must either be absent or have a reason adjacent to it. The reason must be programmatically associated and announced on focus where required.

Examples:

- EN: “Voice calls are not available in this build.”
- RU: “Голосовые звонки недоступны в этой сборке.”
- EN: “Identity verification has not been implemented.”
- RU: “Проверка личности ещё не реализована.”
- EN: “This action is deferred pending security review.”
- RU: “Это действие отложено до завершения проверки безопасности.”

No disabled control should look like a functioning workflow waiting for a click.

---

# 8. Screen-by-screen final content and interaction specification

## 8.1 Onboarding

### ONB-01 — Welcome

Content order:

1. FLY SECRET brand mark and product name.
2. Plain-language product purpose.
3. Full non-crypto disclosure.
4. Continue action.
5. Language selection entry.

Do not use a “secure messenger” headline. Do not use lock/shield art as a proof of protection.

### ONB-02 — Language

- Russian and English.
- Native radio/list semantics.
- Selection persisted locally only if preference storage is implemented.
- No locale-based identity inference.
- Language can be changed later in Settings.

### ONB-03 — Setup or quick preview

Two equal-weight choices:

- “Set up local preferences” / “Настроить приложение”
- “Quick preview” / “Быстрый просмотр”

Neither option creates an account, contacts a server or enrolls an identity. If preview contains synthetic content, announce and display the DEMO state before entry.

### ONB-04 — Local profile

- Optional display alias only.
- No FLY ID generation or display.
- No phone/email enrollment.
- No account-registration claim.
- Optional avatar only if separately authorized and implemented.
- Clear skip action.

### ONB-05 — Privacy briefing

- Mandatory non-crypto disclosure.
- Current data categories and storage boundaries only when evidenced.
- No contact upload or message processing claim unless verified.
- OS backup, telemetry, crash reporting, export and deletion limitations.
- No permission request at onboarding.

### ONB-06 — Completion

- Confirm local preference setup only.
- State what was saved, if anything was actually saved.
- No “account secured”, “identity secured”, “encryption enabled” or similar success copy.

## 8.2 Chats

### CHAT-01 — Conversation list

- Title: Chats.
- Empty state is the default.
- If synthetic rows exist, show persistent DEMO provenance.
- No operational message composer, send control, sent/delivered/read ticks, online indicator or E2EE badge.
- Search may target synthetic fixtures only.
- No contacts import prompt.

Suggested empty copy:

- EN: “Private messaging is not available in this build.”
- RU: “Приватные сообщения недоступны в этой сборке.”

### CHAT-02 — Demo conversation

- Explicit DEMO label in persistent context.
- Synthetic messages only.
- No user-entered sensitive content.
- No real composer or send action.
- No delivery, read or encryption signals.
- Demo interactions must not simulate a real network operation.

### CHAT-03 — Conversation information

- Synthetic/local label only.
- No verified identity, fingerprint, session or device trust data.
- Any local note must be distinguished from verified profile data.

### CHAT-04 — Search

- Search only synthetic fixtures.
- Label results as Demo.
- No remote directory or server-side discovery.
- Empty and error states are explicit and accessible.

## 8.3 Calls

### CALL-01 — Calls landing

- Title: Calls.
- Explanation that voice and video calls are not available.
- No dial, start-call, answer or hang-up control.
- No microphone/camera permission request.
- No fabricated call history.

Suggested copy:

- EN: “Voice and video calls are not available in this build.”
- RU: “Голосовые и видеозвонки недоступны в этой сборке.”

### CALL-02 — Call detail concept

- Only a synthetic record, if a demo fixture is explicitly approved.
- Persistent Demo provenance.
- No peer IP, ICE, relay, codec, transport-security or media-encryption indicators.
- No call activity that resembles a live service.

### CALL-03 — Call readiness explanation

- Informational content only.
- Explain that real calls are deferred pending a separately reviewed media-security design.
- Distinguish transport protection from app-level media E2EE.
- No “ready to call” or percentage readiness indicator.

## 8.4 Contacts

### CONTACT-01 — Contacts list

- Local entries only if separately implemented.
- Synthetic fixtures must be clearly marked Demo.
- No address-book import prompt or permission request.
- No online, trusted or verified status.
- No FLY ID lookup or invitation surface.

### CONTACT-02 — Contact detail

Allowed fields:

- display alias;
- optional local note;
- optional favorite state.

Forbidden:

- FLY ID;
- real key fingerprint;
- “Verified” or “Trusted” badge;
- QR identity;
- fabricated online state.

### CONTACT-03 — Add local contact

Current form:

- display name / alias;
- optional local note;
- favorite flag only if implemented.

No FLY ID field, phone, email, directory lookup, key verification or invitation action.

### CONTACT-04 — Verification explanation

- Explain future fingerprint/QR verification conceptually.
- State “Not available yet” / “Пока недоступно”.
- Do not generate a fingerprint or QR.
- An illustrative example, if ever approved, must be labelled “Illustrative example — not a real verification” / “Иллюстративный пример — не настоящая проверка”.

## 8.5 Settings

### SET-01 — Settings home

Proposed grouped sections:

- Profile
- Local app installations
- Privacy Center
- Security capabilities
- Notifications
- Appearance and accessibility
- Language
- About

### SET-02 — Local profile

- Local display alias.
- Local avatar preference only if implemented.
- Educational FLY ID explanation only; no production ID surface.
- No copy, share, QR, lookup or verification action.

### SET-03 — Local app installations

- Local installation records only, if implemented.
- Local record removal must explain its limited scope.
- No trusted, verified, paired, active or revoked badge.
- No remote revoke claim.

### SET-04 — Privacy Center

- Current data inventory only after implementation inspection.
- Separate stored data from telemetry, logs, crash reports, backups, exports and OS-managed copies.
- Do not use privacy score.
- Do not display LOCAL_ONLY without complete evidence for the relevant operation/data class/platform.

### SET-05 — Security capabilities

This replaces the current operational-dashboard interpretation of Security Center.

Required contents:

- Persistent non-crypto disclosure.
- Capability list with canonical states.
- Exact unavailable/deferred reasons.
- No shield score or positive “protected” status.
- No activation toggles for non-existent controls.
- No fabricated verification, recovery or device trust activity.

### SET-06 — Notifications

- Local preferences only, if implemented.
- No push token collection or permission request in this phase.
- Do not display preview-content controls as operational before a notification service exists.
- Future push payloads must be opaque wake-up only under the security architecture.

### SET-07 — Appearance and accessibility

- System theme default is the proposal.
- Manual theme override only if implemented.
- Text scaling, contrast, reduced motion and accessibility preferences.
- Never imply that a theme switch changes security or privacy state.

### SET-08 — Language

- Russian and English.
- User-controlled.
- Preference storage must be scoped and documented.

### SET-09 — About

- Accurate version/build information only when available.
- Open-source licenses and legal notices.
- Support links only when an official destination exists.
- No fabricated security certification or audit claim.

---

# 9. Demo provenance and network semantics

## 9.1 Demo-route matrix

Any route that may display synthetic content must show both visible and assistive-technology provenance.

| Route | Synthetic content possible | Required provenance |
|---|---|---|
| ONB-03 Quick preview | Yes | Disclosure before entering preview |
| CHAT-01 Conversation list | Yes | Persistent Demo banner |
| CHAT-02 Demo conversation | Yes | Persistent Demo banner |
| CHAT-03 Conversation information | Yes | Demo label attached to record |
| CHAT-04 Search | Yes | Demo label on results |
| CALL-02 Call details concept | Yes | Persistent Demo banner |
| CONTACT-01 Contacts list | Yes | Demo label and banner when fixture shown |
| CONTACT-02 Contact detail | Yes | Demo label attached to record |
| CONTACT-04 Verification explanation | Illustrative example only | “Illustrative example — not a real verification” |
| Settings fixture views | Only when fixtures exist | Persistent Demo banner |

No other route may silently display synthetic message, contact, call or device activity.

Assistive behavior:

- On route entry announce “Demo content. Synthetic data only.” once.
- Keep a visible persistent label in screen context.
- Do not repeat the announcement on every focus movement.
- Announce a transition from local data to demo data.
- Do not mix demo and user-created local records without clearly separated groups and labels.

## 9.2 Network status

Do not display a generic online/offline badge in the current non-crypto shell.

Future labels may be used only if the corresponding signal is actually implemented:

- “Internet connection available” — OS/network reachability only.
- “Service unavailable” — a named service health result.
- “Not connected to service” — actual application connection state.
- “Connection status unknown” — no reliable signal.

Forbidden:

- “Secure connection” without exact property and evidence.
- “Relay connected” unless a real relay connection is established.
- “Anonymous” or “private” based on connectivity.
- “Online” as a proxy for identity verification or E2EE.

Every future network indicator must define source, freshness, polling/event behavior, stale behavior, offline behavior and what it does not prove.

---

# 10. Local reset and deletion semantics

No generic “delete everything securely” action is permitted.

Before implementation, create a data inventory by domain and platform.

Required domains:

- local display preferences;
- local profile alias;
- local contact records;
- local favorites and notes;
- demo fixtures and demo progress;
- caches and temporary files;
- diagnostic and crash artifacts;
- exported files and share-sheet copies;
- browser storage and browser profile sync;
- OS backups and device migration copies.

Each reset action must specify:

1. Exact data categories affected.
2. Categories not affected.
3. Whether the action is reversible.
4. What happens if storage is unavailable or deletion partially fails.
5. Whether OS backups or external copies remain.
6. Whether the action is purely local.
7. Whether a remote account, session or cryptographic authorization exists and is affected.

Current shell copy template:

- EN: “Remove this local record? This does not revoke remote access or erase copies outside this app.”
- RU: “Удалить эту локальную запись? Это не отзывает удалённый доступ и не удаляет копии за пределами приложения.”

Do not use “secure erase”, “deleted everywhere” or “all traces removed” without forensic and platform-specific evidence.

---

# 11. Accessibility acceptance requirements

## 11.1 Target

Proposed target:

- Web: WCAG 2.2 AA.
- Android and iOS: equivalent native accessibility outcomes using platform guidance and assistive technologies.
- Desktop: keyboard, screen-reader, scaling and focus outcomes equivalent to the Web target where applicable.

The target is PENDING owner approval. No accessibility audit is claimed as performed.

## 11.2 Mandatory acceptance matrix

| Dimension | Required acceptance |
|---|---|
| Contrast | Complete foreground/background/state matrix for both themes; WCAG 2.2 AA text and non-text criteria |
| Keyboard | Every action reachable; logical order; no keyboard trap |
| Focus | Visible focus indicator; focus order follows reading order; restoration after dialog |
| Screen readers | Correct roles, names, values, headings, state and status announcements |
| Text scaling | Reflow at 200% without clipped or hidden content |
| Browser zoom | Reflow at 200%; no loss of critical controls |
| Touch targets | At least 44 × 44 CSS px/pt design target; Android aim 48 dp where practical |
| Motion | Reduced-motion preference respected; no status communicated only by animation |
| Errors | Field association, summary where needed, specific correction guidance |
| Dynamic status | Announced when meaningful; no repeated noisy announcements |
| Localization | RU/EN text expansion, pronunciation, punctuation and truncation checked |
| Disabled actions | Reason adjacent and programmatically associated |
| Dialogs | Focus containment and restoration; clear title and action consequence |
| Demo provenance | Visible and announced once per route entry |
| Non-crypto disclosure | Readable, persistent and announced in onboarding |

## 11.3 High-risk screens

Full accessibility test cases are required for:

- onboarding and non-crypto disclosure;
- Chats empty/demo states;
- Calls unavailable state;
- contact creation and local contact detail;
- Security capabilities;
- Local app installations;
- privacy/reset dialogs;
- all error and unknown-status states.

## 11.4 Contrast evidence status

Existing token calculations indicate:

| Pair | Prior ratio | Consolidated treatment |
|---|---:|---|
| Light tertiary #777E7A on #F7F7F5 | 3.88:1 | Replace with proposed #626A65 |
| Light secondary #5E6461 on #F7F7F5 | 5.64:1 | Retain as proposal |
| Light accent #176B63 on white | 6.33:1 | Retain as proposal |
| Dark tertiary #929B95 on #191C1A | 6.01:1 | Retain as proposal |
| Dark secondary #B7BFBA on #191C1A | 9.14:1 | Retain as proposal |
| Dark accent #79BDB2 on #191C1A | 7.96:1 | Retain as proposal |

These figures are token-pair calculations, not a rendered interface audit. Borders, focus, selection, disabled states, overlays and all semantic combinations remain unverified.

---

# 12. Before/after consolidation of all 15 UX-SEC findings

| Finding | Earlier risk | Consolidated normative correction | Phase 1.4.2 status |
|---|---|---|---|
| UX-SEC-01 | Light tertiary text fails normal-text AA | Proposed light tertiary #626A65; complete theme/state contrast matrix required | SPECIFIED |
| UX-SEC-02 | Free-form unverified FLY ID input | Remove FLY ID from current contact form and current production UI | SPECIFIED |
| UX-SEC-03 | Generic LOCAL ONLY overstates locality | Evidence-gated per operation/data class/platform; use unverified wording otherwise | BLOCKED — evidence absent |
| UX-SEC-04 | Local device row confused with trust | Rename to Local app installations; prohibit trust/pair/revoke labels | SPECIFIED |
| UX-SEC-05 | Security Center implies implemented protections | Reframe as Security capabilities; persistent disclosure; no score/toggles | SPECIFIED |
| UX-SEC-06 | Mixed state vocabulary | Use one six-state capability taxonomy; planning/lifecycle/operation states are separate | SPECIFIED |
| UX-SEC-07 | Demo provenance incomplete | Route matrix, persistent label and one-time screen-reader announcement | SPECIFIED |
| UX-SEC-08 | Generic network status | Remove current generic badge; require named signal and freshness semantics later | SPECIFIED |
| UX-SEC-09 | FLY ID share lifecycle vague | No current copy/share; future disclosure and correlation review mandatory | SPECIFIED |
| UX-SEC-10 | Accessibility plan incomplete | Component × state × platform acceptance matrix | SPECIFIED; execution NOT_RUN |
| UX-SEC-11 | Proposal called approved terminology | Use “Proposed terminology baseline — pending owner approval” | SPECIFIED |
| UX-SEC-12 | Reset scope vague | Per-domain data inventory, exact consequences and external-copy limitations | BLOCKED — inventory absent |
| UX-SEC-13 | Chats/Calls may look operational | Informational-only unavailable states; remove operational affordances | SPECIFIED |
| UX-SEC-14 | Disabled reasons inconsistent | Shared adjacent and programmatic reason pattern | SPECIFIED |
| UX-SEC-15 | Identifier anti-confusion incomplete | Future-only identifier-format specification; no current identifier UI | FUTURE GATE |

The 15 corrections remain specifications, not implementation evidence. No finding is IMPLEMENTED, VERIFIED or CLOSED.

---

# 13. Product-owner approval register — DA-01…DA-16

All DA decisions remain PENDING. Options below are provided to make the trade-offs explicit. The recommended baseline is not an approval.

| Decision | Recommended baseline | Alternative(s) | Consequence / trade-off | State |
|---|---|---|---|---|
| DA-01 Visual direction | Obsidian / Porcelain / Graphite | Softer neutral; high-contrast monochrome | Restrained neutral system supports clarity; a stronger brand palette increases visual identity but adds state/contrast risk | PENDING |
| DA-02 Accent | Teal #176B63 light / #79BDB2 dark | Neutral-only accent; another contrast-verified accent | Teal improves action hierarchy; neutral-only is quieter but weakens primary-action distinction | PENDING |
| DA-03 Theme | System default with light and dark | Light-only; dark-only | System default respects platform preference; single theme reduces design/testing scope | PENDING |
| DA-04 Typography | Native system stacks | Licensed brand typeface | Native stacks improve platform fit and localization; custom font increases licensing, glyph and rendering work | PENDING |
| DA-05 Navigation | Chats, Calls, Contacts, Settings | Different order or reduced tabs | Four-tab structure preserves product information architecture; change affects route and user-journey model | PENDING |
| DA-06 Responsive behavior | Bottom tabs on compact, rail/sidebar on wide screens | Bottom tabs at all widths; sidebar at all widths | Adaptive navigation uses space better; fixed navigation is simpler but less optimal across form factors | PENDING |
| DA-07 Accessibility target | WCAG 2.2 AA Web + equivalent native | Higher target or narrower supported platform matrix | Stronger target requires additional testing and design time; reducing target risks excluding users | PENDING |
| DA-08 Non-crypto disclosure | Persistent full notice in onboarding and shell | Full notice in onboarding plus compact persistent shell entry | Full persistent notice maximizes clarity but uses screen space; compact entry must not hide the core warning | PENDING |
| DA-09 Security Center | Security capabilities / availability information; no score | Omit the destination until capabilities exist | Informational page educates users; omission reduces navigation complexity but hides roadmap explanation | PENDING |
| DA-10 Devices screen | Local app installations | Omit until real device lifecycle exists | Renaming preserves local records without trust implications; omission further reduces confusion | PENDING |
| DA-11 Contact form | Alias and optional local note only | Defer contact creation entirely | Minimal local contacts preserve organization; deferral avoids storing even local contact data | PENDING |
| DA-12 Status taxonomy | Canonical six-state model | Show only plain-language status without badges | Taxonomy supports consistency; plain copy-only design minimizes status UI but requires careful repeated wording | PENDING |
| DA-13 Local-only wording | Evidence-backed “Stored locally” only | Never display locality status in MVP | Evidence-gated wording is informative; omission avoids claims until full data-flow evidence exists | PENDING |
| DA-14 Demo state | Persistent route and record provenance | No synthetic data anywhere | Demo labels allow preview; no demo data reduces risk of confusion but reduces demonstration value | PENDING |
| DA-15 Network state | Remove generic network status | Show named, source-backed signal only | Removal avoids false semantics; named signal can help troubleshooting only after actual instrumentation exists | PENDING |
| DA-16 Local reset | Per-domain deletion model and precise confirmation | Defer reset UI until inventory exists | Explicit reset protects expectations; deferral avoids false deletion promises but delays user control | PENDING |

Approval record format for every decision:

- Decision ID.
- APPROVED / REJECTED / REVISE.
- Exact selected option or change.
- Product-owner approver.
- Date.
- Rationale.
- Specification version.

No silence, continued work or tool-generated artifact constitutes approval.

---

# 14. Remediation approval register — RA-01…RA-16

All RA decisions remain PENDING. These decisions approve or revise the specific remediation proposals from Phase 1.4.1; they are separate from the broader DA design decisions.

| Decision | Proposed remediation | Alternative / consequence | State |
|---|---|---|---|
| RA-01 | Replace light tertiary #777E7A with #626A65 | Use higher-contrast neutral; requires full matrix either way | PENDING |
| RA-02 | Remove FLY ID from current contact form | Defer contact form entirely; keeping any unverified ID field retains identity-confusion risk | PENDING |
| RA-03 | Evidence-backed LOCAL ONLY policy | Prohibit locality badge until all data-flow evidence exists; safest short-term option | PENDING |
| RA-04 | Rename Devices to Local app installations | Remove the screen until local records have a justified use; retaining “Devices” risks trust confusion | PENDING |
| RA-05 | Reframe Security Center as Security capabilities | Remove screen until real security capabilities exist; retaining a dashboard risks deceptive security UX | PENDING |
| RA-06 | Persistent non-crypto disclosure | Full persistent text vs compact shell indicator; compact version must not conceal the full limitation | PENDING |
| RA-07 | Canonical six-state taxonomy | Plain-language status only; must still preserve exact semantics and fail-closed unknown handling | PENDING |
| RA-08 | Demo route and announcement policy | Ban synthetic data; otherwise every demo route needs persistent visual and accessible provenance | PENDING |
| RA-09 | Remove generic network state | Add a named, source-backed indicator only after instrumentation and freshness semantics exist | PENDING |
| RA-10 | No current FLY ID sharing; future risk disclosure | Keep future feature deferred until correlation and identifier lifecycle are reviewed | PENDING |
| RA-11 | Component/state/platform accessibility matrix | Narrow platform scope only with explicit product decision; runtime tests remain mandatory | PENDING |
| RA-12 | Rename “Approved terminology” to “Proposed terminology baseline” | No alternative may imply owner approval before sign-off | PENDING |
| RA-13 | Per-domain local deletion model | Defer reset UI until data inventory and failure semantics are complete | PENDING |
| RA-14 | Chats/Calls informational-only surfaces | Omit either destination until real operation exists; operational-looking controls are prohibited | PENDING |
| RA-15 | Shared disabled-action reason pattern | Remove unavailable controls rather than disable them; when shown, adjacent accessible reason is mandatory | PENDING |
| RA-16 | Future identifier anti-confusion requirements | No current identifier UI; future format needs normalization, homoglyph, bidi, truncation and copy test vectors | PENDING |

RA approval must be recorded separately from DA approval. Approving a remediation specification does not authorize its implementation.

---

# 15. UX security and implementation gates

## 15.1 UX security gates

| Gate | Requirement | Phase 1.4.2 status |
|---|---|---|
| UXG-01 | Persistent non-crypto disclosure approved | BLOCKED — owner approval pending |
| UXG-02 | No production FLY ID entry/display in shell | SPECIFIED; implementation not authorized |
| UXG-03 | Device record/trust distinction explicit | SPECIFIED; implementation review pending |
| UXG-04 | Security capabilities page no longer implies active protections | SPECIFIED; owner approval pending |
| UXG-05 | LOCAL ONLY claims tied to verified data flow | BLOCKED — evidence absent |
| UXG-06 | Complete contrast matrix meets target | BLOCKED — rendered matrix not performed |
| UXG-07 | Canonical capability taxonomy approved | BLOCKED — owner approval pending |
| UXG-08 | Demo routes and announcements enumerated | SPECIFIED; runtime checks NOT_RUN |
| UXG-09 | Network signal semantics defined or generic status removed | SPECIFIED; implementation not authorized |
| UXG-10 | Local deletion inventory and confirmation matrix | BLOCKED — implementation inventory absent |
| UXG-11 | Component/state/platform accessibility plan | SPECIFIED; execution NOT_RUN |
| UXG-12 | DA-01…DA-16 owner sign-off | BLOCKED — 16 decisions pending |

A BLOCKED status means evidence or approval is absent. It is not a test failure or test pass.

## 15.2 Remediation gates

| Gate | Requirement | Consolidated state |
|---|---|---|
| RMG-01 | UX-SEC-01 contrast token and complete matrix | SPECIFIED; rendered verification pending |
| RMG-02 | UX-SEC-02 remove FLY ID field | SPECIFIED |
| RMG-03 | UX-SEC-03 data-flow proof for LOCAL ONLY | BLOCKED — evidence absent |
| RMG-04 | UX-SEC-04 local installation terminology | SPECIFIED |
| RMG-05 | UX-SEC-05 non-crypto disclosure and Security capabilities | SPECIFIED |
| RMG-06 | UX-SEC-06 canonical taxonomy | SPECIFIED |
| RMG-07 | UX-SEC-07 demo route and announcement matrix | SPECIFIED |
| RMG-08 | UX-SEC-08 network signal semantics | SPECIFIED |
| RMG-09 | UX-SEC-09 no current FLY ID sharing | SPECIFIED |
| RMG-10 | UX-SEC-10 accessibility acceptance matrix | SPECIFIED; execution NOT_RUN |
| RMG-11 | UX-SEC-11 terminology governance | SPECIFIED |
| RMG-12 | UX-SEC-12 data deletion inventory | BLOCKED — inventory absent |
| RMG-13 | UX-SEC-13 Chats/Calls informational-only behavior | SPECIFIED |
| RMG-14 | UX-SEC-14 disabled-action pattern | SPECIFIED |
| RMG-15 | UX-SEC-15 future identifier format | FUTURE GATE |

No gate is marked VERIFIED or CLOSED by this document.

---

# 16. Platform adaptation requirements

## 16.1 Android

- Use system-native typography and interaction conventions.
- Define status-bar, navigation-bar, keyboard and edge-to-edge behavior.
- Respect Android font scaling and TalkBack.
- Aim for 48 dp touch targets where practical.
- No microphone, camera, contacts, nearby-device, notification or location permission during the non-crypto shell.
- Do not rely on FLAG_SECURE as a universal anti-capture guarantee.
- No Android Keystore or biometric security claim until the relevant feature is implemented and independently assessed.
- Define app-switcher and background behavior for any future sensitive screen.

## 16.2 iOS

- Use native text and control behavior.
- Respect Dynamic Type, VoiceOver, focus and reduced motion.
- Request permissions only when a separately approved feature needs them.
- Do not claim Keychain or Secure Enclave protection for secrets that do not exist in this phase.
- Define app-switcher snapshot and background behavior before real sensitive content is introduced.

## 16.3 Web

- Treat Web as a separate threat model.
- Do not assume hardware-backed key storage parity with native platforms.
- Keyboard and browser history are first-class requirements.
- Address zoom/reflow, screen readers, focus, browser storage and extension/XSS risks.
- No sensitive data in URL, localStorage or sessionStorage.
- No claim of cryptographic or anonymity parity with native platforms.

## 16.4 Desktop

- Define window resizing, keyboard navigation, focus, menu and multi-window behavior.
- Do not expose sensitive content in desktop notifications or app-switcher previews by default in any future sensitive workflow.
- Treat desktop storage and operating-system backup as platform-specific evidence requirements.
- No desktop implementation is authorized by this phase.

---

# 17. MagicPath design handoff and governance

## 17.1 MagicPath artifact

A private MagicPath project named **FLY SECRET Phase 1.4.2** was created for this phase.

The canvas contains a design-specification and decision-reference board covering:

- product posture;
- information architecture;
- identity and privacy;
- capability states;
- proposed design system;
- accessibility and approvals;
- required non-crypto disclosure;
- DA/RA pending status;
- non-prototype and security governance constraints.

The board is not an application screen, clickable prototype, production UI, approved visual design or implementation source.

## 17.2 Design-to-GitHub contract for subsequent phases

Every future design task must produce a traceable handoff:

- Design brief and intended user outcome.
- Design decision record and approval state.
- Screen specifications and route IDs.
- Component inventory and state matrix.
- Design tokens and semantic mappings.
- Responsive rules for supported breakpoints.
- Accessibility criteria.
- Asset manifest with licensing/provenance.
- Mapping from MagicPath components to repository paths.
- QA checklist and evidence links.
- MagicPath project/component/revision references where relevant.

## 17.3 Versioning and implementation flow

When implementation is separately authorized:

1. Confirm the approved MagicPath component and revision.
2. Confirm the target repository, branch and existing source component.
3. Inspect the implementation baseline and dependency compatibility.
4. Create a feature branch from the approved base.
5. Transfer design assets and tokens with explicit source/revision mapping.
6. Implement only the agreed scope.
7. Review diffs for unrelated changes, secrets and dependency drift.
8. Run the required static, unit, visual and platform checks.
9. Compare screenshots against the approved reference at the agreed viewports.
10. Open a PR only when explicitly authorized.
11. Merge only after review and release-gate approval.

The live GitHub repository is the source of truth for code. MagicPath is the source of truth for the approved design artifact. Neither system silently overwrites the other.

## 17.4 Visual QA evidence

Later visual QA must record:

- exact commit SHA;
- exact MagicPath component/revision;
- browser/OS/device and viewport;
- reference and implementation screenshots;
- overlays or side-by-side comparisons;
- layout, typography, spacing, contrast and state mismatches;
- keyboard and assistive technology checks;
- defects, severity, owner and retest result.

A verbal review or a generated preview alone is not a visual QA pass.

---

# 18. Security architecture constraints preserved

- No custom cryptography.
- No production crypto protocol or dependency is selected.
- D-01 remains OPEN.
- No plaintext, private identity/device keys, ratchet secrets, Recovery Kit secrets or media decryption keys may be exposed to backend systems.
- E2EE is not claimed.
- Push, if introduced later, is opaque wake-up only.
- WebRTC transport encryption alone is not proof of application-level media E2EE.
- Multi-device identity, linking, revocation and key lifecycle remain future architecture requirements.
- FLY ID, Device Record ID, cryptographic Device Identity, Transport ID, Session ID and Session Keys remain separate.
- Root/jailbreak detection is not a security guarantee.
- Screen-capture restrictions are not universal guarantees.
- No secrets in logs, URLs, clipboard, localStorage/sessionStorage or push payloads.
- No broad anonymity or metadata-protection claim without a defined and evidenced data-flow.

**TOR MODE is excluded** from product requirements, architecture, UI, configuration, tests and roadmap.

**Decentralization is future-only** and is not part of the current MVP or implementation plan.

---

# 19. Red-Team and QA status

The existing Red-Team catalog is not modified by this phase.

All 27 unique scenario IDs remain NOT_RUN:

- PRIV-01, PRIV-02, PRIV-03, PRIV-04
- ID-01, ID-02, ID-03, ID-04
- RELAY-01, RELAY-02
- CRYPTO-01, CRYPTO-02, CRYPTO-03
- REC-01, REC-02, REC-03
- FILE-01, FILE-02, FILE-03
- CALL-01, CALL-02, CALL-03
- DEC-01, DEC-02, DEC-03
- SUPPLY-01
- LOG-01

DEC scenarios remain future-only. CALL-03 remains the ICE/IP disclosure scenario in the current catalog; any later taxonomy cleanup must be a separate, reviewed change and must preserve all unique scenario IDs.

No scenario was run, passed, failed or waived in Phase 1.4.2.

Accessibility, browser, Android, iOS, Web, Desktop, runtime, visual and contrast-matrix execution remain NOT_RUN. The contrast numbers in this specification are token calculations, not rendered UI evidence.

---

# 20. Phase 1.4.2 execution record

## Performed

- Retrieved the Phase 1.2, 1.3, 1.4 and 1.4.1 documents from the Phase 1.4.1 branch.
- Verified the Phase 1.4.1 base commit and branch relationship.
- Compared Phase 1.4.1 against Phase 1.4: one added document, zero deletions.
- Compared the Phase 1.4.1 working branch against main: 17 commits ahead, 0 behind; the 23 changed files are additions, with no modifications or deletions.
- Retrieved the Red-Team catalog and confirmed 27 unique scenario IDs.
- Consolidated product scope, routes, screen labels, identity semantics, privacy claims, state taxonomy, design tokens, component contracts, accessibility requirements and platform notes.
- Resolved the planning-label versus UI-capability-state ambiguity.
- Recorded DA-01…DA-16 and RA-01…RA-16 as PENDING, without inferring approval.
- Created a dedicated private MagicPath project and a design-specification/decision-reference board.
- Created a dedicated GitHub branch from Phase 1.4.1.

## Not performed

- No application source code written or changed.
- No application prototype or clickable product screens created.
- No MagicPath production UI component implemented.
- No Figma file created.
- No cryptographic spike or dependency selection.
- No build, runtime or deployment.
- No browser/Playwright test.
- No Android/iOS/Web/Desktop test.
- No accessibility or assistive-technology execution.
- No rendered visual QA.
- No data-flow inspection of a running application.
- No Red-Team or penetration test.
- No main modification.
- No Pull Request.

---

# 21. Git verification requirements

Before declaring this phase delivered:

1. Fetch this document from the Phase 1.4.2 branch and record its blob SHA.
2. Compare Phase 1.4.2 against Phase 1.4.1 and confirm exactly one new file and zero deletions.
3. Compare Phase 1.4.2 against main and confirm all changes are additions only.
4. Fetch the Red-Team catalog from Phase 1.4.2 and Phase 1.4.1 and confirm identical blob SHA.
5. Confirm 27 unique scenario IDs remain and all are NOT_RUN.
6. Confirm no PR was created and main was not changed.

---

# 22. Formal Phase 1.4.2 disposition

| Item | Result |
|---|---|
| Phase 1.2 product architecture consolidated | COMPLETE |
| Phase 1.3 design system consolidated | COMPLETE |
| Phase 1.4 findings integrated | COMPLETE |
| Phase 1.4.1 remediations integrated | COMPLETE |
| Contradiction register and precedence | COMPLETE |
| Screen and route baseline | COMPLETE |
| Identity and privacy semantics | COMPLETE |
| Canonical UI capability-state taxonomy | COMPLETE |
| Design token proposal | COMPLETE — owner approval pending |
| Component and interaction contract | COMPLETE |
| Russian and English security disclosure | COMPLETE |
| Accessibility acceptance matrix | COMPLETE — execution NOT_RUN |
| DA-01…DA-16 | 16 PENDING |
| RA-01…RA-16 | 16 PENDING |
| UX security gates | BLOCKED where evidence/approval is absent |
| UX-SEC findings | SPECIFIED only; none implemented/verified/closed |
| MagicPath specification board | CREATED |
| Application implementation | NOT AUTHORIZED |
| Application prototype | NOT CREATED |
| D-01 | OPEN |
| Red-Team | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: PHASE 1.4.2 CONSOLIDATED UX SPECIFICATION COMPLETE. THE DESIGN BASELINE IS READY FOR PRODUCT-OWNER REVIEW, NOT APPROVED FOR IMPLEMENTATION. ALL 32 DESIGN/REMEDIATION DECISIONS REMAIN PENDING. REQUIRED EVIDENCE, RUNTIME QA AND SECURITY GATES REMAIN OPEN.**