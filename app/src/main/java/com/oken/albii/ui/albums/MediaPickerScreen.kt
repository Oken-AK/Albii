package com.oken.albii.ui.albums

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oken.albii.R
import com.oken.albii.media.MediaItem
import com.oken.albii.media.MediaRepository
import com.oken.albii.media.toContentUri
import com.oken.albii.ui.grid.rememberThumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * アプリ内で端末の写真・動画を複数選択する画面（システムのピッカーは使わない）。
 * [excludedUris] はすでにアルバムに入っている項目で、一覧から外す。
 */
@Composable
fun MediaPickerScreen(
    excludedUris: Set<String>,
    onConfirm: (List<Uri>) -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val media by produceState<List<MediaItem>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            MediaRepository(context.contentResolver).loadNewestFirst()
        }
    }
    var selected by remember { mutableStateOf(emptySet<String>()) }
    val candidates = remember(media, excludedUris) {
        media.orEmpty().filter { it.toContentUri().toString() !in excludedUris }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.viewer_close))
            }
            Text(
                text = stringResource(R.string.picker_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = { onConfirm(selected.map { Uri.parse(it) }) },
                enabled = selected.isNotEmpty(),
            ) {
                Text(stringResource(R.string.picker_add_count, selected.size))
            }
        }

        when {
            media == null -> CenterMessage(stringResource(R.string.grid_loading))
            candidates.isEmpty() -> CenterMessage(stringResource(R.string.picker_empty))
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(2.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(candidates, key = { it.isVideo to it.id }) { item ->
                    val uri = item.toContentUri().toString()
                    PickerCell(
                        item = item,
                        isSelected = uri in selected,
                        onToggle = {
                            selected = if (uri in selected) selected - uri else selected + uri
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerCell(item: MediaItem, isSelected: Boolean, onToggle: () -> Unit) {
    val uri = remember(item) { item.toContentUri() }
    val thumbnail = rememberThumbnail(uri)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clickable(onClick = onToggle),
    ) {
        thumbnail?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CenterMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, modifier = Modifier.padding(24.dp))
    }
}
