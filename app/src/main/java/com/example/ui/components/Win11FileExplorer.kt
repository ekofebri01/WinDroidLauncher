package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExplorerItem
import com.example.model.ExplorerLocation
import com.example.model.ExplorerSortBy
import com.example.model.ExplorerViewMode
import com.example.model.FileExplorerState
import com.example.model.MountedSafFolder
import com.example.model.SystemTelemetry
import com.example.ui.theme.LocalWin11Colors
import java.util.Locale

private enum class ExplorerDialogMode {
    NONE,
    NEW_FOLDER,
    NEW_FILE,
    RENAME
}

@Composable
fun Win11FileExplorerContent(
    state: FileExplorerState,
    telemetry: SystemTelemetry,
    installedAppsCount: Int,
    onSelectTab: (Int) -> Unit,
    onAddTab: () -> Unit,
    onCloseTab: (Int) -> Unit,
    onNavigate: (ExplorerLocation) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onUp: () -> Unit,
    onRefresh: () -> Unit,
    onSearchChange: (String) -> Unit,
    onSetViewMode: (ExplorerViewMode) -> Unit,
    onSetSortBy: (ExplorerSortBy) -> Unit,
    onToggleDetailsPane: () -> Unit,
    onItemClick: (ExplorerItem) -> Unit,
    onItemToggleSelect: (ExplorerItem) -> Unit,
    onClearSelection: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onCreateTextFile: (String) -> Unit,
    onImportFile: () -> Unit,
    onMountDeviceFolder: () -> Unit,
    onOpenMountedFolder: (MountedSafFolder) -> Unit,
    onUnmountSafFolder: (String) -> Unit,
    onRenameSelected: (String) -> Unit,
    onCopySelected: () -> Unit,
    onCutSelected: () -> Unit,
    onPaste: () -> Unit,
    onDeleteSelected: () -> Unit,
    onRestoreSelected: () -> Unit,
    onEmptyTrash: () -> Unit,
    onShareItem: (ExplorerItem) -> Unit,
    onOpenAppManager: () -> Unit,
    onOpenSystemStorage: () -> Unit,
    onSavePreviewText: (String) -> Unit,
    onOpenPreviewExternally: (ExplorerItem) -> Unit,
    onSetImageAsWallpaper: (ExplorerItem) -> Unit,
    onDismissPreview: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val activeTab = state.activeTab
    val location = activeTab.location
    val selectedItems = remember(state.selectedIds, state.currentItems) {
        state.currentItems.filter { state.selectedIds.contains(it.id) }
    }

    var dialogMode by remember { mutableStateOf(ExplorerDialogMode.NONE) }
    var showNewMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showViewMenu by remember { mutableStateOf(false) }
    var showNavDrawerCompact by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("win11_file_explorer")
    ) {
        // 1. Windows 11 Tab Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(winColors.cardSurface.copy(alpha = 0.55f))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                state.tabs.forEachIndexed { index, tab ->
                    val isSelected = index == state.activeTabIndex
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(if (isSelected) winColors.micaSurface else Color.Transparent)
                            .clickable { onSelectTab(index) }
                            .padding(horizontal = 10.dp)
                            .testTag("explorer_tab_$index"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Win11FolderIcon(size = 15.dp)
                        Text(
                            text = tab.location.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = winColors.textPrimary,
                            maxLines = 1
                        )
                        if (state.tabs.size > 1) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close tab",
                                tint = winColors.textSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onCloseTab(index) }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onAddTab,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("explorer_new_tab_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New tab",
                        tint = winColors.textPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // 2. Navigation, Breadcrumb Address Bar & Search Row
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(winColors.micaSurface)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val isWide = maxWidth >= 560.dp
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isWide) {
                        IconButton(
                            onClick = { showNavDrawerCompact = !showNavDrawerCompact },
                            modifier = Modifier.size(34.dp).testTag("explorer_nav_toggle")
                        ) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Navigation pane", tint = winColors.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    NavIconBtn(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        enabled = activeTab.backStack.isNotEmpty(),
                        contentDescription = "Back",
                        onClick = onBack
                    )
                    NavIconBtn(
                        icon = Icons.AutoMirrored.Rounded.ArrowForward,
                        enabled = activeTab.forwardStack.isNotEmpty(),
                        contentDescription = "Forward",
                        onClick = onForward
                    )
                    NavIconBtn(
                        icon = Icons.Rounded.ArrowUpward,
                        enabled = location != ExplorerLocation.Home,
                        contentDescription = "Up",
                        onClick = onUp
                    )
                    NavIconBtn(
                        icon = Icons.Rounded.Refresh,
                        enabled = true,
                        contentDescription = "Refresh",
                        onClick = onRefresh
                    )

                    // Breadcrumb Address Pill
                    ExplorerBreadcrumbBar(
                        location = location,
                        onNavigate = onNavigate,
                        modifier = Modifier.weight(1f)
                    )

                    if (isWide) {
                        ExplorerSearchBox(
                            query = state.searchQuery,
                            placeholder = "Search ${location.title}",
                            onQueryChange = onSearchChange,
                            modifier = Modifier.width(190.dp)
                        )
                    }
                }

                if (!isWide) {
                    ExplorerSearchBox(
                        query = state.searchQuery,
                        placeholder = "Search ${location.title}",
                        onQueryChange = onSearchChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        HorizontalDivider(color = winColors.subtleBorder)

        // 3. Windows 11 Command Bar (+ New, Cut, Copy, Paste, Rename, Share, Delete, Sort, View, Details)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(winColors.cardSurface.copy(alpha = 0.45f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // + New Dropdown
            Box {
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showNewMenu = true }
                        .padding(horizontal = 10.dp)
                        .testTag("explorer_new_menu_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(winColors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                    Text("New", style = MaterialTheme.typography.labelMedium, color = winColors.textPrimary)
                }

                DropdownMenu(
                    expanded = showNewMenu,
                    onDismissRequest = { showNewMenu = false },
                    containerColor = winColors.micaSurface
                ) {
                    DropdownMenuItem(
                        text = { Text("Folder", color = winColors.textPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, tint = Color(0xFFF6B93B)) },
                        onClick = {
                            showNewMenu = false
                            dialogMode = ExplorerDialogMode.NEW_FOLDER
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Text Document", color = winColors.textPrimary) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.NoteAdd, contentDescription = null, tint = winColors.accent) },
                        onClick = {
                            showNewMenu = false
                            dialogMode = ExplorerDialogMode.NEW_FILE
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import File from Device...", color = winColors.textPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = winColors.textPrimary) },
                        onClick = {
                            showNewMenu = false
                            onImportFile()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Mount Device Folder (SAF)...", color = winColors.textPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = winColors.accent) },
                        onClick = {
                            showNewMenu = false
                            onMountDeviceFolder()
                        }
                    )
                }
            }

            VerticalDivider(modifier = Modifier.height(22.dp).padding(horizontal = 4.dp), color = winColors.subtleBorder)

            val hasSelection = selectedItems.isNotEmpty()
            val singleSelected = selectedItems.size == 1

            CommandIconBtn(Icons.Rounded.ContentCut, "Cut", hasSelection, onCutSelected)
            CommandIconBtn(Icons.Rounded.ContentCopy, "Copy", hasSelection, onCopySelected)
            CommandIconBtn(
                Icons.Rounded.ContentPaste,
                "Paste",
                state.clipboard != null && location is ExplorerLocation.LocalFolder,
                onPaste
            )
            CommandIconBtn(
                Icons.Rounded.DriveFileRenameOutline,
                "Rename",
                singleSelected && location != ExplorerLocation.RecycleBin
            ) {
                dialogMode = ExplorerDialogMode.RENAME
            }
            CommandIconBtn(
                Icons.Rounded.Share,
                "Share",
                singleSelected && !selectedItems.first().isDirectory
            ) {
                onShareItem(selectedItems.first())
            }
            if (location == ExplorerLocation.RecycleBin) {
                CommandIconBtn(Icons.Rounded.RestoreFromTrash, "Restore", hasSelection, onRestoreSelected)
                CommandIconBtn(Icons.Rounded.DeleteForever, "Empty Bin", state.currentItems.isNotEmpty(), onEmptyTrash)
            } else {
                CommandIconBtn(Icons.Rounded.Delete, "Delete", hasSelection, onDeleteSelected)
            }

            VerticalDivider(modifier = Modifier.height(22.dp).padding(horizontal = 4.dp), color = winColors.subtleBorder)

            // Sort Dropdown
            Box {
                CommandLabelBtn(Icons.AutoMirrored.Rounded.Sort, "Sort") { showSortMenu = true }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    containerColor = winColors.micaSurface
                ) {
                    ExplorerSortBy.entries.forEach { sortOption ->
                        val active = state.sortBy == sortOption
                        val arrow = if (active) (if (state.sortAscending) " ↑" else " ↓") else ""
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${sortOption.label}$arrow",
                                    color = if (active) winColors.accent else winColors.textPrimary
                                )
                            },
                            onClick = {
                                showSortMenu = false
                                onSetSortBy(sortOption)
                            }
                        )
                    }
                }
            }

            // View Dropdown
            Box {
                CommandLabelBtn(Icons.Rounded.GridView, "View") { showViewMenu = true }
                DropdownMenu(
                    expanded = showViewMenu,
                    onDismissRequest = { showViewMenu = false },
                    containerColor = winColors.micaSurface
                ) {
                    ExplorerViewMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.viewMode == mode) "✓ ${mode.label}" else mode.label,
                                    color = if (state.viewMode == mode) winColors.accent else winColors.textPrimary
                                )
                            },
                            onClick = {
                                showViewMenu = false
                                onSetViewMode(mode)
                            }
                        )
                    }
                }
            }

            CommandLabelBtn(
                icon = Icons.Rounded.Info,
                label = "Details",
                highlighted = state.showDetailsPane,
                onClick = onToggleDetailsPane
            )
        }

        HorizontalDivider(color = winColors.subtleBorder)

        // 4. Main Split Body (Navigation Pane + File Canvas + Details Inspector)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val showPermanentNav = maxWidth >= 560.dp
            val showRightDetails = state.showDetailsPane && maxWidth >= 620.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (showPermanentNav) {
                    ExplorerNavigationPane(
                        activeLocation = location,
                        mountedFolders = state.mountedSafFolders,
                        onNavigate = onNavigate,
                        onOpenMounted = onOpenMountedFolder,
                        onUnmount = onUnmountSafFolder,
                        onMountNew = onMountDeviceFolder,
                        modifier = Modifier
                            .width(186.dp)
                            .fillMaxHeight()
                    )
                    VerticalDivider(color = winColors.subtleBorder)
                }

                // Center Content Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (location) {
                        ExplorerLocation.Home -> ExplorerHomeView(
                            recentFiles = state.recentFiles,
                            mountedFolders = state.mountedSafFolders,
                            onNavigate = onNavigate,
                            onOpenMounted = onOpenMountedFolder,
                            onMountNew = onMountDeviceFolder,
                            onOpenItem = onItemClick
                        )
                        ExplorerLocation.ThisPc -> ExplorerThisPcView(
                            telemetry = telemetry,
                            installedAppsCount = installedAppsCount,
                            mountedFolders = state.mountedSafFolders,
                            onNavigate = onNavigate,
                            onOpenMounted = onOpenMountedFolder,
                            onMountNew = onMountDeviceFolder,
                            onOpenAppManager = onOpenAppManager,
                            onOpenSystemStorage = onOpenSystemStorage
                        )
                        else -> ExplorerFolderItemsView(
                            items = state.currentItems,
                            selectedIds = state.selectedIds,
                            viewMode = state.viewMode,
                            isRecycleBin = location == ExplorerLocation.RecycleBin,
                            onItemClick = onItemClick,
                            onItemToggleSelect = onItemToggleSelect,
                            onMountNew = onMountDeviceFolder
                        )
                    }

                    // Compact Mobile Navigation Drawer Overlay
                    if (!showPermanentNav && showNavDrawerCompact) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.28f))
                                .clickable { showNavDrawerCompact = false }
                        ) {
                            ExplorerNavigationPane(
                                activeLocation = location,
                                mountedFolders = state.mountedSafFolders,
                                onNavigate = {
                                    onNavigate(it)
                                    showNavDrawerCompact = false
                                },
                                onOpenMounted = {
                                    onOpenMountedFolder(it)
                                    showNavDrawerCompact = false
                                },
                                onUnmount = onUnmountSafFolder,
                                onMountNew = {
                                    showNavDrawerCompact = false
                                    onMountDeviceFolder()
                                },
                                modifier = Modifier
                                    .width(210.dp)
                                    .fillMaxHeight()
                                    .background(winColors.micaSurface)
                            )
                        }
                    }
                }

                if (showRightDetails) {
                    VerticalDivider(color = winColors.subtleBorder)
                    ExplorerDetailsPane(
                        location = location,
                        selectedItem = selectedItems.firstOrNull(),
                        totalItems = state.currentItems.size,
                        modifier = Modifier
                            .width(205.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }

        // 5. Bottom Status Bar
        HorizontalDivider(color = winColors.subtleBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(winColors.cardSurface.copy(alpha = 0.6f))
                .padding(start = 12.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val countText = when (location) {
                ExplorerLocation.Home -> "${state.recentFiles.size} recent files"
                ExplorerLocation.ThisPc -> "1 drive • ${state.mountedSafFolders.size} mounted folder(s)"
                else -> {
                    val base = "${state.currentItems.size} items"
                    if (selectedItems.isNotEmpty()) {
                        val totalBytes = selectedItems.filterNot { it.isDirectory }.sumOf { it.sizeBytes }
                        val sizePart = if (totalBytes > 0L) " (${formatExplorerBytes(totalBytes, false)})" else ""
                        "$base  |  ${selectedItems.size} selected$sizePart"
                    } else base
                }
            }
            Text(
                text = countText,
                style = MaterialTheme.typography.labelSmall,
                color = winColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ViewList,
                    contentDescription = "Details view",
                    tint = if (state.viewMode == ExplorerViewMode.DETAILS) winColors.accent else winColors.textSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onSetViewMode(ExplorerViewMode.DETAILS) }
                )
                Icon(
                    imageVector = Icons.Rounded.GridView,
                    contentDescription = "Grid view",
                    tint = if (state.viewMode != ExplorerViewMode.DETAILS) winColors.accent else winColors.textSecondary,
                    modifier = Modifier
                        .size(17.dp)
                        .clickable { onSetViewMode(ExplorerViewMode.LARGE_ICONS) }
                )
            }
        }
    }

    // Dialogs
    when (dialogMode) {
        ExplorerDialogMode.NEW_FOLDER -> ExplorerNameInputDialog(
            title = "New Folder",
            label = "Folder name",
            initialValue = "New folder",
            confirmText = "Create",
            onDismiss = { dialogMode = ExplorerDialogMode.NONE },
            onConfirm = {
                onCreateFolder(it)
                dialogMode = ExplorerDialogMode.NONE
            }
        )
        ExplorerDialogMode.NEW_FILE -> ExplorerNameInputDialog(
            title = "New Text Document",
            label = "File name",
            initialValue = "New Text Document.txt",
            confirmText = "Create",
            onDismiss = { dialogMode = ExplorerDialogMode.NONE },
            onConfirm = {
                onCreateTextFile(it)
                dialogMode = ExplorerDialogMode.NONE
            }
        )
        ExplorerDialogMode.RENAME -> {
            val currentName = selectedItems.firstOrNull()?.name.orEmpty()
            ExplorerNameInputDialog(
                title = "Rename",
                label = "New name",
                initialValue = currentName,
                confirmText = "Rename",
                onDismiss = { dialogMode = ExplorerDialogMode.NONE },
                onConfirm = {
                    onRenameSelected(it)
                    dialogMode = ExplorerDialogMode.NONE
                }
            )
        }
        ExplorerDialogMode.NONE -> Unit
    }

    state.previewState?.let { preview ->
        ExplorerPreviewDialog(
            preview = preview,
            onDismiss = onDismissPreview,
            onSaveText = onSavePreviewText,
            onOpenExternally = onOpenPreviewExternally,
            onSetAsWallpaper = onSetImageAsWallpaper
        )
    }
}

@Composable
private fun ExplorerNavigationPane(
    activeLocation: ExplorerLocation,
    mountedFolders: List<MountedSafFolder>,
    onNavigate: (ExplorerLocation) -> Unit,
    onOpenMounted: (MountedSafFolder) -> Unit,
    onUnmount: (String) -> Unit,
    onMountNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        NavPaneRow(
            icon = Icons.Rounded.Home,
            iconTint = Color(0xFF60CDFF),
            label = "Home",
            selected = activeLocation == ExplorerLocation.Home,
            onClick = { onNavigate(ExplorerLocation.Home) }
        )
        NavPaneRow(
            icon = Icons.Rounded.Computer,
            iconTint = Color(0xFF4CC2FF),
            label = "Desktop",
            selected = activeLocation == ExplorerLocation.LocalFolder("Desktop", "Desktop"),
            onClick = { onNavigate(ExplorerLocation.LocalFolder("Desktop", "Desktop")) }
        )
        NavPaneRow(
            icon = Icons.Rounded.Download,
            iconTint = Color(0xFF2FA8F6),
            label = "Downloads",
            selected = activeLocation == ExplorerLocation.LocalFolder("Downloads", "Downloads"),
            onClick = { onNavigate(ExplorerLocation.LocalFolder("Downloads", "Downloads")) }
        )
        NavPaneRow(
            icon = Icons.Rounded.Description,
            iconTint = Color(0xFFF6B93B),
            label = "Documents",
            selected = activeLocation == ExplorerLocation.LocalFolder("Documents", "Documents"),
            onClick = { onNavigate(ExplorerLocation.LocalFolder("Documents", "Documents")) }
        )
        NavPaneRow(
            icon = Icons.Rounded.Image,
            iconTint = Color(0xFF4CD4A9),
            label = "Pictures",
            selected = activeLocation == ExplorerLocation.LocalFolder("Pictures", "Pictures"),
            onClick = { onNavigate(ExplorerLocation.LocalFolder("Pictures", "Pictures")) }
        )
        NavPaneRow(
            icon = Icons.Rounded.MusicNote,
            iconTint = Color(0xFFFF8C42),
            label = "Music",
            selected = activeLocation == ExplorerLocation.LocalFolder("Music", "Music"),
            onClick = { onNavigate(ExplorerLocation.LocalFolder("Music", "Music")) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = winColors.subtleBorder)

        NavPaneRow(
            icon = Icons.Rounded.Computer,
            iconTint = winColors.accent,
            label = "This PC",
            selected = activeLocation == ExplorerLocation.ThisPc,
            onClick = { onNavigate(ExplorerLocation.ThisPc) }
        )
        NavPaneRow(
            icon = Icons.Rounded.Storage,
            iconTint = Color(0xFF94A3B8),
            label = "Local Disk (C:)",
            selected = activeLocation == ExplorerLocation.LocalFolder("", "Local Disk (C:)"),
            onClick = { onNavigate(ExplorerLocation.LocalFolder("", "Local Disk (C:)")) }
        )
        NavPaneRow(
            icon = Icons.Rounded.Delete,
            iconTint = Color(0xFF9BB2C7),
            label = "Recycle Bin",
            selected = activeLocation == ExplorerLocation.RecycleBin,
            onClick = { onNavigate(ExplorerLocation.RecycleBin) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = winColors.subtleBorder)

        Text(
            text = "Device Storage (SAF)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = winColors.textSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        mountedFolders.forEach { mounted ->
            val isSelected = activeLocation is ExplorerLocation.SafFolder &&
                activeLocation.treeUriString == mounted.treeUriString
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) winColors.selectionFill else Color.Transparent)
                    .clickable { onOpenMounted(mounted) }
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = Color(0xFFF6B93B), modifier = Modifier.size(16.dp))
                Text(
                    text = mounted.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Unmount",
                    tint = winColors.textSecondary,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onUnmount(mounted.treeUriString) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onMountNew)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .testTag("explorer_mount_folder_nav"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = winColors.accent, modifier = Modifier.size(16.dp))
            Text(
                text = "Mount Folder...",
                style = MaterialTheme.typography.labelMedium,
                color = winColors.accent
            )
        }
    }
}

@Composable
private fun NavPaneRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) winColors.selectionFill else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(17.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = winColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExplorerBreadcrumbBar(
    location: ExplorerLocation,
    onNavigate: (ExplorerLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(winColors.searchFieldBg)
            .border(1.dp, winColors.border, RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Rounded.Computer, contentDescription = null, tint = winColors.accent, modifier = Modifier.size(15.dp))
        when (location) {
            ExplorerLocation.Home -> {
                BreadcrumbSegment("Home", onClick = { onNavigate(ExplorerLocation.Home) })
            }
            ExplorerLocation.ThisPc -> {
                BreadcrumbSegment("This PC", onClick = { onNavigate(ExplorerLocation.ThisPc) })
            }
            ExplorerLocation.RecycleBin -> {
                BreadcrumbSegment("Recycle Bin", onClick = { onNavigate(ExplorerLocation.RecycleBin) })
            }
            is ExplorerLocation.LocalFolder -> {
                BreadcrumbSegment("This PC", onClick = { onNavigate(ExplorerLocation.ThisPc) })
                BreadcrumbArrow()
                BreadcrumbSegment("Local Disk (C:)", onClick = {
                    onNavigate(ExplorerLocation.LocalFolder("", "Local Disk (C:)"))
                })
                val parts = location.relativePath.trim('/').split('/').filter { it.isNotBlank() }
                var running = ""
                parts.forEach { part ->
                    running = if (running.isBlank()) part else "$running/$part"
                    val targetRel = running
                    BreadcrumbArrow()
                    BreadcrumbSegment(part, onClick = {
                        onNavigate(ExplorerLocation.LocalFolder(targetRel, part))
                    })
                }
            }
            is ExplorerLocation.SafFolder -> {
                BreadcrumbSegment("This PC", onClick = { onNavigate(ExplorerLocation.ThisPc) })
                location.breadcrumbs.forEach { crumb ->
                    BreadcrumbArrow()
                    BreadcrumbSegment(crumb.title, onClick = {
                        val idx = location.breadcrumbs.indexOf(crumb)
                        val subTrail = location.breadcrumbs.take(idx + 1)
                        onNavigate(
                            ExplorerLocation.SafFolder(
                                treeUriString = location.treeUriString,
                                documentUriString = crumb.documentUriString,
                                title = crumb.title,
                                breadcrumbs = subTrail
                            )
                        )
                    })
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbSegment(text: String, onClick: () -> Unit) {
    val winColors = LocalWin11Colors.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = winColors.textPrimary,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun BreadcrumbArrow() {
    val winColors = LocalWin11Colors.current
    Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        tint = winColors.textSecondary,
        modifier = Modifier.size(14.dp)
    )
}

@Composable
private fun ExplorerSearchBox(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(winColors.searchFieldBg)
            .border(1.dp, winColors.border, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = winColors.textSecondary, modifier = Modifier.size(15.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = winColors.textPrimary),
                cursorBrush = SolidColor(winColors.accent),
                modifier = Modifier.fillMaxWidth().testTag("explorer_search_input")
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear",
                tint = winColors.textSecondary,
                modifier = Modifier.size(14.dp).clickable { onQueryChange("") }
            )
        }
    }
}

@Composable
private fun ExplorerHomeView(
    recentFiles: List<ExplorerItem>,
    mountedFolders: List<MountedSafFolder>,
    onNavigate: (ExplorerLocation) -> Unit,
    onOpenMounted: (MountedSafFolder) -> Unit,
    onMountNew: () -> Unit,
    onOpenItem: (ExplorerItem) -> Unit
) {
    val winColors = LocalWin11Colors.current
    val quickFolders = listOf(
        Triple("Desktop", "Desktop", Icons.Rounded.Computer),
        Triple("Downloads", "Downloads", Icons.Rounded.Download),
        Triple("Documents", "Documents", Icons.Rounded.Description),
        Triple("Pictures", "Pictures", Icons.Rounded.Image),
        Triple("Music", "Music", Icons.Rounded.MusicNote)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Quick access",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quickFolders.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { (title, rel, _) ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(winColors.cardSurface)
                                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                                    .clickable { onNavigate(ExplorerLocation.LocalFolder(rel, title)) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Win11FolderIcon(size = 28.dp)
                                Column {
                                    Text(title, style = MaterialTheme.typography.titleSmall, color = winColors.textPrimary)
                                    Text("Local Disk (C:)", style = MaterialTheme.typography.labelSmall, color = winColors.textSecondary)
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Device Storage Folders (SAF)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = winColors.textPrimary
                )
                Text(
                    text = "+ Mount Folder",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = winColors.accent,
                    modifier = Modifier.clickable(onClick = onMountNew).padding(4.dp)
                )
            }
        }

        if (mountedFolders.isEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(winColors.cardSurface.copy(alpha = 0.7f))
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onMountNew)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = winColors.accent, modifier = Modifier.size(24.dp))
                    Column {
                        Text("Connect phone folder (Downloads, DCIM, Documents)", style = MaterialTheme.typography.bodyMedium, color = winColors.textPrimary)
                        Text("Tap to browse real Android storage safely", style = MaterialTheme.typography.labelSmall, color = winColors.textSecondary)
                    }
                }
            }
        } else {
            items(mountedFolders, key = { it.treeUriString }) { mounted ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                        .clickable { onOpenMounted(mounted) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Win11FolderIcon(size = 26.dp)
                    Text(mounted.displayName, style = MaterialTheme.typography.bodyMedium, color = winColors.textPrimary)
                }
            }
        }

        item {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(recentFiles, key = { it.id }) { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenItem(file) }
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExplorerItemIcon(item = file, size = 24.dp)
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = winColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatExplorerDate(file.lastModifiedMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = winColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ExplorerThisPcView(
    telemetry: SystemTelemetry,
    installedAppsCount: Int,
    mountedFolders: List<MountedSafFolder>,
    onNavigate: (ExplorerLocation) -> Unit,
    onOpenMounted: (MountedSafFolder) -> Unit,
    onMountNew: () -> Unit,
    onOpenAppManager: () -> Unit,
    onOpenSystemStorage: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val diskProgress = (telemetry.usedStorageGb / telemetry.totalStorageGb).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Devices and drives",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(winColors.cardSurface)
                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                    .clickable { onNavigate(ExplorerLocation.LocalFolder("", "Local Disk (C:)")) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Rounded.Storage, contentDescription = null, tint = winColors.accent, modifier = Modifier.size(34.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Local Disk (C:)", style = MaterialTheme.typography.titleSmall, color = winColors.textPrimary)
                    LinearProgressIndicator(
                        progress = { diskProgress },
                        color = winColors.accent,
                        trackColor = winColors.border,
                        modifier = Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%.1f GB free of %.0f GB  •  %s",
                            (telemetry.totalStorageGb - telemetry.usedStorageGb).coerceAtLeast(0f),
                            telemetry.totalStorageGb,
                            telemetry.deviceModel
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "Connected Device Folders & Tools",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )
        }

        items(mountedFolders, key = { it.treeUriString }) { mounted ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(winColors.cardSurface)
                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                    .clickable { onOpenMounted(mounted) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Win11FolderIcon(size = 26.dp)
                Text(mounted.displayName, style = MaterialTheme.typography.bodyMedium, color = winColors.textPrimary)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onMountNew)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = winColors.accent, modifier = Modifier.size(20.dp))
                    Text("Mount Android Folder", style = MaterialTheme.typography.labelMedium, color = winColors.textPrimary)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenAppManager)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Computer, contentDescription = null, tint = winColors.accent, modifier = Modifier.size(20.dp))
                    Text("Apps ($installedAppsCount)", style = MaterialTheme.typography.labelMedium, color = winColors.textPrimary)
                }
            }
        }

        item {
            Text(
                text = "Open Android System Storage Settings",
                style = MaterialTheme.typography.labelMedium,
                color = winColors.accent,
                modifier = Modifier.clickable(onClick = onOpenSystemStorage).padding(vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExplorerFolderItemsView(
    items: List<ExplorerItem>,
    selectedIds: Set<String>,
    viewMode: ExplorerViewMode,
    isRecycleBin: Boolean,
    onItemClick: (ExplorerItem) -> Unit,
    onItemToggleSelect: (ExplorerItem) -> Unit,
    onMountNew: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isRecycleBin) "Recycle Bin is empty." else "This folder is empty.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = winColors.textSecondary
                )
                if (!isRecycleBin) {
                    Text(
                        text = "Use '+ New' to create folders/files or Mount a device folder",
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.accent,
                        modifier = Modifier.clickable(onClick = onMountNew).padding(4.dp)
                    )
                }
            }
        }
        return
    }

    when (viewMode) {
        ExplorerViewMode.DETAILS -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Details Header Columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(26.dp))
                    Text("Name", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = winColors.textSecondary, modifier = Modifier.weight(1.6f))
                    Text("Date modified", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = winColors.textSecondary, modifier = Modifier.weight(1f))
                    Text("Size", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = winColors.textSecondary, modifier = Modifier.width(68.dp), textAlign = TextAlign.End)
                }
                HorizontalDivider(color = winColors.subtleBorder)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        val isSelected = selectedIds.contains(item.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) winColors.selectionFill else Color.Transparent)
                                .combinedClickable(
                                    onClick = { onItemClick(item) },
                                    onLongClick = { onItemToggleSelect(item) }
                                )
                                .padding(horizontal = 6.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                contentDescription = "Select",
                                tint = if (isSelected) winColors.accent else winColors.textSecondary.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onItemToggleSelect(item) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ExplorerItemIcon(item = item, size = 22.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = winColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.6f)
                            )
                            Text(
                                text = formatExplorerDate(item.lastModifiedMillis),
                                style = MaterialTheme.typography.labelSmall,
                                color = winColors.textSecondary,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatExplorerBytes(item.sizeBytes, item.isDirectory),
                                style = MaterialTheme.typography.labelSmall,
                                color = winColors.textSecondary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(68.dp)
                            )
                        }
                    }
                }
            }
        }
        ExplorerViewMode.LARGE_ICONS, ExplorerViewMode.TILES -> {
            val cellMin = if (viewMode == ExplorerViewMode.LARGE_ICONS) 104.dp else 160.dp
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = cellMin),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    val isSelected = selectedIds.contains(item.id)
                    val shape = RoundedCornerShape(8.dp)
                    if (viewMode == ExplorerViewMode.LARGE_ICONS) {
                        Column(
                            modifier = Modifier
                                .clip(shape)
                                .background(if (isSelected) winColors.selectionFill else Color.Transparent)
                                .then(if (isSelected) Modifier.border(1.dp, winColors.selectionBorder, shape) else Modifier)
                                .combinedClickable(
                                    onClick = { onItemClick(item) },
                                    onLongClick = { onItemToggleSelect(item) }
                                )
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ExplorerItemIcon(item = item, size = 48.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = winColors.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(shape)
                                .background(if (isSelected) winColors.selectionFill else winColors.cardSurface.copy(alpha = 0.6f))
                                .border(1.dp, if (isSelected) winColors.selectionBorder else winColors.subtleBorder, shape)
                                .combinedClickable(
                                    onClick = { onItemClick(item) },
                                    onLongClick = { onItemToggleSelect(item) }
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ExplorerItemIcon(item = item, size = 34.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = winColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatExplorerBytes(item.sizeBytes, item.isDirectory),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = winColors.textSecondary
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
private fun ExplorerDetailsPane(
    location: ExplorerLocation,
    selectedItem: ExplorerItem?,
    totalItems: Int,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    Column(
        modifier = modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (selectedItem == null) {
            Win11FolderIcon(size = 44.dp)
            Text(location.title, style = MaterialTheme.typography.titleSmall, color = winColors.textPrimary)
            Text("$totalItems items", style = MaterialTheme.typography.labelMedium, color = winColors.textSecondary)
            Text(
                "Select a file or folder (long-press or check box) to inspect its properties.",
                style = MaterialTheme.typography.labelSmall,
                color = winColors.textSecondary
            )
        } else {
            ExplorerItemIcon(item = selectedItem, size = 50.dp)
            Text(
                text = selectedItem.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )
            HorizontalDivider(color = winColors.subtleBorder)
            DetailLine("Type", if (selectedItem.isDirectory) "File folder" else selectedItem.kind.name)
            DetailLine("Size", formatExplorerBytes(selectedItem.sizeBytes, selectedItem.isDirectory))
            DetailLine("Modified", formatExplorerDate(selectedItem.lastModifiedMillis))
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    val winColors = LocalWin11Colors.current
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = winColors.textSecondary)
        Text(value, style = MaterialTheme.typography.labelMedium, color = winColors.textPrimary)
    }
}

@Composable
private fun NavIconBtn(
    icon: ImageVector,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) winColors.textPrimary else winColors.textSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun CommandIconBtn(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(34.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) winColors.textPrimary else winColors.textSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CommandLabelBtn(
    icon: ImageVector,
    label: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (highlighted) winColors.selectionFill else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = label, tint = winColors.textPrimary, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = winColors.textPrimary, fontSize = 11.sp)
    }
}
