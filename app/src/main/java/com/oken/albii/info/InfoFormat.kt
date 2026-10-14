package com.oken.albii.info

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

private val EXIF_DATETIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss", Locale.ROOT)
private val DISPLAY_DATETIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ROOT)

/** 露出時間。1 秒未満は分数（1/125）、1 秒以上は秒で表す。 */
fun formatShutter(seconds: Double): String =
    if (seconds >= 1.0) {
        String.format(Locale.ROOT, "%.1fs", seconds)
    } else {
        "1/${(1.0 / seconds).roundToInt()}"
    }

/** APEX 値（Av）から F 値を求める。F = 2^(Av/2)。 */
fun apertureFromApex(av: Double): Double = 2.0.pow(av / 2.0)

fun formatAperture(fNumber: Double): String = String.format(Locale.ROOT, "f/%.1f", fNumber)

fun formatExposureBias(ev: Double): String = String.format(Locale.ROOT, "%+.1f EV", ev)

fun formatExposureProgram(value: Int): String? = when (value) {
    1 -> "マニュアル"
    2 -> "プログラム"
    3 -> "絞り優先"
    4 -> "シャッター優先"
    5 -> "クリエイティブ"
    6 -> "アクション"
    7 -> "ポートレート"
    8 -> "風景"
    else -> null
}

fun formatMetering(value: Int): String? = when (value) {
    1 -> "平均"
    2 -> "中央重点"
    3 -> "スポット"
    4 -> "マルチスポット"
    5 -> "評価測光"
    6 -> "部分"
    else -> null
}

fun formatWhiteBalance(value: Int): String? = when (value) {
    0 -> "自動"
    1 -> "手動"
    else -> null
}

/** フラッシュの値は bit 0 が発光の有無。 */
fun formatFlash(value: Int): String = if (value and 1 == 1) "発光" else "非発光"

fun formatOrientation(value: Int): String? = when (value) {
    1 -> "通常"
    3 -> "180°"
    6 -> "90° 時計回り"
    8 -> "270° 時計回り"
    else -> null
}

fun formatColorSpace(value: Int): String? = when (value) {
    1 -> "sRGB"
    65535 -> "未補正"
    else -> null
}

/**
 * EXIF の撮影日時（yyyy:MM:dd HH:mm:ss）と撮影時のオフセット（+09:00）から表示用の文字列を作る。
 * 解析できなければ null。オフセットが無い場合は時刻だけを表示する。
 */
fun formatCapturedAt(rawDateTime: String?, rawOffset: String?): String? {
    val local = rawDateTime?.trim()?.let { runCatching { LocalDateTime.parse(it, EXIF_DATETIME) }.getOrNull() }
        ?: return null
    val offset = rawOffset?.trim()?.let { runCatching { ZoneOffset.of(it) }.getOrNull() }
    val text = local.format(DISPLAY_DATETIME)
    return if (offset == null) text else "$text（UTC${offset.id}）"
}

fun formatFileSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1.0) {
        String.format(Locale.ROOT, "%.1f MB", mb)
    } else {
        String.format(Locale.ROOT, "%.0f KB", bytes / 1024.0)
    }
}

fun formatMegaPixels(width: Int, height: Int): String {
    val mp = width.toDouble() * height / 1_000_000.0
    return String.format(Locale.ROOT, "%d × %d（%.1f MP）", width, height, mp)
}

fun formatLatLong(latitude: Double, longitude: Double): String =
    String.format(Locale.ROOT, "%.5f, %.5f", latitude, longitude)

fun formatFocalLength(mm: Double): String = String.format(Locale.ROOT, "%.1f mm", mm)
