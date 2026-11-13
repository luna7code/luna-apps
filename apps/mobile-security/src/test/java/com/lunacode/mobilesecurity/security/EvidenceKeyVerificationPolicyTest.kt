package com.lunacode.mobilesecurity.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceKeyVerificationPolicyTest {
    @Test fun missingKeyMustFailClosedInsteadOfCreatingReplacementKey() {
        assertFalse(EvidenceKeyVerificationPolicy.canVerify(keyPresent = false, signaturePresent = true))
    }

    @Test fun missingSignatureMustFailClosed() {
        assertFalse(EvidenceKeyVerificationPolicy.canVerify(keyPresent = true, signaturePresent = false))
    }

    @Test fun verificationIsAllowedOnlyWhenOriginalKeyAndSignatureExist() {
        assertTrue(EvidenceKeyVerificationPolicy.canVerify(keyPresent = true, signaturePresent = true))
    }
}
