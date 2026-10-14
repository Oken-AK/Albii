package com.oken.albii.info

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.StringRes
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InfoRow(@StringRes val labelRes: Int, val value: String)

data class InfoSection(val category: InfoCategory, val rows: List<InfoRow>)

/**
 * 写真の詳細情報を読み出す。[enabled] に含まれる項目だけを返し、値が取れなかった項目は行ごと省く。
 * ExifInterface は読み取り専用で、元画像には一切書き込まない。
 */
class PhotoInfoRepository(private val context: Context) {

    suspend fun load(uri: Uri, enabled: Set<InfoItem>): List<InfoSection> = withContext(Dispatchers.IO) {
        if (enabled.isEmpty()) return@withContext emptyList()
        val exif = readExif(uri)
        val rows = LinkedHashMap<InfoItem, String>()

        fun put(item: InfoItem, value: String?) {
            if (item in enabled && !value.isNullOrBlank()) rows[item] = value
        }

        exif?.let { e ->
            put(InfoItem.ISO, e.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0)
                .takeIf { it > 0 }?.toString())
            put(InfoItem.SHUTTER, e.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, -1.0)
                .takeIf { it > 0 }?.let(::formatShutter))
            put(InfoItem.APERTURE, e.getAttributeDouble(ExifInterface.TAG_F_NUMBER, -1.0)
                .takeIf { it > 0 }?.let(::formatAperture)
                ?: e.getAttributeDouble(ExifInterface.TAG_APERTURE_VALUE, Double.NaN)
                    .takeIf { !it.isNaN() }?.let { formatAperture(apertureFromApex(it)) })
            put(InfoItem.EXPOSURE_BIAS, e.getAttributeDouble(ExifInterface.TAG_EXPOSURE_BIAS_VALUE, Double.NaN)
                .takeIf { !it.isNaN() }?.let(::formatExposureBias))
            put(InfoItem.EXPOSURE_PROGRAM, e.getAttributeInt(ExifInterface.TAG_EXPOSURE_PROGRAM, -1)
                .takeIf { it >= 0 }?.let(::formatExposureProgram))
            put(InfoItem.METERING, e.getAttributeInt(ExifInterface.TAG_METERING_MODE, -1)
                .takeIf { it >= 0 }?.let(::formatMetering))
            put(InfoItem.WHITE_BALANCE, e.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, -1)
                .takeIf { it >= 0 }?.let(::formatWhiteBalance))
            put(InfoItem.FLASH, e.getAttributeInt(ExifInterface.TAG_FLASH, -1)
                .takeIf { it >= 0 }?.let(::formatFlash))

            put(InfoItem.FOCAL_LENGTH, e.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, -1.0)
                .takeIf { it > 0 }?.let(::formatFocalLength))
            put(InfoItem.FOCAL_LENGTH_35, e.getAttributeInt(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, 0)
                .takeIf { it > 0 }?.let { "$it mm" })
            put(InfoItem.MAX_APERTURE, e.getAttributeDouble(ExifInterface.TAG_MAX_APERTURE_VALUE, -1.0)
                .takeIf { it > 0 }?.let { formatAperture(apertureFromApex(it)) })
            put(InfoItem.LENS_MODEL, e.getAttributeOrNull(ExifInterface.TAG_LENS_MODEL))

            put(InfoItem.MAKE, e.getAttributeOrNull(ExifInterface.TAG_MAKE))
            put(InfoItem.MODEL, e.getAttributeOrNull(ExifInterface.TAG_MODEL))
            put(InfoItem.SOFTWARE, e.getAttributeOrNull(ExifInterface.TAG_SOFTWARE))

            put(InfoItem.CAPTURED_AT, formatCapturedAt(
                e.getAttributeOrNull(ExifInterface.TAG_DATETIME_ORIGINAL),
                e.getAttributeOrNull(ExifInterface.TAG_OFFSET_TIME_ORIGINAL),
            ))
            if (InfoItem.GPS in enabled) {
                e.latLong?.let { put(InfoItem.GPS, formatLatLong(it[0], it[1])) }
            }

            put(InfoItem.ORIENTATION, e.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
                .takeIf { it >= 0 }?.let(::formatOrientation))
            put(InfoItem.COLOR_SPACE, e.getAttributeInt(ExifInterface.TAG_COLOR_SPACE, -1)
                .takeIf { it >= 0 }?.let(::formatColorSpace))
        }

        if (InfoItem.DIMENSIONS in enabled) {
            readDimensions(uri)?.let { (w, h) -> put(InfoItem.DIMENSIONS, formatMegaPixels(w, h)) }
        }
        if (InfoItem.FILE_SIZE in enabled) {
            readSize(uri)?.let { put(InfoItem.FILE_SIZE, formatFileSize(it)) }
        }
        if (InfoItem.FORMAT in enabled) {
            put(InfoItem.FORMAT, context.contentResolver.getType(uri)?.substringAfter('/')?.uppercase())
        }

        InfoItem.entries
            .filter { it in rows }
            .groupBy { it.category }
            .map { (category, items) ->
                InfoSection(category, items.map { InfoRow(it.labelRes, rows.getValue(it)) })
            }
    }

    private fun readExif(uri: Uri): ExifInterface? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
    }.getOrNull()

    private fun readDimensions(uri: Uri): Pair<Int, Int>? = runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        if (options.outWidth > 0 && options.outHeight > 0) options.outWidth to options.outHeight else null
    }.getOrNull()

    private fun readSize(uri: Uri): Long? = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else null
        }
    }.getOrNull()

    private fun ExifInterface.getAttributeOrNull(tag: String): String? =
        getAttribute(tag)?.takeIf { it.isNotBlank() }
}
