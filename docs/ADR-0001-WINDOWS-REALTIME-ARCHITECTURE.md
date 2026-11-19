# ADR-0001: Windows desktop and real-time architecture

- **Status:** Accepted as the implementation direction; renderer engine remains gated by benchmark.
- **Date:** 2026-11-19
- **Scope:** LUNA AVATAR STUDIO Windows desktop.

## Decision

1. **UI shell:** WinUI 3 + Windows App SDK + .NET 10 LTS.
2. **Real-time media core:** C++20 native modules.
3. **Language boundary:** versioned C ABI initially; no per-frame JSON or UI-dispatcher calls.
4. **Process topology:** modular monolith first. Separate worker processes only when measurements or fault-isolation requirements justify them.
5. **Packaging:** MSIX for Windows delivery. Self-signed certificates are test-only; production requires stable trusted publisher identity and protected signing.
6. **Electron:** rejected as the primary Windows shell for the current product scope.
7. **Avatar renderer:** not selected yet. Unity, Unreal and Godot must pass the same 2D/3D benchmark before an ADR selects one.

## Why not Electron

Electron is a legitimate desktop framework and its Chromium multi-process model is documented and supported. It is not rejected as inherently incapable or inherently slow.

It is rejected here because:
- Windows is the only committed desktop target;
- WinUI provides native Windows shell integration without introducing Chromium as a second application runtime;
- camera/audio/GPU workloads still need native modules regardless of UI framework;
- no web UI reuse requirement currently offsets the extra runtime, IPC and packaging surface.

Reopen this decision only if cross-platform desktop becomes a funded product requirement and a prototype demonstrates a net engineering benefit.

## Runtime module responsibilities

| Module | Responsibility | Hot-path rule |
|---|---|---|
| MediaCapture | camera and device adapters | timestamp frames; bounded queues |
| MotionRuntime | tracking and pose inference | no UI thread; confidence/loss metrics |
| AudioEngine | WASAPI capture/render and buffering | real-time safe callbacks; no blocking I/O |
| VoiceRuntime | pinned conversion runtime | fail-closed when conversion is selected |
| AvatarRuntime | skeleton, blendshapes, animation state | deterministic frame timestamps |
| RenderBackend | GPU rendering | adapter interface; benchmarked backend |
| OutputAdapter | OBS/window output | explicit A/V clock and back-pressure |
| Diagnostics | logs, traces, metrics | redact private media and paths |

## Renderer selection gate

Run two minimal prototypes against the same hardware and test data:
- 2D rigged avatar with licensed runtime;
- 3D VRM/glTF humanoid with skeleton and facial blendshapes.

Measure cold start, steady-state FPS, frame-time p95, GPU/CPU/RAM, GPU memory, transparent output, OBS compatibility, package size, import pipeline and license obligations. Select the engine only after publishing results. Do not embed a full game engine in the UI process without evidence that this is the simplest reliable deployment.

## Data decision

Use SQLite for structured project/profile metadata and schema migrations. Keep large avatar assets and models as files with a manifest, content hash, version, provenance and license metadata. JSON remains an interchange/export format, not the sole persistence layer.

## Consequences

- The UI and real-time pipeline can be optimized independently.
- Native media code requires C++ ownership, crash diagnostics and ABI discipline.
- The first implementation remains a modular monolith to limit operational complexity.
- Renderer engine choice is deliberately deferred until a reproducible proof-of-concept exists.
- The current PR #6 does not yet implement this architecture; it is only a UI/persistence scaffold.

## Revisit triggers

- Approved macOS/Linux desktop requirement.
- Measured need for process isolation.
- Renderer spike results.
- Verified Motion Capture or Voice Conversion runtime with incompatible deployment constraints.
- Installer compatibility evidence from clean Windows machines.
