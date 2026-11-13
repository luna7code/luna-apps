package com.lunacode.mobilesecurity.intelligence

import org.json.JSONArray
import org.json.JSONObject

data class SentinelChange(val kind: String, val title: String, val detail: String, val level: String)
data class SentinelReport(val isFirstBaseline: Boolean, val changes: List<SentinelChange>)
data class SentinelApp(
    val packageName: String, val label: String, val version: String, val signer: String,
    val permissions: String, val installer: String, val updatedAt: Long
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
                    item.optString("level", "INFO")
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
                "MEDIUM"
            )
        }
        (before.keys - after.keys).sorted().forEach { packageName ->
            val app = before.getValue(packageName)
            changes += SentinelChange(
                "removed", "Приложение удалено: ${app.label}",
                "Пакет $packageName больше не обнаружен. Если удаление было ожидаемым, дополнительных действий не требуется.",
                "INFO"
            )
        }
        (before.keys intersect after.keys).sorted().forEach { packageName ->
            val old = before.getValue(packageName); val new = after.getValue(packageName)
            when {
                old.signer.isNotBlank() && new.signer.isNotBlank() && old.signer != new.signer ->
                    changes += SentinelChange(
                        "signer_changed", "Изменился сертификат приложения: ${new.label}",
                        "Отпечаток сертификата подписи отличается от предыдущего снимка. Это может быть легитимная ротация ключа, но требует проверки.",
                        "HIGH"
                    )
                old.permissions.isNotBlank() && new.permissions.isNotBlank() && old.permissions != new.permissions ->
                    changes += SentinelChange(
                        "permissions_changed", "Изменились разрешения приложения: ${new.label}",
                        "Состав запрошенных или предоставленных разрешений отличается от предыдущего снимка. Проверьте, какие возможности добавлены или сняты.",
                        "MEDIUM"
                    )
                old.version != new.version || old.updatedAt != new.updatedAt ->
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
                add(SentinelApp(
                    packageName, app.optString("label", packageName),
                    app.optString("version", ""), app.optString("signerSha256", ""),
                    app.optString("permissionsSha256", ""), app.optString("installerPackage", ""),
                    app.optLong("lastUpdateTime")
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
