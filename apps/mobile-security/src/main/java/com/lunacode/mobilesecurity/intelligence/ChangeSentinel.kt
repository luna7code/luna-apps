package com.lunacode.mobilesecurity.intelligence

import org.json.JSONArray
import org.json.JSONObject

data class SentinelChange(
    val kind: String,
    val title: String,
    val detail: String,
    val level: String,
    val packageName: String? = null
)
data class SentinelReport(val isFirstBaseline: Boolean, val changes: List<SentinelChange>)
data class SentinelApp(
    val packageName: String, val label: String, val version: String, val signer: String,
    val permissions: String, val installer: String, val updatedAt: Long,
    val signerHistory: String = signer,
    val multipleSigners: Boolean? = null
)
data class SentinelSnapshot(
    val schema: Int, val inventoryPresent: Boolean, val apps: List<SentinelApp>,
    val findingIds: Set<String>
)

object ChangeSentinel {
    fun enrich(previousEncryptedPlaintext: String?, currentPayload: String): String {
        val current = JSONObject(currentPayload)
        val previous = previousEncryptedPlaintext?.let { runCatching { JSONObject(it) }.getOrNull() }
        val firstBaseline = previous == null || previous.optInt("schema") < 2
        val changes = if (firstBaseline) emptyList()
        else compareSnapshots(snapshot(previous!!), snapshot(current))
        val changeArray = JSONArray()
        changes.forEach { change ->
            changeArray.put(JSONObject().apply {
                put("kind", change.kind); put("title", change.title)
                put("detail", change.detail); put("level", change.level)
                change.packageName?.let { put("packageName", it) }
            })
        }
        current.put("schema", 2)
        current.put("baselineCreated", firstBaseline)
        current.put("changes", changeArray)
        current.put("changeCount", changes.size)
        return current.toString()
    }

    fun read(json: JSONObject): SentinelReport {
        val array = json.optJSONArray("changes") ?: JSONArray()
        val changes = buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(SentinelChange(
                    item.optString("kind", "unknown"),
                    item.optString("title", "Изменение"),
                    item.optString("detail", ""),
                    item.optString("level", "INFO"),
                    item.optString("packageName").takeIf { it.isNotBlank() }
                ))
            }
        }
        return SentinelReport(json.optBoolean("baselineCreated"), changes)
    }

    /** Empty, present inventories are compared; missing inventories are not treated as empty. */
    fun compareSnapshots(previous: SentinelSnapshot, current: SentinelSnapshot): List<SentinelChange> {
        if (!previous.inventoryPresent || !current.inventoryPresent) return emptyList()
        val before = previous.apps.associateBy { it.packageName }
        val after = current.apps.associateBy { it.packageName }
        val changes = mutableListOf<SentinelChange>()
        (after.keys - before.keys).sorted().forEach { packageName ->
            val app = after.getValue(packageName)
            changes += SentinelChange(
                "installed", "Установлено новое приложение: ${app.label}",
                "Пакет $packageName появился после предыдущей проверки. Установочный источник, сообщённый Android: ${app.installer.ifBlank { "неизвестен" }}. Проверьте издателя и источник установки.",
                "MEDIUM", packageName
            )
        }
        (before.keys - after.keys).sorted().forEach { packageName ->
            val app = before.getValue(packageName)
            changes += SentinelChange(
                "removed", "Приложение удалено: ${app.label}",
                "Пакет $packageName больше не обнаружен. Если удаление было ожидаемым, дополнительных действий не требуется.",
                "INFO", packageName
            )
        }
        (before.keys intersect after.keys).sorted().forEach { packageName ->
            val old = before.getValue(packageName); val new = after.getValue(packageName)
            if (old.signer.isNotBlank() && new.signer.isNotBlank()) {
                when (compareSignerLineage(old, new)) {
                    SigningLineageResult.VERIFIED_FORWARD_ROTATION ->
                        changes += SentinelChange(
                            "signer_rotated", "Подтверждена ротация ключа приложения: ${new.label}",
                            "Android подтвердил proof-of-rotation от предыдущего сертификата к текущему. Это штатная ротация подписи, а не самостоятельный признак компрометации.",
                            "INFO", packageName
                        )
                    SigningLineageResult.HISTORICAL_SIGNER ->
                        changes += SentinelChange(
                            "signer_historical", "Обнаружен исторический сертификат приложения: ${new.label}",
                            "Текущий APK использует сертификат из предыдущей истории подписи, но не последний известный ключ. Возможны downgrade или установка старой сборки; проверьте версию и источник.",
                            "HIGH", packageName
                        )
                    SigningLineageResult.MISMATCH, SigningLineageResult.MULTI_SIGNER_MISMATCH ->
                        changes += SentinelChange(
                            "signer_changed", "Изменился сертификат приложения: ${new.label}",
                            "Новый набор сертификатов не совпадает с предыдущей идентичностью; Android не подтвердил допустимую ротацию. Проверьте издателя и источник APK.",
                            "HIGH", packageName
                        )
                    SigningLineageResult.UNKNOWN ->
                        changes += SentinelChange(
                            "signer_change_unverified", "Изменился сертификат приложения: ${new.label}",
                            "В снимках недостаточно данных о multi-signer/истории сертификатов. Легитимную ротацию подтвердить нельзя; проверьте издателя вручную.",
                            "MEDIUM", packageName
                        )
                    SigningLineageResult.EXACT_MATCH, SigningLineageResult.MULTI_SIGNER_MATCH -> Unit
                }
            }
            if (old.permissions.isNotBlank() && new.permissions.isNotBlank() && old.permissions != new.permissions) {
                changes += SentinelChange(
                    "permissions_changed", "Изменились разрешения приложения: ${new.label}",
                    "Состав запрошенных или предоставленных разрешений отличается от предыдущего снимка. Проверьте, какие возможности добавлены или сняты.",
                    "MEDIUM", packageName
                )
            } else if (old.version != new.version || old.updatedAt != new.updatedAt) {
                changes += SentinelChange(
                    "updated", "Обновлено приложение: ${new.label}",
                    "Версия или время обновления изменились с предыдущего снимка.", "INFO"
                )
            }
        }
        (current.findingIds - previous.findingIds).sorted().forEach { id ->
            changes += SentinelChange(
                "new_finding", "Появился новый индикатор аудита",
                "Новый сигнал: $id. Откройте основной отчёт, чтобы посмотреть детали.", "MEDIUM"
            )
        }
        return changes
    }

    private fun compareSignerLineage(previous: SentinelApp, current: SentinelApp): SigningLineageResult {
        val previousSigners = parseSignerList(previous.signer).toSet()
        val currentSigners = parseSignerList(current.signer).toSet()
        if (previousSigners.isEmpty() || currentSigners.isEmpty()) return SigningLineageResult.UNKNOWN
        return SigningLineageComparator.compare(
            SigningLineage(
                currentSigners = previousSigners,
                history = parseSignerList(previous.signerHistory).ifEmpty { previousSigners.toList() },
                multipleSigners = previous.multipleSigners
            ),
            SigningLineage(
                currentSigners = currentSigners,
                history = parseSignerList(current.signerHistory).ifEmpty { currentSigners.toList() },
                multipleSigners = current.multipleSigners
            )
        )
    }

    private fun parseSignerList(value: String): List<String> =
        value.split(',').map { it.trim() }.filter { it.isNotBlank() }

    private fun snapshot(json: JSONObject): SentinelSnapshot {
        val appsArray = json.optJSONArray("apps")
        return SentinelSnapshot(
            json.optInt("schema", 1), appsArray != null, readApps(appsArray),
            readFindingIds(json.optJSONArray("findings"))
        )
    }

    private fun readApps(array: JSONArray?): List<SentinelApp> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val app = array.optJSONObject(index) ?: continue
                val packageName = app.optString("packageName")
                if (packageName.isBlank()) continue
                val multipleSigners = if (app.has("multipleSigners")) {
                    runCatching { app.getBoolean("multipleSigners") }.getOrNull()
                } else null
                add(SentinelApp(
                    packageName = packageName,
                    label = app.optString("label", packageName),
                    version = app.optString("version", ""),
                    signer = app.optString("signerSha256", ""),
                    permissions = app.optString("permissionsSha256", ""),
                    installer = app.optString("installerPackage", ""),
                    updatedAt = app.optLong("lastUpdateTime"),
                    signerHistory = app.optString("signerHistorySha256", ""),
                    multipleSigners = multipleSigners
                ))
            }
        }
    }

    private fun readFindingIds(array: JSONArray?): Set<String> {
        if (array == null) return emptySet()
        return buildSet {
            for (index in 0 until array.length()) {
                val id = array.optJSONObject(index)?.optString("id").orEmpty()
                if (id.isNotBlank()) add(id)
            }
        }
    }
}
