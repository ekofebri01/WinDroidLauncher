package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.ContextMenuRequest
import com.example.model.DesktopIconSize
import com.example.model.DesktopShortcut
import com.example.model.DesktopWindowState
import com.example.model.GitHubUpdateState
import com.example.model.GitHubUpdateStatus
import com.example.model.DesktopWindowType
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.model.ShortcutKind
import com.example.model.SystemTelemetry
import com.example.ui.theme.LocalWin11Colors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun Win11QuickSettingsFlyout(
    telemetry: SystemTelemetry,
    isDarkTheme: Boolean,
    onWifiClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onToggleMute: () -> Unit,
    onHomeSettingsClick: () -> Unit,
    onDisplaySettingsClick: () -> Unit,
    onOpenPersonalize: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(14.dp)

    Surface(
        modifier = modifier
            .widthIn(max = 350.dp)
            .fillMaxWidth()
            .shadow(22.dp, shape)
            .border(1.dp, winColors.border, shape)
            .testTag("win11_quick_settings"),
        shape = shape,
        color = winColors.micaSurface
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 2x3 Quick Toggle Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickSettingTile(
                    icon = if (telemetry.isWifiConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                    label = if (telemetry.isWifiConnected) "Wi-Fi" else "Network",
                    isActive = telemetry.isWifiConnected,
                    onClick = onWifiClick,
                    modifier = Modifier.weight(1f)
                )
                QuickSettingTile(
                    icon = Icons.Rounded.Bluetooth,
                    label = "Bluetooth",
                    isActive = false,
                    onClick = onBluetoothClick,
                    modifier = Modifier.weight(1f)
                )
                QuickSettingTile(
                    icon = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    label = if (isDarkTheme) "Dark" else "Light",
                    isActive = isDarkTheme,
                    onClick = onToggleTheme,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickSettingTile(
                    icon = Icons.Rounded.FlashlightOn,
                    label = "Flashlight",
                    isActive = telemetry.isFlashlightOn,
                    onClick = onToggleFlashlight,
                    modifier = Modifier.weight(1f)
                )
                QuickSettingTile(
                    icon = if (telemetry.isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                    label = if (telemetry.isMuted) "Muted" else "Sound",
                    isActive = !telemetry.isMuted,
                    onClick = onToggleMute,
                    modifier = Modifier.weight(1f)
                )
                QuickSettingTile(
                    icon = Icons.Rounded.Home,
                    label = "Default Home",
                    isActive = false,
                    onClick = onHomeSettingsClick,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(thickness = 1.dp, color = winColors.subtleBorder)

            // Brightness shortcut row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDisplaySettingsClick)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Brightness6,
                    contentDescription = "Display Brightness",
                    tint = winColors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Display & Brightness Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = winColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = winColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Real Media Volume Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onToggleMute, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (telemetry.isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Toggle Mute",
                        tint = winColors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Slider(
                    value = telemetry.mediaVolume.toFloat(),
                    onValueChange = { onVolumeChange(it.toInt()) },
                    valueRange = 0f..telemetry.maxMediaVolume.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = winColors.accent,
                        activeTrackColor = winColors.accent,
                        inactiveTrackColor = winColors.border
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(telemetry.mediaVolume * 100 / telemetry.maxMediaVolume)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = winColors.textSecondary,
                    modifier = Modifier.width(34.dp),
                    textAlign = TextAlign.End
                )
            }

            // Footer: Battery & Settings
            HorizontalDivider(thickness = 1.dp, color = winColors.subtleBorder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (telemetry.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                        contentDescription = null,
                        tint = winColors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${telemetry.batteryPercent}%${if (telemetry.isCharging) " • Charging" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = winColors.textPrimary
                    )
                }
                IconButton(onClick = onOpenPersonalize, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Personalize",
                        tint = winColors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickSettingTile(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) winColors.accentSoft else winColors.cardSurface)
                .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else winColors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = winColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun Win11CalendarFlyout(
    currentTimeMillis: Long,
    telemetry: SystemTelemetry,
    githubUpdateState: GitHubUpdateState = GitHubUpdateState(),
    onOpenThisPc: () -> Unit,
    onOpenUpdateSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(14.dp)
    var monthOffset by remember { mutableIntStateOf(0) }

    val todayCal = remember(currentTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    }
    val displayCal = remember(monthOffset, todayCal.get(Calendar.MONTH), todayCal.get(Calendar.YEAR)) {
        Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }

    val headerDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(currentTimeMillis))
    val monthYearLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayCal.time)

    val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = displayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    val isCurrentMonth = displayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        displayCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    val cells = remember(monthOffset, daysInMonth, firstDayOfWeek) {
        List(firstDayOfWeek) { null } + (1..daysInMonth).toList()
    }

    Surface(
        modifier = modifier
            .widthIn(max = 350.dp)
            .fillMaxWidth()
            .shadow(22.dp, shape)
            .border(1.dp, winColors.border, shape)
            .testTag("win11_calendar_flyout"),
        shape = shape,
        color = winColors.micaSurface
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (githubUpdateState.status == GitHubUpdateStatus.UPDATE_AVAILABLE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.16f))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenUpdateSettings)
                        .padding(12.dp)
                        .testTag("flyout_github_update_notice"),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "GitHub Update Available • ${githubUpdateState.latestTag ?: ""}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF4CD4A9)
                    )
                    Text(
                        text = "Tap to view release notes & download",
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textPrimary
                    )
                }
            }

            // System Notification Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(winColors.cardSurface)
                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenThisPc)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "System Status",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.accent
                    )
                    Text(
                        text = telemetry.androidVersion,
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }
                Text(
                    text = "${telemetry.deviceModel} • RAM ${telemetry.availableRamMb} MB free",
                    style = MaterialTheme.typography.bodyMedium,
                    color = winColors.textPrimary
                )
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "Disk C: %.1f GB used of %.0f GB",
                        telemetry.usedStorageGb,
                        telemetry.totalStorageGb
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = winColors.textSecondary
                )
            }

            HorizontalDivider(thickness = 1.dp, color = winColors.subtleBorder)

            // Today full date label
            Text(
                text = headerDate,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )

            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthYearLabel,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = winColors.textPrimary
                )
                Row {
                    IconButton(onClick = { monthOffset-- }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                            tint = winColors.textPrimary
                        )
                    }
                    IconButton(onClick = { monthOffset++ }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Next month",
                            tint = winColors.textPrimary
                        )
                    }
                }
            }

            // Weekday labels
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Calendar Grid
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (i in 0 until 7) {
                            val day = week.getOrNull(i)
                            val isToday = isCurrentMonth && day == todayDay
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isToday) winColors.accentSoft else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day != null) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isToday) Color.White else winColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Win11WidgetsFlyout(
    telemetry: SystemTelemetry,
    currentTimeMillis: Long,
    recentApps: List<InstalledApp>,
    onAppClick: (InstalledApp) -> Unit,
    onOpenThisPc: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(14.dp)
    val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTimeMillis))
    val dateText = SimpleDateFormat("EEEE, d MMM", Locale.getDefault()).format(Date(currentTimeMillis))
    val storageProgress = (telemetry.usedStorageGb / telemetry.totalStorageGb).coerceIn(0f, 1f)
    val ramUsedMb = (telemetry.totalRamMb - telemetry.availableRamMb).coerceAtLeast(0L)
    val ramProgress = (ramUsedMb.toFloat() / telemetry.totalRamMb.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .widthIn(max = 380.dp)
            .fillMaxWidth()
            .shadow(22.dp, shape)
            .border(1.dp, winColors.border, shape)
            .testTag("win11_widgets_flyout"),
        shape = shape,
        color = winColors.micaSurface
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Widgets",
                    style = MaterialTheme.typography.titleMedium,
                    color = winColors.textPrimary
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.textSecondary
                )
            }

            // Clock & Network Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(winColors.cardSurface)
                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = winColors.textPrimary
                    )
                    Text(
                        text = "${telemetry.networkLabel} • ${telemetry.batteryPercent}% Battery",
                        style = MaterialTheme.typography.bodyMedium,
                        color = winColors.textSecondary
                    )
                }
                Win11WidgetsIcon(size = 34.dp)
            }

            // Device Performance Widget
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(winColors.cardSurface)
                    .border(1.dp, winColors.subtleBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenThisPc)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Storage, contentDescription = null, tint = winColors.accent, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Device Health & Storage",
                        style = MaterialTheme.typography.titleSmall,
                        color = winColors.textPrimary
                    )
                }
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "Internal Disk: %.1f / %.0f GB",
                        telemetry.usedStorageGb,
                        telemetry.totalStorageGb
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.textSecondary
                )
                LinearProgressIndicator(
                    progress = { storageProgress },
                    color = winColors.accent,
                    trackColor = winColors.border,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                )
                Text(
                    text = "Memory (RAM): $ramUsedMb / ${telemetry.totalRamMb} MB",
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.textSecondary
                )
                LinearProgressIndicator(
                    progress = { ramProgress },
                    color = Color(0xFF4CD4A9),
                    trackColor = winColors.border,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                )
            }

            // Quick App Launch Widget
            if (recentApps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Quick Launch",
                        style = MaterialTheme.typography.titleSmall,
                        color = winColors.textPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        recentApps.take(4).forEach { app ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onAppClick(app) }
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AppIconImage(app = app, size = 32.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.labelSmall,
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
    }
}

@Composable
fun Win11TaskViewFlyout(
    openWindows: List<DesktopWindowState>,
    recentApps: List<InstalledApp>,
    onFocusWindow: (DesktopWindowType) -> Unit,
    onCloseWindow: (DesktopWindowType) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(14.dp)

    Surface(
        modifier = modifier
            .widthIn(max = 540.dp)
            .fillMaxWidth()
            .heightIn(max = 460.dp)
            .shadow(22.dp, shape)
            .border(1.dp, winColors.border, shape)
            .testTag("win11_task_view"),
        shape = shape,
        color = winColors.micaSurface
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Task View",
                style = MaterialTheme.typography.titleMedium,
                color = winColors.textPrimary
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (openWindows.isEmpty()) {
                    item {
                        Text(
                            text = "No open desktop windows. Tap This PC, Personalize, or an app to launch.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = winColors.textSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(openWindows, key = { it.type.name }) { win ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(winColors.cardSurface)
                                .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                                .clickable { onFocusWindow(win.type) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WindowTypeIconView(type = win.type, size = 26.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = win.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = winColors.textPrimary
                                )
                                Text(
                                    text = if (win.isMinimized) "Minimized" else "Active window",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = winColors.textSecondary
                                )
                            }
                            IconButton(onClick = { onCloseWindow(win.type) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close window", tint = winColors.textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (recentApps.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Android Apps",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = winColors.textSecondary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(recentApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(winColors.cardSurface.copy(alpha = 0.7f))
                                .clickable { onLaunchApp(app) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppIconImage(app = app, size = 26.dp)
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = winColors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Win11HiddenTrayFlyout(
    isFlashlightOn: Boolean,
    isDarkTheme: Boolean,
    onToggleFlashlight: () -> Unit,
    onToggleTheme: () -> Unit,
    onRefreshApps: () -> Unit,
    onOpenPersonalize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier
            .shadow(18.dp, shape)
            .border(1.dp, winColors.border, shape),
        shape = shape,
        color = winColors.micaSurface
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onToggleFlashlight) {
                Icon(
                    Icons.Rounded.FlashlightOn,
                    contentDescription = "Flashlight",
                    tint = if (isFlashlightOn) winColors.accent else winColors.textPrimary
                )
            }
            IconButton(onClick = onToggleTheme) {
                Icon(
                    if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = "Toggle theme",
                    tint = winColors.textPrimary
                )
            }
            IconButton(onClick = onRefreshApps) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh apps", tint = winColors.textPrimary)
            }
            IconButton(onClick = onOpenPersonalize) {
                Icon(Icons.Rounded.Settings, contentDescription = "Personalize", tint = winColors.textPrimary)
            }
        }
    }
}

@Composable
fun Win11ContextMenuDialog(
    request: ContextMenuRequest,
    preferences: LauncherPreferences,
    onDismiss: () -> Unit,
    onOpenShortcut: (DesktopShortcut) -> Unit,
    onOpenApp: (InstalledApp) -> Unit,
    onTogglePinStart: (String) -> Unit,
    onTogglePinTaskbar: (String) -> Unit,
    onToggleDesktopApp: (String) -> Unit,
    onRemoveShortcut: (DesktopShortcut) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onSetIconSize: (DesktopIconSize) -> Unit,
    onToggleSortAlpha: () -> Unit,
    onRefreshDesktop: () -> Unit,
    onOpenPersonalize: () -> Unit,
    onRestoreDefaultShortcuts: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    fallbackHomeLabel: String? = null,
    onSwitchToSystemHome: () -> Unit = {}
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(12.dp)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 240.dp, max = 300.dp)
                .shadow(24.dp, shape)
                .border(1.dp, winColors.border, shape),
            shape = shape,
            color = winColors.micaSurface
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                if (request.isDesktopBackground) {
                    Text(
                        text = "Desktop options",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    DesktopIconSize.entries.forEach { size ->
                        val selected = preferences.desktopIconSize == size
                        ContextMenuRow(
                            icon = Icons.Rounded.Settings,
                            label = if (selected) "✓ ${size.label}" else "   ${size.label}",
                            onClick = { onSetIconSize(size) }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = winColors.subtleBorder)
                    ContextMenuRow(
                        icon = Icons.Rounded.Refresh,
                        label = if (preferences.sortAlphabetically) "✓ Sort by Name" else "Sort by Name",
                        onClick = onToggleSortAlpha
                    )
                    ContextMenuRow(
                        icon = Icons.Rounded.Refresh,
                        label = "Refresh desktop",
                        onClick = {
                            onRefreshDesktop()
                            onDismiss()
                        }
                    )
                    ContextMenuRow(
                        icon = Icons.Rounded.Home,
                        label = "Restore default icons",
                        onClick = {
                            onRestoreDefaultShortcuts()
                            onDismiss()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = winColors.subtleBorder)
                    ContextMenuRow(
                        icon = Icons.Rounded.Settings,
                        label = "Personalize",
                        onClick = {
                            onDismiss()
                            onOpenPersonalize()
                        }
                    )
                    ContextMenuRow(
                        icon = Icons.Rounded.Home,
                        label = "Set as Default Launcher (Aman)",
                        onClick = {
                            onDismiss()
                            onOpenHomeSettings()
                        }
                    )
                    if (!fallbackHomeLabel.isNullOrBlank()) {
                        ContextMenuRow(
                            icon = Icons.AutoMirrored.Rounded.ExitToApp,
                            label = "Kembali ke $fallbackHomeLabel",
                            onClick = {
                                onDismiss()
                                onSwitchToSystemHome()
                            }
                        )
                    }
                } else {
                    val app = request.app
                    val shortcut = request.shortcut
                    val title = shortcut?.label ?: app?.label ?: "Options"
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    if (shortcut != null) {
                        ContextMenuRow(
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            label = "Open",
                            onClick = { onOpenShortcut(shortcut) }
                        )
                    } else if (app != null) {
                        ContextMenuRow(
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            label = "Open",
                            onClick = { onOpenApp(app) }
                        )
                    }

                    if (app != null) {
                        val isStartPinned = preferences.pinnedStartPackages.contains(app.packageName)
                        val isTaskbarPinned = preferences.pinnedTaskbarPackages.contains(app.packageName)
                        val isDesktopPinned = preferences.desktopAppPackages.contains(app.packageName)

                        ContextMenuRow(
                            icon = Icons.Rounded.PushPin,
                            label = if (isStartPinned) "Unpin from Start" else "Pin to Start",
                            onClick = { onTogglePinStart(app.packageName) }
                        )
                        ContextMenuRow(
                            icon = Icons.Rounded.PushPin,
                            label = if (isTaskbarPinned) "Unpin from Taskbar" else "Pin to Taskbar",
                            onClick = { onTogglePinTaskbar(app.packageName) }
                        )
                        ContextMenuRow(
                            icon = Icons.Rounded.Home,
                            label = if (isDesktopPinned) "Remove from Desktop" else "Add shortcut to Desktop",
                            onClick = { onToggleDesktopApp(app.packageName) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = winColors.subtleBorder)
                        ContextMenuRow(
                            icon = Icons.Rounded.Info,
                            label = "App info & Uninstall",
                            onClick = { onOpenAppInfo(app.packageName) }
                        )
                    } else if (shortcut != null && shortcut.kind != ShortcutKind.SETTINGS) {
                        ContextMenuRow(
                            icon = Icons.Rounded.Close,
                            label = "Hide from Desktop",
                            onClick = { onRemoveShortcut(shortcut) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = winColors.textPrimary, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = winColors.textPrimary
        )
    }
}
