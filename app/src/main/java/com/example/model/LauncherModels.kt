package com.example.model

import android.appwidget.AppWidgetProviderInfo
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

@Immutable
data class DesktopWidget(
    val appWidgetId: Int,
    val providerPackage: String,
    val providerClass: String,
    val label: String,
    val xDp: Float = 24f,
    val yDp: Float = 24f,
    val widthDp: Float = 240f,
    val heightDp: Float = 150f,
    val minWidthDp: Int = 96,
    val minHeightDp: Int = 72
)

@Immutable
data class AppWidgetItem(
    val providerPackage: String,
    val providerClass: String,
    val appName: String,
    val widgetLabel: String,
    val defaultWidthDp: Float,
    val defaultHeightDp: Float,
    val minWidthDp: Int,
    val minHeightDp: Int,
    val previewBitmap: ImageBitmap? = null,
    val appIcon: ImageBitmap? = null,
    val providerInfo: AppWidgetProviderInfo? = null
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

enum class TaskbarStyle(val label: String) {
    DOCKED("Docked"),
    FLOATING("Floating")
}

enum class TaskbarIconSize(val label: String, val iconDp: Int, val appIconDp: Int) {
    SMALL("Small", 18, 22),
    MEDIUM("Medium", 22, 26),
    LARGE("Large", 26, 30)
}

enum class TaskbarSearchMode(val label: String) {
    AUTO("Auto"),
    ICON_ONLY("Icon"),
    SEARCH_BOX("Search Box"),
    HIDDEN("Hidden")
}

enum class ActivePanel {
    NONE,
    START_MENU,
    SEARCH,
    QUICK_SETTINGS,
    CALENDAR_NOTIFICATIONS,
    TASK_VIEW,
    HIDDEN_TRAY
}

enum class DesktopWindowType(val defaultTitle: String) {
    FILE_EXPLORER("File Explorer"),
    THIS_PC("This PC"),
    USER_FILES("User Files"),
    SETTINGS_PERSONALIZE("Settings — Personalization"),
    TASKBAR_PANEL_SETTINGS("Taskbar & Panel Settings"),
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
    val taskbarHeightDp: Int = 54,
    val taskbarStyle: TaskbarStyle = TaskbarStyle.DOCKED,
    val taskbarIconSize: TaskbarIconSize = TaskbarIconSize.MEDIUM,
    val taskbarOpacity: Float = 0.90f,
    val taskbarShowDivider: Boolean = true,
    val taskbarShowActiveIndicator: Boolean = true,
    val taskbarSearchMode: TaskbarSearchMode = TaskbarSearchMode.AUTO,
    val showTaskViewInTaskbar: Boolean = true,
    val showFileExplorerInTaskbar: Boolean = true,
    val showPinnedAppsInTaskbar: Boolean = true,
    val showHiddenTrayInTaskbar: Boolean = true,
    val showTrayNetwork: Boolean = true,
    val showTrayVolume: Boolean = true,
    val showTrayBattery: Boolean = true,
    val showTrayBatteryPercent: Boolean = true,
    val showTrayClock: Boolean = true,
    val showTrayDate: Boolean = true,
    val showTrayNotificationDot: Boolean = true,
    val showDesktopButtonInTaskbar: Boolean = true,
    val use24HourClock: Boolean = true,
    val showSecondsInClock: Boolean = false,
    val hideSystemStatusBar: Boolean = false,
    val hideSystemNavBar: Boolean = false,
    val userName: String = "Administrator",
    val pinnedStartPackages: List<String> = emptyList(),
    val pinnedTaskbarPackages: List<String> = emptyList(),
    val desktopAppPackages: List<String> = emptyList(),
    val recentAppPackages: List<String> = emptyList(),
    val hiddenDefaultShortcuts: Set<String> = emptySet(),
    val sortAlphabetically: Boolean = false,
    val safeHomeGuardEnabled: Boolean = true,
    val githubRepoSlug: String = "",
    val autoCheckUpdates: Boolean = true,
    val desktopShortcutOrder: List<String> = emptyList(),
    val desktopWidgets: List<DesktopWidget> = emptyList()
)

@Immutable
data class HomeLauncherStatus(
    val isDefaultHome: Boolean = false,
    val currentDefaultHomeLabel: String = "System Launcher",
    val currentDefaultHomePackage: String? = null,
    val otherHomeLaunchers: List<InstalledApp> = emptyList()
)

enum class GitHubUpdateStatus {
    IDLE,
    CHECKING,
    UPDATE_AVAILABLE,
    UP_TO_DATE,
    NOT_CONFIGURED,
    ERROR
}

enum class InAppUpdateStage {
    NONE,
    PERMISSION_REQUIRED,
    DOWNLOADING,
    VERIFYING,
    INSTALLING,
    CLEANING_UP,
    INSTALLED_SUCCESS,
    FAILED
}

@Immutable
data class GitHubUpdateState(
    val status: GitHubUpdateStatus = GitHubUpdateStatus.IDLE,
    val currentVersion: String = "1.0",
    val latestTag: String? = null,
    val releaseTitle: String? = null,
    val releaseNotes: String? = null,
    val releaseUrl: String? = null,
    val apkAssetUrl: String? = null,
    val apkSizeBytes: Long = 0L,
    val publishedAt: String? = null,
    val errorMessage: String? = null,
    val lastCheckedMillis: Long? = null,
    val inAppStage: InAppUpdateStage = InAppUpdateStage.NONE,
    val downloadProgress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val installProgress: Float = 0f,
    val downloadedApkPath: String? = null,
    val cleanedUpAfterInstall: Boolean = false
)

@Immutable
data class ContextMenuRequest(
    val shortcut: DesktopShortcut? = null,
    val app: InstalledApp? = null,
    val isDesktopBackground: Boolean = false
)
