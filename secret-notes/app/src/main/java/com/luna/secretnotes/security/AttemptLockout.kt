package com.luna.secretnotes.security

import android.content.Context
import java.nio.ByteBuffer
import java.security.MessageDigest

data class LockoutState(
    val failedAttempts: Int = 0,
    val blockedUntilEpochMs: Long = 0L
)

object LockoutPolicy {
    const val MAX_ATTEMPTS_BEFORE_DELAY = 5
    const val BASE_DELAY_MS = 30_000L
    const val MAX_DELAY_MS = 15 * 60_000L

    fun remainingDelayMs(state: LockoutState, nowEpochMs: Long): Long =
        (state.blockedUntilEpochMs - nowEpochMs).coerceAtLeast(0L)

    fun afterFailure(state: LockoutState, nowEpochMs: Long): LockoutState {
        val failures = (state.failedAttempts + 1).coerceAtMost(30)
        if (failures < MAX_ATTEMPTS_BEFORE_DELAY) {
            return LockoutState(failures, state.blockedUntilEpochMs)
        }
        val exponent = (failures - MAX_ATTEMPTS_BEFORE_DELAY).coerceAtMost(10)
        val delay = (BASE_DELAY_MS * (1L shl exponent)).coerceAtMost(MAX_DELAY_MS)
        return LockoutState(failures, nowEpochMs + delay)
    }

    fun afterSuccess(): LockoutState = LockoutState()
}

interface LockoutStateStore {
    fun read(): LockoutState
    fun write(state: LockoutState)
}

/**
 * Persists retry state with an Android Keystore HMAC. Missing or modified state
 * while the integrity key still exists fails closed instead of resetting attempts.
 */
class SharedPreferencesLockoutStateStore(
    context: Context,
    private val integrityMac: AndroidKeystoreMac = AndroidKeystoreMac(LOCKOUT_KEY_ALIAS)
) : LockoutStateStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun read(): LockoutState {
        val hasMac = preferences.contains(KEY_MAC)
        if (!hasMac) {
            if (integrityMac.hasKey()) {
                throw SecurityException("Lockout integrity record is missing")
            }
            return LockoutState()
        }
        val state = LockoutState(
            failedAttempts = preferences.getInt(KEY_FAILURES, 0),
            blockedUntilEpochMs = preferences.getLong(KEY_BLOCKED_UNTIL, 0L)
        )
        val expected = integrityMac.authenticate(encode(state))
        val actual = android.util.Base64.decode(
            preferences.getString(KEY_MAC, null) ?: error("Missing lockout MAC"),
            android.util.Base64.NO_WRAP
        )
        val valid = MessageDigest.isEqual(expected, actual)
        expected.fill(0)
        actual.fill(0)
        if (!valid) throw SecurityException("Lockout integrity check failed")
        return state
    }

    override fun write(state: LockoutState) {
        val mac = integrityMac.authenticate(encode(state))
        val committed = preferences.edit()
            .putInt(KEY_FAILURES, state.failedAttempts)
            .putLong(KEY_BLOCKED_UNTIL, state.blockedUntilEpochMs)
            .putString(KEY_MAC, android.util.Base64.encodeToString(mac, android.util.Base64.NO_WRAP))
            .commit()
        mac.fill(0)
        check(committed) { "Unable to persist lockout state" }
    }

    private fun encode(state: LockoutState): ByteArray =
        ByteBuffer.allocate(12)
            .putInt(state.failedAttempts)
            .putLong(state.blockedUntilEpochMs)
            .array()

    private companion object {
        const val PREFS_NAME = "secret_notes_lockout"
        const val KEY_FAILURES = "failed_attempts"
        const val KEY_BLOCKED_UNTIL = "blocked_until"
        const val KEY_MAC = "state_mac"
        const val LOCKOUT_KEY_ALIAS = "secret_notes_lockout_integrity_v1"
    }
}

class AttemptLockout(
    private val store: LockoutStateStore,
    private val now: () -> Long = System::currentTimeMillis
) {
    fun ensureAllowed() {
        val remaining = LockoutPolicy.remainingDelayMs(store.read(), now())
        if (remaining > 0L) throw TooManyAttemptsException(remaining)
    }

    fun recordFailure() {
        store.write(LockoutPolicy.afterFailure(store.read(), now()))
    }

    fun recordSuccess() {
        store.write(LockoutPolicy.afterSuccess())
    }
}

class TooManyAttemptsException(val retryAfterMs: Long) :
    IllegalStateException("Too many incorrect credentials. Retry after $retryAfterMs ms.")

class InvalidCredentialException :
    IllegalArgumentException("The PIN or password is incorrect.")
