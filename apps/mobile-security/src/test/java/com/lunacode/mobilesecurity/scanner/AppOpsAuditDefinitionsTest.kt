package com.lunacode.mobilesecurity.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppOpsAuditDefinitionsTest {
    @Test fun includesCoreSensitiveOperationsOnSupportedSdk() {
        val keys = AppOpsAuditDefinitions.forSdk(26).map { it.key }.toSet()
        assertTrue(keys.containsAll(setOf("overlay", "usage", "write-settings", "camera", "record-audio", "fine-location", "coarse-location", "install-packages")))
    }

    @Test fun gatesNewerOperationsByPlatformVersion() {
        val sdk29 = AppOpsAuditDefinitions.forSdk(29).map { it.key }.toSet()
        assertTrue("background-location" in sdk29)
        assertFalse("manage-storage" in sdk29)
        assertFalse("bluetooth-scan" in sdk29)
        assertFalse("post-notification" in sdk29)

        val sdk30 = AppOpsAuditDefinitions.forSdk(30).map { it.key }.toSet()
        assertTrue("manage-storage" in sdk30)
        assertFalse("bluetooth-scan" in sdk30)

        val sdk31 = AppOpsAuditDefinitions.forSdk(31).map { it.key }.toSet()
        assertTrue("bluetooth-scan" in sdk31)
        assertFalse("post-notification" in sdk31)

        val sdk33 = AppOpsAuditDefinitions.forSdk(33).map { it.key }.toSet()
        assertTrue(setOf("post-notification", "read-media-images", "read-media-video", "read-media-audio").all { it in sdk33 })
    }

    @Test fun everyDefinitionHasUniqueKeyAndNonBlankOperation() {
        for (sdk in listOf(26, 29, 30, 31, 33, 36)) {
            val definitions = AppOpsAuditDefinitions.forSdk(sdk)
            assertEquals(definitions.size, definitions.map { it.key }.toSet().size)
            assertTrue(definitions.all { it.key.isNotBlank() && it.operation.isNotBlank() && it.label.isNotBlank() })
        }
    }
}
