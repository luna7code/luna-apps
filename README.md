# LUNA AVATAR STUDIO

Windows desktop MVP — первый рабочий фундамент продукта.

## Что реализовано в этой версии
- Нативная оболочка Windows на WinUI 3.
- Навигация Home / Character / Studio / Settings.
- Редактирование и сохранение профиля персонажа в локальный JSON.
- Выбор типа персонажа 2D / 3D и сохранение пути к пользовательскому asset.
- Отдельная диагностика статуса Motion Capture, Voice Conversion и Live Output.
- Локальное хранение в `%LOCALAPPDATA%\LunaAvatarStudio`.
- CI workflow для сборки Windows MSIX и создания тестового подписанного пакета.

## Что намеренно НЕ заявлено как готовое
Проверенных Motion Capture и Voice Conversion runtime пока нет. Studio показывает их как неподключённые. Камера и микрофон не захватываются, Live Output не отправляет данные в OBS.

## Требования
- Windows 10 2004 (build 19041) или новее, x64.
- Visual Studio 2022 с workload .NET Desktop Development и WinUI tooling.
- .NET 8 SDK.

## Сборка
Откройте `src/LunaAvatarStudio/LunaAvatarStudio.csproj` в Visual Studio 2022 и соберите Release / x64. GitHub Actions собирает MSIX при Pull Request в main.

## Установка CI-пакета
Скачайте artifact `luna-avatar-studio-windows-test-installer`, распакуйте и выполните в PowerShell:
```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\Install-LUNA-AVATAR-STUDIO.ps1
```
Пакет использует временный self-signed сертификат CI. Это тестовая подпись, не публичный сертификат издателя.

## Документы
- [MVP scope](docs/PRODUCT_MVP.md)
- [Build and install](docs/BUILD_INSTALL.md)
- [Engineering status](docs/ENGINEERING_STATUS.md)
