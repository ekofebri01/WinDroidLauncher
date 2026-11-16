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
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.DesktopIconSize
import com.example.model.GitHubUpdateState
import com.example.model.GitHubUpdateStatus
import com.example.model.HomeLauncherStatus
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
    homeLauncherStatus: HomeLauncherStatus,
    githubUpdateState: GitHubUpdateState,
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
    onRequestSafeHomeSwitch: () -> Unit,
    onSetSafeHomeGuard: (Boolean) -> Unit,
    onLaunchFallbackHome: (InstalledApp?) -> Unit,
    onSaveAndCheckGitHubRepo: (String) -> Unit,
    onSetAutoCheckUpdates: (Boolean) -> Unit,
    onCheckGitHubUpdates: () -> Unit,
    onOpenGitHubRelease: (Boolean) -> Unit,
    onStartInAppUpdate: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    var nameDraft by remember(preferences.userName) { mutableStateOf(preferences.userName) }
    var repoDraft by remember(preferences.githubRepoSlug) { mutableStateOf(preferences.githubRepoSlug) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Default Home Launcher (Safe Mode) Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(winColors.accentSoft.copy(alpha = 0.14f))
                .border(1.dp, winColors.accent.copy(alpha = 0.48f), RoundedCornerShape(10.dp))
                .padding(12.dp)
                .testTag("settings_home_launcher_card"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = if (homeLauncherStatus.isDefaultHome) Color(0xFF4CD4A9) else winColors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (homeLauncherStatus.isDefaultHome) {
                                "Aktif sebagai Home Default"
                            } else {
                                "Gantikan Home Launcher (Aman)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = winColors.textPrimary
                        )
                    }
                    Text(
                        text = if (homeLauncherStatus.isDefaultHome) {
                            "Win11 Launcher aktif sebagai beranda utama. Anda tetap bisa kembali ke launcher bawaan kapan saja."
                        } else {
                            "Home aktif saat ini: ${homeLauncherStatus.currentDefaultHomeLabel}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRequestSafeHomeSwitch,
                    colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                    modifier = Modifier.weight(1f).testTag("settings_set_default_home")
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (homeLauncherStatus.isDefaultHome) "Ubah Home" else "Jadikan Default")
                }

                val stockLauncher = homeLauncherStatus.otherHomeLaunchers.firstOrNull()
                if (stockLauncher != null) {
                    OutlinedButton(
                        onClick = { onLaunchFallbackHome(stockLauncher) },
                        modifier = Modifier.weight(1f).testTag("settings_open_stock_home")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Launcher Asli",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            SettingSwitchRow(
                title = "Mode Aman (Konfirmasi & Tombol Kembali ke Launcher Asli)",
                checked = preferences.safeHomeGuardEnabled,
                onCheckedChange = onSetSafeHomeGuard
            )
        }

        // Windows Update (GitHub Releases Detector) Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(winColors.cardSurface)
                .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
                .testTag("settings_github_update_card"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = if (githubUpdateState.status == GitHubUpdateStatus.UPDATE_AVAILABLE) {
                            Color(0xFF4CD4A9)
                        } else {
                            winColors.accent
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Windows Update (GitHub)",
                            style = MaterialTheme.typography.titleSmall,
                            color = winColors.textPrimary
                        )
                        Text(
                            text = "Versi terpasang: v${githubUpdateState.currentVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = winColors.textSecondary
                        )
                    }
                }
                if (githubUpdateState.status == GitHubUpdateStatus.CHECKING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = winColors.accent
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = repoDraft,
                    onValueChange = { repoDraft = it },
                    label = { Text("Repo GitHub (owner/repo)") },
                    placeholder = { Text("contoh: user/win11-launcher") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("settings_github_repo_input")
                )
                Button(
                    onClick = {
                        if (repoDraft.isNotBlank()) onSaveAndCheckGitHubRepo(repoDraft)
                        else onCheckGitHubUpdates()
                    },
                    enabled = githubUpdateState.status != GitHubUpdateStatus.CHECKING,
                    colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                    modifier = Modifier.testTag("settings_check_github_update")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cek")
                }
            }

            SettingSwitchRow(
                title = "Cek update GitHub otomatis saat dibuka",
                checked = preferences.autoCheckUpdates,
                onCheckedChange = onSetAutoCheckUpdates
            )

            when (githubUpdateState.status) {
                GitHubUpdateStatus.UPDATE_AVAILABLE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.14f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Update baru tersedia: ${githubUpdateState.latestTag ?: ""}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF4CD4A9)
                        )
                        githubUpdateState.releaseTitle?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = winColors.textPrimary)
                        }
                        githubUpdateState.releaseNotes?.let { notes ->
                            Text(
                                text = notes,
                                style = MaterialTheme.typography.labelSmall,
                                color = winColors.textSecondary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (githubUpdateState.apkSizeBytes > 0L) {
                            Text(
                                text = "Paket APK tersedia (${formatExplorerBytes(githubUpdateState.apkSizeBytes, false)}) • Instalasi langsung di aplikasi",
                                style = MaterialTheme.typography.labelSmall,
                                color = winColors.textSecondary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (githubUpdateState.apkAssetUrl != null) {
                                Button(
                                    onClick = onStartInAppUpdate,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.testTag("settings_in_app_update_button")
                                ) {
                                    Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Update di App")
                                }
                                OutlinedButton(
                                    onClick = { onOpenGitHubRelease(false) },
                                    modifier = Modifier.testTag("settings_open_github_release")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Release")
                                }
                            } else {
                                Button(
                                    onClick = { onOpenGitHubRelease(false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                                    modifier = Modifier.testTag("settings_open_github_release")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Buka Release")
                                }
                            }
                        }
                    }
                }
                GitHubUpdateStatus.UP_TO_DATE -> {
                    Text(
                        text = "✓ Sudah versi terbaru (GitHub: ${githubUpdateState.latestTag ?: githubUpdateState.currentVersion})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CD4A9)
                    )
                }
                GitHubUpdateStatus.NOT_CONFIGURED, GitHubUpdateStatus.ERROR -> {
                    githubUpdateState.errorMessage?.let { err ->
                        Text(
                            text = err,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF8A80)
                        )
                    }
                }
                GitHubUpdateStatus.CHECKING -> {
                    Text(
                        text = "Memeriksa rilis terbaru di GitHub...",
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }
                GitHubUpdateStatus.IDLE -> Unit
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

@Composable
fun SafeHomeChooserDialog(
    status: HomeLauncherStatus,
    onDismiss: () -> Unit,
    onConfirmSystemHomeChooser: () -> Unit,
    onOpenFallbackLauncher: (InstalledApp) -> Unit
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(14.dp)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = shape,
            color = winColors.micaSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, winColors.border, shape)
                .testTag("safe_home_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = winColors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Ganti Home Launcher (Aman)",
                            style = MaterialTheme.typography.titleMedium,
                            color = winColors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = winColors.textPrimary)
                    }
                }

                HorizontalDivider(color = winColors.subtleBorder)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(winColors.cardSurface)
                        .border(1.dp, winColors.subtleBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Status Saat Ini: ${status.currentDefaultHomeLabel}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textPrimary
                    )
                    Text(
                        text = "• Launcher bawaan HP Anda tetap aman dan tidak dihapus.\n• Anda bisa kembali kapan saja lewat Start → Power → Default Home App atau membuka launcher asli langsung dari sini.",
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }

                Button(
                    onClick = onConfirmSystemHomeChooser,
                    colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                    modifier = Modifier.fillMaxWidth().testTag("safe_home_confirm_button")
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (status.isDefaultHome) "Ubah Pengaturan Home Sistem" else "Pilih Win11 sebagai Home Default")
                }

                if (status.otherHomeLaunchers.isNotEmpty()) {
                    Text(
                        text = "Atau beralih cepat ke launcher lain:",
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                    status.otherHomeLaunchers.take(2).forEach { homeApp ->
                        OutlinedButton(
                            onClick = { onOpenFallbackLauncher(homeApp) },
                            modifier = Modifier.fillMaxWidth().testTag("safe_home_fallback_${homeApp.packageName}")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka ${homeApp.label}")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("safe_home_preview_button")
                    ) {
                        Text("Tetap Mode Preview", color = winColors.textSecondary)
                    }
                }
            }
        }
    }
}
