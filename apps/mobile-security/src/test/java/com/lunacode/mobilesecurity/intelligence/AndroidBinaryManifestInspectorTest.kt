package com.lunacode.mobilesecurity.intelligence

import org.junit.Assert.*
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
}
