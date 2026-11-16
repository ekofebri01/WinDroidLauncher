package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DesktopIconSize
import com.example.model.LauncherPreferences
import com.example.model.TaskbarAlignment
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
            use24HourClock = prefs[Keys.USE_24_HOUR_CLOCK] ?: true,
            showSecondsInClock = prefs[Keys.SHOW_SECONDS_CLOCK] ?: false,
            hideSystemStatusBar = prefs[Keys.HIDE_SYSTEM_STATUS_BAR] ?: false,
            userName = prefs[Keys.USER_NAME]?.takeIf { it.isNotBlank() } ?: "Administrator",
            pinnedStartPackages = decodeList(prefs[Keys.PINNED_START_PACKAGES]),
            pinnedTaskbarPackages = decodeList(prefs[Keys.PINNED_TASKBAR_PACKAGES]),
            desktopAppPackages = decodeList(prefs[Keys.DESKTOP_APP_PACKAGES]),
            recentAppPackages = decodeList(prefs[Keys.RECENT_APP_PACKAGES]),
            hiddenDefaultShortcuts = prefs[Keys.HIDDEN_DEFAULT_SHORTCUTS] ?: emptySet(),
            sortAlphabetically = prefs[Keys.SORT_ALPHABETICALLY] ?: false
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

    private fun decodeList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split("|").filter { it.isNotBlank() }
    }

    private fun encodeList(items: List<String>): String =
        items.distinct().joinToString("|")
}
