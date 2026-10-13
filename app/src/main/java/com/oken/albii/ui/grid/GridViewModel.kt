package com.oken.albii.ui.grid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val entries: List<GridEntry> = emptyList(),
    val mediaCount: Int = 0,
    val sortOrder: SortOrder = SortOrder.NEWEST_AT_TOP,
    val groupMode: GroupMode = GroupMode.DAY,
)

class GridViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application.contentResolver)
    private val zone: ZoneId = ZoneId.systemDefault()

    private val _state = MutableStateFlow(GridUiState())
    val state: StateFlow<GridUiState> = _state.asStateFlow()

    private var newestFirst: List<MediaItem> = emptyList()
    private var rebuildJob: Job? = null

    fun load() {
        val current = _state.value
        if (current.isLoading || current.isLoaded) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            newestFirst = withContext(Dispatchers.IO) { repository.loadNewestFirst() }
            _state.update { it.copy(isLoaded = true) }
            rebuild()
        }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        if (_state.value.sortOrder == sortOrder) return
        _state.update { it.copy(sortOrder = sortOrder) }
        rebuild()
    }

    fun setGroupMode(groupMode: GroupMode) {
        if (_state.value.groupMode == groupMode) return
        _state.update { it.copy(groupMode = groupMode) }
        rebuild()
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
                    isLoading = false,
                    entries = entries,
                    mediaCount = newestFirst.size,
                )
            }
        }
    }
}
