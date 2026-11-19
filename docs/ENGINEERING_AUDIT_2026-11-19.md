# LUNA AVATAR STUDIO — Engineering Audit
**Дата:** 2026-11-19  
**Объект:** PR #6, commit 7ea66b6edb4fa1ba7e4302b4c5d9a681c6037413  
**Репозиторий:** luna7code/luna-apps  
**Режим:** read-only audit исходников и фактического CI; документ аудита добавляется отдельно, runtime этим документом не изменяется.

## Executive verdict

**PR #6 не готов к merge и не является MVP продукта.** Это bootstrap shell: WinUI-окно, четыре страницы, форма профиля и локальная JSON-запись. Ни одного сквозного пользовательского сценария «камера → трекинг → аватар → голос → OBS» пока нет.

**Архитектурное решение:** Electron не использовать как основную платформу Windows-приложения. Сохранить нативную Windows-архитектуру: WinUI 3 для UI, .NET 10 LTS для application shell/orchestration, C++20 для real-time media core. Motion Capture и Voice Conversion подключать только через проверенные, закреплённые по версии runtime. Не вводить микросервисы и IPC в горячий путь до измерений.

Это решение не означает, что Electron «медленный» или технически не способен на real-time. Он просто добавляет Chromium/Node process model и IPC, при этом всё равно потребует native modules или отдельного native media core для низколатентной камеры, аудио и GPU inference. Для Windows-only продукта это лишний слой без доказанного преимущества. См. [Electron Process Model](https://www.electronjs.org/docs/latest/tutorial/process-model) и [Electron Performance](https://www.electronjs.org/docs/latest/tutorial/performance).

## 1. Проверенные факты

### 1.1 CI
GitHub Actions run [44326878919](https://github.com/luna7code/luna-apps/actions/runs/44326878919) завершился с **failure**.

Успешно: checkout, установка .NET SDK, MSBuild setup, NuGet restore.  
Провал: шаг Restore and build WinUI application, XAML compiler:

    MainWindow.xaml(8,25): XamlCompiler error WMC0011:
    Unknown member 'PaneBackground' on element 'NavigationView'

Шаги поиска MSIX, подписи и упаковки Installer были пропущены. **MSIX и ZIP Installer не созданы этим run.**

### 1.2 Состав приложения
Подтверждено в коде:
- WinUI 3 / .NET 8 project;
- навигация Home / Character / Studio / Settings;
- поля имени, формата 2D/3D и пути к asset;
- запись одного JSON-файла в %LOCALAPPDATA%/LunaAvatarStudio;
- статические тексты «not connected» для Motion Capture, Voice Conversion и Live Output.

Не обнаружено в PR:
- загрузчик или декодер 2D/3D моделей;
- renderer, skeleton, blendshapes, animation retargeting;
- camera capture и tracking runtime;
- audio capture, DSP или Voice Conversion runtime;
- OBS output adapter;
- hardware capability detection;
- performance profiles;
- unit/integration/UI tests;
- schema migration, backup/recovery и structured diagnostics.

### 1.3 Документация
В PR присутствуют только PRODUCT_MVP.md, BUILD_INSTALL.md, ENGINEERING_STATUS.md и README. Это инструкции к заготовке, а не полная архитектурная документация. Нет C4/context/container/component views, data-flow diagrams, thread model, media timing model, runtime/plugin contracts, threat model, compatibility matrix, test strategy, ADRs и release engineering specification.

## 2. Critical findings

| ID | Severity | Finding | Required action |
|---|---|---|---|
| ENG-001 | P0 | CI красный: неизвестное XAML-свойство PaneBackground | Исправить XAML, повторить чистую Windows build |
| ENG-002 | P0 | Проект таргетит .NET 8, поддержка закончилась 2026-11-10 | Перейти на .NET 10 LTS и закрепить SDK |
| ENG-003 | P0 | Windows App SDK 1.6 в проекте снят с поддержки | Перейти на актуальную стабильную поддерживаемую ветку; сейчас 2.7.0, проверить upgrade/build |
| ENG-004 | P0 | README называет заготовку «Windows desktop MVP» | Переименовать в foundation/prototype до реализации вертикального сценария |
| ENG-005 | P1 | Installer pipeline не прошёл даже до генерации MSIX | Сделать package validation, sign/verify, install/uninstall smoke tests |
| ENG-006 | P1 | В CI используется временный self-signed publisher certificate | Оставить только для тестов; production signing identity и защищённое хранение ключа — отдельный release gate |
| ENG-007 | P1 | Uninstaller удаляет пакет, но не удаляет тестовый сертификат из TrustedPeople | Добавить безопасное удаление сертификата по thumbprint и документировать cleanup |
| ENG-008 | P1 | Нет автоматических тестов профиля и восстановления после повреждения JSON | Добавить unit tests, backup/restore, schema version |
| ENG-009 | P1 | Нет hardware/runtime compatibility contract | Ввести capability report и проверенную матрицу GPU/OS/camera/audio |
| ENG-010 | P1 | Нет threat/privacy model для камеры, микрофона, моделей и локальных файлов | Добавить consent, least privilege, model provenance, log redaction и threat model |

### Поддержка платформы
На дату аудита .NET 8 достиг End of Support 10 ноября 2026 года. Для нового продукта следует использовать .NET 10 LTS, поддерживаемый до ноября 2028 года. Источник: [Microsoft .NET Support Policy](https://dotnet.microsoft.com/en-us/platform/support/policy).

Windows App SDK 1.6 также out of support. На дату аудита стабильная ветка Windows App SDK — 2.7.0. Переход на неё должен быть отдельным upgrade/build spike, а не слепой заменой версии. Источники: [Windows App SDK release channels](https://learn.microsoft.com/en-us/windows/apps/windows-app-sdk/release-channels), [Windows App SDK downloads](https://learn.microsoft.com/en-us/windows/apps/windows-app-sdk/downloads).

## 3. Architecture decision

### 3.1 Desktop shell
**Выбор: WinUI 3 + Windows App SDK + .NET 10 LTS.**

Обязанности shell:
- navigation, windows, dialogs, accessibility, settings;
- lifecycle, update coordination, user consent;
- configuration and project orchestration;
- diagnostics UI;
- управляемый вызов native core через версионированный API.

Не размещать в UI thread camera inference, audio DSP, disk scans, model downloads или long-running GPU work.

Windows App SDK — рекомендуемая Microsoft платформа для новых Windows desktop apps: [Microsoft Windows App SDK](https://learn.microsoft.com/en-us/windows/apps/windows-app-sdk/).

### 3.2 Electron
**Решение: NO-GO как основная оболочка LUNA Windows.**

Причины:
1. Продукт Windows-first; кроссплатформенный UI пока не является требованием.
2. Chromium renderer + Node main/utility process добавляет второй runtime stack.
3. Низколатентный audio/video pipeline всё равно требует native C++/GPU integration.
4. UI-to-native bridge создаёт ещё один контракт/IPC boundary, который не приносит измеримого преимущества текущему scope.

Electron можно пересмотреть только если появится утверждённое требование shipping одного UI на Windows/macOS/Linux и сравнительный прототип покажет выигрыш в скорости разработки без ухудшения latency, memory, device access и installer footprint.

### 3.3 Real-time core
**Выбор: C++20 native libraries внутри одного приложения на первом этапе.**

Модули:
- MediaCapture — camera/device adapters;
- MotionRuntime — tracking and pose inference;
- AudioEngine — WASAPI capture/render, ring buffers, DSP;
- VoiceRuntime — pinned voice-conversion runtime adapter;
- AvatarRuntime — skeleton, blendshape, animation state;
- RenderBackend — GPU renderer adapter;
- OutputAdapter — OBS/window output adapters;
- Diagnostics — metrics, traces, crash context.

C# ↔ C++ boundary: узкий versioned C ABI (или WinRT component после proof). Не передавать кадры/аудиоблоки через JSON, UI dispatcher или синхронные вызовы на каждый frame. Использовать bounded queues, explicit ownership, timestamps и back-pressure.

**Не дробить на микросервисы.** Отдельный worker process допустим только если crash isolation, GPU runtime conflicts, model sandboxing или independent update доказуемо требуют process boundary. Тогда использовать shared memory для крупных frame/audio buffers и control channel отдельно.

### 3.4 Motion Capture
- Capture adapter отделён от tracking runtime.
- Сначала одна проверенная webcam и одна тестовая конфигурация.
- Runtime должен быть version-pinned, license-reviewed и воспроизводимо доставляться.
- Inference provider abstraction: CPU fallback и GPU provider; конкретный provider выбирается только после hardware benchmark.
- Выдавать timestamped landmarks, confidence, tracking-loss state и source identity.
- Не обещать multi-camera/fingers/feet до тестов синхронизации и error metrics.

### 3.5 Voice Engine
- Audio I/O и DSP в native real-time thread.
- WASAPI — базовый Windows audio backend.
- Предварительно фиксировать sample rate, channel count и frame/block size; измерять end-to-end latency.
- Никаких heap allocations, blocking locks, file/network I/O в audio callback.
- При выборе converted voice — fail-closed: runtime failure означает mute/stop output, а не незаметный raw-mic passthrough.
- Публиковать median/p95 latency, underruns, dropped blocks, RTF и аудиокачество на фиксированном hardware manifest.

### 3.6 Renderer decision gate
**Не выбирать Unreal/Unity/Godot по презентациям.** UI framework не является avatar renderer. Текущий PR не содержит ни одного renderer.

Перед выбором сделать изолированные spikes на одинаковом hardware:
- 2D: один лицензированный rigged-avatar sample;
- 3D: один VRM/glTF humanoid sample с skeleton, facial blendshapes и retargeting;
- transparent/background output path usable by OBS;
- cold start, GPU memory, steady-state FPS, frame-time p95, A/V sync, package size;
- import/export and license audit.

После spike оформить ADR с победителем и benchmark artifacts. До этого renderer — явный open decision, а не скрытая архитектурная догадка.

### 3.7 Data and assets
- SQLite для профилей, проектов, presets, runtime manifests и migrations.
- Большие модели/assets хранить файлами, не в SQLite BLOB.
- Каждая asset package: format, version, hash, source/license, compatibility metadata.
- Import через staging → validation → atomic move; не запускать пользовательские модели/скрипты автоматически.
- JSON использовать для interchange/export, не как единственный долгосрочный database layer.

### 3.8 Installer and updates
- MSIX подходит для первичной Windows package delivery, но CI self-signed package — только тест.
- Production требует постоянной publisher identity, trusted signing, timestamping, signature verification и secure key handling.
- Installer должен проверять OS/architecture, зависимости и свободное место; runtime/model packages — подписанные и version-pinned.
- Uninstall должен удалять приложение и тестовые trust artifacts; пользовательские проекты удалять только по явному выбору.
- Обновление/rollback, offline install и recovery — отдельные тестируемые сценарии.

## 4. Что считать настоящим MVP

MVP — не четыре пустые страницы. MVP — один повторяемый end-to-end workflow:

1. Пользователь создаёт/импортирует один тестовый аватар.
2. Приложение получает поток с одной проверенной камеры.
3. Motion runtime управляет головой и верхней частью тела.
4. Renderer показывает аватар в реальном времени.
5. Пользователь выбирает voice mode; проверенный Voice runtime преобразует голос либо fail-closed отключает звук.
6. Один проверенный output path попадает в OBS.
7. Профиль и настройки переживают перезапуск приложения.
8. Diagnostics показывают FPS, frame-time p95, camera FPS, tracking confidence/loss, voice latency p50/p95, audio underruns, GPU/CPU/RAM.

2D и 3D должны остаться равноправными направлениями продукта. Для первого vertical slice допустим один формат, но до выхода MVP должен существовать проверенный путь для обоих типов.

## 5. Release gates

### Gate A — build integrity
- CI green on clean Windows runner.
- .NET 10 LTS + supported Windows App SDK.
- no compiler warnings elevated to errors unresolved;
- deterministic package version and build metadata.

### Gate B — application integrity
- all navigation and profile tests pass;
- corrupt profile recovery tested;
- no data loss during interrupted save;
- accessibility and DPI scaling checked.

### Gate C — media integrity
- camera and audio devices tested on published hardware matrix;
- no UI-thread work in real-time paths;
- p50/p95 metrics collected;
- Voice fail-closed tested;
- OBS output tested in a real OBS session.

### Gate D — installer integrity
- MSIX signature verified;
- install/upgrade/rollback/uninstall tested on clean Windows;
- user data preservation confirmed;
- no test certificate or private signing key shipped in production artifact.

### Gate E — product claim integrity
Only features proven on a published hardware/software manifest may be advertised as supported.

## 6. Immediate execution order

1. Keep PR #6 open; do not merge.
2. Fix PaneBackground compile failure and rerun CI.
3. Upgrade .NET 8 → .NET 10 LTS; upgrade Windows App SDK 1.6 → supported stable 2.7.0 after compatibility spike.
4. Rename current release scope to Foundation 0.1; remove MVP/installer-ready claims.
5. Add unit tests for ProfileStore and CI test stage.
6. Add architecture ADRs, threat model, data flow, thread model, compatibility matrix and test plan.
7. Build a renderer spike (2D + 3D) before selecting Unity/Unreal/Godot.
8. Integrate one verified Motion runtime.
9. Integrate one verified Voice runtime with fail-closed audio path.
10. Validate OBS output, then production installer.

## 7. Audit limitations

This audit used the PR diff, source files represented in PR #6, and actual GitHub Actions logs for run 44326878919. No local Windows desktop session, physical camera, microphone, GPU benchmark, OBS session, installer installation, or uninstall test was performed. No such test is claimed.

## External engineering references
- [.NET support policy](https://dotnet.microsoft.com/en-us/platform/support/policy)
- [Windows App SDK release channels](https://learn.microsoft.com/en-us/windows/apps/windows-app-sdk/release-channels)
- [Windows App SDK](https://learn.microsoft.com/en-us/windows/apps/windows-app-sdk/)
- [Electron process model](https://www.electronjs.org/docs/latest/tutorial/process-model)
- [Electron performance](https://www.electronjs.org/docs/latest/tutorial/performance)
