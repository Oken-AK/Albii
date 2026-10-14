package com.oken.albii.info

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InfoFormatTest {

    @Test
    fun shutter_under_one_second_is_fraction() {
        assertEquals("1/125", formatShutter(1.0 / 125.0))
    }

    @Test
    fun shutter_one_second_or_more_is_seconds() {
        assertEquals("2.0s", formatShutter(2.0))
    }

    @Test
    fun aperture_from_apex_uses_two_to_the_av_over_two() {
        assertEquals("f/4.0", formatAperture(apertureFromApex(4.0)))
        assertEquals("f/1.4", formatAperture(apertureFromApex(1.0)))
    }

    @Test
    fun exposure_bias_shows_sign() {
        assertEquals("+0.3 EV", formatExposureBias(0.3))
        assertEquals("-0.7 EV", formatExposureBias(-0.7))
        assertEquals("+0.0 EV", formatExposureBias(0.0))
    }

    @Test
    fun flash_uses_bit_zero() {
        assertEquals("発光", formatFlash(1))
        assertEquals("非発光", formatFlash(0))
        assertEquals("発光", formatFlash(0x19))
    }

    @Test
    fun unknown_enum_values_return_null() {
        assertNull(formatExposureProgram(99))
        assertNull(formatMetering(255))
        assertNull(formatWhiteBalance(7))
        assertNull(formatOrientation(0))
        assertNull(formatColorSpace(2))
    }

    @Test
    fun captured_at_with_offset() {
        assertEquals(
            "2024/05/01 12:34:56（UTC+09:00）",
            formatCapturedAt("2024:05:01 12:34:56", "+09:00"),
        )
    }

    @Test
    fun captured_at_without_offset_shows_local_time_only() {
        assertEquals("2024/05/01 12:34:56", formatCapturedAt("2024:05:01 12:34:56", null))
    }

    @Test
    fun captured_at_invalid_returns_null() {
        assertNull(formatCapturedAt(null, "+09:00"))
        assertNull(formatCapturedAt("not a date", null))
    }

    @Test
    fun file_size_switches_units() {
        assertEquals("512 KB", formatFileSize(512L * 1024))
        assertEquals("2.0 MB", formatFileSize(2L * 1024 * 1024))
    }

    @Test
    fun megapixels_formats_dimensions() {
        assertEquals("4000 × 3000（12.0 MP）", formatMegaPixels(4000, 3000))
    }

    @Test
    fun defaults_exclude_device_gps_and_optional_image_fields() {
        val defaults = InfoItem.defaults
        assert(InfoItem.ISO in defaults)
        assert(InfoItem.MAKE !in defaults)
        assert(InfoItem.GPS !in defaults)
        assert(InfoItem.COLOR_SPACE !in defaults)
    }
}
