package com.lunacode.mobilesecurity.intelligence

import com.lunacode.mobilesecurity.scanner.AuditFinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityPostureTest {
    @Test
    fun informationOnlyFindingsDoNotReduceExposureIndex() {
        val report = SecurityPosture.evaluate(
            listOf(AuditFinding("info", "Info", "Details", "INFO", "source"))
        )
        assertEquals(100, report.score)
        assertTrue(report.recommendations.isEmpty())
    }

    @Test
    fun duplicateFindingIdsAreCountedOnce() {
        val duplicate = AuditFinding("screen-lock-missing", "No lock", "Set a lock", "HIGH", "Device")
        val report = SecurityPosture.evaluate(listOf(duplicate, duplicate))
        assertEquals(1, report.highCount)
        assertEquals(84, report.score)
    }

    @Test
    fun exposureIndexIsBoundedAndExplainable() {
        val findings = (1..12).map {
            AuditFinding("high-$it", "Risk $it", "Details", "HIGH", "source")
        }
        val report = SecurityPosture.evaluate(findings)
        assertEquals(25, report.score)
        assertEquals("Высокая экспозиция", report.grade)
        assertTrue(report.explanation.contains("не вероятность заражения"))
    }

    @Test
    fun mapsAppOpsFindingsToSpecificSettingsRemediation() {
        val report = SecurityPosture.evaluate(
            listOf(AuditFinding(
                "app-op-overlay-com.example.overlay",
                "Overlay allowed",
                "Operation allowed",
                "MEDIUM",
                "com.example.overlay"
            ))
        )
        assertEquals("overlay", report.recommendations.single().settingsAction)
        assertEquals("com.example.overlay", report.recommendations.single().packageName)
    }

    @Test
    fun mapsCameraMicrophoneAndBackgroundLocationToPackageSettings() {
        val findings = listOf(
            AuditFinding("app-op-camera-com.example.recorder", "Camera", "Allowed", "MEDIUM", "com.example.recorder"),
            AuditFinding("app-op-record-audio-com.example.recorder", "Microphone", "Allowed", "MEDIUM", "com.example.recorder"),
            AuditFinding("app-op-background-location-com.example.tracker", "Background location", "Allowed", "MEDIUM", "com.example.tracker")
        )
        val recommendations = SecurityPosture.evaluate(findings).recommendations
        assertTrue(recommendations.any { it.title == "Проверить доступ к камере" && it.packageName == "com.example.recorder" })
        assertTrue(recommendations.any { it.title == "Проверить доступ к микрофону" && it.packageName == "com.example.recorder" })
        assertTrue(recommendations.any { it.title == "Проверить геолокацию в фоне" && it.packageName == "com.example.tracker" })
    }

    @Test
    fun prioritizesActionableRemediation() {
        val report = SecurityPosture.evaluate(
            listOf(
                AuditFinding(
                    "screen-lock-missing",
                    "No lock",
                    "Set a lock",
                    "HIGH",
                    "Device"
                ),
                AuditFinding(
                    "remote-com.example.remote",
                    "Remote tool",
                    "Review app",
                    "MEDIUM",
                    "com.example.remote"
                )
            )
        )
        assertFalse(report.recommendations.isEmpty())
        assertEquals("HIGH", report.recommendations.first().priority)
    }
}
