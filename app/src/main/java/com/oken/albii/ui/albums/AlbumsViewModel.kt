package com.oken.albii.ui.albums

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oken.albii.data.album.AlbumDatabase
import com.oken.albii.data.album.AlbumEntity
import com.oken.albii.data.album.AlbumItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** アプリ内 DB（Room）に保存したアルバムの一覧・作成・写真の追加・削除を担当する。 */
class AlbumsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AlbumDatabase.create(application).albumDao()

    val albums: StateFlow<List<AlbumEntity>> = dao.observeAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeItems(albumId: Long): Flow<List<AlbumItemEntity>> = dao.observeItems(albumId)

    fun createAlbum(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            dao.insertAlbum(AlbumEntity(name = trimmed, createdAtMillis = System.currentTimeMillis()))
        }
    }

    fun deleteAlbum(albumId: Long) {
        viewModelScope.launch {
            dao.deleteItemsOf(albumId)
            dao.deleteAlbum(albumId)
        }
    }

    /** フォトピッカーで選んだ URI を追加する。永続権限が取れない場合でも参照だけは保存する。 */
    fun addItems(albumId: Long, uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val rows = withContext(Dispatchers.IO) {
                uris.map { uri ->
                    runCatching {
                        getApplication<Application>().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    }
                    AlbumItemEntity(
                        albumId = albumId,
                        uri = uri.toString(),
                        isVideo = getApplication<Application>().contentResolver.getType(uri)
                            ?.startsWith("video/") == true,
                        addedAtMillis = now,
                    )
                }
            }
            dao.insertItems(rows)
        }
    }

    fun removeItem(item: AlbumItemEntity) {
        viewModelScope.launch { dao.deleteItem(item) }
    }
}
