package com.oken.albii.media

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

/** 端末内の画像・動画の軽量インデックス（ID と日時のみ）を取得する。サムネイルは表示時に読み込む。 */
class MediaRepository(private val contentResolver: ContentResolver) {

    fun loadNewestFirst(): List<MediaItem> {
        val items = ArrayList<MediaItem>()
        queryTable(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, isVideo = false, into = items)
        queryTable(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, isVideo = true, into = items)
        items.sortByDescending { it.dateTakenMillis }
        return items
    }

    private fun queryTable(uri: Uri, isVideo: Boolean, into: MutableList<MediaItem>) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_ADDED,
        )
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val takenColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
            val addedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            while (cursor.moveToNext()) {
                val taken = if (cursor.isNull(takenColumn)) null else cursor.getLong(takenColumn)
                into += MediaItem(
                    id = cursor.getLong(idColumn),
                    isVideo = isVideo,
                    dateTakenMillis = resolveDateMillis(taken, cursor.getLong(addedColumn)),
                )
            }
        }
    }
}
