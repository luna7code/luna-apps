$ErrorActionPreference = "Stop"
$packages = Get-AppxPackage -Name "LunaAvatarStudio"
if (-not $packages) {
    Write-Host "LUNA AVATAR STUDIO is not installed for the current user."
    exit 0
}
$packages | Remove-AppxPackage
Write-Host "LUNA AVATAR STUDIO has been uninstalled. Local profile data is preserved."
