package com.oken.albii.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oken.albii.R
import com.oken.albii.ui.albums.AlbumsScreen
import com.oken.albii.ui.devices.DevicesScreen
import com.oken.albii.ui.grid.GridViewModel
import com.oken.albii.ui.grid.PhotoGridScreen
import com.oken.albii.ui.settings.SettingsScreen
import com.oken.albii.ui.viewer.ViewerItem
import com.oken.albii.ui.viewer.ViewerScreen

private enum class MainTab(
    val titleRes: Int,
    val icon: ImageVector,
) {
    PHOTOS(R.string.tab_photos, Icons.Default.Home),
    ALBUMS(R.string.tab_albums, Icons.Default.Star),
    DEVICES(R.string.tab_devices, Icons.AutoMirrored.Filled.List),
}

private class ViewerRequest(val items: List<ViewerItem>, val startIndex: Int)

/**
 * アプリの骨組み。上部バー（ハンバーガー → 設定）、下部の 3 タブ、
 * 設定とビューアは全画面のオーバーレイとして重ねる。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val gridViewModel: GridViewModel = viewModel()
    val gridState by gridViewModel.state.collectAsStateWithLifecycle()
    var tabIndex by rememberSaveable { mutableIntStateOf(MainTab.PHOTOS.ordinal) }
    val tab = MainTab.entries[tabIndex]
    var menuExpanded by remember { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var viewer by remember { mutableStateOf<ViewerRequest?>(null) }

    val openViewer: (List<ViewerItem>, Int) -> Unit = { items, index ->
        viewer = ViewerRequest(items, index)
    }

    BackHandler(enabled = viewer != null) { viewer = null }
    BackHandler(enabled = showSettings && viewer == null) { showSettings = false }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(tab.titleRes)) },
                    actions = {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu_open))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = {
                                    menuExpanded = false
                                    showSettings = true
                                },
                            )
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    MainTab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = item == tab,
                            onClick = { tabIndex = item.ordinal },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.titleRes)) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    MainTab.PHOTOS -> PhotoGridScreen(onOpen = openViewer, viewModel = gridViewModel)
                    MainTab.ALBUMS -> AlbumsScreen(onOpen = openViewer)
                    MainTab.DEVICES -> DevicesScreen(onOpen = openViewer, viewModel = gridViewModel)
                }
            }
        }

        if (showSettings) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                SettingsScreen(
                    onBack = { showSettings = false },
                    viewModel = gridViewModel,
                )
            }
        }

        viewer?.let { request ->
            ViewerScreen(
                items = request.items,
                startIndex = request.startIndex,
                enabledInfoItems = gridState.enabledInfoItems,
                onClose = { viewer = null },
            )
        }
    }
}
