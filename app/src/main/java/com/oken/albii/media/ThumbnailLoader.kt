package com.oken.albii.media

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/** MediaStore のサムネイルを読み込み、メモリ上の LRU キャッシュで再利用する。 */
object ThumbnailLoader {
    private const val THUMBNAIL_PX = 256

    /** 同時に走らせる loadThumbnail の数。無制限だとスクロール中に IPC とデコードが詰まる。 */
    private val permits = Semaphore(MAX_CONCURRENT_LOADS)

    private val cache: LruCache<String, Bitmap> by lazy {
        val maxKb = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
        object : LruCache<String, Bitmap>(maxKb) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
        }
    }

    fun cached(uri: Uri): Bitmap? = cache.get(uri.toString())

    suspend fun load(contentResolver: ContentResolver, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        val key = uri.toString()
        cache.get(key) ?: permits.withPermit {
            cache.get(key) ?: runCatching {
                contentResolver.loadThumbnail(uri, Size(THUMBNAIL_PX, THUMBNAIL_PX), null)
            }.getOrNull()?.also { cache.put(key, it) }
        }
    }

    private const val MAX_CONCURRENT_LOADS = 4
}
