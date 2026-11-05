package com.oken.albii.ui.viewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * ビューアで表示する大きめの画像（動画はポスター用のフレーム）の読み込みとメモリキャッシュ。
 * デコードは重いので、メインスレッドでは行わない。
 */
object ViewerImages {
    private const val FULL_SIZE_MAX_PX = 2048

    private val cache: LruCache<String, Bitmap> by lazy {
        val maxKb = (Runtime.getRuntime().maxMemory() / 1024 / 12).toInt()
        object : LruCache<String, Bitmap>(maxKb) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
        }
    }

    fun cached(uri: Uri): Bitmap? = cache.get(uri.toString())

    suspend fun load(context: Context, item: ViewerItem): Bitmap? = withContext(Dispatchers.IO) {
        val key = item.uri.toString()
        cache.get(key) ?: decode(context, item)?.also { cache.put(key, it) }
    }

    /** 画面に収まる範囲で大きめにデコードする。動画は端末のサムネイル（フレーム）を使う。 */
    private fun decode(context: Context, item: ViewerItem): Bitmap? = runCatching {
        if (item.isVideo) {
            context.contentResolver.loadThumbnail(item.uri, Size(FULL_SIZE_MAX_PX, FULL_SIZE_MAX_PX), null)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, item.uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE)
                val longSide = max(info.size.width, info.size.height)
                if (longSide > FULL_SIZE_MAX_PX) {
                    val ratio = FULL_SIZE_MAX_PX.toFloat() / longSide
                    decoder.setTargetSize(
                        (info.size.width * ratio).roundToInt(),
                        (info.size.height * ratio).roundToInt(),
                    )
                }
            }
        }
    }.getOrNull()
}
