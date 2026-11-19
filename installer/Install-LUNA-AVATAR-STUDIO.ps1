$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$certificate = Join-Path $root "LUNA-AVATAR-STUDIO-TEST.cer"
$package = Join-Path $root "LUNA-AVATAR-STUDIO.msix"
if (-not (Test-Path $certificate) -or -not (Test-Path $package)) {
    throw "Installer files are incomplete. Keep the MSIX and test certificate in the same folder."
}
Write-Host "Installing temporary test certificate into CurrentUser\TrustedPeople..."
Import-Certificate -FilePath $certificate -CertStoreLocation "Cert:\CurrentUser\TrustedPeople" | Out-Null
Write-Host "Installing LUNA AVATAR STUDIO..."
Add-AppxPackage -Path $package
Write-Host "Installation completed. Launch LUNA AVATAR STUDIO from the Windows Start menu."
Write-Warning "This package uses a temporary self-signed CI certificate and is intended for testing only."
