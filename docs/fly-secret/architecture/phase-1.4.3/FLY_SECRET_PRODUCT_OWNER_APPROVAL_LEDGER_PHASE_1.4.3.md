# FLY SECRET — Phase 1.4.3
## Product Owner Design Approval Ledger

**Status:** IN PROGRESS — DA-01 APPROVED; DA-02…DA-16 and RA-01…RA-16 PENDING  
**Repository:** luna7code/luna-apps  
**Base document:** `docs/fly-secret/architecture/phase-1.4.2/FLY_SECRET_UX_DESIGN_CONSOLIDATION_OWNER_APPROVAL_PHASE_1.4.2.md`  
**Base commit:** `9316b33c3eced91ef08b48b719d53953dbc44d26`  
**Working branch:** `security/fly-secret-product-owner-approval-1.4.3`

---

## 1. Governance

This ledger records only explicit product-owner decisions. A recommendation, canvas option, default selection, or discussion is not approval. Each decision must be approved separately; no implicit or batch approvals are permitted.

This phase is an approval and design-specification activity only. It does not authorize application code, a working prototype, production changes, deployment, or a pull request.

## 2. DA-01 — Visual direction

**Status:** APPROVED  
**Owner decision (clarified by visual reference):** Primary direction **H — Claret / Burgundy & Near-Black**, matching the user-provided four-screen CLARET reference. **F — Obsidian & Emerald** remains the separately selectable additional theme in Settings, as previously requested.

### 2.1 Primary identity: H — Claret / Burgundy & Near-Black

- Use the uploaded CLARET reference image (four mobile screens: Loading, Home, Education, Profile) as the visual source of truth for the H direction.
- Deep burgundy / wine-red primary surfaces and cards against near-black backgrounds.
- Muted dusty-rose / red accents and warm ivory typography; restrained fine borders and low-glare surfaces.
- Premium editorial character: elegant serif display typography paired with compact uppercase sans-serif labels, generous spacing, subtle texture, and restrained ornament.
- Do not substitute teal, emerald, or generic carbon-gray styling for this primary palette.
- Exact production color tokens, contrast pairs, typography mapping, and component-state tokens remain to be measured/specifed under DA-02 and accessibility verification.

### 2.2 Additional Settings theme: F — Obsidian & Emerald

- Available as an alternate user-selectable theme in Settings.
- Deep obsidian surfaces with a restrained emerald accent.
- It supplements H; it does not replace the primary identity.
- Exact color tokens, semantic mappings, light/dark behavior, component states, and contrast acceptance remain subject to DA-02 and accessibility verification.

### 2.3 Consequences and constraints

1. The design system must support a primary Claret / Burgundy & Near-Black appearance based on the supplied reference and a separately selectable Obsidian & Emerald theme.
2. Theme selection is a user preference; it must not alter security capability states or imply a security level.
3. Emerald must not be used as a universal proxy for trust, identity verification, encryption, or security success.
4. Warning, danger, success, focus, disabled, and informational states require explicit semantic tokens independent of the brand accent.
5. Text, icons, controls, focus indicators, and state distinctions must remain accessible in both themes. Color alone must not convey meaning.
6. DA-02 must define exact tokens and contrast requirements. No unverified contrast ratio is claimed here.
7. This approval specifies design direction only; it does not authorize implementation or prototype creation.

**Approval evidence:** Explicit owner selection of H and optional F, followed by clarification that H was selected because of the color shown and that the uploaded CLARET reference is the intended palette. The uploaded image is the visual reference for H.

## 2.4 Visual-reference interpretation correction

The previous ledger wording “Carbon & Signal” was an incorrect assistant interpretation of H and is superseded by this clarification. The owner-selected H direction is the burgundy / wine-red and near-black CLARET palette in the uploaded reference, not teal/gray Carbon & Signal. This correction does not approve DA-02 or any exact hex token.


## 3. DA-02 — Accent system

**Status:** APPROVED — owner explicitly approved option C / Deep Burgundy and its preliminary palette tokens. Contrast and component-state verification remain OPEN.  
**Owner-selected proposal:** **C — Deep Burgundy**, adapted to the corrected DA-01 Claret / Burgundy & Near-Black direction.

### 3.1 Proposed palette roles (provisional, not approved tokens)

| Role | Candidate value | Intended use |
|---|---|---|
| Base / near-black | `#080607` | Main app background and deepest canvas |
| Deep wine surface | `#26090E` | Elevated dark surfaces and grouped sections |
| Burgundy surface / accent | `#4B1018` | Cards, selected surfaces, restrained brand emphasis |
| Strong burgundy accent | `#791F2D` | Candidate primary-action / active accent, subject to contrast and state testing |
| Warm ivory text | `#F1E8E1` | Candidate primary text on dark surfaces |
| Muted secondary text | To be measured | Secondary labels; must meet applicable text contrast requirements |

The owner has approved these values as the **preliminary DA-02 palette tokens**. They are approved as the design direction and initial token candidates, but are not yet contrast-validated or cleared for implementation. Any token adjustment required by accessibility testing must be returned for owner review if it materially changes the approved visual direction.

### 3.2 Proposed usage rules

1. H remains the primary Claret / Burgundy & Near-Black theme. DA-02 defines a restrained burgundy tonal hierarchy rather than introducing teal or another unrelated brand hue.
2. Burgundy should be used as a surface and brand accent with controlled intensity; avoid large saturated red fields on every screen.
3. Near-black remains the dominant background. Use stepped wine surfaces to distinguish cards, navigation, dialogs and selected regions.
4. Warm ivory is the proposed primary text direction. Secondary, disabled and placeholder text require separate measured tokens.
5. Focus indicators must remain clearly visible and must not rely only on a subtle burgundy shade; use a contrast-verified outline/treatment.
6. Success, warning, danger, informational, focus and destructive states must have independent semantic tokens. Burgundy must not mean success, verification, trust, encryption or security.
7. F — Obsidian & Emerald remains a separately selectable Settings theme under DA-01. DA-02's Deep Burgundy accent proposal applies to H and does not replace or merge F's emerald identity.
8. Color alone must never communicate status; pair it with text, iconography, shape or other non-color cues.

### 3.3 Consequences and trade-offs

**Benefits**
- Preserves the exact dark wine / burgundy character that motivated the owner's H selection.
- Creates a coherent monochromatic tonal hierarchy with fewer competing hues.
- Keeps H visually distinct from the optional Obsidian & Emerald theme.
- Supports a premium editorial direction when paired with warm typography, spacing and restrained ornament.

**Risks / costs**
- Dark red surfaces can collapse into near-black at low brightness or on low-quality displays, reducing hierarchy.
- Similar burgundy shades can be difficult to distinguish for users with color-vision deficiencies; state and selection cues need non-color differentiation.
- Burgundy is unsuitable as the sole danger/success signal because its meaning is ambiguous.
- Candidate primary-action burgundy may not meet text/icon contrast requirements; action controls may need ivory foreground, stronger tonal separation, border or shape treatment.
- More dark-theme-specific contrast and component-state testing is required; light-theme behavior remains a separate DA-03 decision.

### 3.4 Required verification before implementation / final contrast sign-off

- Calculate contrast for every text/background pair, including normal and large text.
- Verify icons, controls, focus rings, selected states, disabled states, borders, overlays, sheets, dialogs and navigation.
- Check OLED/low-brightness appearance and distinguishability between adjacent wine surfaces.
- Check color-vision-deficiency simulations and ensure no status is color-only.
- Produce paired H/F theme token matrices and confirm that semantic state colors remain consistent in meaning.
- Do not claim WCAG 2.2 AA conformance until the actual complete component/state matrix is measured and reviewed.

### 3.5 Decision record

- Decision: DA-02 Accent system.
- Owner selection: Option C — Deep Burgundy.
- State: **APPROVED** by explicit product-owner confirmation.
- Approved option: C — Deep Burgundy.
- Approved preliminary tokens: `#080607`, `#26090E`, `#4B1018`, `#791F2D`, `#F1E8E1`.
- Contrast validation: **OPEN / REQUIRED**; no WCAG conformance claim is made.
- Final secondary-text and semantic-state tokens: still to be specified and verified.
- Implementation/prototype authorization: **NOT GRANTED**.



## 4. DA-03 — Theme behavior, appearance mode and preference persistence

**Status:** PENDING — owner selected Option A for final consideration; explicit approval not received.  
**Baseline source:** Phase 1.4.2 DA-03 recommends system default with light and dark; alternatives are light-only or dark-only.  
**Current approved visual constraints:** H = Claret / Burgundy & Near-Black with DA-02 Deep Burgundy preliminary tokens; F = optional Obsidian & Emerald theme in Settings. DA-03 does not alter either approved palette.

### 4.1 Decision scope

DA-03 must define four related but separate behaviors:
- appearance mode: follow operating-system preference, force light, or force dark;
- palette family: H (default) or F (optional alternate);
- manual override behavior and how the user returns to System;
- persistence of the user's choices across app restarts and supported platforms.

The appearance mode and palette family should be separate settings, not a single combined theme identifier. Theme selection is purely visual and must not imply or change encryption, identity verification, trust, privacy, or network-security state.

### 4.2 Option A — Adaptive system mode + independent H/F palette selection (RECOMMENDED)

- Appearance setting: **System / Light / Dark**.
- Palette setting: **H — Claret / Burgundy** (default) or **F — Obsidian / Emerald**.
- Both H and F receive coordinated Light and Dark variants. The supplied H reference remains the visual source of truth for H Dark; H Light is a carefully derived companion, not a replacement for the reference. F's Light/Dark variants must preserve its separate emerald identity.
- Default for a fresh install: **System + H**.
- When System is selected, the app follows the OS appearance automatically. A manual Light or Dark choice overrides the OS until the user selects System again.
- Switching H/F preserves the current appearance mode. Example: Dark + H → Dark + F; System + H → System + F.
- Persist `appearanceMode` and `paletteFamily` as separate app preferences. Proposed default values: `system` and `H`.
- Persist changes across restarts. Do not imply cloud synchronization; cross-device sync is out of scope unless separately approved with data-flow evidence.
- Settings should show the current effective appearance as well as the selected mode, so System does not obscure whether the device is currently rendering Light or Dark.

**Benefits:** Best platform integration; predictable manual override; users can choose either palette independently of brightness; scalable across Android, iOS, Desktop and Web.

**Costs / risks:** Highest design and QA scope: four palette/mode combinations, OS-change handling, first launch, restart, accessibility and platform parity. H Light and F Light are not present in the supplied reference and need explicit design work. Avoid flashes of the wrong theme during startup.

### 4.3 Option B — Dark-first fixed themes, manual palette switch

- H and F are both Dark-only visual themes.
- Settings exposes a direct **H / F** switch; the operating-system Light/Dark preference is ignored.
- The selected palette is persisted across restarts.
- No System / Light / Dark appearance control is shown.

**Benefits:** Closest to the current dark reference; smallest theme matrix; simpler to implement and validate.

**Costs / risks:** Ignores OS preference and may reduce comfort/accessibility in bright environments; no native Light appearance; weaker platform integration. Must not imply the app follows system appearance.

### 4.4 Option C — System Light/Dark for H; F remains Dark-only

- Appearance setting: **System / Light / Dark** for H.
- H has Light and Dark variants; H Dark follows the supplied reference.
- Selecting F switches to its fixed Dark appearance. The UI must clearly disclose that F is Dark-only; the selected System/Light preference is retained and resumes when the user returns to H.
- Palette choice H/F persists separately from H appearance preference.

**Benefits:** Preserves system-aware behavior for the default palette while limiting the work needed for the optional theme.

**Costs / risks:** Inconsistent behavior between H and F; switching to F can change effective brightness unexpectedly; users may misunderstand why System no longer controls the screen. Requires clear explanation in Settings.

### 4.5 Comparison

| Capability | A — Adaptive + independent | B — Dark-only manual | C — Hybrid |
|---|---|---|---|
| Follows OS appearance | Yes, in System mode | No | Yes for H only |
| Manual Light/Dark override | Yes | No | Yes for H |
| H/F independently selectable | Yes | Yes | Yes |
| Both palettes support Light/Dark | Yes | No | H only |
| Preference persists | Yes | Yes | Yes |
| Complexity | High | Low | Medium |
| Platform consistency | Highest | Lowest | Mixed |

### 4.6 Required behavior and acceptance checks (if A is selected)

1. Fresh install resolves to System + H without a theme flash.
2. System mode responds to OS Light/Dark changes while the app is active and after resume.
3. Manual Light/Dark remains selected across OS changes and app restarts.
4. Selecting System clears the manual appearance override and resumes OS following.
5. Switching H/F preserves the appearance mode and does not reset unrelated settings.
6. Preferences persist after process death and normal app restart on each supported platform.
7. Web behavior must account for browser/system preference changes and storage restrictions; do not claim native parity without tests.
8. Settings labels and previews identify both the selected palette family and effective appearance.
9. No theme or color state changes any security capability or status.
10. No startup screen, notification, push payload or network request may expose private content merely because a theme setting changes.

### 4.7 Persistence and data handling proposal

- Store only non-sensitive UI preferences; no cryptographic material is involved.
- Keep appearance mode and palette family as separate preference fields.
- Proposed logical values: `appearanceMode = system | light | dark`; `paletteFamily = H | F`.
- Persistence mechanism remains platform-specific and must follow the app's approved storage architecture.
- Do not describe the preference as “local only” until backup, sync, telemetry, crash-reporting, export and deletion behavior are documented and verified.
- No cloud synchronization is included in this proposal.


### 4.9 Final-consideration specification — Option A (owner-selected direction; NOT APPROVED)

The owner selected Option A as the direction for final consideration. This is not approval. The following exact behavioral contract is proposed for sign-off.

**A. Independent settings**
- `appearanceMode` accepts exactly `system`, `light`, `dark`.
- `paletteFamily` accepts exactly `H`, `F`.
- Fresh-install defaults are `system` and `H`.
- The effective appearance is derived from the selected mode and current OS/browser preference; it is not persisted as a third user preference.
- The effective palette is derived from `paletteFamily` and effective appearance. Theme choice does not change any security capability, identity state, privacy state, or network behavior.

**B. Deterministic resolution**
- If `appearanceMode=system`, resolve Light or Dark from the platform appearance signal.
- If `appearanceMode=light` or `dark`, ignore subsequent OS appearance changes until the user selects System.
- When the user changes from manual Light/Dark to System, immediately re-resolve from the current platform signal.
- If the platform does not expose a usable appearance signal, use Light as the documented fallback while keeping the stored mode as `system`; the Settings UI must explain that system appearance is unavailable on that platform. This fallback requires owner confirmation as part of DA-03.
- The app must not silently convert a manual override into System or silently change the selected palette.

**C. Palette switching**
- H and F each have Light and Dark variants.
- Switching H ↔ F preserves `appearanceMode`; the target palette's variant matching the current effective appearance is applied.
- H Dark must retain the approved CLARET reference direction and DA-02 preliminary Deep Burgundy tokens. H Light is a derived companion and must be reviewed as part of later visual approval; DA-03 does not approve its detailed colors.
- F Light and F Dark must preserve the distinct Obsidian & Emerald identity. DA-03 does not approve detailed F tokens.
- No mixed H/F surfaces or partial palette inheritance is permitted, except shared semantic tokens whose meanings are explicitly defined and contrast-verified.

**D. Persistence scope**
- Persist both settings independently across normal app restarts and process termination on each supported platform.
- Proposed persistence scope is per app installation/device. No account sync, cross-device sync, cloud backup guarantee, or “local only” claim is approved by this decision.
- On first launch with no stored preference, apply System + H. On upgrade, preserve an existing valid preference if one exists; otherwise use System + H.
- If preference storage is unavailable or corrupt, fail predictably to System + H for that run, do not crash, and do not claim the fallback was saved. Whether to surface a recoverable settings warning must be decided in implementation design.
- Deleting app data / reinstalling returns to System + H unless a separately approved migration or backup policy says otherwise.

**E. Startup and lifecycle**
- Resolve preferences and the current effective theme before presenting the first fully styled application frame, to avoid a visible flash of the wrong palette.
- Re-evaluate System mode on cold start, app resume, and platform appearance-change events.
- Apply theme changes atomically across navigation, dialogs, sheets, forms, controls, empty states and accessibility surfaces; avoid mixed-theme frames.
- If the platform reports an appearance change while the app is backgrounded, use the latest value on resume.

**F. Settings UX**
- Show two separate controls: “Appearance” (System / Light / Dark) and “Color theme” (H / F, with descriptive names).
- Show the effective appearance, e.g. “System · currently Dark”, when System is selected.
- Include previews for H and F in the currently effective appearance; do not make preview selection change the live theme until the user confirms/selects it.
- Explain that both palettes support Light and Dark under this proposal.
- Do not use security badges, shield icons, “verified”, “protected”, or security-score language for theme selection.

**G. Platform-specific acceptance matrix**
- Android and iOS: verify OS signal, manual override, resume behavior, process death, restart, first frame, and palette switching.
- Desktop: verify supported OS appearance integration and restart behavior for each supported desktop target; platform-specific support must be documented rather than assumed.
- Web: verify browser/system preference changes, page reload, tab lifecycle, storage denial/quota/corruption and browser-specific behavior. Do not claim native parity without evidence.
- Across all targets: verify all four combinations H-Light, H-Dark, F-Light, F-Dark and all critical component states. DA-02 contrast verification remains required; DA-03 approval does not waive it.

**H. Explicit consequences of selecting A**
- Product benefit: users can choose a brand palette independently of brightness and retain OS integration.
- Accessibility benefit: users can force Light or Dark even when OS preference is unsuitable; actual accessibility still depends on contrast and component testing.
- Design cost: four complete theme variants, not merely recolored backgrounds; each requires surfaces, text, borders, icons, focus, selected/disabled states, overlays and dialogs.
- Engineering cost: platform appearance observers, deterministic preference resolution, persistence, migration/default handling, startup theme resolution and Web storage fallbacks.
- QA cost: four palette/appearance combinations × supported platforms × lifecycle transitions, including OS changes during background/resume.
- Maintenance cost: future components must define tokens for all four combinations and cannot hard-code H-only or F-only colors.
- Schedule implication: DA-03 A is the most complete option and has the largest design/implementation/test surface. No schedule estimate is made at this approval stage.

**I. Owner confirmation required**
Please explicitly approve or request changes to all of the following:
1. Both H and F receive Light and Dark variants.
2. Fresh install and missing/corrupt preference fallback: System + H.
3. System follows the current OS/browser appearance; manual Light/Dark overrides it until System is reselected.
4. H/F switching preserves the selected appearance mode.
5. Preferences persist per app installation/device; no cloud or cross-device sync is included.
6. Platform without a usable system appearance signal falls back to Light while retaining System mode and showing an explanatory Settings message.
7. Startup resolves the effective theme before the first fully styled frame; lifecycle changes apply atomically.
8. Detailed H Light and F Light/Dark palette tokens remain subject to later visual approval and contrast verification.

**Decision state:** PENDING. Option A is selected as the direction for final consideration only. No DA-03 approval has been inferred.

### 4.8 Decision record

- Decision: DA-03 Theme behavior.
- Options presented: A, B, C.
- Recommendation: **A — Adaptive system mode + independent H/F palette selection**.
- State: **PENDING**; Option A selected as direction for final consideration only.
- Product-owner approval: **NOT RECEIVED**.
- Exact behavioral contract: documented in §4.9; all eight confirmation points remain pending.
- Implementation/prototype authorization: **NOT GRANTED**.


## 5. Decision register

| Decision | Status |
|---|---|
| DA-01 — Visual direction | **APPROVED** |
| DA-02 — Accent system | **APPROVED** — Deep Burgundy; preliminary tokens approved, contrast validation OPEN |
| DA-03 — Theme behavior | PENDING — options A/B/C presented; no approval |
| DA-04…DA-16 | PENDING |
| RA-01…RA-16 | PENDING |

The normative definitions, recommendations, and consequences for the remaining decisions stay in the Phase 1.4.2 baseline document. This ledger does not silently change those definitions.

## 6. Project invariants retained

- The current product scope remains a non-crypto MVP shell; no real messaging, calls, file transfer, push, recovery, or E2EE is claimed as implemented.
- The mandatory bilingual non-crypto disclosure and fail-closed capability-state taxonomy remain in force.
- Four primary destinations remain Chats, Calls, Contacts, Settings.
- TOR MODE remains excluded from product, architecture, UI, configuration, tests, and roadmap.
- Decentralization remains future-only.
- All 27 Red-Team scenarios remain **NOT_RUN**.
- `main` is not modified. No pull request is created.

## 7. Verification status

- [x] Owner decision recorded in this ledger.
- [x] MagicPath Phase 1.4.3 decision board updated with DA-01 outcome and DA-02 as the next decision.
- [x] Red-Team test-case inventory compared with the Phase 1.4.2 baseline: 27 unique scenario IDs; all 27 initial statuses remain `NOT_RUN`.
- [x] DA-02 — Deep Burgundy and preliminary tokens recorded as explicitly APPROVED.
- [x] DA-03 — options A/B/C and recommendation recorded as proposal only; approval not inferred.
- [ ] Contrast verification for all text, icon, control, focus, selected, disabled, overlay, dialog and navigation states remains OPEN.
- [x] GitHub fetch-back verification of this ledger (content and blob SHA confirmed).
- [x] Branch comparison against `main`: 21 commits ahead, 0 behind; all 21 files are additions, with no deletions. `main` remains unchanged.
- [ ] DA-02…DA-16 approvals.
- [ ] RA-01…RA-16 approvals.
- [ ] Any implementation, prototype, or product QA — not authorized in this phase.
