package com.oken.albii.ui.grid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oken.albii.R
import com.oken.albii.media.GridEntry
import com.oken.albii.media.GroupMode
import com.oken.albii.media.SortOrder
import com.oken.albii.media.toContentUri
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGridScreen(viewModel: GridViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val zone = remember { ZoneId.systemDefault() }
    val listState = rememberLazyGridState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.grid_title)) },
                actions = {
                    TextButton(onClick = {
                        viewModel.setSortOrder(
                            if (state.sortOrder == SortOrder.NEWEST_AT_TOP) {
                                SortOrder.NEWEST_AT_BOTTOM
                            } else {
                                SortOrder.NEWEST_AT_TOP
                            },
                        )
                    }) {
                        Text(
                            text = stringResource(
                                if (state.sortOrder == SortOrder.NEWEST_AT_TOP) {
                                    R.string.sort_newest_top
                                } else {
                                    R.string.sort_newest_bottom
                                },
                            ),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            GroupModeChips(
                selected = state.groupMode,
                onSelect = viewModel::setGroupMode,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    !state.isLoaded -> CenterText(stringResource(R.string.grid_loading))
                    state.entries.isEmpty() -> CenterText(stringResource(R.string.grid_empty))
                    else -> {
                        MediaGrid(
                            entries = state.entries,
                            listState = listState,
                            modifier = Modifier.fillMaxSize(),
                        )
                        FastScrollbar(
                            listState = listState,
                            entries = state.entries,
                            zone = zone,
                            modifier = Modifier.align(Alignment.CenterEnd),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupModeChips(selected: GroupMode, onSelect: (GroupMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        GroupMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                label = {
                    Text(
                        stringResource(
                            when (mode) {
                                GroupMode.NONE -> R.string.group_none
                                GroupMode.DAY -> R.string.group_day
                                GroupMode.MONTH -> R.string.group_month
                            },
                        ),
                    )
                },
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

@Composable
private fun MediaGrid(
    entries: List<GridEntry>,
    listState: LazyGridState,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        state = listState,
        modifier = modifier,
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
                is GridEntry.Media -> ThumbnailCell(entry)
            }
        }
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
private fun ThumbnailCell(entry: GridEntry.Media) {
    val item = entry.item
    val thumbnail = rememberThumbnail(remember(item) { item.toContentUri() })
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp),
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

@Composable
private fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}
