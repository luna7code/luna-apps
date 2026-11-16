# SECRET NOTES — Cosmic Design System

Status: **Active direction: Cosmic / Event Horizon**. This replaces Dark Retro Synthwave as the product's primary visual theme.

## Design intent
A quiet, premium deep-space atmosphere inspired by black holes, star fields, pulsars and distant galaxies. The app should feel like a private instrument, not an arcade game or neon cyberpunk interface.

## Palette
| Role | Hex |
|---|---:|
| Event-horizon background | `#03050A` |
| Surface | `#090D16` |
| Elevated surface | `#111827` |
| Card | `#151E2D` |
| Pressed card | `#202C3D` |
| Primary text / starlight | `#F4F7FC` |
| Secondary text | `#B9C4D4` |
| Muted text | `#77869A` |
| Border | `#526176` |
| Subtle border | `#263346` |
| Pulsar blue focus accent | `#9CCBFF` |
| Nebula violet accent | `#B5A8D8` |

## Visual rules
- Near-black surfaces dominate. Star points and nebula light are subtle decoration only.
- Starlight/silver is the primary action accent; pulsar blue is reserved for focus and selection; muted violet is secondary.
- No synthwave pink, saturated neon, arcade grids, or loud gradients behind text.
- Never place star texture directly behind small text or text fields; keep reading surfaces calm.
- Motion, if added later, is slow and subtle: a distant stellar shimmer, never flashing.
- Validate WCAG 2.2 AA contrast: 4.5:1 for normal text, 3:1 for large text and essential boundaries.

## Launcher identity
Use the user's supplied silver planet with orbital ring on a black field as the launcher mark. Android manifest references `@drawable/ic_launcher`.

## Implementation
- Compose theme: `SecretNotesTheme`
- Tokens: `CosmicTheme`
- Only dark scheme ships.
- Cosmic starfield is currently used on the credential gate. Other screens must use the same tokens when implemented.
