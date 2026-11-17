package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DesktopIconSize
import com.example.model.LauncherPreferences
import com.example.model.TaskbarAlignment
import com.example.model.TaskbarIconSize
import com.example.model.TaskbarSearchMode
import com.example.model.TaskbarStyle
import com.example.model.WallpaperOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "win11_launcher_prefs")

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val WALLPAPER_OPTION = stringPreferencesKey("wallpaper_option")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val DESKTOP_ICON_SIZE = stringPreferencesKey("desktop_icon_size")
        val TASKBAR_ALIGNMENT = stringPreferencesKey("taskbar_alignment")
        val TASKBAR_HEIGHT_DP = intPreferencesKey("taskbar_height_dp")
        val TASKBAR_STYLE = stringPreferencesKey("taskbar_style")
        val TASKBAR_ICON_SIZE = stringPreferencesKey("taskbar_icon_size")
        val TASKBAR_OPACITY = floatPreferencesKey("taskbar_opacity")
        val TASKBAR_SHOW_DIVIDER = booleanPreferencesKey("taskbar_show_divider")
        val TASKBAR_SHOW_ACTIVE_INDICATOR = booleanPreferencesKey("taskbar_show_active_indicator")
        val TASKBAR_SEARCH_MODE = stringPreferencesKey("taskbar_search_mode")
        val SHOW_TASK_VIEW_IN_TASKBAR = booleanPreferencesKey("show_task_view_in_taskbar")
        val SHOW_FILE_EXPLORER_IN_TASKBAR = booleanPreferencesKey("show_file_explorer_in_taskbar")
        val SHOW_PINNED_APPS_IN_TASKBAR = booleanPreferencesKey("show_pinned_apps_in_taskbar")
        val SHOW_HIDDEN_TRAY_IN_TASKBAR = booleanPreferencesKey("show_hidden_tray_in_taskbar")
        val SHOW_TRAY_NETWORK = booleanPreferencesKey("show_tray_network")
        val SHOW_TRAY_VOLUME = booleanPreferencesKey("show_tray_volume")
        val SHOW_TRAY_BATTERY = booleanPreferencesKey("show_tray_battery")
        val SHOW_TRAY_BATTERY_PERCENT = booleanPreferencesKey("show_tray_battery_percent")
        val SHOW_TRAY_CLOCK = booleanPreferencesKey("show_tray_clock")
        val SHOW_TRAY_DATE = booleanPreferencesKey("show_tray_date")
        val SHOW_TRAY_NOTIFICATION_DOT = booleanPreferencesKey("show_tray_notification_dot")
        val SHOW_DESKTOP_BUTTON_IN_TASKBAR = booleanPreferencesKey("show_desktop_button_in_taskbar")
        val USE_24_HOUR_CLOCK = booleanPreferencesKey("use_24_hour_clock")
        val SHOW_SECONDS_CLOCK = booleanPreferencesKey("show_seconds_clock")
        val HIDE_SYSTEM_STATUS_BAR = booleanPreferencesKey("hide_system_status_bar")
        val USER_NAME = stringPreferencesKey("user_name")
        val PINNED_START_PACKAGES = stringPreferencesKey("pinned_start_packages")
        val PINNED_TASKBAR_PACKAGES = stringPreferencesKey("pinned_taskbar_packages")
        val DESKTOP_APP_PACKAGES = stringPreferencesKey("desktop_app_packages")
        val RECENT_APP_PACKAGES = stringPreferencesKey("recent_app_packages")
        val HIDDEN_DEFAULT_SHORTCUTS = stringSetPreferencesKey("hidden_default_shortcuts")
        val SORT_ALPHABETICALLY = booleanPreferencesKey("sort_alphabetically")
        val SAFE_HOME_GUARD = booleanPreferencesKey("safe_home_guard")
        val GITHUB_REPO_SLUG = stringPreferencesKey("github_repo_slug")
        val AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        val PENDING_UPDATE_CLEANUP_PATH = stringPreferencesKey("pending_update_cleanup_path")
        val DESKTOP_SHORTCUT_ORDER = stringPreferencesKey("desktop_shortcut_order")
    }

    val preferencesFlow: Flow<LauncherPreferences> = context.launcherDataStore.data.map { prefs ->
        LauncherPreferences(
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
            wallpaperOption = prefs[Keys.WALLPAPER_OPTION]?.let { name ->
                WallpaperOption.entries.firstOrNull { it.name == name }
            } ?: WallpaperOption.BLOOM_DARK,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI],
            desktopIconSize = prefs[Keys.DESKTOP_ICON_SIZE]?.let { name ->
                DesktopIconSize.entries.firstOrNull { it.name == name }
            } ?: DesktopIconSize.MEDIUM,
            taskbarAlignment = prefs[Keys.TASKBAR_ALIGNMENT]?.let { name ->
                TaskbarAlignment.entries.firstOrNull { it.name == name }
            } ?: TaskbarAlignment.CENTER,
            taskbarHeightDp = (prefs[Keys.TASKBAR_HEIGHT_DP] ?: 54).coerceIn(44, 72),
            taskbarStyle = prefs[Keys.TASKBAR_STYLE]?.let { name ->
                TaskbarStyle.entries.firstOrNull { it.name == name }
            } ?: TaskbarStyle.DOCKED,
            taskbarIconSize = prefs[Keys.TASKBAR_ICON_SIZE]?.let { name ->
                TaskbarIconSize.entries.firstOrNull { it.name == name }
            } ?: TaskbarIconSize.MEDIUM,
            taskbarOpacity = (prefs[Keys.TASKBAR_OPACITY] ?: 0.90f).coerceIn(0.45f, 1.0f),
            taskbarShowDivider = prefs[Keys.TASKBAR_SHOW_DIVIDER] ?: true,
            taskbarShowActiveIndicator = prefs[Keys.TASKBAR_SHOW_ACTIVE_INDICATOR] ?: true,
            taskbarSearchMode = prefs[Keys.TASKBAR_SEARCH_MODE]?.let { name ->
                TaskbarSearchMode.entries.firstOrNull { it.name == name }
            } ?: TaskbarSearchMode.AUTO,
            showTaskViewInTaskbar = prefs[Keys.SHOW_TASK_VIEW_IN_TASKBAR] ?: true,
            showFileExplorerInTaskbar = prefs[Keys.SHOW_FILE_EXPLORER_IN_TASKBAR] ?: true,
            showPinnedAppsInTaskbar = prefs[Keys.SHOW_PINNED_APPS_IN_TASKBAR] ?: true,
            showHiddenTrayInTaskbar = prefs[Keys.SHOW_HIDDEN_TRAY_IN_TASKBAR] ?: true,
            showTrayNetwork = prefs[Keys.SHOW_TRAY_NETWORK] ?: true,
            showTrayVolume = prefs[Keys.SHOW_TRAY_VOLUME] ?: true,
            showTrayBattery = prefs[Keys.SHOW_TRAY_BATTERY] ?: true,
            showTrayBatteryPercent = prefs[Keys.SHOW_TRAY_BATTERY_PERCENT] ?: true,
            showTrayClock = prefs[Keys.SHOW_TRAY_CLOCK] ?: true,
            showTrayDate = prefs[Keys.SHOW_TRAY_DATE] ?: true,
            showTrayNotificationDot = prefs[Keys.SHOW_TRAY_NOTIFICATION_DOT] ?: true,
            showDesktopButtonInTaskbar = prefs[Keys.SHOW_DESKTOP_BUTTON_IN_TASKBAR] ?: true,
            use24HourClock = prefs[Keys.USE_24_HOUR_CLOCK] ?: true,
            showSecondsInClock = prefs[Keys.SHOW_SECONDS_CLOCK] ?: false,
            hideSystemStatusBar = prefs[Keys.HIDE_SYSTEM_STATUS_BAR] ?: false,
            userName = prefs[Keys.USER_NAME]?.takeIf { it.isNotBlank() } ?: "Administrator",
            pinnedStartPackages = decodeList(prefs[Keys.PINNED_START_PACKAGES]),
            pinnedTaskbarPackages = decodeList(prefs[Keys.PINNED_TASKBAR_PACKAGES]),
            desktopAppPackages = decodeList(prefs[Keys.DESKTOP_APP_PACKAGES]),
            recentAppPackages = decodeList(prefs[Keys.RECENT_APP_PACKAGES]),
            hiddenDefaultShortcuts = prefs[Keys.HIDDEN_DEFAULT_SHORTCUTS] ?: emptySet(),
            sortAlphabetically = prefs[Keys.SORT_ALPHABETICALLY] ?: false,
            safeHomeGuardEnabled = prefs[Keys.SAFE_HOME_GUARD] ?: true,
            githubRepoSlug = prefs[Keys.GITHUB_REPO_SLUG] ?: "",
            autoCheckUpdates = prefs[Keys.AUTO_CHECK_UPDATES] ?: true,
            desktopShortcutOrder = decodeList(prefs[Keys.DESKTOP_SHORTCUT_ORDER])
        )
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.launcherDataStore.edit { it[Keys.IS_DARK_THEME] = isDark }
    }

    suspend fun setWallpaper(option: WallpaperOption, customUri: String? = null) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.WALLPAPER_OPTION] = option.name
            if (customUri != null) {
                prefs[Keys.CUSTOM_WALLPAPER_URI] = customUri
            }
            if (option == WallpaperOption.BLOOM_LIGHT) {
                prefs[Keys.IS_DARK_THEME] = false
            } else if (option == WallpaperOption.BLOOM_DARK) {
                prefs[Keys.IS_DARK_THEME] = true
            }
        }
    }

    suspend fun setDesktopIconSize(size: DesktopIconSize) {
        context.launcherDataStore.edit { it[Keys.DESKTOP_ICON_SIZE] = size.name }
    }

    suspend fun setTaskbarAlignment(alignment: TaskbarAlignment) {
        context.launcherDataStore.edit { it[Keys.TASKBAR_ALIGNMENT] = alignment.name }
    }

    suspend fun setTaskbarHeightDp(heightDp: Int) {
        context.launcherDataStore.edit { it[Keys.TASKBAR_HEIGHT_DP] = heightDp.coerceIn(44, 72) }
    }

    suspend fun setTaskbarStyle(style: TaskbarStyle) {
        context.launcherDataStore.edit { it[Keys.TASKBAR_STYLE] = style.name }
    }

    suspend fun setTaskbarIconSize(iconSize: TaskbarIconSize) {
        context.launcherDataStore.edit { it[Keys.TASKBAR_ICON_SIZE] = iconSize.name }
    }

    suspend fun setTaskbarOpacity(opacity: Float) {
        context.launcherDataStore.edit { it[Keys.TASKBAR_OPACITY] = opacity.coerceIn(0.45f, 1.0f) }
    }

    suspend fun setTaskbarShowDivider(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.TASKBAR_SHOW_DIVIDER] = show }
    }

    suspend fun setTaskbarShowActiveIndicator(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.TASKBAR_SHOW_ACTIVE_INDICATOR] = show }
    }

    suspend fun setTaskbarSearchMode(mode: TaskbarSearchMode) {
        context.launcherDataStore.edit { it[Keys.TASKBAR_SEARCH_MODE] = mode.name }
    }

    suspend fun setShowTaskViewInTaskbar(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_TASK_VIEW_IN_TASKBAR] = show }
    }

    suspend fun setShowFileExplorerInTaskbar(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_FILE_EXPLORER_IN_TASKBAR] = show }
    }

    suspend fun setShowPinnedAppsInTaskbar(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_PINNED_APPS_IN_TASKBAR] = show }
    }

    suspend fun setShowHiddenTrayInTaskbar(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_HIDDEN_TRAY_IN_TASKBAR] = show }
    }

    suspend fun setShowTrayNetwork(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_TRAY_NETWORK] = show }
    }

    suspend fun setShowTrayVolume(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_TRAY_VOLUME] = show }
    }

    suspend fun setShowTrayBattery(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_TRAY_BATTERY] = show }
    }

    suspend fun setShowTrayBatteryPercent(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_TRAY_BATTERY_PERCENT] = show }
    }

    suspend fun setShowTrayClock(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_TRAY_CLOCK] = show }
    }

    suspend fun setShowTrayDate(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_TRAY_DATE] = show }
    }

    suspend fun setShowTrayNotificationDot(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_TRAY_NOTIFICATION_DOT] = show }
    }

    suspend fun setShowDesktopButtonInTaskbar(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_DESKTOP_BUTTON_IN_TASKBAR] = show }
    }

    suspend fun resetTaskbarSettingsToDefault() {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.TASKBAR_ALIGNMENT] = TaskbarAlignment.CENTER.name
            prefs[Keys.TASKBAR_HEIGHT_DP] = 54
            prefs[Keys.TASKBAR_STYLE] = TaskbarStyle.DOCKED.name
            prefs[Keys.TASKBAR_ICON_SIZE] = TaskbarIconSize.MEDIUM.name
            prefs[Keys.TASKBAR_OPACITY] = 0.90f
            prefs[Keys.TASKBAR_SHOW_DIVIDER] = true
            prefs[Keys.TASKBAR_SHOW_ACTIVE_INDICATOR] = true
            prefs[Keys.TASKBAR_SEARCH_MODE] = TaskbarSearchMode.AUTO.name
            prefs[Keys.SHOW_TASK_VIEW_IN_TASKBAR] = true
            prefs[Keys.SHOW_FILE_EXPLORER_IN_TASKBAR] = true
            prefs[Keys.SHOW_PINNED_APPS_IN_TASKBAR] = true
            prefs[Keys.SHOW_HIDDEN_TRAY_IN_TASKBAR] = true
            prefs[Keys.SHOW_TRAY_NETWORK] = true
            prefs[Keys.SHOW_TRAY_VOLUME] = true
            prefs[Keys.SHOW_TRAY_BATTERY] = true
            prefs[Keys.SHOW_TRAY_BATTERY_PERCENT] = true
            prefs[Keys.SHOW_TRAY_CLOCK] = true
            prefs[Keys.SHOW_TRAY_DATE] = true
            prefs[Keys.SHOW_TRAY_NOTIFICATION_DOT] = true
            prefs[Keys.SHOW_DESKTOP_BUTTON_IN_TASKBAR] = true
        }
    }

    suspend fun setUse24HourClock(use24h: Boolean) {
        context.launcherDataStore.edit { it[Keys.USE_24_HOUR_CLOCK] = use24h }
    }

    suspend fun setShowSecondsInClock(showSeconds: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_SECONDS_CLOCK] = showSeconds }
    }

    suspend fun setHideSystemStatusBar(hide: Boolean) {
        context.launcherDataStore.edit { it[Keys.HIDE_SYSTEM_STATUS_BAR] = hide }
    }

    suspend fun setUserName(name: String) {
        context.launcherDataStore.edit {
            it[Keys.USER_NAME] = name.trim().ifEmpty { "Administrator" }
        }
    }

    suspend fun setSortAlphabetically(sort: Boolean) {
        context.launcherDataStore.edit { it[Keys.SORT_ALPHABETICALLY] = sort }
    }

    suspend fun setSafeHomeGuard(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SAFE_HOME_GUARD] = enabled }
    }

    suspend fun setGitHubRepoSlug(slug: String) {
        context.launcherDataStore.edit { it[Keys.GITHUB_REPO_SLUG] = slug }
    }

    suspend fun setAutoCheckUpdates(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.AUTO_CHECK_UPDATES] = enabled }
    }

    val pendingUpdateCleanupFlow: Flow<String?> = context.launcherDataStore.data.map { prefs ->
        prefs[Keys.PENDING_UPDATE_CLEANUP_PATH]
    }

    suspend fun setPendingUpdateCleanupPath(path: String?) {
        context.launcherDataStore.edit { prefs ->
            if (path.isNullOrBlank()) prefs.remove(Keys.PENDING_UPDATE_CLEANUP_PATH)
            else prefs[Keys.PENDING_UPDATE_CLEANUP_PATH] = path
        }
    }

    suspend fun togglePinToStart(packageName: String, fallbackSeed: List<String>) {
        context.launcherDataStore.edit { prefs ->
            val current = decodeList(prefs[Keys.PINNED_START_PACKAGES]).ifEmpty { fallbackSeed }.toMutableList()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(0, packageName)
            }
            prefs[Keys.PINNED_START_PACKAGES] = encodeList(current)
        }
    }

    suspend fun togglePinToTaskbar(packageName: String, fallbackSeed: List<String>) {
        context.launcherDataStore.edit { prefs ->
            val current = decodeList(prefs[Keys.PINNED_TASKBAR_PACKAGES]).ifEmpty { fallbackSeed }.toMutableList()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                if (current.size >= 6) current.removeAt(current.lastIndex)
                current.add(packageName)
            }
            prefs[Keys.PINNED_TASKBAR_PACKAGES] = encodeList(current)
        }
    }

    suspend fun toggleDesktopApp(packageName: String) {
        context.launcherDataStore.edit { prefs ->
            val current = decodeList(prefs[Keys.DESKTOP_APP_PACKAGES]).toMutableList()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[Keys.DESKTOP_APP_PACKAGES] = encodeList(current)
        }
    }

    suspend fun hideDefaultShortcut(shortcutId: String) {
        context.launcherDataStore.edit { prefs ->
            val current = (prefs[Keys.HIDDEN_DEFAULT_SHORTCUTS] ?: emptySet()).toMutableSet()
            current.add(shortcutId)
            prefs[Keys.HIDDEN_DEFAULT_SHORTCUTS] = current
        }
    }

    suspend fun restoreDefaultShortcuts() {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.HIDDEN_DEFAULT_SHORTCUTS] = emptySet()
        }
    }

    suspend fun recordRecentApp(packageName: String) {
        context.launcherDataStore.edit { prefs ->
            val current = decodeList(prefs[Keys.RECENT_APP_PACKAGES]).toMutableList()
            current.remove(packageName)
            current.add(0, packageName)
            prefs[Keys.RECENT_APP_PACKAGES] = encodeList(current.take(8))
        }
    }

    suspend fun setDesktopShortcutOrder(order: List<String>) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.DESKTOP_SHORTCUT_ORDER] = encodeList(order)
        }
    }

    private fun decodeList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split("|").filter { it.isNotBlank() }
    }

    private fun encodeList(items: List<String>): String =
        items.distinct().joinToString("|")
}
