package com.oken.albii.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oken.albii.R
import com.oken.albii.data.AppSettings
import com.oken.albii.info.InfoCategory
import com.oken.albii.info.InfoItem
import com.oken.albii.media.GroupMode
import com.oken.albii.media.SortOrder
import com.oken.albii.ui.grid.GridViewModel

/** 設定画面。並び順・グループ・列数を設定し、今後の追加項目（写真情報の表示項目）も置く。 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: GridViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SettingsSection(title = stringResource(R.string.settings_section_display)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.settings_sort))
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        FilterChip(
                            selected = state.sortOrder == SortOrder.NEWEST_AT_TOP,
                            onClick = { viewModel.setSortOrder(SortOrder.NEWEST_AT_TOP) },
                            label = { Text(stringResource(R.string.sort_newest_top)) },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        FilterChip(
                            selected = state.sortOrder == SortOrder.NEWEST_AT_BOTTOM,
                            onClick = { viewModel.setSortOrder(SortOrder.NEWEST_AT_BOTTOM) },
                            label = { Text(stringResource(R.string.sort_newest_bottom)) },
                        )
                    }
                    Text(text = stringResource(R.string.settings_group), modifier = Modifier.padding(top = 16.dp))
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        GroupMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.groupMode == mode,
                                onClick = { viewModel.setGroupMode(mode) },
                                label = {
                                    Text(
                                        stringResource(
                                            when (mode) {
                                                GroupMode.NONE -> R.string.group_none
                                                GroupMode.DAY -> R.string.group_day
                                                GroupMode.MONTH -> R.string.group_month
                                            },
                                        ),
                                    )
                                },
                                modifier = Modifier.padding(end = 8.dp),
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.settings_columns) + "：${state.gridColumns}",
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    Slider(
                        value = state.gridColumns.toFloat(),
                        onValueChange = { viewModel.setGridColumns(it.toInt()) },
                        valueRange = AppSettings.MIN_GRID_COLUMNS.toFloat()..AppSettings.MAX_GRID_COLUMNS.toFloat(),
                        steps = AppSettings.MAX_GRID_COLUMNS - AppSettings.MIN_GRID_COLUMNS - 1,
                    )
                    Text(
                        text = stringResource(R.string.settings_columns_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            SettingsSection(title = stringResource(R.string.settings_section_info)) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.settings_info_hint),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    InfoCategory.entries.forEach { category ->
                        InfoCategoryBlock(
                            category = category,
                            enabledItems = state.enabledInfoItems,
                            onCategoryChange = { viewModel.setInfoCategoryEnabled(category, it) },
                            onItemChange = { item, enabled -> viewModel.setInfoItemEnabled(item, enabled) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCategoryBlock(
    category: InfoCategory,
    enabledItems: Set<InfoItem>,
    onCategoryChange: (Boolean) -> Unit,
    onItemChange: (InfoItem, Boolean) -> Unit,
) {
    val items = InfoItem.entries.filter { it.category == category }
    val allOn = items.all { it in enabledItems }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCategoryChange(!allOn) }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = allOn, onCheckedChange = onCategoryChange)
            Text(
                text = stringResource(category.labelRes),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        items.forEach { item ->
            val checked = item in enabledItems
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemChange(item, !checked) }
                    .padding(start = 40.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = checked, onCheckedChange = { onItemChange(item, it) })
                Text(text = stringResource(item.labelRes), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 12.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            content()
        }
    }
}
