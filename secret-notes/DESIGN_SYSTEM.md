# SECRET NOTES — Dark Retro Synthwave Design System

Status: **approved direction**. Dark Retro Synthwave replaces Plum Shadow as the active Android visual theme.

## Design intent

A restrained retro-futurist night aesthetic: near-black violet foundations, layered plum-purple surfaces, neon magenta as the primary action accent, electric cyan for secondary/focus states, and pale lavender-white typography. Use glow sparingly. The app remains a privacy-first notebook, not an arcade UI.

## Android Compose

- Package: `com.luna.secretnotes.ui.theme`
- Theme: `SecretNotesTheme { ... }`
- Tokens: `RetroSynthwave`
- Default and only shipped scheme in this iteration: dark.
- Material 3 primary: neon pink; secondary: electric cyan; tertiary: soft violet.

## Color tokens

| Role | Hex |
|---|---:|
| Background | `#090611` |
| Surface | `#120D1E` |
| Elevated surface | `#1B1230` |
| Card | `#211538` |
| Pressed card | `#2D1B48` |
| Primary text | `#F8F4FF` |
| Secondary text | `#C9BEDF` |
| Muted text | `#A99BBF` |
| Border | `#624A7E` |
| Subtle border | `#38284B` |
| Focus ring / cyan | `#47E8FF` |
| Primary neon pink | `#FF4FD8` |
| Pressed pink | `#E63BBF` |
| On pink | `#24091F` |
| Pink container | `#4A1643` |
| On pink container | `#FFD7F6` |
| Neon violet | `#B68CFF` |
| Error | `#FF6B8A` |
| Error container | `#5C1428` |
| Success | `#65F0B4` |
| Success container | `#124431` |

## Visual rules

1. Keep 85–90% of the interface in dark neutral/plum surfaces. Neon is an accent, not a background.
2. Use pink for primary CTAs, selected navigation, and key active states.
3. Use cyan for focus rings, secondary actions, links, and subtle technical indicators. Do not use cyan and pink at equal visual weight in the same component.
4. Use violet for restrained depth and decorative details only.
5. Cards use the explicit `RetroSynthwave.Card` token; avoid default gray Material cards.
6. Glow should be localized to focused controls, selected tabs, or a small logo detail. Never apply large blurred neon glows behind body text.
7. Do not use gradients behind long text, notes, or editor surfaces.
8. Error and success states must include labels/icons; never rely on color alone.
9. Preserve strong contrast and calm reading surfaces. The editor should be less saturated than navigation and action chrome.

## Accessibility

- Normal text must reach WCAG 2.2 AA 4.5:1 against the exact surface.
- Large text and essential UI boundaries must reach at least 3:1.
- The palette tokens are not a blanket contrast guarantee: verify each foreground/background pairing when building components, especially neon text on purple surfaces and muted text on cards.
- Focus indicators must remain visible without glow effects.

## Scope

This is the approved color design system and Compose theme foundation. It is not a claim that all app screens, components, editor states, or visual QA have been completed.
