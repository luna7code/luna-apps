package com.luna.secretnotes

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectIdentityTest {
    @Test
    fun applicationIdIsDedicatedToSecretNotes() {
        assertEquals("com.luna.secretnotes", AppIdentity.APPLICATION_ID)
    }
}
