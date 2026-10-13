package com.oken.albii.ui.grid

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.oken.albii.media.ThumbnailLoader

@Composable
fun rememberThumbnail(uri: Uri): ImageBitmap? {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(
        initialValue = ThumbnailLoader.cached(uri)?.asImageBitmap(),
        key1 = uri,
    ) {
        value = ThumbnailLoader.load(context.contentResolver, uri)?.asImageBitmap()
    }
    return image
}
