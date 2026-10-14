package com.oken.albii.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oken.albii.info.InfoItem
import com.oken.albii.media.GroupMode
import com.oken.albii.media.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val sortOrder: SortOrder = SortOrder.NEWEST_AT_TOP,
    val groupMode: GroupMode = GroupMode.DAY,
    val gridColumns: Int = DEFAULT_GRID_COLUMNS,
    val enabledInfoItems: Set<InfoItem> = InfoItem.defaults,
) {
    companion object {
        const val DEFAULT_GRID_COLUMNS = 4
        const val MIN_GRID_COLUMNS = 2
        const val MAX_GRID_COLUMNS = 6
    }
}

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** 設定を端末内の DataStore に保存する。アプリ再起動後も値を保つ。 */
class SettingsStore(private val context: Context) {

    private val sortKey = stringPreferencesKey("sort_order")
    private val groupKey = stringPreferencesKey("group_mode")
    private val columnsKey = intPreferencesKey("grid_columns")
    private val infoItemsKey = stringSetPreferencesKey("info_items")

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            sortOrder = prefs[sortKey]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() }
                ?: SortOrder.NEWEST_AT_TOP,
            groupMode = prefs[groupKey]?.let { runCatching { GroupMode.valueOf(it) }.getOrNull() }
                ?: GroupMode.DAY,
            gridColumns = (prefs[columnsKey] ?: AppSettings.DEFAULT_GRID_COLUMNS)
                .coerceIn(AppSettings.MIN_GRID_COLUMNS, AppSettings.MAX_GRID_COLUMNS),
            enabledInfoItems = prefs[infoItemsKey]
                ?.mapNotNull { name -> runCatching { InfoItem.valueOf(name) }.getOrNull() }
                ?.toSet()
                ?: InfoItem.defaults,
        )
    }

    suspend fun setInfoItems(value: Set<InfoItem>) {
        context.settingsDataStore.edit { it[infoItemsKey] = value.map { item -> item.name }.toSet() }
    }

    suspend fun setSortOrder(value: SortOrder) {
        context.settingsDataStore.edit { it[sortKey] = value.name }
    }

    suspend fun setGroupMode(value: GroupMode) {
        context.settingsDataStore.edit { it[groupKey] = value.name }
    }

    suspend fun setGridColumns(value: Int) {
        context.settingsDataStore.edit {
            it[columnsKey] = value.coerceIn(AppSettings.MIN_GRID_COLUMNS, AppSettings.MAX_GRID_COLUMNS)
        }
    }
}
