# MVP scope — first Windows foundation

## Implemented in source
- WinUI 3 desktop shell and four navigation destinations.
- Character profile editing (name, 2D/3D type, optional asset path).
- Local JSON persistence with temporary-file replacement.
- Explicit runtime availability messaging.
- Settings page with local data folder access.

## Not implemented
- Avatar asset parsing, import, rendering or animation.
- Camera capture, face/pose landmarks, tracking or multi-camera fusion.
- Voice conversion, microphone capture, audio routing or A/V synchronization.
- OBS integration and live output.
- GPU inference, model management and performance profiles.
- Automatic hardware detection and installer prerequisite management.

## MVP acceptance sequence
1. Clean Windows build succeeds.
2. Install package on a clean Windows 10/11 x64 machine.
3. Navigate all four sections.
4. Save a character profile, close the app, relaunch and verify persisted values.
5. Open the local data directory and inspect the JSON profile.
6. Confirm unavailable runtime states are explicit and no device capture occurs.
7. Verify uninstall preserves the user's local profile.

A future motion/voice integration must use a pinned, licensed, reproducible runtime and hardware test evidence.
