package com.oken.albii.ui.grid

import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.oken.albii.media.GridEntry
import com.oken.albii.media.entryIndexForFraction
import com.oken.albii.media.fractionForEntryIndex
import com.oken.albii.media.labelAtEntry
import java.time.ZoneId
import kotlin.math.roundToInt

private val ThumbHeight = 48.dp
private val TrackWidth = 32.dp

/** 右端のドラッグ可能なスクロールバー。ドラッグ中は現在位置の日付ラベルを表示する。 */
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

    val scrollFraction by remember(entries) {
        derivedStateOf { fractionForEntryIndex(listState.firstVisibleItemIndex, entries.size) }
    }
    val shownFraction = if (dragging) dragFraction else scrollFraction

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
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
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
