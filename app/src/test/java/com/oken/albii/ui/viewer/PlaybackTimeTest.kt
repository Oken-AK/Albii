package com.oken.albii.ui.viewer

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackTimeTest {

    @Test
    fun formatsUnderOneMinute() {
        assertEquals("0:00", formatPlaybackTime(0))
        assertEquals("0:03", formatPlaybackTime(3_668))
        assertEquals("0:59", formatPlaybackTime(59_999))
    }

    @Test
    fun formatsMinutes() {
        assertEquals("1:00", formatPlaybackTime(60_000))
        assertEquals("16:05", formatPlaybackTime(16 * 60_000L + 5_000))
    }

    @Test
    fun formatsHours() {
        assertEquals("1:02:03", formatPlaybackTime(3_600_000L + 2 * 60_000 + 3_000))
    }

    @Test
    fun negativeIsClampedToZero() {
        assertEquals("0:00", formatPlaybackTime(-500))
    }
}
