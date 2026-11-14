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
**Owner decision:** Primary direction **H — Carbon & Signal**, with **F — Obsidian & Emerald** as an additional selectable theme in Settings.

### 2.1 Primary identity: H — Carbon & Signal

- Near-black / carbon foundation.
- Neutral surface hierarchy.
- One restrained signature accent; its exact hue and tokens are not decided in DA-01.
- Applies as the primary visual identity across the product.
- The accepted trade-off is that distinctiveness depends on disciplined accent usage, typography, layout, and contrast rather than multiple decorative colors.

### 2.2 Additional Settings theme: F — Obsidian & Emerald

- Available as an alternate user-selectable theme in Settings.
- Deep obsidian surfaces with a restrained emerald accent.
- It supplements H; it does not replace the primary identity.
- Exact color tokens, semantic mappings, light/dark behavior, component states, and contrast acceptance remain subject to DA-02 and accessibility verification.

### 2.3 Consequences and constraints

1. The design system must support a primary Carbon & Signal appearance and a separately selectable Obsidian & Emerald theme.
2. Theme selection is a user preference; it must not alter security capability states or imply a security level.
3. Emerald must not be used as a universal proxy for trust, identity verification, encryption, or security success.
4. Warning, danger, success, focus, disabled, and informational states require explicit semantic tokens independent of the brand accent.
5. Text, icons, controls, focus indicators, and state distinctions must remain accessible in both themes. Color alone must not convey meaning.
6. DA-02 must define exact tokens and contrast requirements. No unverified contrast ratio is claimed here.
7. This approval specifies design direction only; it does not authorize implementation or prototype creation.

**Approval evidence:** Explicit product-owner instruction in the project conversation: “Я выбераю H и дополнительно в настройках тему F”. Recorded as the owner decision for DA-01.

## 3. Decision register

| Decision | Status |
|---|---|
| DA-01 — Visual direction | **APPROVED** |
| DA-02…DA-16 | PENDING |
| RA-01…RA-16 | PENDING |

The normative definitions, recommendations, and consequences for the remaining decisions stay in the Phase 1.4.2 baseline document. This ledger does not silently change those definitions.

## 4. Project invariants retained

- The current product scope remains a non-crypto MVP shell; no real messaging, calls, file transfer, push, recovery, or E2EE is claimed as implemented.
- The mandatory bilingual non-crypto disclosure and fail-closed capability-state taxonomy remain in force.
- Four primary destinations remain Chats, Calls, Contacts, Settings.
- TOR MODE remains excluded from product, architecture, UI, configuration, tests, and roadmap.
- Decentralization remains future-only.
- All 27 Red-Team scenarios remain **NOT_RUN**.
- `main` is not modified. No pull request is created.

## 5. Verification status

- [x] Owner decision recorded in this ledger.
- [x] MagicPath Phase 1.4.3 decision board updated with DA-01 outcome.
- [ ] GitHub fetch-back verification of this ledger.
- [ ] Branch comparison against `main`.
- [ ] DA-02…DA-16 approvals.
- [ ] RA-01…RA-16 approvals.
- [ ] Any implementation, prototype, or product QA — not authorized in this phase.
