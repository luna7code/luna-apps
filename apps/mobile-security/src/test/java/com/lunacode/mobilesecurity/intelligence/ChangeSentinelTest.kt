package com.lunacode.mobilesecurity.intelligence

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangeSentinelTest {
    private fun report(vararg apps: JSONObject): String =
        JSONObject().apply {
            put("schema", 2)
            put("apps", JSONArray().apply { apps.forEach(::put) })
            put("findings", JSONArray())
        }.toString()

    private fun app(
        packageName: String,
        signer: String = "CERT-A",
        permissions: String = "PERM-A",
        version: String = "1:1"
    ) = JSONObject().apply {
        put("packageName", packageName)
        put("label", packageName.substringAfterLast('.'))
        put("signerSha256", signer)
        put("permissionsSha256", permissions)
        put("version", version)
        put("installerPackage", "installer")
        put("lastUpdateTime", 1L)
    }

    @Test
    fun detectsNewInstallWhenPreviousInventoryWasEmpty() {
        val previous = report()
        val current = report(app("com.example.new"))
        val result = ChangeSentinel.enrich(previous, current)
        val parsed = ChangeSentinel.read(JSONObject(result))
        assertFalse(parsed.isFirstBaseline)
        assertTrue(parsed.changes.any { it.kind == "installed" })
    }

    @Test
    fun detectsRemovalWhenCurrentInventoryIsEmpty() {
        val previous = report(app("com.example.old"))
        val current = report()
        val parsed = ChangeSentinel.read(JSONObject(ChangeSentinel.enrich(previous, current)))
        assertTrue(parsed.changes.any { it.kind == "removed" })
    }

    @Test
    fun signerChangeIsReportedAsHighPriorityIndicator() {
        val previous = report(app("com.example.app", signer = "CERT-A"))
        val current = report(app("com.example.app", signer = "CERT-B"))
        val parsed = ChangeSentinel.read(JSONObject(ChangeSentinel.enrich(previous, current)))
        val change = parsed.changes.single { it.kind == "signer_changed" }
        assertEquals("HIGH", change.level)
    }

    @Test
    fun missingInventoryDoesNotCreateFalseInstallOrRemovalEvents() {
        val previous = JSONObject().put("schema", 2).toString()
        val current = report(app("com.example.app"))
        val parsed = ChangeSentinel.read(JSONObject(ChangeSentinel.enrich(previous, current)))
        assertTrue(parsed.changes.isEmpty())
    }
}
