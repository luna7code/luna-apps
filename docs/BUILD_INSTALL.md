# Build and install

## Build environment
- Windows 10/11 x64.
- Visual Studio 2022 with Windows App SDK / WinUI tooling.
- .NET 8 SDK.
- Windows 10 SDK 10.0.19041 or newer.

## Local build
Open `src/LunaAvatarStudio/LunaAvatarStudio.csproj` in Visual Studio 2022. Select Release and x64, restore NuGet packages, then build.

GitHub Actions builds the MSIX on `windows-latest`, signs it with an ephemeral CI certificate, and publishes a ZIP artifact.

## Install CI test package
1. Download and extract `LUNA-AVATAR-STUDIO-WINDOWS-TEST.zip`.
2. Open PowerShell as the current user.
3. Run:
   ```powershell
   Set-ExecutionPolicy -Scope Process Bypass
   .\Install-LUNA-AVATAR-STUDIO.ps1
   ```
4. Launch the app from the Windows Start menu.

The installer imports the included test certificate into the current user's Trusted People store and installs the signed MSIX.

## Uninstall
Run `Uninstall-LUNA-AVATAR-STUDIO.ps1` in PowerShell. The app's local profile directory is preserved at:
`%LOCALAPPDATA%\LunaAvatarStudio`

## Signing warning
The CI workflow generates a temporary self-signed certificate. It is only suitable for a controlled test build. Do not distribute this package as a public release. A production release requires a stable publisher identity, a trusted code-signing certificate, secure private-key storage, and a release-signing workflow.

## Troubleshooting
- If package deployment reports a missing framework, inspect the build output and Windows App SDK deployment configuration; do not install random runtimes from third-party sites.
- If Windows blocks installation, verify the certificate publisher matches the package identity and that the certificate is in the current user's Trusted People store.
- If build fails, retain the full MSBuild log and exact Windows SDK / Visual Studio versions.
