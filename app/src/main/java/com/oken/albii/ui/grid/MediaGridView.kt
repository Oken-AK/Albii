package com.oken.albii.ui.grid

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.oken.albii.media.GridEntry
import com.oken.albii.media.toContentUri
import com.oken.albii.ui.viewer.ViewerItem
import com.oken.albii.data.AppSettings

/** ピンチ 1 段階ぶんの拡大率しきい値。これを超えたら列数を 1 つ変える。 */
private const val PINCH_STEP = 1.25f

/**
 * 写真の一覧（列・ヘッダー・右端のスクロールバー・ピンチで列数変更）。
 * [onOpen] には表示中の写真・動画の一覧と、タップされた位置を渡す。
 */
@Composable
fun MediaGridView(
    entries: List<GridEntry>,
    columns: Int,
    onColumnsChange: (Int) -> Unit,
    onOpen: (items: List<ViewerItem>, startIndex: Int) -> Unit,
    listState: LazyGridState = rememberLazyGridState(),
    modifier: Modifier = Modifier,
) {
    val viewerItems = remember(entries) {
        entries.filterIsInstance<GridEntry.Media>().map {
            ViewerItem(it.item.toContentUri(), it.item.isVideo)
        }
    }
    val mediaIndexAt = remember(entries) {
        var count = 0
        IntArray(entries.size) { i ->
            val index = count
            if (entries[i] is GridEntry.Media) count++
            index
        }
    }

    Box(modifier = modifier) {
        PinchToChangeColumns(
            columns = columns,
            onColumnsChange = onColumnsChange,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(end = 40.dp),
            ) {
                items(
                    count = entries.size,
                    key = { index ->
                        when (val entry = entries[index]) {
                            is GridEntry.Header -> "header-${entry.groupKey}"
                            is GridEntry.Media -> "media-${entry.item.isVideo}-${entry.item.id}"
                        }
                    },
                    span = { index ->
                        when (entries[index]) {
                            is GridEntry.Header -> GridItemSpan(maxLineSpan)
                            is GridEntry.Media -> GridItemSpan(1)
                        }
                    },
                ) { index ->
                    when (val entry = entries[index]) {
                        is GridEntry.Header -> HeaderCell(entry.label)
                        is GridEntry.Media -> ThumbnailCell(
                            entry = entry,
                            onClick = { onOpen(viewerItems, mediaIndexAt[index]) },
                        )
                    }
                }
            }
        }
        if (entries.isNotEmpty()) {
            FastScrollbar(
                listState = listState,
                entries = entries,
                zone = java.time.ZoneId.systemDefault(),
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

/**
 * フレームワークの多点ジェスチャー検出（detectTransformGestures）でピンチを拾い、
 * 拡大なら列を減らし（大きく）、縮小なら列を増やす（小さく）。
 */
@Composable
private fun PinchToChangeColumns(
    columns: Int,
    onColumnsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var accumulated by remember { mutableFloatStateOf(1f) }
    val currentColumns by rememberUpdatedState(columns)
    val currentOnChange by rememberUpdatedState(onColumnsChange)

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTransformGestures { _, _, zoom, _ ->
                accumulated *= zoom
                when {
                    accumulated >= PINCH_STEP -> {
                        currentOnChange((currentColumns - 1).coerceAtLeast(AppSettings.MIN_GRID_COLUMNS))
                        accumulated = 1f
                    }
                    accumulated <= 1f / PINCH_STEP -> {
                        currentOnChange((currentColumns + 1).coerceAtMost(AppSettings.MAX_GRID_COLUMNS))
                        accumulated = 1f
                    }
                }
            }
        },
    ) {
        content()
    }
}

@Composable
private fun HeaderCell(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 6.dp),
    )
}

@Composable
private fun ThumbnailCell(entry: GridEntry.Media, onClick: () -> Unit) {
    val item = entry.item
    val thumbnail = rememberThumbnail(remember(item) { item.toContentUri() })
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clickable(onClick = onClick),
    ) {
        thumbnail?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (item.isVideo) {
            Text(
                text = "▶",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
            )
        }
    }
}
