# SECRET NOTES — Plum Shadow Design System

Status: **approved**. This is the canonical color system for the Android application.

## Design intent

Plum Shadow is a restrained premium palette: near-black plum foundations, quiet plum-tinted cards, warm high-contrast text, and a muted lilac-plum accent. It avoids neon saturation and generic electric purple. Dark mode is the default; light mode is fully specified and available through the same semantic token set.

## Android Compose entry point

- Package: `com.luna.secretnotes.ui.theme`
- Theme: `SecretNotesTheme(darkTheme = true) { ... }`
- Tokens and Material 3 schemes: `PlumShadow`
- Default mode: dark.

## Semantic tokens

| Role | Dark | Light |
|---|---:|---:|
| Background | `#100D12` | `#FAF7FB` |
| Surface | `#19141D` | `#FFFBFF` |
| Elevated surface | `#211A26` | `#F7F0F8` |
| Card | `#261D2B` | `#F2EAF5` |
| Pressed card | `#302438` | `#E9DDEC` |
| Primary text | `#F7F1F8` | `#211825` |
| Secondary text | `#C8BACD` | `#514657` |
| Muted text | `#9D8FA3` | `#6D6072` |
| Border | `#514456` | `#817487` |
| Subtle border | `#392F3D` | `#D8CBDC` |
| Accent | `#D6A8E2` | `#70427C` |
| On accent | `#2B1731` | `#FFFFFF` |
| Accent container | `#4A3152` | `#F2DDF7` |
| On accent container | `#F5DDF9` | `#2B1233` |
| Error | `#FFB4AB` | `#BA1A1A` |
| Error container | `#93000A` | `#FFDAD6` |
| Success | `#A6D7B5` | `#216A43` |
| Success container | `#1D3A2B` | `#D2F0DC` |

## Verified text contrast pairs

WCAG relative-luminance calculations for the following intended pairings:

| Pair | Contrast |
|---|---:|
| Dark primary text / background | 17.36:1 |
| Dark secondary text / surface | 9.80:1 |
| Dark muted text / surface | 5.95:1 |
| Dark accent / background | 9.70:1 |
| Light primary text / background | 16.17:1 |
| Light secondary text / surface | 8.67:1 |
| Light muted text / surface | 5.75:1 |
| Light accent / background | 7.16:1 |
| White text / light accent button | 7.61:1 |

These ratios apply only to the exact foreground/background pairs listed. Recalculate for other combinations, transparency, overlays, disabled states, and images.

## Usage rules

1. Use `background` only for the app canvas and large page backgrounds.
2. Use `surface` for primary panels; `surfaceVariant` for secondary/elevated regions.
3. Use `PlumShadow.DarkCard` / `LightCard` for note cards. Do not approximate cards with arbitrary gray.
4. Primary text must use `onSurface` or `onBackground`; secondary text uses `onSurfaceVariant`.
5. Use the accent for primary actions, selected states, links, and focus indicators — not for large decorative fills.
6. Error and success colors are semantic. Never communicate status through color alone; pair with an icon or text.
7. Borders should be subtle. Use the stronger border for focused/selected controls.
8. Do not introduce blue, neon gradients, or unrelated accent colors.

## Accessibility requirements

- Normal text: WCAG 2.2 AA minimum 4.5:1 against its actual surface.
- Large text and essential UI boundaries: minimum 3:1.
- Recheck contrast whenever a token is used on a surface other than its paired semantic background.
- Status text must remain legible in both modes.

## Scope

This file defines color tokens and their usage. It does not claim that all screens, note editor states, accessibility checks, or the complete application UI are implemented.
