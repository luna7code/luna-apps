package com.lunacode.mobilesecurity.intelligence

import org.json.JSONArray
import org.json.JSONObject

data class SentinelChange(
    val kind: String,
    val title: String,
    val detail: String,
    val level: String
)

data class SentinelReport(
    val isFirstBaseline: Boolean,
    val changes: List<SentinelChange>
)

object ChangeSentinel {
    fun enrich(previousEncryptedPlaintext: String?, currentPayload: String): String {
        val current = JSONObject(currentPayload)
        val previous = previousEncryptedPlaintext?.let {
            runCatching { JSONObject(it) }.getOrNull()
        }
        val firstBaseline = previous == null || previous.optInt("schema") < 2
        val changes = if (firstBaseline) emptyList() else compare(previous!!, current)
        val changeArray = JSONArray()
        changes.forEach { change ->
            changeArray.put(JSONObject().apply {
                put("kind", change.kind)
                put("title", change.title)
                put("detail", change.detail)
                put("level", change.level)
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
                add(
                    SentinelChange(
                        kind = item.optString("kind", "unknown"),
                        title = item.optString("title", "Изменение"),
                        detail = item.optString("detail", ""),
                        level = item.optString("level", "INFO")
                    )
                )
            }
        }
        return SentinelReport(json.optBoolean("baselineCreated"), changes)
    }

    private data class AppFingerprint(
        val label: String,
        val version: String,
        val signer: String,
        val permissions: String,
        val installer: String,
        val updatedAt: Long
    )

    private fun compare(previous: JSONObject, current: JSONObject): List<SentinelChange> {
        // Missing inventory is different from a valid empty inventory.
        // Skip comparison only when one report has no inventory field at all.
        val beforeArray = previous.optJSONArray("apps") ?: return emptyList()
        val afterArray = current.optJSONArray("apps") ?: return emptyList()
        val before = readApps(beforeArray)
        val after = readApps(afterArray)

        val changes = mutableListOf<SentinelChange>()
        (after.keys - before.keys).sorted().forEach { packageName ->
            val app = after.getValue(packageName)
            changes += SentinelChange(
                "installed",
                "Установлено новое приложение: ${app.label}",
                "Пакет $packageName появился после предыдущей проверки. Установочный источник, сообщённый Android: ${app.installer.ifBlank { "неизвестен" }}. Проверьте издателя и источник установки.",
                "MEDIUM"
            )
        }
        (before.keys - after.keys).sorted().forEach { packageName ->
            val app = before.getValue(packageName)
            changes += SentinelChange(
                "removed",
                "Приложение удалено: ${app.label}",
                "Пакет $packageName больше не обнаружен. Если удаление было ожидаемым, дополнительных действий не требуется.",
                "INFO"
            )
        }
        (before.keys intersect after.keys).sorted().forEach { packageName ->
            val old = before.getValue(packageName)
            val new = after.getValue(packageName)
            if (old.signer.isNotBlank() && new.signer.isNotBlank() && old.signer != new.signer) {
                changes += SentinelChange(
                    "signer_changed",
                    "Изменился сертификат приложения: ${new.label}",
                    "Отпечаток сертификата подписи отличается от предыдущего снимка. Это может быть легитимная смена ключа, но требует проверки.",
                    "HIGH"
                )
            } else if (old.permissions.isNotBlank() && new.permissions.isNotBlank() && old.permissions != new.permissions) {
                changes += SentinelChange(
                    "permissions_changed",
                    "Изменились разрешения приложения: ${new.label}",
                    "Состав запрошенных или предоставленных разрешений отличается от предыдущего снимка. Проверьте, какие возможности добавлены или сняты.",
                    "MEDIUM"
                )
            } else if (old.version != new.version || old.updatedAt != new.updatedAt) {
                changes += SentinelChange(
                    "updated",
                    "Обновлено приложение: ${new.label}",
                    "Версия или время обновления изменились с предыдущего снимка.",
                    "INFO"
                )
            }
        }

        val oldFindings = readFindingIds(previous.optJSONArray("findings"))
        val newFindings = readFindingIds(current.optJSONArray("findings"))
        (newFindings - oldFindings).sorted().forEach { id ->
            changes += SentinelChange(
                "new_finding",
                "Появился новый индикатор аудита",
                "Новый сигнал: $id. Откройте основной отчёт, чтобы посмотреть детали.",
                "MEDIUM"
            )
        }
        return changes
    }

    private fun readApps(array: JSONArray?): Map<String, AppFingerprint> {
        if (array == null) return emptyMap()
        val result = linkedMapOf<String, AppFingerprint>()
        for (index in 0 until array.length()) {
            val app = array.optJSONObject(index) ?: continue
            val packageName = app.optString("packageName")
            if (packageName.isBlank()) continue
            result[packageName] = AppFingerprint(
                label = app.optString("label", packageName),
                version = app.optString("version", ""),
                signer = app.optString("signerSha256", ""),
                permissions = app.optString("permissionsSha256", ""),
                installer = app.optString("installerPackage", ""),
                updatedAt = app.optLong("lastUpdateTime")
            )
        }
        return result
    }

    private fun readFindingIds(array: JSONArray?): Set<String> {
        if (array == null) return emptySet()
        return buildSet {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optString("id")
                if (id.isNotBlank()) add(id)
            }
        }
    }
}
