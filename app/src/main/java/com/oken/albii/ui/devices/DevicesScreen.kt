package com.oken.albii.ui.devices

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oken.albii.R
import com.oken.albii.media.SortOrder
import com.oken.albii.media.toContentUri
import com.oken.albii.ui.grid.FolderSummary
import com.oken.albii.ui.grid.GridViewModel
import com.oken.albii.ui.grid.MediaGridView
import com.oken.albii.ui.grid.rememberThumbnail
import com.oken.albii.ui.viewer.ViewerItem

/** 端末タブ。端末内のフォルダ（バケット）一覧と、フォルダごとの写真グリッドを表示する。 */
@Composable
fun DevicesScreen(
    onOpen: (items: List<ViewerItem>, startIndex: Int) -> Unit,
    viewModel: GridViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var bucketId by rememberSaveable { mutableStateOf<Long?>(null) }

    val selected = folders.firstOrNull { it.bucketId == bucketId }
    if (bucketId != null && selected != null) {
        BackHandler { bucketId = null }
        val entries = remember(bucketId, state.sortOrder, state.groupMode, state.mediaCount) {
            viewModel.entriesFor(bucketId)
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { bucketId = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(text = selected.name, style = MaterialTheme.typography.titleLarge)
            }
            MediaGridView(
                entries = entries,
                columns = state.gridColumns,
                onColumnsChange = viewModel::setGridColumns,
                onOpen = onOpen,
                newestAtBottom = state.sortOrder == SortOrder.NEWEST_AT_BOTTOM,
                modifier = Modifier.fillMaxSize(),
            )
        }
        return
    }

    if (folders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.devices_empty))
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(folders, key = { it.bucketId }) { folder ->
            FolderRow(folder = folder, onClick = { bucketId = folder.bucketId })
        }
    }
}

@Composable
private fun FolderRow(folder: FolderSummary, onClick: () -> Unit) {
    val cover = rememberThumbnail(remember(folder.cover) { folder.cover.toContentUri() })
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
        ) {
            cover?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = folder.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.devices_count, folder.count),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
