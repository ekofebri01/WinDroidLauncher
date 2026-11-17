package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.model.ActivePanel
import com.example.model.DesktopShortcut
import com.example.model.DesktopWindowType
import com.example.model.ExplorerLocation
import com.example.model.ShortcutKind
import com.example.model.WallpaperOption
import com.example.ui.components.AppManagerWindowContent
import com.example.ui.components.PersonalizeWindowContent
import com.example.ui.components.SafeHomeChooserDialog
import com.example.ui.components.ThisPcWindowContent
import com.example.ui.components.UserFilesWindowContent
import com.example.ui.components.Win11CalendarFlyout
import com.example.ui.components.Win11ContextMenuDialog
import com.example.ui.components.Win11DesktopGrid
import com.example.ui.components.Win11FileExplorerContent
import com.example.ui.components.Win11HiddenTrayFlyout
import com.example.ui.components.Win11InAppUpdateDialog
import com.example.ui.components.Win11LockScreen
import com.example.ui.components.Win11QuickSettingsFlyout
import com.example.ui.components.Win11StartMenu
import com.example.ui.components.Win11TaskViewFlyout
import com.example.ui.components.Win11Taskbar
import com.example.ui.components.Win11TaskbarPanelSelectorDialog
import com.example.ui.components.Win11WindowFrame
import com.example.ui.theme.Win11LauncherTheme
import com.example.viewmodel.FileExplorerViewModel
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private var homeIntentSeq by mutableIntStateOf(0)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            homeIntentSeq++
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val explorerViewModel: FileExplorerViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val explorerState by explorerViewModel.state.collectAsStateWithLifecycle()
            val explorerToast by explorerViewModel.toastEvent.collectAsStateWithLifecycle()
            val installPrompt by viewModel.installPromptIntent.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            val homeRoleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                viewModel.refreshHomeLauncherStatus()
            }

            val unknownSourcesLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                viewModel.onResumeCheckInstallPermission()
            }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.refreshHomeLauncherStatus()
                        viewModel.onResumeCheckInstallPermission()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            LaunchedEffect(homeIntentSeq) {
                if (homeIntentSeq > 0) {
                    viewModel.handleHomeIntent()
                }
            }

            LaunchedEffect(installPrompt) {
                installPrompt?.let { promptIntent ->
                    runCatching {
                        context.startActivity(promptIntent)
                    }.onFailure {
                        viewModel.showToast("Tidak dapat membuka prompt instalasi sistem")
                    }
                    viewModel.clearInstallPromptIntent()
                }
            }

            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri: Uri? ->
                if (uri != null) {
                    runCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    viewModel.setWallpaper(WallpaperOption.CUSTOM_URI, uri.toString())
                }
            }

            val mountFolderLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) { treeUri: Uri? ->
                if (treeUri != null) {
                    explorerViewModel.mountSafTreeUri(treeUri)
                }
            }

            val importFileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { docUri: Uri? ->
                if (docUri != null) {
                    explorerViewModel.importFileUri(docUri)
                }
            }

            // Sync system bars immersion preference
            LaunchedEffect(state.preferences.hideSystemStatusBar, state.preferences.isDarkTheme) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (state.preferences.hideSystemStatusBar) {
                    controller.hide(WindowInsetsCompat.Type.statusBars())
                } else {
                    controller.show(WindowInsetsCompat.Type.statusBars())
                }
            }

            LaunchedEffect(state.toastMessage) {
                state.toastMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            LaunchedEffect(explorerToast) {
                explorerToast?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    explorerViewModel.clearToast()
                }
            }

            // Intercept Back button cleanly for panels/windows (standard launcher behavior)
            BackHandler(
                enabled = state.isLocked ||
                    state.showSafeHomeDialog ||
                    state.activePanel != ActivePanel.NONE ||
                    state.openWindows.any { !it.isMinimized } ||
                    state.contextMenu != null
            ) {
                when {
                    state.showSafeHomeDialog -> viewModel.dismissSafeHomeDialog()
                    state.contextMenu != null -> viewModel.dismissContextMenu()
                    state.activePanel != ActivePanel.NONE -> viewModel.closeAllPanels()
                    state.activeWindowType == DesktopWindowType.FILE_EXPLORER &&
                        explorerState.activeTab.backStack.isNotEmpty() -> {
                        explorerViewModel.navigateBack()
                    }
                    state.activeWindowType != null -> {
                        val active = state.activeWindowType
                        if (active != null) viewModel.minimizeWindow(active)
                    }
                    state.isLocked -> viewModel.unlockScreen()
                }
            }

            Win11LauncherTheme(darkTheme = state.preferences.isDarkTheme) {
                Win11DesktopRoot(
                    state = state,
                    explorerState = explorerState,
                    viewModel = viewModel,
                    explorerViewModel = explorerViewModel,
                    onPickCustomWallpaper = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onMountDeviceFolder = { mountFolderLauncher.launch(null) },
                    onImportFile = { importFileLauncher.launch(arrayOf("*/*")) },
                    onLaunchSystemHomeRole = {
                        viewModel.dismissSafeHomeDialog()
                        val roleIntent = viewModel.createHomeRoleRequestIntent()
                        if (roleIntent != null) {
                            runCatching { homeRoleLauncher.launch(roleIntent) }
                                .onFailure { viewModel.openHomeLauncherSettings() }
                        } else {
                            viewModel.openHomeLauncherSettings()
                        }
                    },
                    onRequestUnknownSourcesPermission = {
                        runCatching {
                            unknownSourcesLauncher.launch(viewModel.openUnknownSourcesPermission())
                        }.onFailure {
                            viewModel.showToast("Tidak dapat membuka pengaturan izin sumber tidak dikenal")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun Win11DesktopRoot(
    state: com.example.viewmodel.LauncherUiState,
    explorerState: com.example.model.FileExplorerState,
    viewModel: LauncherViewModel,
    explorerViewModel: FileExplorerViewModel,
    onPickCustomWallpaper: () -> Unit,
    onMountDeviceFolder: () -> Unit,
    onImportFile: () -> Unit,
    onLaunchSystemHomeRole: () -> Unit,
    onRequestUnknownSourcesPermission: () -> Unit
) {
    val prefs = state.preferences
    val fallbackHomeApp = state.homeLauncherStatus.otherHomeLaunchers.firstOrNull()

    fun handleOpenShortcut(shortcut: DesktopShortcut) {
        when (shortcut.kind) {
            ShortcutKind.THIS_PC -> {
                explorerViewModel.openLocationInActiveOrNewTab(ExplorerLocation.ThisPc)
                viewModel.selectShortcut(shortcut.id)
                viewModel.openWindow(DesktopWindowType.FILE_EXPLORER)
            }
            ShortcutKind.USER_FILES -> {
                explorerViewModel.openLocationInActiveOrNewTab(ExplorerLocation.Home)
                viewModel.selectShortcut(shortcut.id)
                viewModel.openWindow(DesktopWindowType.FILE_EXPLORER)
            }
            ShortcutKind.RECYCLE_BIN -> {
                explorerViewModel.openLocationInActiveOrNewTab(ExplorerLocation.RecycleBin)
                viewModel.selectShortcut(shortcut.id)
                viewModel.openWindow(DesktopWindowType.FILE_EXPLORER)
            }
            else -> viewModel.openShortcut(shortcut)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        var showTaskbarPanelSelector by remember { mutableStateOf(false) }
        // 1. Desktop Wallpaper Layer
        WallpaperBackground(
            option = prefs.wallpaperOption,
            customUri = prefs.customWallpaperUri,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Main Desktop Workspace + Bottom Taskbar
        Column(modifier = Modifier.fillMaxSize()) {
            // Desktop Canvas (Respects status bar cutout at top)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(if (!prefs.hideSystemStatusBar) Modifier.statusBarsPadding() else Modifier)
            ) {
                // Desktop Icons Grid
                Win11DesktopGrid(
                    shortcuts = state.desktopShortcuts,
                    iconSize = prefs.desktopIconSize,
                    selectedShortcutId = state.selectedShortcutId,
                    onDesktopTap = { viewModel.closeAllPanels() },
                    onDesktopLongPress = { viewModel.showDesktopBackgroundMenu() },
                    onShortcutTap = { handleOpenShortcut(it) },
                    onShortcutLongPress = { viewModel.showShortcutContextMenu(it) }
                )

                // Open Windows Layer
                state.openWindows.filterNot { it.isMinimized }.forEachIndexed { index, winState ->
                    androidx.compose.runtime.key(winState.type) {
                        Win11WindowFrame(
                            windowState = winState,
                            isFocused = state.activeWindowType == winState.type,
                            cascadeIndex = index,
                            onFocus = { viewModel.openWindow(winState.type) },
                            onMinimize = { viewModel.minimizeWindow(winState.type) },
                            onToggleMaximize = { viewModel.toggleMaximizeWindow(winState.type) },
                            onClose = { viewModel.closeWindow(winState.type) }
                        ) {
                        when (winState.type) {
                            DesktopWindowType.FILE_EXPLORER -> Win11FileExplorerContent(
                                state = explorerState,
                                telemetry = state.telemetry,
                                installedAppsCount = state.installedApps.size,
                                onSelectTab = { explorerViewModel.selectTab(it) },
                                onAddTab = { explorerViewModel.addNewTab() },
                                onCloseTab = { explorerViewModel.closeTab(it) },
                                onNavigate = { explorerViewModel.navigateTo(it) },
                                onBack = { explorerViewModel.navigateBack() },
                                onForward = { explorerViewModel.navigateForward() },
                                onUp = { explorerViewModel.navigateUp() },
                                onRefresh = { explorerViewModel.refreshCurrentTab() },
                                onSearchChange = { explorerViewModel.updateSearchQuery(it) },
                                onSetViewMode = { explorerViewModel.setViewMode(it) },
                                onSetSortBy = { explorerViewModel.setSortBy(it) },
                                onToggleDetailsPane = { explorerViewModel.toggleDetailsPane() },
                                onItemClick = { explorerViewModel.openItem(it) },
                                onItemToggleSelect = { explorerViewModel.toggleItemSelection(it.id) },
                                onClearSelection = { explorerViewModel.clearSelection() },
                                onCreateFolder = { explorerViewModel.createNewFolder(it) },
                                onCreateTextFile = { explorerViewModel.createNewTextFile(it) },
                                onImportFile = onImportFile,
                                onMountDeviceFolder = onMountDeviceFolder,
                                onOpenMountedFolder = { explorerViewModel.openMountedSafFolder(it) },
                                onUnmountSafFolder = { explorerViewModel.unmountSafFolder(it) },
                                onRenameSelected = { explorerViewModel.renameSelectedItem(it) },
                                onCopySelected = { explorerViewModel.copySelected() },
                                onCutSelected = { explorerViewModel.cutSelected() },
                                onPaste = { explorerViewModel.pasteClipboard() },
                                onDeleteSelected = { explorerViewModel.deleteSelected() },
                                onRestoreSelected = { explorerViewModel.restoreSelectedFromTrash() },
                                onEmptyTrash = { explorerViewModel.emptyRecycleBin() },
                                onShareItem = { explorerViewModel.shareItem(it) },
                                onOpenAppManager = { viewModel.openWindow(DesktopWindowType.RECYCLE_BIN_APPS) },
                                onOpenSystemStorage = { viewModel.openStorageSettings() },
                                onSavePreviewText = { explorerViewModel.savePreviewText(it) },
                                onOpenPreviewExternally = { explorerViewModel.openExternally(it) },
                                onSetImageAsWallpaper = { item ->
                                    val uri = explorerViewModel.getFileUriForWallpaper(item)
                                    if (uri != null) {
                                        viewModel.setWallpaper(WallpaperOption.CUSTOM_URI, uri)
                                        explorerViewModel.dismissPreview()
                                        viewModel.showToast("Desktop wallpaper updated")
                                    }
                                },
                                onDismissPreview = { explorerViewModel.dismissPreview() }
                            )
                            DesktopWindowType.THIS_PC -> ThisPcWindowContent(
                                telemetry = state.telemetry,
                                installedAppsCount = state.installedApps.size,
                                onOpenStorageSettings = { viewModel.openStorageSettings() },
                                onOpenSystemSettings = { viewModel.openSystemSettings() },
                                onOpenUserFiles = {
                                    explorerViewModel.openLocationInActiveOrNewTab(ExplorerLocation.Home)
                                    viewModel.openWindow(DesktopWindowType.FILE_EXPLORER)
                                }
                            )
                            DesktopWindowType.USER_FILES -> UserFilesWindowContent(
                                onOpenMimeCategory = { viewModel.openDocumentFolder(it) },
                                onOpenStorageSettings = { viewModel.openStorageSettings() }
                            )
                            DesktopWindowType.SETTINGS_PERSONALIZE -> PersonalizeWindowContent(
                                preferences = prefs,
                                homeLauncherStatus = state.homeLauncherStatus,
                                githubUpdateState = state.githubUpdateState,
                                onSetDarkTheme = { viewModel.setDarkTheme(it) },
                                onSelectWallpaper = { viewModel.setWallpaper(it) },
                                onPickCustomWallpaper = onPickCustomWallpaper,
                                onSetDesktopIconSize = { viewModel.setDesktopIconSize(it) },
                                onSetTaskbarAlignment = { viewModel.setTaskbarAlignment(it) },
                                onSetTaskbarHeightDp = { viewModel.setTaskbarHeightDp(it) },
                                onSetTaskbarStyle = { viewModel.setTaskbarStyle(it) },
                                onSetTaskbarIconSize = { viewModel.setTaskbarIconSize(it) },
                                onSetTaskbarOpacity = { viewModel.setTaskbarOpacity(it) },
                                onSetTaskbarShowDivider = { viewModel.setTaskbarShowDivider(it) },
                                onSetTaskbarShowActiveIndicator = { viewModel.setTaskbarShowActiveIndicator(it) },
                                onSetTaskbarSearchMode = { viewModel.setTaskbarSearchMode(it) },
                                onSetShowTaskView = { viewModel.setShowTaskViewInTaskbar(it) },
                                onSetShowFileExplorer = { viewModel.setShowFileExplorerInTaskbar(it) },
                                onSetShowPinnedApps = { viewModel.setShowPinnedAppsInTaskbar(it) },
                                onSetShowHiddenTray = { viewModel.setShowHiddenTrayInTaskbar(it) },
                                onSetShowTrayNetwork = { viewModel.setShowTrayNetwork(it) },
                                onSetShowTrayVolume = { viewModel.setShowTrayVolume(it) },
                                onSetShowTrayBattery = { viewModel.setShowTrayBattery(it) },
                                onSetShowTrayBatteryPercent = { viewModel.setShowTrayBatteryPercent(it) },
                                onSetShowTrayClock = { viewModel.setShowTrayClock(it) },
                                onSetShowTrayDate = { viewModel.setShowTrayDate(it) },
                                onSetShowTrayNotificationDot = { viewModel.setShowTrayNotificationDot(it) },
                                onSetShowDesktopButton = { viewModel.setShowDesktopButtonInTaskbar(it) },
                                onResetTaskbarDefaults = { viewModel.resetTaskbarSettingsToDefault() },
                                onSet24HourClock = { viewModel.setUse24HourClock(it) },
                                onSetShowSeconds = { viewModel.setShowSecondsInClock(it) },
                                onSetHideStatusBar = { viewModel.setHideSystemStatusBar(it) },
                                onSetUserName = { viewModel.setUserName(it) },
                                onRestoreDefaults = { viewModel.restoreDefaultShortcuts() },
                                onRequestSafeHomeSwitch = {
                                    if (prefs.safeHomeGuardEnabled) {
                                        viewModel.requestSafeHomeSwitch()
                                    } else {
                                        onLaunchSystemHomeRole()
                                    }
                                },
                                onSetSafeHomeGuard = { viewModel.setSafeHomeGuard(it) },
                                onLaunchFallbackHome = { target -> viewModel.launchFallbackHomeLauncher(target) },
                                onSaveAndCheckGitHubRepo = { viewModel.saveAndCheckGitHubRepo(it) },
                                onSetAutoCheckUpdates = { viewModel.setAutoCheckUpdates(it) },
                                onCheckGitHubUpdates = { viewModel.checkForGitHubUpdates() },
                                onOpenGitHubRelease = { preferApk -> viewModel.openGitHubReleaseUrl(preferApk) },
                                onStartInAppUpdate = { viewModel.startInAppUpdate() },
                                onOpenPanelSelector = { showTaskbarPanelSelector = true }
                            )
                            DesktopWindowType.RECYCLE_BIN_APPS -> AppManagerWindowContent(
                                apps = state.installedApps,
                                desktopPackages = prefs.desktopAppPackages,
                                onLaunchApp = { viewModel.launchApp(it) },
                                onToggleDesktop = { viewModel.toggleDesktopApp(it) },
                                onOpenAppInfo = { viewModel.openAppInfo(it) }
                            )
                        }
                    }
                    }
                }

                // Scrim for dismissing open flyouts/start menu when tapping outside
                if (state.activePanel != ActivePanel.NONE) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.14f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { viewModel.closeAllPanels() }
                    )
                }

                // Start Menu / Search Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.activePanel == ActivePanel.START_MENU || state.activePanel == ActivePanel.SEARCH,
                    enter = fadeIn() + slideInVertically { it / 4 } + scaleIn(
                        initialScale = 0.94f,
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    ),
                    exit = fadeOut() + slideOutVertically { it / 5 } + scaleOut(
                        targetScale = 0.95f,
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Win11StartMenu(
                        userName = prefs.userName,
                        pinnedApps = state.pinnedStartApps,
                        allApps = state.installedApps,
                        recentApps = state.recentApps,
                        recommendedFiles = explorerState.recentFiles,
                        searchQuery = state.searchQuery,
                        showAllApps = state.showAllAppsInStart,
                        showPowerMenu = state.showPowerMenu,
                        searchFocusedMode = state.activePanel == ActivePanel.SEARCH,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onToggleAllApps = { viewModel.setShowAllAppsInStart(it) },
                        onAppClick = { viewModel.launchApp(it) },
                        onAppLongPress = { viewModel.showAppContextMenu(it) },
                        onRecommendedFileClick = { file ->
                            viewModel.openWindow(DesktopWindowType.FILE_EXPLORER)
                            explorerViewModel.openItem(file)
                        },
                        onOpenWindow = { viewModel.openWindow(it) },
                        onRetract = { viewModel.closeAllPanels() },
                        onTogglePowerMenu = { viewModel.togglePowerMenu() },
                        onLockClick = { viewModel.lockScreen() },
                        onRefreshClick = {
                            viewModel.refreshInstalledApps()
                            viewModel.closeAllPanels()
                        },
                        onHomeSettingsClick = {
                            if (prefs.safeHomeGuardEnabled) {
                                viewModel.requestSafeHomeSwitch()
                            } else {
                                viewModel.closeAllPanels()
                                onLaunchSystemHomeRole()
                            }
                        },
                        fallbackHomeLabel = fallbackHomeApp?.label,
                        onSwitchToSystemHome = { viewModel.launchFallbackHomeLauncher(fallbackHomeApp) }
                    )
                }

                // Quick Settings Flyout
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.activePanel == ActivePanel.QUICK_SETTINGS,
                    enter = fadeIn() + slideInVertically { it / 3 },
                    exit = fadeOut() + slideOutVertically { it / 3 },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Win11QuickSettingsFlyout(
                        telemetry = state.telemetry,
                        isDarkTheme = prefs.isDarkTheme,
                        onWifiClick = { viewModel.openWifiSettings() },
                        onBluetoothClick = { viewModel.openBluetoothSettings() },
                        onToggleTheme = { viewModel.setDarkTheme(!prefs.isDarkTheme) },
                        onToggleFlashlight = { viewModel.toggleFlashlight() },
                        onToggleMute = { viewModel.toggleMute() },
                        onHomeSettingsClick = {
                            if (prefs.safeHomeGuardEnabled) {
                                viewModel.requestSafeHomeSwitch()
                            } else {
                                viewModel.closeAllPanels()
                                onLaunchSystemHomeRole()
                            }
                        },
                        onDisplaySettingsClick = { viewModel.openDisplaySettings() },
                        onOpenPersonalize = { viewModel.openWindow(DesktopWindowType.SETTINGS_PERSONALIZE) },
                        onVolumeChange = { viewModel.setMediaVolume(it) }
                    )
                }

                // Calendar & Notifications Flyout
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.activePanel == ActivePanel.CALENDAR_NOTIFICATIONS,
                    enter = fadeIn() + slideInVertically { it / 3 },
                    exit = fadeOut() + slideOutVertically { it / 3 },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Win11CalendarFlyout(
                        currentTimeMillis = state.currentTimeMillis,
                        telemetry = state.telemetry,
                        githubUpdateState = state.githubUpdateState,
                        onOpenThisPc = { viewModel.openWindow(DesktopWindowType.THIS_PC) },
                        onOpenUpdateSettings = { viewModel.openWindow(DesktopWindowType.SETTINGS_PERSONALIZE) }
                    )
                }

                // Task View Flyout
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.activePanel == ActivePanel.TASK_VIEW,
                    enter = fadeIn() + scaleIn(initialScale = 0.94f),
                    exit = fadeOut() + scaleOut(targetScale = 0.95f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                ) {
                    Win11TaskViewFlyout(
                        openWindows = state.openWindows,
                        recentApps = state.recentApps,
                        onFocusWindow = { viewModel.openWindow(it) },
                        onCloseWindow = { viewModel.closeWindow(it) },
                        onLaunchApp = { viewModel.launchApp(it) }
                    )
                }

                // Hidden Tray Flyout
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.activePanel == ActivePanel.HIDDEN_TRAY,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 90.dp, bottom = 10.dp)
                ) {
                    Win11HiddenTrayFlyout(
                        isFlashlightOn = state.telemetry.isFlashlightOn,
                        isDarkTheme = prefs.isDarkTheme,
                        onToggleFlashlight = { viewModel.toggleFlashlight() },
                        onToggleTheme = { viewModel.setDarkTheme(!prefs.isDarkTheme) },
                        onRefreshApps = {
                            viewModel.refreshInstalledApps()
                            viewModel.closeAllPanels()
                        },
                        onOpenPersonalize = { viewModel.openWindow(DesktopWindowType.SETTINGS_PERSONALIZE) }
                    )
                }
            }

            // 3. Bottom Windows 11 Taskbar
            Win11Taskbar(
                activePanel = state.activePanel,
                preferences = prefs,
                pinnedApps = state.pinnedTaskbarApps,
                openWindows = state.openWindows,
                activeWindowType = state.activeWindowType,
                telemetry = state.telemetry,
                currentTimeMillis = state.currentTimeMillis,
                onStartClick = { viewModel.togglePanel(ActivePanel.START_MENU) },
                onSearchClick = { viewModel.togglePanel(ActivePanel.SEARCH) },
                onTaskViewClick = { viewModel.togglePanel(ActivePanel.TASK_VIEW) },
                onFileExplorerClick = {
                    viewModel.focusOrToggleWindowFromTaskbar(DesktopWindowType.FILE_EXPLORER)
                },
                onHiddenTrayClick = { viewModel.togglePanel(ActivePanel.HIDDEN_TRAY) },
                onQuickSettingsClick = { viewModel.togglePanel(ActivePanel.QUICK_SETTINGS) },
                onClockClick = { viewModel.togglePanel(ActivePanel.CALENDAR_NOTIFICATIONS) },
                onShowDesktopClick = { viewModel.showDesktop() },
                onPinnedAppClick = { viewModel.launchApp(it) },
                onPinnedAppLongPress = { viewModel.showAppContextMenu(it) },
                onWindowTaskbarClick = { viewModel.focusOrToggleWindowFromTaskbar(it) },
                onTaskbarLongClick = { showTaskbarPanelSelector = true }
            )
        }

        // 4. Context Menu Overlay
        state.contextMenu?.let { menuReq ->
            Win11ContextMenuDialog(
                request = menuReq,
                preferences = prefs,
                onDismiss = { viewModel.dismissContextMenu() },
                onOpenShortcut = { handleOpenShortcut(it) },
                onOpenApp = { viewModel.launchApp(it) },
                onTogglePinStart = { viewModel.togglePinToStart(it) },
                onTogglePinTaskbar = { viewModel.togglePinToTaskbar(it) },
                onToggleDesktopApp = { viewModel.toggleDesktopApp(it) },
                onRemoveShortcut = { viewModel.removeShortcutFromDesktop(it) },
                onOpenAppInfo = { viewModel.openAppInfo(it) },
                onSetIconSize = { viewModel.setDesktopIconSize(it) },
                onToggleSortAlpha = { viewModel.toggleSortAlphabetically() },
                onRefreshDesktop = { viewModel.refreshInstalledApps() },
                onOpenPersonalize = { viewModel.openWindow(DesktopWindowType.SETTINGS_PERSONALIZE) },
                onRestoreDefaultShortcuts = { viewModel.restoreDefaultShortcuts() },
                onOpenHomeSettings = {
                    if (prefs.safeHomeGuardEnabled) {
                        viewModel.requestSafeHomeSwitch()
                    } else {
                        onLaunchSystemHomeRole()
                    }
                },
                fallbackHomeLabel = fallbackHomeApp?.label,
                onSwitchToSystemHome = { viewModel.launchFallbackHomeLauncher(fallbackHomeApp) }
            )
        }

        // 5. Safe Default Home Chooser Dialog
        if (state.showSafeHomeDialog) {
            SafeHomeChooserDialog(
                status = state.homeLauncherStatus,
                onDismiss = { viewModel.dismissSafeHomeDialog() },
                onConfirmSystemHomeChooser = onLaunchSystemHomeRole,
                onOpenFallbackLauncher = { target -> viewModel.launchFallbackHomeLauncher(target) }
            )
        }

        // 6. Animated In-App APK Update & Auto-Cleanup Dialog
        Win11InAppUpdateDialog(
            updateState = state.githubUpdateState,
            onRequestInstallPermission = onRequestUnknownSourcesPermission,
            onRetryUpdate = { viewModel.startInAppUpdate() },
            onDismiss = { viewModel.dismissInAppUpdateStage() }
        )

        // 6b. Taskbar Panel Selector Dialog (Long-press taskbar or Settings button)
        if (showTaskbarPanelSelector) {
            Win11TaskbarPanelSelectorDialog(
                preferences = prefs,
                onSetTaskbarAlignment = { viewModel.setTaskbarAlignment(it) },
                onSetTaskbarStyle = { viewModel.setTaskbarStyle(it) },
                onSetTaskbarSearchMode = { viewModel.setTaskbarSearchMode(it) },
                onSetShowTaskView = { viewModel.setShowTaskViewInTaskbar(it) },
                onSetShowFileExplorer = { viewModel.setShowFileExplorerInTaskbar(it) },
                onSetShowPinnedApps = { viewModel.setShowPinnedAppsInTaskbar(it) },
                onSetShowHiddenTray = { viewModel.setShowHiddenTrayInTaskbar(it) },
                onSetShowTrayNetwork = { viewModel.setShowTrayNetwork(it) },
                onSetShowTrayVolume = { viewModel.setShowTrayVolume(it) },
                onSetShowTrayBattery = { viewModel.setShowTrayBattery(it) },
                onSetShowTrayBatteryPercent = { viewModel.setShowTrayBatteryPercent(it) },
                onSetShowTrayClock = { viewModel.setShowTrayClock(it) },
                onSetShowTrayDate = { viewModel.setShowTrayDate(it) },
                onSetShowTrayNotificationDot = { viewModel.setShowTrayNotificationDot(it) },
                onSet24HourClock = { viewModel.setUse24HourClock(it) },
                onSetShowSeconds = { viewModel.setShowSecondsInClock(it) },
                onSetShowDesktopButton = { viewModel.setShowDesktopButtonInTaskbar(it) },
                onResetTaskbarDefaults = { viewModel.resetTaskbarSettingsToDefault() },
                onDismiss = { showTaskbarPanelSelector = false }
            )
        }

        // 7. Lock Screen Overlay
        AnimatedVisibility(
            visible = state.isLocked,
            enter = fadeIn(),
            exit = fadeOut() + slideOutVertically { -it / 2 }
        ) {
            Win11LockScreen(
                currentTimeMillis = state.currentTimeMillis,
                use24HourClock = prefs.use24HourClock,
                telemetry = state.telemetry,
                userName = prefs.userName,
                onUnlock = { viewModel.unlockScreen() }
            )
        }
    }
}

@Composable
private fun WallpaperBackground(
    option: WallpaperOption,
    customUri: String?,
    modifier: Modifier = Modifier
) {
    if (option == WallpaperOption.CUSTOM_URI && !customUri.isNullOrBlank()) {
        AsyncImage(
            model = customUri,
            contentDescription = "Desktop Wallpaper",
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.img_win11_bloom_dark),
            modifier = modifier
        )
    } else {
        val resId = when (option) {
            WallpaperOption.BLOOM_LIGHT -> R.drawable.img_win11_bloom_light
            WallpaperOption.CAPTURED_FLOW -> R.drawable.img_win11_captured_flow
            else -> R.drawable.img_win11_bloom_dark
        }
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Windows 11 Wallpaper",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
