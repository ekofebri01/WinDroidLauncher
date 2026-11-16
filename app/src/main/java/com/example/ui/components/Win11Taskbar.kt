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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivePanel
import com.example.model.DesktopWindowState
import com.example.model.DesktopWindowType
import com.example.model.InstalledApp
import com.example.model.SystemTelemetry
import com.example.model.TaskbarAlignment
import com.example.ui.theme.LocalWin11Colors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Win11Taskbar(
    activePanel: ActivePanel,
    taskbarAlignment: TaskbarAlignment,
    pinnedApps: List<InstalledApp>,
    openWindows: List<DesktopWindowState>,
    activeWindowType: DesktopWindowType?,
    telemetry: SystemTelemetry,
    currentTimeMillis: Long,
    use24HourClock: Boolean,
    showSecondsInClock: Boolean,
    onStartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTaskViewClick: () -> Unit,
    onFileExplorerClick: () -> Unit,
    onWidgetsClick: () -> Unit,
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
        use24HourClock && showSecondsInClock -> "HH:mm:ss"
        use24HourClock -> "HH:mm"
        showSecondsInClock -> "hh:mm:ss a"
        else -> "hh:mm a"
    }
    val timeText = SimpleDateFormat(timePattern, Locale.getDefault()).format(Date(currentTimeMillis))
    val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(currentTimeMillis))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(winColors.acrylicTaskbar)
            .navigationBarsPadding()
            .testTag("win11_taskbar")
    ) {
        HorizontalDivider(thickness = 1.dp, color = winColors.border)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 6.dp)
        ) {
            val isCompact = maxWidth < 460.dp
            val isExpanded = maxWidth >= 660.dp
            val maxPinnedVisible = if (isCompact) 3 else if (isExpanded) 6 else 4
            val visiblePinned = pinnedApps.take(maxPinnedVisible)

            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Widgets Pill
                TaskbarActionTile(
                    isActive = activePanel == ActivePanel.WIDGETS,
                    onClick = onWidgetsClick,
                    testTag = "taskbar_widgets_button",
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = if (isExpanded) 8.dp else 4.dp)
                    ) {
                        Win11WidgetsIcon(size = 20.dp)
                        if (isExpanded) {
                            Column {
                                Text(
                                    text = "${telemetry.batteryPercent}% • Ready",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = winColors.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = telemetry.networkLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = winColors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Center (or Left-aligned) Taskbar Dock
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = if (taskbarAlignment == TaskbarAlignment.CENTER) {
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
                            hasOpenIndicator = activePanel == ActivePanel.START_MENU,
                            isFocusedIndicator = activePanel == ActivePanel.START_MENU,
                            onClick = onStartClick,
                            testTag = "taskbar_start_button"
                        ) {
                            Win11StartLogo(size = 22.dp)
                        }

                        // Search Pill or Icon
                        if (isExpanded) {
                            Row(
                                modifier = Modifier
                                    .height(38.dp)
                                    .width(150.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(winColors.cardSurface.copy(alpha = 0.8f))
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
                                    modifier = Modifier.size(17.dp)
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
                                hasOpenIndicator = activePanel == ActivePanel.SEARCH,
                                isFocusedIndicator = activePanel == ActivePanel.SEARCH,
                                onClick = onSearchClick,
                                testTag = "taskbar_search_button"
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search apps",
                                    tint = winColors.textPrimary,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        // Task View Button
                        TaskbarActionTile(
                            isActive = activePanel == ActivePanel.TASK_VIEW,
                            hasOpenIndicator = activePanel == ActivePanel.TASK_VIEW,
                            isFocusedIndicator = activePanel == ActivePanel.TASK_VIEW,
                            onClick = onTaskViewClick,
                            testTag = "taskbar_task_view_button"
                        ) {
                            Win11TaskViewIcon(size = 20.dp, tint = winColors.textPrimary)
                        }

                        // Pinned File Explorer (Windows 11 Laptop Signature)
                        val explorerWindow = openWindows.firstOrNull { it.type == DesktopWindowType.FILE_EXPLORER }
                        val explorerFocused = activeWindowType == DesktopWindowType.FILE_EXPLORER &&
                            explorerWindow?.isMinimized == false
                        TaskbarActionTile(
                            isActive = explorerFocused,
                            hasOpenIndicator = explorerWindow != null,
                            isFocusedIndicator = explorerFocused,
                            onClick = onFileExplorerClick,
                            testTag = "taskbar_file_explorer_button"
                        ) {
                            Win11FolderIcon(size = 24.dp)
                        }

                        // Other Open Built-in Windows
                        openWindows.filterNot { it.type == DesktopWindowType.FILE_EXPLORER }.forEach { winState ->
                            val isFocused = activeWindowType == winState.type && !winState.isMinimized
                            TaskbarActionTile(
                                isActive = isFocused,
                                hasOpenIndicator = true,
                                isFocusedIndicator = isFocused,
                                onClick = { onWindowTaskbarClick(winState.type) },
                                testTag = "taskbar_window_${winState.type.name}"
                            ) {
                                WindowTypeIconView(type = winState.type, size = 22.dp)
                            }
                        }

                        // Pinned Real Android Apps
                        visiblePinned.forEach { app ->
                            Box(
                                modifier = Modifier
                                    .size(width = 42.dp, height = 44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { onPinnedAppClick(app) },
                                        onLongClick = { onPinnedAppLongPress(app) }
                                    )
                                    .testTag("taskbar_app_${app.packageName}"),
                                contentAlignment = Alignment.Center
                            ) {
                                AppIconImage(app = app, size = 26.dp)
                            }
                        }
                    }
                }

                // Right: Authentic Windows 11 System Tray
                Row(
                    modifier = Modifier.testTag("win11_system_tray"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (!isCompact) {
                        Box(
                            modifier = Modifier
                                .size(width = 28.dp, height = 46.dp)
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
                    Row(
                        modifier = Modifier
                            .height(46.dp)
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
                        if (!isCompact) {
                            Icon(
                                imageVector = if (telemetry.isMuted) Icons.AutoMirrored.Rounded.VolumeOff
                                else Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Volume",
                                tint = winColors.textPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
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

                    // Clock & Date Stack
                    Row(
                        modifier = Modifier
                            .height(42.dp)
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
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = winColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(winColors.accent)
                        )
                    }

                    // Show Desktop Sliver
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .clickable(onClick = onShowDesktopClick)
                            .testTag("tray_show_desktop_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(winColors.border)
                        )
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
            .height(44.dp)
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
