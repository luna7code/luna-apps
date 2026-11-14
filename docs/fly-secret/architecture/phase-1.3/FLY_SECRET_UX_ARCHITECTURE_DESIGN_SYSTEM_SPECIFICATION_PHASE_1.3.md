# FLY SECRET — Phase 1.3
## UX Architecture & Design System Specification

**Status:** UX / DESIGN SYSTEM SPECIFICATION COMPLETE — NO IMPLEMENTATION AUTHORIZED  
**Baseline:** Phase 1.2 — Non-Crypto MVP Product Architecture  
**Base branch:** `security/fly-secret-non-crypto-mvp-product-architecture-1.2`  
**Working branch:** `security/fly-secret-ux-design-system-specification-1.3`  
**Repository:** `luna7code/luna-apps`  
**Date:** 2026-11-14

> This is a design and interaction specification. It is not a coded design system, visual prototype, Figma file, app implementation, usability test or security certification. Palette and component choices are proposed design baselines requiring product-owner approval before implementation.

---

# 1. Design mission and UX principles

## 1.1 Product experience

FLY SECRET should feel like a precise, calm, premium communications product. The visual system should prioritize legibility, predictable interaction, clear privacy explanations and low cognitive load over decorative “cybersecurity” imagery.

The UX must communicate three distinct truths:
1. what the product intends to provide;
2. what the current build actually provides;
3. what is deferred pending architecture and security approval.

A polished visual design must never make an unavailable security capability appear implemented.

## 1.2 Experience principles

- **Clarity over theater:** no fake encryption animations, scanning effects, terminal motifs or “military-grade” claims.
- **Quiet confidence:** restrained surfaces, typography-led hierarchy, purposeful whitespace.
- **Honest capability states:** Available, Local-only, Demo, Disabled and Deferred are visibly distinct.
- **Consistent navigation:** Chats, Calls, Contacts, Settings remain the four primary destinations.
- **Local-first defaults:** do not prompt for permissions or upload data merely to complete onboarding.
- **Progressive disclosure:** show essential information first; explain identity, device and privacy details on demand.
- **Accessible by construction:** semantics, contrast, text scaling, keyboard, screen reader and reduced motion are part of component definitions.
- **Platform respect:** shared visual language, native interaction conventions.
- **No security by color:** status always includes text/icon/shape; never color alone.
- **No deceptive affordances:** a disabled or deferred action must not look like a working send, call, verify, recovery or encryption action.

## 1.3 Design governance

Every screen and component must be reviewed against:
- user purpose;
- actual feature availability;
- data displayed and stored;
- permission requirement;
- accessibility;
- error and empty states;
- misuse/deception risk;
- platform-specific behavior.

Any future design change that implies a new trust state, cryptographic status, account state or network capability requires security/product review, not only visual approval.

---

# 2. Visual direction

## 2.1 Proposed visual character

**Direction:** Obsidian / Porcelain / Graphite — minimal, editorial, neutral.

- Dark surfaces: near-black graphite, not pure black everywhere.
- Light surfaces: warm-neutral white, not harsh white on every panel.
- Text: high-contrast neutral ink.
- Accent: restrained cool teal used for primary action and selected navigation, not as a “secure” indicator.
- Warning and destructive colors are reserved for actual warning/destructive semantics.
- Avoid neon green, electric blue, glowing borders, gradients that imitate encryption, padlock wallpaper, and ornamental circuit-board patterns.

This is a proposal for owner review, not an already approved brand palette.

## 2.2 Color tokens

Color values are initial design tokens. They must be contrast-tested against actual text sizes and states before implementation.

### Light theme

| Token | Proposed value | Use |
|---|---|---|
| color.canvas | #F7F7F5 | App background |
| color.surface | #FFFFFF | Primary cards and sheets |
| color.surface.subtle | #F0F1EF | Secondary grouped surface |
| color.surface.raised | #FFFFFF | Dialogs and popovers |
| color.text.primary | #171918 | Primary text |
| color.text.secondary | #5E6461 | Secondary text |
| color.text.tertiary | #777E7A | Metadata; contrast must be checked |
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
| color.focus | #175FC4 | Focus ring; not brand accent |

### Dark theme

| Token | Proposed value | Use |
|---|---|---|
| color.canvas | #111312 | App background |
| color.surface | #191C1A | Primary cards and sheets |
| color.surface.subtle | #202522 | Secondary grouped surface |
| color.surface.raised | #252A27 | Dialogs and popovers |
| color.text.primary | #F2F4F1 | Primary text |
| color.text.secondary | #B7BFBA | Secondary text |
| color.text.tertiary | #929B95 | Metadata; contrast must be checked |
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
| color.focus | #8AB4F8 | Focus ring |

### Semantic color rules

- Accent means “primary interactive emphasis”, never “encrypted”.
- Success means a specific operation completed, never “secure”.
- Warning means user attention is required.
- Danger means destructive action or error.
- Neutral is the default for deferred, unknown and informational states.
- Verified/cryptographic trust colors are not defined because verification is not implemented in this phase.
- All colors require WCAG contrast verification; tertiary text may not be used for essential content until verified.

## 2.3 Color usage ratios

Suggested starting balance:
- 75–85% neutral canvas/surfaces;
- 10–20% text and structural boundaries;
- no more than 5% accent and semantic colors.

Do not add color merely to fill empty space. Avoid rainbow status rows and gradients.

## 2.4 Theme behavior

- Light and dark themes are both specified.
- System theme is the default proposal.
- Manual override may be offered under Settings → Appearance.
- Theme changes must not alter status semantics.
- Theme preference is non-sensitive local preference.
- No forced theme transition animation.

---

# 3. Typography

## 3.1 Font strategy

Use platform-native system sans-serif stacks to preserve rendering quality, localization and accessibility. A branded typeface may be considered only after licensing, glyph coverage and performance review.

Proposed stacks:
- Android: Roboto / system sans-serif.
- iOS: SF Pro / system sans-serif.
- Web: system UI stack with appropriate fallback for Cyrillic.
- Desktop: native system sans-serif.

No font is bundled by this phase.

## 3.2 Type scale

| Token | Size / line height | Weight | Intended use |
|---|---:|---|---|
| type.display | 32 / 40 | Semibold | Onboarding hero, sparingly |
| type.h1 | 28 / 36 | Semibold | Main screen title |
| type.h2 | 22 / 30 | Semibold | Section title |
| type.h3 | 18 / 26 | Semibold | Card/row group title |
| type.body.large | 17 / 25 | Regular | Introductory content |
| type.body | 16 / 24 | Regular | Main reading text |
| type.body.small | 14 / 20 | Regular | Supporting text |
| type.label | 14 / 20 | Medium | Buttons and labels |
| type.caption | 12 / 18 | Regular | Metadata only |
| type.numeric | 14–16 / 20–24 | Medium, tabular | Counts and dates |

Rules:
- Never use caption size for instructions, warnings or security disclosures.
- Avoid long all-caps text.
- Use sentence case for labels.
- Do not rely on thin font weights on dark surfaces.
- IDs and opaque identifiers use a monospaced style only when that improves character distinction; they remain selectable/copyable only after explicit authorization.
- No truncation of identity or safety explanations without an accessible expansion.

## 3.3 Dynamic text

- Respect OS text scaling and browser zoom.
- Layouts must reflow without clipping at 200% text scale/zoom.
- Avoid fixed-height text containers.
- Long Russian labels must wrap.
- Numeric values use tabular numerals where useful.
- Line length target for long-form explanation: approximately 55–80 characters on large screens; shorter on mobile.

---

# 4. Spacing, grid and shape

## 4.1 Base spacing scale

Use a 4 pt base scale:

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

No arbitrary one-off spacing unless documented for platform-native controls.

## 4.2 Responsive layout grid

| Viewport | Layout | Content width / columns |
|---|---|---|
| 320–359 px | Compact mobile | Single column, 16 px side inset |
| 360–599 px | Mobile | Single column, 16–20 px inset |
| 600–839 px | Large mobile / small tablet | Single column or 2-column settings, 24 px inset |
| 840–1199 px | Tablet / compact desktop | Navigation rail + content, max 2 columns |
| 1200–1599 px | Desktop | Sidebar + centered content, max content 1120 px |
| 1600+ px | Wide desktop | Sidebar + constrained content, max content 1200 px |

These are design breakpoints, not implemented CSS or measured platform support.

## 4.3 Touch and pointer targets

- Minimum target: 44 × 44 CSS px/pt as a cross-platform design target.
- Android-native controls should respect platform touch target guidance; aim for 48 dp where practical.
- Separate adjacent destructive and non-destructive actions.
- Hover is supplementary, never the only way to discover an action.
- Web focus ring must be visible independently of hover.

## 4.4 Shape tokens

| Token | Radius | Use |
|---|---:|---|
| radius.xs | 6 | Small chips |
| radius.sm | 10 | Inputs, compact buttons |
| radius.md | 14 | Cards and rows |
| radius.lg | 20 | Sheets and larger panels |
| radius.xl | 28 | Onboarding feature panels |
| radius.pill | 999 | Pills and segmented controls |

Use restrained elevation. Prefer borders and tonal surface shifts over multiple floating shadows.

## 4.5 Elevation

- Level 0: canvas.
- Level 1: surface contrast/border.
- Level 2: modal/sheet elevation.
- Level 3: transient popover only.

Avoid nested card-in-card-in-card compositions.

---

# 5. Iconography and illustration

## 5.1 Icon rules

- Use one consistent outline icon family.
- Standard sizes: 16, 20, 24; 28 only for large empty-state illustrations.
- Stroke weight and optical alignment must be consistent.
- Use labels for primary navigation.
- Do not invent lock/shield icons as proof of security.
- Do not use a checkmark for identity verification in the non-crypto MVP.
- Do not use a green dot to imply that a user is online or trusted.

## 5.2 Illustrations

- Prefer simple geometric or editorial illustrations.
- Avoid hooded-hacker clichés, circuit brains, glowing shields and fake cryptographic diagrams.
- Illustrations are optional; empty states should remain useful without them.
- Decorative illustrations must not contain text required to understand a workflow.

---

# 6. Motion and feedback

## 6.1 Motion principles

- Motion explains hierarchy, continuity or state change.
- No continuous ambient animation.
- No simulated scanning, encryption, key generation or network negotiation.
- Avoid parallax and large spring overshoot.
- Reduced-motion preference disables non-essential transitions.

## 6.2 Timing proposals

| Motion | Duration |
|---|---:|
| Press feedback | 80–120 ms |
| Small state transition | 120–180 ms |
| Sheet/dialog transition | 180–240 ms |
| Page transition | 180–260 ms |
| Toast appearance | 140–180 ms |

These are proposed design targets, not benchmarked values.

## 6.3 Feedback channels

- Inline feedback for field validation.
- Toast/snackbar for brief reversible local operations.
- Dialog for destructive or irreversible local actions.
- Persistent banner for app-wide unavailable/degraded capability.
- Haptic feedback only for meaningful user actions and only where platform conventions support it.
- Never rely on sound, vibration or color alone.

---

# 7. Component system

## 7.1 AppShell

An application-level layout component.

Anatomy:
- platform navigation container;
- screen content region;
- global status/notice region;
- optional modal host.

States:
- normal;
- first-run;
- offline/unknown (only if a real network state is available);
- recoverable local error.

Invariants:
- no global “secure” badge;
- no hidden feature activation;
- no sensitive data in route parameters.

## 7.2 PrimaryNavigation

Destinations in fixed order:
1. Chats
2. Calls
3. Contacts
4. Settings

Visual anatomy:
- icon;
- persistent text label;
- selected state using accent surface/indicator plus selected semantics;
- optional non-sensitive count only if a real local count exists.

States:
- selected;
- unselected;
- focused;
- pressed;
- disabled only when the route is genuinely unavailable.

Do not show unread counts from fake or synthetic messages outside a clearly marked demo.

## 7.3 ScreenHeader

Anatomy:
- title;
- optional supporting text;
- optional back action;
- optional non-destructive contextual action.

No action may be shown without a defined behavior.

## 7.4 Buttons

Variants:
- Primary: one dominant action per screen region.
- Secondary: alternative action.
- Tertiary: low-emphasis action.
- Destructive: irreversible local operation only.
- Icon button: only with accessible name and adequate target.

States:
- default;
- hover (pointer platforms);
- pressed;
- focus-visible;
- disabled;
- loading only for an actual operation.

Disabled actions require an explanation nearby or an accessible reason. Do not display a disabled “Send” button in a way that suggests a live messaging capability unless the whole surface is explicitly labeled demo.

## 7.5 Input fields

Anatomy:
- persistent label;
- input;
- optional helper text;
- validation/error message.

Rules:
- never rely on placeholder as label;
- validation is specific and actionable;
- no auto-submit on partial identity fields;
- sensitive input masking is not a substitute for secure storage;
- no password, PIN, private key or recovery-secret field is included in this phase.

## 7.6 Cards and grouped rows

- Use cards for distinct tasks, not every setting row.
- Group related preferences in a flat list.
- Avoid unnecessary borders around every item.
- Use consistent row height and hit area.
- Keep secondary metadata visually subordinate but readable.

## 7.7 StatusBadge

Allowed states:
- LOCAL ONLY
- DEMO
- UNAVAILABLE
- DEFERRED
- INFORMATION

No “E2EE”, “VERIFIED”, “TRUSTED” or “SECURE” badge is defined for this phase.

Badge includes text and optional icon. Color is secondary.

## 7.8 EmptyState

Anatomy:
- concise title;
- explanation;
- optional illustration;
- one relevant next action if one exists.

Do not invent a future release date. Do not pressure the user to invite contacts to an unavailable service.

## 7.9 DisabledFeaturePanel

Anatomy:
- feature name;
- state label;
- short reason;
- safe alternative (e.g. view product explanation);
- no fake action.

It must explain that the feature is not available in the current build, not imply that the user's device or account is defective.

## 7.10 DemoDataBanner

Persistent label on all demo screens:
- “DEMO — synthetic data”
- visually distinct from production state;
- not dismissible on a route containing synthetic conversation/contact data;
- accessible announcement on route entry.

## 7.11 DeviceRecordListItem

Fields:
- user-defined label;
- coarse platform family;
- local/demo record state.

Must not display:
- hardware serial;
- IMEI/MAC;
- device fingerprint;
- cryptographic public key;
- “trusted” or “verified” status.

## 7.12 IdentifierDisclosurePanel

Explains:
- FLY ID is an identifier, not a password or private key;
- sharing it can enable correlation;
- current shell does not verify identity or establish encrypted sessions.

Any copy/share affordance remains disabled until actual ID lifecycle and disclosure UX are approved.

## 7.13 Dialog and bottom sheet

- Use dialogs for confirmation and short decisions.
- Use sheets for contextual details on mobile.
- Destructive dialog names exactly what is deleted.
- Default focus is on the safe action, not destructive confirmation.
- Escape/back dismisses non-destructive overlays.
- Focus is trapped and restored on Web.
- No dialog may imply remote account deletion when only local preferences are reset.

## 7.14 Toast and inline notice

- Toast for transient local result.
- Inline notice for persistent or field-specific explanation.
- Do not use toast for critical security warnings.
- Avoid stacking multiple notices.
- Do not include FLY ID or private data in notification text.

---

# 8. Screen-by-screen layout specifications

All layouts below are structural wireframe descriptions. They are not rendered mockups or implemented screens.

## 8.1 Onboarding

### ONB-01 Welcome

Mobile:
1. brand mark and wordmark;
2. headline and one-paragraph product purpose;
3. concise current capability disclosure;
4. primary Continue button;
5. language action.

Desktop:
- centered narrow content column;
- optional abstract illustration in a separate column;
- same order and disclosure.

No claims such as “fully anonymous”, “unbreakable” or “military-grade encryption”.

### ONB-02 Language

- title;
- language list with radio semantics;
- selected indicator and text;
- Continue action.

### ONB-03 Setup or quick preview

Two equal-weight choices:
- Set up local preferences;
- Quick preview.

Both are local-only. Neither creates an account or contacts a server.

### ONB-04 Local profile

- optional display alias field;
- optional avatar placeholder only if local avatar feature is later approved;
- FLY ID explanation;
- skip action;
- continue action.

Do not generate or display a production FLY ID in the non-crypto shell.

### ONB-05 Privacy briefing

- local data categories;
- no contact upload;
- no real messaging/calls in current build;
- storage/deletion limitations;
- acknowledgement checkbox only if legally/product required, not as a substitute for consent.

### ONB-06 Completion

- setup complete message;
- summary of local preferences;
- enter app action;
- no “identity secured” success state.

## 8.2 Chats

### CHAT-01 Conversation list

Mobile:
- header “Chats”;
- optional local/demo search field;
- conversation rows only in demo mode;
- empty state as default;
- persistent demo banner if synthetic data is shown.

Desktop:
- left list pane and right empty/detail pane only for demo mode;
- no two-pane layout that implies a live server session.

### CHAT-02 Demo conversation

- top bar with demo contact label;
- persistent DEMO banner;
- synthetic bubbles with neutral styling;
- no real composer/send affordance;
- no sent/delivered/read ticks;
- no encryption lock icon.

### CHAT-03 Conversation information

- synthetic/local label;
- local note if applicable;
- “No identity verification available” explanation;
- no key fingerprint or session metadata.

### CHAT-04 Search

- search only synthetic demo fixtures;
- empty result state;
- clear query action;
- no remote discovery.

## 8.3 Calls

### CALL-01 Calls landing

- title “Calls”;
- explanation that voice/video calling is not available;
- no call button;
- no call permission request;
- no fabricated history.

### CALL-02 Call details concept

- demo-only record if a fixture exists;
- label clearly synthetic;
- no IP, ICE, relay, codec or call encryption indicators.

### CALL-03 Call readiness explanation

- what a future call feature would need;
- transport security versus app-level E2EE distinction;
- no “ready to call” state.

## 8.4 Contacts

### CONTACT-01 Contacts list

- title;
- local search;
- local-only list;
- add local contact action only if separately implemented;
- empty state with no address-book import prompt.

### CONTACT-02 Contact detail

- display alias;
- optional local note;
- optional favorite state;
- FLY ID note clearly marked unverified;
- no Verified or Trusted badge.

### CONTACT-03 Add local contact

- alias;
- optional unverified FLY ID note;
- local note;
- save locally action;
- clear privacy explanation.

### CONTACT-04 Verification explanation

- concise explanation of future fingerprint/QR verification;
- status “Not available yet”;
- no generated fingerprint or fake QR.

## 8.5 Settings

### SET-01 Settings home

Grouped rows:
- Profile
- Devices
- Privacy Center
- Security Center
- Notifications
- Appearance & accessibility
- Language
- About

On large screens use a navigation list plus detail pane.

### SET-02 Profile

- local display alias;
- optional local avatar;
- FLY ID concept explanation;
- identifier disclosure warning;
- copy/share disabled until real lifecycle is approved.

### SET-03 Devices

- local/demo records;
- device label and platform family;
- explanation of local record deletion versus future cryptographic revocation;
- no trusted-device indicator.

### SET-04 Privacy Center

- current data inventory;
- local storage categories;
- permissions summary only if sourced from platform state;
- local reset control;
- deletion limitations.

### SET-05 Security Center

Capability rows:
- Identity verification — unavailable;
- Encrypted messaging — unavailable;
- Device trust — unavailable;
- Recovery — unavailable;
- App lock / biometrics — not implemented in Phase 1.3.

Use neutral state labels. No global security score.

### SET-06 Notifications

- preference design only;
- no push permission prompt;
- no remote token state;
- no message preview controls until notification threat model exists.

### SET-07 Appearance and accessibility

- system/light/dark theme choice;
- text size guidance;
- reduced motion;
- contrast and accessibility explanation.

### SET-08 Language

- Russian and English;
- current selection;
- localized labels.

### SET-09 About

- product name;
- app version/build only when actual implementation provides it;
- license and legal notices;
- support destination only when verified.

## 8.6 Global overlays and errors

### Global error
- concise human-readable message;
- safe retry only if an operation truly failed and is retryable;
- no raw stack trace or secret-bearing diagnostic.

### Local preference corruption
- explain local settings could not be read;
- offer reset of that namespace only;
- preserve unrelated user data where possible;
- no claim of crypto recovery.

### Offline state
- only show if a real connectivity signal is available;
- does not imply privacy or anonymity;
- do not use “private mode”.

---

# 9. Interaction and state specification

## 9.1 Standard control states

Every interactive component must define:
- default;
- hover (pointer devices);
- pressed;
- focus-visible;
- selected;
- disabled;
- loading, only for a real operation;
- success;
- validation error;
- unavailable/deferred.

## 9.2 Form validation

- Validate at field level where possible.
- Preserve user-entered non-sensitive input after recoverable errors.
- Do not expose account-existence signals.
- Do not auto-correct opaque identifiers silently.
- Do not copy values to clipboard automatically.
- No identity registration form is active in this phase.

## 9.3 Destructive actions

For local reset:
1. name exact data domains;
2. state what remains;
3. require explicit confirmation;
4. provide cancel as the safe default;
5. report actual local operation result;
6. avoid “secure erase” guarantee.

## 9.4 Navigation state

- Restore last selected tab when appropriate.
- Do not persist sensitive screen contents in route state.
- Back action closes modal before leaving parent screen.
- Direct Web route load must resolve to a safe shell state.
- No deep link may activate a disabled feature.

## 9.5 Feature state transitions

Allowed presentation states:
- SPECIFIED → DEMO_ONLY (only after demo is separately authorized);
- SPECIFIED → LOCAL_ONLY (only after local implementation is separately authorized);
- SPECIFIED → DISABLED;
- DEFERRED_SECURITY_GATE remains deferred until gate approval.

No visual transition from “deferred” to “available” may occur because of a configuration flag alone.

## 9.6 Loading and progress

- Loading indicators correspond to actual local work.
- Progress percentage is shown only when measurable.
- Never animate a fake key exchange, encrypted tunnel, identity scan or secure handshake.
- Indeterminate spinner must have accessible status text.

---

# 10. UX security rules

## 10.1 Identity semantics

The product UI must preserve the following distinct entities:

| Entity | UI meaning | Must never be presented as |
|---|---|---|
| FLY ID | Opaque user-facing identifier | Password, secret, bearer token, public key |
| Device Record ID | Local/administrative record reference | Cryptographic device identity or proof of trust |
| Cryptographic device identity | Future protocol-bound device identity | A local device label or hardware serial |
| Transport ID | Scoped routing/delivery identifier | Stable user identity or proof of anonymity |
| Session ID | Future protocol session reference | Session key or user identity |
| Session Keys | Future cryptographic secrets | UI identifier, API token, local preference |

No UI component may merge these concepts into a generic “account ID” or “device key”.

## 10.2 No false E2EE claims

Prohibited:
- lock icon next to a chat implying E2EE;
- “end-to-end encrypted” marketing copy;
- “message protected” status;
- verified contact checkmark;
- encrypted call label;
- security score based on missing/unimplemented controls;
- mock encryption progress animation.

Permitted:
- “Messaging is not available in this build.”
- “Identity verification is not available yet.”
- “This is synthetic demo data.”
- “A network connection does not prove end-to-end encryption.”

## 10.3 Permission minimization

- Ask only at the moment an authorized feature needs permission.
- Explain why before system prompt where platform guidance permits.
- No contacts, microphone, camera, photo library, notification or nearby-device prompts in this phase.
- Denial must not block unrelated local settings.

## 10.4 Privacy disclosure

- Explain local versus remote processing.
- State exactly what is stored.
- Do not use vague “we never see your data” claims without a verified system architecture.
- Do not promise anonymity based solely on no phone/email.
- Explain identifier correlation risks.
- No analytics consent prompt unless analytics exists and has approved purpose.

## 10.5 Clipboard and sharing

- No automatic clipboard writes.
- Warn before sharing an identifier.
- Never put private keys, recovery secrets or session keys on clipboard.
- Copy/share of FLY ID remains disabled until identity lifecycle and disclosure requirements are approved.

## 10.6 Screen capture and app switching

- Explain limitations honestly.
- Do not promise universal screenshot prevention.
- Native platform mitigations require separate implementation review.
- Web cannot promise native screenshot prevention.
- Avoid displaying sensitive content in this phase.

## 10.7 Error and notification privacy

- No secrets or user content in errors.
- No sensitive identifiers in push payloads.
- No message previews in notification design until threat model and user control are approved.
- No stable transport IDs in user-visible diagnostic panels.

---

# 11. Platform-specific UX adaptation

## 11.1 Android

Navigation:
- bottom navigation for compact phones;
- navigation rail for larger widths;
- system back closes overlays first, then navigates;
- preserve expected Android edge-to-edge behavior.

Controls:
- target 48 dp where practical;
- clear ripple/pressed state;
- TalkBack semantics;
- respect font scaling and display size;
- use native permission prompts only after separate feature approval.

Layout:
- account for status/navigation bars, cutouts, IME and gesture navigation;
- scroll forms above keyboard;
- no fixed bottom CTA obscured by IME.

Privacy:
- no sensitive content in recent-task snapshot in any future sensitive screen without explicit review;
- FLAG_SECURE is not a universal capture guarantee;
- no Keystore integration is implied by this visual specification.

## 11.2 iOS

Navigation:
- tab bar for primary four destinations;
- native navigation stack for details;
- sheets for short contextual tasks;
- preserve expected swipe-back behavior.

Controls:
- Dynamic Type;
- VoiceOver labels and hints;
- minimum 44 pt hit targets;
- system keyboard and focus behavior;
- respect Reduce Motion.

Layout:
- safe-area-aware top and bottom content;
- keyboard avoidance;
- no content hidden behind home indicator;
- large title behavior only where it improves hierarchy.

Privacy:
- no Keychain or Secure Enclave claim;
- no assumption that app-switcher snapshot suppression prevents all capture;
- no use of IDFV as identity proof.

## 11.3 Web

Navigation:
- persistent sidebar at desktop widths;
- compact responsive navigation at mobile widths;
- browser back/forward and direct routes;
- URL contains route only, never secrets or sensitive data.

Controls:
- keyboard-first operation;
- visible focus ring;
- semantic HTML roles in implementation;
- hover never required;
- support browser zoom to 200%.

Layout:
- responsive grid;
- avoid fixed viewport-height traps;
- modals trap/restore focus;
- support touch and pointer.

Privacy:
- no secrets in localStorage/sessionStorage/URL;
- Web is a distinct threat model;
- no native parity claims;
- CSP, dependency policy and origin controls belong to a separate Web security release gate.

## 11.4 Desktop (deferred)

If later authorized:
- keyboard shortcuts documented and discoverable;
- window resize and multi-window behavior reviewed;
- system title bar conventions respected;
- clipboard and notification privacy reviewed;
- OS credential store boundary defined separately.

---

# 12. Accessibility specification

## 12.1 Conformance target

Target WCAG 2.2 AA for Web surfaces and equivalent native accessibility outcomes for Android/iOS. This is a target, not a claim of conformance.

## 12.2 Visual accessibility

- text contrast 4.5:1 for normal text;
- 3:1 for large text;
- 3:1 for essential UI component boundaries and focus indicators where applicable;
- no color-only state;
- no text embedded in raster illustrations;
- scalable text and reflow.

## 12.3 Screen readers

- meaningful screen titles;
- logical heading hierarchy;
- persistent input labels;
- explicit selected navigation state;
- announce errors near affected controls;
- announce modal title and restore focus after close;
- demo and disabled states announced clearly;
- do not announce decorative icon names.

## 12.4 Keyboard and switch access

- all functions reachable by keyboard;
- logical tab order;
- no keyboard trap except modal focus containment with correct escape behavior;
- visible focus;
- no drag-only interaction;
- adequate target size and spacing.

## 12.5 Cognitive accessibility

- plain language;
- one primary action per screen region;
- avoid unexplained cryptographic jargon;
- explain unavailable states and next steps;
- stable navigation;
- no urgency or fear-based copy.

## 12.6 Motion and sensory

- respect reduced motion;
- no flashing;
- no color-only notification;
- no sound-only alert;
- haptics optional and non-essential.

---

# 13. Content design and terminology

## 13.1 Approved terminology

| Term | Definition / use |
|---|---|
| FLY ID | Opaque user-facing identifier, not secret |
| Device record | Local or administrative record of an installation |
| Device identity | Future cryptographic identity bound to protocol lifecycle |
| Transport ID | Scoped routing identifier |
| Session | Future protocol session |
| Session keys | Future cryptographic secret material |
| Local-only | Operation/data remains on this device for the stated feature |
| Demo | Synthetic data, not real communication |
| Not available yet | Capability is not implemented |
| Verified | Reserved for future protocol-backed verification only |

## 13.2 Prohibited copy patterns

- “100% anonymous”
- “unhackable”
- “military-grade”
- “fully secure”
- “all messages are encrypted”
- “your identity is verified” without a real verification protocol
- “device trusted” based only on local inventory
- “secure deletion” without a verified erasure model

## 13.3 Copy patterns

Prefer:
- “This feature is not available in the current build.”
- “This screen uses synthetic demo data.”
- “Your local display preferences are stored on this device.”
- “A FLY ID is an identifier, not a password.”
- “Sharing an identifier may make it easier to link activity.”
- “A network connection alone does not establish end-to-end encryption.”

---

# 14. Design tokens and handoff contract

## 14.1 Token namespaces

Future implementation should use semantic token namespaces:
- color.*
- type.*
- space.*
- radius.*
- elevation.*
- motion.*
- size.*
- border.*
- focus.*

Components must consume semantic tokens rather than hard-coded colors and spacing.

## 14.2 Token ownership

- Product/design owner approves visual direction and brand tokens.
- Accessibility reviewer approves contrast and interaction targets.
- Security reviewer approves security-related status vocabulary and disclosures.
- Platform owners approve native adaptations.
- Engineering maps tokens to implementation after separate authorization.

## 14.3 Handoff package required before implementation

A future implementation handoff should include:
- approved palette and typography;
- responsive screen specifications;
- component anatomy and state matrix;
- Russian and English copy;
- accessibility annotations;
- platform-specific exceptions;
- empty/error/loading/unavailable states;
- asset licensing and source;
- security copy review;
- acceptance criteria.

This document provides a textual specification but does not create visual assets or a Figma library.

---

# 15. UX QA and acceptance matrix

No visual QA, accessibility audit, browser test or native device test was run in Phase 1.3.

| Area | Future acceptance criterion | Status |
|---|---|---|
| Brand | Neutral premium visual direction approved | PENDING OWNER |
| Color | Light/dark semantic tokens contrast-tested | NOT_RUN |
| Typography | Russian/English glyph coverage and scaling checked | NOT_RUN |
| Navigation | Four tabs consistent on mobile and large screens | NOT_RUN |
| Onboarding | Current capability disclosure visible | NOT_RUN |
| Chats | No false send/delivery/read/E2EE affordance | NOT_RUN |
| Calls | No false call-ready state or permission prompt | NOT_RUN |
| Contacts | No false verified/trusted state | NOT_RUN |
| Settings | Privacy/security categories accurately described | NOT_RUN |
| Identity | FLY ID distinct from device and transport identifiers | NOT_RUN |
| Device rows | No hardware fingerprint or trust implication | NOT_RUN |
| Components | Default/pressed/focus/disabled/error states specified | NOT_RUN |
| Android | TalkBack, IME, back, safe areas, target size | NOT_RUN |
| iOS | VoiceOver, Dynamic Type, safe areas, Reduce Motion | NOT_RUN |
| Web | Keyboard, 200% zoom, direct routes, focus handling | NOT_RUN |
| Contrast | WCAG 2.2 AA target checked | NOT_RUN |
| Localization | Long Russian strings and pluralization checked | NOT_RUN |
| Motion | Reduced-motion behavior checked | NOT_RUN |
| Privacy copy | No unsupported anonymity/E2EE claims | NOT_RUN |

These UX acceptance criteria are separate from the 27 Red-Team security scenarios. They do not change the Red-Team status.

---

# 16. Product-owner review decisions

The following proposals require approval before implementation:

1. Approve Obsidian / Porcelain / Graphite visual direction or request a different brand direction.
2. Approve the proposed restrained teal accent and semantic color system.
3. Approve system-native typography strategy.
4. Approve four-tab navigation order: Chats, Calls, Contacts, Settings.
5. Approve responsive breakpoints and desktop sidebar behavior.
6. Approve light + dark theme support and system default.
7. Approve WCAG 2.2 AA target for Web and equivalent native outcomes.
8. Decide whether the first coded shell is Android+iOS only or includes Web.

Until sign-off, these remain proposed design decisions.

---

# 17. Explicit non-scope

Phase 1.3 does not deliver:
- coded design system or reusable components;
- Figma or rendered prototype;
- app screens or frontend source code;
- custom illustrations, icons or production assets;
- usability research or participant testing;
- accessibility conformance audit;
- browser, Playwright or native-device QA;
- real messaging, calls, files, push or recovery;
- E2EE, key generation, device trust or cryptographic verification;
- backend, API or database implementation;
- analytics, telemetry or crash reporting integration.

TOR MODE remains excluded from product, architecture, UI, configuration, tests and roadmap. Decentralization remains future-only.

---

# 18. Phase 1.3 execution record

**Performed:**
- fetched and reviewed Phase 1.2 baseline;
- verified four primary navigation destinations and identity separation requirements in baseline;
- created dedicated Phase 1.3 branch from Phase 1.2;
- authored visual direction, semantic palette, typography, spacing/grid, shape, iconography, motion, component specifications, screen-by-screen layouts, interaction states, security UX rules, Android/iOS/Web adaptations, accessibility target, terminology and future QA criteria.

**Not performed:**
- no code or application implementation;
- no prototype or Figma artifact;
- no image generation or production asset creation;
- no technical spike or build;
- no visual/browser/native QA;
- no accessibility audit;
- no tests or Red-Team execution;
- no backend, API or database work;
- no messaging, calls, files, push, recovery or E2EE;
- no crypto engine selection;
- no main branch modification;
- no Pull Request creation.

---

# 19. Formal Phase 1.3 disposition

| Item | Result |
|---|---|
| UX principles | COMPLETE |
| Visual direction | PROPOSED — OWNER APPROVAL REQUIRED |
| Palette and semantic tokens | SPECIFIED — CONTRAST QA NOT_RUN |
| Typography and spacing | SPECIFIED |
| Responsive grid | SPECIFIED |
| Component library specification | COMPLETE |
| Screen-by-screen layouts | COMPLETE |
| Interaction states | COMPLETE |
| Security UX rules | COMPLETE |
| Android adaptation | SPECIFIED |
| iOS adaptation | SPECIFIED |
| Web adaptation | SPECIFIED |
| Accessibility | TARGET SPECIFIED — AUDIT NOT_RUN |
| Product-owner approval | PENDING |
| Implementation | NOT AUTHORIZED |
| D-01 | OPEN |
| Red-Team | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: UX ARCHITECTURE AND DESIGN SYSTEM SPECIFIED. VISUAL DIRECTION AWAITS OWNER APPROVAL. NO CODE, PROTOTYPE OR QA WAS PERFORMED. ALL SECURITY AND SCOPE GATES FROM PHASES 1.0–1.2 REMAIN IN FORCE.**
