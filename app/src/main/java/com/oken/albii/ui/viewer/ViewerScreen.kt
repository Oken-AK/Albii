package com.oken.albii.ui.viewer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.oken.albii.R
import com.oken.albii.info.InfoItem
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/** ビューアに渡す 1 件分の情報。写真・動画どちらも content:// の URI で扱う。 */
data class ViewerItem(val uri: Uri, val isVideo: Boolean)

private const val FULL_SIZE_MAX_PX = 2048
private val SWIPE_UP_THRESHOLD = 80.dp

/** 全画面ビューア。左右スワイプで前後へ移動し、ピンチで拡大・縮小する。 */
@Composable
fun ViewerScreen(
    items: List<ViewerItem>,
    startIndex: Int,
    enabledInfoItems: Set<InfoItem>,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, max(0, items.size - 1)),
        pageCount = { items.size },
    )
    var zoomed by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val current = items.getOrNull(pagerState.currentPage)
    val swipeUpThresholdPx = with(LocalDensity.current) { SWIPE_UP_THRESHOLD.toPx() }
    var verticalDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(zoomed) {
                // 下から上へのスワイプで写真情報を開く。拡大中は画像の移動を優先する。
                detectVerticalDragGestures(
                    onDragStart = { verticalDrag = 0f },
                    onVerticalDrag = { _, delta -> verticalDrag += delta },
                    onDragEnd = { if (!zoomed && verticalDrag < -swipeUpThresholdPx) showInfo = true },
                    onDragCancel = { verticalDrag = 0f },
                )
            },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            ViewerPage(
                item = items[page],
                isActive = pagerState.currentPage == page,
                onZoomChanged = { isZoomed -> zoomed = isZoomed },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.viewer_close),
                    tint = Color.White,
                )
            }
        }

        if (showInfo && current != null) {
            PhotoInfoSheet(
                item = current,
                enabledInfoItems = enabledInfoItems,
                onDismiss = { showInfo = false },
            )
        }

        if (current != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (current.isVideo) {
                    IconButton(onClick = { launchExternal(context, current, Intent.ACTION_VIEW) }) {
                        Icon(Icons.Default.PlayArrow, stringResource(R.string.viewer_play), tint = Color.White)
                    }
                } else {
                    IconButton(onClick = { launchExternal(context, current, Intent.ACTION_EDIT) }) {
                        Icon(Icons.Default.Edit, stringResource(R.string.viewer_edit), tint = Color.White)
                    }
                }
                if (!current.isVideo) {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Default.Info, stringResource(R.string.info_button), tint = Color.White)
                    }
                }
                IconButton(onClick = { shareItem(context, current) }) {
                    Icon(Icons.Default.Share, stringResource(R.string.viewer_share), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ViewerPage(item: ViewerItem, isActive: Boolean, onZoomChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = item.uri) {
        value = loadFullSize(context, item)?.asImageBitmap()
    }
    val preview = bitmap ?: return

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }

    // ページが離れたら拡大を戻す。戻ってきたときに拡大されたままにしない。
    LaunchedEffect(isActive) {
        if (!isActive) {
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }
    }

    // 画像の縁が画面の内側に留まる最大移動量。中心基準で拡大するため (scale - 1) / 2 倍の半分。
    fun maxOffsetX(s: Float) = viewport.width * (s - 1f) / 2f
    fun maxOffsetY(s: Float) = viewport.height * (s - 1f) / 2f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { viewport = it }
            .pointerInput(item.uri) {
                // 拡大していない 1 本指のスワイプは消費せず、外側の HorizontalPager に任せる。
                // 拡大中でも、縁に達した状態で外向きに引いた横移動は消費せず、ページ送りに渡す。
                awaitEachGesture {
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val pinching = event.changes.count { it.pressed } >= 2
                        if (pinching || scale > 1f) {
                            val newScale = (scale * event.calculateZoom()).coerceIn(1f, 5f)
                            if (newScale > 1f) {
                                val pan = event.calculatePan()
                                val mx = maxOffsetX(newScale)
                                val my = maxOffsetY(newScale)
                                val pushingOutX = !pinching &&
                                    abs(pan.x) > abs(pan.y) &&
                                    ((pan.x > 0f && offsetX >= mx) || (pan.x < 0f && offsetX <= -mx))
                                if (pushingOutX) {
                                    scale = newScale
                                    onZoomChanged(true)
                                    continue
                                }
                                offsetX = (offsetX + pan.x).coerceIn(-mx, mx)
                                offsetY = (offsetY + pan.y).coerceIn(-my, my)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                            scale = newScale
                            onZoomChanged(newScale > 1.01f)
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = preview,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
        )
    }
}

/** 画面に収まる範囲で大きめの画像をデコードする。動画は端末のサムネイルを使う。 */
private fun loadFullSize(context: Context, item: ViewerItem): Bitmap? = runCatching {
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

private fun launchExternal(context: Context, item: ViewerItem, action: String) {
    val mime = context.contentResolver.getType(item.uri) ?: if (item.isVideo) "video/*" else "image/*"
    val intent = Intent(action)
        .setDataAndType(item.uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, null))
}

private fun shareItem(context: Context, item: ViewerItem) {
    val mime = context.contentResolver.getType(item.uri) ?: if (item.isVideo) "video/*" else "image/*"
    val intent = Intent(Intent.ACTION_SEND)
        .setType(mime)
        .putExtra(Intent.EXTRA_STREAM, item.uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, null))
}
