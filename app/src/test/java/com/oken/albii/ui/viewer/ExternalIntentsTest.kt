package com.oken.albii.ui.viewer

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 実機で動画の再生ボタンを押すとアプリが落ちた不具合の回帰テスト。
 * 原因は、本アプリが持たない書き込み権限を外部アプリへ付与しようとして
 * startActivity が SecurityException を投げたこと。
 */
class ExternalIntentsTest {

    @Test
    fun externalGrantIsReadOnly() {
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION, EXTERNAL_GRANT_FLAGS)
    }

    @Test
    fun neverRequestsWriteOrPersistableGrant() {
        val forbidden = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        assertEquals(0, EXTERNAL_GRANT_FLAGS and forbidden)
    }
}
