package com.luna.secretnotes.security

enum class CredentialType {
    NUMERIC_PIN,
    ALPHANUMERIC_PASSWORD
}

/**
 * Default enrollment policy: 8+ decimal digits for a PIN, or 10+ letters/digits
 * for an alphanumeric password. The policy is explicit and can be versioned.
 */
object PasswordPolicy {
    const val MIN_PIN_LENGTH = 8
    const val MIN_PASSWORD_LENGTH = 10

    fun validate(secret: CharArray): CredentialType {
        require(secret.isNotEmpty()) { "Credential must not be empty" }
        val isNumericPin = secret.all { it in '0'..'9' }
        if (isNumericPin) {
            require(secret.size >= MIN_PIN_LENGTH) {
                "PIN must contain at least $MIN_PIN_LENGTH digits"
            }
            return CredentialType.NUMERIC_PIN
        }

        require(secret.size >= MIN_PASSWORD_LENGTH) {
            "Alphanumeric password must contain at least $MIN_PASSWORD_LENGTH characters"
        }
        require(secret.all { it.isLetterOrDigit() }) {
            "Password must contain only letters and digits"
        }
        return CredentialType.ALPHANUMERIC_PASSWORD
    }
}
