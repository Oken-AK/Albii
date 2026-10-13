package com.oken.albii.media

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

fun MediaItem.toContentUri(): Uri {
    val base = if (isVideo) {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    return ContentUris.withAppendedId(base, id)
}
