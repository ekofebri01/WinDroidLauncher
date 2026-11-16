package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.DesktopWindowType
import com.example.model.ExplorerItem
import com.example.model.InstalledApp
import com.example.ui.theme.LocalWin11Colors

@Composable
fun Win11StartMenu(
    userName: String,
    pinnedApps: List<InstalledApp>,
    allApps: List<InstalledApp>,
    recentApps: List<InstalledApp>,
    recommendedFiles: List<ExplorerItem>,
    searchQuery: String,
    showAllApps: Boolean,
    showPowerMenu: Boolean,
    searchFocusedMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleAllApps: (Boolean) -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit,
    onRecommendedFileClick: (ExplorerItem) -> Unit,
    onOpenWindow: (DesktopWindowType) -> Unit,
    onRetract: () -> Unit,
    onTogglePowerMenu: () -> Unit,
    onLockClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onHomeSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }
    val filteredFiles = remember(recommendedFiles, searchQuery) {
        if (searchQuery.isBlank()) recommendedFiles.take(4)
        else recommendedFiles.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                (it.localRelativePath?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    var dragDownAccumulated by remember { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(14.dp)

    Surface(
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .heightIn(min = 390.dp, max = 600.dp)
            .shadow(24.dp, shape)
            .border(1.dp, winColors.border, shape)
            .testTag("win11_start_menu"),
        shape = shape,
        color = winColors.micaSurface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Retractable Handle Bar (swipe down or tap chevron to retract Start Menu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { dragDownAccumulated = 0f },
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount > 0f) {
                                    dragDownAccumulated += dragAmount
                                    if (dragDownAccumulated > 72f) {
                                        dragDownAccumulated = 0f
                                        onRetract()
                                    }
                                }
                            }
                        )
                    }
                    .padding(top = 8.dp, start = 20.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.width(28.dp))
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(winColors.textSecondary.copy(alpha = 0.42f))
                        .clickable(onClick = onRetract)
                        .testTag("start_retract_handle")
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Retract Start Menu",
                    tint = winColors.textSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onRetract)
                        .testTag("start_retract_button")
                )
            }

            // Top Search Input Bar
            Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(winColors.searchFieldBg)
                        .border(1.5.dp, winColors.accent.copy(alpha = 0.65f), RoundedCornerShape(21.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = winColors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search for apps, files, and settings",
                                style = MaterialTheme.typography.bodyMedium,
                                color = winColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = winColors.textPrimary),
                            cursorBrush = SolidColor(winColors.accent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_search_input")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = winColors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Main Start Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                when {
                    searchFocusedMode || searchQuery.isNotBlank() -> {
                        SearchResultsPane(
                            query = searchQuery,
                            appResults = filteredApps,
                            fileResults = filteredFiles,
                            onAppClick = onAppClick,
                            onAppLongPress = onAppLongPress,
                            onFileClick = onRecommendedFileClick,
                            onOpenWindow = onOpenWindow
                        )
                    }
                    showAllApps -> {
                        AllAppsAlphabeticalPane(
                            apps = allApps,
                            onBackClick = { onToggleAllApps(false) },
                            onAppClick = onAppClick,
                            onAppLongPress = onAppLongPress
                        )
                    }
                    else -> {
                        PinnedAndRecommendedPane(
                            pinnedApps = pinnedApps,
                            recentApps = recentApps,
                            recommendedFiles = recommendedFiles,
                            onAllAppsClick = { onToggleAllApps(true) },
                            onAppClick = onAppClick,
                            onAppLongPress = onAppLongPress,
                            onRecommendedFileClick = onRecommendedFileClick,
                            onOpenWindow = onOpenWindow
                        )
                    }
                }
            }

            // Bottom Mica Profile & Power Footer
            HorizontalDivider(thickness = 1.dp, color = winColors.subtleBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(winColors.cardSurface.copy(alpha = 0.45f))
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenWindow(DesktopWindowType.SETTINGS_PERSONALIZE) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("start_user_profile"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(winColors.accent.copy(alpha = 0.2f))
                            .border(1.dp, winColors.accent.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "User Avatar",
                            tint = winColors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleSmall,
                        color = winColors.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onOpenWindow(DesktopWindowType.SETTINGS_PERSONALIZE) },
                        modifier = Modifier.testTag("start_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Launcher Settings",
                            tint = winColors.textPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = onTogglePowerMenu,
                            modifier = Modifier.testTag("start_power_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PowerSettingsNew,
                                contentDescription = "Power menu",
                                tint = winColors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showPowerMenu,
                            onDismissRequest = onTogglePowerMenu,
                            containerColor = winColors.micaSurface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Lock screen", color = winColors.textPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = winColors.textPrimary)
                                },
                                onClick = onLockClick
                            )
                            DropdownMenuItem(
                                text = { Text("Refresh apps", color = winColors.textPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = winColors.textPrimary)
                                },
                                onClick = onRefreshClick
                            )
                            DropdownMenuItem(
                                text = { Text("Default Home App", color = winColors.textPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Apps, contentDescription = null, tint = winColors.textPrimary)
                                },
                                onClick = onHomeSettingsClick
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class BuiltInStartPin(
    val title: String,
    val windowType: DesktopWindowType
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedAndRecommendedPane(
    pinnedApps: List<InstalledApp>,
    recentApps: List<InstalledApp>,
    recommendedFiles: List<ExplorerItem>,
    onAllAppsClick: () -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit,
    onRecommendedFileClick: (ExplorerItem) -> Unit,
    onOpenWindow: (DesktopWindowType) -> Unit
) {
    val winColors = LocalWin11Colors.current
    val builtInPins = remember {
        listOf(
            BuiltInStartPin("File Explorer", DesktopWindowType.FILE_EXPLORER),
            BuiltInStartPin("This PC", DesktopWindowType.THIS_PC),
            BuiltInStartPin("Settings", DesktopWindowType.SETTINGS_PERSONALIZE),
            BuiltInStartPin("Apps", DesktopWindowType.RECYCLE_BIN_APPS)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Pinned Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pinned",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(winColors.cardSurface)
                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(6.dp))
                    .clickable(onClick = onAllAppsClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("start_all_apps_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "All apps",
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.textPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = winColors.textPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Pinned Apps Grid (Windows 11 system tools + user pinned apps)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("start_pinned_apps_area")
        ) {
            val cols = if (maxWidth >= 440.dp) 5 else 4
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(builtInPins, key = { "builtin_${it.windowType.name}" }) { pin ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenWindow(pin.windowType) }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("start_pin_${pin.windowType.name}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WindowTypeIconView(type = pin.windowType, size = 34.dp)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = pin.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = winColors.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                items(pinnedApps, key = { it.packageName }) { app ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongPress(app) }
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppIconImage(app = app, size = 34.dp)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = winColors.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Recommended Files & Recent Section
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recommended",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(winColors.cardSurface)
                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(6.dp))
                    .clickable { onOpenWindow(DesktopWindowType.FILE_EXPLORER) }
                    .padding(horizontal = 9.dp, vertical = 4.dp)
                    .testTag("start_recommended_more_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelSmall,
                    color = winColors.textPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = winColors.textPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        val filesToShow = recommendedFiles.take(4)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(bottom = 10.dp)
                .testTag("start_recommended_files_area")
        ) {
            if (filesToShow.isNotEmpty()) {
                filesToShow.chunked(2).forEach { rowFiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowFiles.forEach { file ->
                            val folderLabel = file.localRelativePath
                                ?.substringBeforeLast('/', "")
                                ?.ifBlank { "Local Disk (C:)" }
                                ?: "File"
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(winColors.cardSurface.copy(alpha = 0.5f))
                                    .clickable { onRecommendedFileClick(file) }
                                    .padding(8.dp)
                                    .testTag("start_recommended_file_${file.name}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                ExplorerItemIcon(item = file, size = 28.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                        color = winColors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$folderLabel • ${formatExplorerDate(file.lastModifiedMillis)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = winColors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (rowFiles.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                val itemsToShow = recentApps.take(2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsToShow.forEach { app ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAppClick(app) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppIconImage(app = app, size = 28.dp)
                            Column {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = winColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Recently opened",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = winColors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (itemsToShow.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenWindow(DesktopWindowType.FILE_EXPLORER) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Win11FolderIcon(size = 28.dp)
                            Column {
                                Text(
                                    text = "Open File Explorer",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = winColors.textPrimary
                                )
                                Text(
                                    text = "Browse recent files & folders",
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AllAppsAlphabeticalPane(
    apps: List<InstalledApp>,
    onBackClick: () -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit
) {
    val winColors = LocalWin11Colors.current
    val grouped = remember(apps) {
        apps.groupBy { app ->
            val ch = app.label.firstOrNull()?.uppercaseChar() ?: '#'
            if (ch.isLetter()) ch.toString() else "#"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All apps (${apps.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(winColors.cardSurface)
                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(6.dp))
                    .clickable(onClick = onBackClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = winColors.textPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.textPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            grouped.forEach { (letter, groupApps) ->
                item(key = "header_$letter") {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = winColors.accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
                items(groupApps, key = { "${it.packageName}/${it.activityName}" }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongPress(app) }
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppIconImage(app = app, size = 30.dp)
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = winColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchResultsPane(
    query: String,
    appResults: List<InstalledApp>,
    fileResults: List<ExplorerItem>,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongPress: (InstalledApp) -> Unit,
    onFileClick: (ExplorerItem) -> Unit,
    onOpenWindow: (DesktopWindowType) -> Unit
) {
    val winColors = LocalWin11Colors.current
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (query.isBlank()) {
                "Quick tools, files & apps"
            } else {
                "Best match (${appResults.size + fileResults.size})"
            },
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = winColors.textPrimary,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (query.isBlank() || "file explorer".contains(query, true) || "files".contains(query, true) || "folder".contains(query, true)) {
                item(key = "tool_file_explorer") {
                    SearchSystemRow(
                        title = "File Explorer",
                        subtitle = "Browse folders, drives, and device storage",
                        icon = { Win11FolderIcon(size = 28.dp) },
                        onClick = { onOpenWindow(DesktopWindowType.FILE_EXPLORER) }
                    )
                }
            }
            if (query.isBlank() || "this pc".contains(query, true) || "storage".contains(query, true)) {
                item(key = "tool_this_pc") {
                    SearchSystemRow(
                        title = "This PC",
                        subtitle = "System specs, RAM & internal disk",
                        icon = { Win11ThisPcIcon(size = 28.dp) },
                        onClick = { onOpenWindow(DesktopWindowType.THIS_PC) }
                    )
                }
            }
            if (query.isBlank() || "personalize".contains(query, true) || "wallpaper".contains(query, true) || "settings".contains(query, true)) {
                item(key = "tool_personalize") {
                    SearchSystemRow(
                        title = "Personalize & Settings",
                        subtitle = "Wallpapers, theme, taskbar alignment",
                        icon = { Win11SettingsIcon(size = 28.dp) },
                        onClick = { onOpenWindow(DesktopWindowType.SETTINGS_PERSONALIZE) }
                    )
                }
            }

            if (fileResults.isNotEmpty()) {
                item(key = "files_header") {
                    Text(
                        text = "Documents & Files",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textSecondary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                items(fileResults, key = { "file_${it.id}" }) { file ->
                    SearchSystemRow(
                        title = file.name,
                        subtitle = file.localRelativePath ?: "Recommended file",
                        icon = { ExplorerItemIcon(item = file, size = 28.dp) },
                        onClick = { onFileClick(file) }
                    )
                }
            }

            if (appResults.isEmpty() && fileResults.isEmpty()) {
                item(key = "empty_search") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No apps or files found matching \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = winColors.textSecondary
                        )
                    }
                }
            } else {
                items(appResults, key = { "${it.packageName}/${it.activityName}" }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongPress(app) }
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppIconImage(app = app, size = 32.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = winColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "App",
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

@Composable
private fun SearchSystemRow(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(winColors.cardSurface.copy(alpha = 0.65f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        icon()
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = winColors.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = winColors.textSecondary
            )
        }
    }
}
