package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.LauncherPreferences
import com.example.model.TaskbarAlignment
import com.example.model.TaskbarSearchMode
import com.example.model.TaskbarStyle
import com.example.ui.theme.LocalWin11Colors

@Composable
fun Win11TaskbarPanelSelectorDialog(
    preferences: LauncherPreferences,
    onSetTaskbarAlignment: (TaskbarAlignment) -> Unit,
    onSetTaskbarStyle: (TaskbarStyle) -> Unit,
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
    onResetTaskbarDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    val winColors = LocalWin11Colors.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp)
                .fillMaxHeight(0.84f)
                .clip(RoundedCornerShape(16.dp))
                .background(winColors.cardSurface)
                .border(1.dp, winColors.border, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("taskbar_panel_selector_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Pilih Panel & Item Taskbar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = winColors.textPrimary
                            )
                            Text(
                                text = "Aktifkan atau sembunyikan setiap panel taskbar sesuai selera",
                                style = MaterialTheme.typography.bodySmall,
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Gaya & Posisi Taskbar",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Panel Search",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Panel & Tombol Utama",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
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

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Panel System Tray (Kanan)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
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

                HorizontalDivider(color = winColors.subtleBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("selector_close_button")
                    ) {
                        Text("Selesai", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
