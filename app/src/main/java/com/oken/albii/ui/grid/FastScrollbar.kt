package com.oken.albii.ui.grid

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oken.albii.media.GridEntry
import com.oken.albii.media.entryIndexForFraction
import com.oken.albii.media.fractionForEntryIndex
import com.oken.albii.media.labelAtEntry
import kotlinx.coroutines.delay
import java.time.ZoneId
import kotlin.math.roundToInt

private val ThumbHeight = 48.dp
private val TrackWidth = 32.dp
private const val AUTO_HIDE_DELAY_MS = 1500L

/**
 * 右端のドラッグ可能なスクロールバー。写真の上に重ねて表示し、
 * スクロールもドラッグもしていないときは一定時間後にフェードアウトする。端に触れると再表示する。
 * ドラッグ中は現在位置の日付ラベルを表示する。
 */
@Composable
fun FastScrollbar(
    listState: LazyGridState,
    entries: List<GridEntry>,
    zone: ZoneId,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var visible by remember { mutableStateOf(false) }
    var wakeTick by remember { mutableIntStateOf(0) }
    val scrolling = listState.isScrollInProgress

    val scrollFraction by remember(entries) {
        derivedStateOf { fractionForEntryIndex(listState.firstVisibleItemIndex, entries.size) }
    }
    val shownFraction = if (dragging) dragFraction else scrollFraction

    LaunchedEffect(scrolling, dragging, wakeTick) {
        visible = true
        if (!scrolling && !dragging) {
            delay(AUTO_HIDE_DELAY_MS)
            visible = false
        }
    }
    val barAlpha by animateFloatAsState(
        targetValue = if (visible || dragging) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrollbarAlpha",
    )

    LaunchedEffect(dragging, dragFraction) {
        if (dragging && entries.isNotEmpty()) {
            listState.scrollToItem(entryIndexForFraction(dragFraction, entries.size))
        }
    }

    fun fractionAt(y: Float): Float =
        if (trackHeightPx <= 0f) 0f else (y / trackHeightPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(TrackWidth)
            .graphicsLayer { alpha = barAlpha }
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    wakeTick++
                    tryAwaitRelease()
                })
            }
            .pointerInput(entries) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        dragging = true
                        dragFraction = fractionAt(offset.y)
                    },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false },
                    onVerticalDrag = { change, _ ->
                        dragFraction = fractionAt(change.position.y)
                    },
                )
            },
    ) {
        val thumbHeightPx = with(density) { ThumbHeight.toPx() }
        val travelPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 4.dp)
                .offset { IntOffset(0, (travelPx * shownFraction).roundToInt()) }
                .width(6.dp)
                .height(ThumbHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(3.dp),
                ),
        )
        if (dragging && entries.isNotEmpty()) {
            val label = labelAtEntry(
                entries,
                entryIndexForFraction(dragFraction, entries.size),
                zone,
            )
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .wrapContentWidth(Alignment.End, unbounded = true)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = (travelPx * dragFraction).roundToInt(),
                            )
                        }
                        .padding(end = 16.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}
