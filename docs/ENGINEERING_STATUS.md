# Engineering status

## Evidence status
This repository starts the Windows desktop MVP from a clean baseline. Source is authored in the feature branch. No Windows build or runtime test is claimed until GitHub Actions reports a successful run.

## Confirmed source scope
- WinUI 3 / .NET 8 project definition.
- Four-page desktop shell.
- Local character profile persistence.
- Explicit unavailable statuses for unintegrated real-time engines.
- MSIX packaging configuration and CI installer workflow.

## Unverified
- NuGet restore and WinUI compilation on Windows runner.
- MSIX packaging output path and package deployment.
- First-run and upgrade behavior on clean Windows.
- Runtime memory and responsiveness.
- Actual 2D/3D avatar rendering.

## Not implemented
- Motion Capture runtime.
- Voice Conversion runtime.
- OBS output.
- Camera/microphone device capture.
- GPU inference and performance profiles.

## Release gate
Do not merge or publish a public installer until CI is green, package installation is tested on a clean Windows machine, and profile persistence/uninstall behavior is verified.
