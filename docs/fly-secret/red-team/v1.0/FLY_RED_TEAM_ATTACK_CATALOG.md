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

# Каталог атак

Каталог включает privacy, identity, relay, cryptography, recovery, file transfer, calls, future decentralization, supply chain и diagnostics.

- **ID:** PRIV-01
- **Сценарий:** Корреляция трафика и восстановление социального графа
- **Активы:** IP-адреса, временные метки, размеры пакетов, связи отправитель–получатель.
- **Предпосылки:** Два или более synthetic-клиента, собственный relay, размеченные шаблоны сообщений и лабораторный сбор trace.
- **Модель атакующего:** Оператор relay или наблюдатель лабораторного сегмента без доступа к plaintext.
- **Безопасный воспроизводимый тест:** Сгенерировать размеченные synthetic-переписки; снять trace; оценить, можно ли сопоставить пары по времени/размеру; повторить с предусмотренными batching/padding.
- **Ожидаемый результат:** Остаточная корреляция измерена; гарантии приватности не сильнее фактической защиты; чувствительные логи минимизированы.
- **Критерий провала:** Стабильные пары или граф общения восстанавливаются вопреки заявленным гарантиям.
- **Серьёзность:** High
- **Обнаружение:** Корреляционный анализ размеченных trace; precision/recall; аудит логов и retention.
- **Исправление:** Минимизировать метаданные; оценить batching/padding; ограничить retention; разделить сервисные идентификаторы.
- **Регрессионный тест:** Повторить для burst-трафика, offline delivery, reconnect и разных размеров сообщений.
- **Маппинг:** MASVS-PRIVACY; RFC 9458 (ограниченное применение)
- **Начальный статус:** `NOT_RUN`

- **ID:** PRIV-02
- **Сценарий:** Перечисление FLY ID через directory
- **Активы:** Пространство FLY ID, directory API, статусы существования пользователей.
- **Предпосылки:** Staging directory с synthetic ID; тестовый клиент; согласованный бюджет запросов.
- **Модель атакующего:** Зарегистрированный злоумышленник, проверяющий только собственный staging endpoint.
- **Безопасный воспроизводимый тест:** Выполнить ограниченный перебор синтетического диапазона; сравнить коды, размеры ответов, задержки и rate-limit для существующих/несуществующих тестовых ID.
- **Ожидаемый результат:** Массовое определение существования ID затруднено; ответы не раскрывают лишнее; abuse ограничен.
- **Критерий провала:** Высокоточное перечисление выполняется без существенных ограничений либо ответы явно раскрывают существование ID.
- **Серьёзность:** High
- **Обнаружение:** Timing/size analysis, rate-limit telemetry, security events.
- **Исправление:** Privacy-preserving discovery; единообразные ответы; rate limits; abuse detection; непредсказуемые ID.
- **Регрессионный тест:** Повторить несколькими synthetic-клиентами и при разных скоростях запросов.
- **Маппинг:** MASVS-PRIVACY; MASVS-AUTH
- **Начальный статус:** `NOT_RUN`

- **ID:** PRIV-03
- **Сценарий:** Связывание push token с FLY ID
- **Активы:** Push token, device identifier, payload, время доставки.
- **Предпосылки:** Собственный тестовый push gateway и synthetic аккаунты.
- **Модель атакующего:** Оператор push/backend, анализирующий только тестовые payload и логи.
- **Безопасный воспроизводимый тест:** Сгенерировать уведомления; проверить payload, analytics и server logs на FLY ID, имена, plaintext, постоянные идентификаторы и лишние временные метки.
- **Ожидаемый результат:** Push содержит минимальный opaque wake-up signal; чувствительные поля отсутствуют.
- **Критерий провала:** Payload или telemetry связывают постоянный идентификатор, пользователя и содержимое сверх необходимого.
- **Серьёзность:** High
- **Обнаружение:** Secret/DLP scan; schema review; аудит корреляции логов.
- **Исправление:** Разделить push и identity; ротировать токены по выбранной модели; исключить plaintext; ограничить retention и доступ.
- **Регрессионный тест:** Повторить на Android/iOS release-конфигурациях и для разных событий.
- **Маппинг:** MASVS-PRIVACY; MASVS-NETWORK
- **Начальный статус:** `NOT_RUN`

- **ID:** PRIV-04
- **Сценарий:** Утечка адресной книги при contact discovery
- **Активы:** Контакты, производные идентификаторы, contact discovery API.
- **Предпосылки:** Synthetic address book с фиктивными номерами; тестовый backend.
- **Модель атакующего:** Оператор сервиса или скомпрометированный telemetry SDK.
- **Безопасный воспроизводимый тест:** Передать синтетическую адресную книгу через тестовый flow; проверить поля, логи, crash reports и retention.
- **Ожидаемый результат:** Сервер не получает plaintext address book без отдельного обоснования; минимизация и защита discovery документированы.
- **Критерий провала:** В логах/backend обнаружены plaintext контакты или постоянные производные идентификаторы без необходимости.
- **Серьёзность:** High
- **Обнаружение:** Data-flow tracing, schema/log review, canary markers.
- **Исправление:** Рассмотреть PSI или иной privacy-preserving discovery; минимизировать поля/retention; запретить логирование.
- **Регрессионный тест:** Повторить для пустой/большой книги и сетевого отказа.
- **Маппинг:** MASVS-PRIVACY; MASVS-STORAGE
- **Начальный статус:** `NOT_RUN`

- **ID:** ID-01
- **Сценарий:** Подмена ключа в directory / split-view
- **Активы:** FLY ID binding, identity public keys, trust state, transparency proofs.
- **Предпосылки:** Два тестовых клиента и управляемый lab directory; тестовая модель key transparency.
- **Модель атакующего:** Злонамеренный оператор directory, выдающий разные ответы synthetic-клиентам.
- **Безопасный воспроизводимый тест:** Для одного synthetic FLY ID вернуть разные публичные ключи двум клиентам; проверить consistency/inclusion proofs и реакцию UI.
- **Ожидаемый результат:** Split-view обнаруживается; Verified сбрасывается; показывается предупреждение; интерфейс не маскирует состояние как verified.
- **Критерий провала:** Клиенты молча принимают разные ключи либо продолжают показывать Verified.
- **Серьёзность:** Critical
- **Обнаружение:** Сравнение tree heads/proofs; client event log; независимый monitor harness.
- **Исправление:** Append-only key transparency; signed tree heads; inclusion/consistency proofs; независимые monitors; QR/fingerprint verification.
- **Регрессионный тест:** Повторить при задержанных, отсутствующих, устаревших и противоречивых proof.
- **Маппинг:** MASVS-CRYPTO; Signal identity-verification concepts
- **Начальный статус:** `NOT_RUN`

- **ID:** ID-02
- **Сценарий:** Replay регистрации устройства
- **Активы:** Registration challenge, device public key, identity binding.
- **Предпосылки:** Staging identity service; synthetic keypairs; журнал транзакций.
- **Модель атакующего:** Атакующий повторяет запись ранее использованного тестового challenge/response.
- **Безопасный воспроизводимый тест:** Повторить challenge; изменить контекст устройства; отправить подпись с неверным domain/context.
- **Ожидаемый результат:** Challenge одноразовый, короткоживущий, привязан к действию и контексту; replay отклоняется.
- **Критерий провала:** Старый challenge/response повторно регистрирует устройство или меняет identity state.
- **Серьёзность:** Critical
- **Обнаружение:** Аудит регистрации, nonce state, security event logs.
- **Исправление:** CSPRNG nonce; expiry; атомарное одноразовое потребление; domain separation; подпись полного контекста.
- **Регрессионный тест:** Повторить после рестарта и конкурентными запросами.
- **Маппинг:** MASVS-AUTH; MASVS-CRYPTO
- **Начальный статус:** `NOT_RUN`

- **ID:** ID-03
- **Сценарий:** Клонирование устройства без private key
- **Активы:** Device ID, device public key, mailbox/session authorization.
- **Предпосылки:** Два тестовых устройства; synthetic account; доступ только к публичным идентификаторам.
- **Модель атакующего:** Атакующий копирует публичные поля и пытается выдать себя за зарегистрированное устройство.
- **Безопасный воспроизводимый тест:** Создать вторую регистрацию с занятым Device ID без proof-of-possession соответствующего private key; проверить доступ к mailbox.
- **Ожидаемый результат:** Клон отклонён; mailbox и session keys недоступны.
- **Критерий провала:** Клон получает ACTIVE или доступ к сообщениям без владения ключом.
- **Серьёзность:** Critical
- **Обнаружение:** Device registry audit; challenge verification; key-delivery logs.
- **Исправление:** Случайные device IDs; per-device keypairs; signed challenge; явные linking/revocation flows.
- **Регрессионный тест:** Повторить с replay, revoked device и восстановлением старого состояния.
- **Маппинг:** Signal Sesame; MASVS-AUTH
- **Начальный статус:** `NOT_RUN`

- **ID:** ID-04
- **Сценарий:** Ошибочное подтверждение контакта в UX
- **Активы:** Fingerprint/safety number, verification state, contact identity.
- **Предпосылки:** Два synthetic контакта; тестовые ключи и изменённые fingerprints.
- **Модель атакующего:** Злонамеренный directory или контакт, меняющий ключ.
- **Безопасный воспроизводимый тест:** Сменить ключ контакта; проверить чаты, push, список контактов и call UI на обновление состояния.
- **Ожидаемый результат:** Unknown/Verified/Key changed различаются; Verified показывается только для текущего проверенного ключа.
- **Критерий провала:** UI сохраняет Verified после смены ключа или скрывает предупреждение.
- **Серьёзность:** High
- **Обнаружение:** UI state tests; event audit; screenshot assertions на тестовых сборках.
- **Исправление:** Единый trust-state machine; сброс stale verified state; понятное предупреждение.
- **Регрессионный тест:** Повторить после переустановки, multi-device sync и смены профиля.
- **Маппинг:** MASVS-CRYPTO; MASVS-RESILIENCE
- **Начальный статус:** `NOT_RUN`

- **ID:** RELAY-01
- **Сценарий:** Drop, reorder, duplicate и corruption envelopes
- **Активы:** Encrypted envelopes, message IDs, receipts, ratchet state.
- **Предпосылки:** Два synthetic-клиента; собственный relay; fault-injection proxy.
- **Модель атакующего:** Злонамеренный relay, задерживающий, удаляющий, переставляющий и дублирующий сообщения.
- **Безопасный воспроизводимый тест:** В lab пропустить, переставить, продублировать и повредить envelopes; проверить обработку клиентами.
- **Ожидаемый результат:** Повреждение обнаруживается; дубли идемпотентны; delivery semantics корректны; ошибки видимы.
- **Критерий провала:** Изменённое сообщение принимается как корректное либо duplicate повторно выполняет действие.
- **Серьёзность:** High
- **Обнаружение:** Сверка message IDs, ACK, session state и клиентских событий.
- **Исправление:** Использовать проверенный протокол с AEAD; idempotency; ограниченные retries; явная модель receipts.
- **Регрессионный тест:** Повторить при offline, reconnect и multi-device.
- **Маппинг:** Signal Double Ratchet; Sesame
- **Начальный статус:** `NOT_RUN`

- **ID:** RELAY-02
- **Сценарий:** Исчерпание очереди и межпользовательская деградация
- **Активы:** Mailbox quotas, queue scheduler, storage, worker pool.
- **Предпосылки:** Изолированный relay с жёсткими тестовыми лимитами и disposable tenant.
- **Модель атакующего:** Synthetic-клиент создаёт контролируемый объём очереди.
- **Безопасный воспроизводимый тест:** Постепенно заполнить только lab mailbox в согласованном лимите; измерить влияние на другие synthetic tenants.
- **Ожидаемый результат:** Квоты/fair scheduling ограничивают влияние; сервис возвращает контролируемый отказ.
- **Критерий провала:** Один tenant исчерпывает общие ресурсы и нарушает доставку другим.
- **Серьёзность:** High
- **Обнаружение:** Queue depth, disk, memory, latency, per-tenant metrics.
- **Исправление:** Per-account quotas; TTL; backpressure; fair scheduling; admission control; circuit breakers.
- **Регрессионный тест:** Повторить с несколькими tenants, медленными workers и восстановлением после перегрузки.
- **Маппинг:** MASVS-CODE; availability engineering
- **Начальный статус:** `NOT_RUN`

- **ID:** CRYPTO-01
- **Сценарий:** Rollback ratchet state после восстановления backup
- **Активы:** Ratchet state, message keys, nonce/counter state, local DB.
- **Предпосылки:** Тестовая сессия; disposable backup/snapshot; тестовые ключи.
- **Модель атакующего:** Атакующий может вернуть старое локальное состояние тестового клиента.
- **Безопасный воспроизводимый тест:** Продвинуть сессию; восстановить старый snapshot в lab; продолжить обмен и проверить повторное использование key/nonce/state.
- **Ожидаемый результат:** Rollback обнаруживается либо сессия безопасно переустанавливается; повторное использование ключевого материала не происходит.
- **Критерий провала:** Старое состояние принимается и вызывает повторное использование key/nonce или обход replay protection.
- **Серьёзность:** Critical
- **Обнаружение:** Monotonic state markers; key-use instrumentation; protocol harness.
- **Исправление:** Атомарное сохранение state; anti-rollback strategy; safe session reset; обработка crash boundaries.
- **Регрессионный тест:** Повторить при аварийном завершении между этапами записи состояния.
- **Маппинг:** Signal Double Ratchet; MASVS-CRYPTO; MASVS-STORAGE
- **Начальный статус:** `NOT_RUN`

- **ID:** CRYPTO-02
- **Сценарий:** Exhaustion skipped message keys
- **Активы:** Skipped message keys, memory, CPU, session state.
- **Предпосылки:** Protocol harness и тестовые границы лимитов.
- **Модель атакующего:** Атакующий отправляет заголовки с большими пропусками счётчика.
- **Безопасный воспроизводимый тест:** Подать значения N-1, N, N+1 и выше лимита; измерить память, CPU и изменение session state.
- **Ожидаемый результат:** Есть документированный жёсткий лимит; некорректный пакет отклоняется без неограниченного роста ресурсов.
- **Критерий провала:** Неограниченное выделение памяти/диска, зависание или повреждение сессии.
- **Серьёзность:** High
- **Обнаружение:** Memory/CPU profiling; skipped-key counters; crash monitoring.
- **Исправление:** Ограничить skipped keys и TTL; rate-limit; безопасно отклонять аномальные сообщения.
- **Регрессионный тест:** Boundary tests и fuzz regression вокруг лимитов.
- **Маппинг:** Signal Double Ratchet
- **Начальный статус:** `NOT_RUN`

- **ID:** CRYPTO-03
- **Сценарий:** Downgrade криптографического набора/версии
- **Активы:** Protocol version, cipher suite, handshake transcript.
- **Предпосылки:** Два synthetic-клиента с тестовыми поддерживаемыми версиями.
- **Модель атакующего:** Атакующий контролирует тестовый relay/negotiation path.
- **Безопасный воспроизводимый тест:** Изменить/удалить поля версии и capability в lab handshake; проверить transcript binding и поведение несовместимых версий.
- **Ожидаемый результат:** Downgrade невозможен без явного безопасного перехода; неизвестная версия отклоняется.
- **Критерий провала:** Клиенты незаметно переходят на устаревший или небезопасный набор.
- **Серьёзность:** Critical
- **Обнаружение:** Handshake transcript audit; negotiated-version telemetry без секретов.
- **Исправление:** Аутентифицировать negotiation; domain separation; запрет небезопасного fallback; version policy.
- **Регрессионный тест:** Повторить с отсутствующим полем, старой версией и конфликтом capabilities.
- **Маппинг:** Signal specifications; MASVS-CRYPTO
- **Начальный статус:** `NOT_RUN`

- **ID:** REC-01
- **Сценарий:** Кража Recovery Kit и анализ утечек
- **Активы:** Recovery secret, encrypted backup, clipboard, logs, analytics.
- **Предпосылки:** Synthetic recovery kit и тестовый backup.
- **Модель атакующего:** Лицо с копией тестового секрета или оператор telemetry.
- **Безопасный воспроизводимый тест:** Проверить logs, clipboard, crash reports, analytics и backup access без секрета; реальные секреты не использовать.
- **Ожидаемый результат:** Секрет отсутствует в telemetry; сервер не может расшифровать backup; предупреждения объясняют последствия компрометации.
- **Критерий провала:** Секрет обнаружен в логах/telemetry или сервер может расшифровать пользовательский backup.
- **Серьёзность:** Critical
- **Обнаружение:** Secret scanning; key-access audit; data-flow review.
- **Исправление:** Client-side encryption; высокоэнтропийный секрет; проверенный KDF; redaction; минимизация clipboard exposure.
- **Регрессионный тест:** Повторить корректный/некорректный секрет и restore на Android/iOS.
- **Маппинг:** MASVS-STORAGE; MASVS-CRYPTO
- **Начальный статус:** `NOT_RUN`

- **ID:** REC-02
- **Сценарий:** Replay recovery token и race condition
- **Активы:** One-time recovery token, transaction state, device registry.
- **Предпосылки:** Staging recovery service; synthetic tokens; concurrency harness.
- **Модель атакующего:** Атакующий повторяет использованный токен или одновременно отправляет несколько запросов.
- **Безопасный воспроизводимый тест:** Применить токен один раз; повторить последовательно и конкурентно в disposable staging.
- **Ожидаемый результат:** Ровно одна разрешённая транзакция завершается; токен атомарно потребляется.
- **Критерий провала:** Повторное применение создаёт устройство, открывает backup или меняет identity state.
- **Серьёзность:** Critical
- **Обнаружение:** Token state audit; transaction IDs; concurrency tests.
- **Исправление:** Atomic consume; expiry; nonce; transaction binding; idempotency; rate limits.
- **Регрессионный тест:** Повторить race, retry, timeout и перезапуск сервиса.
- **Маппинг:** MASVS-AUTH
- **Начальный статус:** `NOT_RUN`

- **ID:** REC-03
- **Сценарий:** Неавторизованное связывание нового устройства
- **Активы:** Device registry, key delivery, account trust state.
- **Предпосылки:** Три synthetic устройства; staging linking flow.
- **Модель атакующего:** Атакующий инициирует pairing без доверенного подтверждения.
- **Безопасный воспроизводимый тест:** Попытаться связать устройство без approval существующего trusted device и без корректного recovery proof.
- **Ожидаемый результат:** Новое устройство остаётся PENDING и не получает ключи до авторизации.
- **Критерий провала:** Устройство получает ACTIVE или ключи без требуемого подтверждения.
- **Серьёзность:** Critical
- **Обнаружение:** State-transition audit; key-delivery logs.
- **Исправление:** Явное approval; короткоживущий pairing; подписанный transcript; уведомление доверенных устройств.
- **Регрессионный тест:** Повторить offline/timeout, истечение QR и повторное использование pairing nonce.
- **Маппинг:** Signal Sesame; MASVS-AUTH
- **Начальный статус:** `NOT_RUN`

- **ID:** FILE-01
- **Сценарий:** IDOR / object-level authorization в файловом API
- **Активы:** File IDs, object storage, download/thumbnail/delete endpoints.
- **Предпосылки:** Два synthetic аккаунта A/B и отдельные тестовые файлы.
- **Модель атакующего:** Пользователь A обращается к объекту B.
- **Безопасный воспроизводимый тест:** От A запросить file ID B через metadata, download, thumbnail, delete, share и retry endpoints.
- **Ожидаемый результат:** Все неавторизованные операции запрещены; ответы не раскрывают лишние метаданные.
- **Критерий провала:** A получает содержимое/thumbnail/metadata либо может удалить объект B.
- **Серьёзность:** Critical
- **Обнаружение:** Negative authorization suite; audit logs; API policy tests.
- **Исправление:** Object-level authorization на сервере; scoped capabilities; короткоживущие URL; проверка каждой операции.
- **Регрессионный тест:** Повторить для ролей, методов и вариантов URL.
- **Маппинг:** MASVS-AUTH; MASVS-NETWORK
- **Начальный статус:** `NOT_RUN`

- **ID:** FILE-02
- **Сценарий:** Утечка EXIF/GPS и скрытых метаданных
- **Активы:** EXIF, GPS, camera serial, timestamps, file names.
- **Предпосылки:** Фиктивные изображения/видео с canary EXIF markers.
- **Модель атакующего:** Получатель или storage operator анализирует тестовый файл.
- **Безопасный воспроизводимый тест:** Загрузить synthetic file с включённой/выключенной очисткой; сравнить original, outgoing copy, thumbnail и download.
- **Ожидаемый результат:** Поведение соответствует выбору пользователя; очистка удаляет заявленные поля; UX точно описывает ограничения.
- **Критерий провала:** GPS/serial остаются при включённой очистке либо UX вводит в заблуждение.
- **Серьёзность:** Medium
- **Обнаружение:** EXIF parser; binary diff; metadata inventory.
- **Исправление:** Локальная очистка; отдельная отправляемая копия; явный UX; документировать исключения форматов.
- **Регрессионный тест:** Повторить JPEG/HEIC/PNG/MP4 и разные контейнеры.
- **Маппинг:** MASVS-PRIVACY; MASVS-STORAGE
- **Начальный статус:** `NOT_RUN`

- **ID:** FILE-03
- **Сценарий:** Parser abuse и чрезмерный размер файла
- **Активы:** Media parsers, thumbnails, decompression, memory/disk quotas.
- **Предпосылки:** Изолированная тестовая сборка; безопасные тестовые файлы.
- **Модель атакующего:** Злонамеренный контакт отправляет аномальный файл.
- **Безопасный воспроизводимый тест:** Проверить граничные размеры, повреждённые заголовки, необычное разрешение и чрезмерные metadata в локальном harness.
- **Ожидаемый результат:** Файл отклоняется или безопасно обрабатывается с ограничением ресурсов.
- **Критерий провала:** Crash, unbounded allocation, path traversal, зависание или выход за sandbox.
- **Серьёзность:** High
- **Обнаружение:** Crash/fuzz harness; memory limits; parser telemetry.
- **Исправление:** Ограничения размера/распаковки; sandbox; обновление библиотек; безопасная обработка ошибок.
- **Регрессионный тест:** Regression corpus для каждого malformed sample.
- **Маппинг:** MASVS-CODE; MASVS-STORAGE
- **Начальный статус:** `NOT_RUN`

- **ID:** CALL-01
- **Сценарий:** Подмена signalling и replay приглашения
- **Активы:** Call invite, SDP/ICE metadata, peer identity binding.
- **Предпосылки:** Два клиента и собственный signalling server.
- **Модель атакующего:** Злонамеренный staging relay/signalling operator.
- **Безопасный воспроизводимый тест:** Изменить synthetic invite, подменить peer binding и повторить старое приглашение в lab.
- **Ожидаемый результат:** Подмена отклоняется до доверенного звонка; replay распознаётся.
- **Критерий провала:** Звонок устанавливается с подменённой peer/session identity.
- **Серьёзность:** Critical
- **Обнаружение:** Signalling transcript audit; nonce/session binding checks.
- **Исправление:** Authenticated signalling; nonce; expiry; transcript binding; identity verification UX.
- **Регрессионный тест:** Повторить изменение каждого критического поля и reconnect.
- **Маппинг:** RFC 8827; MASVS-NETWORK
- **Начальный статус:** `NOT_RUN`

- **ID:** CALL-02
- **Сценарий:** Проверка E2EE media при использовании SFU
- **Активы:** Media ciphertext, media key schedule, SFU credentials.
- **Предпосылки:** Три synthetic клиента и собственный test SFU; canary media stream.
- **Модель атакующего:** Оператор test SFU без endpoint keys.
- **Безопасный воспроизводимый тест:** Провести лабораторный звонок; проверить владение ключами; убедиться, что SFU не расшифровывает тестовый поток.
- **Ожидаемый результат:** Расшифровка доступна только endpoint clients; SFU не владеет media keys.
- **Критерий провала:** SFU credentials или server config позволяют получить plaintext media.
- **Серьёзность:** Critical
- **Обнаружение:** Key ownership review; packet analysis; endpoint/SFU harness.
- **Исправление:** Отдельное E2EE media шифрование; проверенный key schedule; ротация; независимый review.
- **Регрессионный тест:** Повторить reconnect, key rotation и изменение состава участников.
- **Маппинг:** RFC 8827; отдельная media E2EE specification
- **Начальный статус:** `NOT_RUN`

- **ID:** CALL-03
- **Сценарий:** Утечка IP через ICE candidates
- **Активы:** IP-адреса, ICE candidates, STUN/TURN path.
- **Предпосылки:** Два тестовых устройства в контролируемых сетях; собственные STUN/TURN endpoints.
- **Модель атакующего:** Собеседник или signalling observer видит ICE candidates.
- **Безопасный воспроизводимый тест:** Снять trace собственного звонка; проверить candidate types и фактический маршрут; сравнить privacy settings.
- **Ожидаемый результат:** Раскрытие IP соответствует документированной модели и выбору пользователя; UI не обещает сокрытие без основания.
- **Критерий провала:** Реальный IP раскрывается вопреки явному обещанию/настройке.
- **Серьёзность:** High
- **Обнаружение:** PCAP только на собственных endpoints; ICE candidate audit.
- **Исправление:** Понятные TURN/relay настройки и trade-offs; безопасные defaults; точная документация.
- **Регрессионный тест:** Повторить Wi-Fi/mobile, IPv4/IPv6 и reconnect.
- **Маппинг:** RFC 8827; MASVS-NETWORK
- **Начальный статус:** `NOT_RUN`

- **ID:** DEC-01
- **Сценарий:** Sybil-концентрация узлов
- **Активы:** Node identities, routing table, operator ownership, quorum.
- **Предпосылки:** Локальная симуляция узлов; synthetic operator identities; фиксированная модель сети.
- **Модель атакующего:** Один оператор создаёт множество synthetic nodes.
- **Безопасный воспроизводимый тест:** Увеличивать долю узлов одного оператора в симуляции; измерять контроль маршрутов, репликации и quorum.
- **Ожидаемый результат:** Концентрация измеряется и ограничивается политикой; сеть не представляется независимой при концентрации.
- **Критерий провала:** Один оператор незаметно контролирует критическую долю маршрутов/решений.
- **Серьёзность:** Critical
- **Обнаружение:** Operator diversity metrics; route/quorum simulation; ownership clustering.
- **Исправление:** Sybil resistance; operator diversity; admission/governance controls; независимые bootstrap sources.
- **Регрессионный тест:** Повторить при разных размерах сети, churn и colluding operators.
- **Маппинг:** Future decentralization threat model
- **Начальный статус:** `NOT_RUN`

- **ID:** DEC-02
- **Сценарий:** Eclipse / изоляция клиента
- **Активы:** Peer discovery, bootstrap sources, routing view.
- **Предпосылки:** Изолированный клиент и собственный набор lab peers.
- **Модель атакующего:** Оператор контролирует часть discovery/bootstrap инфраструктуры.
- **Безопасный воспроизводимый тест:** В локальной сети ограничить доступ к честным peers; проверить peer diversity, consistency и предупреждения.
- **Ожидаемый результат:** Клиент обнаруживает деградацию diversity и не считает односторонний view полным.
- **Критерий провала:** Злоумышленник контролирует view клиента без обнаружения или удерживает его в изоляции.
- **Серьёзность:** High
- **Обнаружение:** Peer diversity metrics; signed record checks; route graph.
- **Исправление:** Несколько независимых bootstrap sources; diversity-aware routing; consistency checks; recovery path.
- **Регрессионный тест:** Повторить churn/reconnect, смену bootstrap и partition.
- **Маппинг:** Future decentralization threat model
- **Начальный статус:** `NOT_RUN`

- **ID:** DEC-03
- **Сценарий:** Подмена узла и ротация node key
- **Активы:** Node signing keys, signed advertisements, revocation state.
- **Предпосылки:** Staging node registry; synthetic node keys.
- **Модель атакующего:** Атакующий публикует поддельную запись узла или использует устаревший ключ.
- **Безопасный воспроизводимый тест:** Изменить endpoint/key в node advertisement без подписи владельца; проверить stale/revoked key.
- **Ожидаемый результат:** Поддельная запись отклоняется; ротация и отзыв ключа проверяются криптографически.
- **Критерий провала:** Клиенты доверяют неподписанной записи или отозванному ключу.
- **Серьёзность:** Critical
- **Обнаружение:** Signature validation logs; node registry audit.
- **Исправление:** Подписанные объявления; key rotation; revocation; expiry; проверяемые trust roots.
- **Регрессионный тест:** Повторить с задержанным обновлением, rollback и старым ключом.
- **Маппинг:** Future decentralization threat model; MASVS-CRYPTO
- **Начальный статус:** `NOT_RUN`

- **ID:** SUPPLY-01
- **Сценарий:** Подмена зависимости или build artifact в CI
- **Активы:** Source tree, lockfiles, dependency checksums, CI artifacts, release signing.
- **Предпосылки:** Изолированный CI sandbox; тестовая зависимость; disposable pipeline.
- **Модель атакующего:** Атакующий меняет metadata тестовой зависимости или артефакт до подписи.
- **Безопасный воспроизводимый тест:** Изменить checksum/tag/lockfile в тестовом pipeline; проверить review/provenance gates; не использовать production secrets.
- **Ожидаемый результат:** Неодобренный dependency drift или артефакт блокирует выпуск; provenance проверяется.
- **Критерий провала:** Подмена попадает в релизный артефакт или подпись выполняется без ожидаемых проверок.
- **Серьёзность:** Critical
- **Обнаружение:** SBOM diff; provenance verification; CI audit; artifact hash comparison.
- **Исправление:** Pinned dependencies; review; isolated signing; signed provenance; минимальные CI permissions.
- **Регрессионный тест:** Повторить checksum mismatch, tag movement и artifact substitution.
- **Маппинг:** MASVS-CODE; supply-chain security
- **Начальный статус:** `NOT_RUN`

- **ID:** LOG-01
- **Сценарий:** Секреты и plaintext в логах/crash reports
- **Активы:** Plaintext markers, private keys, recovery tokens, auth tokens.
- **Предпосылки:** Synthetic canary secrets; debug/release test builds; собственная telemetry staging.
- **Модель атакующего:** Оператор telemetry или локальный пользователь с доступом к диагностике.
- **Безопасный воспроизводимый тест:** Выполнить тестовые registration/message/recovery/file и controlled crash flows; просканировать logs/crash payloads на canary markers.
- **Ожидаемый результат:** Plaintext, private keys, recovery secrets и auth tokens отсутствуют в диагностике.
- **Критерий провала:** Любой canary secret или plaintext marker найден в логах/crash payloads.
- **Серьёзность:** High
- **Обнаружение:** Secret scanning; log review; telemetry schema audit.
- **Исправление:** Allowlist logging; redaction; crash scrubber; запрет секретных полей; разделение debug/release.
- **Регрессионный тест:** Повторить на Android/iOS и для разных типов ошибок.
- **Маппинг:** MASVS-STORAGE; MASVS-PRIVACY
- **Начальный статус:** `NOT_RUN`

## Сквозные риски

1. **Privacy join:** contact discovery, push, временные метки, call signalling и telemetry могут совместно деанонимизировать псевдонимного пользователя.
2. **Endpoint compromise:** E2EE не защищает plaintext после отображения на скомпрометированном устройстве.
3. **Recovery escalation:** recovery не должен обходить device revocation, key-change warnings или контактную верификацию.
4. **Malicious update:** CI, signing keys и release artifacts входят в криптографическую границу доверия.
5. **Multi-device cascade:** компрометация устройства раскрывает доступные ему данные; удалённо гарантированно удалить скопированный plaintext нельзя.
6. **Metadata accumulation:** отсутствие plaintext на сервере не означает отсутствия метаданных.
7. **Decentralization ≠ anonymity:** распределение узлов не устраняет traffic correlation, Sybil или collusion.

## Формат отчёта о находке

Для каждой находки фиксировать Test ID, commit/build hash, окружение, OS/device, шаги, фактический и ожидаемый результат, impact, severity rationale, sanitized evidence, remediation owner, срок, regression test и повторный статус.