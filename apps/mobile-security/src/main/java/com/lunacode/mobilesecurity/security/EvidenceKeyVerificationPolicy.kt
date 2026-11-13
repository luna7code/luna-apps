package com.lunacode.mobilesecurity.security

/** Pure policy seam for fail-closed Keystore verification behavior. */
internal object EvidenceKeyVerificationPolicy {
    fun canVerify(keyPresent: Boolean, signaturePresent: Boolean): Boolean =
        keyPresent && signaturePresent
}
