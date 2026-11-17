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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.LauncherPreferences
import com.example.model.TaskbarAlignment
import com.example.model.TaskbarIconSize
import com.example.model.TaskbarSearchMode
import com.example.model.TaskbarStyle
import com.example.ui.theme.LocalWin11Colors
import kotlin.math.roundToInt

@Composable
fun Win11TaskbarSettingsSection(
    preferences: LauncherPreferences,
    onSetTaskbarAlignment: (TaskbarAlignment) -> Unit,
    onSetTaskbarHeightDp: (Int) -> Unit,
    onSetTaskbarStyle: (TaskbarStyle) -> Unit,
    onSetTaskbarIconSize: (TaskbarIconSize) -> Unit,
    onSetTaskbarOpacity: (Float) -> Unit,
    onSetTaskbarShowDivider: (Boolean) -> Unit,
    onSetTaskbarShowActiveIndicator: (Boolean) -> Unit,
    onSetTaskbarSearchMode: (TaskbarSearchMode) -> Unit,
    onSetShowTaskView: (Boolean) -> Unit,
    onSetShowFileExplorer: (Boolean) -> Unit,
    onSetShowPinnedApps: (Boolean) -> Unit,
    onSetShowHiddenTray: (Boolean) -> Unit,
    onSetShowTrayNetwork: (Boolean) -> Unit,
    onSetShowTrayVolume: (Boolean) -> Unit,
    onSetShowTrayBattery: (Boolean) -> Unit,
    onSetShowTrayBatteryPercent: (Boolean) -> Unit,
    onSetShowTrayClock: (Boolean) -> Unit,
    onSetShowTrayDate: (Boolean) -> Unit,
    onSetShowTrayNotificationDot: (Boolean) -> Unit,
    onSetShowDesktopButton: (Boolean) -> Unit,
    onSet24HourClock: (Boolean) -> Unit,
    onSetShowSeconds: (Boolean) -> Unit,
    onResetTaskbarDefaults: () -> Unit,
    onOpenPanelSelector: () -> Unit
) {
    val winColors = LocalWin11Colors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(winColors.cardSurface)
            .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
            .testTag("settings_taskbar_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + Reset Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = winColors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Personalisasi Taskbar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textPrimary
                    )
                    Text(
                        text = "Atur ukuran, tampilan, dan komponen yang tampil di taskbar",
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }
            }
            TextButton(
                onClick = onResetTaskbarDefaults,
                modifier = Modifier.testTag("settings_taskbar_reset")
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        // Quick Selector Button
        Button(
            onClick = onOpenPanelSelector,
            colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_open_panel_selector")
        ) {
            Icon(Icons.Rounded.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pilih & Atur Panel Taskbar Sendiri...", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        HorizontalDivider(color = winColors.subtleBorder)

        // 1. Taskbar Height Slider + Presets
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tinggi Taskbar",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = winColors.textPrimary
                )
                Text(
                    text = "${preferences.taskbarHeightDp} dp",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = winColors.accent,
                    modifier = Modifier.testTag("settings_taskbar_height_label")
                )
            }
            Slider(
                value = preferences.taskbarHeightDp.toFloat(),
                onValueChange = { onSetTaskbarHeightDp(it.roundToInt()) },
                valueRange = 44f..72f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = winColors.accent,
                    activeTrackColor = winColors.accent,
                    inactiveTrackColor = winColors.border
                ),
                modifier = Modifier.fillMaxWidth().testTag("settings_taskbar_height_slider")
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Compact" to 46, "Standard" to 54, "Tall" to 64).forEach { (label, dpVal) ->
                    val selected = preferences.taskbarHeightDp == dpVal
                    ChoiceChipBox(
                        text = "$label ($dpVal)",
                        selected = selected,
                        onClick = { onSetTaskbarHeightDp(dpVal) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_taskbar_height_preset_$dpVal")
                    )
                }
            }
        }

        HorizontalDivider(color = winColors.subtleBorder)

        // 2. Style, Alignment & Icon Size
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Tampilan & Posisi Taskbar",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskbarStyle.entries.forEach { style ->
                    ChoiceChipBox(
                        text = "Style: ${style.label}",
                        selected = preferences.taskbarStyle == style,
                        onClick = { onSetTaskbarStyle(style) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_taskbar_style_${style.name}")
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskbarAlignment.entries.forEach { align ->
                    ChoiceChipBox(
                        text = "Align: ${align.label}",
                        selected = preferences.taskbarAlignment == align,
                        onClick = { onSetTaskbarAlignment(align) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_taskbar_align_${align.name}")
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskbarIconSize.entries.forEach { size ->
                    ChoiceChipBox(
                        text = "Ikon: ${size.label}",
                        selected = preferences.taskbarIconSize == size,
                        onClick = { onSetTaskbarIconSize(size) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_taskbar_icon_${size.name}")
                    )
                }
            }

            // Opacity slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opasitas Latar Taskbar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = winColors.textPrimary
                )
                Text(
                    text = "${(preferences.taskbarOpacity * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.accent
                )
            }
            Slider(
                value = preferences.taskbarOpacity,
                onValueChange = { onSetTaskbarOpacity(it) },
                valueRange = 0.45f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = winColors.accent,
                    activeTrackColor = winColors.accent,
                    inactiveTrackColor = winColors.border
                ),
                modifier = Modifier.fillMaxWidth().testTag("settings_taskbar_opacity_slider")
            )

            TaskbarToggleRow(
                title = "Garis Pembatas Atas Taskbar",
                checked = preferences.taskbarShowDivider,
                onCheckedChange = onSetTaskbarShowDivider,
                testTag = "settings_toggle_taskbar_divider"
            )
            TaskbarToggleRow(
                title = "Indikator Garis Window/Panel Aktif",
                checked = preferences.taskbarShowActiveIndicator,
                onCheckedChange = onSetTaskbarShowActiveIndicator,
                testTag = "settings_toggle_taskbar_indicator"
            )
        }

        HorizontalDivider(color = winColors.subtleBorder)

        // 3. Dock Components (Apa saja yang muncul di taskbar)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Komponen Utama Taskbar",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )

            Text(
                text = "Mode Tombol Search",
                style = MaterialTheme.typography.labelSmall,
                color = winColors.textSecondary
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TaskbarSearchMode.entries.forEach { mode ->
                    ChoiceChipBox(
                        text = mode.label,
                        selected = preferences.taskbarSearchMode == mode,
                        onClick = { onSetTaskbarSearchMode(mode) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_taskbar_search_${mode.name}")
                    )
                }
            }

            TaskbarToggleRow(
                title = "Tombol Task View",
                checked = preferences.showTaskViewInTaskbar,
                onCheckedChange = onSetShowTaskView,
                testTag = "settings_toggle_taskbar_task_view"
            )
            TaskbarToggleRow(
                title = "Pin File Explorer",
                checked = preferences.showFileExplorerInTaskbar,
                onCheckedChange = onSetShowFileExplorer,
                testTag = "settings_toggle_taskbar_explorer"
            )
            TaskbarToggleRow(
                title = "Aplikasi Ter-pin (Pinned Apps)",
                checked = preferences.showPinnedAppsInTaskbar,
                onCheckedChange = onSetShowPinnedApps,
                testTag = "settings_toggle_taskbar_pinned_apps"
            )
            TaskbarToggleRow(
                title = "Tombol Show Desktop (Pojok Kanan)",
                checked = preferences.showDesktopButtonInTaskbar,
                onCheckedChange = onSetShowDesktopButton,
                testTag = "settings_toggle_taskbar_show_desktop"
            )
        }

        HorizontalDivider(color = winColors.subtleBorder)

        // 4. System Tray Components
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Komponen System Tray (Kanan)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary
            )
            TaskbarToggleRow(
                title = "Panah Hidden Icons (^)",
                checked = preferences.showHiddenTrayInTaskbar,
                onCheckedChange = onSetShowHiddenTray,
                testTag = "settings_toggle_tray_hidden"
            )
            TaskbarToggleRow(
                title = "Indikator Wi-Fi / Jaringan",
                checked = preferences.showTrayNetwork,
                onCheckedChange = onSetShowTrayNetwork,
                testTag = "settings_toggle_tray_network"
            )
            TaskbarToggleRow(
                title = "Ikon Volume",
                checked = preferences.showTrayVolume,
                onCheckedChange = onSetShowTrayVolume,
                testTag = "settings_toggle_tray_volume"
            )
            TaskbarToggleRow(
                title = "Ikon Baterai",
                checked = preferences.showTrayBattery,
                onCheckedChange = onSetShowTrayBattery,
                testTag = "settings_toggle_tray_battery"
            )
            TaskbarToggleRow(
                title = "Persentase Baterai (%)",
                checked = preferences.showTrayBatteryPercent,
                onCheckedChange = onSetShowTrayBatteryPercent,
                enabled = preferences.showTrayBattery,
                testTag = "settings_toggle_tray_battery_percent"
            )
            TaskbarToggleRow(
                title = "Jam & Tanggal",
                checked = preferences.showTrayClock,
                onCheckedChange = onSetShowTrayClock,
                testTag = "settings_toggle_tray_clock"
            )
            TaskbarToggleRow(
                title = "Tampilkan Tanggal di Bawah Jam",
                checked = preferences.showTrayDate,
                onCheckedChange = onSetShowTrayDate,
                enabled = preferences.showTrayClock,
                testTag = "settings_toggle_tray_date"
            )
            TaskbarToggleRow(
                title = "Titik Notifikasi Samping Jam",
                checked = preferences.showTrayNotificationDot,
                onCheckedChange = onSetShowTrayNotificationDot,
                enabled = preferences.showTrayClock,
                testTag = "settings_toggle_tray_dot"
            )
            TaskbarToggleRow(
                title = "Format Jam 24-Jam",
                checked = preferences.use24HourClock,
                onCheckedChange = onSet24HourClock,
                enabled = preferences.showTrayClock,
                testTag = "settings_toggle_tray_24h"
            )
            TaskbarToggleRow(
                title = "Tampilkan Detik pada Jam",
                checked = preferences.showSecondsInClock,
                onCheckedChange = onSetShowSeconds,
                enabled = preferences.showTrayClock,
                testTag = "settings_toggle_tray_seconds"
            )
        }
    }
}

@Composable
internal fun ChoiceChipBox(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) winColors.accentSoft else winColors.micaSurface)
            .border(
                width = 1.dp,
                color = if (selected) winColors.accent else winColors.subtleBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else winColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun TaskbarToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    enabled: Boolean = true
) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 3.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = winColors.textPrimary,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = winColors.accentSoft
            )
        )
    }
}
