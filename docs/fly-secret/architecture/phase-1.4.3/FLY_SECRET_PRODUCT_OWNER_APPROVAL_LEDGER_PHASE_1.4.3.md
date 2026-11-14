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


## 4. Decision register

| Decision | Status |
|---|---|
| DA-01 — Visual direction | **APPROVED** |
| DA-02 — Accent system | **APPROVED** — Deep Burgundy; preliminary tokens approved, contrast validation OPEN |
| DA-03…DA-16 | PENDING |
| RA-01…RA-16 | PENDING |

The normative definitions, recommendations, and consequences for the remaining decisions stay in the Phase 1.4.2 baseline document. This ledger does not silently change those definitions.

## 5. Project invariants retained

- The current product scope remains a non-crypto MVP shell; no real messaging, calls, file transfer, push, recovery, or E2EE is claimed as implemented.
- The mandatory bilingual non-crypto disclosure and fail-closed capability-state taxonomy remain in force.
- Four primary destinations remain Chats, Calls, Contacts, Settings.
- TOR MODE remains excluded from product, architecture, UI, configuration, tests, and roadmap.
- Decentralization remains future-only.
- All 27 Red-Team scenarios remain **NOT_RUN**.
- `main` is not modified. No pull request is created.

## 6. Verification status

- [x] Owner decision recorded in this ledger.
- [x] MagicPath Phase 1.4.3 decision board updated with DA-01 outcome and DA-02 as the next decision.
- [x] Red-Team test-case inventory compared with the Phase 1.4.2 baseline: 27 unique scenario IDs; all 27 initial statuses remain `NOT_RUN`.
- [x] DA-02 — Deep Burgundy and preliminary tokens recorded as explicitly APPROVED.
- [ ] Contrast verification for all text, icon, control, focus, selected, disabled, overlay, dialog and navigation states remains OPEN.
- [x] GitHub fetch-back verification of this ledger (content and blob SHA confirmed).
- [x] Branch comparison against `main`: 21 commits ahead, 0 behind; all 21 files are additions, with no deletions. `main` remains unchanged.
- [ ] DA-02…DA-16 approvals.
- [ ] RA-01…RA-16 approvals.
- [ ] Any implementation, prototype, or product QA — not authorized in this phase.
