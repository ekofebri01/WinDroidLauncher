package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileExplorerRepository
import com.example.model.ClipboardMode
import com.example.model.ExplorerClipboard
import com.example.model.ExplorerItem
import com.example.model.ExplorerLocation
import com.example.model.ExplorerPreviewState
import com.example.model.ExplorerSortBy
import com.example.model.ExplorerTab
import com.example.model.ExplorerViewMode
import com.example.model.FileExplorerState
import com.example.model.FileItemKind
import com.example.model.MountedSafFolder
import com.example.model.SafBreadcrumb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FileExplorerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = FileExplorerRepository(application)

    private val _state = MutableStateFlow(FileExplorerState())
    val state: StateFlow<FileExplorerState> = _state.asStateFlow()

    private val _toastEvent = MutableStateFlow<String?>(null)
    val toastEvent: StateFlow<String?> = _toastEvent.asStateFlow()

    init {
        viewModelScope.launch {
            repo.mountedFoldersFlow.collect { mounted ->
                _state.update { it.copy(mountedSafFolders = mounted) }
            }
        }
        refreshCurrentTab()
    }

    fun openLocationInActiveOrNewTab(location: ExplorerLocation) {
        val current = _state.value
        val existingIdx = current.tabs.indexOfFirst { it.location == location }
        if (existingIdx >= 0) {
            _state.update { it.copy(activeTabIndex = existingIdx, selectedIds = emptySet(), searchQuery = "") }
        } else if (current.activeTab.location == ExplorerLocation.Home || current.tabs.size >= 5) {
            navigateTo(location)
            return
        } else {
            val newTab = ExplorerTab(
                id = "tab_${System.nanoTime()}",
                location = location
            )
            val updatedTabs = current.tabs + newTab
            _state.update {
                it.copy(
                    tabs = updatedTabs,
                    activeTabIndex = updatedTabs.lastIndex,
                    selectedIds = emptySet(),
                    searchQuery = ""
                )
            }
        }
        refreshCurrentTab()
    }

    fun addNewTab(location: ExplorerLocation = ExplorerLocation.Home) {
        _state.update { current ->
            if (current.tabs.size >= 6) return@update current
            val newTab = ExplorerTab(id = "tab_${System.nanoTime()}", location = location)
            val updated = current.tabs + newTab
            current.copy(
                tabs = updated,
                activeTabIndex = updated.lastIndex,
                selectedIds = emptySet(),
                searchQuery = ""
            )
        }
        refreshCurrentTab()
    }

    fun selectTab(index: Int) {
        _state.update { current ->
            if (index !in current.tabs.indices) return@update current
            current.copy(activeTabIndex = index, selectedIds = emptySet(), searchQuery = "")
        }
        refreshCurrentTab()
    }

    fun closeTab(index: Int) {
        _state.update { current ->
            if (current.tabs.size <= 1) {
                val reset = ExplorerTab(id = "tab_home", location = ExplorerLocation.Home)
                return@update current.copy(tabs = listOf(reset), activeTabIndex = 0, selectedIds = emptySet())
            }
            val updated = current.tabs.toMutableList().apply { removeAt(index) }
            val nextIdx = current.activeTabIndex.coerceAtMost(updated.lastIndex)
            current.copy(tabs = updated, activeTabIndex = nextIdx, selectedIds = emptySet())
        }
        refreshCurrentTab()
    }

    fun navigateTo(location: ExplorerLocation) {
        _state.update { current ->
            val idx = current.activeTabIndex
            val tab = current.activeTab
            if (tab.location == location) return@update current
            val updatedTab = tab.copy(
                location = location,
                backStack = tab.backStack + tab.location,
                forwardStack = emptyList()
            )
            val updatedTabs = current.tabs.toMutableList().apply { set(idx, updatedTab) }
            current.copy(tabs = updatedTabs, selectedIds = emptySet(), searchQuery = "")
        }
        refreshCurrentTab()
    }

    fun navigateBack() {
        _state.update { current ->
            val idx = current.activeTabIndex
            val tab = current.activeTab
            val prev = tab.backStack.lastOrNull() ?: return@update current
            val updatedTab = tab.copy(
                location = prev,
                backStack = tab.backStack.dropLast(1),
                forwardStack = tab.forwardStack + tab.location
            )
            val updatedTabs = current.tabs.toMutableList().apply { set(idx, updatedTab) }
            current.copy(tabs = updatedTabs, selectedIds = emptySet(), searchQuery = "")
        }
        refreshCurrentTab()
    }

    fun navigateForward() {
        _state.update { current ->
            val idx = current.activeTabIndex
            val tab = current.activeTab
            val next = tab.forwardStack.lastOrNull() ?: return@update current
            val updatedTab = tab.copy(
                location = next,
                backStack = tab.backStack + tab.location,
                forwardStack = tab.forwardStack.dropLast(1)
            )
            val updatedTabs = current.tabs.toMutableList().apply { set(idx, updatedTab) }
            current.copy(tabs = updatedTabs, selectedIds = emptySet(), searchQuery = "")
        }
        refreshCurrentTab()
    }

    fun navigateUp() {
        when (val loc = _state.value.activeTab.location) {
            ExplorerLocation.Home -> Unit
            ExplorerLocation.ThisPc -> navigateTo(ExplorerLocation.Home)
            ExplorerLocation.RecycleBin -> navigateTo(ExplorerLocation.Home)
            is ExplorerLocation.LocalFolder -> {
                val rel = loc.relativePath.trim('/')
                if (rel.isBlank()) {
                    navigateTo(ExplorerLocation.ThisPc)
                } else {
                    val parent = rel.substringBeforeLast('/', "")
                    val title = if (parent.isBlank()) "Local Disk (C:)" else parent.substringAfterLast('/')
                    navigateTo(ExplorerLocation.LocalFolder(parent, title))
                }
            }
            is ExplorerLocation.SafFolder -> {
                if (loc.breadcrumbs.size <= 1) {
                    navigateTo(ExplorerLocation.ThisPc)
                } else {
                    val parentTrail = loc.breadcrumbs.dropLast(1)
                    val parent = parentTrail.last()
                    navigateTo(
                        ExplorerLocation.SafFolder(
                            treeUriString = loc.treeUriString,
                            documentUriString = parent.documentUriString,
                            title = parent.title,
                            breadcrumbs = parentTrail
                        )
                    )
                }
            }
        }
    }

    fun openMountedSafFolder(mounted: MountedSafFolder) {
        val treeUri = Uri.parse(mounted.treeUriString)
        val docId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
        val docUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, docId).toString()
        navigateTo(
            ExplorerLocation.SafFolder(
                treeUriString = mounted.treeUriString,
                documentUriString = docUri,
                title = mounted.displayName,
                breadcrumbs = listOf(SafBreadcrumb(docUri, mounted.displayName))
            )
        )
    }

    fun mountSafTreeUri(treeUri: Uri) {
        viewModelScope.launch {
            repo.addMountedSafFolder(treeUri)
            _toastEvent.value = "Device folder connected"
        }
    }

    fun unmountSafFolder(treeUriString: String) {
        viewModelScope.launch {
            repo.removeMountedSafFolder(treeUriString)
            val loc = _state.value.activeTab.location
            if (loc is ExplorerLocation.SafFolder && loc.treeUriString == treeUriString) {
                navigateTo(ExplorerLocation.ThisPc)
            }
        }
    }

    fun refreshCurrentTab() {
        val snapshot = _state.value
        val location = snapshot.activeTab.location
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val items = repo.listItems(
                location = location,
                sortBy = snapshot.sortBy,
                ascending = snapshot.sortAscending,
                searchQuery = snapshot.searchQuery
            )
            val recent = repo.listRecentFiles()
            _state.update {
                it.copy(
                    currentItems = items,
                    recentFiles = recent,
                    isLoading = false
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        refreshCurrentTab()
    }

    fun setViewMode(mode: ExplorerViewMode) {
        _state.update { it.copy(viewMode = mode) }
    }

    fun setSortBy(sortBy: ExplorerSortBy) {
        _state.update { current ->
            val asc = if (current.sortBy == sortBy) !current.sortAscending else true
            current.copy(sortBy = sortBy, sortAscending = asc)
        }
        refreshCurrentTab()
    }

    fun toggleDetailsPane() {
        _state.update { it.copy(showDetailsPane = !it.showDetailsPane) }
    }

    fun toggleItemSelection(itemId: String) {
        _state.update { current ->
            val next = current.selectedIds.toMutableSet()
            if (next.contains(itemId)) next.remove(itemId) else next.add(itemId)
            current.copy(selectedIds = next)
        }
    }

    fun selectSingleItem(itemId: String) {
        _state.update { it.copy(selectedIds = setOf(itemId)) }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet()) }
    }

    fun openItem(item: ExplorerItem) {
        if (item.isDirectory) {
            when {
                item.localRelativePath != null -> {
                    navigateTo(
                        ExplorerLocation.LocalFolder(
                            relativePath = item.localRelativePath,
                            title = item.name
                        )
                    )
                }
                item.safDocumentUri != null && item.safTreeUri != null -> {
                    val cur = _state.value.activeTab.location
                    val trail = if (cur is ExplorerLocation.SafFolder) {
                        cur.breadcrumbs + SafBreadcrumb(item.safDocumentUri, item.name)
                    } else {
                        listOf(SafBreadcrumb(item.safDocumentUri, item.name))
                    }
                    navigateTo(
                        ExplorerLocation.SafFolder(
                            treeUriString = item.safTreeUri,
                            documentUriString = item.safDocumentUri,
                            title = item.name,
                            breadcrumbs = trail
                        )
                    )
                }
            }
            return
        }

        // File opening / previewing
        when (item.kind) {
            FileItemKind.TEXT -> {
                viewModelScope.launch {
                    val content = repo.readTextContent(item)
                    if (content != null) {
                        _state.update {
                            it.copy(
                                previewState = ExplorerPreviewState(
                                    item = item,
                                    textContent = content,
                                    isEditableText = true
                                )
                            )
                        }
                    } else if (!repo.openExternally(item)) {
                        _toastEvent.value = "Cannot open ${item.name}"
                    }
                }
            }
            FileItemKind.IMAGE -> {
                val uri = repo.getShareOrViewUri(item)?.toString()
                if (uri != null) {
                    _state.update {
                        it.copy(
                            previewState = ExplorerPreviewState(
                                item = item,
                                imageUriString = uri
                            )
                        )
                    }
                } else {
                    repo.openExternally(item)
                }
            }
            else -> {
                if (!repo.openExternally(item)) {
                    _toastEvent.value = "No app installed to open ${item.name}"
                }
            }
        }
    }

    fun dismissPreview() {
        _state.update { it.copy(previewState = null) }
    }

    fun savePreviewText(newContent: String) {
        val preview = _state.value.previewState ?: return
        viewModelScope.launch {
            if (repo.writeTextContent(preview.item, newContent)) {
                _toastEvent.value = "Saved ${preview.item.name}"
                _state.update { it.copy(previewState = null) }
                refreshCurrentTab()
            } else {
                _toastEvent.value = "Failed to save file"
            }
        }
    }

    fun openExternally(item: ExplorerItem) {
        if (!repo.openExternally(item)) {
            _toastEvent.value = "No external app available for ${item.name}"
        }
    }

    fun shareItem(item: ExplorerItem) {
        if (!repo.shareItem(item)) {
            _toastEvent.value = "Unable to share ${item.name}"
        }
    }

    fun createNewFolder(name: String) {
        val loc = ensureWritableLocation() ?: return
        viewModelScope.launch {
            if (repo.createFolder(loc, name)) {
                refreshCurrentTab()
            } else {
                _toastEvent.value = "Could not create folder"
            }
        }
    }

    fun createNewTextFile(name: String) {
        val loc = ensureWritableLocation() ?: return
        viewModelScope.launch {
            if (repo.createTextFile(loc, name, "")) {
                refreshCurrentTab()
            } else {
                _toastEvent.value = "Could not create file"
            }
        }
    }

    fun importFileUri(uri: Uri) {
        val loc = _state.value.activeTab.location
        val targetRel = if (loc is ExplorerLocation.LocalFolder) loc.relativePath else "Documents"
        viewModelScope.launch {
            if (repo.importUriToLocalFolder(uri, targetRel)) {
                if (loc !is ExplorerLocation.LocalFolder) {
                    navigateTo(ExplorerLocation.LocalFolder("Documents", "Documents"))
                } else {
                    refreshCurrentTab()
                }
                _toastEvent.value = "File imported"
            } else {
                _toastEvent.value = "Import failed"
            }
        }
    }

    private fun ensureWritableLocation(): ExplorerLocation? {
        val loc = _state.value.activeTab.location
        return when (loc) {
            is ExplorerLocation.LocalFolder, is ExplorerLocation.SafFolder -> loc
            else -> {
                val fallback = ExplorerLocation.LocalFolder("Documents", "Documents")
                navigateTo(fallback)
                fallback
            }
        }
    }

    fun renameSelectedItem(newName: String) {
        val selected = selectedItems().firstOrNull() ?: return
        viewModelScope.launch {
            if (repo.renameItem(selected, newName)) {
                _state.update { it.copy(selectedIds = emptySet()) }
                refreshCurrentTab()
            } else {
                _toastEvent.value = "Rename failed"
            }
        }
    }

    fun copySelected() {
        val items = selectedItems()
        if (items.isEmpty()) return
        _state.update {
            it.copy(clipboard = ExplorerClipboard(items = items, mode = ClipboardMode.COPY))
        }
        _toastEvent.value = "${items.size} item(s) copied"
    }

    fun cutSelected() {
        val items = selectedItems()
        if (items.isEmpty()) return
        _state.update {
            it.copy(clipboard = ExplorerClipboard(items = items, mode = ClipboardMode.CUT))
        }
        _toastEvent.value = "${items.size} item(s) cut"
    }

    fun pasteClipboard() {
        val clip = _state.value.clipboard ?: return
        val loc = _state.value.activeTab.location
        if (loc !is ExplorerLocation.LocalFolder) {
            _toastEvent.value = "Open a Local Disk folder to paste"
            return
        }
        viewModelScope.launch {
            if (repo.pasteItems(clip.items, clip.mode == ClipboardMode.CUT, loc)) {
                if (clip.mode == ClipboardMode.CUT) {
                    _state.update { it.copy(clipboard = null) }
                }
                refreshCurrentTab()
                _toastEvent.value = "Pasted ${clip.items.size} item(s)"
            } else {
                _toastEvent.value = "Paste failed"
            }
        }
    }

    fun deleteSelected() {
        val items = selectedItems()
        if (items.isEmpty()) return
        val inTrash = _state.value.activeTab.location == ExplorerLocation.RecycleBin
        viewModelScope.launch {
            repo.deleteItems(items, fromRecycleBin = inTrash)
            _state.update { it.copy(selectedIds = emptySet()) }
            refreshCurrentTab()
            _toastEvent.value = if (inTrash) "Deleted permanently" else "Moved to Recycle Bin"
        }
    }

    fun restoreSelectedFromTrash() {
        val items = selectedItems()
        if (items.isEmpty()) return
        viewModelScope.launch {
            repo.restoreFromRecycleBin(items)
            _state.update { it.copy(selectedIds = emptySet()) }
            refreshCurrentTab()
            _toastEvent.value = "Restored ${items.size} item(s)"
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            repo.emptyRecycleBin()
            _state.update { it.copy(selectedIds = emptySet()) }
            refreshCurrentTab()
            _toastEvent.value = "Recycle Bin emptied"
        }
    }

    fun selectedItems(): List<ExplorerItem> {
        val ids = _state.value.selectedIds
        return _state.value.currentItems.filter { ids.contains(it.id) }
    }

    fun getFileUriForWallpaper(item: ExplorerItem): String? =
        repo.getShareOrViewUri(item)?.toString()

    fun clearToast() {
        _toastEvent.value = null
    }
}
