package com.example.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.ApkInstallerManager
import com.example.data.GitHubUpdateRepository
import com.example.data.LauncherPreferencesRepository
import com.example.data.SystemRepository
import com.example.model.ActivePanel
import com.example.model.ContextMenuRequest
import com.example.model.DesktopIconSize
import com.example.model.DesktopShortcut
import com.example.model.DesktopWindowState
import com.example.model.DesktopWindowType
import com.example.model.GitHubUpdateState
import com.example.model.GitHubUpdateStatus
import com.example.model.HomeLauncherStatus
import com.example.model.InAppUpdateStage
import com.example.model.InstalledApp
import com.example.model.LauncherPreferences
import com.example.model.ShortcutKind
import com.example.model.SystemTelemetry
import com.example.model.TaskbarAlignment
import com.example.model.TaskbarIconSize
import com.example.model.TaskbarSearchMode
import com.example.model.TaskbarStyle
import com.example.model.WallpaperOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    val showSafeHomeDialog: Boolean = false,
    val homeLauncherStatus: HomeLauncherStatus = HomeLauncherStatus(),
    val githubUpdateState: GitHubUpdateState = GitHubUpdateState(currentVersion = BuildConfig.VERSION_NAME),
    val toastMessage: String? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = LauncherPreferencesRepository(application)
    private val systemRepo = SystemRepository(application)
    private val githubUpdateRepo = GitHubUpdateRepository()
    private val apkInstaller = ApkInstallerManager(application)

    private val installedAppsFlow = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val isLoadingAppsFlow = MutableStateFlow(true)
    private val telemetryFlow = MutableStateFlow(systemRepo.readCurrentTelemetry())
    private val homeStatusFlow = MutableStateFlow(systemRepo.readHomeLauncherStatus())
    private val githubUpdateFlow = MutableStateFlow(
        GitHubUpdateState(currentVersion = BuildConfig.VERSION_NAME)
    )
    private val _installPromptIntent = MutableStateFlow<Intent?>(null)
    val installPromptIntent: StateFlow<Intent?> = _installPromptIntent

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
        val showSafeHomeDialog: Boolean = false,
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
        homeStatusFlow,
        githubUpdateFlow,
        uiInteractionFlow
    ) { values ->
        val prefs = values[0] as LauncherPreferences
        @Suppress("UNCHECKED_CAST")
        val apps = values[1] as List<InstalledApp>
        val isLoading = values[2] as Boolean
        val telemetry = values[3] as SystemTelemetry
        val timeMillis = values[4] as Long
        val homeStatus = values[5] as HomeLauncherStatus
        val updateState = values[6] as GitHubUpdateState
        val interaction = values[7] as InteractionState

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
            showSafeHomeDialog = interaction.showSafeHomeDialog,
            homeLauncherStatus = homeStatus,
            githubUpdateState = updateState,
            toastMessage = interaction.toastMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherUiState())

    init {
        refreshInstalledApps()
        refreshHomeLauncherStatus()
        apkInstaller.registerListeners(
            onProgress = { progress ->
                githubUpdateFlow.update { st ->
                    if (st.inAppStage == InAppUpdateStage.INSTALLING) {
                        st.copy(installProgress = progress.coerceIn(0f, 1f))
                    } else st
                }
            },
            onUserAction = { intent ->
                _installPromptIntent.value = intent
            },
            onSuccess = {
                viewModelScope.launch {
                    completeInstallAndCleanup()
                }
            },
            onFailure = { errorMsg ->
                val apkPath = githubUpdateFlow.value.downloadedApkPath
                githubUpdateRepo.cleanupUpdateFiles(getApplication(), apkPath)
                viewModelScope.launch { prefsRepo.setPendingUpdateCleanupPath(null) }
                githubUpdateFlow.update {
                    it.copy(
                        inAppStage = InAppUpdateStage.FAILED,
                        errorMessage = errorMsg,
                        downloadedApkPath = null
                    )
                }
            }
        )
        viewModelScope.launch {
            val pendingPath = prefsRepo.pendingUpdateCleanupFlow.first()
            if (!pendingPath.isNullOrBlank()) {
                githubUpdateRepo.cleanupUpdateFiles(getApplication(), pendingPath)
                prefsRepo.setPendingUpdateCleanupPath(null)
                githubUpdateFlow.update {
                    it.copy(
                        status = GitHubUpdateStatus.UP_TO_DATE,
                        inAppStage = InAppUpdateStage.INSTALLED_SUCCESS,
                        installProgress = 1f,
                        cleanedUpAfterInstall = true,
                        downloadedApkPath = null
                    )
                }
            } else {
                // Ensure no stray update files remain in cache
                githubUpdateRepo.cleanupUpdateFiles(getApplication())
            }
            val initialPrefs = prefsRepo.preferencesFlow.first()
            if (initialPrefs.autoCheckUpdates && initialPrefs.githubRepoSlug.isNotBlank()) {
                checkForGitHubUpdates(initialPrefs.githubRepoSlug, silent = true)
            }
        }
        viewModelScope.launch {
            systemRepo.observePackageChanges().collect {
                refreshInstalledApps()
                refreshHomeLauncherStatus()
            }
        }
        viewModelScope.launch {
            systemRepo.observeSystemTelemetry().collect { snapshot ->
                telemetryFlow.value = snapshot
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        apkInstaller.dispose()
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

    fun setTaskbarHeightDp(heightDp: Int) {
        viewModelScope.launch { prefsRepo.setTaskbarHeightDp(heightDp) }
    }

    fun setTaskbarStyle(style: TaskbarStyle) {
        viewModelScope.launch { prefsRepo.setTaskbarStyle(style) }
    }

    fun setTaskbarIconSize(iconSize: TaskbarIconSize) {
        viewModelScope.launch { prefsRepo.setTaskbarIconSize(iconSize) }
    }

    fun setTaskbarOpacity(opacity: Float) {
        viewModelScope.launch { prefsRepo.setTaskbarOpacity(opacity) }
    }

    fun setTaskbarShowDivider(show: Boolean) {
        viewModelScope.launch { prefsRepo.setTaskbarShowDivider(show) }
    }

    fun setTaskbarShowActiveIndicator(show: Boolean) {
        viewModelScope.launch { prefsRepo.setTaskbarShowActiveIndicator(show) }
    }

    fun setTaskbarSearchMode(mode: TaskbarSearchMode) {
        viewModelScope.launch { prefsRepo.setTaskbarSearchMode(mode) }
    }

    fun setShowTaskViewInTaskbar(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowTaskViewInTaskbar(show) }
    }

    fun setShowFileExplorerInTaskbar(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowFileExplorerInTaskbar(show) }
    }

    fun setShowPinnedAppsInTaskbar(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowPinnedAppsInTaskbar(show) }
    }

    fun setShowHiddenTrayInTaskbar(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowHiddenTrayInTaskbar(show) }
    }

    fun setShowTrayNetwork(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowTrayNetwork(show) }
    }

    fun setShowTrayVolume(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowTrayVolume(show) }
    }

    fun setShowTrayBattery(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowTrayBattery(show) }
    }

    fun setShowTrayBatteryPercent(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowTrayBatteryPercent(show) }
    }

    fun setShowTrayClock(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowTrayClock(show) }
    }

    fun setShowTrayDate(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowTrayDate(show) }
    }

    fun setShowTrayNotificationDot(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowTrayNotificationDot(show) }
    }

    fun setShowDesktopButtonInTaskbar(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowDesktopButtonInTaskbar(show) }
    }

    fun resetTaskbarSettingsToDefault() {
        viewModelScope.launch { prefsRepo.resetTaskbarSettingsToDefault() }
        showToast("Pengaturan taskbar dikembalikan ke default")
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

    fun refreshHomeLauncherStatus() {
        homeStatusFlow.value = systemRepo.readHomeLauncherStatus()
    }

    fun requestSafeHomeSwitch() {
        refreshHomeLauncherStatus()
        uiInteractionFlow.update {
            it.copy(
                activePanel = ActivePanel.NONE,
                showPowerMenu = false,
                contextMenu = null,
                showSafeHomeDialog = true
            )
        }
    }

    fun dismissSafeHomeDialog() {
        uiInteractionFlow.update { it.copy(showSafeHomeDialog = false) }
    }

    fun setSafeHomeGuard(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setSafeHomeGuard(enabled) }
    }

    fun createHomeRoleRequestIntent(): Intent? = systemRepo.createHomeRoleRequestIntent()

    fun launchFallbackHomeLauncher(target: InstalledApp? = null) {
        val candidate = target ?: homeStatusFlow.value.otherHomeLaunchers.firstOrNull()
        dismissSafeHomeDialog()
        closeAllPanels()
        if (candidate != null && systemRepo.launchFallbackHomeLauncher(candidate)) {
            showToast("Beralih ke ${candidate.label}")
        } else {
            systemRepo.openHomeLauncherSettings()
        }
    }

    fun handleHomeIntent() {
        uiInteractionFlow.update {
            it.copy(
                activePanel = ActivePanel.NONE,
                showPowerMenu = false,
                contextMenu = null,
                showSafeHomeDialog = false,
                isLocked = false
            )
        }
        refreshHomeLauncherStatus()
    }

    fun saveAndCheckGitHubRepo(rawRepo: String) {
        val normalized = GitHubUpdateRepository.normalizeGitHubRepoSlug(rawRepo)
        if (normalized == null) {
            githubUpdateFlow.value = GitHubUpdateState(
                status = GitHubUpdateStatus.NOT_CONFIGURED,
                currentVersion = BuildConfig.VERSION_NAME,
                errorMessage = "Format repo tidak valid. Gunakan format owner/repo (contoh: torvalds/linux)."
            )
            return
        }
        viewModelScope.launch {
            prefsRepo.setGitHubRepoSlug(normalized)
            checkForGitHubUpdates(normalized)
        }
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setAutoCheckUpdates(enabled) }
    }

    fun checkForGitHubUpdates(repoOverride: String? = null, silent: Boolean = false) {
        val targetRepo = (repoOverride ?: uiState.value.preferences.githubRepoSlug).trim()
        viewModelScope.launch {
            githubUpdateFlow.update {
                it.copy(
                    status = GitHubUpdateStatus.CHECKING,
                    currentVersion = BuildConfig.VERSION_NAME,
                    errorMessage = null
                )
            }
            val result = githubUpdateRepo.checkLatestUpdate(targetRepo)
            githubUpdateFlow.value = result
            if (!silent) {
                when (result.status) {
                    GitHubUpdateStatus.UPDATE_AVAILABLE ->
                        showToast("Update GitHub tersedia: ${result.latestTag}")
                    GitHubUpdateStatus.UP_TO_DATE ->
                        showToast("Aplikasi sudah versi terbaru (${result.currentVersion})")
                    GitHubUpdateStatus.NOT_CONFIGURED ->
                        showToast("Isi repositori GitHub (owner/repo) terlebih dahulu")
                    GitHubUpdateStatus.ERROR ->
                        showToast(result.errorMessage ?: "Gagal cek update GitHub")
                    else -> Unit
                }
            }
        }
    }

    fun openGitHubReleaseUrl(preferApkAsset: Boolean = false) {
        val st = githubUpdateFlow.value
        val url = (if (preferApkAsset) st.apkAssetUrl else null) ?: st.releaseUrl
        if (url != null) {
            if (!systemRepo.openExternalUrl(url)) {
                showToast("Tidak ada browser untuk membuka tautan rilis")
            }
        }
    }

    fun startInAppUpdate() {
        val current = githubUpdateFlow.value
        val apkUrl = current.apkAssetUrl
        if (apkUrl.isNullOrBlank()) {
            showToast("Rilis ini belum memiliki file .apk, membuka halaman GitHub")
            openGitHubReleaseUrl(preferApkAsset = false)
            return
        }
        if (!apkInstaller.canRequestPackageInstalls()) {
            githubUpdateFlow.update {
                it.copy(
                    inAppStage = InAppUpdateStage.PERMISSION_REQUIRED,
                    errorMessage = null
                )
            }
            return
        }
        downloadAndInstallUpdate(apkUrl, current.latestTag ?: "latest")
    }

    fun openUnknownSourcesPermission(): Intent = apkInstaller.createUnknownSourcesIntent()

    fun onResumeCheckInstallPermission() {
        if (githubUpdateFlow.value.inAppStage == InAppUpdateStage.PERMISSION_REQUIRED &&
            apkInstaller.canRequestPackageInstalls()
        ) {
            startInAppUpdate()
        }
    }

    private fun downloadAndInstallUpdate(apkUrl: String, versionTag: String) {
        viewModelScope.launch {
            githubUpdateFlow.update {
                it.copy(
                    inAppStage = InAppUpdateStage.DOWNLOADING,
                    downloadProgress = 0f,
                    downloadedBytes = 0L,
                    totalBytes = it.apkSizeBytes,
                    installProgress = 0f,
                    errorMessage = null,
                    cleanedUpAfterInstall = false
                )
            }
            val downloadResult = runCatching {
                githubUpdateRepo.downloadApk(
                    context = getApplication(),
                    apkUrl = apkUrl,
                    versionTag = versionTag
                ) { downloaded, total ->
                    val effectiveTotal = if (total > 0L) total else githubUpdateFlow.value.apkSizeBytes
                    val fraction = if (effectiveTotal > 0L) {
                        (downloaded.toFloat() / effectiveTotal.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    githubUpdateFlow.update { st ->
                        st.copy(
                            downloadedBytes = downloaded,
                            totalBytes = effectiveTotal,
                            downloadProgress = fraction
                        )
                    }
                }
            }

            val apkFile = downloadResult.getOrNull()
            if (apkFile == null) {
                val err = downloadResult.exceptionOrNull()?.message ?: "Gagal mengunduh file update"
                githubUpdateRepo.cleanupUpdateFiles(getApplication())
                githubUpdateFlow.update {
                    it.copy(
                        inAppStage = InAppUpdateStage.FAILED,
                        errorMessage = err
                    )
                }
                return@launch
            }

            githubUpdateFlow.update {
                it.copy(
                    inAppStage = InAppUpdateStage.VERIFYING,
                    downloadProgress = 1f,
                    downloadedApkPath = apkFile.absolutePath
                )
            }
            prefsRepo.setPendingUpdateCleanupPath(apkFile.absolutePath)
            delay(450L)

            githubUpdateFlow.update {
                it.copy(
                    inAppStage = InAppUpdateStage.INSTALLING,
                    installProgress = 0.1f
                )
            }
            val installRes = apkInstaller.installApk(apkFile)
            if (installRes.isFailure) {
                val err = installRes.exceptionOrNull()?.message ?: "Gagal memulai proses instalasi"
                githubUpdateRepo.cleanupUpdateFiles(getApplication(), apkFile.absolutePath)
                prefsRepo.setPendingUpdateCleanupPath(null)
                githubUpdateFlow.update {
                    it.copy(
                        inAppStage = InAppUpdateStage.FAILED,
                        errorMessage = err,
                        downloadedApkPath = null
                    )
                }
            }
        }
    }

    private suspend fun completeInstallAndCleanup() {
        val apkPath = githubUpdateFlow.value.downloadedApkPath
        githubUpdateFlow.update {
            it.copy(
                inAppStage = InAppUpdateStage.CLEANING_UP,
                installProgress = 0.95f
            )
        }
        delay(350L)
        githubUpdateRepo.cleanupUpdateFiles(getApplication(), apkPath)
        prefsRepo.setPendingUpdateCleanupPath(null)
        githubUpdateFlow.update {
            it.copy(
                status = GitHubUpdateStatus.UP_TO_DATE,
                inAppStage = InAppUpdateStage.INSTALLED_SUCCESS,
                installProgress = 1f,
                downloadedApkPath = null,
                cleanedUpAfterInstall = true
            )
        }
    }

    fun clearInstallPromptIntent() {
        _installPromptIntent.value = null
    }

    fun dismissInAppUpdateStage() {
        val stage = githubUpdateFlow.value.inAppStage
        if (stage == InAppUpdateStage.FAILED ||
            stage == InAppUpdateStage.INSTALLED_SUCCESS ||
            stage == InAppUpdateStage.PERMISSION_REQUIRED
        ) {
            githubUpdateRepo.cleanupUpdateFiles(getApplication(), githubUpdateFlow.value.downloadedApkPath)
            viewModelScope.launch { prefsRepo.setPendingUpdateCleanupPath(null) }
            githubUpdateFlow.update {
                it.copy(
                    inAppStage = InAppUpdateStage.NONE,
                    downloadedApkPath = null,
                    errorMessage = if (stage == InAppUpdateStage.FAILED) it.errorMessage else null
                )
            }
        }
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
