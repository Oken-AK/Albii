package com.oken.albii.ui.albums

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oken.albii.R
import com.oken.albii.data.album.AlbumEntity
import com.oken.albii.data.album.AlbumItemEntity
import com.oken.albii.ui.grid.rememberThumbnail
import com.oken.albii.ui.viewer.ViewerItem

/** アルバムタブ。一覧と詳細（写真のグリッド）を切り替える。 */
@Composable
fun AlbumsScreen(
    onOpen: (items: List<ViewerItem>, startIndex: Int) -> Unit,
    viewModel: AlbumsViewModel = viewModel(),
) {
    val albums by viewModel.albums.collectAsState()
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showCreate by remember { mutableStateOf(false) }

    val selected = albums.firstOrNull { it.id == selectedId }
    if (selectedId != null && selected == null && albums.isNotEmpty()) {
        selectedId = null
    }

    if (selected != null) {
        BackHandler { selectedId = null }
        AlbumDetail(
            album = selected,
            viewModel = viewModel,
            onBack = { selectedId = null },
            onOpen = onOpen,
            onDeleted = { selectedId = null },
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (albums.isEmpty()) {
            CenterMessage(stringResource(R.string.albums_empty))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(albums, key = { it.id }) { album ->
                    AlbumRow(album = album, onClick = { selectedId = album.id })
                }
            }
        }
        FloatingActionButton(
            onClick = { showCreate = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.albums_create))
        }
    }

    if (showCreate) {
        CreateAlbumDialog(
            onConfirm = { name ->
                viewModel.createAlbum(name)
                showCreate = false
            },
            onDismiss = { showCreate = false },
        )
    }
}

@Composable
private fun AlbumRow(album: AlbumEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = album.name, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CreateAlbumDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.albums_create)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.albums_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) { Text(stringResource(R.string.albums_create_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.albums_cancel)) }
        },
    )
}

@Composable
private fun AlbumDetail(
    album: AlbumEntity,
    viewModel: AlbumsViewModel,
    onBack: () -> Unit,
    onOpen: (items: List<ViewerItem>, startIndex: Int) -> Unit,
    onDeleted: () -> Unit,
) {
    val items by viewModel.observeItems(album.id).collectAsState(initial = emptyList())
    var showPicker by rememberSaveable { mutableStateOf(false) }

    if (showPicker) {
        MediaPickerScreen(
            excludedUris = items.map { it.uri }.toSet(),
            onConfirm = { uris ->
                viewModel.addItems(album.id, uris)
                showPicker = false
            },
            onClose = { showPicker = false },
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.album_add_photos))
            }
            IconButton(onClick = {
                viewModel.deleteAlbum(album.id)
                onDeleted()
            }) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.album_delete))
            }
        }

        if (items.isEmpty()) {
            CenterMessage(stringResource(R.string.album_empty))
        } else {
            Text(
                text = stringResource(R.string.album_remove_hint),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            val viewerItems = items.map { ViewerItem(Uri.parse(it.uri), it.isVideo) }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(2.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(items, key = { _, it -> it.uri }) { index, item ->
                    AlbumThumbnail(
                        item = item,
                        onClick = { onOpen(viewerItems, index) },
                        onLongPress = { viewModel.removeItem(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumThumbnail(
    item: AlbumItemEntity,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val thumbnail = rememberThumbnail(remember(item.uri) { Uri.parse(item.uri) })
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .pointerInput(item.uri) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { onLongPress() })
            },
    ) {
        thumbnail?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun CenterMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, modifier = Modifier.padding(24.dp))
    }
}
