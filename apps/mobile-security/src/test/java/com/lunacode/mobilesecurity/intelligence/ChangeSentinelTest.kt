package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangeSentinelTest {
    private fun snapshot(vararg apps: SentinelApp) =
        SentinelSnapshot(2, true, apps.toList(), emptySet())

    private fun app(
        packageName: String,
        signer: String = "CERT-A",
        permissions: String = "PERM-A",
        version: String = "1:1",
        history: String = signer,
        multipleSigners: Boolean? = false
    ) = SentinelApp(
        packageName = packageName,
        label = packageName.substringAfterLast('.'),
        version = version,
        signer = signer,
        signerHistory = history,
        multipleSigners = multipleSigners,
        permissions = permissions,
        installer = "installer",
        updatedAt = 1L
    )

    @Test fun detectsNewInstallWhenPreviousInventoryWasEmpty() {
        val changes = ChangeSentinel.compareSnapshots(snapshot(), snapshot(app("com.example.new")))
        assertTrue(changes.any { it.kind == "installed" })
    }

    @Test fun detectsRemovalWhenCurrentInventoryIsEmpty() {
        val changes = ChangeSentinel.compareSnapshots(snapshot(app("com.example.old")), snapshot())
        assertTrue(changes.any { it.kind == "removed" })
    }

    @Test fun verifiedForwardRotationIsInformationalNotHighSeverity() {
        val changes = ChangeSentinel.compareSnapshots(
            snapshot(app("com.example.app", signer = "CERT-A", history = "CERT-A")),
            snapshot(app("com.example.app", signer = "CERT-B", history = "CERT-A,CERT-B"))
        )
        val rotation = changes.single { it.kind == "signer_rotated" }
        assertEquals("INFO", rotation.level)
    }

    @Test fun historicalSignerIsReportedAsHighPriority() {
        val changes = ChangeSentinel.compareSnapshots(
            snapshot(app("com.example.app", signer = "CERT-B", history = "CERT-A,CERT-B")),
            snapshot(app("com.example.app", signer = "CERT-A", history = "CERT-A"))
        )
        val change = changes.single { it.kind == "signer_historical" }
        assertEquals("HIGH", change.level)
    }

    @Test fun unrelatedSignerIsReportedAsHighPriority() {
        val changes = ChangeSentinel.compareSnapshots(
            snapshot(app("com.example.app", signer = "CERT-A", history = "CERT-A")),
            snapshot(app("com.example.app", signer = "CERT-C", history = "CERT-C"))
        )
        assertEquals("HIGH", changes.single { it.kind == "signer_changed" }.level)
    }

    @Test fun missingLegacyLineageIsReportedAsUnverifiedNotTrustedRotation() {
        val changes = ChangeSentinel.compareSnapshots(
            snapshot(app("com.example.app", signer = "CERT-A", history = "", multipleSigners = null)),
            snapshot(app("com.example.app", signer = "CERT-B", history = "CERT-A,CERT-B"))
        )
        val change = changes.single { it.kind == "signer_change_unverified" }
        assertEquals("MEDIUM", change.level)
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
