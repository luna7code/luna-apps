package com.luna.secretnotes.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LockoutPolicyTest {
    @Test
    fun appliesProgressiveDelayAfterFiveFailures() {
        var state = LockoutState()
        repeat(4) { state = LockoutPolicy.afterFailure(state, 1_000L) }
        assertEquals(0L, LockoutPolicy.remainingDelayMs(state, 1_000L))

        state = LockoutPolicy.afterFailure(state, 1_000L)
        assertEquals(30_000L, LockoutPolicy.remainingDelayMs(state, 1_000L))

        state = LockoutPolicy.afterFailure(state, 31_000L)
        assertEquals(60_000L, LockoutPolicy.remainingDelayMs(state, 31_000L))
    }

    @Test
    fun capsDelayAtFifteenMinutes() {
        var state = LockoutState()
        repeat(30) { state = LockoutPolicy.afterFailure(state, 10_000L) }
        assertTrue(LockoutPolicy.remainingDelayMs(state, 10_000L) <= LockoutPolicy.MAX_DELAY_MS)
        assertEquals(LockoutPolicy.MAX_DELAY_MS, LockoutPolicy.remainingDelayMs(state, 10_000L))
    }

    @Test
    fun successResetsFailureCounter() {
        assertEquals(LockoutState(), LockoutPolicy.afterSuccess())
    }
}
