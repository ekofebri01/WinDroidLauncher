package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
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
import androidx.compose.runtime.mutableStateOf
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
    onOpenTaskbarSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(12.dp)
    val activeContentColor = if (winColors.isDark) Color(0xFF001C32) else Color.White
    val volumePct = (telemetry.mediaVolume * 100 / telemetry.maxMediaVolume.coerceAtLeast(1)).coerceIn(0, 100)

    Surface(
        modifier = modifier
            .widthIn(max = 356.dp)
            .fillMaxWidth()
            .shadow(24.dp, shape)
            .border(1.dp, winColors.border, shape)
            .testTag("win11_quick_settings"),
        shape = shape,
        color = winColors.micaSurface
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 3x2 Windows 11 Fluent Quick Action Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickSettingSplitTile(
                        icon = if (telemetry.isWifiConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                        label = when {
                            telemetry.isWifiConnected -> "Wi-Fi"
                            telemetry.isCellularConnected -> "Cellular"
                            else -> "Network"
                        },
                        isActive = telemetry.isWifiConnected || telemetry.isCellularConnected,
                        hasSplitChevron = true,
                        activeContentColor = activeContentColor,
                        onClick = onWifiClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickSettingSplitTile(
                        icon = Icons.Rounded.Bluetooth,
                        label = "Bluetooth",
                        isActive = false,
                        hasSplitChevron = true,
                        activeContentColor = activeContentColor,
                        onClick = onBluetoothClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickSettingSplitTile(
                        icon = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        label = if (isDarkTheme) "Dark Mode" else "Light Mode",
                        isActive = isDarkTheme,
                        hasSplitChevron = false,
                        activeContentColor = activeContentColor,
                        onClick = onToggleTheme,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickSettingSplitTile(
                        icon = Icons.Rounded.FlashlightOn,
                        label = "Flashlight",
                        isActive = telemetry.isFlashlightOn,
                        hasSplitChevron = false,
                        activeContentColor = activeContentColor,
                        onClick = onToggleFlashlight,
                        modifier = Modifier.weight(1f)
                    )
                    QuickSettingSplitTile(
                        icon = Icons.Rounded.Info,
                        label = if (telemetry.isMuted) "Muted" else "Sound $volumePct%",
                        isActive = !telemetry.isMuted,
                        hasSplitChevron = false,
                        activeContentColor = activeContentColor,
                        onClick = onToggleMute,
                        modifier = Modifier.weight(1f),
                        customIcon = { iconColor ->
                            Win11TrayVolumeIcon(
                                volume = telemetry.mediaVolume,
                                maxVolume = telemetry.maxMediaVolume,
                                isMuted = telemetry.isMuted,
                                tint = iconColor,
                                size = 18.dp
                            )
                        }
                    )
                    QuickSettingSplitTile(
                        icon = Icons.Rounded.Home,
                        label = "Default Home",
                        isActive = false,
                        hasSplitChevron = false,
                        activeContentColor = activeContentColor,
                        onClick = onHomeSettingsClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Display & Brightness Quick Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(winColors.cardSurface.copy(alpha = 0.7f))
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onDisplaySettingsClick)
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Brightness6,
                        contentDescription = "Display Brightness",
                        tint = winColors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Display & Brightness Settings",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
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
                    IconButton(onClick = onToggleMute, modifier = Modifier.size(34.dp)) {
                        Win11TrayVolumeIcon(
                            volume = telemetry.mediaVolume,
                            maxVolume = telemetry.maxMediaVolume,
                            isMuted = telemetry.isMuted,
                            tint = winColors.textPrimary,
                            size = 18.dp
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
                        text = "$volumePct%",
                        style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                        color = winColors.textSecondary,
                        modifier = Modifier.width(34.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Windows 11 Tinted Footer Strip
            HorizontalDivider(thickness = 1.dp, color = winColors.subtleBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(winColors.cardSurface.copy(alpha = 0.65f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Win11TrayBatteryIcon(
                        percent = telemetry.batteryPercent,
                        isCharging = telemetry.isCharging,
                        tint = winColors.textPrimary,
                        size = 17.dp
                    )
                    Text(
                        text = "${telemetry.batteryPercent}%${if (telemetry.isCharging) " • Charging" else ""}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        color = winColors.textPrimary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onOpenTaskbarSettings,
                        modifier = Modifier.size(34.dp).testTag("quick_settings_tray_customize")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Customize System Tray",
                            tint = winColors.textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenPersonalize,
                        modifier = Modifier.size(34.dp).testTag("quick_settings_personalize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "All Settings",
                            tint = winColors.textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSettingSplitTile(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    hasSplitChevron: Boolean,
    activeContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    customIcon: (@Composable (Color) -> Unit)? = null
) {
    val winColors = LocalWin11Colors.current
    val tileShape = RoundedCornerShape(8.dp)
    val contentTint = if (isActive) activeContentColor else winColors.textPrimary

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(tileShape)
                .background(if (isActive) winColors.accent else winColors.cardSurface)
                .border(
                    width = 1.dp,
                    color = if (isActive) winColors.accent else winColors.subtleBorder,
                    shape = tileShape
                )
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (customIcon != null) {
                    customIcon(contentTint)
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentTint,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            if (hasSplitChevron) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(22.dp)
                        .background(contentTint.copy(alpha = 0.22f))
                )
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = contentTint.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
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
    val shape = RoundedCornerShape(12.dp)
    var monthOffset by remember { mutableIntStateOf(0) }
    var calendarExpanded by remember { mutableStateOf(true) }

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

    val headerDate = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(currentTimeMillis))
    val monthYearLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayCal.time)

    val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = displayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    val isCurrentMonth = displayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        displayCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    val cells = remember(monthOffset, daysInMonth, firstDayOfWeek) {
        List(firstDayOfWeek) { null } + (1..daysInMonth).toList()
    }
    val hasUpdateNotice = githubUpdateState.status == GitHubUpdateStatus.UPDATE_AVAILABLE
    val notificationCount = if (hasUpdateNotice) 2 else 1
    val storageProgress = (telemetry.usedStorageGb / telemetry.totalStorageGb.coerceAtLeast(1f)).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .widthIn(max = 356.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .testTag("win11_calendar_flyout"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Card 1: Windows 11 Notification Center
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(22.dp, shape)
                .border(1.dp, winColors.border, shape),
            shape = shape,
            color = winColors.micaSurface
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = winColors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = winColors.textPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(winColors.cardSurface)
                            .border(1.dp, winColors.subtleBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$notificationCount active",
                            style = MaterialTheme.typography.labelSmall,
                            color = winColors.textSecondary
                        )
                    }
                }

                if (hasUpdateNotice) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.16f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenUpdateSettings)
                            .padding(11.dp)
                            .testTag("flyout_github_update_notice"),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Windows Update • ${githubUpdateState.latestTag ?: ""}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF4CD4A9)
                        )
                        Text(
                            text = "New release ready. Tap to download & install",
                            style = MaterialTheme.typography.labelSmall,
                            color = winColors.textPrimary
                        )
                    }
                }

                // System Status Notification Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenThisPc)
                        .padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "System Health",
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
                        style = MaterialTheme.typography.bodySmall,
                        color = winColors.textPrimary
                    )
                    LinearProgressIndicator(
                        progress = { storageProgress },
                        color = winColors.accent,
                        trackColor = winColors.border,
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                    )
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "Local Disk (C:) %.1f GB used of %.0f GB",
                            telemetry.usedStorageGb,
                            telemetry.totalStorageGb
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }
            }
        }

        // Card 2: Collapsible Windows 11 Calendar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(22.dp, shape)
                .border(1.dp, winColors.border, shape),
            shape = shape,
            color = winColors.micaSurface
        ) {
            Column {
                // Date Header Strip + Collapse Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(winColors.cardSurface.copy(alpha = 0.65f))
                        .clickable { calendarExpanded = !calendarExpanded }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .testTag("calendar_collapse_toggle"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = headerDate,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = winColors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(winColors.cardSurface)
                            .border(1.dp, winColors.subtleBorder, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (calendarExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                            contentDescription = if (calendarExpanded) "Collapse calendar" else "Expand calendar",
                            tint = winColors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = calendarExpanded) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Month navigation (Win11 Up/Down arrows + Today shortcut)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = monthYearLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = winColors.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (monthOffset != 0) {
                                    Text(
                                        text = "Today",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = winColors.accent,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { monthOffset = 0 }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(onClick = { monthOffset-- }, modifier = Modifier.size(30.dp)) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = "Previous month",
                                        tint = winColors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { monthOffset++ }, modifier = Modifier.size(30.dp)) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = "Next month",
                                        tint = winColors.textPrimary,
                                        modifier = Modifier.size(18.dp)
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
                        val todayTextColor = if (winColors.isDark) Color(0xFF001C32) else Color.White
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            cells.chunked(7).forEach { week ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (i in 0 until 7) {
                                        val day = week.getOrNull(i)
                                        val isToday = isCurrentMonth && day == todayDay
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(27.dp)
                                                .clip(CircleShape)
                                                .background(if (isToday) winColors.accent else Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (day != null) {
                                                Text(
                                                    text = day.toString(),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                        fontFeatureSettings = "tnum"
                                                    ),
                                                    color = if (isToday) todayTextColor else winColors.textPrimary
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
    onOpenTaskbarSettings: () -> Unit = {},
    onOpenWifiSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier
            .widthIn(min = 196.dp, max = 236.dp)
            .shadow(20.dp, shape)
            .border(1.dp, winColors.border, shape)
            .testTag("win11_hidden_tray_flyout"),
        shape = shape,
        color = winColors.micaSurface
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System Tray",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = winColors.textSecondary
                )
                Text(
                    text = "Customize",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = winColors.accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onOpenTaskbarSettings)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HiddenTrayOverflowTile(
                    icon = Icons.Rounded.FlashlightOn,
                    description = "Flashlight",
                    isActive = isFlashlightOn,
                    onClick = onToggleFlashlight,
                    modifier = Modifier.weight(1f)
                )
                HiddenTrayOverflowTile(
                    icon = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    description = "Toggle theme",
                    isActive = isDarkTheme,
                    onClick = onToggleTheme,
                    modifier = Modifier.weight(1f)
                )
                HiddenTrayOverflowTile(
                    icon = Icons.Rounded.Refresh,
                    description = "Refresh apps",
                    isActive = false,
                    onClick = onRefreshApps,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HiddenTrayOverflowTile(
                    icon = Icons.Rounded.Wifi,
                    description = "Network Settings",
                    isActive = false,
                    onClick = onOpenWifiSettings,
                    modifier = Modifier.weight(1f)
                )
                HiddenTrayOverflowTile(
                    icon = Icons.Rounded.Tune,
                    description = "Taskbar & Tray Settings",
                    isActive = false,
                    onClick = onOpenTaskbarSettings,
                    modifier = Modifier.weight(1f)
                )
                HiddenTrayOverflowTile(
                    icon = Icons.Rounded.Settings,
                    description = "Personalize",
                    isActive = false,
                    onClick = onOpenPersonalize,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HiddenTrayOverflowTile(
    icon: ImageVector,
    description: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(shape)
            .background(if (isActive) winColors.accent.copy(alpha = 0.16f) else winColors.cardSurface)
            .border(
                width = 1.dp,
                color = if (isActive) winColors.accent.copy(alpha = 0.45f) else winColors.subtleBorder,
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isActive) winColors.accent else winColors.textPrimary,
            modifier = Modifier.size(18.dp)
        )
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
    onSwitchToSystemHome: () -> Unit = {},
    hasDesktopWidgets: Boolean = false,
    onAddWidget: () -> Unit = {},
    onEditWidgets: () -> Unit = {}
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
                    ContextMenuRow(
                        icon = Icons.Rounded.Widgets,
                        label = "Tambah Widget Aplikasi",
                        onClick = {
                            onDismiss()
                            onAddWidget()
                        }
                    )
                    if (hasDesktopWidgets) {
                        ContextMenuRow(
                            icon = Icons.Rounded.Tune,
                            label = "Atur / Ubah Ukuran Widget",
                            onClick = {
                                onDismiss()
                                onEditWidgets()
                            }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = winColors.subtleBorder)
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
