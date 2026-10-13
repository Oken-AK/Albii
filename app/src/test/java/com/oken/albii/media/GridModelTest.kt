package com.oken.albii.media

import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GridModelTest {

    private val zone = ZoneId.of("Asia/Tokyo")

    private fun millisOf(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Long =
        java.time.LocalDateTime.of(year, month, day, hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun item(id: Long, millis: Long, isVideo: Boolean = false) =
        MediaItem(id = id, isVideo = isVideo, dateTakenMillis = millis)

    @Test
    fun resolveDateMillis_prefersDateTaken() {
        assertEquals(1_000L, resolveDateMillis(1_000L, 5L))
    }

    @Test
    fun resolveDateMillis_fallsBackToDateAddedSeconds_whenMissingOrZero() {
        assertEquals(5_000L, resolveDateMillis(null, 5L))
        assertEquals(5_000L, resolveDateMillis(0L, 5L))
    }

    @Test
    fun orderItems_reversesForNewestAtBottom() {
        val newestFirst = listOf(item(1, 3), item(2, 2), item(3, 1))
        assertEquals(listOf(1L, 2L, 3L), orderItems(newestFirst, SortOrder.NEWEST_AT_TOP).map { it.id })
        assertEquals(listOf(3L, 2L, 1L), orderItems(newestFirst, SortOrder.NEWEST_AT_BOTTOM).map { it.id })
    }

    @Test
    fun groupKey_dayChangesAtLocalMidnight() {
        val before = groupKeyOf(millisOf(2024, 5, 1, 23, 59), GroupMode.DAY, zone)
        val after = groupKeyOf(millisOf(2024, 5, 2, 0, 0), GroupMode.DAY, zone)
        assertEquals(before + 1, after)
    }

    @Test
    fun groupKey_monthChangesAtMonthBoundaryAndAcrossYear() {
        val may31 = groupKeyOf(millisOf(2024, 5, 31, 23), GroupMode.MONTH, zone)
        val jun1 = groupKeyOf(millisOf(2024, 6, 1, 0), GroupMode.MONTH, zone)
        val dec = groupKeyOf(millisOf(2024, 12, 31, 12), GroupMode.MONTH, zone)
        val jan = groupKeyOf(millisOf(2025, 1, 1, 12), GroupMode.MONTH, zone)
        assertEquals(may31 + 1, jun1)
        assertEquals(dec + 1, jan)
    }

    @Test
    fun groupLabel_formatsDayAndMonth() {
        assertEquals(
            "2024年5月2日（木）",
            groupLabelOf(millisOf(2024, 5, 2, 10), GroupMode.DAY, zone),
        )
        assertEquals(
            "2024年5月",
            groupLabelOf(millisOf(2024, 5, 2, 10), GroupMode.MONTH, zone),
        )
    }

    @Test
    fun buildGridEntries_noneHasNoHeaders() {
        val items = listOf(item(1, millisOf(2024, 5, 1, 9)), item(2, millisOf(2024, 5, 1, 10)))
        val entries = buildGridEntries(items, GroupMode.NONE, zone)
        assertEquals(2, entries.size)
        assert(entries.all { it is GridEntry.Media })
    }

    @Test
    fun buildGridEntries_insertsHeaderOnlyWhenGroupChanges() {
        val items = listOf(
            item(1, millisOf(2024, 5, 2, 12)),
            item(2, millisOf(2024, 5, 2, 9)),
            item(3, millisOf(2024, 5, 1, 9), isVideo = true),
        )
        val entries = buildGridEntries(items, GroupMode.DAY, zone)
        assertEquals(5, entries.size)
        assertEquals(GridEntry.Header(groupKeyOf(items[0].dateTakenMillis, GroupMode.DAY, zone), "2024年5月2日（木）"), entries[0])
        assertEquals(GridEntry.Media(items[0]), entries[1])
        assertEquals(GridEntry.Media(items[1]), entries[2])
        assertEquals("2024年5月1日（水）", (entries[3] as GridEntry.Header).label)
        assertEquals(GridEntry.Media(items[2]), entries[4])
    }

    @Test
    fun buildGridEntries_monthGroupingMergesDays() {
        val items = listOf(
            item(1, millisOf(2024, 5, 2, 12)),
            item(2, millisOf(2024, 5, 1, 9)),
        )
        val entries = buildGridEntries(items, GroupMode.MONTH, zone)
        assertEquals(3, entries.size)
        assertEquals("2024年5月", (entries[0] as GridEntry.Header).label)
    }

    @Test
    fun entryIndexForFraction_mapsEndpointsAndClamps() {
        assertEquals(0, entryIndexForFraction(0f, 11))
        assertEquals(10, entryIndexForFraction(1f, 11))
        assertEquals(5, entryIndexForFraction(0.5f, 11))
        assertEquals(10, entryIndexForFraction(2f, 11))
        assertEquals(0, entryIndexForFraction(-1f, 11))
        assertEquals(0, entryIndexForFraction(0.7f, 1))
    }

    @Test
    fun fractionForEntryIndex_roundTripsWithIndex() {
        assertEquals(0f, fractionForEntryIndex(0, 11), 0.0001f)
        assertEquals(1f, fractionForEntryIndex(10, 11), 0.0001f)
        assertEquals(0.5f, fractionForEntryIndex(5, 11), 0.0001f)
        assertEquals(0f, fractionForEntryIndex(3, 1), 0.0001f)
    }

    @Test
    fun labelAtEntry_usesNearestPrecedingHeader() {
        val items = listOf(
            item(1, millisOf(2024, 5, 2, 12)),
            item(2, millisOf(2024, 5, 2, 9)),
            item(3, millisOf(2024, 5, 1, 9)),
        )
        val entries = buildGridEntries(items, GroupMode.DAY, zone)
        assertEquals("2024年5月2日（木）", labelAtEntry(entries, 2, zone))
        assertEquals("2024年5月1日（水）", labelAtEntry(entries, 4, zone))
    }

    @Test
    fun labelAtEntry_withoutHeadersFallsBackToDay() {
        val items = listOf(item(1, millisOf(2024, 5, 2, 12)))
        val entries = buildGridEntries(items, GroupMode.NONE, zone)
        assertEquals("2024年5月2日（木）", labelAtEntry(entries, 0, zone))
        assertNull(labelAtEntry(emptyList(), 0, zone))
    }
}
