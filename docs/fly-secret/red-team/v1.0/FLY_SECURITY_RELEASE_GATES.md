# FLY SECRET — Security Release Gates

- Версия: 1.1 (Phase 0.1 review)
- Базовая спецификация: Red-Team v1.0
- Статус: требования и критерии допуска; тесты не выполнялись
- Все 27 сценариев: `NOT_RUN`
- TOR MODE: исключён
- Децентрализация: future-only
- Реализация приложения: не начиналась

## Release policy

Решение по умолчанию до появления evidence — **NO-GO**. `PASS` требует фактического исполнения теста, commit/build hash и воспроизводимого evidence. `NOT_RUN` и `BLOCKED` не считаются PASS. `N/A` требует письменного обоснования и reviewer approval.

- Critical findings всегда блокируют выпуск до исправления и регрессионной проверки.
- High findings блокируют публичный выпуск в crypto, identity, recovery, privacy, file authorization, calls и supply chain.
- Никакие marketing claims об абсолютной анонимности, невозможности скриншота или безусловном E2EE не допускаются без доказанной модели и независимого review.

## Gate 0 — Architecture and threat model

- [ ] Data-flow diagrams: identity, messaging, files, calls, recovery, push, telemetry.
- [ ] Trust boundaries, assets, attacker capabilities и platform scope утверждены.
- [ ] Crypto ADR содержит точную библиотеку/версию, лицензию, аудит, maintenance и платформенную поддержку.
- [ ] Нет самодельного протокола из самостоятельно скомбинированных криптографических примитивов.
- [ ] 1:1, group messaging, multi-device и recovery scope определены.
- [ ] Web threat model отделён от native mobile.
- [ ] TOR MODE отсутствует в requirements, architecture, UI, config, workflows и tests.
- [ ] Децентрализация не включена в текущий release scope.

Evidence: architecture decision records, diagrams, threat model approval, dependency review.

## Gate 1 — Identity and device lifecycle

- [ ] FLY ID отделён от identity key, device key, session key и transport identifiers.
- [ ] Registration challenge одноразовый, с expiry и proof-of-possession.
- [ ] Device linking требует явного trusted approval.
- [ ] Device revocation, rotation и session invalidation определены.
- [ ] Key transparency split-view и key-change flows проверяются.
- [ ] Verified UI привязан к текущему fingerprint и сбрасывается при смене ключа.

Required scenarios: ID-01…ID-04. All require executed evidence before release.

## Gate 2 — Cryptography and messaging

- [ ] Внедрена reviewed protocol implementation; точные версии зафиксированы.
- [ ] Forward secrecy и post-compromise recovery описаны без чрезмерных обещаний.
- [ ] Ratchet state persistence, crash consistency и rollback resistance проверены.
- [ ] Skipped-key/resource limits ограничены.
- [ ] Protocol negotiation защищён от downgrade.
- [ ] Replay/reorder/duplicate/corrupt handling проверены.
- [ ] PQ claims соответствуют фактически поддерживаемой и независимо проверенной реализации.
- [ ] Group protocol (если включён) имеет отдельный обоснованный выбор; MLS не считается автоматической заменой 1:1 сессий.

Required scenarios: CRYPTO-01…03, RELAY-01.

## Gate 3 — Recovery

- [ ] Device Transfer, Recovery Kit и Identity Re-establishment — отдельные flows.
- [ ] Сервер не хранит plaintext private keys/recovery secrets и не может расшифровать recovery backup.
- [ ] Token replay/race и unauthorized linking проверены.
- [ ] Потеря всех recovery materials объяснена пользователю как возможная необратимая потеря доступа.
- [ ] Старые устройства/сессии отзываются согласно явной политике.

Required scenarios: REC-01…03.

## Gate 4 — Privacy and metadata

- [ ] Data inventory, retention, deletion и access policies утверждены.
- [ ] Push содержит только минимальный opaque wake-up signal.
- [ ] Plaintext, private keys, recovery secrets и sensitive IDs отсутствуют в logs/analytics/crash reports.
- [ ] Contact discovery прошёл privacy review.
- [ ] FLY ID enumeration resistance проверена.
- [ ] Metadata correlation измерена; остаточные риски документированы.
- [ ] IP/call metadata disclosures совпадают с UI и документацией.
- [ ] Нет claims об абсолютной анонимности без adversary model.

Required scenarios: PRIV-01…04, CALL-03, LOG-01.

## Gate 5 — Local storage and file transfer

- [ ] Local DB, key storage, OS backup/restore и temporary files проверены для каждой платформы.
- [ ] Object-level authorization применяется на каждом file endpoint.
- [ ] EXIF/GPS handling документирован и протестирован.
- [ ] File size/type/parser/time/resource limits заданы.
- [ ] Download, preview, thumbnail, delete и share endpoints покрыты authorization tests.
- [ ] Deletion semantics и остаточные копии объяснены пользователю.

Required scenarios: FILE-01…03.

## Gate 6 — Voice/video calls

- [ ] Signalling authenticated; replay/substitution защищены.
- [ ] Application-level E2EE media доказана отдельно от DTLS-SRTP transport security.
- [ ] SFU не имеет media decryption keys.
- [ ] Media key rotation и membership changes проверены.
- [ ] ICE/IP disclosure и STUN/TURN behavior проверены в собственной лаборатории.
- [ ] Abuse/resource limits определены.

Required scenarios: CALL-01…03. Независимый media E2EE review обязателен.

## Gate 7 — Relay and availability

- [ ] Drop/reorder/duplicate/corrupt/delay fault matrix пройдена.
- [ ] Idempotency и delivery/receipt semantics определены.
- [ ] Per-tenant queue quotas, backpressure и fair scheduling проверены.
- [ ] Cross-tenant isolation подтверждена.
- [ ] Monitoring, abuse response и incident procedures готовы.

Required scenarios: RELAY-01…02.

## Gate 8 — Supply chain, CI and Android release

- [ ] GitHub Actions workflows используют минимальные permissions.
- [ ] Third-party Actions закреплены по immutable commit SHA.
- [ ] Dependencies pinned/locked; dependency review и vulnerability policy включены.
- [ ] SBOM формируется и архивируется.
- [ ] Artifact provenance/checksum проверяются.
- [ ] Signing keys/keystore не находятся в Git и не передаются через чат.
- [ ] Production signing доступен только через protected environment/approval.
- [ ] Feature branches не могут автоматически опубликовать production-signed APK.
- [ ] APK/AAB artifacts привязаны к commit, workflow run и version.
- [ ] Rollback и compromised signing key response документированы.

Required scenario: SUPPLY-01. Build pipeline security review required before first public APK.

## Gate 9 — Future decentralization (not in current release)

Этот gate не является условием текущего MVP и не разрешает включать децентрализованную сеть. Он применяется только после отдельного product/security approval.

- [ ] Node identity, key rotation/revocation и signed announcements.
- [ ] Sybil resistance и operator diversity.
- [ ] Eclipse/partition simulations.
- [ ] Collusion and ownership concentration analysis.
- [ ] Multi-node message integrity, storage and resource abuse limits.
- [ ] Независимый аудит governance и operator independence.

Future scenarios: DEC-01…DEC-03. До отдельного решения остаются NOT_RUN и out of scope.

## Gate 10 — Final release decision

Release dossier must contain:
- release tag, commit SHA, build/workflow run and artifact checksum;
- full test matrix with statuses and links to sanitized evidence;
- all open Critical/High findings, owner, remediation and retest;
- protocol/library versions and independent review reports;
- SBOM, provenance and signing approval;
- privacy/data-retention statement and user-facing security limitations;
- incident response, rollback and recovery plans;
- named approvers and dated GO/NO-GO decision.

### Decision rules

| Finding/status | Public release decision |
|---|---|
| Any open Critical | NO-GO |
| High in crypto/identity/recovery/privacy/auth/calls/supply chain | NO-GO |
| NOT_RUN or BLOCKED for a mandatory gate | NO-GO |
| N/A without written rationale | NO-GO |
| Medium/Low | Formal risk acceptance, owner, deadline and mitigation |
| All mandatory gates passed with evidence | Eligible for GO review; not automatic approval |

## Current status

No application code exists in this workstream and no security test has been executed. This document defines future release criteria; it does not certify the product.

- Test scenarios: 27 × `NOT_RUN`
- Architecture decision: CONDITIONAL NO-GO for implementation pending ADRs
- TOR MODE: excluded
- Decentralization: future-only
- APK: future GitHub Actions artifact after Android implementation
