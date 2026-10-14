package com.oken.albii.ui.grid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oken.albii.data.AppSettings
import com.oken.albii.data.SettingsStore
import com.oken.albii.info.InfoCategory
import com.oken.albii.info.InfoItem
import com.oken.albii.media.GridEntry
import com.oken.albii.media.GroupMode
import com.oken.albii.media.MediaItem
import com.oken.albii.media.MediaRepository
import com.oken.albii.media.SortOrder
import com.oken.albii.media.buildGridEntries
import com.oken.albii.media.orderItems
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GridUiState(
    val isLoaded: Boolean = false,
    val entries: List<GridEntry> = emptyList(),
    val mediaCount: Int = 0,
    val sortOrder: SortOrder = SortOrder.NEWEST_AT_TOP,
    val groupMode: GroupMode = GroupMode.DAY,
    val gridColumns: Int = AppSettings.DEFAULT_GRID_COLUMNS,
    val enabledInfoItems: Set<InfoItem> = InfoItem.defaults,
)

/** 端末のフォルダ（MediaStore のバケット）1 件分の要約。 */
data class FolderSummary(
    val bucketId: Long,
    val name: String,
    val count: Int,
    val cover: MediaItem,
)

class GridViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application.contentResolver)
    private val settingsStore = SettingsStore(application)
    private val zone: ZoneId = ZoneId.systemDefault()

    private val _state = MutableStateFlow(GridUiState())
    val state: StateFlow<GridUiState> = _state.asStateFlow()

    private val _folders = MutableStateFlow<List<FolderSummary>>(emptyList())
    val folders: StateFlow<List<FolderSummary>> = _folders.asStateFlow()

    private var newestFirst: List<MediaItem> = emptyList()
    private var rebuildJob: Job? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            settingsStore.settings.collect { settings ->
                _state.update {
                    it.copy(
                        sortOrder = settings.sortOrder,
                        groupMode = settings.groupMode,
                        gridColumns = settings.gridColumns,
                        enabledInfoItems = settings.enabledInfoItems,
                    )
                }
                rebuild()
            }
        }
    }

    fun load() {
        if (loadJob != null || _state.value.isLoaded) return
        loadJob = viewModelScope.launch {
            newestFirst = withContext(Dispatchers.IO) { repository.loadNewestFirst() }
            _folders.value = buildFolders(newestFirst)
            _state.update { it.copy(isLoaded = true) }
            rebuild()
        }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch { settingsStore.setSortOrder(sortOrder) }
    }

    fun setGroupMode(groupMode: GroupMode) {
        viewModelScope.launch { settingsStore.setGroupMode(groupMode) }
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch { settingsStore.setGridColumns(columns) }
    }

    fun setInfoItemEnabled(item: InfoItem, enabled: Boolean) {
        val current = _state.value.enabledInfoItems
        val next = if (enabled) current + item else current - item
        viewModelScope.launch { settingsStore.setInfoItems(next) }
    }

    fun setInfoCategoryEnabled(category: InfoCategory, enabled: Boolean) {
        val current = _state.value.enabledInfoItems
        val targets = InfoItem.entries.filter { it.category == category }.toSet()
        val next = if (enabled) current + targets else current - targets
        viewModelScope.launch { settingsStore.setInfoItems(next) }
    }

    /** 指定したフォルダ（バケット）の写真を、現在の並び順・グループで組み直す。null なら全件。 */
    fun entriesFor(bucketId: Long?): List<GridEntry> {
        val snapshot = _state.value
        val source = if (bucketId == null) newestFirst else newestFirst.filter { it.bucketId == bucketId }
        return buildGridEntries(
            items = orderItems(source, snapshot.sortOrder),
            mode = snapshot.groupMode,
            zone = zone,
        )
    }

    private fun rebuild() {
        rebuildJob?.cancel()
        rebuildJob = viewModelScope.launch {
            val snapshot = _state.value
            val entries = withContext(Dispatchers.Default) {
                buildGridEntries(
                    items = orderItems(newestFirst, snapshot.sortOrder),
                    mode = snapshot.groupMode,
                    zone = zone,
                )
            }
            _state.update {
                it.copy(
                    entries = entries,
                    mediaCount = newestFirst.size,
                )
            }
        }
    }

    private fun buildFolders(items: List<MediaItem>): List<FolderSummary> =
        items.groupBy { it.bucketId }
            .map { (bucketId, group) ->
                FolderSummary(
                    bucketId = bucketId,
                    name = group.first().bucketName.ifBlank { "(不明)" },
                    count = group.size,
                    cover = group.first(),
                )
            }
            .sortedByDescending { it.cover.dateTakenMillis }
}
