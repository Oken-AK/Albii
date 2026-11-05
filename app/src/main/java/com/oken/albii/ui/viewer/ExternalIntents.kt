package com.oken.albii.ui.viewer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.oken.albii.R

/**
 * 外部アプリへ渡す URI に付ける権限は読み取りだけにする。
 * MediaStore の他アプリ所有のファイルには書き込めないので、書き込み権限を付けると
 * startActivity が SecurityException を投げてアプリが落ちる。
 */
internal const val EXTERNAL_GRANT_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION

internal fun buildViewIntent(uri: Uri, mime: String): Intent =
    Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(EXTERNAL_GRANT_FLAGS)

internal fun buildEditIntent(uri: Uri, mime: String): Intent =
    Intent(Intent.ACTION_EDIT).setDataAndType(uri, mime).addFlags(EXTERNAL_GRANT_FLAGS)

internal fun buildShareIntent(uri: Uri, mime: String): Intent =
    Intent(Intent.ACTION_SEND)
        .setType(mime)
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(EXTERNAL_GRANT_FLAGS)

fun launchView(context: Context, item: ViewerItem) =
    startChooserSafely(context, buildViewIntent(item.uri, mimeOf(context, item)))

fun launchEdit(context: Context, item: ViewerItem) =
    startChooserSafely(context, buildEditIntent(item.uri, mimeOf(context, item)))

fun launchShare(context: Context, item: ViewerItem) =
    startChooserSafely(context, buildShareIntent(item.uri, mimeOf(context, item)))

private fun mimeOf(context: Context, item: ViewerItem): String =
    context.contentResolver.getType(item.uri) ?: if (item.isVideo) "video/*" else "image/*"

/** 外部アプリの起動に失敗しても落とさず、トーストで知らせる。 */
private fun startChooserSafely(context: Context, intent: Intent) {
    try {
        context.startActivity(Intent.createChooser(intent, null))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.external_app_not_found, Toast.LENGTH_SHORT).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, R.string.external_open_denied, Toast.LENGTH_SHORT).show()
    }
}
