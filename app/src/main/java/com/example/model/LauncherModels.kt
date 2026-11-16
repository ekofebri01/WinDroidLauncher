package com.example.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

@Immutable
data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: ImageBitmap?,
    val isSystemApp: Boolean = false
)

enum class ShortcutKind {
    THIS_PC,
    USER_FILES,
    BROWSER,
    SETTINGS,
    RECYCLE_BIN,
    APP
}

@Immutable
data class DesktopShortcut(
    val id: String,
    val label: String,
    val kind: ShortcutKind,
    val packageName: String? = null,
    val app: InstalledApp? = null
)

enum class WallpaperOption(val displayName: String) {
    BLOOM_DARK("Bloom Dark"),
    BLOOM_LIGHT("Bloom Light"),
    CAPTURED_FLOW("Captured Flow"),
    CUSTOM_URI("Custom Photo")
}

enum class DesktopIconSize(val label: String, val iconDp: Int, val cellWidthDp: Int, val cellHeightDp: Int) {
    SMALL("Small icons", 38, 76, 84),
    MEDIUM("Medium icons", 46, 86, 94),
    LARGE("Large icons", 56, 98, 106)
}

enum class TaskbarAlignment(val label: String) {
    CENTER("Center"),
    LEFT("Left")
}

enum class ActivePanel {
    NONE,
    START_MENU,
    SEARCH,
    QUICK_SETTINGS,
    CALENDAR_NOTIFICATIONS,
    WIDGETS,
    TASK_VIEW,
    HIDDEN_TRAY
}

enum class DesktopWindowType(val defaultTitle: String) {
    FILE_EXPLORER("File Explorer"),
    THIS_PC("This PC"),
    USER_FILES("User Files"),
    SETTINGS_PERSONALIZE("Settings — Personalization"),
    RECYCLE_BIN_APPS("Installed Apps Manager")
}

@Immutable
data class DesktopWindowState(
    val type: DesktopWindowType,
    val title: String = type.defaultTitle,
    val isMinimized: Boolean = false,
    val isMaximized: Boolean = false
)

@Immutable
data class SystemTelemetry(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val isWifiConnected: Boolean = true,
    val isCellularConnected: Boolean = false,
    val networkLabel: String = "Wi-Fi",
    val mediaVolume: Int = 7,
    val maxMediaVolume: Int = 15,
    val isMuted: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val totalRamMb: Long = 8192L,
    val availableRamMb: Long = 4096L,
    val totalStorageGb: Float = 128f,
    val usedStorageGb: Float = 42f,
    val deviceModel: String = "Android PC",
    val androidVersion: String = "15"
)

@Immutable
data class LauncherPreferences(
    val isDarkTheme: Boolean = true,
    val wallpaperOption: WallpaperOption = WallpaperOption.BLOOM_DARK,
    val customWallpaperUri: String? = null,
    val desktopIconSize: DesktopIconSize = DesktopIconSize.MEDIUM,
    val taskbarAlignment: TaskbarAlignment = TaskbarAlignment.CENTER,
    val use24HourClock: Boolean = true,
    val showSecondsInClock: Boolean = false,
    val hideSystemStatusBar: Boolean = false,
    val userName: String = "Administrator",
    val pinnedStartPackages: List<String> = emptyList(),
    val pinnedTaskbarPackages: List<String> = emptyList(),
    val desktopAppPackages: List<String> = emptyList(),
    val recentAppPackages: List<String> = emptyList(),
    val hiddenDefaultShortcuts: Set<String> = emptySet(),
    val sortAlphabetically: Boolean = false
)

@Immutable
data class ContextMenuRequest(
    val shortcut: DesktopShortcut? = null,
    val app: InstalledApp? = null,
    val isDesktopBackground: Boolean = false
)
