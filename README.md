# LUNA AVATAR STUDIO

**Status: engineering foundation 0.1 — not an MVP and not ready to install.**

This repository currently contains a WinUI 3 shell, four navigation destinations, and a minimal local character-profile form. It does not yet render avatars or capture camera/microphone data.

## Implemented in source
- WinUI 3 navigation: Home / Character / Studio / Settings.
- Character profile fields: name, 2D/3D selection, optional asset path.
- Local JSON persistence under %LOCALAPPDATA%/LunaAvatarStudio.
- Explicit unavailable states for Motion Capture, Voice Conversion and Live Output.

## Not implemented
- 2D/3D asset import, renderer, animation or avatar preview.
- Motion Capture, camera capture or pose estimation.
- Voice Conversion, audio capture or audio routing.
- OBS output.
- Hardware detection, performance profiles, diagnostics pipeline.
- Automated tests and installer validation.

## Build status
The initial Windows CI run failed during XAML compilation. The MSIX packaging, signing and ZIP installer steps were skipped. **There is no verified installer artifact yet.**

The current project targets .NET 8 and Windows App SDK 1.6; both are out of support as of the audit date (2026-11-19). Upgrade and a clean build are mandatory before merge.

## Engineering decisions
- [Engineering audit 2026-11-19](docs/ENGINEERING_AUDIT_2026-11-19.md)
- [ADR-0001: Windows real-time architecture](docs/ADR-0001-WINDOWS-REALTIME-ARCHITECTURE.md)
- [MVP scope and acceptance sequence](docs/PRODUCT_MVP.md)
- [Build and install notes](docs/BUILD_INSTALL.md)
- [Engineering status](docs/ENGINEERING_STATUS.md)

## Release policy
Do not merge or distribute until CI is green, the MSIX is signed with the intended publisher identity, installation is verified on a clean supported Windows device, and profile persistence/uninstall tests pass. A self-signed CI package is test-only.
