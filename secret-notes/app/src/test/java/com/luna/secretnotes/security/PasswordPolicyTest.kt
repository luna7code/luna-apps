package com.luna.secretnotes.security

import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordPolicyTest {
    @Test
    fun acceptsEightDigitPin() {
        assertEquals(CredentialType.NUMERIC_PIN, PasswordPolicy.validate("12345678".toCharArray()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsShortPin() {
        PasswordPolicy.validate("1234567".toCharArray())
    }

    @Test
    fun acceptsTenCharacterAlphaNumericPassword() {
        assertEquals(
            CredentialType.ALPHANUMERIC_PASSWORD,
            PasswordPolicy.validate("notes202611".toCharArray())
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPunctuationInAlphaNumericPassword() {
        PasswordPolicy.validate("private-note1".toCharArray())
    }
}
