package com.oken.albii.media

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class SortOrder { NEWEST_AT_TOP, NEWEST_AT_BOTTOM }

enum class GroupMode { NONE, DAY, MONTH }

data class MediaItem(
    val id: Long,
    val isVideo: Boolean,
    val dateTakenMillis: Long,
    val bucketId: Long = 0L,
    val bucketName: String = "",
)

sealed interface GridEntry {
    data class Header(val groupKey: Long, val label: String) : GridEntry
    data class Media(val item: MediaItem) : GridEntry
}

/** 日付の有無に関わらず、撮影日時が無い場合は追加日時（秒）を使う。 */
fun resolveDateMillis(dateTakenMillis: Long?, dateAddedSeconds: Long): Long =
    if (dateTakenMillis != null && dateTakenMillis > 0) dateTakenMillis else dateAddedSeconds * 1000L

/** 新しいものが上になるよう並べた一覧を、指定の並び順に変換する。 */
fun orderItems(newestFirst: List<MediaItem>, sortOrder: SortOrder): List<MediaItem> =
    if (sortOrder == SortOrder.NEWEST_AT_TOP) newestFirst else newestFirst.asReversed()

fun groupKeyOf(millis: Long, mode: GroupMode, zone: ZoneId): Long {
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    return when (mode) {
        GroupMode.NONE -> 0L
        GroupMode.DAY -> date.toEpochDay()
        GroupMode.MONTH -> date.year * 12L + (date.monthValue - 1)
    }
}

fun groupLabelOf(millis: Long, mode: GroupMode, zone: ZoneId): String {
    val zoned = Instant.ofEpochMilli(millis).atZone(zone)
    val pattern = when (mode) {
        GroupMode.MONTH -> "yyyy年M月"
        GroupMode.DAY, GroupMode.NONE -> "yyyy年M月d日（E）"
    }
    return zoned.format(DateTimeFormatter.ofPattern(pattern, Locale.JAPAN))
}

/** 並び済みのアイテムに、グループが変わるところで見出しを挿入する。 */
fun buildGridEntries(
    items: List<MediaItem>,
    mode: GroupMode,
    zone: ZoneId,
): List<GridEntry> {
    if (mode == GroupMode.NONE) return items.map { GridEntry.Media(it) }

    val entries = ArrayList<GridEntry>(items.size + 64)
    var currentKey: Long? = null
    for (item in items) {
        val key = groupKeyOf(item.dateTakenMillis, mode, zone)
        if (key != currentKey) {
            entries += GridEntry.Header(key, groupLabelOf(item.dateTakenMillis, mode, zone))
            currentKey = key
        }
        entries += GridEntry.Media(item)
    }
    return entries
}

/** スクロールバーの位置（0.0〜1.0）を一覧のインデックスに変換する。 */
fun entryIndexForFraction(fraction: Float, count: Int): Int {
    if (count <= 1) return 0
    return (fraction.coerceIn(0f, 1f) * (count - 1)).roundToInt()
}

fun fractionForEntryIndex(index: Int, count: Int): Float {
    if (count <= 1) return 0f
    return index.coerceIn(0, count - 1).toFloat() / (count - 1)
}

/** 指定位置より前にある直近の見出し（無ければ日付）を表示用ラベルとして返す。 */
fun labelAtEntry(entries: List<GridEntry>, index: Int, zone: ZoneId): String? {
    if (entries.isEmpty()) return null
    for (i in index.coerceIn(0, entries.size - 1) downTo 0) {
        val entry = entries[i]
        if (entry is GridEntry.Header) return entry.label
    }
    val first = entries.getOrNull(index.coerceIn(0, entries.size - 1)) as? GridEntry.Media ?: return null
    return groupLabelOf(first.item.dateTakenMillis, GroupMode.DAY, zone)
}
