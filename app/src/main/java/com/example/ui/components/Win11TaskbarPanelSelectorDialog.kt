package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.LauncherPreferences
import com.example.model.TaskbarAlignment
import com.example.model.TaskbarIconSize
import com.example.model.TaskbarSearchMode
import com.example.model.TaskbarStyle
import com.example.ui.theme.LocalWin11Colors
import kotlin.math.roundToInt

@Composable
fun Win11TaskbarPanelSettingsContent(
    preferences: LauncherPreferences,
    onSetHideStatusBar: (Boolean) -> Unit,
    onSetHideNavBar: (Boolean) -> Unit,
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
    onSet24HourClock: (Boolean) -> Unit,
    onSetShowSeconds: (Boolean) -> Unit,
    onSetShowDesktopButton: (Boolean) -> Unit,
    onResetTaskbarDefaults: () -> Unit
) {
    val winColors = LocalWin11Colors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("taskbar_panel_settings_content"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = winColors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Pilih Panel & Item Taskbar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = winColors.textPrimary
                    )
                    Text(
                        text = "Geser/ubah ukuran jendela ini untuk melihat perubahan taskbar secara langsung",
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }
            }
            TextButton(
                onClick = onResetTaskbarDefaults,
                modifier = Modifier.testTag("selector_reset_button")
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        HorizontalDivider(color = winColors.subtleBorder)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Navbar & Layar Sistem",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textSecondary
            )
            TaskbarToggleRow(
                title = "Sembunyikan Navbar Android (Hide Nav Bar)",
                checked = preferences.hideSystemNavBar,
                onCheckedChange = onSetHideNavBar,
                testTag = "selector_toggle_hide_navbar"
            )
            TaskbarToggleRow(
                title = "Sembunyikan Status Bar Atas (Full-Screen)",
                checked = preferences.hideSystemStatusBar,
                onCheckedChange = onSetHideStatusBar,
                testTag = "selector_toggle_hide_statusbar"
            )

            HorizontalDivider(color = winColors.subtleBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tinggi Taskbar",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = winColors.textSecondary
                )
                Text(
                    text = "${preferences.taskbarHeightDp} dp",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = winColors.accent
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
                modifier = Modifier.fillMaxWidth().testTag("selector_height_slider")
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Compact" to 46, "Standard" to 54, "Tall" to 64).forEach { (label, dpVal) ->
                    ChoiceChipBox(
                        text = "$label ($dpVal)",
                        selected = preferences.taskbarHeightDp == dpVal,
                        onClick = { onSetTaskbarHeightDp(dpVal) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = winColors.subtleBorder)

            Text(
                text = "Gaya, Posisi & Ukuran Ikon",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TaskbarStyle.entries.forEach { style ->
                    ChoiceChipBox(
                        text = style.label,
                        selected = preferences.taskbarStyle == style,
                        onClick = { onSetTaskbarStyle(style) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("selector_style_${style.name}")
                    )
                }
                TaskbarAlignment.entries.forEach { align ->
                    ChoiceChipBox(
                        text = align.label,
                        selected = preferences.taskbarAlignment == align,
                        onClick = { onSetTaskbarAlignment(align) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("selector_align_${align.name}")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TaskbarIconSize.entries.forEach { size ->
                    ChoiceChipBox(
                        text = "Ikon: ${size.label}",
                        selected = preferences.taskbarIconSize == size,
                        onClick = { onSetTaskbarIconSize(size) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

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
                modifier = Modifier.fillMaxWidth()
            )

            TaskbarToggleRow(
                title = "Garis Pembatas Atas Taskbar",
                checked = preferences.taskbarShowDivider,
                onCheckedChange = onSetTaskbarShowDivider,
                testTag = "selector_toggle_divider"
            )
            TaskbarToggleRow(
                title = "Indikator Garis Window/Panel Aktif",
                checked = preferences.taskbarShowActiveIndicator,
                onCheckedChange = onSetTaskbarShowActiveIndicator,
                testTag = "selector_toggle_indicator"
            )

            HorizontalDivider(color = winColors.subtleBorder)

            Text(
                text = "Panel Search",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TaskbarSearchMode.entries.forEach { mode ->
                    ChoiceChipBox(
                        text = mode.label,
                        selected = preferences.taskbarSearchMode == mode,
                        onClick = { onSetTaskbarSearchMode(mode) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("selector_search_${mode.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Panel & Tombol Utama",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textSecondary
            )

            TaskbarToggleRow(
                title = "Tombol Task View",
                checked = preferences.showTaskViewInTaskbar,
                onCheckedChange = onSetShowTaskView,
                testTag = "selector_toggle_task_view"
            )
            TaskbarToggleRow(
                title = "Pin File Explorer",
                checked = preferences.showFileExplorerInTaskbar,
                onCheckedChange = onSetShowFileExplorer,
                testTag = "selector_toggle_explorer"
            )
            TaskbarToggleRow(
                title = "Aplikasi Ter-pin (Pinned Apps)",
                checked = preferences.showPinnedAppsInTaskbar,
                onCheckedChange = onSetShowPinnedApps,
                testTag = "selector_toggle_pinned"
            )
            TaskbarToggleRow(
                title = "Tombol Show Desktop (Pojok Kanan)",
                checked = preferences.showDesktopButtonInTaskbar,
                onCheckedChange = onSetShowDesktopButton,
                testTag = "selector_toggle_show_desktop"
            )

            HorizontalDivider(color = winColors.subtleBorder)

            Text(
                text = "Panel System Tray (Kanan)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textSecondary
            )

            TaskbarToggleRow(
                title = "Panah Hidden Icons (^)",
                checked = preferences.showHiddenTrayInTaskbar,
                onCheckedChange = onSetShowHiddenTray,
                testTag = "selector_toggle_hidden_tray"
            )
            TaskbarToggleRow(
                title = "Indikator Wi-Fi / Jaringan",
                checked = preferences.showTrayNetwork,
                onCheckedChange = onSetShowTrayNetwork,
                testTag = "selector_toggle_network"
            )
            TaskbarToggleRow(
                title = "Ikon Volume",
                checked = preferences.showTrayVolume,
                onCheckedChange = onSetShowTrayVolume,
                testTag = "selector_toggle_volume"
            )
            TaskbarToggleRow(
                title = "Ikon Baterai",
                checked = preferences.showTrayBattery,
                onCheckedChange = onSetShowTrayBattery,
                testTag = "selector_toggle_battery"
            )
            TaskbarToggleRow(
                title = "Persentase Baterai (%)",
                checked = preferences.showTrayBatteryPercent,
                onCheckedChange = onSetShowTrayBatteryPercent,
                enabled = preferences.showTrayBattery,
                testTag = "selector_toggle_battery_percent"
            )
            TaskbarToggleRow(
                title = "Panel Jam & Kalender",
                checked = preferences.showTrayClock,
                onCheckedChange = onSetShowTrayClock,
                testTag = "selector_toggle_clock"
            )
            TaskbarToggleRow(
                title = "Tampilkan Tanggal di Bawah Jam",
                checked = preferences.showTrayDate,
                onCheckedChange = onSetShowTrayDate,
                enabled = preferences.showTrayClock,
                testTag = "selector_toggle_date"
            )
            TaskbarToggleRow(
                title = "Titik Notifikasi Samping Jam",
                checked = preferences.showTrayNotificationDot,
                onCheckedChange = onSetShowTrayNotificationDot,
                enabled = preferences.showTrayClock,
                testTag = "selector_toggle_dot"
            )
            TaskbarToggleRow(
                title = "Format Jam 24-Jam",
                checked = preferences.use24HourClock,
                onCheckedChange = onSet24HourClock,
                enabled = preferences.showTrayClock,
                testTag = "selector_toggle_24h"
            )
            TaskbarToggleRow(
                title = "Tampilkan Detik pada Jam",
                checked = preferences.showSecondsInClock,
                onCheckedChange = onSetShowSeconds,
                enabled = preferences.showTrayClock,
                testTag = "selector_toggle_seconds"
            )
        }
    }
}
