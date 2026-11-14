# FLY SECRET — Red-Team Test Specification

**Версия:** 1.0  
**Тип:** проектная спецификация и план тестирования  
**Исполнение:** все сценарии имеют начальный статус `NOT_RUN`; тесты не запускались.  
**Реализация приложения:** не начиналась и не входит в задачу этого пакета.  
**TOR MODE:** исключён из требований, архитектуры, UI, конфигурации и тестов.  
**Децентрализация:** сохранена как отдельное будущее архитектурное направление; сейчас не реализуется.

## Правила безопасного тестирования

1. Только изолированная лабораторная или staging-среда, принадлежащая проекту; synthetic accounts, synthetic data и тестовые ключи.
2. Не атаковать production, реальные пользовательские аккаунты, сторонние системы или инфраструктуру третьих лиц.
3. Destructive-тесты — только на disposable state с проверяемым восстановлением.
4. Нагрузку ограничивать заранее заданными лимитами собственной лаборатории.
5. Не помещать plaintext, private keys, recovery secrets, токены и персональные данные в отчёты; доказательства редактировать.
6. `NOT_RUN`, `BLOCKED` и `N/A` не считаются `PASS`.
7. Отсутствие найденной уязвимости не является доказательством её отсутствия.

## Статусы

- `NOT_RUN` — сценарий описан, но не выполнялся.
- `PASS` — выполнен, ожидаемый результат подтверждён доказательствами.
- `FAIL` — критерий безопасности нарушен.
- `BLOCKED` — выполнение невозможно из-за отсутствующей реализации, среды или зависимости.
- `N/A` — неприменим; требуется письменное обоснование.

## Серьёзность

- **Critical:** компрометация plaintext, ключей, криптографической идентичности, доверенного устройства или цепочки выпуска.
- **High:** существенная утечка приватности, нарушение авторизации, масштабная деградация доступности или контроля устройств.
- **Medium:** ограниченное нарушение с существенными предпосылками или ограниченным охватом.
- **Low:** ограниченный эффект без прямой компрометации ключей или сообщений.

## Обязательные поля каждого сценария

ID; активы; предпосылки; модель атакующего; безопасный воспроизводимый тест; ожидаемый результат; критерий провала; серьёзность; обнаружение; исправление; регрессионный тест; нормативный/технический маппинг; начальный статус `NOT_RUN`.

# Security Release Gates

## Политика выпуска

`PASS` требует фактического исполнения теста и сохранённого evidence. `NOT_RUN` и `BLOCKED` не являются PASS. `N/A` требует обоснования. До появления evidence решение по умолчанию — **NO-GO**. Critical findings блокируют выпуск. High findings блокируют публичный выпуск, особенно в crypto/privacy/auth/recovery.

## Gate 0 — Architecture and threat model

- [ ] Data-flow diagrams для identity, messaging, files, calls, recovery, push и telemetry.
- [ ] Trust boundaries и классификация данных.
- [ ] Threat model утверждён.
- [ ] TOR MODE отсутствует в требованиях, UI, конфигурации и тестовой матрице.
- [ ] Decentralization выделена как будущий этап.
- [ ] Crypto libraries/spec versions, лицензии, платформы и maintenance status проверены.
- [ ] Нет самодельного протокола из самостоятельно скомбинированных примитивов.
- [ ] Web threat model отделён от native mobile threat model.

## Gate 1 — Identity and cryptography

- [ ] Registration proof-of-possession и replay protection.
- [ ] Key-change detection и contact verification.
- [ ] Key transparency split-view tests.
- [ ] Ratchet replay/rollback/nonce/skipped-key tests.
- [ ] Device linking, revocation и rekey tests.
- [ ] Post-compromise recovery semantics документированы.
- [ ] PQ claims соответствуют реально внедрённым и проверенным компонентам.
- [ ] Protocol negotiation защищён от downgrade.

## Gate 2 — Privacy

- [ ] Field-level data inventory и retention policy.
- [ ] Нет plaintext/recovery secret/private key в push, logs, analytics или crash reports.
- [ ] Contact discovery прошёл privacy review.
- [ ] Enumeration resistance проверена.
- [ ] Call IP exposure документирован и совпадает с UX.
- [ ] Metadata correlation измерена; residual risks опубликованы.
- [ ] Third-party SDK review завершён.
- [ ] Нет абсолютных обещаний анонимности без adversary model.

## Gate 3 — Local storage, recovery and files

- [ ] Local DB и key management прошли review.
- [ ] OS backup inclusion/exclusion проверена на каждой платформе.
- [ ] Recovery не требует server-side plaintext escrow.
- [ ] Recovery replay/race tests пройдены.
- [ ] Object-level authorization проверена на каждом файловом endpoint.
- [ ] File parser limits/fuzzing выполнены в lab.
- [ ] EXIF/metadata behavior документирован и проверен.
- [ ] Семантика удаления и остаточные копии объяснены пользователю.

## Gate 4 — Voice/video calls

- [ ] Signalling authentication и replay protection.
- [ ] E2EE media проверена отдельно; DTLS-SRTP само по себе не доказывает E2EE через SFU.
- [ ] Key rotation и membership change проверены.
- [ ] IP disclosure policy проверена.
- [ ] SFU не имеет ключей расшифрования media.
- [ ] Abuse/rate limits и resource limits определены.

## Gate 5 — Relay and delivery

- [ ] Drop/reorder/duplicate/corrupt/delay matrix пройдена.
- [ ] Queue quotas и backpressure проверены.
- [ ] Idempotency подтверждена.
- [ ] Delivery semantics и receipts формализованы.
- [ ] Tenant isolation проверена.
- [ ] Monitoring и incident response готовы.

## Gate 6 — Decentralization (только если она будет отдельно утверждена и введена)

- [ ] Signed node identity и rotation.
- [ ] Sybil/Eclipse simulations.
- [ ] Operator diversity/collusion analysis.
- [ ] Node revocation.
- [ ] Multi-node delivery integrity.
- [ ] Storage/resource abuse limits.
- [ ] Независимый аудит архитектуры и governance.

## Gate 7 — Supply chain and release

- [ ] SBOM сформирован и архивирован.
- [ ] Dependencies pinned и reviewed.
- [ ] Build provenance и artifact signatures проверяются.
- [ ] CI secrets изолированы; permissions минимальны.
- [ ] Release signing keys защищены контролируемым процессом.
- [ ] Reproducible-build strategy оценена.
- [ ] Critical/High dependency findings устранены или исключены из релиза.
- [ ] Rollback и incident response отрепетированы.

## Evidence requirements

Для каждого теста сохранять ID, commit/build hash, версию приложения, OS/device, версии инструментов, конфигурацию лаборатории, шаги, sanitized logs/PCAP, результат, reviewer и дату. Не сохранять реальные пользовательские данные, секреты и private keys.

## Независимые проверки перед публичным выпуском

- Независимый cryptographic protocol review.
- OWASP MASVS/MASTG mobile assessment.
- Staging backend/API penetration test на собственных системах.
- Privacy/metadata assessment.
- Supply-chain/build pipeline review.
- Отдельная проверка E2EE media.
- Regression verification после исправлений.

## Decision record

Решение `GO / NO-GO / CONDITIONAL GO` включает версию/commit и артефакты; статусы тестов и evidence; открытые Critical/High findings; принятые риски, владельцев и сроки; независимые аудиты; incident response plan.

Пока evidence отсутствует, это план контроля, а не подтверждение безопасности продукта.