# SECRET NOTES — Cosmic Design System

Status: **Active direction: Cosmic / Event Horizon**, replacing Dark Retro Synthwave.

## Design intent
Quiet premium deep-space atmosphere inspired by black holes, star fields, pulsars and distant galaxies. The app should feel like a private instrument, not an arcade game or neon cyberpunk interface.

## Palette
| Role | Hex |
|---|---:|
| Event-horizon background | `#03050A` |
| Surface | `#090D16` |
| Elevated surface | `#111827` |
| Card | `#151E2D` |
| Pressed card | `#202C3D` |
| Starlight text | `#F4F7FC` |
| Secondary text | `#B9C4D4` |
| Muted text | `#77869A` |
| Border | `#526176` |
| Subtle border | `#263346` |
| Pulsar blue | `#9CCBFF` |
| Nebula violet | `#B5A8D8` |

## Rules
- Near-black surfaces dominate. Star points and nebula light are subtle decoration only.
- Starlight/silver is the primary action accent; pulsar blue is focus/selection; muted violet is secondary.
- No synthwave pink, saturated neon, arcade grids, or loud gradients behind text.
- Do not place star texture behind small text or text fields.
- Motion should be slow and subtle; never flash.
- Validate WCAG 2.2 AA: 4.5:1 normal text, 3:1 large text and essential boundaries.

## Launcher identity
Use the supplied silver planet and orbital ring on a black field. Manifest references `@drawable/ic_launcher`.

## Compose theme
- `SecretNotesTheme`
- `CosmicTheme`
- Dark scheme only.
