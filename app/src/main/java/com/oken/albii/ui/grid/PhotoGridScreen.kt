package com.oken.albii.ui.grid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oken.albii.R
import com.oken.albii.media.GroupMode
import com.oken.albii.media.SortOrder
import com.oken.albii.ui.viewer.ViewerItem

/** フォトタブ。並び替え・グループ切り替えと写真グリッドを表示する（枠は MainScreen が持つ）。 */
@Composable
fun PhotoGridScreen(
    onOpen: (items: List<ViewerItem>, startIndex: Int) -> Unit,
    viewModel: GridViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        ControlRow(
            groupMode = state.groupMode,
            sortOrder = state.sortOrder,
            onGroupSelect = viewModel::setGroupMode,
            onToggleSort = {
                viewModel.setSortOrder(
                    if (state.sortOrder == SortOrder.NEWEST_AT_TOP) {
                        SortOrder.NEWEST_AT_BOTTOM
                    } else {
                        SortOrder.NEWEST_AT_TOP
                    },
                )
            },
        )
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                !state.isLoaded -> CenterText(stringResource(R.string.grid_loading))
                state.entries.isEmpty() -> CenterText(stringResource(R.string.grid_empty))
                else -> MediaGridView(
                    entries = state.entries,
                    columns = state.gridColumns,
                    onColumnsChange = viewModel::setGridColumns,
                    onOpen = onOpen,
                    newestAtBottom = state.sortOrder == SortOrder.NEWEST_AT_BOTTOM,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ControlRow(
    groupMode: GroupMode,
    sortOrder: SortOrder,
    onGroupSelect: (GroupMode) -> Unit,
    onToggleSort: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GroupMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == groupMode,
                onClick = { onGroupSelect(mode) },
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
        Box(modifier = Modifier.weight(1f))
        TextButton(onClick = onToggleSort) {
            Text(
                text = stringResource(
                    if (sortOrder == SortOrder.NEWEST_AT_TOP) {
                        R.string.sort_newest_top
                    } else {
                        R.string.sort_newest_bottom
                    },
                ),
                style = MaterialTheme.typography.labelLarge,
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
