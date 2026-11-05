package com.oken.albii.ui.viewer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScrubSeekThrottleTest {

    @Test
    fun firstRequestIsSentImmediately() {
        val throttle = ScrubSeekThrottle(minIntervalMs = 50)
        assertEquals(1_000L, throttle.submit(1_000, nowMs = 0))
    }

    @Test
    fun requestsWithinIntervalAreHeldAndOnlyLatestSurvives() {
        val throttle = ScrubSeekThrottle(minIntervalMs = 50)
        throttle.submit(1_000, nowMs = 0)

        assertNull(throttle.submit(1_100, nowMs = 10))
        assertNull(throttle.submit(900, nowMs = 20))
        assertNull(throttle.submit(500, nowMs = 30))

        assertEquals(500L, throttle.takeIfDue(nowMs = 50))
        assertNull(throttle.takeIfDue(nowMs = 60))
    }

    @Test
    fun backwardRequestsAreTreatedLikeForwardOnes() {
        val throttle = ScrubSeekThrottle(minIntervalMs = 50)
        assertEquals(3_000L, throttle.submit(3_000, nowMs = 0))
        assertNull(throttle.submit(2_000, nowMs = 10))
        assertEquals(2_000L, throttle.takeIfDue(nowMs = 50))
        assertEquals(100L, throttle.submit(100, nowMs = 100))
    }

    @Test
    fun delayReportsRemainingWaitOrNullWhenNothingPending() {
        val throttle = ScrubSeekThrottle(minIntervalMs = 50)
        assertNull(throttle.delayUntilDueMs(nowMs = 0))

        throttle.submit(1_000, nowMs = 0)
        assertNull(throttle.delayUntilDueMs(nowMs = 5))

        throttle.submit(2_000, nowMs = 20)
        assertEquals(30L, throttle.delayUntilDueMs(nowMs = 20))
        assertEquals(0L, throttle.delayUntilDueMs(nowMs = 80))
    }

    @Test
    fun submitWithoutHistorySendsImmediatelyAndLeavesNothingPending() {
        val throttle = ScrubSeekThrottle(minIntervalMs = 50)
        assertEquals(1_000L, throttle.submit(1_000, nowMs = 5))
        assertNull(throttle.takeIfDue(nowMs = 6))
    }

    @Test
    fun resetForgetsPendingAndHistory() {
        val throttle = ScrubSeekThrottle(minIntervalMs = 50)
        throttle.submit(1_000, nowMs = 0)
        throttle.submit(2_000, nowMs = 10)
        throttle.reset()

        assertNull(throttle.takeIfDue(nowMs = 100))
        assertEquals(3_000L, throttle.submit(3_000, nowMs = 101))
    }

    @Test
    fun scrubTargetMapsFractionToPositionInsideTheVideo() {
        assertEquals(0L, scrubTargetMs(0f, 4_000))
        assertEquals(2_000L, scrubTargetMs(0.5f, 4_000))
        assertEquals(3_999L, scrubTargetMs(1f, 4_000))
    }

    @Test
    fun scrubTargetClampsOutOfRangeAndUnknownDuration() {
        assertEquals(0L, scrubTargetMs(-0.3f, 4_000))
        assertEquals(3_999L, scrubTargetMs(1.7f, 4_000))
        assertEquals(0L, scrubTargetMs(0.5f, 0))
    }
}
