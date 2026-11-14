# FLY SECRET — Phase 0.1 Architecture Security Review

- Версия: 0.1
- Основа анализа: семь документов Red-Team Test Specification v1.0 в `docs/fly-secret/red-team/v1.0/`
- Ветка: `security/fly-secret-architecture-review-0.1`
- Статус: архитектурный анализ документации; не аудит реализации
- Реализация приложения: НЕ НАЧИНАЛАСЬ
- Исполнение тестов: все 27 сценариев `NOT_RUN`
- TOR MODE: ИСКЛЮЧЁН
- Децентрализация: только будущее направление, не входит в текущую архитектуру поставки

## 1. Executive summary

В документах определены основные границы доверия и 27 сценариев: privacy (4), identity (4), relay (2), cryptography (3), recovery (3), files (3), calls (3), future decentralization (3), supply chain (1), diagnostics/logging (1).

Архитектурная готовность к реализации: **NO-GO до фиксации обязательных решений ниже**. Это не означает, что уязвимости уже обнаружены в приложении: приложения и исполняемой системы пока нет. Риски ниже — проектные угрозы и условия, которые должны быть предотвращены дизайном и подтверждены тестами после реализации.

### Ключевые блокирующие решения

1. **Криптографический стек:** выбрать одну поддерживаемую реализацию протокола/библиотеки после проверки аудитов, лицензий, API, платформ, обновлений и interoperability. Не собирать собственный протокол из примитивов. Signal PQXDH/Double Ratchet/Sesame и актуальные post-quantum механизмы — ориентиры для оценки, а не утверждение, что конкретная библиотека уже выбрана.
2. **Identity и directory:** определить FLY ID, identity key, device key, session key и transport identifier как разные сущности. Зафиксировать proof-of-possession, key transparency, key-change handling и независимую проверку fingerprint.
3. **Recovery:** разделить перенос активной сессии, Recovery Kit и восстановление идентичности. Сервер не получает plaintext private keys или recovery secrets. Пользователь должен понимать, что потеря единственного recovery material может означать необратимую потерю доступа.
4. **Multi-device:** формализовать состояние каждого устройства, linking approval, revocation, rotation, stale sessions, delayed/reordered/replayed envelopes и синхронизацию доверия.
5. **Calls:** отдельно спроектировать и доказать application-level E2EE media. DTLS-SRTP/WebRTC transport security не доказывает, что SFU не может расшифровать media.
6. **Privacy:** определить adversary model, минимизацию метаданных, retention, contact discovery, push payload, analytics/crash reporting и измеримые ограничения корреляции.
7. **Supply chain:** определить защищённый GitHub Actions workflow, least-privilege permissions, pinned actions/dependencies, SBOM, artifact provenance, signing-key custody и release approval.

## 2. Матрица Critical / High

Оценка — проектная inherent severity до внедрения controls. Она не является подтверждённым finding. Статус каждого сценария остаётся `NOT_RUN`.

| ID / область | Уровень | Главный риск | Обязательные меры / критерий закрытия |
|---|---|---|---|
| ID-01 — directory split-view | Critical | Подмена ключа или разные ключи для одного FLY ID | Append-only key transparency; signed tree heads; inclusion/consistency proofs; независимые monitors; Verified сбрасывается при любом key change |
| ID-02 — registration replay | Critical | Повтор challenge регистрирует устройство/меняет identity | Одноразовый CSPRNG challenge, expiry, atomic consume, binding к device/action/transcript, proof-of-possession |
| ID-03 — device cloning | Critical | Клон получает mailbox/session access без private key | Отдельный per-device keypair; proof-of-possession; серверная state machine; явное linking/revocation |
| CRYPTO-01 — ratchet rollback | Critical | Восстановление старого состояния приводит к повтору key/nonce или обходу replay | Атомарная запись состояния; anti-rollback strategy; crash consistency; безопасное переустановление сессии при сомнении |
| CRYPTO-03 — downgrade | Critical | Negotiation откатывается к слабой/устаревшей версии | Подписанный/аутентифицированный transcript; строгая version policy; запрет silent fallback; compatibility tests |
| REC-01 — Recovery Kit theft | Critical | Компрометация recovery secret или server-side plaintext escrow | Client-side encryption; высокоэнтропийный secret; проверенный KDF; secret redaction; сервер не может расшифровать backup |
| REC-02 — recovery replay/race | Critical | Одноразовый токен применён повторно или конкурентно | Atomic one-time consume; expiry; transaction binding; idempotency; concurrency regression |
| REC-03 — unauthorized device link | Critical | Новое устройство получает ключи без одобрения | PENDING/ACTIVE state machine; trusted-device approval; short-lived pairing; signed transcript; notifications |
| FILE-01 — IDOR | Critical | Чтение/удаление чужих файлов | Object-level authorization на каждом endpoint; scoped capabilities; негативные тесты на все методы и варианты URL |
| CALL-02 — SFU media access | Critical | SFU/relay может расшифровать звонок вопреки E2EE обещанию | Протокол E2EE media поверх transport; media-key lifecycle; независимый cryptographic review; тест, что SFU не имеет ключей |
| SUPPLY-01 — CI/dependency tampering | Critical | Подмена dependency, workflow или release artifact | Pinned actions/dependencies; минимальные permissions; protected environments; artifact attestations; SBOM; независимое управление signing |
| LOG-01 — secrets in logs/crash | Critical* | Утечка plaintext/private/recovery secrets через telemetry | Запрет секретов в логах; canary/secret scans; redaction; allowlist telemetry; crash-report review. *Critical, если затронуты ключи/recovery/plaintext; иначе High |
| PRIV-01 — traffic correlation | High | Восстановление социального графа по времени/размерам | Метаданные минимизированы; retention ограничен; измерять correlation precision/recall; не обещать абсолютную анонимность |
| PRIV-02 — FLY ID enumeration | High | Выявление зарегистрированных пользователей | Непредсказуемые IDs; privacy-preserving discovery; uniform responses; rate limits и abuse detection |
| PRIV-03 — push correlation | High | Связывание push token, ID и пользователя | Opaque wake-up payload; разделение push/identity; ротация токенов по threat model; минимальные логи |
| PRIV-04 — contact discovery | High | Утечка адресной книги и стабильных хешей | Data minimization; PSI/альтернатива после crypto/privacy review; запрет логирования; retention policy |
| ID-04 — stale Verified UX | High | UI показывает доверие после смены ключа | Единый trust-state machine; немедленный сброс Verified; предупреждение на всех surfaces |
| RELAY-01 — envelope fault injection | High | Drop/reorder/duplicate/corruption нарушает целостность/доставку | AEAD в составе vetted protocol; idempotency; replay protection; bounded retries; delivery semantics |
| RELAY-02 — queue exhaustion | High | Один tenant влияет на всех пользователей | Per-tenant quota; backpressure; fair scheduling; bounded queue/TTL; load isolation |
| CRYPTO-02 — skipped-key exhaustion | High | Память/CPU exhaustion или desync | Жёсткие лимиты skipped keys и TTL; отказ до выделения больших ресурсов; fuzz/boundary tests |
| FILE-02 — EXIF/GPS | High | Медиа раскрывают геолокацию/устройство | Явная политика metadata stripping; preview/confirmation UX; проверка оригинал/обработанная копия |
| FILE-03 — parser/resource abuse | High | Повреждённый/большой файл вызывает DoS или parser compromise | Размеры/типы/таймауты/quotas; sandboxed parsing; обновляемые парсеры; fuzzing в lab |
| CALL-01 — signalling substitution/replay | High | Подмена peer/session/call state | Аутентификация signalling; transcript binding; anti-replay; связывание call identity с проверенным contact identity |
| CALL-03 — ICE/IP disclosure | High | Раскрытие IP вопреки ожиданиям пользователя | Документировать candidate/route disclosure; безопасные defaults; явный UX; собственные STUN/TURN tests |
| DEC-01 — Sybil concentration | Critical (future) | Один оператор контролирует критическую долю узлов/quorum | До будущего запуска: Sybil resistance, operator diversity, governance analysis, simulation; сейчас не реализовывать |
| DEC-02 — Eclipse | High (future) | Клиент изолирован и получает неполный network view | Независимые bootstrap sources, peer diversity, consistency checks, partition recovery; только future gate |
| DEC-03 — node impersonation/rotation | High (future) | Подмена node identity или неотозванный ключ | Подписанные node announcements, expiry, rotation/revocation, independent trust roots; только future gate |

* Матрица не присваивает PASS ни одному сценарию. DEC-01/02/03 — условные будущие риски и не являются требованием к текущему MVP.

## 3. Cross-cutting архитектурные контроли

### 3.1 Идентификаторы и ключи

В спецификации должны быть явно разведены:
- FLY ID — публичный/пользовательский идентификатор, не секрет и не ключ;
- identity key — долговременная криптографическая идентичность;
- device key — отдельная идентичность устройства;
- session/ratchet state — эфемерное состояние протокола;
- transport/mailbox identifiers — ротируемые технические идентификаторы.

Нельзя использовать один ID как универсальный credential. Серверная авторизация устройства должна опираться на криптографическое подтверждение владения ключом и проверяемый lifecycle.

### 3.2 E2EE и выбор протокола

До реализации оформить ADR:
- выбранный vetted protocol/library и точная версия;
- аудит/репутация, лицензия, поддерживаемые платформы, maintenance cadence;
- 1:1 и group messaging scope;
- multi-device semantics;
- PQ migration/interop plan без преждевременных claims;
- поведение при downgrade, key change, device revoke и recovery;
- тестовый harness и независимый review.

Не писать собственный ratchet, KEM, KDF, handshake или media crypto. Применять Signal specs и MLS RFC по назначению: MLS — ориентир для групповых сценариев, а не автоматическая замена 1:1 протокола.

### 3.3 Recovery и потеря устройств

Recovery нельзя сводить к обычному входу в аккаунт:
- Device Transfer — перенос с доступного доверенного устройства;
- Recovery Kit — пользовательский секрет/материал для восстановления;
- Identity Re-establishment — новый trust lifecycle, если прежние ключи потеряны.

Для каждого пути определить, что именно восстанавливается, какие старые сессии отзываются, как уведомляются контакты и как предотвращается silent identity substitution. Не обещать восстановление, если пользователь потерял все необходимые секреты.

### 3.4 Метаданные и транспорт

- Push payload — только минимальный opaque wake-up.
- Логи, analytics, crash reports — по allowlist; секреты и plaintext запрещены.
- Contact discovery — отдельный privacy review.
- Metadata retention и deletion — измеримая политика.
- RFC 9458 OHTTP может рассматриваться только для подходящих stateless запросов с соответствующей relay/gateway архитектурой; это не универсальная анонимность и не TOR.
- TOR MODE не включать ни в один слой.

### 3.5 Файлы и звонки

- File service никогда не должен считать знание file ID достаточной авторизацией.
- Metadata stripping должен быть осознанным продуктовым поведением с понятным выбором пользователя.
- Для calls отдельно моделировать signalling, ICE/STUN/TURN, SFU и application-level media encryption.
- Успешное WebRTC transport encryption не является доказательством E2EE media.

### 3.6 Платформы и локальное хранилище

Native Android/iOS и Web имеют разные trust boundaries. Android Keystore/biometrics/FLAG_SECURE и encrypted DB — platform controls, а не абсолютная гарантия. Для Web отдельно оценить XSS, browser storage, extensions, service workers и runtime compromise. Anti-screenshot/anti-forwarding не предотвращает съёмку внешней камерой.

## 4. Release gates — критерии GO / NO-GO

| Gate | Условие допуска | Обязательное evidence | Блокирующее условие |
|---|---|---|---|
| G0 Architecture | Утверждены data flows, trust boundaries, threat model, platform scope и crypto ADR | Подписанные ADR, diagrams, dependency/library review | Неизвестно, где находятся ключи/секреты или кто может видеть plaintext |
| G1 Identity | Proof-of-possession, key transparency, verification UX, device lifecycle определены | Test design + executed results для ID-01…04 | Любой Critical identity issue; stale Verified |
| G2 Cryptography | Vetted protocol выбран; session state, rollback, downgrade, multi-device определены | Независимый protocol review; CRYPTO-01…03 и relay regressions | Custom crypto без review; key/nonce reuse; silent downgrade |
| G3 Recovery | Transfer/Recovery Kit/re-establishment разделены; server escrow исключён | Threat review; REC-01…03 executed evidence; UX loss scenarios | Сервер знает recovery secret/private key; replay или unauthorized link |
| G4 Privacy | Data inventory, push/logging, discovery, retention и metadata claims утверждены | Privacy assessment; PRIV-01…04; LOG-01; sanitized traces | Plaintext/secret leakage или необоснованные anonymity claims |
| G5 Files | Object authorization, parser limits, metadata policy готовы | FILE-01…03 executed evidence; endpoint authorization matrix | Cross-account file access; parser compromise; uncontrolled resource use |
| G6 Calls | Signalling authenticated; E2EE media independent from SFU; IP disclosure documented | CALL-01…03 evidence; independent media E2EE review | SFU can decrypt; call identity substitution; material undisclosed IP exposure |
| G7 Relay | Delivery, idempotency, quotas, isolation and abuse controls defined | RELAY-01/02 evidence; fault/load test reports | Cross-tenant impact, duplicate action, unauthenticated message mutation |
| G8 Supply chain | Reproducible/reviewable build, SBOM, pinned dependencies, signing custody | CI workflow review, provenance, SBOM, signing/permissions audit | Untrusted artifact, exposed signing secret, unreviewed privileged workflow |
| G9 Release decision | Все предыдущие обязательные gates выполнены и findings закрыты | Release dossier, commit/build hashes, reviewers, risk acceptance | Любой открытый Critical; High в crypto/privacy/auth/recovery; NOT_RUN/ BLOCKED без waiver не считаются PASS |

### Severity policy

- **Critical:** всегда NO-GO до исправления и подтверждённой регрессии.
- **High:** NO-GO для публичного релиза в crypto, identity, recovery, privacy, file authorization, calls и supply chain. Исключение возможно только через формальный risk acceptance, компенсирующие меры и одобрение ответственных reviewers; не для компрометации ключей/plaintext.
- **Medium/Low:** владелец, срок, impact и mitigation фиксируются в risk register; решение документируется.
- **NOT_RUN/BLOCKED:** не эквивалентны PASS. На этапе до реализации допустимы как плановые статусы, но не как release evidence.

## 5. Android build and release policy (future)

APK/AAB должны собираться GitHub Actions из контролируемой ветки/тега:
- отдельные CI и release workflows;
- least-privilege `GITHUB_TOKEN` permissions;
- pinned third-party Actions по immutable commit SHA;
- зависимости с lockfiles и review;
- unit tests, lint, security checks и SBOM до release;
- release signing key хранится вне Git, в защищённом secret/HSM-backed процессе; keystore и пароли не запрашивать в чате и не коммитить;
- APK/AAB публикуются как immutable artifacts с provenance/checksum;
- production release требует protected environment и ручного approval;
- workflow не должен публиковать подписанный production APK из произвольной feature-ветки.

Сейчас workflow приложения не создаётся и APK не собирается: реализация отсутствует. Это целевая политика для будущего Android-модуля.

## 6. Документные замечания и корректировки

1. В `FLY_DECENTRALIZATION_THREAT_MODEL.md` сценарий CALL-03 (ICE/IP disclosure) находится в секции будущей децентрализации. По смыслу это call/network privacy риск, а не децентрализационный риск. В risk matrix он классифицирован как Calls/Privacy; исходную тестовую спецификацию следует в следующей редакции перенести в соответствующую секцию, сохранив ID.
2. В будущем тест-кейсы должны связывать каждый сценарий с конкретным build SHA, protocol/library version, platform и evidence. Сейчас этих значений нет, поэтому тесты остаются NOT_RUN.
3. Термины "Signal-level", "post-quantum", "anonymous" и "E2EE" должны иметь точное определение и scope. Их нельзя использовать как marketing guarantee до независимой проверки.
4. В документах есть повторяющийся общий заголовок/шаблон Red-Team. При следующей редакции можно нормализовать метаданные документов, не меняя сценарии и IDs.

## 7. Решения, которые требуется утвердить до реализации

- Crypto library/protocol ADR с проверкой поддержки React Native и native bridges.
- Модель directory/key transparency и независимых monitors.
- Модель FLY ID и per-device identity lifecycle.
- Три отдельных recovery flows и пользовательские последствия потери секретов.
- 1:1 vs group messaging scope и протокол для каждого.
- Call E2EE media architecture и SFU threat model.
- Contact discovery и metadata privacy model.
- Native mobile vs Web security baseline и платформенные ограничения.
- Data retention/deletion policy.
- GitHub Actions security/release design.

## 8. Итог Phase 0.1

**Решение: CONDITIONAL NO-GO для начала реализации.**

Разрешённая следующая работа: уточнение и утверждение архитектурных ADR, data-flow diagrams, abuse/privacy requirements и test harness design. Не разрешено считать безопасность доказанной или переводить тесты в PASS без реализации и evidence.

- Все 27 тестов: `NOT_RUN`.
- TOR MODE: исключён.
- Децентрализация: future-only.
- Приложение: не реализуется в Phase 0.1.
- APK: будущая сборка через GitHub Actions, после появления Android-кода.
