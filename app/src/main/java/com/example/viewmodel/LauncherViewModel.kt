package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LauncherPreferencesRepository
import com.example.data.SystemRepository
import com.example.model.ActivePanel
import com.example.model.ContextMenuRequest
import com.example.model.DesktopIconSize
import com.example.model.DesktopShortcut
import com.example.model.DesktopWindowState
import com.example.model.DesktopWindowType
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.model.ShortcutKind
import com.example.model.SystemTelemetry
import com.example.model.TaskbarAlignment
import com.example.model.WallpaperOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class LauncherUiState(
    val preferences: LauncherPreferences = LauncherPreferences(),
    val installedApps: List<InstalledApp> = emptyList(),
    val isLoadingApps: Boolean = true,
    val desktopShortcuts: List<DesktopShortcut> = emptyList(),
    val pinnedStartApps: List<InstalledApp> = emptyList(),
    val pinnedTaskbarApps: List<InstalledApp> = emptyList(),
    val recentApps: List<InstalledApp> = emptyList(),
    val telemetry: SystemTelemetry = SystemTelemetry(),
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val activePanel: ActivePanel = ActivePanel.NONE,
    val openWindows: List<DesktopWindowState> = emptyList(),
    val activeWindowType: DesktopWindowType? = null,
    val selectedShortcutId: String? = null,
    val searchQuery: String = "",
    val showAllAppsInStart: Boolean = false,
    val showPowerMenu: Boolean = false,
    val isLocked: Boolean = false,
    val contextMenu: ContextMenuRequest? = null,
    val toastMessage: String? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = LauncherPreferencesRepository(application)
    private val systemRepo = SystemRepository(application)

    private val installedAppsFlow = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val isLoadingAppsFlow = MutableStateFlow(true)
    private val telemetryFlow = MutableStateFlow(systemRepo.readCurrentTelemetry())

    private val uiInteractionFlow = MutableStateFlow(InteractionState())

    private data class InteractionState(
        val activePanel: ActivePanel = ActivePanel.NONE,
        val openWindows: List<DesktopWindowState> = emptyList(),
        val activeWindowType: DesktopWindowType? = null,
        val selectedShortcutId: String? = null,
        val searchQuery: String = "",
        val showAllAppsInStart: Boolean = false,
        val showPowerMenu: Boolean = false,
        val isLocked: Boolean = false,
        val contextMenu: ContextMenuRequest? = null,
        val toastMessage: String? = null
    )

    private val clockFlow: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            val now = Calendar.getInstance()
            val millisUntilNextSecond = 1000L - now.get(Calendar.MILLISECOND)
            delay(millisUntilNextSecond.coerceIn(250L, 1000L))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    val uiState: StateFlow<LauncherUiState> = combine(
        prefsRepo.preferencesFlow,
        installedAppsFlow,
        isLoadingAppsFlow,
        telemetryFlow,
        clockFlow,
        uiInteractionFlow
    ) { values ->
        val prefs = values[0] as LauncherPreferences
        @Suppress("UNCHECKED_CAST")
        val apps = values[1] as List<InstalledApp>
        val isLoading = values[2] as Boolean
        val telemetry = values[3] as SystemTelemetry
        val timeMillis = values[4] as Long
        val interaction = values[5] as InteractionState

        val appByPkg = apps.associateBy { it.packageName }

        val defaultStartSeed = computeDefaultStartSeed(apps)
        val startPackages = prefs.pinnedStartPackages.ifEmpty { defaultStartSeed }
        val pinnedStartApps = startPackages.mapNotNull { appByPkg[it] }

        val defaultTaskbarSeed = computeDefaultTaskbarSeed(apps)
        val taskbarPackages = prefs.pinnedTaskbarPackages.ifEmpty { defaultTaskbarSeed }
        val pinnedTaskbarApps = taskbarPackages.mapNotNull { appByPkg[it] }

        val recentApps = prefs.recentAppPackages.mapNotNull { appByPkg[it] }
            .ifEmpty { apps.take(4) }

        val desktopShortcuts = buildDesktopShortcuts(prefs, appByPkg)

        LauncherUiState(
            preferences = prefs,
            installedApps = apps,
            isLoadingApps = isLoading,
            desktopShortcuts = desktopShortcuts,
            pinnedStartApps = pinnedStartApps,
            pinnedTaskbarApps = pinnedTaskbarApps,
            recentApps = recentApps,
            telemetry = telemetry,
            currentTimeMillis = timeMillis,
            activePanel = interaction.activePanel,
            openWindows = interaction.openWindows,
            activeWindowType = interaction.activeWindowType,
            selectedShortcutId = interaction.selectedShortcutId,
            searchQuery = interaction.searchQuery,
            showAllAppsInStart = interaction.showAllAppsInStart,
            showPowerMenu = interaction.showPowerMenu,
            isLocked = interaction.isLocked,
            contextMenu = interaction.contextMenu,
            toastMessage = interaction.toastMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherUiState())

    init {
        refreshInstalledApps()
        viewModelScope.launch {
            systemRepo.observePackageChanges().collect {
                refreshInstalledApps()
            }
        }
        viewModelScope.launch {
            systemRepo.observeSystemTelemetry().collect { snapshot ->
                telemetryFlow.value = snapshot
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            isLoadingAppsFlow.value = true
            installedAppsFlow.value = systemRepo.loadInstalledApps()
            telemetryFlow.value = systemRepo.readCurrentTelemetry()
            isLoadingAppsFlow.value = false
        }
    }

    private fun computeDefaultStartSeed(apps: List<InstalledApp>): List<String> {
        val preferredKeywords = listOf(
            "chrome", "browser", "messages", "dialer", "phone", "contacts",
            "camera", "gallery", "photos", "settings", "vending", "play",
            "calendar", "clock", "calculator", "gmail", "mail", "maps", "youtube"
        )
        val prioritized = preferredKeywords.mapNotNull { kw ->
            apps.firstOrNull {
                it.packageName.lowercase().contains(kw) || it.label.lowercase().contains(kw)
            }?.packageName
        }.distinct()
        val combined = (prioritized + apps.map { it.packageName }).distinct()
        return combined.take(12)
    }

    private fun computeDefaultTaskbarSeed(apps: List<InstalledApp>): List<String> {
        val preferredKeywords = listOf("chrome", "browser", "dialer", "phone", "messages", "camera", "vending")
        val prioritized = preferredKeywords.mapNotNull { kw ->
            apps.firstOrNull {
                it.packageName.lowercase().contains(kw) || it.label.lowercase().contains(kw)
            }?.packageName
        }.distinct()
        return (prioritized + apps.map { it.packageName }).distinct().take(4)
    }

    private fun buildDesktopShortcuts(
        prefs: LauncherPreferences,
        appByPkg: Map<String, InstalledApp>
    ): List<DesktopShortcut> {
        val defaults = listOf(
            DesktopShortcut("sys_this_pc", "This PC", ShortcutKind.THIS_PC),
            DesktopShortcut("sys_user_files", "File Explorer", ShortcutKind.USER_FILES),
            DesktopShortcut("sys_browser", "Edge", ShortcutKind.BROWSER),
            DesktopShortcut("sys_personalize", "Personalize", ShortcutKind.SETTINGS),
            DesktopShortcut("sys_recycle_bin", "Recycle Bin", ShortcutKind.RECYCLE_BIN)
        ).filterNot { prefs.hiddenDefaultShortcuts.contains(it.id) }

        val userApps = prefs.desktopAppPackages.mapNotNull { pkg ->
            appByPkg[pkg]?.let { app ->
                DesktopShortcut(
                    id = "app_$pkg",
                    label = app.label,
                    kind = ShortcutKind.APP,
                    packageName = pkg,
                    app = app
                )
            }
        }
        val combined = defaults + userApps
        return if (prefs.sortAlphabetically) {
            combined.sortedBy { it.label.lowercase() }
        } else {
            combined
        }
    }

    fun togglePanel(panel: ActivePanel) {
        uiInteractionFlow.update { current ->
            val next = if (current.activePanel == panel) ActivePanel.NONE else panel
            current.copy(
                activePanel = next,
                searchQuery = if (next == ActivePanel.START_MENU || next == ActivePanel.SEARCH) "" else current.searchQuery,
                showAllAppsInStart = false,
                showPowerMenu = false,
                contextMenu = null
            )
        }
    }

    fun closeAllPanels() {
        uiInteractionFlow.update {
            it.copy(
                activePanel = ActivePanel.NONE,
                showPowerMenu = false,
                contextMenu = null,
                selectedShortcutId = null
            )
        }
    }

    fun selectShortcut(id: String?) {
        uiInteractionFlow.update {
            it.copy(
                selectedShortcutId = id,
                activePanel = ActivePanel.NONE,
                contextMenu = null
            )
        }
    }

    fun openShortcut(shortcut: DesktopShortcut) {
        uiInteractionFlow.update {
            it.copy(
                selectedShortcutId = shortcut.id,
                activePanel = ActivePanel.NONE,
                contextMenu = null
            )
        }
        when (shortcut.kind) {
            ShortcutKind.THIS_PC -> openWindow(DesktopWindowType.THIS_PC)
            ShortcutKind.USER_FILES -> openWindow(DesktopWindowType.USER_FILES)
            ShortcutKind.SETTINGS -> openWindow(DesktopWindowType.SETTINGS_PERSONALIZE)
            ShortcutKind.RECYCLE_BIN -> openWindow(DesktopWindowType.RECYCLE_BIN_APPS)
            ShortcutKind.BROWSER -> {
                if (!systemRepo.openDefaultBrowser()) {
                    showToast("No web browser installed")
                }
            }
            ShortcutKind.APP -> shortcut.app?.let { launchApp(it) }
        }
    }

    fun openWindow(type: DesktopWindowType) {
        uiInteractionFlow.update { current ->
            val existing = current.openWindows.firstOrNull { it.type == type }
            val updatedList = if (existing == null) {
                current.openWindows + DesktopWindowState(type = type)
            } else {
                current.openWindows.filterNot { it.type == type } + existing.copy(isMinimized = false)
            }
            current.copy(
                openWindows = updatedList,
                activeWindowType = type,
                activePanel = ActivePanel.NONE,
                contextMenu = null
            )
        }
    }

    fun focusOrToggleWindowFromTaskbar(type: DesktopWindowType) {
        uiInteractionFlow.update { current ->
            val target = current.openWindows.firstOrNull { it.type == type }
            if (target == null) {
                current.copy(
                    openWindows = current.openWindows + DesktopWindowState(type = type),
                    activeWindowType = type,
                    activePanel = ActivePanel.NONE
                )
            } else if (current.activeWindowType == type && !target.isMinimized) {
                val updated = current.openWindows.map {
                    if (it.type == type) it.copy(isMinimized = true) else it
                }
                val nextActive = updated.lastOrNull { !it.isMinimized }?.type
                current.copy(openWindows = updated, activeWindowType = nextActive, activePanel = ActivePanel.NONE)
            } else {
                val updated = current.openWindows.filterNot { it.type == type } + target.copy(isMinimized = false)
                current.copy(openWindows = updated, activeWindowType = type, activePanel = ActivePanel.NONE)
            }
        }
    }

    fun minimizeWindow(type: DesktopWindowType) {
        uiInteractionFlow.update { current ->
            val updated = current.openWindows.map {
                if (it.type == type) it.copy(isMinimized = true) else it
            }
            val nextActive = updated.lastOrNull { !it.isMinimized }?.type
            current.copy(openWindows = updated, activeWindowType = nextActive)
        }
    }

    fun toggleMaximizeWindow(type: DesktopWindowType) {
        uiInteractionFlow.update { current ->
            val updated = current.openWindows.map {
                if (it.type == type) it.copy(isMaximized = !it.isMaximized, isMinimized = false) else it
            }
            current.copy(openWindows = updated, activeWindowType = type)
        }
    }

    fun closeWindow(type: DesktopWindowType) {
        uiInteractionFlow.update { current ->
            val updated = current.openWindows.filterNot { it.type == type }
            val nextActive = updated.lastOrNull { !it.isMinimized }?.type
            current.copy(openWindows = updated, activeWindowType = nextActive)
        }
    }

    fun showDesktop() {
        uiInteractionFlow.update { current ->
            val anyVisible = current.openWindows.any { !it.isMinimized }
            val updated = if (anyVisible) {
                current.openWindows.map { it.copy(isMinimized = true) }
            } else {
                current.openWindows.map { it.copy(isMinimized = false) }
            }
            current.copy(
                openWindows = updated,
                activeWindowType = if (anyVisible) null else updated.lastOrNull()?.type,
                activePanel = ActivePanel.NONE,
                contextMenu = null,
                showPowerMenu = false
            )
        }
    }

    fun launchApp(app: InstalledApp) {
        closeAllPanels()
        if (systemRepo.launchApp(app)) {
            viewModelScope.launch {
                prefsRepo.recordRecentApp(app.packageName)
            }
        } else {
            showToast("Unable to open ${app.label}")
        }
    }

    fun updateSearchQuery(query: String) {
        uiInteractionFlow.update { it.copy(searchQuery = query) }
    }

    fun setShowAllAppsInStart(showAll: Boolean) {
        uiInteractionFlow.update { it.copy(showAllAppsInStart = showAll) }
    }

    fun togglePowerMenu() {
        uiInteractionFlow.update { it.copy(showPowerMenu = !it.showPowerMenu) }
    }

    fun lockScreen() {
        uiInteractionFlow.update {
            it.copy(
                isLocked = true,
                activePanel = ActivePanel.NONE,
                showPowerMenu = false,
                contextMenu = null
            )
        }
    }

    fun unlockScreen() {
        uiInteractionFlow.update { it.copy(isLocked = false) }
    }

    fun showDesktopBackgroundMenu() {
        uiInteractionFlow.update {
            it.copy(
                contextMenu = ContextMenuRequest(isDesktopBackground = true),
                activePanel = ActivePanel.NONE
            )
        }
    }

    fun showShortcutContextMenu(shortcut: DesktopShortcut) {
        uiInteractionFlow.update {
            it.copy(
                selectedShortcutId = shortcut.id,
                contextMenu = ContextMenuRequest(shortcut = shortcut, app = shortcut.app)
            )
        }
    }

    fun showAppContextMenu(app: InstalledApp) {
        uiInteractionFlow.update {
            it.copy(contextMenu = ContextMenuRequest(app = app))
        }
    }

    fun dismissContextMenu() {
        uiInteractionFlow.update { it.copy(contextMenu = null) }
    }

    fun togglePinToStart(packageName: String) {
        val seed = computeDefaultStartSeed(installedAppsFlow.value)
        viewModelScope.launch {
            prefsRepo.togglePinToStart(packageName, seed)
        }
        dismissContextMenu()
    }

    fun togglePinToTaskbar(packageName: String) {
        val seed = computeDefaultTaskbarSeed(installedAppsFlow.value)
        viewModelScope.launch {
            prefsRepo.togglePinToTaskbar(packageName, seed)
        }
        dismissContextMenu()
    }

    fun toggleDesktopApp(packageName: String) {
        viewModelScope.launch {
            prefsRepo.toggleDesktopApp(packageName)
        }
        dismissContextMenu()
    }

    fun removeShortcutFromDesktop(shortcut: DesktopShortcut) {
        viewModelScope.launch {
            if (shortcut.kind == ShortcutKind.APP && shortcut.packageName != null) {
                prefsRepo.toggleDesktopApp(shortcut.packageName)
            } else {
                prefsRepo.hideDefaultShortcut(shortcut.id)
            }
        }
        dismissContextMenu()
    }

    fun restoreDefaultShortcuts() {
        viewModelScope.launch { prefsRepo.restoreDefaultShortcuts() }
        showToast("Default desktop icons restored")
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch { prefsRepo.setDarkTheme(isDark) }
    }

    fun setWallpaper(option: WallpaperOption, customUri: String? = null) {
        viewModelScope.launch { prefsRepo.setWallpaper(option, customUri) }
    }

    fun setDesktopIconSize(size: DesktopIconSize) {
        viewModelScope.launch { prefsRepo.setDesktopIconSize(size) }
        dismissContextMenu()
    }

    fun setTaskbarAlignment(alignment: TaskbarAlignment) {
        viewModelScope.launch { prefsRepo.setTaskbarAlignment(alignment) }
    }

    fun setUse24HourClock(use24h: Boolean) {
        viewModelScope.launch { prefsRepo.setUse24HourClock(use24h) }
    }

    fun setShowSecondsInClock(showSeconds: Boolean) {
        viewModelScope.launch { prefsRepo.setShowSecondsInClock(showSeconds) }
    }

    fun setHideSystemStatusBar(hide: Boolean) {
        viewModelScope.launch { prefsRepo.setHideSystemStatusBar(hide) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { prefsRepo.setUserName(name) }
    }

    fun toggleSortAlphabetically() {
        val current = uiState.value.preferences.sortAlphabetically
        viewModelScope.launch { prefsRepo.setSortAlphabetically(!current) }
        dismissContextMenu()
    }

    fun setMediaVolume(level: Int) {
        systemRepo.setMediaVolume(level)
        telemetryFlow.value = systemRepo.readCurrentTelemetry()
    }

    fun toggleMute() {
        systemRepo.toggleMute()
        telemetryFlow.value = systemRepo.readCurrentTelemetry()
    }

    fun toggleFlashlight() {
        val active = systemRepo.toggleFlashlight()
        telemetryFlow.value = systemRepo.readCurrentTelemetry().copy(isFlashlightOn = active)
    }

    fun openAppInfo(packageName: String) {
        dismissContextMenu()
        systemRepo.openAppInfo(packageName)
    }

    fun openHomeLauncherSettings() = systemRepo.openHomeLauncherSettings()
    fun openWifiSettings() = systemRepo.openWifiSettings()
    fun openBluetoothSettings() = systemRepo.openBluetoothSettings()
    fun openDisplaySettings() = systemRepo.openDisplaySettings()
    fun openStorageSettings() = systemRepo.openStorageSettings()
    fun openSystemSettings() = systemRepo.openSystemSettings()
    fun openDocumentFolder(mimeType: String) = systemRepo.openDocumentFolder(mimeType)

    fun showToast(message: String) {
        uiInteractionFlow.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        uiInteractionFlow.update { it.copy(toastMessage = null) }
    }
}
