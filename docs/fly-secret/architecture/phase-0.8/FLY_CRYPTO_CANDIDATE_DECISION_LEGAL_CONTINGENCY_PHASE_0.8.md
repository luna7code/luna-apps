# FLY SECRET — Phase 0.8
## Crypto Candidate Decision & Legal Contingency

**Статус:** DOCUMENTATION COMPLETE — CANDIDATE NOT SELECTED — NO-GO FOR SPIKE / PRODUCTION INTEGRATION  
**База:** Phase 0.7 — Legal & Upstream Blocker Resolution  
**Базовая ветка:** `security/fly-secret-legal-upstream-resolution-0.7`  
**Рабочая ветка:** `security/fly-secret-crypto-candidate-contingency-0.8`  
**Репозиторий:** `luna7code/luna-apps`  
**Область:** сценарии отказа от libsignal, допустимые архитектурные альтернативы, повторное ADR и критерии выбора.

> Этот документ является архитектурным decision framework, а не юридическим заключением, аудитом криптографической библиотеки или утверждением о пригодности какого-либо кандидата. Юридическое заключение не получено. Запрос upstream libsignal не отправлен; ответ upstream отсутствует. Ни одна библиотека не выбрана.

---

## 1. Executive decision

Phase 0.7 зафиксировала NO-GO для технического spike и production integration: AGPL-3.0-only требует письменного заключения квалифицированного юриста, позиция upstream по поддержке стороннего использования неизвестна, архитектурная совместимость и supply-chain provenance не закрыты.

Phase 0.8 не снимает эти блокеры и не заменяет юридическую или техническую проверку. Она определяет, что делать, если:

- юридическая оценка AGPL-3.0-only признаёт условия несовместимыми с моделью распространения FLY SECRET;
- юрист не может дать однозначное заключение;
- upstream libsignal подтверждает, что стороннее использование не поддерживается, или не предоставляет приемлемых условий/API stability/security coordination;
- выбранная версия или платформенная поверхность не проходит архитектурные и supply-chain критерии.

**Текущее решение:**
- libsignal — CONDITIONAL / BLOCKED, не выбран;
- альтернативные кандидаты — UNASSESSED, не выбраны;
- технический spike — NOT AUTHORIZED;
- production integration — NO-GO;
- D-01 (crypto implementation/protocol choice) остаётся OPEN;
- все 27 Red-Team сценариев остаются `NOT_RUN`.

Нельзя интерпретировать отсутствие ответа upstream как разрешение, поддержку или одобрение. Нельзя трактовать наличие открытой лицензии как автоматическое подтверждение юридической совместимости.

## 2. Trigger matrix: когда включать contingency

| Событие | Классификация | Обязательное действие | Что запрещено |
|---|---|---|---|
| Письменное заключение: AGPL несовместима с целевой коммерческой/закрытой моделью | HARD STOP | Исключить libsignal из допустимых production-кандидатов; открыть новый ADR | Продолжать интеграцию «на свой риск» |
| Юридическое заключение неоднозначно или неполно | BLOCKED | Запросить уточнение; сохранить NO-GO | Считать неопределённость разрешением |
| Upstream письменно сообщает, что стороннее использование не поддерживается, и это неприемлемо для продукта | HARD STOP / PRODUCT RISK | Зафиксировать отказ как основание исключения; новый ADR | Представлять библиотеку как поддерживаемую upstream |
| Upstream не отвечает | UNKNOWN | Зафиксировать отсутствие ответа; оценить остаточный риск только после юридического и security review | Приписывать молчанию согласие |
| Upstream подтверждает API/поддержку, но лицензия остаётся несовместимой | HARD STOP | Исключить libsignal независимо от поддержки | Считать поддержку заменой лицензии |
| Лицензия приемлема, но Android/iOS parity, lifecycle или security assurance не пройдены | TECHNICAL NO-GO | Не проводить интеграцию; открыть/обновить ADR и оценить альтернативы | Выбирать по удобству API |
| Все обязательные legal, upstream, architecture и supply-chain gates закрыты | CONDITIONAL RE-ENTRY | Новый formal review; отдельное письменное разрешение на spike | Автоматически начинать spike |

### 2.1 Условия повторного рассмотрения libsignal

Вернуться к libsignal можно только при одновременном наличии:
1. Письменного заключения counsel по конкретной модели распространения, linking/FFI, notices, source obligations и transitive dependencies.
2. Письменной позиции upstream либо документированного решения product/security owners принять unsupported third-party risk.
3. Зафиксированной версии/commit, проверенных Android/iOS API surfaces и воспроизводимого provenance.
4. Закрытых ARC-01..14 или формального, ограниченного и принятого исключения без Critical/High residual risks.
5. Повторного ADR с явным сравнением кандидатов и подписанным решением.
6. Отдельного разрешения на spike. Этот документ такого разрешения не выдаёт.

## 3. Допустимые архитектурные пути

Пути ниже — архитектурные сценарии, а не рейтинговый список готовых библиотек. Указание проекта или протокола не является доказательством пригодности конкретной версии.

### Path A — Retain libsignal conditionally

**Суть:** оставить Signal-family реализацию основным кандидатом только если юридический и upstream блокеры разрешены, а конкретная версия проходит платформенную и архитектурную проверку.

**Преимущества**
- Близость к требуемой 1:1 модели и Double Ratchet семейству.
- Потенциальная Android/iOS/Rust/TypeScript интеграционная поверхность, которую ещё нужно подтвердить для выбранной версии.
- Меньше семантических изменений в текущей архитектуре, чем при переходе на Matrix.

**Риски и ограничения**
- AGPL-3.0-only — самостоятельный legal gate; native wrapper или FFI не следует считать способом обхода лицензии.
- Upstream прямо указывал, что стороннее использование не поддерживается; это не даёт гарантии стабильности API, SLA или помощи при инцидентах.
- Нельзя предполагать совместимость с произвольной FLY ID directory, собственным relay или Recovery Kit без отдельной проверки протокольных предпосылок.
- Поддержка платформы, версии и сборочных артефактов должна быть проверена на уровне immutable commit и реальных release artifacts.

**Решение:** допустим только как условный кандидат. При юридическом HARD STOP — исключить из production-кандидатов, пока не появится новая подтверждённая правовая основа.

### Path B — Matrix Olm / vodozemac family

**Суть:** рассмотреть Matrix crypto stack как отдельную протокольную архитектуру, а не как drop-in замену libsignal.

**Преимущества**
- Иная лицензирующая и экосистемная траектория, которую можно оценивать отдельно.
- Rust-ориентированная реализация может быть интересна для общего криптографического слоя при наличии проверенных bindings и платформенного процесса.
- Matrix SDK предоставляет более широкий набор Matrix-специфичных механизмов, чем изолированная криптобиблиотека.

**Критические отличия**
- Olm/Megolm и Matrix state semantics не эквивалентны Signal Protocol / Double Ratchet.
- Внедрение может потребовать пересмотра identity, device list, key distribution, room/group semantics, key backup, sync и серверных контрактов.
- vodozemac не является Signal-compatible drop-in.
- Наличие Rust core не доказывает наличие поддерживаемых, стабильных и безопасных Android/iOS binding surfaces для требуемой версии.
- Matrix Rust SDK означает принятие Matrix-модели взаимодействия и инфраструктурных семантик, а не просто замену внутренней библиотеки.

**Обязательные повторные решения:** D-01 crypto protocol/library; D-02 FLY ID and identity binding; D-03 transparency; D-04 device lifecycle; D-05 recovery; D-06 messaging semantics; D-08 metadata/service boundary; D-09 native boundary; D-10 web boundary (если затрагивается); D-11 supply-chain/release, плюс любые ADR, чьи интерфейсы меняются.

**Решение:** допустим как отдельный вариант только при готовности пересмотреть протокол и продуктовые/серверные семантики. Не считать быстрым заменителем.

### Path C — Matrix Rust SDK / matrix-sdk-crypto

**Суть:** оценивать полноценный Matrix SDK crypto architecture, если продукт допускает Matrix-совместимую модель, а не только использовать vodozemac как низкоуровневую библиотеку.

**Преимущества**
- Более полный набор SDK-level orchestration и протокольного состояния.
- Меньше соблазна самостоятельно собирать высокоуровневую Matrix-модель из низкоуровневых примитивов.

**Риски**
- Значительная архитектурная зависимость от Matrix semantics, homeserver behavior, sync, device/key management и event model.
- Это может конфликтовать с FLY ID, минимизацией metadata и собственным ciphertext relay.
- Нужно отдельно доказать, какие компоненты обязательны, какие можно отделить, и какие данные видит сервер.
- Rust SDK bindings и мобильная интеграция требуют проверки по конкретным версиям, платформам и артефактам.

**Решение:** допустим только при осознанном выборе Matrix-подобной архитектуры и повторном утверждении backend/product requirements. Не является «сменой библиотеки» без системных последствий.

### Path D — Defer 1:1 crypto implementation; preserve safe non-crypto work

**Суть:** отложить выбор crypto engine и не реализовывать production messaging, пока не найден приемлемый, проверяемый вариант.

**Возможная безопасная работа после отдельной авторизации**
- UX flows и дизайн-система без фиктивных security claims.
- Domain models/interfaces как неисполняемые спецификации, не содержащие самодельной криптографии.
- Threat model, protocol-independent API contracts и тестовые fixtures без реальных ключей/секретов.
- Backend scaffolding только для явно разрешённых, не чувствительных функций, с запретом хранения plaintext и без имитации E2EE.
- Документация миграции и interoperability requirements.

**Риски**
- Отсрочка основной функции продукта.
- Опасность появления mock security, где интерфейс сообщает E2EE/Verified без реального криптографического доказательства.
- Риск закрепить неверные assumptions в API, storage schema или UX.

**Защитные условия**
- Любые UI/демо состояния должны быть явно помечены как mock/demo и не должны выдавать себя за защищённый обмен.
- Не создавать production key/session storage, псевдо-ratchet или самодельный crypto protocol.
- Не отправлять plaintext в relay, analytics, logs или тестовые backend endpoints.
- Отдельная авторизация нужна даже для безопасного non-crypto implementation scope.

**Решение:** допустимый fallback при блокировке всех криптографических кандидатов. Это не означает разрешение писать код в рамках Phase 0.8.

### Path E — Custom protocol from primitives (REJECTED)

**Суть:** собрать собственный messaging protocol из libsodium, Noise, RustCrypto или отдельных primitives.

**Почему отклонён**
- Примитивы не дают готового и проверенного messaging protocol, identity lifecycle, ratchet, skipped-key handling, replay resistance, multi-device semantics и recovery.
- Самостоятельная композиция создаёт высокий риск ошибок протокольного дизайна и реализации.
- Это противоречит требованию не разрабатывать собственную криптографию и использовать независимо проверенные реализации.

**Решение:** не является допустимой contingency-веткой. Не возвращать в shortlist без изменения security policy и независимого экспертного обоснования; такое изменение потребует отдельного product/security approval.

### Path F — OpenMLS / MLS family for 1:1 replacement (REJECTED AS DIRECT SUBSTITUTE)

MLS (RFC 9420) и OpenMLS относятся к групповому messaging protocol domain. Они не являются прямой заменой 1:1 Signal-family session protocol. MLS может рассматриваться в отдельном будущем ADR для групп, после выбора поддерживаемой реализации и утверждения group lifecycle. Он не закрывает текущий D-01 для 1:1.

## 4. Сводное сравнение путей

| Путь | Меняет 1:1 protocol semantics | Требует пересмотра backend/service model | Legal gate | Главный риск | Статус |
|---|---:|---:|---|---|---|
| A. Conditional libsignal | Нет, если кандидат проходит | Проверить совместимость | AGPL review обязателен | Лицензия и unsupported upstream | BLOCKED |
| B. Olm/vodozemac | Да | Вероятно, да | Проверить точную лицензию/зависимости | Неэквивалентность Signal; bindings | UNASSESSED |
| C. Matrix Rust SDK | Да | Да, существенно | Проверить точную лицензию/зависимости | Matrix coupling и metadata model | UNASSESSED |
| D. Defer crypto | Не выбирается | Можно ограничить | Зависит от будущего кандидата | Задержка и mock-security | AVAILABLE FALLBACK |
| E. Custom protocol | Самостоятельный дизайн | Да | Не главный блокер | Критический protocol risk | REJECTED |
| F. MLS as 1:1 substitute | Да, но не эквивалент | Да | Проверить точную лицензию | Неверная область применения | REJECTED AS SUBSTITUTE |

## 5. Candidate selection criteria — mandatory evidence

Ни один кандидат не может быть выбран по популярности, языку реализации, числу звёзд, наличию wrapper или одному лишь факту аудита. Каждое поле должно содержать ссылку/артефакт, версию, дату проверки и владельца review.

### 5.1 Hard gates (pass/fail)

| ID | Gate | Минимальное доказательство | Fail condition |
|---|---|---|---|
| SEL-01 | Legal/licensing | Письменное заключение counsel по exact version, linking, distribution, transitive deps | Неясные/несовместимые обязательства |
| SEL-02 | Protocol suitability | Документированная модель handshake, sessions, replay/reorder, identity binding | Нельзя удовлетворить threat model без protocol fork |
| SEL-03 | Independent assurance | Аудит конкретного кода/версии, scope, findings и remediation mapping | Критические нерешённые findings или неизвестный scope |
| SEL-04 | Android | Подтверждённые API, ABI, lifecycle, secure persistence, reproducible artifact | Нет поддерживаемой и проверяемой интеграции |
| SEL-05 | iOS | Подтверждённые API, toolchain, lifecycle, secure persistence, reproducible artifact | Нет parity или неуправляемая FFI surface |
| SEL-06 | Maintenance/support | Release cadence, security advisories, vulnerability disclosure, support posture | Неприемлемый maintenance/support risk |
| SEL-07 | Supply chain | Immutable source, signed/tagged provenance where available, checksums, SBOM, license scan | Невозможно установить происхождение артефакта |
| SEL-08 | FLY identity compatibility | Отдельные FLY ID, identity/device/session/transport identifiers | Требуется смешать FLY ID и ключи или раскрыть private keys |
| SEL-09 | Multi-device lifecycle | Add/revoke/rotate, concurrent sessions, delayed/reordered/replayed messages | Нет безопасной модели per-device state |
| SEL-10 | Recovery compatibility | Client-controlled recovery; no server plaintext/private-key escrow | Требуется server-side secret escrow |
| SEL-11 | Relay/privacy | Ciphertext-only relay, minimized metadata, opaque push | Серверу нужен plaintext или избыточные секреты |
| SEL-12 | Persistence/rollback | Atomic state transitions, crash consistency, rollback resistance | Ratchet/session rollback нельзя обнаружить/предотвратить |
| SEL-13 | Cross-platform semantics | Interoperability vectors and identical security semantics across platforms | Platform-specific divergence |
| SEL-14 | Performance | Reproducible benchmarks on defined low/mid/high devices | Unacceptable latency/memory/battery |
| SEL-15 | Operational response | Incident response, CVE intake, patch and upgrade path | Нет безопасного пути реагирования на vulnerability |

**Все hard gates обязательны.** Неизвестное значение трактуется как BLOCKED, а не PASS. Исключения требуют письменного risk acceptance и не могут обходить Critical security invariant или legal prohibition.

### 5.2 Comparative scoring after hard gates

Только кандидаты, прошедшие все hard gates, могут сравниваться по weighted score. Предлагаемые веса — предварительные и должны быть утверждены владельцами продукта и security:

| Критерий | Предлагаемый вес |
|---|---:|
| Protocol/security assurance and audit relevance | 25% |
| Legal and license compatibility | 15% |
| Android/iOS support and parity | 15% |
| FLY identity, device and recovery fit | 15% |
| Maintenance, support and incident response | 10% |
| Supply-chain reproducibility | 10% |
| Performance and resource profile | 5% |
| Integration/operational complexity | 5% |

Scoring не может компенсировать провал hard gate: например, высокая производительность не компенсирует несовместимую лицензию или отсутствие безопасной модели multi-device. Если веса меняются, это фиксируется в ADR до выставления баллов. Для каждого score обязательны evidence link, reviewer и confidence level.

## 6. Required protocol and architecture compatibility review

До выбора кандидата reviewers должны ответить на вопросы ниже на основании спецификации, исходного кода, тест-векторов и конкретных версий.

### Identity and trust
- Может ли FLY ID оставаться случайным opaque identifier, не являясь публичным ключом?
- Можно ли отдельно связать FLY ID, identity key, device key, session key и transport routing identifier?
- Поддерживается ли key transparency с proof verification, monitors/witnesses и UX для key changes?
- Можно ли отозвать устройство без тихого переиспользования старого Verified state?

### Sessions and multi-device
- Как создаётся сессия, как привязывается peer identity, как обрабатываются replay, reorder, duplicate, skipped keys и concurrent sends?
- Каково атомарное persistence boundary для каждого изменения ratchet/session state?
- Как защищаться от snapshot rollback, process crash, backup restore и device cloning?
- Как выполняются device add/revoke, session rotation и доставка сообщений на несколько устройств?
- Какие state migrations поддерживаются между версиями?

### Recovery
- Может ли Recovery Kit оставаться client-controlled?
- Не требуется ли передавать серверу plaintext recovery secret, private key или session/ratchet state?
- Как предотвращаются recovery replay, race, downgrade и silent account takeover?
- Как подтверждается новая identity/device binding после восстановления?

### Service boundary and privacy
- Может ли relay хранить только ciphertext и минимальные routing metadata?
- Нужны ли библиотеке конкретные directory, key server, homeserver, sync или backup services?
- Можно ли отделить protocol metadata от FLY account/device metadata?
- Не появляются ли plaintext, keys, tokens или sensitive protocol state в logs, crash reports, analytics и push payloads?

### Platform boundary
- Остаются ли private keys, ratchet state и crypto operations в native/Rust boundary, не пересекая JS bridge?
- Если plaintext передаётся JS для UI, задокументированы ли XSS/runtime compromise последствия для Web и RN?
- Имеют ли Android и iOS эквивалентные security semantics, lifecycle handling и failure behavior?
- Есть ли тесты на app background/foreground, process death, device lock, storage failure и OS upgrade?

## 7. ADR reopening requirements

Повторный ADR обязателен, если contingency меняет хотя бы одно из следующего:
- криптографический протокол, handshake, KDF, ratchet или wire format;
- identity binding или связь FLY ID с cryptographic identity;
- key transparency, directory trust или verification UX;
- per-device key/session lifecycle, revocation или multi-device delivery;
- Recovery Kit, device transfer, re-establishment или key escrow assumptions;
- message envelope, relay API, storage schema, server-visible metadata или push behavior;
- native/JS boundary, persistence model или platform-specific crypto APIs;
- legal/compliance model, dependency license или distribution obligations;
- Web/Desktop threat model или interoperability commitments.

### 7.1 Minimum ADR package
Новый ADR должен включать:
1. Decision context and explicit reason for reopening.
2. Rejected and accepted alternatives, including Path A–D.
3. Exact candidate name, version, immutable commit/tag, license and artifact provenance.
4. Protocol semantics and threat-model mapping.
5. Independent audit scope, findings and remediation status.
6. Android/iOS API and lifecycle evidence.
7. FLY ID, device, session, recovery and transparency compatibility.
8. Backend/relay/metadata impact and data-flow diagrams.
9. Supply-chain, SBOM, vulnerability response and upgrade strategy.
10. Performance benchmark plan/results, clearly separating planned from executed.
11. Residual risk register, owner, severity and acceptance authority.
12. Explicit GO/NO-GO, signatures/approvals and permitted next actions.

### 7.2 ADR dependencies to revisit
- **D-01:** crypto protocol/library — always reopen for a candidate change.
- **D-02:** FLY ID — reopen if identity binding or directory semantics change.
- **D-03:** key transparency — reopen if trust roots, key publication or proof model changes.
- **D-04:** multi-device lifecycle — reopen if device keys/session lifecycle changes.
- **D-05:** recovery — reopen if key backup, transfer or re-establishment semantics change.
- **D-06:** messaging — reopen if envelopes, 1:1/group semantics or session state changes.
- **D-08:** metadata/service boundary — reopen if server dependencies or observable metadata change.
- **D-09:** native boundary — reopen if bindings, secret custody or bridge behavior changes.
- **D-10:** Web — reopen if crypto API, runtime or storage assumptions change.
- **D-11:** CI/release — reopen if native build, artifact provenance, license or signing pipeline changes.

Не нужно механически переписывать ADR, чьи assumptions не затронуты; но каждый из них должен быть явно помечен RECONFIRMED или REOPENED с обоснованием.

## 8. Decision workflow

```text
Phase 0.7 blockers
      |
      v
Legal opinion on exact libsignal use?
      | yes-compatible                 | incompatible / unclear
      v                                v
Upstream posture acceptable?       Exclude libsignal or remain blocked
      | yes             | no/unknown     |
      v                 v                v
Architecture + supply-chain review      Reopen D-01 and compare Paths B/C/D
      | pass      | fail                        |
      v           v                             v
Formal candidate ADR  Reopen ADR / defer       Hard gates + evidence review
      |                                           |
      v                                           v
Separate written spike authorization? <----------+
      | yes                 | no
      v                     v
Authorized isolated spike  NO-GO; documentation only
```

Этот flow не означает, что технический spike уже разрешён. Любое положительное решение на этапе candidate ADR является только основанием запросить отдельную авторизацию.

## 9. Evidence register and unresolved items

| Item | Current status | Evidence needed | Owner |
|---|---|---|---|
| LEG-01..12 AGPL review | BLOCKED | Written counsel memo on exact use/distribution | Product/legal owner |
| Upstream libsignal inquiry | NOT SENT | Sent inquiry + archived response, or explicit no-response record | Maintainer liaison |
| ARC-01..14 compatibility | NOT CLOSED | Review mapped to exact candidate/version | Architecture/security reviewers |
| Supply-chain gate | NOT CLOSED | Immutable source, checksums, SBOM, provenance, license inventory | Release/security |
| Candidate B evidence | UNASSESSED | Exact version, audit, bindings, lifecycle and legal evidence | Candidate review |
| Candidate C evidence | UNASSESSED | Exact SDK/version, Matrix coupling, server/data-flow evidence | Candidate review |
| Path D scope | FALLBACK ONLY | Separate authorization for any implementation scope | Product owner |
| D-01 | OPEN | Formal evidence-based ADR and approvals | Product + security |

## 10. Red-Team and release-gate preservation

Все 27 уникальных Red-Team scenario IDs остаются `NOT_RUN`:
- PRIV-01..04
- ID-01..04
- RELAY-01..02
- CRYPTO-01..03
- REC-01..03
- FILE-01..03
- CALL-01..03
- DEC-01..03
- SUPPLY-01
- LOG-01

Ни один сценарий не запускался в рамках Phase 0.8. `NOT_RUN` не является PASS. Нельзя менять статус тестов без фактического выполнения, артефакта результата и review.

TOR MODE исключён из продукта, архитектуры, UI, конфигурации, тестов и roadmap. Децентрализация остаётся future-only и не является частью текущего MVP или contingency scope. Phase 0.8 не меняет release gates и не закрывает ни один security gate.

## 11. Scope and execution record

В рамках Phase 0.8 разрешено и выполнено только документирование decision framework.

**Не выполнялось:**
- юридическое заключение или обращение к юристу;
- отправка upstream inquiry или получение ответа;
- выбор/утверждение crypto library;
- технический spike, прототипирование или интеграция;
- написание application code;
- build, unit/integration/security tests или benchmark;
- изменение main;
- создание Pull Request;
- изменение статусов Red-Team тестов.

**Следующее возможное действие:** после отдельной авторизации — провести evidence collection по legal/upstream/конкретным кандидатам и подготовить новый ADR. До этого D-01 остаётся OPEN, spike и production integration — NO-GO.

---

## 12. Formal Phase 0.8 disposition

| Decision | Result |
|---|---|
| Contingency paths documented | YES |
| libsignal selected | NO |
| Alternative selected | NO |
| D-01 closed | NO — OPEN |
| Legal blocker resolved | NO |
| Upstream blocker resolved | NO |
| Technical spike authorized | NO |
| Production integration approved | NO |
| Red-Team scenarios | 27 / 27 NOT_RUN |
| TOR MODE | EXCLUDED |
| Decentralization | FUTURE-ONLY |
| main changed | NO |
| PR created | NO |

**Final disposition: DOCUMENTATION COMPLETE / NO-GO FOR IMPLEMENTATION.**  
The next decision must be evidence-led and formally recorded in a reopened ADR. No candidate may be selected by assumption or convenience.
