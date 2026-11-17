package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Battery1Bar
import androidx.compose.material.icons.rounded.Battery3Bar
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivePanel
import com.example.model.DesktopWindowState
import com.example.model.DesktopWindowType
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.model.SystemTelemetry
import com.example.model.TaskbarAlignment
import com.example.model.TaskbarSearchMode
import com.example.model.TaskbarStyle
import com.example.ui.theme.LocalWin11Colors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Win11Taskbar(
    activePanel: ActivePanel,
    preferences: LauncherPreferences,
    pinnedApps: List<InstalledApp>,
    openWindows: List<DesktopWindowState>,
    activeWindowType: DesktopWindowType?,
    telemetry: SystemTelemetry,
    currentTimeMillis: Long,
    onStartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTaskViewClick: () -> Unit,
    onFileExplorerClick: () -> Unit,
    onHiddenTrayClick: () -> Unit,
    onQuickSettingsClick: () -> Unit,
    onClockClick: () -> Unit,
    onShowDesktopClick: () -> Unit,
    onPinnedAppClick: (InstalledApp) -> Unit,
    onPinnedAppLongPress: (InstalledApp) -> Unit,
    onWindowTaskbarClick: (DesktopWindowType) -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val timePattern = when {
        preferences.use24HourClock && preferences.showSecondsInClock -> "HH:mm:ss"
        preferences.use24HourClock -> "HH:mm"
        preferences.showSecondsInClock -> "hh:mm:ss a"
        else -> "hh:mm a"
    }
    val timeText = SimpleDateFormat(timePattern, Locale.getDefault()).format(Date(currentTimeMillis))
    val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(currentTimeMillis))

    val isFloating = preferences.taskbarStyle == TaskbarStyle.FLOATING
    val barHeightDp = preferences.taskbarHeightDp.coerceIn(44, 72)
    val tileHeight = (barHeightDp - 8).coerceIn(36, 62).dp
    val sysIconSize = preferences.taskbarIconSize.iconDp.dp
    val appIconSize = preferences.taskbarIconSize.appIconDp.dp
    val showIndicator = preferences.taskbarShowActiveIndicator
    val barBg = winColors.acrylicTaskbar.copy(alpha = preferences.taskbarOpacity.coerceIn(0.45f, 1.0f))

    val outerModifier = if (isFloating) {
        val floatingShape = RoundedCornerShape(16.dp)
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(floatingShape)
            .background(barBg)
            .border(1.dp, winColors.border, floatingShape)
            .testTag("win11_taskbar")
    } else {
        modifier
            .fillMaxWidth()
            .background(barBg)
            .navigationBarsPadding()
            .testTag("win11_taskbar")
    }

    Column(modifier = outerModifier) {
        if (!isFloating && preferences.taskbarShowDivider) {
            HorizontalDivider(thickness = 1.dp, color = winColors.border)
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeightDp.dp)
                .padding(horizontal = 8.dp)
        ) {
            val isCompact = maxWidth < 460.dp
            val isExpanded = maxWidth >= 660.dp
            val maxPinnedVisible = if (isCompact) 3 else if (isExpanded) 6 else 4
            val visiblePinned = if (preferences.showPinnedAppsInTaskbar) {
                pinnedApps.take(maxPinnedVisible)
            } else {
                emptyList()
            }

            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Taskbar Dock (Center or Left-aligned; Widget button removed)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = if (preferences.taskbarAlignment == TaskbarAlignment.CENTER) {
                        Alignment.Center
                    } else {
                        Alignment.CenterStart
                    }
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Start Button
                        TaskbarActionTile(
                            isActive = activePanel == ActivePanel.START_MENU,
                            hasOpenIndicator = showIndicator && activePanel == ActivePanel.START_MENU,
                            isFocusedIndicator = activePanel == ActivePanel.START_MENU,
                            tileHeight = tileHeight,
                            onClick = onStartClick,
                            testTag = "taskbar_start_button"
                        ) {
                            Win11StartLogo(size = sysIconSize)
                        }

                        // Search Pill / Icon (Configurable)
                        val renderSearchBox = when (preferences.taskbarSearchMode) {
                            TaskbarSearchMode.SEARCH_BOX -> true
                            TaskbarSearchMode.AUTO -> isExpanded
                            TaskbarSearchMode.ICON_ONLY, TaskbarSearchMode.HIDDEN -> false
                        }
                        if (preferences.taskbarSearchMode != TaskbarSearchMode.HIDDEN) {
                            if (renderSearchBox) {
                                val searchHeight = (barHeightDp - 14).coerceIn(30, 44).dp
                                Row(
                                    modifier = Modifier
                                        .height(searchHeight)
                                        .width(if (isCompact) 110.dp else 150.dp)
                                        .clip(RoundedCornerShape(19.dp))
                                        .background(winColors.cardSurface.copy(alpha = 0.85f))
                                        .border(1.dp, winColors.border, RoundedCornerShape(19.dp))
                                        .clickable(onClick = onSearchClick)
                                        .padding(horizontal = 12.dp)
                                        .testTag("taskbar_search_button"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search",
                                        tint = winColors.textSecondary,
                                        modifier = Modifier.size((sysIconSize.value - 4f).coerceAtLeast(14f).dp)
                                    )
                                    Text(
                                        text = "Search",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = winColors.textSecondary,
                                        maxLines = 1
                                    )
                                }
                            } else {
                                TaskbarActionTile(
                                    isActive = activePanel == ActivePanel.SEARCH,
                                    hasOpenIndicator = showIndicator && activePanel == ActivePanel.SEARCH,
                                    isFocusedIndicator = activePanel == ActivePanel.SEARCH,
                                    tileHeight = tileHeight,
                                    onClick = onSearchClick,
                                    testTag = "taskbar_search_button"
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search apps",
                                        tint = winColors.textPrimary,
                                        modifier = Modifier.size(sysIconSize)
                                    )
                                }
                            }
                        }

                        // Task View Button
                        if (preferences.showTaskViewInTaskbar) {
                            TaskbarActionTile(
                                isActive = activePanel == ActivePanel.TASK_VIEW,
                                hasOpenIndicator = showIndicator && activePanel == ActivePanel.TASK_VIEW,
                                isFocusedIndicator = activePanel == ActivePanel.TASK_VIEW,
                                tileHeight = tileHeight,
                                onClick = onTaskViewClick,
                                testTag = "taskbar_task_view_button"
                            ) {
                                Win11TaskViewIcon(
                                    size = (sysIconSize.value - 1f).coerceAtLeast(16f).dp,
                                    tint = winColors.textPrimary
                                )
                            }
                        }

                        // Pinned File Explorer
                        val explorerWindow = openWindows.firstOrNull { it.type == DesktopWindowType.FILE_EXPLORER }
                        val explorerFocused = activeWindowType == DesktopWindowType.FILE_EXPLORER &&
                            explorerWindow?.isMinimized == false
                        if (preferences.showFileExplorerInTaskbar) {
                            TaskbarActionTile(
                                isActive = explorerFocused,
                                hasOpenIndicator = showIndicator && explorerWindow != null,
                                isFocusedIndicator = explorerFocused,
                                tileHeight = tileHeight,
                                onClick = onFileExplorerClick,
                                testTag = "taskbar_file_explorer_button"
                            ) {
                                Win11FolderIcon(size = (sysIconSize.value + 2f).coerceAtLeast(18f).dp)
                            }
                        }

                        // Open Built-in Windows (plus File Explorer if open while unpinned)
                        openWindows.filter { win ->
                            win.type != DesktopWindowType.FILE_EXPLORER || !preferences.showFileExplorerInTaskbar
                        }.forEach { winState ->
                            val isFocused = activeWindowType == winState.type && !winState.isMinimized
                            TaskbarActionTile(
                                isActive = isFocused,
                                hasOpenIndicator = showIndicator,
                                isFocusedIndicator = isFocused,
                                tileHeight = tileHeight,
                                onClick = { onWindowTaskbarClick(winState.type) },
                                testTag = "taskbar_window_${winState.type.name}"
                            ) {
                                WindowTypeIconView(type = winState.type, size = sysIconSize)
                            }
                        }

                        // Pinned Real Android Apps
                        visiblePinned.forEach { app ->
                            Box(
                                modifier = Modifier
                                    .size(width = (appIconSize.value + 16f).dp, height = tileHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { onPinnedAppClick(app) },
                                        onLongClick = { onPinnedAppLongPress(app) }
                                    )
                                    .testTag("taskbar_app_${app.packageName}"),
                                contentAlignment = Alignment.Center
                            ) {
                                AppIconImage(app = app, size = appIconSize)
                            }
                        }
                    }
                }

                // Right: Customizable Windows 11 System Tray
                val hasQuickTrayItems = preferences.showTrayNetwork ||
                    preferences.showTrayVolume ||
                    preferences.showTrayBattery
                val hasAnyTrayContent = preferences.showHiddenTrayInTaskbar ||
                    hasQuickTrayItems ||
                    preferences.showTrayClock ||
                    preferences.showDesktopButtonInTaskbar

                if (hasAnyTrayContent) {
                    Row(
                        modifier = Modifier.testTag("win11_system_tray"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (preferences.showHiddenTrayInTaskbar && !isCompact) {
                            Box(
                                modifier = Modifier
                                    .size(width = 28.dp, height = tileHeight)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (activePanel == ActivePanel.HIDDEN_TRAY) winColors.cardHover
                                        else Color.Transparent
                                    )
                                    .clickable(onClick = onHiddenTrayClick)
                                    .testTag("tray_hidden_icons_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowUp,
                                    contentDescription = "Show hidden icons",
                                    tint = winColors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Quick Settings Pill: Wi-Fi + Volume + Battery
                        if (hasQuickTrayItems) {
                            Row(
                                modifier = Modifier
                                    .height(tileHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (activePanel == ActivePanel.QUICK_SETTINGS) winColors.cardHover
                                        else Color.Transparent
                                    )
                                    .clickable(onClick = onQuickSettingsClick)
                                    .padding(horizontal = 6.dp)
                                    .testTag("tray_quick_settings_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (preferences.showTrayNetwork) {
                                    val netIcon = when {
                                        telemetry.isWifiConnected -> Icons.Rounded.Wifi
                                        telemetry.isCellularConnected -> Icons.Rounded.SignalCellularAlt
                                        else -> Icons.Rounded.WifiOff
                                    }
                                    Icon(
                                        imageVector = netIcon,
                                        contentDescription = telemetry.networkLabel,
                                        tint = winColors.textPrimary,
                                        modifier = Modifier
                                            .size(15.dp)
                                            .testTag("tray_wifi_indicator")
                                    )
                                }
                                if (preferences.showTrayVolume && !isCompact) {
                                    Icon(
                                        imageVector = if (telemetry.isMuted) Icons.AutoMirrored.Rounded.VolumeOff
                                        else Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = winColors.textPrimary,
                                        modifier = Modifier
                                            .size(15.dp)
                                            .testTag("tray_volume_indicator")
                                    )
                                }
                                if (preferences.showTrayBattery) {
                                    val batteryIcon = when {
                                        telemetry.isCharging -> Icons.Rounded.BatteryChargingFull
                                        telemetry.batteryPercent <= 15 -> Icons.Rounded.BatteryAlert
                                        telemetry.batteryPercent <= 35 -> Icons.Rounded.Battery1Bar
                                        telemetry.batteryPercent <= 65 -> Icons.Rounded.Battery3Bar
                                        telemetry.batteryPercent <= 88 -> Icons.Rounded.Battery5Bar
                                        else -> Icons.Rounded.BatteryFull
                                    }
                                    Row(
                                        modifier = Modifier.testTag("tray_battery_status"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = batteryIcon,
                                            contentDescription = "Battery ${telemetry.batteryPercent}%",
                                            tint = if (telemetry.batteryPercent <= 15 && !telemetry.isCharging) {
                                                Color(0xFFFF6B6B)
                                            } else {
                                                winColors.textPrimary
                                            },
                                            modifier = Modifier.size(15.dp)
                                        )
                                        if (preferences.showTrayBatteryPercent) {
                                            Text(
                                                text = "${telemetry.batteryPercent}%",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = winColors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Clock & Date Stack
                        if (preferences.showTrayClock) {
                            Row(
                                modifier = Modifier
                                    .height((barHeightDp - 10).coerceIn(34, 58).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (activePanel == ActivePanel.CALENDAR_NOTIFICATIONS) winColors.cardHover
                                        else Color.Transparent
                                    )
                                    .clickable(onClick = onClockClick)
                                    .padding(horizontal = 7.dp)
                                    .testTag("tray_clock_date_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = timeText,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = winColors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip
                                    )
                                    if (preferences.showTrayDate) {
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = winColors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip
                                        )
                                    }
                                }
                                if (preferences.showTrayNotificationDot) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(winColors.accent)
                                    )
                                }
                            }
                        }

                        // Show Desktop Sliver
                        if (preferences.showDesktopButtonInTaskbar) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height((barHeightDp - 18).coerceIn(24, 42).dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .clickable(onClick = onShowDesktopClick)
                                    .testTag("tray_show_desktop_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height((barHeightDp - 28).coerceIn(14, 30).dp)
                                        .background(winColors.border)
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
private fun TaskbarActionTile(
    isActive: Boolean,
    hasOpenIndicator: Boolean = false,
    isFocusedIndicator: Boolean = false,
    tileHeight: Dp = 44.dp,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val bg by animateColorAsState(
        targetValue = if (isActive) winColors.cardHover.copy(alpha = 0.9f) else Color.Transparent,
        label = "taskbar_tile_bg"
    )
    Box(
        modifier = modifier
            .height(tileHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
        if (hasOpenIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .width(if (isFocusedIndicator) 16.dp else 6.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(if (isFocusedIndicator) winColors.pillActive else winColors.pillInactive)
            )
        }
    }
}
