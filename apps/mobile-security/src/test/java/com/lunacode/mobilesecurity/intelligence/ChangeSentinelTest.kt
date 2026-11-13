package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangeSentinelTest {
    private fun snapshot(vararg apps: SentinelApp) =
        SentinelSnapshot(2, true, apps.toList(), emptySet())

    private fun app(
        packageName: String, signer: String = "CERT-A",
        permissions: String = "PERM-A", version: String = "1:1"
    ) = SentinelApp(
        packageName, packageName.substringAfterLast('.'), version, signer,
        permissions, "installer", 1L
    )

    @Test fun detectsNewInstallWhenPreviousInventoryWasEmpty() {
        val changes = ChangeSentinel.compareSnapshots(snapshot(), snapshot(app("com.example.new")))
        assertTrue(changes.any { it.kind == "installed" })
    }

    @Test fun detectsRemovalWhenCurrentInventoryIsEmpty() {
        val changes = ChangeSentinel.compareSnapshots(snapshot(app("com.example.old")), snapshot())
        assertTrue(changes.any { it.kind == "removed" })
    }

    @Test fun signerChangeIsReportedAsHighPriorityIndicator() {
        val changes = ChangeSentinel.compareSnapshots(
            snapshot(app("com.example.app", signer = "CERT-A")),
            snapshot(app("com.example.app", signer = "CERT-B"))
        )
        assertEquals("HIGH", changes.single { it.kind == "signer_changed" }.level)
    }

    @Test fun missingInventoryDoesNotCreateFalseInstallOrRemovalEvents() {
        val previous = SentinelSnapshot(2, false, emptyList(), emptySet())
        assertTrue(ChangeSentinel.compareSnapshots(previous, snapshot(app("com.example.app"))).isEmpty())
    }

    @Test fun reportsPermissionAndVersionChanges() {
        val old = app("com.example.app")
        assertTrue(ChangeSentinel.compareSnapshots(
            snapshot(old), snapshot(app("com.example.app", permissions = "PERM-B"))
        ).any { it.kind == "permissions_changed" })
        assertTrue(ChangeSentinel.compareSnapshots(
            snapshot(old), snapshot(app("com.example.app", version = "2:2"))
        ).any { it.kind == "updated" })
    }
}
