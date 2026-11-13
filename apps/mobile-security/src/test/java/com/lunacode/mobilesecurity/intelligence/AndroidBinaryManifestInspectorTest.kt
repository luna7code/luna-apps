package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.*
import kotlin.random.Random
import org.junit.Test

class AndroidBinaryManifestInspectorTest {
    @Test
    fun malformedAndPlainXmlInputReturnWarningsInsteadOfCrashing() {
        val result = AndroidBinaryManifestInspector.inspect("<manifest/>".toByteArray())
        assertNull(result.packageName)
        assertTrue(result.parseWarnings.isNotEmpty())
    }

    @Test
    fun truncatedBinaryXmlIsRejectedSafely() {
        val bytes = byteArrayOf(3, 0, 8, 0, 40, 0, 0, 0, 1)
        val result = AndroidBinaryManifestInspector.inspect(bytes)
        assertTrue(result.parseWarnings.isNotEmpty())
    }
    @Test
    fun malformedBinaryXmlPropertyCorpusReturnsBoundedWarnings() {
        val random = Random(0x584d4c)
        repeat(512) {
            val bytes = ByteArray(random.nextInt(0, 8192))
            random.nextBytes(bytes)
            if (bytes.size >= 8) {
                bytes[0] = 3
                bytes[1] = 0
            }
            val result = AndroidBinaryManifestInspector.inspect(bytes)
            assertNotNull(result)
            assertTrue(result.parseWarnings.size <= 16)
        }
    }

}
