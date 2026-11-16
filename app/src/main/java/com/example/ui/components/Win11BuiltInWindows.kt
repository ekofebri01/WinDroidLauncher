package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DesktopIconSize
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.model.SystemTelemetry
import com.example.model.TaskbarAlignment
import com.example.model.WallpaperOption
import com.example.ui.theme.LocalWin11Colors
import java.util.Locale

@Composable
fun ThisPcWindowContent(
    telemetry: SystemTelemetry,
    installedAppsCount: Int,
    onOpenStorageSettings: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onOpenUserFiles: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val diskProgress = (telemetry.usedStorageGb / telemetry.totalStorageGb).coerceIn(0f, 1f)
    val ramUsedMb = (telemetry.totalRamMb - telemetry.availableRamMb).coerceAtLeast(0L)
    val ramProgress = (ramUsedMb.toFloat() / telemetry.totalRamMb.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Devices and drives",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = winColors.textPrimary
        )

        // Drive C: Internal Storage
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(winColors.cardSurface)
                .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenStorageSettings)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Storage,
                contentDescription = "Local Disk C",
                tint = winColors.accent,
                modifier = Modifier.size(34.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Local Disk (C:) — Internal Storage",
                    style = MaterialTheme.typography.titleSmall,
                    color = winColors.textPrimary
                )
                LinearProgressIndicator(
                    progress = { diskProgress },
                    color = if (diskProgress > 0.88f) Color(0xFFE74C3C) else winColors.accent,
                    trackColor = winColors.border,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "%.1f GB free of %.0f GB",
                        (telemetry.totalStorageGb - telemetry.usedStorageGb).coerceAtLeast(0f),
                        telemetry.totalStorageGb
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = winColors.textSecondary
                )
            }
        }

        // System Memory & Specs
        Text(
            text = "System specifications",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = winColors.textPrimary
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(winColors.cardSurface)
                .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpecRow(Icons.Rounded.PhoneAndroid, "Device Name", telemetry.deviceModel)
            SpecRow(Icons.Rounded.Info, "Operating System", telemetry.androidVersion)
            SpecRow(Icons.Rounded.Memory, "Memory (RAM)", "$ramUsedMb MB / ${telemetry.totalRamMb} MB")
            LinearProgressIndicator(
                progress = { ramProgress },
                color = Color(0xFF4CD4A9),
                trackColor = winColors.border,
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape)
            )
            SpecRow(Icons.Rounded.FolderOpen, "Installed Applications", "$installedAppsCount apps")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onOpenUserFiles,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("User Files")
            }
            Button(
                onClick = onOpenSystemSettings,
                colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Android Settings")
            }
        }
    }
}

@Composable
private fun SpecRow(icon: ImageVector, label: String, value: String) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = winColors.accent, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = winColors.textSecondary)
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = winColors.textPrimary
        )
    }
}

@Composable
fun UserFilesWindowContent(
    onOpenMimeCategory: (String) -> Unit,
    onOpenStorageSettings: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val folders = listOf(
        Triple("Pictures", "image/*", Icons.Rounded.Image),
        Triple("Documents", "application/*", Icons.Rounded.Description),
        Triple("Videos", "video/*", Icons.Rounded.VideoLibrary),
        Triple("Music & Audio", "audio/*", Icons.Rounded.AudioFile)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick access folders",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = winColors.textPrimary
        )

        folders.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { (title, mime, icon) ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(winColors.cardSurface)
                            .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                            .clickable { onOpenMimeCategory(mime) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Win11FolderIcon(size = 32.dp)
                        Column {
                            Text(title, style = MaterialTheme.typography.titleSmall, color = winColors.textPrimary)
                            Text("Open library", style = MaterialTheme.typography.labelSmall, color = winColors.textSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(onClick = onOpenStorageSettings, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage Device Storage")
        }
    }
}

@Composable
fun PersonalizeWindowContent(
    preferences: LauncherPreferences,
    onSetDarkTheme: (Boolean) -> Unit,
    onSelectWallpaper: (WallpaperOption) -> Unit,
    onPickCustomWallpaper: () -> Unit,
    onSetDesktopIconSize: (DesktopIconSize) -> Unit,
    onSetTaskbarAlignment: (TaskbarAlignment) -> Unit,
    onSet24HourClock: (Boolean) -> Unit,
    onSetShowSeconds: (Boolean) -> Unit,
    onSetHideStatusBar: (Boolean) -> Unit,
    onSetUserName: (String) -> Unit,
    onRestoreDefaults: () -> Unit,
    onOpenHomeSettings: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    var nameDraft by remember(preferences.userName) { mutableStateOf(preferences.userName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Default Home Action Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(winColors.accentSoft.copy(alpha = 0.18f))
                .border(1.dp, winColors.accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Use Win11 Launcher as Home",
                    style = MaterialTheme.typography.titleSmall,
                    color = winColors.textPrimary
                )
                Text(
                    "Open Android Default Home App settings",
                    style = MaterialTheme.typography.labelSmall,
                    color = winColors.textSecondary
                )
            }
            Button(
                onClick = onOpenHomeSettings,
                colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                modifier = Modifier.testTag("settings_set_default_home")
            ) {
                Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Set Default")
            }
        }

        // Wallpaper Picker Section
        Text(
            text = "Background & Wallpaper",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = winColors.textPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WallpaperPresetCard(
                label = "Bloom Dark",
                drawableRes = R.drawable.img_win11_bloom_dark,
                selected = preferences.wallpaperOption == WallpaperOption.BLOOM_DARK,
                onClick = { onSelectWallpaper(WallpaperOption.BLOOM_DARK) },
                modifier = Modifier.weight(1f)
            )
            WallpaperPresetCard(
                label = "Bloom Light",
                drawableRes = R.drawable.img_win11_bloom_light,
                selected = preferences.wallpaperOption == WallpaperOption.BLOOM_LIGHT,
                onClick = { onSelectWallpaper(WallpaperOption.BLOOM_LIGHT) },
                modifier = Modifier.weight(1f)
            )
            WallpaperPresetCard(
                label = "Flow",
                drawableRes = R.drawable.img_win11_captured_flow,
                selected = preferences.wallpaperOption == WallpaperOption.CAPTURED_FLOW,
                onClick = { onSelectWallpaper(WallpaperOption.CAPTURED_FLOW) },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedButton(
            onClick = onPickCustomWallpaper,
            modifier = Modifier.fillMaxWidth().testTag("settings_pick_wallpaper")
        ) {
            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose Custom Photo from Gallery")
        }

        // Appearance Toggles
        Text(
            text = "Theme & Taskbar",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = winColors.textPrimary
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(winColors.cardSurface)
                .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingSwitchRow("Windows 11 Dark Mode", preferences.isDarkTheme, onSetDarkTheme)
            SettingSwitchRow("24-Hour Taskbar Clock", preferences.use24HourClock, onSet24HourClock)
            SettingSwitchRow("Show Seconds in Clock", preferences.showSecondsInClock, onSetShowSeconds)
            SettingSwitchRow("Full-Screen Desktop (Hide Android Top Bar)", preferences.hideSystemStatusBar, onSetHideStatusBar)
        }

        // Taskbar Alignment & Icon Size
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskbarAlignment.entries.forEach { align ->
                val selected = preferences.taskbarAlignment == align
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) winColors.accentSoft else winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                        .clickable { onSetTaskbarAlignment(align) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Taskbar: ${align.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else winColors.textPrimary
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DesktopIconSize.entries.forEach { size ->
                val selected = preferences.desktopIconSize == size
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) winColors.accentSoft else winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp))
                        .clickable { onSetDesktopIconSize(size) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = size.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else winColors.textPrimary
                    )
                }
            }
        }

        // Profile Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nameDraft,
                onValueChange = { nameDraft = it },
                label = { Text("Account Name") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { onSetUserName(nameDraft) },
                colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft)
            ) {
                Text("Save")
            }
        }

        OutlinedButton(onClick = onRestoreDefaults, modifier = Modifier.fillMaxWidth()) {
            Text("Restore Default Desktop Icons")
        }
    }
}

@Composable
private fun WallpaperPresetCard(
    label: String,
    drawableRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) winColors.accent else winColors.border,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick)
        ) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(18.dp)
                )
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = winColors.textPrimary)
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val winColors = LocalWin11Colors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = winColors.textPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = winColors.accentSoft
            )
        )
    }
}

@Composable
fun AppManagerWindowContent(
    apps: List<InstalledApp>,
    desktopPackages: List<String>,
    onLaunchApp: (InstalledApp) -> Unit,
    onToggleDesktop: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit
) {
    val winColors = LocalWin11Colors.current
    var filter by remember { mutableStateOf("") }
    val filtered = remember(apps, filter) {
        if (filter.isBlank()) apps else apps.filter { it.label.contains(filter, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            placeholder = { Text("Filter installed apps...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filtered, key = { "${it.packageName}/${it.activityName}" }) { app ->
                val isOnDesktop = desktopPackages.contains(app.packageName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                        .clickable { onLaunchApp(app) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppIconImage(app = app, size = 32.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            app.label,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = winColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            app.packageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = winColors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onToggleDesktop(app.packageName) }) {
                        Icon(
                            Icons.Rounded.PushPin,
                            contentDescription = "Add or remove from Desktop",
                            tint = if (isOnDesktop) winColors.accent else winColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { onOpenAppInfo(app.packageName) }) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = "App info & uninstall",
                            tint = winColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
