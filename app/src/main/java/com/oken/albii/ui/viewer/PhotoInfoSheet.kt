package com.oken.albii.ui.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oken.albii.R
import com.oken.albii.info.InfoItem
import com.oken.albii.info.InfoSection
import com.oken.albii.info.PhotoInfoRepository

/**
 * 写真の詳細情報を下からせり上がるシートで表示する。
 * 表示する項目は設定で選ばれたものだけ。動画は現時点では対象外。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoInfoSheet(
    item: ViewerItem,
    enabledInfoItems: Set<InfoItem>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val loaded by produceState<Result<List<InfoSection>>?>(initialValue = null, item.uri, enabledInfoItems) {
        value = if (item.isVideo || enabledInfoItems.isEmpty()) {
            Result.success(emptyList())
        } else {
            runCatching { PhotoInfoRepository(context).load(item.uri, enabledInfoItems) }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        val result = loaded
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.info_sheet_title),
                style = MaterialTheme.typography.titleLarge,
            )
            when {
                item.isVideo -> Message(R.string.info_sheet_video_unsupported)
                enabledInfoItems.isEmpty() -> Message(R.string.info_sheet_empty)
                result == null -> Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
                result.isFailure -> Message(R.string.info_sheet_load_failed)
                result.getOrNull().isNullOrEmpty() -> Message(R.string.info_sheet_no_data)
                else -> result.getOrNull().orEmpty().forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(section.category.labelRes),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        section.rows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text = stringResource(row.labelRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 16.dp),
                                )
                                Text(
                                    text = row.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Message(resId: Int) {
    Text(
        text = stringResource(resId),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
